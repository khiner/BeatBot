#ifndef GENERATORS_H
#define GENERATORS_H

#include <stdlib.h>
#include <math.h>

#define TABLE_SIZE 2048
#define SAMPLE_RATE 44100

typedef struct SineWave_t {
	float *table;
	float time;
	float rate;
	float phaseOffset;
	float alpha;
	unsigned int iIndex;
} SineWave;

SineWave *sinewave_create();
void sinewave_setRateInSamples(SineWave *config, float rate);
void sinewave_setFrequency(SineWave *config, float frequency);
void sinewave_addTimeInSamples(SineWave *config, float timeInSamples);
void sinewave_addTimeInPhase(SineWave *config, float phase);
void sinewave_addPhaseOffset(SineWave *config, float phaseOffset);
float sinewave_tick(SineWave *config);

#endif //GENERATORS_H