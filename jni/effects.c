#include "effects.h"
#include <stdlib.h>
#include <android/log.h>

void initEffect(Effect *effect, bool on, bool dynamic, void *config,
				void (*set), void (*process), void (*destroy)) {
	effect->on = on;
	effect->dynamic = dynamic;
	effect->config = config;
	effect->set = set;
	effect->process = process;
	effect->destroy = destroy;				
}

DecimateConfig *decimateconfig_create(float bits, float rate) {
    DecimateConfig *decimateConfig = malloc(sizeof(DecimateConfig));
	decimateConfig->cnt = 0;
	decimateConfig->y = 0;
	decimateConfig->bits = (int)bits;
	decimateConfig->rate = rate;
	return decimateConfig;
}

void decimateconfig_set(void *p, float bits, float rate) {
	DecimateConfig *config = (DecimateConfig *)p;
	if ((int)bits != 0)
		config->bits = (int)bits;
	config->rate = rate;	
}

void decimate_process(void *p, float buffer[], int size) {
	DecimateConfig *config = (DecimateConfig *)p;
    int m = 1 << (config->bits - 1);
	int i;
	for (i = 0; i < size; i++) {
	    config->cnt += config->rate;
	    if (config->cnt >= 1) {
	        config->cnt -= 1;
		config->y = (long int)(buffer[i]*m)/(float)m;
	    }
	    buffer[i] = config->y;
	}
}

void decimateconfig_destroy(void *p) {	
	if(p != NULL) free((DecimateConfig *)p);
}

DelayConfig *delayconfig_create(float time, float fdb) {
	// allocate memory and set feedback parameter
	DelayConfig *p = (DelayConfig *)malloc(sizeof(DelayConfig));
	p->beatmatch = false;
	p->rp = 0;
	delayconfig_set(p, time, fdb);
	return p;
}

void delayconfig_set(void *p, float time, float fdb) {
	DelayConfig *config = (DelayConfig *)p;
	config->time = time > 0.05f ? (time < 1.99f ? time : 1.99f) : 0.05f;
	config->size = config->time*44100;
	config->delay = calloc(sizeof(float), config->size);
	config->fdb = fdb > 0.f ? (fdb < 1.f ? fdb : 0.9999999f) : 0.f;
}

void delayconfig_setTime(DelayConfig *config, float time) {
	config->time = time > 0.05f ? (time < 1.99f ? time : 1.99f) : 0.05f;
	int newSize = config->time*44100;
	float *newBuffer = calloc(newSize, sizeof(float));
	if (config->size > 0 && config->size <= newSize) {
		long prefix = newSize - config->size;
		delay_process(config, &(newBuffer[prefix]), config->size);
	} else if (config->size > 0 && config->size > newSize) {
		long cut = config->size - newSize;
		float *tempZeros = calloc(cut, sizeof(float));
		delay_process(config, tempZeros, cut);
		free(tempZeros);
		delay_process(config, newBuffer, newSize);
	}
	free(config->delay);
	config->rp = 0; 			
	config->size = newSize;
	config->delay = newBuffer;	
}

void delayconfig_setFeedback(DelayConfig *config, float fdb) {
	config->fdb = fdb > 0.f ? (fdb < 1.f ? fdb : 0.9999999f) : 0.f;
}

void delay_process(void *p, float buffer[], int size) {
	DelayConfig *config = (DelayConfig *)p;
	// process the delay, replacing the buffer
	float out, *delay = config->delay, fdb = config->fdb;
	int i, *rp = &(config->rp);
	for(i = 0; i < size; i++){
		out = delay[*rp];
		config->delay[(*rp)++] = buffer[i] + out*fdb;
		if(*rp == config->size) *rp = 0;
		buffer[i] = out;
	}
}

void delayconfig_destroy(void *p){
	// free memory
	if(p != NULL) free((DelayConfig *)p);
}

FilterConfig *filterconfig_create(float cutoff, float q) {
    FilterConfig *filterConfig = malloc(sizeof(FilterConfig));
    filterConfig->cutoff = cutoff;
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

void filterconfig_set(void *p, float cutoff, float q) {
	FilterConfig *config = (FilterConfig *)p;
    if (cutoff < config->min_cutoff)
        cutoff = config->min_cutoff;
    else if(cutoff > config->max_cutoff)
        cutoff = config->max_cutoff;

    config->cutoff = cutoff;
    
	if(q < 0.f)
   	    q = 0.f;
	else if(q > 1.f)
	    q = 1.f;

	config->q = q;
	
	float wp = config->t2 * tanf(config->t3 * cutoff);
	float bd, bd_tmp, b1, b2;

	q *= BUDDA_Q_SCALE;
	q += 1.f;

	b1 = (0.765367f / (q*wp));
	b2 = 1.f / (wp * wp);

	bd_tmp = config->t0 * b2 + 1.f;

	bd = 1.f / (bd_tmp + config->t2 * b1);

	config->gain = bd;

	config->coef2 = (2.f - config->t1 * b2);

	config->coef0 = config->coef2 * bd;
	config->coef1 = (bd_tmp - config->t2 * b1) * bd;

	b1 = (1.847759f / (q*wp));

	bd = 1.f / (bd_tmp + config->t2 * b1);

	config->gain *= bd;
	config->coef2 *= bd;
	config->coef3 = (bd_tmp - config->t2 * b1) * bd;
}

void filter_process(void *p, float buffer[], int size) {
	FilterConfig *config = (FilterConfig *)p;
	int i;
	for (i = 0; i < size; i++) {
		if (buffer[i] == 0) continue;
	    float output = buffer[i] * config->gain;
	    float new_hist;

	    output -= config->history1 * config->coef0;
	    new_hist = output - config->history2 * config->coef1;

	    output = new_hist + config->history1 * 2.f + config->history2;

	    config->history2 = config->history1;
	    config->history1 = new_hist;

	    output -= config->history3 * config->coef2;
	    new_hist = output - config->history4 * config->coef3;

	    output = new_hist + config->history3 * 2.f + config->history4;

	    config->history4 = config->history3;
	    config->history3 = new_hist;

	    buffer[i] = output;
	}
}

void filterconfig_destroy(void *p) {
	if(p != NULL) free((FilterConfig *)p);
}

VolumePanConfig *volumepanconfig_create(float volume, float pan) {
    VolumePanConfig *p = (VolumePanConfig *)malloc(sizeof(VolumePanConfig));
	p->volume = volume;
	p->pan = pan;
	return p;
}

void volumepanconfig_set(void *p, float volume, float pan) {
	VolumePanConfig *config = (VolumePanConfig *)p;
	config->volume = volume;
	config->pan = pan;
}

void volumepan_process(void *p, float buffer[], int size) {
	VolumePanConfig *config = (VolumePanConfig *)p;
	float leftVolume = (1 - config->pan)*config->volume;
	float rightVolume = config->pan*config->volume;

	int i;
	for (i = 0; i < size; i+=2) {
		if (buffer[i] == 0) continue;
		buffer[i] = buffer[i]*leftVolume; // left channel
		buffer[i+1] = buffer[i + 1]*rightVolume; // right channel
	}  
}

void volumepanconfig_destroy(void *p) {
        if(p != NULL) free((VolumePanConfig *)p);
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
