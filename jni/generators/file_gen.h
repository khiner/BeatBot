#ifndef FILEGEN_H
#define FILEGEN_H

#include "libsndfile/sndfile.h"

typedef struct FileGen_t {
	// mutex for buffer since setting the data happens on diff thread than processing
	pthread_mutex_t fileMutex;
	SNDFILE *sampleFile;
	AdsrConfig *adsr;
	float tempSample[2];
	float otherTempSample[2];
	float *buffer;
	float currFrame;
	long frames;
	long loopBegin;
	long loopEnd;
	long loopLength;
	long bufferStartFrame;
	bool looping;
	bool reverse;
	float sampleRate;
	float gain;
	int channels;
} FileGen;

FileGen *filegen_create(const char *sampleName);
float filegen_getSample(FileGen *fileGen, long frame, int channel);
void filegen_setSampleFile(FileGen *fileGen, const char *sampleFileName);
void filegen_setLoopWindow(FileGen *fileGen, long loopBeginSample,
		long loopEndSample);
void filegen_setReverse(FileGen *fileGen, bool reverse);
void filegen_reset(FileGen *config);

static inline void filegen_sndFileRead(FileGen *config, long frame,
		float *sample) {

	if (frame >= config->bufferStartFrame + BUFF_SIZE
			|| frame < config->bufferStartFrame) {
		long seekTo =
				config->reverse ?
						(frame - BUFF_SIZE >= 0 ? frame - BUFF_SIZE : 0) :
						frame;
		sf_seek(config->sampleFile, seekTo, SEEK_SET);
		sf_readf_float(config->sampleFile, config->buffer, BUFF_SIZE);
		config->bufferStartFrame = seekTo;
	}
	frame -= config->bufferStartFrame;

	int channel;
	for (channel = 0; channel < config->channels; channel++) {
		sample[channel] = config->buffer[frame * config->channels + channel];
	}
}

static inline void filegen_tick(FileGen *config, float *sample) {
	// wrap sample around loop window
	if (config->looping) {
		if (config->currFrame >= config->loopEnd) {
			config->currFrame -= config->loopLength;
		} else if (config->currFrame <= config->loopBegin) {
			config->currFrame += config->loopLength;
		}
	}

	if (config->currFrame > config->loopEnd
			|| config->currFrame < config->loopBegin) {
		sample[0] = sample[1] = 0;
		return;
	}

	// perform linear interpolation on the next two samples
	// (ignoring wrapping around loop - this is close enough and we avoid an extra
	//  read from disk)
	long frame = (long) config->currFrame;
	float remainder = config->currFrame - frame;

	// read next two samples from current sample (rounded down)
	int channel;

	filegen_sndFileRead(config, frame, config->tempSample);
	filegen_sndFileRead(config, frame + 1, config->otherTempSample);
	for (channel = 0; channel < config->channels; channel++) {
		float samp1 = config->tempSample[channel];
		float samp2 = config->otherTempSample[channel];
		sample[channel] = (1.0f - remainder) * samp1 + remainder * samp2;
	}

	// copy left channel to right channel if mono
	if (config->channels == 1) {
		sample[1] = sample[0];
	}

	// get next sample.  if reverse, go backwards, else go forwards
	if (config->reverse) {
		config->currFrame -= config->sampleRate;
	} else {
		config->currFrame += config->sampleRate;
	}
	float gain = adsr_tick(config->adsr) * config->gain;
	sample[0] *= gain;
	sample[1] *= gain;
}

static inline void filegen_generate(FileGen *config, float **inBuffer,
		int size) {
	int i, channel;
	pthread_mutex_lock(&config->fileMutex);
	for (i = 0; i < size; i++) {
		filegen_tick(config, config->tempSample);
		for (channel = 0; channel < 2; channel++) {
			inBuffer[channel][i] = config->tempSample[channel];
		}
	}
	pthread_mutex_unlock(&config->fileMutex);
}

void filegen_destroy(void *config);

#endif // FILEGEN_H
