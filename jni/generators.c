#include "generators.h"

SineWave *sinewave_create() {
	SineWave *sineWave = (SineWave *)malloc(sizeof(SineWave));
	sineWave->time = 0;
	sineWave->rate = 1;
	sineWave->phaseOffset = 0;
	sineWave->table = (float *)malloc((TABLE_SIZE + 1)*sizeof(float));
	float tmp = 1.0f/TABLE_SIZE;
	int i;
	for (i = 0; i < TABLE_SIZE; i++) {
		sineWave->table[i] = sin(M_PI * 2 * i * tmp);
	}
	return sineWave;
}

void sinewave_setRateInSamples(SineWave *config, float rate) {
	config->rate = rate;
}

void sinewave_setFrequency(SineWave *config, float frequency) {
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

float sinewave_tick(SineWave *config) {
	while (config->time < 0)
		config->time += TABLE_SIZE;
	while (config->time >= TABLE_SIZE)
		config->time -= TABLE_SIZE;
	
	config->iIndex = floorf(config->time);
	config->alpha = config->time - config->iIndex;
	float tmp = config->table[config->iIndex];
	tmp += (config->alpha * (config->table[config->iIndex + 1] - tmp) );
	
	config->time += config->rate;
	
	return tmp;
}
