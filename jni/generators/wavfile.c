#include "wavfile.h"
#include "../track.h"

void wavfile_setSampleFile(WavFile *wavFile, const char *sampleFileName) {
	wavFile->sampleFile = fopen(sampleFileName, "rb");
	fseek(wavFile->sampleFile, 0, SEEK_END);
	wavFile->totalSamples = ftell(wavFile->sampleFile) / TWO_FLOAT_SZ; // 2 floats per sample
	fseek(wavFile->sampleFile, 0, SEEK_SET);

	// init loop / currSample position data
	wavFile->loopBegin = wavFile->currSample = 0;
	wavFile->loopEnd = wavFile->totalSamples - 2;
	wavFile->loopLength = wavFile->loopEnd - wavFile->loopBegin;
}

WavFile *wavfile_create(const char *sampleName) {
	WavFile *wavFile = (WavFile *) malloc(sizeof(WavFile));
	pthread_mutex_init(&wavFile->bufferMutex, NULL);
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
	free(config);
	config = NULL;
}
