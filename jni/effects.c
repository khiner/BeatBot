#include "effects.h"
#include <stdlib.h>
#include <android/log.h>

DELAYLINE *delayline_create(float delay, float fdb) {
	// allocate memory and set feedback parameter
	DELAYLINE *p = (DELAYLINE *)malloc(sizeof(DELAYLINE));
	p->size = delay*41000;	
	p->delay = calloc(sizeof(float), p->size);
	p->rp = 0;
	p->fdb = fdb > 0.f ? (fdb < 1.f ? fdb : 0.99999999f) : 0.f;
	return p;
}

void delayline_process(DELAYLINE *p,float *buffer,
					   int size) {
	// process the delay, replacing the buffer
	float out, *delay = p->delay, fdb = p->fdb;
	int i, dsize = p->size, *rp = &(p->rp);
	for(i = 0; i < size; i++){
		out = delay[*rp];
		p->delay[(*rp)++] = buffer[i] + out*fdb;
		if(*rp == dsize) *rp = 0;
		buffer[i] = out;
	}
}

void *delayline_destroy(DELAYLINE *p){
	// free memory
	if(p != NULL) free(p);
}