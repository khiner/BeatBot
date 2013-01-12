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
