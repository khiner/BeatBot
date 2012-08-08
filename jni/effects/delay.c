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

void delayconfigi_destroy(void *p) {
	DelayConfigI *config = (DelayConfigI *) p;
	int channel;
	for (channel = 0; channel < 2; channel++)
		free(config->delayBuffer[channel]);
	free(config->delayBuffer);
	free((DelayConfigI *) p);
}

/********* JNI METHODS **********/
void Java_com_kh_beatbot_DelayActivity_setDelayOn(JNIEnv *env, jclass clazz,
		jint trackNum, jboolean on) {
	Track *track = getTrack(env, clazz, trackNum);
	Effect *delay = &(track->effects[DELAY_ID]);
	delay->on = on;
}

void Java_com_kh_beatbot_DelayActivity_setDelayParam(JNIEnv *env, jclass clazz,
		jint trackNum, jint paramNum, jfloat param) {
	Track *track = getTrack(env, clazz, trackNum);
	DelayConfigI *config = (DelayConfigI *) track->effects[DELAY_ID].config;
	int channel;
	if (paramNum == 0) { // delay time left
		delayconfigi_setDelayTimeLeft(config, param);
	} if (paramNum == 1) { // delay time right
		delayconfigi_setDelayTimeRight(config, param);
	} else if (paramNum == 2) { // feedback
		delayconfigi_setFeedback(config, param);
	} else if (paramNum == 3) { // wet/dry
		config->wet = param;
	}
}
