#ifndef REVERB_H
#define REVERB_H

#include "effects.h"

typedef struct allpass {
	float *buffer;
	float *bufptr;
	int size;
} allpass_state;

typedef struct comb {
	float filterstore;
	float *buffer;
	float *injptr;
	float *extptr;
	float *extpending;
	int size;
} comb_state;

#define numcombs 6
#define numallpasses 4
#define scalehfdamp 0.4f
#define scaleliveness 0.4f
#define offsetliveness 0.58f
#define scaleroom 1111
#define stereospread 23
#define fixedgain 0.09f

#define comb0 1116
#define comb1 1188
#define comb2 1277
#define comb3 1356
#define comb4 1422
#define comb5 1491
#define comb6 1557
#define comb7 1617

#define all0 556
#define all1 441
#define all2 341
#define all3 225

/* These values assume 44.1KHz sample rate
 they will probably be OK for 48KHz sample rate
 but would need scaling for 96KHz (or other) sample rates.
 The values were obtained by listening tests. */

static const int combL[numcombs] = { comb0, comb1, comb2, comb3, comb4, comb5 };

static const int combR[numcombs] = { comb0 + stereospread, comb1 + stereospread,
		comb2 + stereospread, comb3 + stereospread, comb4 + stereospread, comb5
				+ stereospread };

static const int allL[numallpasses] = { all0, all1, all2, all3, };

static const int allR[numallpasses] = { all0 + stereospread,
		all1 + stereospread, all2 + stereospread, all3 + stereospread, };

/* enough storage for L or R */
typedef struct ReverbState_t {
	comb_state comb[numcombs];
	allpass_state allpass[numallpasses];

	float bufcomb0[comb0 + stereospread];
	float bufcomb1[comb1 + stereospread];
	float bufcomb2[comb2 + stereospread];
	float bufcomb3[comb3 + stereospread];
	float bufcomb4[comb4 + stereospread];
	float bufcomb5[comb5 + stereospread];
	float bufcomb6[comb6 + stereospread];
	float bufcomb7[comb7 + stereospread];

	float bufallpass0[all0 + stereospread];
	float bufallpass1[all1 + stereospread];
	float bufallpass2[all2 + stereospread];
	float bufallpass3[all3 + stereospread];

	int energy;
} ReverbState;

typedef struct ReverbConfig_t {
	ReverbState *state;
	float feedback;
	float hfDamp;
	float wet;

	int inject;
	int width;
} ReverbConfig;

static inline void underguard(float *x) {
	union {
		u_int32_t i;
		float f;
	} ix;
	ix.f = *x;
	if ((ix.i & 0x7f800000) == 0)
		*x = 0.0f;
}

static inline float allpass_process(allpass_state *a, float input) {
	float val = *a->bufptr;
	float output = val - input;

	*a->bufptr = val * .5f + input;
	underguard(a->bufptr);

	if (a->bufptr <= a->buffer)
		a->bufptr += a->size;
	--a->bufptr;

	return output;
}

static inline float comb_process(comb_state *c, float feedback, float hfDamp,
		float input) {
	float val = *c->extptr;
	c->filterstore = val + (c->filterstore - val) * hfDamp * scalehfdamp;
	underguard(&c->filterstore);

	*c->injptr = input + c->filterstore * feedback;
	underguard(c->injptr);

	if (c->injptr <= c->buffer)
		c->injptr += c->size;
	--c->injptr;
	if (c->extptr <= c->buffer)
		c->extptr += c->size;
	--c->extptr;

	return val;
}

ReverbConfig *reverbconfig_create(float feedback, float hfDamp);
void reverbconfig_set(void *config, float feedback, float hfDamp);

static inline void reverb_process(ReverbConfig *config, float **buffers, int size) {
	float out, val = 0;
	int i, j;

	for (i = 0; i < size; i++) {
		out = 0;
		val = buffers[0][i];
		for (j = 0; j < numcombs; j++)
			out += comb_process(config->state->comb + j, config->feedback,
					config->hfDamp, val);
		for (j = 0; j < numallpasses; j++)
			out = allpass_process(config->state->allpass + j, out);
		buffers[0][i] = buffers[1][i] = out * fixedgain;
	}
}

void reverbconfig_destroy(void *config);

#endif // REVERB_H
