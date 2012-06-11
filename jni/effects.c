#include "effects.h"
#include <stdlib.h>
#include <android/log.h>

#define M_PI 3.14159265358979323846
#define MAX_FRAME_LENGTH 8192

void smbFft(float *fftBuffer, long fftFrameSize, long sign);
double smbAtan2(double x, double y);


// -----------------------------------------------------------------------------------------------------------------


void smbPitchShift(float pitchShift, long numSampsToProcess, long fftFrameSize, long osamp, float sampleRate, float *indata, float *outdata)
/*
	Routine smbPitchShift(). See top of file for explanation
	Purpose: doing pitch shifting while maintaining duration using the Short
	Time Fourier Transform.
	Author: (c)1999-2009 Stephan M. Bernsee <smb [AT] dspdimension [DOT] com>
*/
{

	static float gInFIFO[MAX_FRAME_LENGTH];
	static float gOutFIFO[MAX_FRAME_LENGTH];
	static float gFFTworksp[2*MAX_FRAME_LENGTH];
	static float gLastPhase[MAX_FRAME_LENGTH/2+1];
	static float gSumPhase[MAX_FRAME_LENGTH/2+1];
	static float gOutputAccum[2*MAX_FRAME_LENGTH];
	static float gAnaFreq[MAX_FRAME_LENGTH];
	static float gAnaMagn[MAX_FRAME_LENGTH];
	static float gSynFreq[MAX_FRAME_LENGTH];
	static float gSynMagn[MAX_FRAME_LENGTH];
	static long gRover = false, gInit = false;
	double magn, phase, tmp, window, real, imag;
	double freqPerBin, expct;
	long i,k, qpd, index, inFifoLatency, stepSize, fftFrameSize2;

	/* set up some handy variables */
	fftFrameSize2 = fftFrameSize/2;
	stepSize = fftFrameSize/osamp;
	freqPerBin = sampleRate/(double)fftFrameSize;
	expct = 2.*M_PI*(double)stepSize/(double)fftFrameSize;
	inFifoLatency = fftFrameSize-stepSize;
	if (gRover == false) gRover = inFifoLatency;

	/* initialize our static arrays */
	if (gInit == false) {
		memset(gInFIFO, 0, MAX_FRAME_LENGTH*sizeof(float));
		memset(gOutFIFO, 0, MAX_FRAME_LENGTH*sizeof(float));
		memset(gFFTworksp, 0, 2*MAX_FRAME_LENGTH*sizeof(float));
		memset(gLastPhase, 0, (MAX_FRAME_LENGTH/2+1)*sizeof(float));
		memset(gSumPhase, 0, (MAX_FRAME_LENGTH/2+1)*sizeof(float));
		memset(gOutputAccum, 0, 2*MAX_FRAME_LENGTH*sizeof(float));
		memset(gAnaFreq, 0, MAX_FRAME_LENGTH*sizeof(float));
		memset(gAnaMagn, 0, MAX_FRAME_LENGTH*sizeof(float));
		gInit = true;
	}

	/* main processing loop */
	for (i = 0; i < numSampsToProcess; i++){

		/* As long as we have not yet collected enough data just read in */
		gInFIFO[gRover] = indata[i];
		outdata[i] = gOutFIFO[gRover-inFifoLatency];
		gRover++;

		/* now we have enough data for processing */
		if (gRover >= fftFrameSize) {
			gRover = inFifoLatency;

			/* do windowing and re,im interleave */
			for (k = 0; k < fftFrameSize;k++) {
				window = -.5*cos(2.*M_PI*(double)k/(double)fftFrameSize)+.5;
				gFFTworksp[2*k] = gInFIFO[k] * window;
				gFFTworksp[2*k+1] = 0.;
			}


			/* ***************** ANALYSIS ******************* */
			/* do transform */
			smbFft(gFFTworksp, fftFrameSize, -1);

			/* this is the analysis step */
			for (k = 0; k <= fftFrameSize2; k++) {

				/* de-interlace FFT buffer */
				real = gFFTworksp[2*k];
				imag = gFFTworksp[2*k+1];

				/* compute magnitude and phase */
				magn = 2.*sqrt(real*real + imag*imag);
				phase = atan2(imag,real);

				/* compute phase difference */
				tmp = phase - gLastPhase[k];
				gLastPhase[k] = phase;

				/* subtract expected phase difference */
				tmp -= (double)k*expct;

				/* map delta phase into +/- Pi interval */
				qpd = tmp/M_PI;
				if (qpd >= 0) qpd += qpd&1;
				else qpd -= qpd&1;
				tmp -= M_PI*(double)qpd;

				/* get deviation from bin frequency from the +/- Pi interval */
				tmp = osamp*tmp/(2.*M_PI);

				/* compute the k-th partials' true frequency */
				tmp = (double)k*freqPerBin + tmp*freqPerBin;

				/* store magnitude and true frequency in analysis arrays */
				gAnaMagn[k] = magn;
				gAnaFreq[k] = tmp;

			}

			/* ***************** PROCESSING ******************* */
			/* this does the actual pitch shifting */
			memset(gSynMagn, 0, fftFrameSize*sizeof(float));
			memset(gSynFreq, 0, fftFrameSize*sizeof(float));
			for (k = 0; k <= fftFrameSize2; k++) { 
				index = k*pitchShift;
				if (index <= fftFrameSize2) { 
					gSynMagn[index] += gAnaMagn[k]; 
					gSynFreq[index] = gAnaFreq[k] * pitchShift; 
				} 
			}
			
			/* ***************** SYNTHESIS ******************* */
			/* this is the synthesis step */
			for (k = 0; k <= fftFrameSize2; k++) {

				/* get magnitude and true frequency from synthesis arrays */
				magn = gSynMagn[k];
				tmp = gSynFreq[k];

				/* subtract bin mid frequency */
				tmp -= (double)k*freqPerBin;

				/* get bin deviation from freq deviation */
				tmp /= freqPerBin;

				/* take osamp into account */
				tmp = 2.*M_PI*tmp/osamp;

				/* add the overlap phase advance back in */
				tmp += (double)k*expct;

				/* accumulate delta phase to get bin phase */
				gSumPhase[k] += tmp;
				phase = gSumPhase[k];

				/* get real and imag part and re-interleave */
				gFFTworksp[2*k] = magn*cos(phase);
				gFFTworksp[2*k+1] = magn*sin(phase);
			} 

			/* zero negative frequencies */
			for (k = fftFrameSize+2; k < 2*fftFrameSize; k++) gFFTworksp[k] = 0.;

			/* do inverse transform */
			smbFft(gFFTworksp, fftFrameSize, 1);

			/* do windowing and add to output accumulator */ 
			for(k=0; k < fftFrameSize; k++) {
				window = -.5*cos(2.*M_PI*(double)k/(double)fftFrameSize)+.5;
				gOutputAccum[k] += 2.*window*gFFTworksp[2*k]/(fftFrameSize2*osamp);
			}
			for (k = 0; k < stepSize; k++) gOutFIFO[k] = gOutputAccum[k];

			/* shift accumulator */
			memmove(gOutputAccum, gOutputAccum+stepSize, fftFrameSize*sizeof(float));

			/* move input FIFO */
			for (k = 0; k < inFifoLatency; k++) gInFIFO[k] = gInFIFO[k+stepSize];
		}
	}
}

