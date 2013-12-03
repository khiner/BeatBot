#include "../all.h"
#include "libsndfile/sndfile.h"

static const char* rawExtension = ".raw";
static float data[BUFF_SIZE];

static inline int fileExists(char *filename) {
	struct stat buffer;
	return (stat(filename, &buffer) == 0);
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
	SNDFILE *infile;
	SF_INFO sfinfo;
	int readcount, samp = 0, chan = 0, i = 0;

	if (!(infile = sf_open(sampleFileName, SFM_READ, &sfinfo))) { /* Open failed so print an error message. */
		__android_log_print(ANDROID_LOG_INFO, "wavfile", "Not able to open input file %s.\n", "input.wav");
		/* Print the error message fron libsndfile. */
		sf_perror(NULL );
		//return 1;
	};

	if (sfinfo.channels > MAX_CHANNELS) {
		__android_log_print(ANDROID_LOG_INFO, "wavfile", "Not able to process more than %d channels\n", MAX_CHANNELS);
		//return 1;
	};

	config->channels = sfinfo.channels;
	config->totalSamples = sfinfo.frames;

	// if a different sample was already loaded, destroy it.
	wavfile_freeBuffers(config);

	if (false) {
		/** allocate memory to hold samples (memory is freed in wavfile_destroy)
		 *
		 * NOTE: We don't directly write to wavFile sample buffer because we want to
		 * completely load all samples from file before making wavFile->samples non-NULL.
		 * That way, we can still immediately read from file on a per-sample basis, until
		 * wavFile->samples is non-null, at which point we can read directly from memory.
		 */
		float **tempSamples = malloc(2 * sizeof(void *));
		tempSamples[0] = malloc(config->totalSamples * ONE_FLOAT_SZ);
		// (right channel will be null if only mono sound)
		tempSamples[1] =
				config->channels == 2 ?
						malloc(config->totalSamples * ONE_FLOAT_SZ) : NULL;

		// write to wavFile float buffers
		while ((readcount = sf_read_float(infile, data, BUFF_SIZE))) {
	        for (i = 0; i < readcount;) {
	        	for (chan = 0; chan < config->channels; chan++) {
		        	tempSamples[chan][samp] = data[i++];
		        }
	        	samp++;
		    }
		};
		// make wavFile->samples point to fully-loaded sample buffer in memory
		config->samples = tempSamples;
		// cleanup
		tempSamples = NULL;
	} else { // file too big for memory. write to temp file in external storage
		// concat sampleFileName with ".raw" extension
		config->sampleFileName = malloc(strlen(sampleFileName) + 1 + 4);
		strcpy(config->sampleFileName, sampleFileName); /* copy name into the new var */
		strcat(config->sampleFileName, rawExtension); /* add the extension */

		if (!fileExists(config->sampleFileName)) {
			// open *.raw file next to *.wav file - we will read directly from this file
			FILE *tempFile = fopen(config->sampleFileName, "wb");

			// copy all wav bytes to floats on disk
			while ((readcount = sf_read_float(infile, data, BUFF_SIZE))) {
				for (i = 0; i < readcount; i++) {
					fwrite(&data[i], ONE_FLOAT_SZ, 1, tempFile);
			    }
			};
			fflush(tempFile);
			fclose(tempFile);
		} else {
			__android_log_print(ANDROID_LOG_INFO, "wavfile",
					"Reusing existing sample");
		}
		config->sampleFile = fopen(config->sampleFileName, "rb");
	}

	sf_close(infile);

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
