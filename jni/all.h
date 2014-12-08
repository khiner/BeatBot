#ifndef ALL_H_
#define ALL_H_

#define bool _Bool
#define false 0
#define true 1

#define SAMPLE_RATE 44100.0f
#define INV_SAMPLE_RATE 1.0f / SAMPLE_RATE

#define CHORUS   0
#define DECIMATE 1
#define DELAY    2
#define FILTER   3
#define FLANGER  4
#define REVERB   5
#define TREMELO  6

#define ONE_SHORT_SIZE        sizeof(short)
#define ONE_FLOAT_SIZE        sizeof(float)
#define TWO_FLOAT_SIZE        2 * ONE_FLOAT_SIZE
#define FOUR_FLOAT_SIZE       4 * ONE_FLOAT_SIZE
#define RESOLUTION            480
#define MAX_CHANNELS          2
#define MAX_EFFECTS_PER_TRACK 3 // also need to change GlobalVars.MAX_EFFECT_PER_TRACK
#define TABLE_SIZE            2048
#define INV_TABLE_SIZE        1.0f / TABLE_SIZE
#define BUFF_SIZE_FRAMES      1024 // each frame has one short for each channel
#define BUFF_SIZE_SHORTS      BUFF_SIZE_FRAMES * 2
#define BUFF_SIZE_BYTES       BUFF_SIZE_SHORTS * ONE_SHORT_SIZE
#define BUFF_SIZE_FLOATS      BUFF_SIZE_SHORTS * ONE_FLOAT_SIZE
#define SHORT_TO_FLOAT        (1.0f / 32768.0f)
#define FLOAT_TO_SHORT        32768
#define MIN_FLANGER_DELAY     0.0006f * SAMPLE_RATE
#define MAX_FLANGER_DELAY     0.007f * SAMPLE_RATE

#include <stdlib.h>
#include <stdio.h>
#include <sys/stat.h>
#include <math.h>
#include <pthread.h>
#include <android/log.h>
#include <android/asset_manager_jni.h>
#include <jni.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

#include "utils.h"
#include "effects/effects.h"
#include "effects/adsr.h"
#include "effects/chorus.h"
#include "effects/decimate.h"
#include "effects/delay.h"
#include "effects/filter.h"
#include "effects/flanger.h"
#include "effects/reverb.h"
#include "effects/tremelo.h"
#include "effects/volpan.h"
#include "generators/generators.h"
#include "generators/sinewave.h"
#include "generators/file_gen.h"
#include "midievent.h"
#include "nativeaudio.h"
#include "ticker.h"
#include "track.h"

#endif /* ALL_H_ */
