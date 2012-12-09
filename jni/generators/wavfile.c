#include "../all.h"

void wavfile_freeBuffers(WavFile *wavFile) {
	// if this sample was short enough, it was also loaded into memory, and will be non-null
	if (wavFile->samples != NULL) {
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
	wavFile->sampleFile = fopen(sampleFileName, "rb");
	fseek(wavFile->sampleFile, 0, SEEK_END);
	wavFile->totalSamples = ftell(wavFile->sampleFile) / TWO_FLOAT_SZ; // 2 floats per sample
	fseek(wavFile->sampleFile, 0, SEEK_SET);

	// init loop / currSample position data
	wavFile->loopBegin = 0;
	wavFile->loopEnd = wavFile->totalSamples - 2;
	if (wavFile->currSample >= wavFile->loopEnd) {
		wavFile->currSample = 0;
	}
	wavFile->loopLength = wavFile->loopEnd - wavFile->loopBegin;

	// if a different sample was already loaded, destroy it.
	wavfile_freeBuffers(wavFile);
	// if sample is less than 8 seconds, load into memory from a separate, temporary file
	if (wavFile->totalSamples <= 8 * SAMPLE_RATE) {
		FILE *tempFile = fopen(sampleFileName, "rb");
		/** allocate memory to hold samples (memory is freed in wavfile_destroy)
		 *
		 * NOTE: We don't directly write to wavFile sample buffer because we want to
		 * completely load all samples from file before making wavFile->samples non-NULL.
		 * That way, we can immediately still read from file on a per-sample basis, until
		 * wavFile->samples is non-null, at which point we can read directly from memory.
		 */
		float **tempSamples = malloc(2 * sizeof(void *));
		tempSamples[0] = malloc(wavFile->totalSamples * sizeof(float));
		tempSamples[1] = malloc(wavFile->totalSamples * sizeof(float));
		// load all samples from file into sample buffer
		int sampleNum;
		for (sampleNum = 0; sampleNum < wavFile->totalSamples; sampleNum++) {
			fread(&tempSamples[0][sampleNum], 1, ONE_FLOAT_SZ, tempFile);
			fread(&tempSamples[1][sampleNum], 1, ONE_FLOAT_SZ, tempFile);
		}
		// make wavFile->samples point to fully-loaded sample buffer in memory
		wavFile->samples = tempSamples;
		// cleanup
		tempSamples = NULL;
		fflush(tempFile);
		fclose(tempFile);
	}
}

WavFile *wavfile_create(const char *sampleName) {
	WavFile *wavFile = (WavFile *) malloc(sizeof(WavFile));
	wavFile->currSample = 0;
	wavFile->samples = NULL;
	wavfile_setSampleFile(wavFile, sampleName);
	wavFile->looping = wavFile->reverse = false;
	return wavFile;
}

void wavfile_reset(WavFile *config) {
	config->currSample = config->reverse ? config->loopEnd : config->loopBegin;
	fseek(config->sampleFile, config->currSample * TWO_FLOAT_SZ, SEEK_SET);
}

void wavfile_destroy(void *p) {
	WavFile *config = (WavFile *) p;
	fflush(config->sampleFile);
	fclose(config->sampleFile);
	wavfile_freeBuffers(config);
	free(config);
	config = NULL;
}
