#include "../all.h"

FilterConfig *filterconfig_create() {
	FilterConfig *config = (FilterConfig *) malloc(sizeof(FilterConfig));
	config->rScale = .7f;
	config->mode = 0;
	config->baseF = SAMPLE_RATE / 4;
	config->modDepth = .5f;
	config->mod = sinewave_create();
	sinewave_setFrequency(config->mod, .5f);
	int filterNum;
	for (filterNum = 0; filterNum < 2; filterNum++) {
		config->inner[filterNum] = malloc(sizeof(InnerFilterConfig));
		config->inner[filterNum]->in1[filterNum] = 0;
		config->inner[filterNum]->in2[filterNum] = 0;
		config->inner[filterNum]->out1[filterNum] = 0;
		config->inner[filterNum]->out2[filterNum] = 0;
	}
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
	FilterConfig *config = (FilterConfig *) p;
	free(config->inner[0]);
	free(config->inner[1]);
	free(config);
}

void filterconfig_setParam(void *p, float paramNumFloat, float param) {
	int paramNum = (int) paramNumFloat;
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
		config->mode = (int) param;
		if ((int) param == LP_MODE || (int)param == HP_MODE) {
			config->rScale = .7f;
		} else if ((int) param == BP_MODE) { // bandpass filter
			// lp and hp are chained, so reduce resonance to avoid clipping
			config->rScale = .4f;
		}
	}
}
