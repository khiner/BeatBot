#ifndef DELAY_H
#define DELAY_H

#include "effects.h"

typedef struct DelayConfigI_t {
	float **delayBuffer; // delay buffer for each channel
	float delayTime[2]; // delay time in seconds: 0-1; one for each channel
	float feedback; // feedback amount: 0-1; used for both channels
	float wet; // wet/dry; used for both channels
	float alpha[2];
	float omAlpha[2];
	float delaySamples[2]; // (fractional) delay time in samples: 0 - SAMPLE_RATE; one for each channel
	float out;
	int maxSamples; // maximum size of delay buffer (set to SAMPLE_RATE by default)
	int numBeats[2]; // number of beats to delay for beatmatch; one for each channel
	int rp[2], wp[2]; // read & write pointers
	bool beatmatch; // sync to the beat?
	pthread_mutex_t mutex; // mutex since sets happen on a different thread than processing
} DelayConfigI;

DelayConfigI *delayconfigi_create(float delay, float feedback, int maxSamples);
void delayconfigi_set(void *config, float delay, float feedback);
void delayconfigi_setFeedback(DelayConfigI *config, float feedback);
void delayconfigi_setNumBeats(DelayConfigI *config, int numBeatsL,
		int numBeatsR);
void delayconfigi_syncToBPM(DelayConfigI *config);

static inline void delayconfigi_setDelaySamples(DelayConfigI *config,
		float numSamplesL, float numSamplesR) {
	int *rp, *wp, channel;
	float rpf;
	pthread_mutex_lock(&config->mutex);
	config->delaySamples[0] = numSamplesL;
	config->delaySamples[1] = numSamplesR;
	for (channel = 0; channel < 2; channel++) {
		rp = &(config->rp[channel]);
		wp = &(config->wp[channel]);
		rpf = *wp - config->delaySamples[channel]; // read chases write
		while (rpf < 0)
			rpf += config->maxSamples;
		*rp = (int) rpf;
		if (*rp >= config->maxSamples)
			(*rp) = 0;
		config->alpha[channel] = rpf - (*rp);
		config->omAlpha[channel] = 1.0f - config->alpha[channel];
	}
	pthread_mutex_unlock(&config->mutex);
}

static inline void delayconfigi_setDelayTime(DelayConfigI *config, float lDelay,
		float rDelay) {
	pthread_mutex_lock(&config->mutex);
	config->delayTime[0] =
			lDelay > 0.0001 ? (lDelay <= 1 ? lDelay : 1) : 0.0001;
	config->delayTime[1] =
			rDelay > 0.0001 ? (rDelay <= 1 ? rDelay : 1) : 0.0001;
	pthread_mutex_unlock(&config->mutex);
	delayconfigi_setDelaySamples(config, config->delayTime[0] * SAMPLE_RATE,
			config->delayTime[1] * SAMPLE_RATE);
}

static inline float delayi_tick(DelayConfigI *config, float in, int channel) {
	if (config->rp[channel] >= config->maxSamples)
		config->rp[channel] = 0;
	if (config->wp[channel] >= config->maxSamples)
		config->wp[channel] = 0;

	float interpolated = config->delayBuffer[channel][config->rp[channel]++]
			* config->omAlpha[channel];
	interpolated += config->delayBuffer[channel][config->rp[channel]
			% config->maxSamples] * config->alpha[channel];
	config->out = interpolated * config->wet + in * (1 - config->wet);
	if (config->out > 1)
		config->out = 1;
	config->delayBuffer[channel][config->wp[channel]++] = in
			+ config->out * config->feedback;
	return config->out;
}

static inline void delayi_process(void *p, float **buffers, int size) {
	DelayConfigI *config = (DelayConfigI *) p;
	int channel, samp;
	for (samp = 0; samp < size; samp++) {
		pthread_mutex_lock(&config->mutex);
		for (channel = 0; channel < 2; channel++) {
			buffers[channel][samp] = delayi_tick(config, buffers[channel][samp],
					channel);
		}
		pthread_mutex_unlock(&config->mutex);
	}
}

void delayconfigi_destroy(void *p);

#endif // DELAY_H
