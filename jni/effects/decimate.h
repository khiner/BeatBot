#ifndef DECIMATE_H
#define DECIMATE_H

typedef struct DecimateConfig_t {
	float rate; // 0-1
	float cnt;
	float y;
	int bits; // 4-32
	int m; // bits scaling factor = 2^bits
} DecimateConfig;

DecimateConfig *decimateconfig_create();
void decimateconfig_set(void *p, float bits, float rate);
void decimateconfig_setParam(void *p, float paramNum, float param);

static inline void decimate_process(DecimateConfig *config, float **buffers, int size) {
	int i;
	for (i = 0; i < size; i++) {
		config->cnt += config->rate;
		if (config->cnt >= 1) {
			config->cnt -= 1;
			config->y = (long int) (buffers[0][i] * config->m) / (float) config->m;
		}
		buffers[0][i] = buffers[1][i] = config->y;
	}
}

void decimateconfig_destroy(void *p);

#endif // DECIMATE_H
