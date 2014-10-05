#include "../all.h"

FilterConfig *filterconfig_create() {
	FilterConfig *config = (FilterConfig *) malloc(sizeof(FilterConfig));
	config->mode = 0;
	config->baseF = SAMPLE_RATE / 4;
	config->modDepth = .5f;
	config->mod = sinewave_create();
	sinewave_setFrequency(config->mod, SAMPLE_RATE / 2);
	config->y1[0] = config->y1[1] = config->y2[0] = config->y2[1] = 0;
	filterconfig_set(config, SAMPLE_RATE / 2, .5f);
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
	free(config);
}

void filterconfig_setParam(void *p, float paramNumFloat, float param) {
	int paramNum = (int) paramNumFloat;
	FilterConfig *config = (FilterConfig *) p;
	switch (paramNum) {
	case 0:
		config->baseF = param; // frequency
		break;
	case 1:
		config->r = 1 - param; // resonance
		break;
	case 2:
		filterconfig_setModRate(config, param); // mod rate
		break;
	case 3:
		filterconfig_setModDepth(config, param); // mod depth
		break;
	case 4:
		config->mode = (int) param; // mode
		filterconfig_set(config, config->baseF, config->r);
		break;
	}
}
