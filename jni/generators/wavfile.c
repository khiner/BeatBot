#include "../all.h"

static char tempChar;
static int i;

static inline float bytesToFloat(unsigned char firstByte,
		unsigned char secondByte) {
	// convert two bytes to one short (little endian)
	short s = (secondByte << 8) | firstByte;
	// convert to range from -1 to (just below) 1
	return s / 32768.0;
}

static inline int fileExists(char *filename) {
	struct stat buffer;
	return (stat(filename, &buffer) == 0);
}

static inline char nextChar(FILE *file) {
	fscanf(file, "%c", &tempChar);
	return tempChar;
}

void wavfile_freeBuffers(WavFile *wavFile) {
	// if this sample was short enough, it was also loaded into memory, and will be non-null
	if (wavFile->samples != NULL ) {
		// use temp sample so there is no period when wavFile->samples is non-NULL but has freed memory
		float **temp = wavFile->samples;
		wavFile->samples = NULL;
		free(temp[0]);
		free(temp[1]);
		free(temp);
		temp = NULL;
	}
}

void wavfile_setSampleFile(WavFile *config, const char *sampleFileName) {
	// if a different sample was already loaded, destroy it.
	wavfile_freeBuffers(config);

	FILE *wavFile = fopen(sampleFileName, "rb");
	fseek(wavFile, 0, SEEK_END);
	int length = ftell(wavFile);

	// Determine if mono or stereo
	fseek(wavFile, 22, SEEK_SET); // Forget byte 23 as 99.999% of WAVs are 1 or 2 channels
	config->channels = nextChar(wavFile);

	// Get past all the other sub chunks to get to the data subchunk:
	fseek(wavFile, 12, SEEK_SET); // First Subchunk ID from 12 to 16

	char first = nextChar(wavFile);
	char second = nextChar(wavFile);
	char third = nextChar(wavFile);
	char fourth = nextChar(wavFile);

	// Keep iterating until we find the data chunk (i.e. 64 61 74 61 ...... (i.e. 100 97 116 97 in decimal))
	while (first != 100 || second != 97 || third != 116 || fourth != 97) {
		int chunkSize = nextChar(wavFile) + nextChar(wavFile) * 256
				+ nextChar(wavFile) * 65536 + nextChar(wavFile) * 16777216;
		fseek(wavFile, chunkSize, SEEK_CUR);

		first = nextChar(wavFile);
		second = nextChar(wavFile);
		third = nextChar(wavFile);
		fourth = nextChar(wavFile);
	}
	fseek(wavFile, 4, SEEK_CUR);

	if (config->channels == 1) {
		length -= 32; // don't know why the end of mono file is garbage
	}

	// pos is now positioned to start of actual sound data.
	// 2 bytes per sample per channel
	config->totalSamples = (length - ftell(wavFile)) / (2 * config->channels);

	if (config->totalSamples >= SAMPLE_RATE * 5) {
		/** allocate memory to hold samples (memory is freed in wavfile_destroy)
		 *
		 * NOTE: We don't directly write to wavFile sample buffer because we want to
		 * completely load all samples from file before making wavFile->samples non-NULL.
		 * That way, we can still immediately read from file on a per-sample basis, until
		 * wavFile->samples is non-null, at which point we can read directly from memory.
		 */
		float **tempSamples = malloc(2 * sizeof(void *));
		tempSamples[0] = malloc(config->totalSamples * sizeof(float));
		// (right channel will be null if only mono sound)
		tempSamples[1] =
				config->channels == 2 ?
						malloc(config->totalSamples * sizeof(float)) : NULL;

		// write to wavFile float buffers
		for (i = 0; i < config->totalSamples; i++) {
			tempSamples[0][i] = bytesToFloat(nextChar(wavFile), nextChar(wavFile));
			if (config->channels == 2) {
				tempSamples[1][i] = bytesToFloat(nextChar(wavFile), nextChar(wavFile));
			}
		}
		// make wavFile->samples point to fully-loaded sample buffer in memory
		config->samples = tempSamples;
		// cleanup
		tempSamples = NULL;
	} else { // file too big for memory. write to temp file in external storage
		// concat sampleFileName with ".raw" extension
		const char* extension = ".raw";
		config->sampleFileName = malloc(strlen(sampleFileName) + 1 + 4);
		strcpy(config->sampleFileName, sampleFileName); /* copy name into the new var */
		strcat(config->sampleFileName, extension); /* add the extension */

		if (!fileExists(config->sampleFileName)) {
			// open *.raw file next to *.wav file - we will read directly from this file
			FILE *tempFile = fopen(config->sampleFileName, "wb");

			// copy all wav bytes to floats on disk
			for (i = 0; i < config->totalSamples; i++) {
				float samp = bytesToFloat(nextChar(wavFile), nextChar(wavFile));
				fwrite(&samp, sizeof(float), 1, tempFile);
				if (config->channels == 2) {
					samp = bytesToFloat(nextChar(wavFile), nextChar(wavFile));
					fwrite(&samp, sizeof(float), 1, tempFile);
				}
			}
			fflush(tempFile);
			fclose(tempFile);
		}
		config->sampleFile = fopen(config->sampleFileName, "rb");
	}

	fclose(wavFile);

	// init loop / currSample position data
	config->loopBegin = 0;
	config->loopEnd = config->totalSamples;
	if (config->currSample >= config->loopEnd) {
		// TODO maybe should be currSample = loopEnd
		// TODO this could cause unwanted repeat
		config->currSample = 0;
	}
	config->loopLength = config->loopEnd - config->loopBegin;
}

