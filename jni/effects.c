#include "effects.h"

static void inject_set(ReverbState *r,int inject) {
  	int i;
  	for(i=0;i<numcombs;i++){
	    int off=(1000-inject)*r->comb[i].size/scaleroom;
	    r->comb[i].extpending=r->comb[i].injptr-off;
	    if(r->comb[i].extpending<r->comb[i].buffer)r->comb[i].extpending+=r->comb[i].size;
  	}
}

static ReverbState *initReverbState() {
  	int inject = 300;
  	const int *combtuning = combL;
  	const int *alltuning = allL; 
  	int i;
  	ReverbState *r = calloc(1, sizeof(ReverbState));
  
  	r->comb[0].buffer=r->bufcomb0;
  	r->comb[1].buffer=r->bufcomb1;
  	r->comb[2].buffer=r->bufcomb2;
  	r->comb[3].buffer=r->bufcomb3;
  	r->comb[4].buffer=r->bufcomb4;
  	r->comb[5].buffer=r->bufcomb5;
  	r->comb[6].buffer=r->bufcomb6;
  	r->comb[7].buffer=r->bufcomb7;

  	for(i=0;i<numcombs;i++)
	    r->comb[i].size=combtuning[i];
  	for(i=0;i<numcombs;i++)
	    r->comb[i].injptr=r->comb[i].buffer;

  	r->allpass[0].buffer=r->bufallpass0;
  	r->allpass[1].buffer=r->bufallpass1;
  	r->allpass[2].buffer=r->bufallpass2;
  	r->allpass[3].buffer=r->bufallpass3;
  	for(i=0;i<numallpasses;i++)
	    r->allpass[i].size=alltuning[i];
  	for(i=0;i<numallpasses;i++)
	    r->allpass[i].bufptr=r->allpass[i].buffer;
	    
	inject_set(r,inject);
  	for(i=0;i<numcombs;i++)
	    r->comb[i].extptr=r->comb[i].extpending;
	    
  return r;
}

void initEffect(Effect *effect, bool on, bool dynamic, void *config,
				void (*set), void (*process), void (*destroy)) {
	effect->on = on;
	effect->dynamic = dynamic;
	effect->config = config;
	effect->set = set;
	effect->process = process;
	effect->destroy = destroy;				
}

void initAdsrPoints(AdsrConfig *config) {
	config->adsrPoints[0].sampleCents = 0;
	config->adsrPoints[1].sampleCents = 0.25f;
	config->adsrPoints[2].sampleCents = 0.5f;
	config->adsrPoints[3].sampleCents = 0.75f;
	config->adsrPoints[4].sampleCents = 1;
}

AdsrConfig *adsrconfig_create(int totalSamples) {
	AdsrConfig *config = (AdsrConfig *)malloc(sizeof(AdsrConfig));
	initAdsrPoints(config);
	config->active = false;
	config->initial = config->end = 0.0001f;
	config->sustain = 0.6f;
	config->peak = 1.0f;
	resetAdsr(config);
	updateAdsr(config, totalSamples);
	return config;
}

void adsrconfig_destroy(void *p) {
	AdsrConfig *config = (AdsrConfig *)p;
	free(config->adsrPoints);
	free(config);
}

void updateAdsr(AdsrConfig *config, int totalSamples) {
	config->totalSamples = totalSamples;
	int i, length;
	for (i = 0; i < 5; i++) {
		config->adsrPoints[i].sampleNum = (int)(config->adsrPoints[i].sampleCents*totalSamples);
	}
	for (i = 0; i < 4; i++) {
		length = config->adsrPoints[i + 1].sampleNum - config->adsrPoints[i].sampleNum; 
		if (i == 0)
			config->attackCoeff = (1.0f - config->initial)/(length + 1);
		else if (i == 1)
			config->decayCoeff = 1.0f/(length + 1);
		else if (i == 3)
			config->releaseCoeff = (1.0f - config->end)/(length + 1);
	}
	config->gateSample = config->adsrPoints[3].sampleNum;
}

void resetAdsr(AdsrConfig *config) {
	config->currSampleNum  = 0;
	config->currLevel = config->initial;
	config->rising = true;
}

DecimateConfig *decimateconfig_create(float bits, float rate) {
    DecimateConfig *decimateConfig = (DecimateConfig *)malloc(sizeof(DecimateConfig));
	decimateConfig->cnt = 0;
	decimateConfig->y = 0;
	decimateConfig->bits = (int)bits;
	decimateConfig->rate = rate;
	return decimateConfig;
}

void decimateconfig_set(void *p, float bits, float rate) {
	DecimateConfig *config = (DecimateConfig *)p;
	if ((int)bits != 0)
		config->bits = (int)bits;
	config->rate = rate;	
}

