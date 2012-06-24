#ifndef EFFECTS_H
#define EFFECTS_H

#include <stdlib.h>
#include <math.h>
#include <stdbool.h>

#define BUDDA_Q_SCALE 6.f

// static effect ids
#define STATIC_VOL_PAN_ID 0
#define DECIMATE_ID 1
#define FILTER_ID 2

// dynamic effect ids
#define DYNAMIC_VOL_PAN_ID 0
#define DELAY_ID 1

typedef struct DecimateConfig_t {
    int bits; // 2-32
    float rate; // 0-1
    float cnt;
    float y;
} DecimateConfig;

typedef struct DelayConfig_t {
	float *delay; // delayline
	int size;     // length  in samples
	int rp;       // read pointer
	float fdb;    // feedback amount: 0-1
	float time;   // time from 0-1
} DelayConfig;

typedef struct FilterConfig_t {
    float t0, t1, t2, t3;
    float coef0, coef1, coef2, coef3;
    float history1, history2, history3, history4;
    float gain;
    float cutoff, q;
    float min_cutoff, max_cutoff;	
} FilterConfig;

typedef struct VolumePanConfig_t {
    float volume;
    float pan;
} VolumePanConfig;

typedef struct Effect_t {
	bool on;
	void *config;
	void (*set)(void *, float, float);
	void (*process)(void *, float *, int);
	void (*destroy)(void *);
} Effect;

void initEffect(Effect *effect, bool on, void *config,
				void (*set), void (*process), void (*destroy));
DecimateConfig *decimateconfig_create(float bits, float rate);
void decimateconfig_set(void *p, float bits, float rate);
void decimate_process(void *p, float buffer[], int size);
void decimateconfig_destroy(void *p);

DelayConfig *delayconfig_create(float delay, float fdb);
void delayconfig_set(void *delayConfig, float time, float fdb);
void delayconfig_setTime(DelayConfig *delayConfig, float time);
void delayconfig_setFeedback(DelayConfig *delayCoinfig, float fdb);
void delay_process(void *p, float buffer[], int size);
void delayconfig_destroy(void *p);

FilterConfig *filterconfig_create(float cutoff, float q);
void filterconfig_set(void *filterConfig, float cutoff, float q);
void filter_process(void *p, float buffer[], int size);
void filterconfig_destroy(void *p);

VolumePanConfig *volumepanconfig_create(float volume, float pan);
void volumepanconfig_set(void *volumePanConfig, float volume, float pan);
void volumepan_process(void *p, float buffer[], int size);
void volumepanconfig_destroy(void *p);

void reverse(float buffer[], int begin, int end);
void normalize(float buffer[], int size);

static const int numStaticEffects = 3;
static const int numDynamicEffects = 2;
#endif // EFFECTS_H