WavFile *wavfile_create(const char *sampleName) {
	WavFile *wavFile = (WavFile *) malloc(sizeof(WavFile));
	wavFile->currSample = 0;
	wavFile->gain = 1;
	wavFile->samples = NULL;
	wavFile->sampleFile = NULL;
	wavfile_setSampleFile(wavFile, sampleName);
	wavFile->looping = wavFile->reverse = false;
	wavFile->adsr = adsrconfig_create();
	return wavFile;
}

float wavfile_getSample(WavFile *wavFile, int sampleIndex, int channel) {
	float ret;
	if (wavFile->samples != NULL ) {
		ret = wavFile->samples[channel][sampleIndex];
	} else {
		fseek(wavFile->sampleFile,
				sampleIndex * wavFile->channels * ONE_FLOAT_SZ, SEEK_SET);
		fread(&ret, 1, ONE_FLOAT_SZ, wavFile->sampleFile);
	}
	return ret * wavFile->gain;
}

void wavfile_setLoopWindow(WavFile *wavFile, long loopBeginSample,
		long loopEndSample) {
	if (wavFile->loopBegin == loopBeginSample
			&& wavFile->loopEnd == loopEndSample)
		return;
	wavFile->loopBegin = loopBeginSample;
	wavFile->loopEnd = loopEndSample;
	wavFile->loopLength = wavFile->loopEnd - wavFile->loopBegin;
}

void wavfile_setReverse(WavFile *wavFile, bool reverse) {
	wavFile->reverse = reverse;
	// if the track is not looping, the wavFile generator will not loop to the beginning/end
	// after enabling/disabling reverse
	if (reverse && wavFile->currSample == wavFile->loopBegin)
		wavFile->currSample = wavFile->loopEnd;
	else if (!reverse && wavFile->currSample == wavFile->loopEnd)
		wavFile->currSample = wavFile->loopBegin;
}

void wavfile_reset(WavFile *config) {
	config->currSample = config->reverse ? config->loopEnd : config->loopBegin;
	resetAdsr(config->adsr);
}

void wavfile_destroy(void *p) {
	WavFile *wavFile = (WavFile *) p;
	if (wavFile->sampleFile != NULL ) {
		fflush(wavFile->sampleFile);
		fclose(wavFile->sampleFile);
		wavFile->sampleFile = NULL;
		free(wavFile->sampleFileName);
	}
	wavfile_freeBuffers(wavFile);
	adsrconfig_destroy(wavFile->adsr);
	free(wavFile);
	wavFile = NULL;
}
