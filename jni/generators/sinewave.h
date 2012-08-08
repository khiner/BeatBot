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
	while (config->time < 0)
		config->time += TABLE_SIZE;
	while (config->time >= TABLE_SIZE)
		config->time -= TABLE_SIZE;

	config->iIndex = floorf(config->time);
	config->alpha = config->time - config->iIndex;
	float tmp = config->table[config->iIndex];
	tmp += (config->alpha * (config->table[config->iIndex + 1] - tmp));

	config->time += config->rate;

	return tmp;
}

#endif // SINEWAVE_H
