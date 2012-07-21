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
#define MIN_CHORUS_DELAY 0.006f*SAMPLE_RATE
#define MAX_CHORUS_DELAY 0.025f*SAMPLE_RATE
#define MIN_FLANGER_DELAY 0.0005f*SAMPLE_RATE
#define MAX_FLANGER_DELAY 0.007f*SAMPLE_RATE
#define MAX_PITCH_DELAY_SAMPS 5024

#define STATIC_VOL_PAN_ID 0
#define STATIC_PITCH_ID 1
#define DECIMATE_ID 2
#define LP_FILTER_ID 3
#define HP_FILTER_ID 4
#define DYNAMIC_VOL_PAN_ID 5
#define DYNAMIC_PITCH_ID 6
#define DELAY_ID 7
#define CHORUS_ID 8
#define FLANGER_ID 9
#define REVERB_ID 10
#define ADSR_ID 11

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
	float  delayTime[2];       // delay time in seconds: 0-1; one for each channel
	float  feedback;           // feedback amount: 0-1; used for both channels
	float  wet;                // wet/dry; used for both channels
	float  alpha[2];
	float  omAlpha[2];
	float  delaySamples[2];    // (fractional) delay time in samples: 0 - SAMPLE_RATE; one for each channel
	float  out;
	unsigned int    maxSamples; 	       // maximum size of delay buffer (set to SAMPLE_RATE by default)
	unsigned int    numBeats[2];		   // number of beats to delay for beatmatch; one for each channel
	unsigned int    rp[2], wp[2];       // read & write pointers
	bool   beatmatch; 		   // sync to the beat?
	pthread_mutex_t mutex;     // mutex since sets happen on a different thread than processing
} DelayConfigI;

typedef struct ChorusConfig_t {
	DelayConfigI *delayConfig;     // delay line
	SineWave     *mod[2];          // table-based sine wave generator for modulation
	float        baseTime;         // center time for delay modulation
	float 		 modAmt;           // modulation depth
} ChorusConfig;

typedef struct FlangerConfig_t {
	DelayConfigI *delayConfig;     // delay line
	SineWave     *mod[2];          // table-based sine wave generator for modulation
	float        baseTime;         // center time for delay modulation
	float 		 modAmt;           // modulation depth
} FlangerConfig;

typedef struct FilterConfig_t {
	float a1, a2, a3, b1, b2;
	float f, c, r;
	float in1[2], in2[2]; // one for each channel
	float out1[2], out2[2]; // one for each channel
} FilterConfig;

