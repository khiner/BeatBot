#include "decimate.h"

DecimateConfig *decimateconfig_create() {
	DecimateConfig *decimateConfig = (DecimateConfig *) malloc(
			sizeof(DecimateConfig));
	decimateConfig->cnt = 0;
	decimateConfig->y = 0;
	decimateConfig->bits = 16;
	decimateConfig->rate = .5f;
	return decimateConfig;
}

void decimateconfig_destroy(void *p) {
	if (p != NULL)
		free((DecimateConfig *) p);
}

void decimateconfig_setParam(void *p, float paramNumFloat, float param) {
	int paramNum = (int)paramNumFloat;
	DecimateConfig *config = (DecimateConfig *) p;
	if (paramNum == 0) { // rate
		config->rate = param;
	} else if (paramNum == 1) { // bits
		// bits range from 4 to 32
		param *= 28;
		param += 4;
		if ((int) param != 0)
			config->bits = (int) param;
	}
}
