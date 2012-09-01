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
	float rScale;
	SineWave *mod; // table-based sine wave generator for modulation
} FilterConfig;

FilterConfig *filterconfig_create(float cutoff, float r, bool lp);

static inline void filterconfig_set(void *p, float f, float r) {
	FilterConfig *config = (FilterConfig *) p;
	// provided cutoff is between 0 and 1.  map this to a value between
	// 0 and samplerate/2 = 21950... - 50 because high frequencies are bad news
	config->f = f;
	f *= SAMPLE_RATE / 2;
	f = f > 50 ? (f < SAMPLE_RATE / 2 - 50 ? f : SAMPLE_RATE / 2 - 50) : 50;
	config->frequency = f;
	config->r = r;
	float f0 = config->frequency * INV_SAMPLE_RATE;
	if (config->lp) {
		// for frequencies < ~ 4000 Hz, approximate the tan function as an optimization.
		config->c = f0 < 0.1f ? 1.0f / (f0 * M_PI) : tan((0.5f - f0) * M_PI);
		config->a1 = 1.0f
				/ (1.0f + config->r * config->c + config->c * config->c);
		config->a2 = 2.0f * config->a1;
		config->a3 = config->a1;
		config->b1 = 2.0f * (1.0f - config->c * config->c) * config->a1;
	} else {
		config->c = f0 < 0.1f ? f0 * M_PI : tan(M_PI * f0);
		config->a1 = 1.0f
				/ (1.0f + config->r * config->c + config->c * config->c);
		config->a2 = -2.0f * config->a1;
		config->a3 = config->a1;
		config->b1 = 2.0f * (config->c * config->c - 1.0f) * config->a1;
	}
	config->b2 = (1.0f - config->r * config->c + config->c * config->c)
			* config->a1;
}

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