typedef struct PitchConfig_t {
	DelayConfigI *delayLine[2];
	float delaySamples[2];
	float env[2];
	float rate;
	float wet;
	unsigned long delayLength;
	unsigned long halfLength;
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

static inline void underguard(float *x) {
  	union {
	    u_int32_t i;
	    float f;
  	} ix;
  	ix.f = *x;
  	if((ix.i & 0x7f800000)==0) *x=0.0f;
}

static inline float allpass_process(allpass_state *a, float  input){
  	float val    = *a->bufptr;
  	float output = val - input;
  
  	*a->bufptr   = val * .5f + input;
  	underguard(a->bufptr);
  
  	if(a->bufptr<=a->buffer) a->bufptr += a->size;
  	--a->bufptr;

  	return output;
}

static inline float comb_process(comb_state *c, float  feedback, float  hfDamp, float  input){
  	float val      = *c->extptr;
  	c->filterstore = val + (c->filterstore - val)*hfDamp;
  	underguard(&c->filterstore);

  	*c->injptr     = input + c->filterstore * feedback;
  	underguard(c->injptr);

  	if(c->injptr<=c->buffer) c->injptr += c->size;
  	--c->injptr;
  	if(c->extptr<=c->buffer) c->extptr += c->size;
  	--c->extptr;
  	
  return val;
}

AdsrConfig *adsrconfig_create(int totalSamples);

void updateAdsr(AdsrConfig *config, int totalSamples);
void resetAdsr(AdsrConfig *config);

static inline void adsr_process(void *p, float **buffers, int size) {
	AdsrConfig *config = (AdsrConfig *)p;
	if (!config->active) return;
	int i;
	for (i = 0; i < size; i++) {
		if (++config->currSampleNum < config->gateSample) {
			if (config->rising) { // attack phase
				config->currLevel += config->attackCoeff*(config->peak/0.63f - config->currLevel);
				if (config->currLevel > 1.0f) {
					config->currLevel = 1.0f;
					config->rising = false;
				}
			} else { // decal/sustain
				config->currLevel += config->decayCoeff * (config->sustain - config->currLevel)/0.63f;
			}
		} else if (config->currSampleNum < config->adsrPoints[4].sampleNum) { // past gate sample, go to release phase
			config->currLevel += config->releaseCoeff * (config->end - config->currLevel)/0.63f;
			if (config->currLevel < config->end) {
				config->currLevel = config->end;
			}
		} else if (config->currSampleNum < config->totalSamples) {
			config->currLevel = 0;
		} else {
			resetAdsr(config);
		}
		buffers[0][i] *= config->currLevel;
		buffers[1][i] *= config->currLevel;
    }
}

void adsrconfig_destroy(void *p);

DecimateConfig *decimateconfig_create(float bits, float rate);
void decimateconfig_set(void *p, float bits, float rate);

static inline void decimate_process(void *p, float **buffers, int size) {
	DecimateConfig *config = (DecimateConfig *)p;
    int m = 1 << (config->bits - 1);
	int i;
	for (i = 0; i < size; i++) {
	    config->cnt += config->rate;
	    if (config->cnt >= 1) {
	        config->cnt -= 1;
		config->y = (long int)(buffers[0][i]*m)/(float)m;
	    }
	    buffers[0][i] = buffers[1][i] = config->y;
	}
}

void decimateconfig_destroy(void *p);

DelayConfigI *delayconfigi_create(float delay, float feedback, int maxSamples);
void delayconfigi_set(void *config, float delay, float feedback);
void delayconfigi_setFeedback(DelayConfigI *config, float feedback);
void delayconfigi_setNumBeats(DelayConfigI *config, unsigned int numBeatsL, unsigned int numBeatsR);
void delayconfigi_syncToBPM(DelayConfigI *config);

static inline void delayconfigi_setDelaySamples(DelayConfigI *config, float numSamplesL, float numSamplesR) {
	unsigned int *rp, *wp, channel;
	float rpf;
	pthread_mutex_lock(&config->mutex);
	config->delaySamples[0] = numSamplesL;
	config->delaySamples[1] = numSamplesR;
	for (channel = 0; channel < 2; channel++) {
		rp = &(config->rp[channel]);
		wp = &(config->wp[channel]);
		rpf = *wp - config->delaySamples[channel]; // read chases write
		while (rpf < 0)
			rpf += config->maxSamples;
		*rp = (unsigned int)rpf;
		if (*rp >= config->maxSamples) (*rp) = 0;
		config->alpha[channel] = rpf - (*rp);
		config->omAlpha[channel] = 1.0f - config->alpha[channel];
	}
	pthread_mutex_unlock(&config->mutex);
}

static inline void delayconfigi_setDelayTime(DelayConfigI *config, float lDelay, float rDelay) {
	pthread_mutex_lock(&config->mutex);
	config->delayTime[0] = lDelay > 0.0001 ? (lDelay <= 1 ? lDelay : 1) : 0.0001;
	config->delayTime[1] = rDelay > 0.0001 ? (rDelay <= 1 ? rDelay : 1) : 0.0001;
	pthread_mutex_unlock(&config->mutex);
	delayconfigi_setDelaySamples(config, config->delayTime[0]*SAMPLE_RATE, config->delayTime[1]*SAMPLE_RATE);
}

static inline float delayi_tick(DelayConfigI *config, float in, int channel) {
	if (config->rp[channel] >= config->maxSamples) config->rp[channel] = 0;
	if (config->wp[channel] >= config->maxSamples) config->wp[channel] = 0;
	
	float interpolated = config->delayBuffer[channel][config->rp[channel]++] * config->omAlpha[channel];
	interpolated += config->delayBuffer[channel][config->rp[channel] % config->maxSamples] * config->alpha[channel];
	config->out = interpolated*config->wet + in*(1 - config->wet);
	if (config->out > 1) config->out = 1;
	config->delayBuffer[channel][config->wp[channel]++] = in + config->out*config->feedback;
	return config->out;	
}

static inline void delayi_process(void *p, float **buffers, int size) {
	DelayConfigI *config = (DelayConfigI *)p;
	int channel, samp;
	for (samp = 0; samp < size; samp++) {
		pthread_mutex_lock(&config->mutex);
		for (channel = 0; channel < 2; channel++) {
			buffers[channel][samp] = delayi_tick(config, buffers[channel][samp], channel);
		}
		pthread_mutex_unlock(&config->mutex);
	}
}

void delayconfigi_destroy(void *p);

FilterConfig *filterconfig_create(float cutoff, float r);
void filterconfig_setLp(void *config, float cutoff, float r);
void filterconfig_setHp(void *config, float cutoff, float r);

static inline void filter_process(void *p, float **buffers, int size) {
	FilterConfig *config = (FilterConfig *)p;
	int channel, samp;
	for (channel = 0; channel < 2; channel++) {
		for(samp = 0; samp < size; samp++) {
			float out = config->a1 * buffers[channel][samp] +
				        config->a2 * config->in1[channel] +
					    config->a3 * config->in2[channel] -
					    config->b1 * config->out1[channel] -
					    config->b2 * config->out2[channel];
			config->in2[channel] = config->in1[channel];
			config->in1[channel] = buffers[channel][samp];
			config->out2[channel] = config->out1[channel];
			config->out1[channel] = out;
			buffers[channel][samp] = out;
		}	
	}
}

void filterconfig_destroy(void *config);

ChorusConfig *chorusconfig_create(float modFreq, float modAmt);
void chorusconfig_set(void *p, float modFreq, float modAmt);

void chorusconfig_setBaseTime(ChorusConfig *config, float baseTime);
void chorusconfig_setFeedback(ChorusConfig *config, float feedback);
void chorusconfig_setModFreq(ChorusConfig *config, float modFreq);
void chorusconfig_setModAmt(ChorusConfig *config, float modAmt);

static inline void chorus_process(void *p, float **buffers, int size) {
	ChorusConfig *config = (ChorusConfig *)p;
	int channel, samp;
	for (samp = 0; samp < size; samp++) {
		float dTimeL = config->baseTime * 0.707 * (1.0f + config->modAmt * sinewave_tick(config->mod[0]));
		float dTimeR = config->baseTime * 0.5 * (1.0f - config->modAmt * sinewave_tick(config->mod[1]));
		delayconfigi_setDelaySamples(config->delayConfig, dTimeL, dTimeR);
		pthread_mutex_lock(&config->delayConfig->mutex);
		for (channel = 0; channel < 2; channel++) {
			buffers[channel][samp] = delayi_tick(config->delayConfig, buffers[channel][samp], channel);
		}
		pthread_mutex_unlock(&config->delayConfig->mutex);
	}
}

void chorusconfig_destroy(void *p);

FlangerConfig *flangerconfig_create(float delayTime, float feedback);
void flangerconfig_set(void *p, float delayTime, float feedback);
void flangerconfig_setBaseTime(FlangerConfig *config, float baseTime);
void flangerconfig_setFeedback(FlangerConfig *config, float feedback);
void flangerconfig_setModFreq(FlangerConfig *config, float modFreq);
void flangerconfig_setModAmt(FlangerConfig *config, float modAmt);
void flangerconfig_setPhaseShift(FlangerConfig *config, float phaseShift);

static inline void flanger_process(void *p, float **buffers, int size) {
	FlangerConfig *config = (FlangerConfig *)p;
	int channel, samp;
	for (samp = 0; samp < size; samp++) {
		float dTimeL = config->baseTime * (1.0f + config->modAmt * sinewave_tick(config->mod[0]));
		float dTimeR = config->baseTime * (1.0f + config->modAmt * sinewave_tick(config->mod[1]));
		delayconfigi_setDelaySamples(config->delayConfig, dTimeL, dTimeR);
		pthread_mutex_lock(&config->delayConfig->mutex);
		for (channel = 0; channel < 2; channel++) {
			buffers[channel][samp] = delayi_tick(config->delayConfig, buffers[channel][samp], channel);
		}
		pthread_mutex_unlock(&config->delayConfig->mutex);
	}	
}

void flangerconfig_destroy(void *config);

PitchConfig *pitchconfig_create();
void pitchconfig_setShift(PitchConfig *config, float shift);

static inline float pitch_tick(PitchConfig *config, float in, int channel) {
	// Calculate the two delay length values, keeping them within the
	// range 12 to maxDelay-12.
	config->delaySamples[0] += config->rate;
	while (config->delaySamples[0] > MAX_PITCH_DELAY_SAMPS - 12)
		config->delaySamples[0] -= config->delayLength;
	while ( config->delaySamples[0] < 12 )
		config->delaySamples[0] += config->delayLength;

	config->delaySamples[1] = config->delaySamples[0] + config->halfLength;
	while (config->delaySamples[1] > MAX_PITCH_DELAY_SAMPS - 12)
		config->delaySamples[1] -= config->delayLength;
	while (config->delaySamples[1] < 12)
		config->delaySamples[1] += config->delayLength;

	// Set the new delay line lengths.
	delayconfigi_setDelaySamples(config->delayLine[0], config->delaySamples[0], config->delaySamples[0]);
	delayconfigi_setDelaySamples(config->delayLine[1], config->delaySamples[1], config->delaySamples[1]);

	// Calculate a triangular envelope.
	config->env[1] = fabs((config->delaySamples[0] - config->halfLength + 12) *
						 (1.0 / (config->halfLength + 12)));
	config->env[0] = 1.0 - config->env[1];

	// Delay input and apply envelope.
	float out = config->env[0] * delayi_tick(config->delayLine[0], in, channel);
	out += config->env[1] * delayi_tick(config->delayLine[1], in, channel);

	// Compute effect mix and output.
	out *= config->wet;
	out += (1.0 - config->wet) * in;

	return out;
}

static inline void pitch_process(void *p, float **buffers, int size) {
	PitchConfig *config = (PitchConfig *)p;
	int channel, samp;
	for (channel = 0; channel < 2; channel++) {
		for (samp = 0; samp < size; samp++) {
			buffers[channel][samp] = pitch_tick(config, buffers[channel][samp], channel);
		}
	}
}

void pitchconfig_destroy(void *config);

ReverbConfig *reverbconfig_create(float feedback, float hfDamp);
void reverbconfig_set(void *config, float feedback, float hfDamp);

static inline void reverb_process(void *p, float **buffers, int size) {	
	ReverbConfig *config = (ReverbConfig *)p;
  	float out, val=0;
  	int i, j;

  	for (i = 0; i < size; i++) {
    	out = 0;
    	val = buffers[0][i];
	    for(j = 0; j < numcombs; j++)
      		out += comb_process(config->state->comb + j, config->feedback, config->hfDamp, val);
	    for(j = 0; j < numallpasses; j++)
    	    out = allpass_process(config->state->allpass + j, out);
	    buffers[0][i] = buffers[1][i] = out;
	}
}

void reverbconfig_destroy(void *config);

VolumePanConfig *volumepanconfig_create(float volume, float pan);
void volumepanconfig_set(void *config, float volume, float pan);

static inline void volumepan_process(void *p, float **buffers, int size) {
	VolumePanConfig *config = (VolumePanConfig *)p;
	float leftVolume = (1 - config->pan)*config->volume;
	float rightVolume = config->pan*config->volume;
	int i;
	for (i = 0; i < size; i++) {
		if (buffers[0][i] == 0) continue;
		buffers[0][i] *= leftVolume; // left channel
	}
	for (i = 0; i < size; i++) {
		if (buffers[1][i] == 0) continue;
		buffers[1][i] *= rightVolume; // right channel	
	}
}

void volumepanconfig_destroy(void *config);

void reverse(float buffer[], int begin, int end);
void normalize(float buffer[], int size);
			      
static const int numEffects = 12;

#endif // EFFECTS_H
