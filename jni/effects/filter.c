#include "../all.h"

FilterConfig *filterconfig_create() {
	FilterConfig *config = (FilterConfig *) malloc(sizeof(FilterConfig));
	config->mode = 0;
	config->baseF = SAMPLE_RATE / 4;
	config->q = 0.5f;
	config->modDepth = 0.5f;
	config->mod = sinewave_create();
	sinewave_setFrequency(config->mod, 0.5f);
	config->y1[0] = config->y1[1] = config->y2[0] = config->y2[1] = 0;
	filterconfig_set(config, config->baseF, config->q);
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
	FilterConfig *config = (FilterConfig *) p;
	switch ((int) paramNumFloat) {
	case 0:
		config->baseF = param; // frequency
		break;
	case 1:
		config->q = 1 - param; // resonance
		break;
	case 2:
		filterconfig_setModRate(config, param); // mod rate
		break;
	case 3:
		filterconfig_setModDepth(config, param); // mod depth
		break;
	case 4:
		config->mode = (int) param; // mode
		filterconfig_set(config, config->baseF, config->q);
		break;
	}
}