void decimateconfig_destroy(void *p) {	
	if(p != NULL) free((DecimateConfig *)p);
}

DelayConfigI *delayconfigi_create(float delay, float feedback, int maxSamples) {
	// allocate memory and set feedback parameter
	DelayConfigI *p = (DelayConfigI *)malloc(sizeof(DelayConfigI));
	pthread_mutex_init(&p->mutex, NULL);
	delayconfigi_setMaxSamples(p, maxSamples);
	p->rp[0] = p->wp[1] = 0;
	delayconfigi_set(p, delay, feedback);
	p->wet = 0.5f;
	p->numBeats = 4;
	p->beatmatch = false;
	return p;
}

void delayconfigi_set(void *p, float delay, float feedback) {
	DelayConfigI *config = (DelayConfigI *)p;
	delayconfigi_setDelayTime(config, delay);
	delayconfigi_setFeedback(config, feedback);
}

void delayconfigi_setMaxSamples(DelayConfigI *config, int maxSamples) {
	config->maxSamples = maxSamples;
	//free(config->delayBuffer[0]);
	//free(config->delayBuffer[1]);
	config->delayBuffer = (float **)malloc(2*sizeof(float *));
	config->delayBuffer[0] = (float *)malloc(maxSamples*sizeof(float));
	config->delayBuffer[1] = (float *)malloc(maxSamples*sizeof(float));
}

void delayconfigi_setFeedback(DelayConfigI *config, float feedback) {
	int i;
	for (i = 0; i < 2; i++)
		config->feedback[i] = feedback > 0.f ? (feedback < 1.f ? feedback : 0.9999999f) : 0.f;
}

void delayconfigi_setNumBeats(DelayConfigI *config, int numBeats) {
	if (numBeats == config->numBeats) return;
	config->numBeats = numBeats;
	delayconfigi_syncToBPM(config);
}

void delayconfigi_syncToBPM(DelayConfigI *config) {
	if (!config->beatmatch) return;
	// divide by 60 for seconds, divide by 16 for 16th notes
	float newTime = (BPM/960.0f)*(float)config->numBeats;
	delayconfigi_setDelayTime(config, newTime);	
}

void delayconfigi_destroy(void *p){
	DelayConfigI *config = (DelayConfigI *)p;
	free(config->delayBuffer);
	free((DelayConfigI *)p);
}

FilterConfig *filterconfig_create(float f, float r) {
	FilterConfig *config = (FilterConfig *)malloc(sizeof(FilterConfig));
	config->hp = false;
	config->in1[0] = config->in1[1] = 0;
	config->in2[0] = config->in2[1] = 0;
	config->out1[0] = config->out1[1] = 0;
	config->out2[0] = config->out2[1] = 0;
	filterconfig_set(config, f, r);
	return config;
}

void filterconfig_set(void *p, float f, float r) {
	FilterConfig *config = (FilterConfig *)p;
	config->f = f;
	config->r = r;	
	float f0 = f * INV_SAMPLE_RATE;
	if (config->hp) { // highpass filter settings
		config->c = f0 < 0.1f ? f0 * M_PI : tan(M_PI * f0);
		config->a1 = 1.0f/(1.0f + config->r * config->c + config->c * config->c);
		config->a2 = -2.0f * config->a1;
		config->a3 = config->a1;
		config->b1 = 2.0f * (config->c * config->c - 1.0f) * config->a1;
	} else { // lowpass filter settings
		// for frequencies < ~ 4000 Hz, approximate the tan function as an optimization.
		config->c = f0 < 0.1f ? 1.0f / (f0 * M_PI) : tan((0.5f - f0) * M_PI);
		config->a1 = 1.0f/(1.0f + config->r * config->c + config->c * config->c);
		config->a2 = 2.0f * config->a1;
		config->a3 = config->a1;
		config->b1 = 2.0f * (1.0f - config->c * config->c) * config->a1;
	}
	config->b2 = (1.0f - config->r * config->c + config->c * config->c) * config->a1;
}

void filterconfig_destroy(void *p) {
	free((FilterConfig *)p);
}

FlangerConfig *flangerconfig_create(float delayTime, float feedback) {
	FlangerConfig *flangerConfig = (FlangerConfig *)malloc(sizeof(FlangerConfig));
	float delayTimeSamples = delayTime*SAMPLE_RATE;
	flangerConfig->delayConfig = delayconfigi_create(delayTime, feedback, MAX_FLANGER_DELAY + 1024);
	flangerconfig_set(flangerConfig, delayTimeSamples, feedback);
	flangerConfig->mod = sinewave_create();
	flangerConfig->modAmt = .5f;
	return flangerConfig;
}

