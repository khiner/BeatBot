#ifndef ADSR_H
#define ADSR_H

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
	float currSample;
	int gateSample; // sample to begin release
	int totalSamples;
	bool active, rising;
} AdsrConfig;

AdsrConfig *adsrconfig_create(int totalSamples);
void adsrconfig_setNumSamples(AdsrConfig *config, int numSamples);

void updateAdsr(AdsrConfig *config, int totalSamples);
void resetAdsr(AdsrConfig *config);

static inline float adsr_tick(AdsrConfig *config, float sampleRate) {
	if (!config->active) {
		return 1;
	}

	config->currSample += sampleRate;
	if (config->currSample < config->gateSample) {
		if (config->rising) { // attack phase
			config->currLevel += config->attackCoeff
					* (config->peak / 0.63f - config->currLevel);
			if (config->currLevel > 1.0f) {
				config->currLevel = 1.0f;
				config->rising = false;
			}
		} else { // decay/sustain
			config->currLevel += config->decayCoeff
					* (config->sustain - config->currLevel) / 0.63f;
		}
	} else if (config->currSample < config->adsrPoints[4].sampleNum) {
		// past gate sample, go to release phase
		config->currLevel += config->releaseCoeff
				* (config->end - config->currLevel) / 0.63f;
		if (config->currLevel < config->end) {
			config->currLevel = config->end;
		}
	} else if (config->currSample < config->totalSamples) {
		config->currLevel = 0;
	} else {
		resetAdsr(config);
	}
	return config->currLevel;
}

static inline void adsr_process(AdsrConfig *config, float **buffers, int size) {
	if (!config->active)
		return;
	int i;
	for (i = 0; i < size; i++) {
		float gain = adsr_tick(config, 1);
		buffers[0][i] *= gain;
		buffers[1][i] *= gain;
	}
}

void adsrconfig_destroy(void *p);

#endif // ADSR_H
