#ifndef WAVFILE_H
#define WAVFILE_H

#include "../effects/effects.h"
#include "../effects/adsr.h"

#define CONVMYFLT (1./32768.)

typedef struct WavFile_t {
	float **buffers; // buffer to hold wav data
	int totalSamples;
	int currSample;
	int loopBegin;
	int loopEnd;
	bool looping;
	bool reverse;
} WavFile;

WavFile *wavfile_create();
void wavfile_reset(WavFile *config);

static inline void wavfile_generate(WavFile *config, float **inBuffer, int size) {
	int i, channel;
	for (i = 0; i < size; i++) {
		// wrap sample around loop window
		if (config->currSample > config->loopEnd) {
			if (config->looping)
				config->currSample = config->loopBegin;
			else
				return;
		} else if (config->currSample < config->loopBegin) {
			if (config->looping)
				config->currSample = config->loopEnd;
			else
				return;
		}
		for (channel = 0; channel < 2; channel++) {
			inBuffer[channel][i] = config->buffers[channel][config->currSample];
		}
		// get next sample.  if reverse, go backwards, else go forwards
		if (config->reverse)
			config->currSample--;
		else
			config->currSample++;
	}
}

void wavfile_destroy(void *config);

#endif // WAVFILE_H
