#include "../all.h"

AdsrConfig *adsrconfig_create() {
	AdsrConfig *config = (AdsrConfig *) malloc(sizeof(AdsrConfig));
	return config;
}

void adsrconfig_destroy(void *p) {
	free((AdsrConfig *) p);
}

void resetAdsr(AdsrConfig *config) {
	config->currSample = 0;
	config->currLevel = config->initial;
	config->stoppedSample = FLT_MAX;
}

void adsrconfig_setParam(AdsrConfig *config, float paramNumFloat, float value) {
	int paramNum = (int)paramNumFloat;
	switch (paramNum) {
	case 0:
		config->attack = value;
		break;
	case 1:
		config->decay = value;
		break;
	case 2:
		config->sustain = value;
		break;
	case 3:
		config->release = value;
		break;
	case 4:
		config->peak = value;
		break;
	case 5:
		config->initial = value;
		break;
	}
}
