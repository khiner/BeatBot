#include "decimate.h"

DecimateConfig *decimateconfig_create(float bits, float rate) {
	DecimateConfig *decimateConfig = (DecimateConfig *) malloc(
			sizeof(DecimateConfig));
	decimateConfig->cnt = 0;
	decimateConfig->y = 0;
	decimateConfig->bits = (int) bits;
	decimateConfig->rate = rate;
	return decimateConfig;
}

void decimateconfig_set(void *p, float bits, float rate) {
	DecimateConfig *config = (DecimateConfig *) p;
	if ((int) bits != 0)
		config->bits = (int) bits;
	config->rate = rate;
}

void decimateconfig_destroy(void *p) {
	if (p != NULL)
		free((DecimateConfig *) p);
}