// -----------------------------------------------------------------------------------------------------------------


void smbFft(float *fftBuffer, long fftFrameSize, long sign)
/* 
	FFT routine, (C)1996 S.M.Bernsee. Sign = -1 is FFT, 1 is iFFT (inverse)
	Fills fftBuffer[0...2*fftFrameSize-1] with the Fourier transform of the
	time domain data in fftBuffer[0...2*fftFrameSize-1]. The FFT array takes
	and returns the cosine and sine parts in an interleaved manner, ie.
	fftBuffer[0] = cosPart[0], fftBuffer[1] = sinPart[0], asf. fftFrameSize
	must be a power of 2. It expects a complex input signal (see footnote 2),
	ie. when working with 'common' audio signals our input signal has to be
	passed as {in[0],0.,in[1],0.,in[2],0.,...} asf. In that case, the transform
	of the frequencies of interest is in fftBuffer[0...fftFrameSize].
*/
{
	float wr, wi, arg, *p1, *p2, temp;
	float tr, ti, ur, ui, *p1r, *p1i, *p2r, *p2i;
	long i, bitm, j, le, le2, k;

	for (i = 2; i < 2*fftFrameSize-2; i += 2) {
		for (bitm = 2, j = 0; bitm < 2*fftFrameSize; bitm <<= 1) {
			if (i & bitm) j++;
			j <<= 1;
		}
		if (i < j) {
			p1 = fftBuffer+i; p2 = fftBuffer+j;
			temp = *p1; *(p1++) = *p2;
			*(p2++) = temp; temp = *p1;
			*p1 = *p2; *p2 = temp;
		}
	}
	for (k = 0, le = 2; k < (long)(log(fftFrameSize)/log(2.)+.5); k++) {
		le <<= 1;
		le2 = le>>1;
		ur = 1.0;
		ui = 0.0;
		arg = M_PI / (le2>>1);
		wr = cos(arg);
		wi = sign*sin(arg);
		for (j = 0; j < le2; j += 2) {
			p1r = fftBuffer+j; p1i = p1r+1;
			p2r = p1r+le2; p2i = p2r+1;
			for (i = j; i < 2*fftFrameSize; i += le) {
				tr = *p2r * ur - *p2i * ui;
				ti = *p2r * ui + *p2i * ur;
				*p2r = *p1r - tr; *p2i = *p1i - ti;
				*p1r += tr; *p1i += ti;
				p1r += le; p1i += le;
				p2r += le; p2i += le;
			}
			tr = ur*wr - ui*wi;
			ui = ur*wi + ui*wr;
			ur = tr;
		}
	}
}


