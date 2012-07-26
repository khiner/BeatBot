#ifndef FILTER_H
#define FILTER_H

#include "effects.h"

typedef struct FilterConfig_t {
	float a1, a2, a3, b1, b2;
	float f, c, r;
	float in1[2], in2[2]; // one for each channel
	float out1[2], out2[2]; // one for each channel
} FilterConfig;

FilterConfig *filterconfig_create(float cutoff, float r);
void filterconfig_setLp(void *config, float cutoff, float r);
void filterconfig_setHp(void *config, float cutoff, float r);

static inline void filter_process(void *p, float **buffers, int size) {
	FilterConfig *config = (FilterConfig *) p;
	int channel, samp;
	for (channel = 0; channel < 2; channel++) {
		for (samp = 0; samp < size; samp++) {
			float out = config->a1 * buffers[channel][samp]
					+ config->a2 * config->in1[channel]
					+ config->a3 * config->in2[channel]
					- config->b1 * config->out1[channel]
					- config->b2 * config->out2[channel];
			config->in2[channel] = config->in1[channel];
			config->in1[channel] = buffers[channel][samp];
			config->out2[channel] = config->out1[channel];
			config->out1[channel] = out;
			buffers[channel][samp] = out;
		}
	}
}

void filterconfig_destroy(void *config);

#endif // FILTER_H
