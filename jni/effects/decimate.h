#ifndef DECIMATE_H
#define DECIMATE_H

#include "effects.h"

typedef struct DecimateConfig_t {
	int bits; // 4-32
	float rate; // 0-1
	float cnt;
	float y;
} DecimateConfig;

DecimateConfig *decimateconfig_create(float bits, float rate);
void decimateconfig_set(void *p, float bits, float rate);

static inline void decimate_process(DecimateConfig *config, float **buffers, int size) {
	int m = 1 << (config->bits - 1);
	int i;
	for (i = 0; i < size; i++) {
		config->cnt += config->rate;
		if (config->cnt >= 1) {
			config->cnt -= 1;
			config->y = (long int) (buffers[0][i] * m) / (float) m;
		}
		buffers[0][i] = buffers[1][i] = config->y;
	}
}

void decimateconfig_destroy(void *p);

#endif // DECIMATE_H
