#include "effects.h"
#include <stdlib.h>
#include <android/log.h>

VolumePanConfig *volumepanconfig_create(float volume, float pan) {
        VolumePanConfig *p = (VolumePanConfig *)malloc(sizeof(VolumePanConfig));
	p->volume = volume;
	p->pan = pan;
	return p;
}

void volumepanconfig_set(VolumePanConfig *p, float volume, float pan) {
	p->volume = volume;
	p->pan = pan;
}

void volumepan_process(VolumePanConfig *p, float buffer[], int size) {
	float leftVolume = (1 - p->pan)*p->volume;
	float rightVolume = p->pan*p->volume;

	int i;
	for (i = 0; i < size; i+=2) {
      	        // left channel
		buffer[i] = buffer[i]*leftVolume;
                // right channel
		buffer[i+1] = buffer[i + 1]*rightVolume;
	}  
}

void *volumepanconfig_destroy(VolumePanConfig *p) {
        if(p != NULL) free(p);
}

DelayConfig *delayconfig_create(float delay, float fdb) {
	// allocate memory and set feedback parameter
	DelayConfig *p = (DelayConfig *)malloc(sizeof(DelayConfig));
	p->size = delay*41000;	
	p->delay = calloc(sizeof(float), p->size);
	p->rp = 0;
	p->fdb = fdb > 0.f ? (fdb < 1.f ? fdb : 0.99999999f) : 0.f;
	return p;
}

void delay_process(DelayConfig *p,float buffer[], int size) {
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

void *delayconfig_destroy(DelayConfig *p){
	// free memory
	if(p != NULL) free(p);
}

FilterConfig *filterconfig_create(float cutoff, float q) {
        FilterConfig *filterConfig = malloc(sizeof(FilterConfig));
	filterConfig->history1 = 0;
	filterConfig->history2 = 0;
	filterConfig->history3 = 0;
	filterConfig->history4 = 0;

	float pi = 3.1415926535897;
	float fs = 44100; // sample rate

	filterConfig->t0 = 4.f * fs * fs;
	filterConfig->t1 = 8.f * fs * fs;
	filterConfig->t2 = 2.f * fs;
	filterConfig->t3 = pi / fs;

	filterConfig->min_cutoff = fs * 0.01f;
	filterConfig->max_cutoff = fs * 0.45f;
	filterconfig_set(filterConfig, cutoff, q);
	return filterConfig;
}

void filterconfig_set(FilterConfig *filterConfig, float cutoff, float q) {
        if (cutoff < filterConfig->min_cutoff)
            cutoff = filterConfig->min_cutoff;
        else if(cutoff > filterConfig->max_cutoff)
            cutoff = filterConfig->max_cutoff;

	if(q < 0.f)
   	    q = 0.f;
	else if(q > 1.f)
	    q = 1.f;

	float wp = filterConfig->t2 * tanf(filterConfig->t3 * cutoff);
	float bd, bd_tmp, b1, b2;

	q *= BUDDA_Q_SCALE;
	q += 1.f;

	b1 = (0.765367f / (q*wp));
	b2 = 1.f / (wp * wp);

	bd_tmp = filterConfig->t0 * b2 + 1.f;

	bd = 1.f / (bd_tmp + filterConfig->t2 * b1);

	filterConfig->gain = bd;

	filterConfig->coef2 = (2.f - filterConfig->t1 * b2);

	filterConfig->coef0 = filterConfig->coef2 * bd;
	filterConfig->coef1 = (bd_tmp - filterConfig->t2 * b1) * bd;

	b1 = (1.847759f / (q*wp));

	bd = 1.f / (bd_tmp + filterConfig->t2 * b1);

	filterConfig->gain *= bd;
	filterConfig->coef2 *= bd;
	filterConfig->coef3 = (bd_tmp - filterConfig->t2 * b1) * bd;
}

void filter_process(FilterConfig *p, float buffer[], int size) {
	int i;
	for (i = 0; i < size; i++) {
	    float output = buffer[i] * p->gain;
	    float new_hist;

	    output -= p->history1 * p->coef0;
	    new_hist = output - p->history2 * p->coef1;

	    output = new_hist + p->history1 * 2.f;
	    output += p->history2;

	    p->history2 = p->history1;
	    p->history1 = new_hist;

	    output -= p->history3 * p->coef2;
	    new_hist = output - p->history4 * p->coef3;

	    output = new_hist + p->history3 * 2.f;
	    output += p->history4;

	    p->history4 = p->history3;
	    p->history3 = new_hist;

	    buffer[i] = output;
	}
}

void *filterconfig_destroy(FilterConfig *p) {
	if(p != NULL) free(p);
}

DecimateConfig *decimateconfig_create(int bits, float rate) {
        DecimateConfig *decimateConfig = malloc(sizeof(DecimateConfig));
	decimateConfig->cnt = 0;
	decimateConfig->y = 0;
	decimateConfig->bits = bits;
	decimateConfig->rate = rate;
}

void decimate_process(DecimateConfig *p, float buffer[], int size) {
        long int m = 1 << (p->bits - 1);
	int i;
	for (i = 0; i < size; i++) {
	    p->cnt += p->rate;
	    if (p->cnt >= 1) {
	        p->cnt -= 1;
		p->y = (long int)(buffer[i]*m)/(float)m;
	    }
	    buffer[i] = p->y;
	}
}

void decimateconfig_destroy(DecimateConfig *p) {
	if(p != NULL) free(p);
}

void swap(float *a , float *b) {
    float tmp;
    tmp = *a;
    (*a) = (*b);
    (*b) = tmp;
}

void reverse(float buffer[], int begin, int end) {
	int i, j;
    //swap 1st with last, then 2nd with last-1, etc.  Till we reach the middle of the string.
	for (i = begin, j = end - 1; i < j; i++, j--) {
        swap( &buffer[i] , &buffer[j]);	
	}
}

void normalize(float buffer[], int size) {
	float maxSample = 0;	
	int i;
	for (i = 0; i < size; i++) {
		if (abs(buffer[i]) > maxSample) {
			maxSample = abs(buffer[i]);
		}
	}
	if (maxSample != 0) {
		for (i = 0; i < size; i++) {
			buffer[i] /= maxSample;
		}
	}		
}
