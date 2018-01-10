#include "../all.h"

DecimateConfig *decimateconfig_create() {
	DecimateConfig *decimateConfig = (DecimateConfig *) malloc(
			sizeof(DecimateConfig));
	decimateConfig->cnt = 0;
	decimateConfig->rate = .5f;
	decimateconfig_setParam(decimateConfig, 1, 16);
	return decimateConfig;
}

void decimateconfig_destroy(void *p) {
	if (p != NULL)
		free((DecimateConfig *) p);
}

void decimateconfig_setParam(void *p, float paramNumFloat, float param) {
	DecimateConfig *config = (DecimateConfig *) p;
	switch ((int) paramNumFloat) {
	case 0: // rate converted to 0-1 range
		config->rate = param * INV_SAMPLE_RATE;
		break;
	case 1: // bits
		config->m = 1 << ((int) param - 1); // m = 2^bits
		break;
	}
}
