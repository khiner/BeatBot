#include "filter.h"

FilterConfig *filterconfig_create(float f, float r) {
	FilterConfig *config = (FilterConfig *) malloc(sizeof(FilterConfig));
	config->in1[0] = config->in1[1] = 0;
	config->in2[0] = config->in2[1] = 0;
	config->out1[0] = config->out1[1] = 0;
	config->out2[0] = config->out2[1] = 0;
	filterconfig_setLp(config, f, r);
	return config;
}

void filterconfig_setLp(void *p, float f, float r) {
	FilterConfig *config = (FilterConfig *) p;
	config->f = f;
	config->r = r;
	float f0 = f * INV_SAMPLE_RATE;
	// for frequencies < ~ 4000 Hz, approximate the tan function as an optimization.
	config->c = f0 < 0.1f ? 1.0f / (f0 * M_PI) : tan((0.5f - f0) * M_PI);
	config->a1 = 1.0f / (1.0f + config->r * config->c + config->c * config->c);
	config->a2 = 2.0f * config->a1;
	config->a3 = config->a1;
	config->b1 = 2.0f * (1.0f - config->c * config->c) * config->a1;
	config->b2 = (1.0f - config->r * config->c + config->c * config->c)
			* config->a1;
}

void filterconfig_setHp(void *p, float f, float r) {
	FilterConfig *config = (FilterConfig *) p;
	config->f = f;
	config->r = r;
	float f0 = f * INV_SAMPLE_RATE;
	config->c = f0 < 0.1f ? f0 * M_PI : tan(M_PI * f0);
	config->a1 = 1.0f / (1.0f + config->r * config->c + config->c * config->c);
	config->a2 = -2.0f * config->a1;
	config->a3 = config->a1;
	config->b1 = 2.0f * (config->c * config->c - 1.0f) * config->a1;
	config->b2 = (1.0f - config->r * config->c + config->c * config->c)
			* config->a1;
}

void filterconfig_destroy(void *p) {
	free((FilterConfig *) p);
}

/********* JNI METHODS **********/
void Java_com_kh_beatbot_EffectActivity_setFilterOn(JNIEnv *env, jclass clazz,
		jint trackNum, jboolean on, jint mode) {
	Track *track = getTrack(env, clazz, trackNum);
	Effect *lpFilter = &(track->effects[LP_FILTER_ID]);
	Effect *hpFilter = &(track->effects[HP_FILTER_ID]);
	if (!on) {
		lpFilter->on = false;
		hpFilter->on = false;
	} else if (mode == 0) { // lowpass filter
		hpFilter->on = false;
		lpFilter->on = true;
	} else if (mode == 1) { // bandpass filter - chain lp and hp filters
		lpFilter->on = true;
		hpFilter->on = true;
	} else if (mode == 2) { // highpass filter
		lpFilter->on = false;
		hpFilter->on = true;
	}
}

void Java_com_kh_beatbot_FilterActivity_setFilterMode(JNIEnv *env, jclass clazz,
		jint trackNum, jint mode) {
	Track *track = getTrack(env, clazz, trackNum);
	Effect *lpFilter = &(track->effects[LP_FILTER_ID]);
	Effect *hpFilter = &(track->effects[HP_FILTER_ID]);
	if (mode == 0) { // lowpass filter
		hpFilter->on = false;
		lpFilter->on = true;
	} else if (mode == 1) { // bandpass filter - chain lp and hp filters
		lpFilter->on = true;
		hpFilter->on = true;
	} else if (mode == 2) { // highpass filter
		lpFilter->on = false;
		hpFilter->on = true;
	}
}

void Java_com_kh_beatbot_EffectActivity_setFilterParam(JNIEnv *env,
		jclass clazz, jint trackNum, jint paramNum, jfloat param) {
	Track *track = getTrack(env, clazz, trackNum);
	Effect lpFilter = track->effects[LP_FILTER_ID];
	Effect hpFilter = track->effects[HP_FILTER_ID];
	FilterConfig *lpConfig = (FilterConfig *) lpFilter.config;
	FilterConfig *hpConfig = (FilterConfig *) hpFilter.config;
	if (paramNum == 0) { // cutoff
		// provided cutoff is between 0 and 1.  map this to a value between
		// 0 and samplerate/2 = 22050... - 50 because high frequencies are bad news
		param *= 22000.0f;
		param = param < 0.01f ? 0.01f : param;
		lpFilter.set(lpConfig, param, lpConfig->r);
		hpFilter.set(hpConfig, param, hpConfig->r);
	} else if (paramNum == 1) {
		param = param < 0.011f ? 0.011f : param;
		lpFilter.set(lpConfig, lpConfig->f, param);
		hpFilter.set(hpConfig, hpConfig->f, param);
	}
}

