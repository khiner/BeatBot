#ifndef WAVFILE_H
#define WAVFILE_H

#include "../effects/effects.h"
#include "../effects/adsr.h"
#include <stdio.h>

#define CONVMYFLT (1./32768.)
#define ONE_FLOAT_SZ sizeof(float)
#define TWO_FLOAT_SZ 2 * sizeof(float)

typedef struct WavFile_t {
	// mutex for buffer since setting the wav data happens on diff thread than processing
	pthread_mutex_t bufferMutex;
	FILE *sampleFile;
	float tempSample[2];
	int totalSamples;
	float currSample;
	long loopBegin;
	long loopEnd;
	bool looping;
	bool reverse;
	float sampleRate;
} WavFile;

WavFile *wavfile_create();
void wavfile_setBytes(WavFile *wavFile, char *bytes, int length);
void wavfile_reset(WavFile *config);
void freeBuffers(WavFile *config);

static inline void wavfile_tick(WavFile *config, float *sample) {
	// wrap sample around loop window
	if (config->looping) {
		if (config->currSample >= config->loopEnd) {
			config->currSample = config->loopBegin;
		} else if (config->currSample <= config->loopBegin) {
			config->currSample = config->loopEnd;
		}
	}

	if (config->currSample > config->loopEnd || config->currSample < config->loopBegin) {
		sample[0] = sample[1] = 0;
	} else {
		fseek(config->sampleFile, (long)config->currSample * TWO_FLOAT_SZ, SEEK_SET);
		fread(&sample[0], 1, ONE_FLOAT_SZ, config->sampleFile);
		fread(&sample[1], 1, ONE_FLOAT_SZ, config->sampleFile);
	}

	// get next sample.  if reverse, go backwards, else go forwards
	if (config->reverse) {
		config->currSample -= config->sampleRate;
	} else {
		config->currSample += config->sampleRate;
	}
}

static inline void wavfile_generate(WavFile *config, float **inBuffer, int size) {
	int i, channel;
	for (i = 0; i < size; i++) {
		wavfile_tick(config, config->tempSample);
		for (channel = 0; channel < 2; channel++) {
			inBuffer[channel][i] = config->tempSample[channel];
		}
	}
}

void wavfile_destroy(void *config);

#endif // WAVFILE_H
