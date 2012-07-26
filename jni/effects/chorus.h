#ifndef CHORUS_H
#define CHORUS_H

#include "effects.h"

#define MIN_CHORUS_DELAY 0.008f*SAMPLE_RATE
#define MAX_CHORUS_DELAY 0.025f*SAMPLE_RATE

typedef struct ChorusConfig_t {
	DelayConfigI *delayConfig; // delay line
	SineWave *mod[2]; // table-based sine wave generator for modulation
	float baseTime; // center time for delay modulation
	float modAmt; // modulation depth
} ChorusConfig;

ChorusConfig *chorusconfig_create(float modFreq, float modAmt);
void chorusconfig_set(void *p, float modFreq, float modAmt);

void chorusconfig_setBaseTime(ChorusConfig *config, float baseTime);
void chorusconfig_setFeedback(ChorusConfig *config, float feedback);
void chorusconfig_setModFreq(ChorusConfig *config, float modFreq);
void chorusconfig_setModAmt(ChorusConfig *config, float modAmt);
void chorusconfig_setWet(ChorusConfig *config, float wet);

static inline void chorus_process(void *p, float **buffers, int size) {
	ChorusConfig *config = (ChorusConfig *) p;
	int channel, samp;
	for (samp = 0; samp < size; samp++) {
		float dTimeL = config->baseTime * 0.707
				* (1.0f + config->modAmt * sinewave_tick(config->mod[0]));
		float dTimeR = config->baseTime * 0.5
				* (1.0f - config->modAmt * sinewave_tick(config->mod[1]));
		delayconfigi_setDelaySamples(config->delayConfig, dTimeL, dTimeR);
		pthread_mutex_lock(&config->delayConfig->mutex);
		for (channel = 0; channel < 2; channel++) {
			buffers[channel][samp] = delayi_tick(config->delayConfig,
					buffers[channel][samp], channel);
		}
		pthread_mutex_unlock(&config->delayConfig->mutex);
	}
}

void chorusconfig_destroy(void *p);

#endif //CHORUS_H
