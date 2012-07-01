#include "effects.h"
#include <android/log.h>

static inline void underguard(float *x) {
  	union {
	    u_int32_t i;
	    float f;
  	} ix;
  	ix.f = *x;
  	if((ix.i & 0x7f800000)==0) *x=0.0f;
}

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

void initEffect(Effect *effect, bool on, bool dynamic, void *config,
				void (*set), void (*process), void (*destroy)) {
	effect->on = on;
	effect->dynamic = dynamic;
	effect->config = config;
	effect->set = set;
	effect->process = process;
	effect->destroy = destroy;				
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

void decimate_process(void *p, float **buffers, int size) {
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

void decimateconfig_destroy(void *p) {	
	if(p != NULL) free((DecimateConfig *)p);
}

DelayConfig *delayconfig_create(float delay, float feedback) {
	// allocate memory and set feedback parameter
	DelayConfig *p = (DelayConfig *)malloc(sizeof(DelayConfig));
	p->delayBuffer = (float **)malloc(2*sizeof(float *));
	p->delayBuffer[0] = (float *)malloc(SAMPLE_RATE*sizeof(float));
	p->delayBuffer[1] = (float *)malloc(SAMPLE_RATE*sizeof(float));
	p->delayBufferSize = SAMPLE_RATE;	
	delayconfig_set(p, delay, feedback);
	p->wet = 0.5f;
	p->numBeats = 4;
	p->beatmatch = false;
	int i;
	for (i = 0; i < 2; i++) {
		p->rp[i] = p->wp[i] = 0;
	}
	int count;
	return p;
}

void delayconfig_set(void *p, float delay, float feedback) {
	DelayConfig *config = (DelayConfig *)p;
	delayconfig_setDelayTime(config, delay);
	delayconfig_setFeedback(config, feedback);
}

void delayconfig_setDelayTime(DelayConfig *config, float delay) {
	config->delayTime = delay > 0 ? (delay <= 1 ? delay : 1) : 0;
	config->delaySamples = (int)(config->delayTime*SAMPLE_RATE);
	if (config->delaySamples < 8) config->delaySamples = 8;
	int i, *rp, *wp;
	for (i = 0; i < 2; i++) {
		rp = &(config->rp[i]);
		wp = &(config->wp[i]);
		*rp = *wp - config->delaySamples;
		while (*rp < config->delaySamples) {
			*rp += config->delaySamples;
		}
	}
}

void delayconfig_setFeedback(DelayConfig *config, float feedback) {
	int i;
	for (i = 0; i < 2; i++)
		config->feedback[i] = feedback > 0.f ? (feedback < 1.f ? feedback : 0.9999999f) : 0.f;
}

void delayconfig_setNumBeats(DelayConfig *config, int numBeats) {
	if (numBeats == config->numBeats) return;
	config->numBeats = numBeats;
	delayconfig_syncToBPM(config);
}

void delayconfig_syncToBPM(DelayConfig *config) {
	if (!config->beatmatch) return;
	// divide by 60 for seconds, divide by 16 for 16th notes
	float newTime = (BPM/960.0f)*(float)config->numBeats;
	delayconfig_setDelayTime(config, newTime);	
}

void delay_process(void *p, float **buffers, int size) {
	DelayConfig *config = (DelayConfig *)p;
	float out;	
	int i, j, *wp, *rp;
	for (i = 0; i < 2; i++) {
		//delayconfig_setDelayTime(config, 0.005f + 0.01f*sin(100*M_PI*config->count++*INV_SAMPLE_RATE));	
		rp = &(config->rp[i]);
		wp = &(config->wp[i]);
		for (j = 0; j < size; j++) {
			out = config->delayBuffer[i][(*rp)++ % config->delaySamples]*config->wet + buffers[i][j]*(1 - config->wet);
			if (out > 1) out = 1;
			config->delayBuffer[i][(*wp)++ % config->delaySamples] = buffers[i][j] + out*config->feedback[i];
			buffers[i][j] = out;
		}
	}
}

void delayconfig_destroy(void *p){
	// free memory
	if(p != NULL) free((DelayConfig *)p);
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
	if (config->hp) { // lowpass filter settings
		config->c = f0 < 0.1f ? f0 * M_PI : tan(M_PI * f0);
		config->a1 = 1.0f/(1.0f + config->r * config->c + config->c * config->c);
		config->a2 = -2.0f * config->a1;
		config->a3 = config->a1;
		config->b1 = 2.0f * (config->c * config->c - 1.0f) * config->a1;
	} else { // highpass filter settings
		// for frequencies < ~ 4000 Hz, approximate the tan function as an optimization.
		config->c = f0 < 0.1f ? 1.0f / (f0 * M_PI) : tan((0.5f - f0) * M_PI);
		config->a1 = 1.0f/(1.0f + config->r * config->c + config->c * config->c);
		config->a2 = 2.0f * config->a1;
		config->a3 = config->a1;
		config->b1 = 2.0f * (1.0f - config->c * config->c) * config->a1;
	}
	config->b2 = (1.0f - config->r * config->c + config->c * config->c) * config->a1;
}

void filter_process(void *p, float **buffers, int size) {
	FilterConfig *config = (FilterConfig *)p;
	int i, j;
	for (i = 0; i < 2; i++) {
		for(j = 0; j < size; j++) {
			float out = config->a1 * buffers[i][j] +
				        config->a2 * config->in1[i] +
					    config->a3 * config->in2[i] -
					    config->b1 * config->out1[i] -
					    config->b2 * config->out2[i];
			config->in2[i] = config->in1[i];
			config->in1[i] = buffers[i][j];
			config->out2[i] = config->out1[i];
			config->out1[i] = out;
			buffers[i][j] = out;
		}	
	}
}

void filterconfig_destroy(void *p) {
	free((FilterConfig *)p);
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

void volumepan_process(void *p, float **buffers, int size) {
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

void reverb_process(void *p, float **buffers, int size) {	
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
