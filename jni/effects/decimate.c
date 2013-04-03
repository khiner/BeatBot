#include "../all.h"

DecimateConfig *decimateconfig_create() {
	DecimateConfig *decimateConfig = (DecimateConfig *) malloc(
			sizeof(DecimateConfig));
	decimateConfig->cnt = 0;
	decimateConfig->y = 0;
	decimateConfig->rate = .5f;
	decimateconfig_setParam(decimateConfig, 1, 16);
	return decimateConfig;
}

void decimateconfig_destroy(void *p) {
	if (p != NULL)
		free((DecimateConfig *) p);
}

void decimateconfig_setParam(void *p, float paramNumFloat, float param) {
	int paramNum = (int)paramNumFloat;
	DecimateConfig *config = (DecimateConfig *) p;
	if (paramNum == 0) { // rate converted to 0-1 range
		config->rate = param * INV_SAMPLE_RATE;
	} else if (paramNum == 1) { // bits
		config->bits = (int) param;
		config->m = 1 << (config->bits - 1); // m = 2^bits
	}
}
