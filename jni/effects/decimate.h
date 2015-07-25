#ifndef DECIMATE_H
#define DECIMATE_H

typedef struct DecimateConfig_t {
	float rate; // 0-1
	float cnt;
	int m; // bits scaling factor = 2^bits (4-32)
} DecimateConfig;

DecimateConfig *decimateconfig_create();
void decimateconfig_set(void *p, float bits, float rate);
void decimateconfig_setParam(void *p, float paramNum, float param);

static inline void decimate_process(DecimateConfig *config, float **buffers,
		int size) {
	int channel, samp;
	for (samp = 0; samp < size; samp++) {
		config->cnt += config->rate;
		if (config->cnt >= 1) {
			config->cnt -= 1;
			for (channel = 0; channel < 2; channel++) {
				buffers[channel][samp] = (long int) (buffers[channel][samp]
						* config->m) / (float) config->m;
			}
		}
	}
}

void decimateconfig_destroy(void *p);

#endif // DECIMATE_H