void flangerconfig_set(void *p, float delayTimeInSamples, float feedback) {
	FlangerConfig *config = (FlangerConfig *)p;
	flangerconfig_setBaseTime(config, delayTimeInSamples);
	delayconfigi_setDelaySamples(config->delayConfig, delayTimeInSamples);
	flangerconfig_setFeedback(config, feedback);
}

void flangerconfig_setBaseTime(FlangerConfig *config, float baseTime) {
	config->baseTime = baseTime;
}

void flangerconfig_setFeedback(FlangerConfig *config, float feedback) {
	delayconfigi_setFeedback(config->delayConfig, feedback);
}

void flangerconfig_setModRate(FlangerConfig *config, float modRate) {
	sinewave_setRate(config->mod, modRate/2);
}

void flangerconfig_setModAmt(FlangerConfig *config, float modAmt) {
	config->modAmt = modAmt;
}

void flangerconfig_destroy(void *p) {
	FlangerConfig *config = (FlangerConfig *)p;
	delayconfigi_destroy(config->delayConfig);
	free(config);
}

PitchConfig *pitchconfig_create() {
	PitchConfig *config = (PitchConfig *)malloc(sizeof(PitchConfig));
	config->delayLength = MAX_PITCH_DELAY_SAMPS - 24;
	config->halfLength = config->delayLength / 2;
	config->delaySamples[0] = 12;
	config->delaySamples[1] = MAX_PITCH_DELAY_SAMPS / 2;
	
	config->delayLine[0] = delayconfigi_create(0, 1, MAX_PITCH_DELAY_SAMPS);
	delayconfigi_setDelaySamples(config->delayLine[0], config->delaySamples[0]);
	config->delayLine[1] = delayconfigi_create(0, 1, MAX_PITCH_DELAY_SAMPS);
	delayconfigi_setDelaySamples(config->delayLine[1], config->delaySamples[1]);
	
	config->rate = 1.0f;
	config->wet = 0.5f;
	return config;
}

void pitchconfig_setShift(PitchConfig *config, float shift) {
	if (shift == 1.0) {
		config->rate = 0.0;
		config->delaySamples[0] = config->halfLength + 12;
	} else {
		config->rate = 1.0 - shift;
	}
}

void pitchconfig_destroy(void *p) {
	PitchConfig *config = (PitchConfig *)p;
	delayconfigi_destroy(config->delayLine[0]);
	delayconfigi_destroy(config->delayLine[1]);
	free((PitchConfig *)config);
}

VolumePanConfig *volumepanconfig_create(float volume, float pan) {
    VolumePanConfig *p = (VolumePanConfig *)malloc(sizeof(VolumePanConfig));
	p->volume = volume;
	p->pan = pan;
	return p;
}

void volumepanconfig_set(void *p, float volume, float pan) {
	VolumePanConfig *config = (VolumePanConfig *)p;
	config->volume = volume;
	config->pan = pan;
}

void volumepanconfig_destroy(void *p) {
	if(p != NULL) free((VolumePanConfig *)p);
}

ReverbConfig *reverbconfig_create(float feedback, float hfDamp) {
	ReverbConfig *config = (ReverbConfig *)malloc(sizeof(ReverbConfig));
	config->state = initReverbState();
	config->feedback = feedback;
	config->hfDamp = hfDamp;
	
	return config;
}

void reverbconfig_set(void *p, float feedback, float hfDamp) {
	ReverbConfig *config = (ReverbConfig *)p;
	config->feedback = feedback;
	config->hfDamp = hfDamp;
}

void reverbconfig_destroy(void *p) {
	ReverbConfig *config = (ReverbConfig *)p;
	free(config->state);
	config->state = NULL;
	free(config);
	config = NULL;
}

void swap(float *a , float *b) {
    float tmp;
    tmp = *a;
    (*a) = (*b);
    (*b) = tmp;
}

void reverse(float buffer[], int begin, int end) {
	int i, j;
    //swap 1st with last, then 2nd with last-1, etc.  Till we reach the middle of the string.
	for (i = begin, j = end - 1; i < j; i++, j--) {
        swap( &buffer[i] , &buffer[j]);	
	}
}

void normalize(float buffer[], int size) {
	float maxSample = 0;	
	int i;
	for (i = 0; i < size; i++) {
		if (abs(buffer[i]) > maxSample) {
			maxSample = abs(buffer[i]);
		}
	}
	if (maxSample != 0) {
		for (i = 0; i < size; i++) {
			buffer[i] /= maxSample;
		}
	}		
}
