#ifndef ADSR_H
#define ADSR_H

#include "effects.h"

typedef struct AdsrPoint_t {
	int sampleNum;
	float sampleCents;
} AdsrPoint;

typedef struct AdsrConfig_t {
	AdsrPoint adsrPoints[5];
	float attackCoeff, decayCoeff, releaseCoeff;
	float currLevel;
	float sustain;
	float initial, peak, end;
	int gateSample; // sample to begin release
	int currSampleNum;
	int totalSamples;
	bool active, rising;
} AdsrConfig;

AdsrConfig *adsrconfig_create(int totalSamples);

void updateAdsr(AdsrConfig *config, int totalSamples);
void resetAdsr(AdsrConfig *config);

static inline void adsr_process(void *p, float **buffers, int size) {
	AdsrConfig *config = (AdsrConfig *) p;
	if (!config->active)
		return;
	int i;
	for (i = 0; i < size; i++) {
		if (++config->currSampleNum < config->gateSample) {
			if (config->rising) { // attack phase
				config->currLevel += config->attackCoeff
						* (config->peak / 0.63f - config->currLevel);
				if (config->currLevel > 1.0f) {
					config->currLevel = 1.0f;
					config->rising = false;
				}
			} else { // decal/sustain
				config->currLevel += config->decayCoeff
						* (config->sustain - config->currLevel) / 0.63f;
			}
		} else if (config->currSampleNum < config->adsrPoints[4].sampleNum) { // past gate sample, go to release phase
			config->currLevel += config->releaseCoeff
					* (config->end - config->currLevel) / 0.63f;
			if (config->currLevel < config->end) {
				config->currLevel = config->end;
			}
		} else if (config->currSampleNum < config->totalSamples) {
			config->currLevel = 0;
		} else {
			resetAdsr(config);
		}
		buffers[0][i] *= config->currLevel;
		buffers[1][i] *= config->currLevel;
	}
}

void adsrconfig_destroy(void *p);
