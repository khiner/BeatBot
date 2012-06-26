#include "effects.h"

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
    DecimateConfig *decimateConfig = malloc(sizeof(DecimateConfig));
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

void decimate_process(void *p, float buffer[], int size) {
	DecimateConfig *config = (DecimateConfig *)p;
    int m = 1 << (config->bits - 1);
	int i;
	for (i = 0; i < size; i++) {
	    config->cnt += config->rate;
	    if (config->cnt >= 1) {
	        config->cnt -= 1;
		config->y = (long int)(buffer[i]*m)/(float)m;
	    }
	    buffer[i] = config->y;
	}
}

void decimateconfig_destroy(void *p) {	
	if(p != NULL) free((DecimateConfig *)p);
}

DelayConfig *delayconfig_create(float time, float feedback) {
	// allocate memory and set feedback parameter
	DelayConfig *p = (DelayConfig *)malloc(sizeof(DelayConfig));
	p->beatmatch = false;
	p->numBeats = 4;
	p->rp = 0;
	delayconfig_set(p, time, feedback);
	return p;
}

void delayconfig_set(void *p, float time, float feedback) {
	DelayConfig *config = (DelayConfig *)p;
	config->time = time > 0.02f ? (time < 3.0f ? time : 3.0f) : 0.02f;
	config->size = config->time*SAMPLE_RATE;
	config->delay = calloc(sizeof(float), config->size);
	config->feedback = feedback > 0.f ? (feedback < 1.f ? feedback : 0.9999999f) : 0.f;
}

void delayconfig_setNumBeats(DelayConfig *config, int numBeats) {
	if (numBeats == config->numBeats) return;
	config->numBeats = numBeats;
	delayconfig_syncToBPM(config);
}

void delayconfig_syncToBPM(DelayConfig *config) {
	if (!config->beatmatch) return;
	float newTime = (BPM/480.0f)*(float)config->numBeats; // divide by 60 for seconds, divide by 8 for 8th notes
	delayconfig_setTime(config, newTime);	
}

void delayconfig_setTime(DelayConfig *config, float time) {
	config->time = time > 0.02f ? (time <= 3.0f ? time : 3.0f) : 0.02f;
	int newSize = config->time*SAMPLE_RATE;
	float *newBuffer = calloc(newSize, sizeof(float));
	if (config->size > 0 && config->size < newSize) {
		long prefix = newSize - config->size;
		//delay_process(config, &(newBuffer[prefix]), config->size);
		memcpy(newBuffer, config->delay, config->size*sizeof(float));		
	} else if (config->size > 0 && config->size > newSize) {
		long cut = config->size - newSize;
		float *tempZeros = calloc(cut, sizeof(float));
		delay_process(config, tempZeros, cut);
		free(tempZeros);
		delay_process(config, newBuffer, newSize);
		config->rp = 0;
	}
	config->size = newSize;
	
	float *oldPtr = config->delay;
	config->delay = newBuffer;
	free(oldPtr);
}

void delayconfig_setFeedback(DelayConfig *config, float feedback) {
	config->feedback = feedback > 0.f ? (feedback < 1.f ? feedback : 0.9999999f) : 0.f;
}

void delay_process(void *p, float buffer[], int size) {
	DelayConfig *config = (DelayConfig *)p;
	// process the delay, replacing the buffer
	float out, *delay = config->delay, feedback = config->feedback;
	int i, *rp = &(config->rp);
	for(i = 0; i < size; i++){
		out = delay[*rp];
		config->delay[(*rp)++] = buffer[i] + out*feedback;
		if(*rp == config->size) *rp = 0;
		buffer[i] = out;
	}
}

void delayconfig_destroy(void *p){
	// free memory
	if(p != NULL) free((DelayConfig *)p);
}

FilterConfig *filterconfig_create(float f, float r) {
	FilterConfig *config = (FilterConfig *)malloc(sizeof(FilterConfig));
	config->hp = false;
	config->in1 = 0;
	config->in2 = 0;
	config->out1 = 0;
	config->out2 = 0;
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

void filter_process(void *p, float buffer[], int size) {
	FilterConfig *config = (FilterConfig *)p;
	int i;
	for(i = 0; i < size; i++) {
		float out = config->a1 * buffer[i] +
					config->a2 * config->in1 +
					config->a3 * config->in2 -
					config->b1 * config->out1 -
					config->b2 * config->out2;
		config->in2 = config->in1;
		config->in1 = buffer[i];
		config->out2 = config->out1;
		config->out1 = out;
		buffer[i] = out;
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

void volumepan_process(void *p, float buffer[], int size) {
	VolumePanConfig *config = (VolumePanConfig *)p;
	float leftVolume = (1 - config->pan)*config->volume;
	float rightVolume = config->pan*config->volume;

	int i;
	for (i = 0; i < size; i+=2) {
		if (buffer[i] == 0) continue;
		buffer[i] = buffer[i]*leftVolume; // left channel
		buffer[i+1] = buffer[i + 1]*rightVolume; // right channel
	}  
}

void volumepanconfig_destroy(void *p) {
	if(p != NULL) free((VolumePanConfig *)p);
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

ReverbConfig *reverbconfig_create(float feedback, float hfDamp) {
	ReverbConfig *config = malloc(sizeof(ReverbConfig));
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

void reverb_process(void *p, float buffer[], int size) {	
	ReverbConfig *config = (ReverbConfig *)p;
  	float out, val=0;
  	int i, j;

  	for (i = 0; i < size; i++) {
    	out = 0;
    	val = buffer[i];
	    for(j = 0; j < numcombs; j++)
      		out += comb_process(config->state->comb + j, config->feedback, config->hfDamp, val);
	    for(j = 0; j < numallpasses; j++)
    	    out = allpass_process(config->state->allpass + j, out);
	    buffer[i] = out;
	}
}

void reverbconfig_destroy(void *p) {
	ReverbConfig *config = (ReverbConfig *)p;
	free(config->state);
	config->state = NULL;
	free(config);
	config = NULL;
}
