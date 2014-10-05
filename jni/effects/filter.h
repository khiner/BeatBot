#ifndef FILTER_H
#define FILTER_H

#define LP_MODE 0
#define HP_MODE 1
#define BP_MODE 2

typedef struct FilterConfig_t {
	int mode; // is this filter an lp, hp or bp filter?
	float freq, r, baseF, modDepth;
	float a0, b1, b2;

	SineWave *mod; // table-based sine wave generator for modulation
	float y1[2], y2[2]; // one for each channel
} FilterConfig;

FilterConfig *filterconfig_create();
void filterconfig_setParam(void *p, float paramNum, float param);

static inline void lpfilterconfig_set(FilterConfig *config, float freq,
		float r) {
	float invr = 1.0 / r;
	float qres = invr > 0.01 ? invr : 0.01;
	float pfreq = freq * INV_SAMPLE_RATE * M_PI;
	float D = tan(pfreq * qres * 0.5);
	float C = (1.0 - D) / (1.0 + D);

	config->r = 1.0 / qres;
	config->b1 = (1.0 + C) * cos(pfreq);
	config->a0 = (1.0 + C - config->b1) * 0.25;
	config->b2 = -C;
}

static inline void hpfilterconfig_set(FilterConfig *config, float freq,
		float r) {
	float invr = 1.0 / r;
	float qres = invr > 0.01 ? invr : 0.01;
	float pfreq = freq * INV_SAMPLE_RATE * M_PI;
	float D = tan(pfreq * qres * 0.5);
	float C = (1.0 - D) / (1.0 + D);

	config->r = 1.0 / qres;
	config->b1 = (1.0 + C) * cos(pfreq);
	config->a0 = (1.0 + C + config->b1) * 0.25;
	config->b2 = -C;
}

static inline void bpfilterconfig_set(FilterConfig *config, float freq,
		float r) {
	float pfreq = freq * INV_SAMPLE_RATE * M_PI;
	float pbw = 1.0 / r * pfreq * 0.5;
	float C = 1.0 / tan(pbw);
	float D = 2.0 * cos(pfreq);

	config->r = r;
	config->a0 = 1.0 / (1.0 + C);
	config->b1 = C * D * config->a0;
	config->b2 = (1.0 - C) * config->a0;
}

static inline void filterconfig_set(void *p, float f, float r) {
	FilterConfig *config = (FilterConfig *) p;
	// provided cutoff is between 0 and SAMPLE_RATE.  map this to a value between
	// 0 and samplerate/2 = 21950... - 50 because high frequencies are bad news
	config->freq =
			f > 50 ? (f < SAMPLE_RATE / 2 - 50 ? f : SAMPLE_RATE / 2 - 50) : 50;
	config->r = r;
	switch (config->mode) {
	case LP_MODE:
		lpfilterconfig_set(config, config->freq, config->r);
		break;
	case HP_MODE:
		hpfilterconfig_set(config, config->freq, config->r);
		break;
	case BP_MODE:
		bpfilterconfig_set(config, config->freq, config->r);
		break;
	}
}

static inline float lpfilterconfig_tick(FilterConfig *config, int channel,
		float in) {
	float y0 = in + config->b1 * config->y1[channel] + config->b2 * config->y2[channel];
	float out = config->a0 * (y0 + 2 * config->y1[channel] + config->y2[channel]);
	config->y2[channel] = config->y1[channel];
	config->y1[channel] = y0;

	return out;
}

static inline float hpfilterconfig_tick(FilterConfig *config, int channel,
		float in) {
	float y0 = in + config->b1 * config->y1[channel]
			+ config->b2 * config->y2[channel];
	float out = config->a0
			* (y0 - 2 * config->y1[channel] + config->y2[channel]);
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
	case HP_MODE:
		return hpfilterconfig_tick(config, channel, sample);
	case BP_MODE:
		return bpfilterconfig_tick(config, channel, sample);
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
				config->r);
		for (channel = 0; channel < 2; channel++) {
			buffers[channel][samp] = filterconfig_tick(config, channel,
					buffers[channel][samp]);
		}
	}
}

void filterconfig_destroy(void *config);

#endif // FILTER_H
