#ifndef EFFECTS_H
#define EFFECTS_H

#include <stdlib.h>
#include <math.h>
#include <pthread.h>
#include <android/log.h>
#include <jni.h>
#include <android/log.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <android/asset_manager_jni.h>
#include "../generators/generators.h"
#include "../generators/wavfile.h"
#include "ticker.h"

#define bool _Bool
#define false 0
#define true 1

#define SAMPLE_RATE 44100.0f
#define INV_SAMPLE_RATE 1.0f/44100.0f

#define CHORUS   0
#define DECIMATE 1
#define DELAY    2
#define FILTER   3
#define FLANGER  4
#define REVERB   5
#define TREMELO  6

typedef struct Effect_t {
	void *config;
	void (*set)(void *, float, float);
	void (*process)(void *, float **, int);
	void (*destroy)(void *);
	int id;
	bool on;
} Effect;

typedef struct EffectNode_t {
	Effect *effect;
	struct EffectNode_t *next;
} EffectNode;

Effect *initEffect(int id, bool on, void *config,
		void (*set), void (*process), void (*destroy));

void reverse(float buffer[], int begin, int end);
void normalize(float buffer[], int size);

#endif // EFFECTS_H
