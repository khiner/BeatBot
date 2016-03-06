#ifndef FILTER_H
#define FILTER_H

#define LP_MODE 0
#define HP_MODE 1
#define BP_MODE 2

typedef struct FilterConfig_t {
	int mode; // is this filter an lp, bp or hp filter?
	float freq, q, baseF, modDepth;
	float a0, b1, b2;

	SineWave *mod; // table-based sine wave generator for modulation
	float y1[2], y2[2]; // one for each channel
} FilterConfig;

FilterConfig *filterconfig_create();
void filterconfig_setParam(void *p, float paramNum, float param);

static inline void lpfilterconfig_set(FilterConfig *config, float freq,
		float q) {
	float qres = (1.0 / q) > 0.001 ? (1.0 / q) : 0.001;
	float pfreq = freq * RADIANS_PER_SAMPLE;
	float D = tan(pfreq * qres * 0.5);
	float C = (1.0 - D) / (1.0 + D);

	config->q = 1.0 / qres;
	config->b1 = (1.0 + C) * cos(pfreq);
	config->a0 = (1.0 + C - config->b1) * 0.25;
	config->b2 = -C;
}

static inline void hpfilterconfig_set(FilterConfig *config, float freq,
		float q) {
	float qres = (1.0 / q) > 0.001 ? (1.0 / q) : 0.001;
	float pfreq = freq * RADIANS_PER_SAMPLE;
	float D = tan(pfreq * qres * 0.5);
	float C = (1.0 - D) / (1.0 + D);

	config->q = 1.0 / qres;
	config->b1 = (1.0 + C) * cos(pfreq);
	config->a0 = (1.0 + C + config->b1) * 0.25;
	config->b2 = -C;
}

static inline void bpfilterconfig_set(FilterConfig *config, float freq,
		float q) {
	float pfreq = freq * RADIANS_PER_SAMPLE;
	float pbw = 1.0 / q * pfreq * 0.5;
	float C = 1.0 / tan(pbw);
	float D = 2.0 * cos(pfreq);

	config->q = q;
	config->a0 = 1.0 / (1.0 + C);
	config->b1 = C * D * config->a0;
	config->b2 = (1.0 - C) * config->a0;
}

static inline void filterconfig_set(void *p, float f, float q) {
	FilterConfig *config = (FilterConfig *) p;
	// provided cutoff is between 0 and SAMPLE_RATE.  clip this to a value between
	// 0 and samplerate/2 = 21950... - 50 because high frequencies are bad news
	config->freq =
			f > 50 ? (f < SAMPLE_RATE / 2 - 50 ? f : SAMPLE_RATE / 2 - 50) : 50;
	switch (config->mode) {
	case LP_MODE:
		lpfilterconfig_set(config, config->freq, q);
		break;
	case BP_MODE:
		bpfilterconfig_set(config, config->freq, q);
		break;
	case HP_MODE:
		hpfilterconfig_set(config, config->freq, q);
		break;
	}
}

static inline float lpfilterconfig_tick(FilterConfig *config, int channel,
		float in) {
	float y0 = config->a0 * in + config->b1 * config->y1[channel] + config->b2 * config->y2[channel];
	float out = y0 + 2 * config->y1[channel] + config->y2[channel];
	config->y2[channel] = config->y1[channel];
	config->y1[channel] = y0;

	return out;
}

static inline float hpfilterconfig_tick(FilterConfig *config, int channel,
		float in) {
	float y0 = config->a0 * in + config->b1 * config->y1[channel]
			+ config->b2 * config->y2[channel];
	float out = y0 - 2 * config->y1[channel] + config->y2[channel];
	config->y2[channel] = config->y1[channel];
	config->y1[channel] = y0;

	return out;
}

static inline float bpfilterconfig_tick(FilterConfig *config, int channel,
		float in) {
	float y0 = in + config->b1 * config->y1[channel]
			+ config->b2 * config->y2[channel];
	float out = config->a0 * (y0 - config->y2[channel]);
	config->y2[channel] = config->y1[channel];
	config->y1[channel] = y0;

	return out;
}

static inline float filterconfig_tick(FilterConfig *config, int channel,
		float sample) {
	switch (config->mode) {
	case LP_MODE:
		return lpfilterconfig_tick(config, channel, sample);
	case BP_MODE:
		return bpfilterconfig_tick(config, channel, sample);
	case HP_MODE:
		return hpfilterconfig_tick(config, channel, sample);
	default:
		return 0;
	}
}

static inline void filter_process(FilterConfig *config, float **buffers,
		int size) {
	int channel, samp;
	for (samp = 0; samp < size; samp++) {
		filterconfig_set(config,
				config->baseF
						* (1.0f + config->modDepth * sinewave_tick(config->mod)),
				config->q);
		for (channel = 0; channel < 2; channel++) {
			buffers[channel][samp] = filterconfig_tick(config, channel,
					buffers[channel][samp]);
		}
	}
}

void filterconfig_destroy(void *config);

#endif // FILTER_H
