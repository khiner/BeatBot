#ifndef FILEGEN_H
#define FILEGEN_H

typedef struct FileGen_t {
	// mutex for buffer since setting the data happens on diff thread than processing
	FILE *sampleFile;
	AdsrConfig *adsr;
	float tempSample[4];
	float **samples;
	float currFrame;
	long frames;
	long loopBegin;
	long loopEnd;
	long loopLength;
	bool looping;
	bool reverse;
	float sampleRate;
	float gain;
	int channels;
	char *sampleFileName;
} FileGen;

FileGen *filegen_create(const char *sampleName);
float filegen_getSample(FileGen *fileGen, int sampleIndex, int channel);
void filegen_setSampleFile(FileGen *fileGen, const char *sampleFileName);
void filegen_setLoopWindow(FileGen *fileGen, long loopBeginSample,
		long loopEndSample);
void filegen_setReverse(FileGen *fileGen, bool reverse);
void filegen_reset(FileGen *config);
void freeBuffers(FileGen *config);

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
	if (config->samples == NULL) {
		fseek(config->sampleFile, frame * config->channels * ONE_FLOAT_SZ,
				SEEK_SET);
		fread(config->tempSample, config->channels, TWO_FLOAT_SZ,
				config->sampleFile);

		for (channel = 0; channel < config->channels; channel++) {
			// interpolate the next two samples linearly
			sample[channel] = (1.0f - remainder) * config->tempSample[channel]
					+ remainder
							* config->tempSample[config->channels + channel];
		}

		if (config->channels == 1) {
			sample[1] = sample[0];
		}
	} else {
		for (channel = 0; channel < config->channels; channel++) {
			// copy left channel to right channel if mono
			float samp1 = config->samples[channel][frame];
			float samp2 = config->samples[channel][frame + 1];
			sample[channel] = (1.0f - remainder) * samp1 + remainder * samp2;
		}
		if (config->channels == 1) {
			sample[1] = sample[0];
		}
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
	for (i = 0; i < size; i++) {
		filegen_tick(config, config->tempSample);
		for (channel = 0; channel < 2; channel++) {
			inBuffer[channel][i] = config->tempSample[channel];
		}
	}
}

void filegen_destroy(void *config);

#endif // FILEGEN_H
