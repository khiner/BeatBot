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
} WavFile;

WavFile *wavfile_create(const char *sampleName);
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
		fseek(config->sampleFile, sampleIndex * TWO_FLOAT_SZ, SEEK_SET);
		fread(config->tempSample, 1, FOUR_FLOAT_SZ, config->sampleFile);
		for (channel = 0; channel < 2; channel++) {
			// interpolate the next two samples linearly
			sample[channel] = (1.0f - remainder) * config->tempSample[channel]
					+ remainder * config->tempSample[2 + channel];
		}
	} else {
		for (channel = 0; channel < 2; channel++) {
			// copy left channel to right channel if mono
			float samp1 =
					config->samples[1] == NULL ?
							config->samples[0][sampleIndex] :
							config->samples[channel][sampleIndex];
			float samp2 =
					config->samples[1] == NULL ?
							config->samples[0][sampleIndex + 1] :
							config->samples[channel][sampleIndex + 1];
			sample[channel] = (1.0f - remainder) * samp1 + remainder * samp2;
		}
	}

	// get next sample.  if reverse, go backwards, else go forwards
	if (config->reverse) {
		config->currSample -= config->sampleRate;
	} else {
		config->currSample += config->sampleRate;
	}
	float gain = adsr_tick(config->adsr);
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
