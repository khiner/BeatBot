#include "../all.h"

AdsrConfig *adsrconfig_create() {
	AdsrConfig *config = (AdsrConfig *) malloc(sizeof(AdsrConfig));
	config->attack = 1;
	config->decay = 1;
	config->sustain = 1;
	config->release = 1;
	config->start = 0;
	config->peak = 1;
	config->currSample = config->stoppedSample = 0;

	return config;
}

void adsrconfig_destroy(void *p) {
	free((AdsrConfig *) p);
}

void resetAdsr(AdsrConfig *config) {
	config->currSample = 0;
	config->currLevel = config->start;
	config->stoppedSample = FLT_MAX;
}

void adsrconfig_setParam(AdsrConfig *config, float paramNumFloat, float value) {
	int paramNum = (int)paramNumFloat;
	switch (paramNum) {
	case 0:
		config->attack = value * SAMPLE_RATE;
		if (config->attack < 1)
			config->attack = 1;
		break;
	case 1:
		config->decay = value * SAMPLE_RATE;
		if (config->decay < 1)
			config->decay = 1;
		break;
	case 2:
		config->sustain = value;
		break;
	case 3:
		config->release = value * SAMPLE_RATE;
		if (config->release < 1)
			config->release = 1;
		break;
	case 4:
		config->start = value;
		break;
	case 5:
		config->peak = value;
		break;
	}
}
