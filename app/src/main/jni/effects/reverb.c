// Adapted from https://github.com/pd-l2ork/pd/blob/master/externals/freeverb~/freeverb~.c

#define float float

#include <string.h>
#include <all.h>

/* these values assume 44.1KHz sample rate
   they will probably be OK for 48KHz sample rate
   but would need scaling for 96KHz (or other) sample rates.
   the values were obtained by listening tests.                */
static const int combtuningL[numcombs]
        = {1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617};
static const int combtuningR[numcombs]
        = {1116 + stereospread, 1188 + stereospread, 1277 + stereospread, 1356 + stereospread,
           1422 + stereospread, 1491 + stereospread, 1557 + stereospread, 1617 + stereospread};

static const int allpasstuningL[numallpasses]
        = {556, 441, 341, 225};
static const int allpasstuningR[numallpasses]
        = {556 + stereospread, 441 + stereospread, 341 + stereospread, 225 + stereospread};

static void comb_setdamp(ReverbConfig *x, float val) {
    x->x_combdamp1 = val;
    x->x_combdamp2 = 1 - val;
}

static void comb_setfeedback(ReverbConfig *x, float val) {
    x->x_combfeedback = val;
}

static void allpass_setfeedback(ReverbConfig *x, float val) {
    x->x_allpassfeedback = val;
}

// ----------- general parameter & calculation stuff -----------

// recalculate internal values after parameter change
static void freeverb_update(ReverbConfig *x) {
    x->x_wet1 = x->x_wet * (x->x_width / 2 + 0.5f);
    x->x_wet2 = x->x_wet * ((1 - x->x_width) / 2);

    x->x_roomsize1 = x->x_roomsize;
    x->x_damp1 = x->x_damp;
    x->x_gain = (float) fixedgain;

    comb_setfeedback(x, x->x_roomsize1);
    comb_setdamp(x, x->x_damp1);
}

static void freeverb_setroomsize(ReverbConfig *x, float value) {
    x->x_roomsize = (value * scaleroom) + offsetroom;
    freeverb_update(x);
}

static void freeverb_setdamp(ReverbConfig *x, float value) {
    x->x_damp = value * scaledamp;
    freeverb_update(x);
}

static void freeverb_setwet(ReverbConfig *x, float value) {
    x->x_wet = value;
    freeverb_update(x);
}

static void freeverb_setwidth(ReverbConfig *x, float value) {
    x->x_width = value;
    freeverb_update(x);
}

void reverbconfig_destroy(void *p) {
    ReverbConfig *x = (ReverbConfig *) p;
    int i;
    // free memory used by delay lines
    for (i = 0; i < numcombs; i++) {
        free(x->x_bufcombL[i]);
        free(x->x_bufcombR[i]);
    }

    for (i = 0; i < numallpasses; i++) {
        free(x->x_bufallpassL[i]);
        free(x->x_bufallpassR[i]);
    }
    free(x);
}

ReverbConfig *reverbconfig_create() {
    int i;

    ReverbConfig *x = (ReverbConfig *) malloc(sizeof(ReverbConfig));

    for (i = 0; i < numcombs; i++) {
        x->x_combtuningL[i] = (int) (combtuningL[i]);
        x->x_combtuningR[i] = (int) (combtuningR[i]);
        x->x_filterstoreL[i] = 0;
        x->x_filterstoreR[i] = 0;
    }

    for (i = 0; i < numallpasses; i++) {
        x->x_allpasstuningL[i] = (int) (allpasstuningL[i]);
        x->x_allpasstuningR[i] = (int) (allpasstuningR[i]);
    }

    // get memory for delay lines
    for (i = 0; i < numcombs; i++) {
        x->x_bufcombL[i] = (float *) malloc(x->x_combtuningL[i] * sizeof(float));
        x->x_bufcombR[i] = (float *) malloc(x->x_combtuningR[i] * sizeof(float));
        x->x_combidxL[i] = 0;
        x->x_combidxR[i] = 0;
    }
    for (i = 0; i < numallpasses; i++) {
        x->x_bufallpassL[i] = (float *) malloc(x->x_allpasstuningL[i] * sizeof(float));
        x->x_bufallpassR[i] = (float *) malloc(x->x_allpasstuningR[i] * sizeof(float));
        x->x_allpassidxL[i] = 0;
        x->x_allpassidxR[i] = 0;
    }

    // set default values
    x->x_allpassfeedback = 0.5;
    freeverb_setwet(x, initialwet);
    freeverb_setroomsize(x, initialroom);
    freeverb_setdamp(x, initialdamp);
    freeverb_setwidth(x, initialwidth);
    freeverb_update(x);

    // buffers will be full of rubbish - so we must fill delay lines with silence
    for (i = 0; i < numcombs; i++) {
        memset(x->x_bufcombL[i], 0x0, x->x_combtuningL[i] * sizeof(float));
        memset(x->x_bufcombR[i], 0x0, x->x_combtuningR[i] * sizeof(float));
    }
    for (i = 0; i < numallpasses; i++) {
        memset(x->x_bufallpassL[i], 0x0, x->x_allpasstuningL[i] * sizeof(float));
        memset(x->x_bufallpassR[i], 0x0, x->x_allpasstuningR[i] * sizeof(float));
    }

    return x;
}

void reverbconfig_setParam(void *p, float paramNumFloat, float value) {
    ReverbConfig *config = (ReverbConfig *) p;
    switch ((int) paramNumFloat) {
        case 0: // Room Size
            freeverb_setroomsize(config, value);
        case 1: // Damping
            freeverb_setdamp(config, value);
            break;
        case 2: // Width
            freeverb_setwidth(config, value);
            break;
        case 3: // Wet/Dry
            freeverb_setwet(config, value);
            break;
    }
}
