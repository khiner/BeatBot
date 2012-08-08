#include "sinewave.h"

SineWave *sinewave_create() {
	SineWave *sineWave = (SineWave *) malloc(sizeof(SineWave));
	sineWave->time = 0;
	sinewave_setFrequency(sineWave, 1);
	sineWave->phaseOffset = 0;
	sineWave->table = (float *) malloc((TABLE_SIZE + 1) * sizeof(float));
	float tmp = 1.0f / TABLE_SIZE;
	int i;
	for (i = 0; i < TABLE_SIZE; i++) {
		sineWave->table[i] = sin(M_PI * 2 * i * tmp);
	}
	return sineWave;
}

void sinewave_setRate(SineWave *config, float rate) {
	config->rate = rate;
	config->frequency = rate * SAMPLE_RATE / TABLE_SIZE;
}

void sinewave_setFrequency(SineWave *config, float frequency) {
	config->frequency = frequency;
	config->rate = TABLE_SIZE * frequency / SAMPLE_RATE;
}

void sinewave_addTimeInSamples(SineWave *config, float timeInSamples) {
	config->time += timeInSamples;
}

void sinewave_addTimeInPhase(SineWave *config, float phase) {
	config->time += TABLE_SIZE * phase;
}

void sinewave_addPhaseOffset(SineWave *config, float phaseOffset) {
	config->time += (phaseOffset - config->phaseOffset) * TABLE_SIZE;
	config->phaseOffset = phaseOffset;
}
