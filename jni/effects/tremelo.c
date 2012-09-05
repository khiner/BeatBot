#include "tremelo.h"

TremeloConfig *tremeloconfig_create(float freq, float depth) {
	TremeloConfig *config = malloc(sizeof(TremeloConfig));
	config->mod[0] = sinewave_create();
	config->mod[1] = sinewave_create();
	tremeloconfig_setFrequency(config, freq);
	tremeloconfig_setDepth(config, depth);
	return config;
}

void tremeloconfig_set(void *p, float freq, float depth) {
	TremeloConfig *config = (TremeloConfig *)p;
	sinewave_setFrequency(config->mod[0], freq);
	sinewave_setFrequency(config->mod[1], freq);
	config->depth = depth;
}

void tremeloconfig_setFrequency(TremeloConfig *config, float freq) {
	sinewave_setFrequency(config->mod[0], freq);
	sinewave_setFrequency(config->mod[1], freq);
}

void tremeloconfig_setPhase(TremeloConfig *config, float phase) {
	sinewave_addPhaseOffset(config->mod[1], phase);
}

void tremeloconfig_setDepth(TremeloConfig *config, float depth) {
	config->depth = depth;
}

void tremeloconfig_destroy(void *p) {
	TremeloConfig *config = (TremeloConfig *)p;
	free(config->mod[0]);
	free(config->mod[1]);
	free(config);
}


/********* JNI METHODS **********/
void Java_com_kh_beatbot_effect_Tremelo_setTremeloOn(JNIEnv *env,
		jclass clazz, jint trackNum, jint on) {
	Track *track = getTrack(env, clazz, trackNum);
	Effect *tremelo = &(track->effects[TREMELO_ID]);
	tremelo->on = on;
}

void Java_com_kh_beatbot_effect_Tremelo_setTremeloParam(JNIEnv *env,
		jclass clazz, jint trackNum, jint paramNum, jfloat param) {
	Track *track = getTrack(env, clazz, trackNum);
	TremeloConfig *config = (TremeloConfig*)track->effects[TREMELO_ID].config;
	if (paramNum == 0) { // mod frequency
		tremeloconfig_setFrequency(config, param);
	} else if (paramNum == 1) { // phase
		tremeloconfig_setPhase(config, param);
	} else if (paramNum == 2) { // depth
		tremeloconfig_setDepth(config, param);
	}
}
