#include "adsr.h"

void initAdsrPoints(AdsrConfig *config) {
	config->adsrPoints[0].sampleCents = 0;
	config->adsrPoints[1].sampleCents = 0.25f;
	config->adsrPoints[2].sampleCents = 0.5f;
	config->adsrPoints[3].sampleCents = 0.75f;
	config->adsrPoints[4].sampleCents = 1;
}

AdsrConfig *adsrconfig_create(int numSamples) {
	AdsrConfig *config = (AdsrConfig *) malloc(sizeof(AdsrConfig));
	adsrconfig_setNumSamples(config, numSamples);
	return config;
}

void adsrconfig_setNumSamples(AdsrConfig *config, int numSamples) {
	initAdsrPoints(config);
	config->active = false;
	config->initial = config->end = 0.0001f;
	config->sustain = 0.6f;
	config->peak = 1.0f;
	resetAdsr(config);
	updateAdsr(config, numSamples);
}

void adsrconfig_destroy(void *p) {
	AdsrConfig *config = (AdsrConfig *) p;
	free(config->adsrPoints);
	free(config);
}

void updateAdsr(AdsrConfig *config, int totalSamples) {
	config->totalSamples = totalSamples;
	int i, length;
	for (i = 0; i < 5; i++) {
		config->adsrPoints[i].sampleNum =
				(int) (config->adsrPoints[i].sampleCents * totalSamples);
	}
	for (i = 0; i < 4; i++) {
		length = config->adsrPoints[i + 1].sampleNum
				- config->adsrPoints[i].sampleNum;
		if (i == 0)
			config->attackCoeff = (1.0f - config->initial) / (length + 1);
		else if (i == 1)
			config->decayCoeff = 1.0f / (length + 1);
		else if (i == 3)
			config->releaseCoeff = (1.0f - config->end) / (length + 1);
	}
	config->gateSample = config->adsrPoints[3].sampleNum;
}

void resetAdsr(AdsrConfig *config) {
	config->currSample = 0;
	config->currLevel = config->initial;
	config->rising = true;
}
