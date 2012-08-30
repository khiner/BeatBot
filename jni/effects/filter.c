#include "filter.h"

FilterConfig *filterconfig_create(float f, float r, bool lp) {
	FilterConfig *config = (FilterConfig *) malloc(sizeof(FilterConfig));
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

void filterconfig_set(void *p, float f, float r) {
	FilterConfig *config = (FilterConfig *) p;
	// provided cutoff is between 0 and 1.  map this to a value between
	// 0 and samplerate/2 = 21950... - 50 because high frequencies are bad news
	config->f = f;
	f *= 22000;
	f = f > 50 ? (f < 21950 ? f : 21950) : 50;
	config->frequency = f;
	config->r = r;
	float f0 = config->frequency * INV_SAMPLE_RATE;
	if (config->lp) {
		// for frequencies < ~ 4000 Hz, approximate the tan function as an optimization.
		config->c = f0 < 0.1f ? 1.0f / (f0 * M_PI) : tan((0.5f - f0) * M_PI);
		config->a1 = 1.0f
				/ (1.0f + config->r * config->c + config->c * config->c);
		config->a2 = 2.0f * config->a1;
		config->a3 = config->a1;
		config->b1 = 2.0f * (1.0f - config->c * config->c) * config->a1;
	} else {
		config->c = f0 < 0.1f ? f0 * M_PI : tan(M_PI * f0);
		config->a1 = 1.0f
				/ (1.0f + config->r * config->c + config->c * config->c);
		config->a2 = -2.0f * config->a1;
		config->a3 = config->a1;
		config->b1 = 2.0f * (config->c * config->c - 1.0f) * config->a1;
	}
	config->b2 = (1.0f - config->r * config->c + config->c * config->c)
			* config->a1;
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

void Java_com_kh_beatbot_EffectActivity_setFilterParam(JNIEnv *env,
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
		param = 1 - param * .7f; // flip and scale to .3 to 1
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

