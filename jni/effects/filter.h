#ifndef FILTER_H
#define FILTER_H

#include "effects.h"
#include "../generators/sinewave.h"

typedef struct FilterConfig_t {
	float in1[2], in2[2]; // one for each channel
	float out1[2], out2[2]; // one for each channel
	float a1, a2, a3, b1, b2;
	float baseF, frequency, f, c, r;
	float modDepth;bool lp; // is this filter an lp or hp filter?
	SineWave *mod; // table-based sine wave generator for modulation
} FilterConfig;

FilterConfig *filterconfig_create(float cutoff, float r, bool lp);
void filterconfig_set(void *config, float cutoff, float r);

static inline void filter_process(FilterConfig *config, float **buffers,
		int size) {
	int channel, samp;
	for (samp = 0; samp < size; samp++) {
		filterconfig_set(config,
				config->baseF * (1.0f + config->modDepth * sinewave_tick(config->mod)), config->r);
		for (channel = 0; channel < 2; channel++) {
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
