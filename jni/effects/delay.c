#include "delay.h"

DelayConfigI *delayconfigi_create(float delay, float feedback, int maxSamples) {
	// allocate memory and set feedback parameter
	DelayConfigI *p = (DelayConfigI *) malloc(sizeof(DelayConfigI));
	pthread_mutex_init(&p->mutex, NULL);
	p->maxSamples = maxSamples;
	p->delayBuffer = (float **) malloc(2 * sizeof(float *));
	p->delayBuffer[0] = (float *) malloc(maxSamples * sizeof(float));
	p->delayBuffer[1] = (float *) malloc(maxSamples * sizeof(float));
	p->rp[0] = p->rp[1] = p->wp[0] = p->wp[1] = 0;
	delayconfigi_set(p, delay, feedback);
	p->wet = 0.5f;
	p->numBeats[0] = p->numBeats[1] = 4;
	p->beatmatch = false;
	return p;
}

void delayconfigi_set(void *p, float delay, float feedback) {
	DelayConfigI *config = (DelayConfigI *) p;
	delayconfigi_setDelayTime(config, delay, delay);
	delayconfigi_setFeedback(config, feedback);
}

void delayconfigi_setFeedback(DelayConfigI *config, float feedback) {
	config->feedback =
			feedback > 0.f ? (feedback < 1.f ? feedback : 0.9999999f) : 0.f;
}

void delayconfigi_setNumBeats(DelayConfigI *config, int numBeatsL,
		int numBeatsR) {
	if (numBeatsL == config->numBeats[0] && numBeatsR == config->numBeats[1])
		return;
	config->numBeats[0] = numBeatsL;
	config->numBeats[1] = numBeatsR;
	delayconfigi_syncToBPM(config);
}

void delayconfigi_syncToBPM(DelayConfigI *config) {
	if (!config->beatmatch)
		return;
	// divide by 60 for seconds, divide by 16 for 16th notes
	float newTimeL = (BPM / 960.0f) * (float) config->numBeats[0];
	float newTimeR = (BPM / 960.0f) * (float) config->numBeats[1];
	delayconfigi_setDelayTime(config, newTimeL, newTimeR);
}

void delayconfigi_destroy(void *p) {
	DelayConfigI *config = (DelayConfigI *) p;
	int channel;
	for (channel = 0; channel < 2; channel++)
		free(config->delayBuffer[channel]);
	free(config->delayBuffer);
	free((DelayConfigI *) p);
}