// -----------------------------------------------------------------------------------------------------------------

/*

    12/12/02, smb
    
    PLEASE NOTE:
    
    There have been some reports on domain errors when the atan2() function was used
    as in the above code. Usually, a domain error should not interrupt the program flow
    (maybe except in Debug mode) but rather be handled "silently" and a global variable
    should be set according to this error. However, on some occasions people ran into
    this kind of scenario, so a replacement atan2() function is provided here.
    
    If you are experiencing domain errors and your program stops, simply replace all
    instances of atan2() with calls to the smbAtan2() function below.
    
*/


double smbAtan2(double x, double y)
{
  double signx;
  if (x > 0.) signx = 1.;  
  else signx = -1.;
  
  if (x == 0.) return 0.;
  if (y == 0.) return signx * M_PI / 2.;
  
  return atan2(x, y);
}

VolumePanConfig *volumepanconfig_create(float volume, float pan) {
        VolumePanConfig *p = (VolumePanConfig *)malloc(sizeof(VolumePanConfig));
	p->volume = volume;
	p->pan = pan;
	return p;
}

void volumepanconfig_set(VolumePanConfig *p, float volume, float pan) {
	p->volume = volume;
	p->pan = pan;
}

void volumepan_process(VolumePanConfig *p, float buffer[], int size) {
	float leftVolume = (1 - p->pan)*p->volume;
	float rightVolume = p->pan*p->volume;

	int i;
	for (i = 0; i < size; i+=2) {
      	        // left channel
		buffer[i] = buffer[i]*leftVolume;
                // right channel
		buffer[i+1] = buffer[i + 1]*rightVolume;
	}  
}

void *volumepanconfig_destroy(VolumePanConfig *p) {
        if(p != NULL) free(p);
}

struct soundtouch4c *pitchconfig_create(float pitch) {
//	struct soundtouch4c *snd;
//	snd = SoundTouch_construct();

//	SoundTouch_setChannels(snd, 2);
//	SoundTouch_setSampleRate(snd, 8000);
//	SoundTouch_setSetting(snd, SETTING_USE_QUICKSEEK, 1);
//	SoundTouch_setSetting(snd, SETTING_USE_AA_FILTER, 1);
//	SoundTouch_setPitchSemiTonesFloat(snd, -5.0f);
	
//	return snd;
	return NULL;
}

void pitchconfig_set(struct soundtouch4c *snd, float pitch) {
//	SoundTouch_setPitchSemiTonesFloat(snd, pitch);
}

void pitch_process(float pitch, float buffer[], int size) {
//	int i;
//	short short_buf[512];

	// convert buffer to short
//	for (i = 0; i < 512; i++) {
//		short_buf[i] = (short)(buffer[i]*32767);
//	}

	/* enqueue samples to SoundTouch for pitch shifting. */
//	SoundTouch_putSamples(snd, short_buf, 256);
		/* copy data back.  when dealing with smaller buffer sizes,
 	* you might have to wait through a few passes before this
	 * routine actually starts returning data */	 
	 
//	SoundTouch_receiveSamplesEx(snd, short_buf, 256);

//	for (i = 0; i < 512; i++) {
//		buffer[i] = (float)short_buf[i]/32767.0f;
//	}
	smbPitchShift(pitch, size, 2048, 4, 44100, buffer, buffer);	
}

void pitchconfig_destroy(struct soundtouch4c *snd) {
	SoundTouch_destruct(snd);
}

DelayConfig *delayconfig_create(float delay, float fdb) {
	// allocate memory and set feedback parameter
	DelayConfig *p = (DelayConfig *)malloc(sizeof(DelayConfig));
	p->size = delay*41000;	
	p->delay = calloc(sizeof(float), p->size);
	p->rp = 0;
	p->fdb = fdb > 0.f ? (fdb < 1.f ? fdb : 0.99999999f) : 0.f;
	return p;
}

