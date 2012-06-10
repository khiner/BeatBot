#ifndef EFFECTS_H
#define EFFECTS_H

#include <stdlib.h>
#include <math.h>

#define BUDDA_Q_SCALE 6.f

typedef struct VolumePanConfig_t {
    float volume;
    float pan;
} VolumePanConfig;

typedef struct DelayConfig_t {
	float *delay; // delayline
	int size;     // length  in samples
	int rp;       // read pointer
	float fdb;    // feedback amount
} DelayConfig;

typedef struct FilterConfig_t {
        float t0, t1, t2, t3;
        float coef0, coef1, coef2, coef3;
        float history1, history2, history3, history4;
        float gain;
        float min_cutoff, max_cutoff;	
} FilterConfig;

typedef struct DecimateConfig_t {
        int bits;
        float rate;
        float cnt;
        float y;
} DecimateConfig;

VolumePanConfig *volumepanconfig_create(float volume, float pan);

void volumepanconfig_set(VolumePanConfig *volumePanConfig, float volume, float pan);

void volumepan_process(VolumePanConfig *p, float buffer[], int size);

void *volumepanconfig_destroy(VolumePanConfig *p);

DelayConfig *delayconfig_create(float delay, float fdb);

void delay_process(DelayConfig *p, float buffer[], int size);

void *delayconfig_destroy(DelayConfig *p);

FilterConfig *filterconfig_create(float cutoff, float q);

void filterconfig_set(FilterConfig *filterConfig, float cutoff, float q);

void filter_process(FilterConfig *p, float buffer[], int size);

void *filterconfig_destroy(FilterConfig *p);

DecimateConfig *decimateconfig_create(int bits, float rate);

void decimate_process(DecimateConfig *p, float buffer[], int size);

void decimateconfig_destroy(DecimateConfig *p);

void reverse(float buffer[], int begin, int end);

void normalize(float buffer[], int size);

#endif // EFFECTS_H
