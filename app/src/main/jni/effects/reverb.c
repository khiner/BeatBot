// Adapted from https://github.com/pd-l2ork/pd/blaob/master/externals/freeverb~/freeverb~.c

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

static char *version = "freeverb~ v1.2";

void *freeverb_class;

/* we need prototypes for Mac for everything */
static void comb_setdamp(t_freeverb *x, float val);

static void comb_setfeedback(t_freeverb *x, float val);

static inline float comb_processL(t_freeverb *x, int filteridx, float input);

static inline float comb_processR(t_freeverb *x, int filteridx, float input);

static void allpass_setfeedback(t_freeverb *x, float val);

static inline float allpass_processL(t_freeverb *x, int filteridx, float input);

static inline float allpass_processR(t_freeverb *x, int filteridx, float input);

int *freeverb_perform(int *w);

static void freeverb_update(t_freeverb *x);

static void freeverb_setroomsize(t_freeverb *x, float value);

static float freeverb_getroomsize(t_freeverb *x);

static void freeverb_setdamp(t_freeverb *x, float value);

static float freeverb_getdamp(t_freeverb *x);

static void freeverb_setwet(t_freeverb *x, float value);

static float freeverb_getwet(t_freeverb *x);

static void freeverb_setdry(t_freeverb *x, float value);

static float freeverb_getdry(t_freeverb *x);

static void freeverb_setwidth(t_freeverb *x, float value);

static float freeverb_getwidth(t_freeverb *x);

static float freeverb_getdb(float f);

/* -------------------- comb filter stuff ----------------------- */
static void comb_setdamp(t_freeverb *x, float val) {
    x->x_combdamp1 = val;
    x->x_combdamp2 = 1 - val;
}

static void comb_setfeedback(t_freeverb *x, float val) {
    x->x_combfeedback = val;
}

/* -------------------- allpass filter stuff ----------------------- */
static void allpass_setfeedback(t_freeverb *x, float val) {
    x->x_allpassfeedback = val;
}

// ----------- general parameter & calculation stuff -----------

// recalculate internal values after parameter change
static void freeverb_update(t_freeverb *x) {
    x->x_wet1 = x->x_wet * (x->x_width / 2 + 0.5f);
    x->x_wet2 = x->x_wet * ((1 - x->x_width) / 2);

    x->x_roomsize1 = x->x_roomsize;
    x->x_damp1 = x->x_damp;
    x->x_gain = (float) fixedgain;

    comb_setfeedback(x, x->x_roomsize1);
    comb_setdamp(x, x->x_damp1);
}

// the following functions set / get the parameters
static void freeverb_setroomsize(t_freeverb *x, float value) {
    x->x_roomsize = (value * scaleroom) + offsetroom;
    freeverb_update(x);
}

static float freeverb_getroomsize(t_freeverb *x) {
    return (x->x_roomsize - offsetroom) / scaleroom;
}

static void freeverb_setdamp(t_freeverb *x, float value) {
    x->x_damp = value * scaledamp;
    freeverb_update(x);
}

static float freeverb_getdamp(t_freeverb *x) {
    return x->x_damp / scaledamp;
}

static void freeverb_setwet(t_freeverb *x, float value) {
    x->x_wet = value * scalewet;
    freeverb_update(x);
}

static float freeverb_getwet(t_freeverb *x) {
    return (x->x_wet / scalewet);
}

static void freeverb_setdry(t_freeverb *x, float value) {
    x->x_dry = value * scaledry;
}

static float freeverb_getdry(t_freeverb *x) {
    return (x->x_dry / scaledry);
}

static void freeverb_setwidth(t_freeverb *x, float value) {
    x->x_width = value;
    freeverb_update(x);
}

static float freeverb_getwidth(t_freeverb *x) {
    return x->x_width;
}

// clean up
void reverbconfig_destroy(void *p) {
    t_freeverb *x = (t_freeverb *) p;
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

t_freeverb *reverbconfig_create() {
    int i;

    t_freeverb *x = (t_freeverb *) malloc(sizeof(t_freeverb));

    // recalculate the reverb parameters in case we don't run at 44.1kHz
    for (i = 0; i < numcombs; i++) {
        x->x_combtuningL[i] = (int) (combtuningL[i] * SAMPLE_RATE / 44100);
        x->x_combtuningR[i] = (int) (combtuningR[i] * SAMPLE_RATE / 44100);
        x->x_filterstoreL[i] = 0;
        x->x_filterstoreR[i] = 0;
    }

    for (i = 0; i < numallpasses; i++) {
        x->x_allpasstuningL[i] = (int) (allpasstuningL[i] * SAMPLE_RATE / 44100);
        x->x_allpasstuningR[i] = (int) (allpasstuningR[i] * SAMPLE_RATE / 44100);
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
    freeverb_setdry(x, initialdry);
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
}
