// Adapted from https://github.com/pd-l2ork/pd/blob/master/externals/freeverb~/freeverb~.c

#ifndef REVERB_H
#define REVERB_H

#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include <errno.h>

#define numcombs        8
#define numallpasses    4
#define fixedgain        0.015f
#define scaledamp        0.4f
#define scaleroom        0.28f
#define offsetroom        0.7f
#define initialroom        0.5f
#define initialdamp        0.5f
#define initialwet        1.0f
#define initialdry        0.0f
#define initialwidth    1.0f
#define stereospread    23

typedef struct _freeverb {
    /* freeverb stuff */
    float x_gain;
    float x_roomsize, x_roomsize1;
    float x_damp, x_damp1;
    float x_wet, x_wet1, x_wet2;
    float x_dry;
    float x_width;

    float x_allpassfeedback;            /* feedback of allpass filters */
    float x_combfeedback;                /* feedback of comb filters */
    float x_combdamp1;
    float x_combdamp2;
    float x_filterstoreL[numcombs];    /* stores last sample value */
    float x_filterstoreR[numcombs];

    /* buffers for the combs */
    float *x_bufcombL[numcombs];
    float *x_bufcombR[numcombs];
    int x_combidxL[numcombs];
    int x_combidxR[numcombs];

    /* buffers for the allpasses */
    float *x_bufallpassL[numallpasses];
    float *x_bufallpassR[numallpasses];
    int x_allpassidxL[numallpasses];
    int x_allpassidxR[numallpasses];

    /* we'll make local copies adjusted to fit our sample rate */
    int x_combtuningL[numcombs];
    int x_combtuningR[numcombs];

    int x_allpasstuningL[numallpasses];
    int x_allpasstuningR[numallpasses];
} t_freeverb;

void reverbconfig_setParam(void *p, float paramNum, float param);

t_freeverb *reverbconfig_create();

void reverbconfig_destroy(void *config);

void reverbconfig_setParam(void *p, float paramNum, float param);

static inline float comb_processL(t_freeverb *x, int filteridx, float input) {
    float output;
    int bufidx = x->x_combidxL[filteridx];

    output = x->x_bufcombL[filteridx][bufidx];

    x->x_filterstoreL[filteridx] =
            (output * x->x_combdamp2) + (x->x_filterstoreL[filteridx] * x->x_combdamp1);

    x->x_bufcombL[filteridx][bufidx] = input + (x->x_filterstoreL[filteridx] * x->x_combfeedback);

    if (++x->x_combidxL[filteridx] >= x->x_combtuningL[filteridx]) x->x_combidxL[filteridx] = 0;

    return output;
}

static inline float comb_processR(t_freeverb *x, int filteridx, float input) {
    float output;
    int bufidx = x->x_combidxR[filteridx];

    output = x->x_bufcombR[filteridx][bufidx];

    x->x_filterstoreR[filteridx] =
            (output * x->x_combdamp2) + (x->x_filterstoreR[filteridx] * x->x_combdamp1);

    x->x_bufcombR[filteridx][bufidx] = input + (x->x_filterstoreR[filteridx] * x->x_combfeedback);

    if (++x->x_combidxR[filteridx] >= x->x_combtuningR[filteridx]) x->x_combidxR[filteridx] = 0;

    return output;
}

static inline float allpass_processL(t_freeverb *x, int filteridx, float input) {
    float output;
    float bufout;
    int bufidx = x->x_allpassidxL[filteridx];

    bufout = x->x_bufallpassL[filteridx][bufidx];

    output = -input + bufout;
    x->x_bufallpassL[filteridx][bufidx] = input + (bufout * x->x_allpassfeedback);

    if (++x->x_allpassidxL[filteridx] >= x->x_allpasstuningL[filteridx])
        x->x_allpassidxL[filteridx] = 0;

    return output;
}

static inline float allpass_processR(t_freeverb *x, int filteridx, float input) {
    float output;
    float bufout;
    int bufidx = x->x_allpassidxR[filteridx];

    bufout = x->x_bufallpassR[filteridx][bufidx];

    output = -input + bufout;
    x->x_bufallpassR[filteridx][bufidx] = input + (bufout * x->x_allpassfeedback);

    if (++x->x_allpassidxR[filteridx] >= x->x_allpasstuningR[filteridx])
        x->x_allpassidxR[filteridx] = 0;

    return output;
}

static inline void reverb_process(t_freeverb *config, float **buffers, int size) {
    int samp;
    int i;
    float outL, outR, inL, inR, input;

    for (samp = 0; samp < size; samp++) {
        outL = outR = 0.f;
        inL = buffers[0][samp];
        inR = buffers[1][samp];
        input = (inL + inR) * config->x_gain;

        // Accumulate comb filters in parallel
        for (i = 0; i < numcombs; i++) {
            outL += comb_processL(config, i, input);
            outR += comb_processR(config, i, input);
        }

        // Feed through allpasses in series
        for (i = 0; i < numallpasses; i++) {
            outL = allpass_processL(config, i, outL);
            outR = allpass_processR(config, i, outR);
        }

        buffers[0][samp] =
                outL * config->x_wet1 + outR * config->x_wet2 + inL * (1 - config->x_wet);
        buffers[1][samp] =
                outR * config->x_wet1 + outL * config->x_wet2 + inR * (1 - config->x_wet);
    }
}

#endif // REVERB_H
