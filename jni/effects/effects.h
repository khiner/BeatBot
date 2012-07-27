#ifndef EFFECTS_H
#define EFFECTS_H

#include <stdlib.h>
#include <math.h>
#include <pthread.h>
#include <android/log.h>

#include "ticker.h"

#define SAMPLE_RATE 44100
#define INV_SAMPLE_RATE 1.0f/44100.0f

#define bool _Bool
#define false 0
#define true 1

#define VOL_PAN_ID 0
#define PITCH_ID 1
#define DECIMATE_ID 2
#define TREMELO_ID 3
#define LP_FILTER_ID 4
#define HP_FILTER_ID 5
#define CHORUS_ID 6
#define DELAY_ID 7
#define FLANGER_ID 8
#define REVERB_ID 9
#define ADSR_ID 10

#define NUM_EFFECTS 11

typedef struct Effect_t {
	void *config;
	void (*set)(void *, float, float);
	void (*process)(void *, float **, int);
	void (*destroy)(void *);
	bool on;
} Effect;

void initEffect(Effect *effect, bool on, void *config,
		void (*set), void (*process), void (*destroy));

void reverse(float buffer[], int begin, int end);
void normalize(float buffer[], int size);

#endif // EFFECTS_H
