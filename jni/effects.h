#ifndef EFFECTS_H
#define EFFECTS_H

#include <stdlib.h>
#include <math.h>
#include <stdbool.h>
#include <pthread.h>
#include <android/log.h>

#include "generators.h"
#include "ticker.h"

#define SAMPLE_RATE 44100
#define INV_SAMPLE_RATE 1.0f/44100.0f
#define MIN_FLANGER_DELAY 0.002f
#define MAX_FLANGER_DELAY 0.015f

#define STATIC_VOL_PAN_ID 0
#define DECIMATE_ID 1
#define FILTER_ID 2
#define DYNAMIC_VOL_PAN_ID 3
#define DELAY_ID 4
#define FLANGER_ID 5
#define REVERB_ID 6
#define ADSR_ID 7

/******* BEGIN FREEVERB STUFF *********/
typedef struct allpass{
  float *buffer;
  float *bufptr;
  int    size;
} allpass_state;

typedef struct comb{
  float	 filterstore;
  float *buffer;
  float *injptr;
  float *extptr;
  float *extpending;
  int    size;
} comb_state;

#define numcombs 8
#define numallpasses 4
#define scalehfdamp 0.4f
#define scaleliveness 0.4f
#define offsetliveness 0.58f
#define scaleroom 1111
#define stereospread 23
#define fixedgain 0.015f

#define comb0 1116
#define comb1 1188
#define comb2 1277
#define comb3 1356
#define comb4 1422
#define comb5 1491
#define comb6 1557
#define comb7 1617

#define all0 556
#define all1 441
#define all2 341
#define all3 225

#define MAX_PITCH_DELAY 5024;

/* These values assume 44.1KHz sample rate
   they will probably be OK for 48KHz sample rate
   but would need scaling for 96KHz (or other) sample rates.
   The values were obtained by listening tests. */

static const int combL[numcombs]={
  comb0, comb1, comb2, comb3, 
  comb4, comb5, comb6, comb7 
};

static const int combR[numcombs]={
  comb0+stereospread, comb1+stereospread, comb2+stereospread, comb3+stereospread, 
  comb4+stereospread, comb5+stereospread, comb6+stereospread, comb7+stereospread 
};

static const int allL[numallpasses]={
  all0, all1, all2, all3,
};

static const int allR[numallpasses]={
  all0+stereospread, all1+stereospread, all2+stereospread, all3+stereospread,
};

/* enough storage for L or R */
typedef struct ReverbState_t {
  	comb_state comb[numcombs];
  	allpass_state allpass[numallpasses];

  	float bufcomb0[comb0+stereospread];
  	float bufcomb1[comb1+stereospread];
  	float bufcomb2[comb2+stereospread];
  	float bufcomb3[comb3+stereospread];
  	float bufcomb4[comb4+stereospread];
	float bufcomb5[comb5+stereospread];
  	float bufcomb6[comb6+stereospread];
  	float bufcomb7[comb7+stereospread];
  
  	float bufallpass0[all0+stereospread];
  	float bufallpass1[all1+stereospread];
  	float bufallpass2[all2+stereospread];
  	float bufallpass3[all3+stereospread];

  	int energy;
} ReverbState;

typedef struct ReverbConfig_t {
	ReverbState *state;
	float feedback;
	float hfDamp;
	int   inject;
	float wet;
	int   width;
} ReverbConfig;

/******* END FREEVERB STUFF *******/

typedef struct AdsrPoint_t {
	int sampleNum;
	float sampleCents;
} AdsrPoint;

typedef struct AdsrConfig_t {
	AdsrPoint adsrPoints[5];
	float attackCoeff, decayCoeff, releaseCoeff;
	float currLevel;
	float sustain;
	float initial, peak, end;
	int gateSample; // sample to begin release
	int currSampleNum;
	int totalSamples;
	bool active, rising;
} AdsrConfig;

typedef struct DecimateConfig_t {
    int   bits; // 4-32
    float rate; // 0-1
    float cnt;
    float y;
} DecimateConfig;

