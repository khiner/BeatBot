#include "../all.h"

ChorusConfig *chorusconfig_create() {
	ChorusConfig *config = (ChorusConfig *) malloc(sizeof(ChorusConfig));
	chorusconfig_setBaseTime(config,
			MIN_CHORUS_DELAY + (MAX_CHORUS_DELAY - MIN_CHORUS_DELAY) / 2);
	config->delayConfig = delayconfigi_create(
			(config->baseTime) * INV_SAMPLE_RATE, .5f, MAX_CHORUS_DELAY + 512);
	config->mod[0] = sinewave_create();
	config->mod[1] = sinewave_create();
	chorusconfig_set(config, .5f, .5f);
	return config;
}

void chorusconfig_set(void *p, float modFreq, float modAmt) {
	ChorusConfig *config = (ChorusConfig *) p;
	chorusconfig_setModAmt(config, modAmt);
	chorusconfig_setModFreq(config, modFreq);
}

void chorusconfig_setBaseTime(ChorusConfig *config, float baseTime) {
	config->baseTime = MIN_CHORUS_DELAY + baseTime*(MAX_CHORUS_DELAY - MIN_CHORUS_DELAY);
}

void chorusconfig_setFeedback(ChorusConfig *config, float feedback) {
	delayconfigi_setFeedback(config->delayConfig, feedback);
}

void chorusconfig_setModFreq(ChorusConfig *config, float modFreq) {
	sinewave_setFrequency(config->mod[0], modFreq);
	sinewave_setFrequency(config->mod[1], modFreq * 1.1111);
}

void chorusconfig_setModAmt(ChorusConfig *config, float modAmt) {
	config->modAmt = modAmt;
}

void chorusconfig_setWet(ChorusConfig *config, float wet) {
	config->delayConfig->wet = wet;
}

void chorusconfig_destroy(void *p) {
	ChorusConfig *config = (ChorusConfig *) p;
	delayconfigi_destroy(config->delayConfig);
	free(config);
}

void chorusconfig_setParam(void *p, float paramNumFloat, float param) {
	int paramNum = (int)paramNumFloat;
	ChorusConfig *config = (ChorusConfig *) p;
	if (paramNum == 0) { // modulation rate
		chorusconfig_setModFreq(config, param);
	} else if (paramNum == 1) { // modulation amount
		chorusconfig_setModAmt(config, param);
	} else if (paramNum == 2) { // delay time
		chorusconfig_setBaseTime(config, param);
	} else if (paramNum == 3) { // feedback
		chorusconfig_setFeedback(config, param);
	} else if (paramNum == 4) { // wet/dry
		chorusconfig_setWet(config, param);
	}
}
