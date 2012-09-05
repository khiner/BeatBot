#include "decimate.h"

DecimateConfig *decimateconfig_create(float bits, float rate) {
	DecimateConfig *decimateConfig = (DecimateConfig *) malloc(
			sizeof(DecimateConfig));
	decimateConfig->cnt = 0;
	decimateConfig->y = 0;
	decimateConfig->bits = (int) bits;
	decimateConfig->rate = rate;
	return decimateConfig;
}

void decimateconfig_set(void *p, float bits, float rate) {
	DecimateConfig *config = (DecimateConfig *) p;
	if ((int) bits != 0)
		config->bits = (int) bits;
	config->rate = rate;
}

void decimateconfig_destroy(void *p) {
	if (p != NULL)
		free((DecimateConfig *) p);
}

/********* JNI METHODS **********/
void Java_com_kh_beatbot_effect_Decimate_setDecimateOn(JNIEnv *env,
		jclass clazz, jint trackNum, jboolean on) {
	Track *track = getTrack(env, clazz, trackNum);
	Effect *decimate = &(track->effects[DECIMATE_ID]);
	decimate->on = on;
}

void Java_com_kh_beatbot_effect_Decimate_setDecimateParam(JNIEnv *env,
		jclass clazz, jint trackNum, jint paramNum, jfloat param) {
	Track *track = getTrack(env, clazz, trackNum);
	Effect decimate = track->effects[DECIMATE_ID];
	DecimateConfig *decimateConfig = (DecimateConfig *) decimate.config;
	if (paramNum == 0) { // rate
		decimate.set(decimateConfig, decimateConfig->bits, param);
	} else if (paramNum == 1) { // bits
		// bits range from 4 to 32
		param *= 28;
		param += 4;
		decimate.set(decimateConfig, param, decimateConfig->rate);
	}
}