void delay_process(DelayConfig *p,float buffer[], int size) {
	// process the delay, replacing the buffer
	float out, *delay = p->delay, fdb = p->fdb;
	int i, dsize = p->size, *rp = &(p->rp);
	for(i = 0; i < size; i++){
		out = delay[*rp];
		p->delay[(*rp)++] = buffer[i] + out*fdb;
		if(*rp == dsize) *rp = 0;
		buffer[i] = out;
	}
}

void *delayconfig_destroy(DelayConfig *p){
	// free memory
	if(p != NULL) free(p);
}

FilterConfig *filterconfig_create(float cutoff, float q) {
        FilterConfig *filterConfig = malloc(sizeof(FilterConfig));
	filterConfig->history1 = 0;
	filterConfig->history2 = 0;
	filterConfig->history3 = 0;
	filterConfig->history4 = 0;

	float pi = 3.1415926535897;
	float fs = 44100; // sample rate

	filterConfig->t0 = 4.f * fs * fs;
	filterConfig->t1 = 8.f * fs * fs;
	filterConfig->t2 = 2.f * fs;
	filterConfig->t3 = pi / fs;

	filterConfig->min_cutoff = fs * 0.01f;
	filterConfig->max_cutoff = fs * 0.45f;
	filterconfig_set(filterConfig, cutoff, q);
	return filterConfig;
}

void filterconfig_set(FilterConfig *filterConfig, float cutoff, float q) {
        if (cutoff < filterConfig->min_cutoff)
            cutoff = filterConfig->min_cutoff;
        else if(cutoff > filterConfig->max_cutoff)
            cutoff = filterConfig->max_cutoff;

	if(q < 0.f)
   	    q = 0.f;
	else if(q > 1.f)
	    q = 1.f;

	float wp = filterConfig->t2 * tanf(filterConfig->t3 * cutoff);
	float bd, bd_tmp, b1, b2;

	q *= BUDDA_Q_SCALE;
	q += 1.f;

	b1 = (0.765367f / (q*wp));
	b2 = 1.f / (wp * wp);

	bd_tmp = filterConfig->t0 * b2 + 1.f;

	bd = 1.f / (bd_tmp + filterConfig->t2 * b1);

	filterConfig->gain = bd;

	filterConfig->coef2 = (2.f - filterConfig->t1 * b2);

	filterConfig->coef0 = filterConfig->coef2 * bd;
	filterConfig->coef1 = (bd_tmp - filterConfig->t2 * b1) * bd;

	b1 = (1.847759f / (q*wp));

	bd = 1.f / (bd_tmp + filterConfig->t2 * b1);

	filterConfig->gain *= bd;
	filterConfig->coef2 *= bd;
	filterConfig->coef3 = (bd_tmp - filterConfig->t2 * b1) * bd;
}

void filter_process(FilterConfig *p, float buffer[], int size) {
	int i;
	for (i = 0; i < size; i++) {
	    float output = buffer[i] * p->gain;
	    float new_hist;

	    output -= p->history1 * p->coef0;
	    new_hist = output - p->history2 * p->coef1;

	    output = new_hist + p->history1 * 2.f;
	    output += p->history2;

	    p->history2 = p->history1;
	    p->history1 = new_hist;

	    output -= p->history3 * p->coef2;
	    new_hist = output - p->history4 * p->coef3;

	    output = new_hist + p->history3 * 2.f;
	    output += p->history4;

	    p->history4 = p->history3;
	    p->history3 = new_hist;

	    buffer[i] = output;
	}
}

void *filterconfig_destroy(FilterConfig *p) {
	if(p != NULL) free(p);
}

DecimateConfig *decimateconfig_create(int bits, float rate) {
        DecimateConfig *decimateConfig = malloc(sizeof(DecimateConfig));
	decimateConfig->cnt = 0;
	decimateConfig->y = 0;
	decimateConfig->bits = bits;
	decimateConfig->rate = rate;
}

void decimate_process(DecimateConfig *p, float buffer[], int size) {
        long int m = 1 << (p->bits - 1);
	int i;
	for (i = 0; i < size; i++) {
	    p->cnt += p->rate;
	    if (p->cnt >= 1) {
	        p->cnt -= 1;
		p->y = (long int)(buffer[i]*m)/(float)m;
	    }
	    buffer[i] = p->y;
	}
}

void decimateconfig_destroy(DecimateConfig *p) {
	if(p != NULL) free(p);
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
