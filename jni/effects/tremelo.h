#ifndef TREMELO_H
#define TREMELO_H

#include "effects.h"

typedef struct TremeloConfig_t {
	SineWave *mod[2];
	float depth;
} TremeloConfig;

TremeloConfig *tremeloconfig_create(float freq, float depth);

void tremeloconfig_set(void *config, float freq, float depth);
void tremeloconfig_setFrequency(TremeloConfig *config, float freqL,
		float freqR);
void tremeloconfig_setDepth(TremeloConfig *config, float depth);

static inline void tremelo_process(void *p, float **buffers, int size) {
	TremeloConfig *config = (TremeloConfig *) p;
	int channel, samp;
	for (channel = 0; channel < 2; channel++) {
		for (samp = 0; samp < size; samp++) {
			// TODO : how to scale amplitude from 0 to 1 without halving amp when depth is 0
			buffers[channel][samp] *= .75f*(1 + sinewave_tick(config->mod[channel]) * config->depth);
		}
	}
}

void tremeloconfig_destroy(void *p);

#endif // TREMELO_H