typedef struct DelayConfigI_t {
	float  **delayBuffer;      // delay buffer for each channel
	float  delayTime;          // delay time in seconds: 0-1
	float  feedback[2];        // feedback amount: 0-1, one for each channel
	float  wet;                // wet/dry
	float  alpha[2];
	float  omAlpha[2];
	float  delaySamples;       // (fractional) delay time in samples: 0 - SAMPLE_RATE
	float  out;	
	int    numBeats;  		   // number of beats to delay for beatmatch
	int    delayBufferSize;    // maximum size of delay buffer (set to SAMPLE_RATE)
	int    rp[2], wp[2];       // read & write pointers
	bool   beatmatch; 		   // sync to the beat?
	pthread_mutex_t mutex;
} DelayConfigI;

typedef struct FlangerConfig_t {
	DelayConfigI *delayConfig;     // delay line
	SineWave     *mod;             // table-based sine wave generator for modulation
	float        baseTime;         // center time for delay modulation
	float 		 modAmt;           // modulation depth
	int          count;            // count for sin modulation of delay length
} FlangerConfig;

typedef struct FilterConfig_t {
	bool hp; // lowpass/highpass flag
	float a1, a2, a3, b1, b2;
	float f, c, r;
	float in1[2], in2[2]; // one for each channel
	float out1[2], out2[2]; // one for each channel
} FilterConfig;

typedef struct PitchConfig_t {
} PitchConfig;

typedef struct VolumePanConfig_t {
    float volume;
    float pan;
} VolumePanConfig;

typedef struct Effect_t {
	bool on;
	bool dynamic;
	void *config;
	void (*set)(void *, float, float);
	void (*process)(void *, float **, int);
	void (*destroy)(void *);
} Effect;

void initEffect(Effect *effect, bool on, bool dynamic, void *config,
				void (*set), void (*process), void (*destroy));
				
AdsrConfig *adsrconfig_create(int totalSamples);
void adsr_process(void *p, float **buffers, int size);
void adsrconfig_destroy(void *p);

void updateAdsr(AdsrConfig *config, int totalSamples);
void resetAdsr(AdsrConfig *config);

DecimateConfig *decimateconfig_create(float bits, float rate);
void decimateconfig_set(void *p, float bits, float rate);
void decimate_process(void *p, float **buffers, int size);
void decimateconfig_destroy(void *p);

DelayConfigI *delayconfigi_create(float delay, float feedback);
void delayconfigi_set(void *config, float delay, float feedback);
void delayconfigi_setDelayTime(DelayConfigI *config, float delay);
void delayconfigi_setNumBeats(DelayConfigI *config, int numBeats);
void delayconfigi_syncToBPM(DelayConfigI *config);
void delayconfigi_setFeedback(DelayConfigI *config, float feedback);
void delayi_process(void *p, float **buffers, int size);
float delayi_tick(DelayConfigI *config, float in, int channel);
void delayconfigi_destroy(void *p);

FilterConfig *filterconfig_create(float cutoff, float r);
void filterconfig_set(void *config, float cutoff, float r);
void filter_process(void *config, float **buffers, int size);
float filter_tick(FilterConfig *config, float in, int channel);
void filterconfig_destroy(void *config);

FlangerConfig *flangerconfig_create(float delayTime, float feedback);
void flangerconfig_set(void *p, float delayTime, float feedback);
void flangerconfig_setBaseTime(FlangerConfig *config, float baseTime);
void flangerconfig_setTime(FlangerConfig *config, float time);
void flangerconfig_setFeedback(FlangerConfig *config, float feedback);
void flangerconfig_setModRate(FlangerConfig *config, float modRate);
void flangerconfig_setModAmt(FlangerConfig *config, float modAmt);
void flanger_process(void *config, float **buffers, int size);
void flangerconfig_destroy(void *config);

PitchConfig *pitchconfig_create(float shift);
void pitchconfig_set(float shift);
void pitch_process(void *config, float **buffers, int size);
void pitch_tick(PitchConfig *config, int channel, int samp);
void pitchconfig_destroy(void *config);

ReverbConfig *reverbconfig_create(float feedback, float hfDamp);
void reverbconfig_set(void *config, float feedback, float hfDamp);
void reverb_process(void *config, float **buffers, int size);
void reverbconfig_destroy(void *config);

VolumePanConfig *volumepanconfig_create(float volume, float pan);
void volumepanconfig_set(void *config, float volume, float pan);
void volumepan_process(void *config, float **buffers, int size);
void volumepanconfig_destroy(void *config);

void reverse(float buffer[], int begin, int end);
void normalize(float buffer[], int size);
			      
static const int numEffects = 8;

#endif // EFFECTS_H
