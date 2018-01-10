#ifndef ADSR_H
#define ADSR_H

typedef struct AdsrPoint_t {
	int sampleNum;
	float sampleCents;
} AdsrPoint;

typedef struct AdsrConfig_t {
	// attack, decay, release all in samples.
	// sustain / start / peak are values between 0 and 1
	float attack, decay, sustain, release, start, peak;
	float currLevel;
	float currSample;
	float stoppedSample;
} AdsrConfig;

AdsrConfig *adsrconfig_create();
void resetAdsr(AdsrConfig *config);

static inline float adsr_tick(AdsrConfig *config) {
	if (config->currSample >= config->stoppedSample) { // note ended - in release phase
		config->currLevel += (-config->sustain) / config->release;
	} else if (config->currSample < config->attack) { // rising
		config->currLevel += (config->peak - config->start) / config->attack;
	} else if (config->currSample < config->attack + config->decay) { // decaying
		config->currLevel += (config->sustain - config->peak) / config->decay;
	}

	config->currLevel = config->currLevel > 0 ? (config->currLevel < 1 ? config->currLevel : 1) : 0;

	config->currSample++;
	return config->currLevel;
}

static inline void adsr_process(AdsrConfig *config, float **buffers, int size) {
	int i;
	for (i = 0; i < size; i++) {
		float gain = adsr_tick(config);
		buffers[0][i] *= gain;
		buffers[1][i] *= gain;
	}
}

void adsrconfig_setParam(AdsrConfig *config, float floatParamNum, float paramValue);
void adsrconfig_destroy(void *p);

#endif // ADSR_H
