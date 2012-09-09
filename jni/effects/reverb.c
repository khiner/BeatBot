#include "reverb.h"

static void inject_set(ReverbState *r, int inject) {
	int i;
	for (i = 0; i < numcombs; i++) {
		int off = (1000 - inject) * r->comb[i].size / scaleroom;
		r->comb[i].extpending = r->comb[i].injptr - off;
		if (r->comb[i].extpending < r->comb[i].buffer)
			r->comb[i].extpending += r->comb[i].size;
	}
}

static ReverbState *initReverbState() {
	int inject = 300;
	const int *combtuning = combL;
	const int *alltuning = allL;
	int i;
	ReverbState *r = calloc(1, sizeof(ReverbState));

	r->comb[0].buffer = r->bufcomb0;
	r->comb[1].buffer = r->bufcomb1;
	r->comb[2].buffer = r->bufcomb2;
	r->comb[3].buffer = r->bufcomb3;
	r->comb[4].buffer = r->bufcomb4;
	r->comb[5].buffer = r->bufcomb5;
	r->comb[6].buffer = r->bufcomb6;
	r->comb[7].buffer = r->bufcomb7;

	for (i = 0; i < numcombs; i++)
		r->comb[i].size = combtuning[i];
	for (i = 0; i < numcombs; i++)
		r->comb[i].injptr = r->comb[i].buffer;

	r->allpass[0].buffer = r->bufallpass0;
	r->allpass[1].buffer = r->bufallpass1;
	r->allpass[2].buffer = r->bufallpass2;
	r->allpass[3].buffer = r->bufallpass3;
	for (i = 0; i < numallpasses; i++)
		r->allpass[i].size = alltuning[i];
	for (i = 0; i < numallpasses; i++)
		r->allpass[i].bufptr = r->allpass[i].buffer;

	inject_set(r, inject);
	for (i = 0; i < numcombs; i++)
		r->comb[i].extptr = r->comb[i].extpending;

	return r;
}

ReverbConfig *reverbconfig_create() {
	ReverbConfig *config = (ReverbConfig *) malloc(sizeof(ReverbConfig));
	config->state = initReverbState();
	config->feedback = .5f;
	config->hfDamp = .5f;
	return config;
}

void reverbconfig_destroy(void *p) {
	ReverbConfig *config = (ReverbConfig *) p;
	free(config->state);
	config->state = NULL;
	free(config);
	config = NULL;
}

void reverbconfig_setParam(void *p, float paramNumFloat, float param) {
	int paramNum = (int)paramNumFloat;
	ReverbConfig *config = (ReverbConfig *) p;
	if (paramNum == 0) { // feedback
		config->feedback = param;
	} else if (paramNum == 1) { // hf damp
		config->hfDamp = param;
	}
}

