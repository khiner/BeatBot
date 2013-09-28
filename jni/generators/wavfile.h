#ifndef WAVFILE_H
#define WAVFILE_H

typedef struct WavFile_t {
	// mutex for buffer since setting the wav data happens on diff thread than processing
	FILE *sampleFile;
	AdsrConfig *adsr;
	float tempSample[4];
	float **samples;
	float currSample;
	long totalSamples;
	long loopBegin;
	long loopEnd;
	long loopLength;
	bool looping;
	bool reverse;
	float sampleRate;
	float gain;
	int channels;
	char *sampleFileName;
} WavFile;

WavFile *wavfile_create(const char *sampleName);
float wavfile_getSample(WavFile *wavFile, int sampleIndex, int channel);
void wavfile_setSampleFile(WavFile *wavFile, const char *sampleFileName);
void wavfile_setLoopWindow(WavFile *wavFile, long loopBeginSample,
		long loopEndSample);
void wavfile_setReverse(WavFile *wavFile, bool reverse);
void wavfile_reset(WavFile *config);
void freeBuffers(WavFile *config);

static inline void wavfile_tick(WavFile *config, float *sample) {
	// wrap sample around loop window
	if (config->looping) {
		if (config->currSample >= config->loopEnd) {
			config->currSample -= config->loopLength;
		} else if (config->currSample <= config->loopBegin) {
			config->currSample += config->loopLength;
		}
	}

	if (config->currSample > config->loopEnd
			|| config->currSample < config->loopBegin) {
		sample[0] = sample[1] = 0;
		return;
	}

	// perform linear interpolation on the next two samples
	// (ignoring wrapping around loop - this is close enough and we avoid an extra
	//  read from disk)
	long sampleIndex = (long) config->currSample;
	float remainder = config->currSample - sampleIndex;
	// read next two samples from current sample (rounded down)
	int channel;
	if (config->samples == NULL) {
		fseek(config->sampleFile, sampleIndex * config->channels * ONE_FLOAT_SZ, SEEK_SET);
		fread(config->tempSample, config->channels, TWO_FLOAT_SZ, config->sampleFile);
		for (channel = 0; channel < config->channels; channel++) {
			// interpolate the next two samples linearly
			sample[channel] = (1.0f - remainder) * config->tempSample[channel]
					+ remainder * config->tempSample[config->channels + channel];
		}
		if (config->channels == 1) {
			sample[1] = sample[0];
		}
	} else {
		for (channel = 0; channel < config->channels; channel++) {
			// copy left channel to right channel if mono
			float samp1 = config->samples[channel][sampleIndex];
			float samp2 = config->samples[channel][sampleIndex + 1];
			sample[channel] = (1.0f - remainder) * samp1 + remainder * samp2;
		}
		if (config->channels == 1) {
			sample[1] = sample[0];
		}
	}

	// get next sample.  if reverse, go backwards, else go forwards
	if (config->reverse) {
		config->currSample -= config->sampleRate;
	} else {
		config->currSample += config->sampleRate;
	}
	float gain = adsr_tick(config->adsr) * config->gain;
	sample[0] *= gain;
	sample[1] *= gain;
}

static inline void wavfile_generate(WavFile *config, float **inBuffer,
		int size) {
	int i, channel;
	for (i = 0; i < size; i++) {
		wavfile_tick(config, config->tempSample);
		for (channel = 0; channel < 2; channel++) {
			inBuffer[channel][i] = config->tempSample[channel];
		}
	}
}

void wavfile_destroy(void *config);

#endif // WAVFILE_H
