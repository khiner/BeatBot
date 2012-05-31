#ifndef EFFECTS_H
#define EFFECTS_H

#include <stdlib.h>

typedef struct delayline_t {
	float *delay; // delayline
	int size;     //  length  in samples
	int rp;       // read pointer
	float fdb;    // feedback amount
} DELAYLINE;

DELAYLINE *delayline_create(float delay, float fdb);

void delayline_process(DELAYLINE *p,float *buffer, int size);

void *delayline_destroy(DELAYLINE *p);

#endif // EFFECTS_H