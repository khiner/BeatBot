#include "filter.h"

FilterConfig *filterconfig_create(float f, float r, bool lp) {
	FilterConfig *config = (FilterConfig *) malloc(sizeof(FilterConfig));
	config->rScale = .7f;
	config->lp = lp;
	config->baseF = f;
	config->in1[0] = config->in1[1] = 0;
	config->in2[0] = config->in2[1] = 0;
	config->out1[0] = config->out1[1] = 0;
	config->out2[0] = config->out2[1] = 0;
	config->modDepth = .5f;
	config->mod = sinewave_create();
	sinewave_setFrequency(config->mod, .5f);
	filterconfig_set(config, f, r);
	return config;
}

void filterconfig_setModRate(FilterConfig *config, float rate) {
	sinewave_setFrequency(config->mod, rate);
}

void filterconfig_setModDepth(FilterConfig *config, float depth) {
	config->modDepth = depth;
}

void filterconfig_destroy(void *p) {
	free((FilterConfig *) p);
}

/********* JNI METHODS **********/
void Java_com_kh_beatbot_effect_Filter_setFilterOn(JNIEnv *env, jclass clazz,
		jint trackNum, jboolean on, jint mode) {
	Track *track = getTrack(env, clazz, trackNum);
	Effect *lpFilter = &(track->effects[LP_FILTER_ID]);
	Effect *hpFilter = &(track->effects[HP_FILTER_ID]);
	FilterConfig *lpConfig = (FilterConfig *) lpFilter->config;
	FilterConfig *hpConfig = (FilterConfig *) hpFilter->config;
	if (!on) {
		lpFilter->on = hpFilter->on = false;
	} else if (mode == 0) { // lowpass filter
		lpConfig->rScale = hpConfig->rScale = .7f;
		hpFilter->on = false;
		lpFilter->on = true;
	} else if (mode == 1) { // bandpass filter - chain lp and hp filters
		lpConfig->rScale = hpConfig->rScale = .4f;
		lpFilter->on = hpFilter->on = true;
	} else if (mode == 2) { // highpass filter
		lpConfig->rScale = hpConfig->rScale = .7f;
		lpFilter->on = false;
		hpFilter->on = true;
	}
}

void Java_com_kh_beatbot_effect_Filter_setFilterParam(JNIEnv *env,
		jclass clazz, jint trackNum, jint paramNum, jfloat param) {
	Track *track = getTrack(env, clazz, trackNum);
	Effect lpFilter = track->effects[LP_FILTER_ID];
	Effect hpFilter = track->effects[HP_FILTER_ID];
	FilterConfig *lpConfig = (FilterConfig *) lpFilter.config;
	FilterConfig *hpConfig = (FilterConfig *) hpFilter.config;
	if (paramNum == 0) { // frequency
		((FilterConfig *) lpFilter.config)->baseF = param;
		((FilterConfig *) hpFilter.config)->baseF = param;
	} else if (paramNum == 1) { // resonance
		param = 1 - param * lpConfig->rScale; // flip and scale to .3 to 1
		lpFilter.set(lpConfig, lpConfig->f, param);
		hpFilter.set(hpConfig, hpConfig->f, param);
	} else if (paramNum == 2) { // mod rate
		filterconfig_setModRate(lpFilter.config, param);
		filterconfig_setModRate(hpFilter.config, param);
	} else if (paramNum == 3) { // mod depth
		filterconfig_setModDepth(lpFilter.config, param);
		filterconfig_setModDepth(hpFilter.config, param);
	}
}
