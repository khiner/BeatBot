#ifndef FILTER_H
#define FILTER_H

#include "effects.h"
#include "../generators/sinewave.h"

#define LP_MODE 0
#define HP_MODE 1
#define BP_MODE 2

typedef struct InnerFitlerConfig_t {
	float a1, a2, a3, b1, b2, c;
} InnerFilterConfig;

typedef struct FilterConfig_t {
	float in1[2], in2[2]; // one for each channel
	float out1[2], out2[2]; // one for each channel
	float baseF, frequency, f, r;
	float modDepth;
	int mode; // is this filter an lp, hp or bp filter?
	float rScale;
	SineWave *mod; // table-based sine wave generator for modulation
	InnerFilterConfig *innerFilterConfig[2];
} FilterConfig;

FilterConfig *filterconfig_create();
void filterconfig_setParam(void *p, float paramNum, float param);

static inline void lpfilterconfig_set(InnerFilterConfig *config,
		float frequency, float r) {
	float f0 = frequency * INV_SAMPLE_RATE;
	// for frequencies < ~ 4000 Hz, approximate the tan function as an optimization.
	config->c = f0 < 0.1f ? 1.0f / (f0 * M_PI) : tan((0.5f - f0) * M_PI);
	config->a1 = 1.0f / (1.0f + r * config->c + config->c * config->c);
	config->a2 = 2.0f * config->a1;
	config->a3 = config->a1;
	config->b1 = 2.0f * (1.0f - config->c * config->c) * config->a1;
	config->b2 = (1.0f - r * config->c + config->c * config->c) * config->a1;
}

static inline void hpfilterconfig_set(InnerFilterConfig *config,
		float frequency, float r) {
	float f0 = frequency * INV_SAMPLE_RATE;
	config->c = f0 < 0.1f ? f0 * M_PI : tan(M_PI * f0);
	config->a1 = 1.0f / (1.0f + r * config->c + config->c * config->c);
	config->a2 = -2.0f * config->a1;
	config->a3 = config->a1;
	config->b1 = 2.0f * (config->c * config->c - 1.0f) * config->a1;
	config->b2 = (1.0f - r * config->c + config->c * config->c) * config->a1;
}

static inline void filterconfig_set(void *p, float f, float r) {
	FilterConfig *config = (FilterConfig *) p;
	// provided cutoff is between 0 and 1.  map this to a value between
	// 0 and samplerate/2 = 21950... - 50 because high frequencies are bad news
	config->f = f;
	f *= SAMPLE_RATE / 2;
	f = f > 50 ? (f < SAMPLE_RATE / 2 - 50 ? f : SAMPLE_RATE / 2 - 50) : 50;
	config->frequency = f;
	config->r = r;
	lpfilterconfig_set(config->innerFilterConfig[0], config->frequency,
			config->r);
	hpfilterconfig_set(config->innerFilterConfig[1], config->frequency,
			config->r);
}

static inline void filter_process(FilterConfig *config, float **buffers,
		int size) {
	int filterNum, channel, samp;
	for (samp = 0; samp < size; samp++) {
		filterconfig_set(config,
				config->baseF
						* (1.0f + config->modDepth * sinewave_tick(config->mod)),
				config->r);
		for (filterNum = 0; filterNum < 2; filterNum++) {
			if (filterNum != config->mode && filterNum != 2) {
				continue;
			}
			for (channel = 0; channel < 2; channel++) {
				float out = config->innerFilterConfig[filterNum]->a1
						* buffers[channel][samp]
						+ config->innerFilterConfig[filterNum]->a2
								* config->in1[channel]
						+ config->innerFilterConfig[filterNum]->a3
								* config->in2[channel]
						- config->innerFilterConfig[filterNum]->b1
								* config->out1[channel]
						- config->innerFilterConfig[filterNum]->b2
								* config->out2[channel];
				config->in2[channel] = config->in1[channel];
				config->in1[channel] = buffers[channel][samp];
				config->out2[channel] = config->out1[channel];
				config->out1[channel] = out;
				buffers[channel][samp] = out;
			}
		}
	}
}

void filterconfig_destroy(void *config);

#endif // FILTER_H
