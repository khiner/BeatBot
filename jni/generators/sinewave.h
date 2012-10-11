#ifndef SINEWAVE_H
#define SINEWAVE_H

#include "generators.h"

typedef struct SineWave_t {
	float *table;
	float alpha;
	float frequency;
	float phaseOffset;
	float rate;
	float time;
	unsigned int iIndex;
} SineWave;

SineWave *sinewave_create();
void sinewave_setRate(SineWave *config, float rate);
void sinewave_setFrequency(SineWave *config, float frequency);
void sinewave_addTimeInSamples(SineWave *config, float timeInSamples);
void sinewave_addTimeInPhase(SineWave *config, float phase);
void sinewave_addPhaseOffset(SineWave *config, float phaseOffset);

static inline float sinewave_tick(SineWave *config) {
	while (config->time >= TABLE_SIZE)
		config->time -= TABLE_SIZE;

	config->iIndex = (unsigned int)config->time;
	config->alpha = config->time - config->iIndex;

	config->time += config->rate;

	return (config->table[config->iIndex] + config->alpha*config->table[config->iIndex + 1]) /
			(1 + config->alpha);
}

#endif // SINEWAVE_H
