#include "filter.h"

FilterConfig *filterconfig_create() {
	FilterConfig *config = (FilterConfig *) malloc(sizeof(FilterConfig));
	config->innerFilterConfig[0] = malloc(sizeof(InnerFilterConfig));
	config->innerFilterConfig[1] = malloc(sizeof(InnerFilterConfig));
	config->rScale = .7f;
	config->mode = 0;
	config->baseF = .5f;
	config->in1[0] = config->in1[1] = 0;
	config->in2[0] = config->in2[1] = 0;
	config->out1[0] = config->out1[1] = 0;
	config->out2[0] = config->out2[1] = 0;
	config->modDepth = .5f;
	config->mod = sinewave_create();
	sinewave_setFrequency(config->mod, .5f);
	filterconfig_set(config, .5f, .5f);
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

void filterconfig_setParam(void *p, float paramNumFloat, float param) {
	int paramNum = (int)paramNumFloat;
	FilterConfig *config = (FilterConfig *) p;
	if (paramNum == 0) { // frequency
		config->baseF = param;
	} else if (paramNum == 1) { // resonance
		param = 1 - param * config->rScale; // flip and scale to .3 to 1
		config->r = param;
	} else if (paramNum == 2) { // mod rate
		filterconfig_setModRate(config, param);
	} else if (paramNum == 3) { // mod depth
		filterconfig_setModDepth(config, param);
	} else if (paramNum == 4) { // mode
		config->mode = (int)param;
		if ((int)param == 0) { // lowpass filter
			config->rScale = .7f;
		} else if ((int)param == 1) { // bandpass filter - chain lp and hp filters
			config->rScale = .4f;
		} else if ((int)param == 2) { // highpass filter
			config->rScale = .7f;
		}
	}
}
