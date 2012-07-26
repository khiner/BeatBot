#include "flanger.h"

FlangerConfig *flangerconfig_create() {
	FlangerConfig *flangerConfig = (FlangerConfig *) malloc(
			sizeof(FlangerConfig));
	flangerConfig->delayConfig = delayconfigi_create(0.003f, 0.5f,
			MAX_FLANGER_DELAY + 512);
	flangerconfig_set(flangerConfig, 0.003f * SAMPLE_RATE, 0.5f);
	flangerConfig->mod[0] = sinewave_create();
	flangerConfig->mod[1] = sinewave_create();
	flangerConfig->modAmt = .5f;
	return flangerConfig;
}

void flangerconfig_set(void *p, float delayTimeInSamples, float feedback) {
	FlangerConfig *config = (FlangerConfig *) p;
	flangerconfig_setBaseTime(config, delayTimeInSamples);
	delayconfigi_setDelaySamples(config->delayConfig, delayTimeInSamples,
			delayTimeInSamples);
	flangerconfig_setFeedback(config, feedback);
}

void flangerconfig_setBaseTime(FlangerConfig *config, float baseTime) {
	config->baseTime = baseTime;
}

void flangerconfig_setFeedback(FlangerConfig *config, float feedback) {
	delayconfigi_setFeedback(config->delayConfig, feedback);
}

void flangerconfig_setModFreq(FlangerConfig *config, float modFreq) {
	int channel;
	for (channel = 0; channel < 2; channel++)
		sinewave_setFrequency(config->mod[channel], modFreq * 16);
}

void flangerconfig_setModAmt(FlangerConfig *config, float modAmt) {
	config->modAmt = modAmt;
}

void flangerconfig_setPhaseShift(FlangerConfig *config, float phaseShift) {
	sinewave_addPhaseOffset(config->mod[1], phaseShift);
}

void flangerconfig_destroy(void *p) {
	FlangerConfig *config = (FlangerConfig *) p;
	delayconfigi_destroy(config->delayConfig);
	free(config);
}
