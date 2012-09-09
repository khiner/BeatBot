#include "delay.h"

DelayConfigI *delayconfigi_create() {
	// allocate memory and set feedback parameter
	DelayConfigI *p = (DelayConfigI *) malloc(sizeof(DelayConfigI));
	pthread_mutex_init(&p->mutex, NULL);
	p->maxSamples = (int)(4.5f * SAMPLE_RATE);
	p->delayBuffer = (float **) malloc(2 * sizeof(float *));
	p->delayBuffer[0] = (float *) malloc(p->maxSamples * sizeof(float));
	p->delayBuffer[1] = (float *) malloc(p->maxSamples * sizeof(float));
	p->rp[0] = p->rp[1] = p->wp[0] = p->wp[1] = 0;
	delayconfigi_setDelayTime(p, .5f, .5f);
	delayconfigi_setFeedback(p, .5f);
	p->wet = 0.5f;
	p->linkChannels = true;
	return p;
}

void delayconfigi_setFeedback(DelayConfigI *config, float feedback) {
	config->feedback =
			feedback > 0.f ? (feedback < 1.f ? feedback : 0.9999999f) : 0.f;
}

void delayconfigi_destroy(void *p) {
	DelayConfigI *config = (DelayConfigI *) p;
	int channel;
	for (channel = 0; channel < 2; channel++)
		free(config->delayBuffer[channel]);
	free(config->delayBuffer);
	free((DelayConfigI *) p);
}

void delayconfigi_setParam(void *p, float paramNumFloat, float param) {
	int paramNum = (int)paramNumFloat;
	DelayConfigI *config = (DelayConfigI *) p;
	if (paramNum == 0) { // delay time left
		delayconfigi_setDelayTimeLeft(config, param);
	} if (paramNum == 1) { // delay time right
		delayconfigi_setDelayTimeRight(config, param);
	} else if (paramNum == 2) { // feedback
		delayconfigi_setFeedback(config, param);
	} else if (paramNum == 3) { // wet/dry
		config->wet = param;
	} else if (paramNum == 4) { // link channels
		config->linkChannels = (int)param == 0 ? false : true;
		delayconfigi_updateDelayTime(config);
	}
}
