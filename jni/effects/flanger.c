#include "flanger.h"

FlangerConfig *flangerconfig_create() {
	FlangerConfig *flangerConfig = (FlangerConfig *) malloc(
			sizeof(FlangerConfig));
	flangerConfig->delayConfig = delayconfigi_create(0.003f, 0.5f,
			MAX_FLANGER_DELAY + 512);
	float delayTimeInSamples = 0.003f * SAMPLE_RATE;
	flangerconfig_setBaseTime(flangerConfig, delayTimeInSamples);
	delayconfigi_setDelaySamples(flangerConfig->delayConfig, delayTimeInSamples,
			delayTimeInSamples);
	flangerconfig_setFeedback(flangerConfig, .5f);
	flangerConfig->mod[0] = sinewave_create();
	flangerConfig->mod[1] = sinewave_create();
	flangerConfig->modAmt = .5f;
	return flangerConfig;
}

void flangerconfig_setBaseTime(FlangerConfig *config, float baseTime) {
	config->baseTime = MIN_FLANGER_DELAY + baseTime*(MAX_FLANGER_DELAY - MIN_FLANGER_DELAY);
}

void flangerconfig_setFeedback(FlangerConfig *config, float feedback) {
	delayconfigi_setFeedback(config->delayConfig, feedback);
}

void flangerconfig_setModFreq(FlangerConfig *config, float modFreq) {
	int channel;
	for (channel = 0; channel < 2; channel++)
		sinewave_setFrequency(config->mod[channel], modFreq);
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

void flangerconfig_setParam(void *p, float paramNumFloat, float param) {
	int paramNum = (int)paramNumFloat;
	FlangerConfig *config = (FlangerConfig *) p;
	if (paramNum == 0) { // delay time
		flangerconfig_setBaseTime(config, param);
	} else if (paramNum == 1) { // feedback
		delayconfigi_setFeedback(config->delayConfig, param);
	} else if (paramNum == 2) { // wet/dry
		config->delayConfig->wet = param;
	} else if (paramNum == 3) { // modulation rate
		flangerconfig_setModFreq(config, param);
	} else if (paramNum == 4) { // modulation amount
		flangerconfig_setModAmt(config, param);
	} else if (paramNum == 5) { // phase offset
		flangerconfig_setPhaseShift(config, param);
	}
}

