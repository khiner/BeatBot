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
