#include "../all.h"

static inline float bytesToFloat(unsigned char firstByte,
		unsigned char secondByte) {
	// convert two bytes to one short (little endian)
	short s = (secondByte << 8) | firstByte;
	// convert to range from -1 to (just below) 1
	return s / 32768.0;
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

void wavfile_setSampleFile(WavFile *wavFile, const char *sampleFileName) {

	// if a different sample was already loaded, destroy it.
	wavfile_freeBuffers(wavFile);

	FILE *file = fopen(sampleFileName, "rb");

	fseek(file, 0, SEEK_END);
	int length = ftell(file);
	fseek(file, 0, SEEK_SET);

	__android_log_print(ANDROID_LOG_ERROR, "wavfile.c", "fileLength = %d", length);
	/* allocate memory for entire content */
	char *wav = calloc(1, length + 1);

	/* copy the file into the buffer */
	fread(wav, length, 1, file);
	fclose(file);

	// Determine if mono or stereo
	int channels = wav[22]; // Forget byte 23 as 99.999% of WAVs are 1 or 2 channels

	// Get past all the other sub chunks to get to the data subchunk:
	int pos = 12; // First Subchunk ID from 12 to 16

	// Keep iterating until we find the data chunk (i.e. 64 61 74 61 ...... (i.e. 100 97 116 97 in decimal))
	while (wav[pos] != 100 || wav[pos + 1] != 97 || wav[pos + 2] != 116
			|| wav[pos + 3] != 97) {
		pos += 4;
		int chunkSize = wav[pos] + wav[pos + 1] * 256 + wav[pos + 2] * 65536
				+ wav[pos + 3] * 16777216;
		pos += 4 + chunkSize;
	}
	pos += 8;

	if (channels == 1) {
		length -= 32; // don't know why the end of mono sample is garbage
	}

	// pos is now positioned to start of actual sound data.
	wavFile->totalSamples = (length - pos) / 2; // 2 bytes per sample (16 bit sound mono)
	if (channels == 2) {
		wavFile->totalSamples /= 2; // 4 bytes per sample (16 bit stereo)
	}

	if (wavFile->totalSamples <= 5 * SAMPLE_RATE) {
		wavFile->samples = (float **) malloc(2 * sizeof(float *));
		// Allocate memory (right will be null if only mono sound)
		wavFile->samples[0] = (float *) calloc(wavFile->totalSamples,
				sizeof(float));
		wavFile->samples[1] =
				(channels == 2) ?
						(float *) calloc(wavFile->totalSamples, sizeof(float)) :
						NULL;

		// write to wavFile float buffers
		int i = 0;
		while (pos < length) {
			wavFile->samples[0][i] = bytesToFloat(wav[pos], wav[pos + 1]);
			pos += 2;
			if (channels == 2) {
				wavFile->samples[1][i] = bytesToFloat(wav[pos], wav[pos + 1]);
				pos += 2;
			}
			i++;
		}
	} else { // file too big for memory. write to temp file in external storage
		// tmpFileName = sampleFileName + ".raw"
		// wavFile->sampleFile = fopen(tmpFileName , "wb");
		// copy all wav bytes to floats on disk
	}
	free(wav);
	wav = NULL;

	// init loop / currSample position data
	wavFile->loopBegin = 0;
	wavFile->loopEnd = wavFile->totalSamples;
	if (wavFile->currSample >= wavFile->loopEnd) {
		wavFile->currSample = 0;
	}
	wavFile->loopLength = wavFile->loopEnd - wavFile->loopBegin;
}

WavFile *wavfile_create(const char *sampleName) {
	WavFile *wavFile = (WavFile *) malloc(sizeof(WavFile));
	wavFile->currSample = 0;
	wavFile->samples = NULL;
	wavfile_setSampleFile(wavFile, sampleName);
	wavFile->looping = wavFile->reverse = false;
	wavFile->adsr = adsrconfig_create();
	return wavFile;
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
	if (wavFile->sampleFile != NULL) {
		fflush(wavFile->sampleFile);
		fclose(wavFile->sampleFile);
	}
	wavfile_freeBuffers(wavFile);
	adsrconfig_destroy(wavFile->adsr);
	free(wavFile);
	wavFile = NULL;
}
