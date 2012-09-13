#include "wavfile.h"

static inline short charsToShort(unsigned char first, unsigned char second) {
	return (first << 8) | second;
}

WavFile *wavfile_create(char *bytes, int length) {
	WavFile *wavFile = (WavFile *)malloc(sizeof(WavFile));
	// each sample has one short for each channel (2 channels), and 2 bytes per short
	wavFile->totalSamples = (length - 44) / 4;
	__android_log_print(ANDROID_LOG_ERROR, "in wavfile create", "total samps = %d", wavFile->totalSamples);
	wavFile->buffers = (float **) malloc(2 * sizeof(float *));
	wavFile->buffers[0] = (float *) calloc(wavFile->totalSamples, sizeof(float));
	wavFile->buffers[1] = (float *) calloc(wavFile->totalSamples, sizeof(float));
	wavFile->looping = wavFile->reverse = false;
	wavFile->loopBegin = 0;
	wavFile->loopEnd = wavFile->totalSamples;
	wavFile->currSample = 0;

	int i;
	for (i = 0; i < wavFile->totalSamples; i++) {
		// first 44 bytes of a wav file are header
		wavFile->buffers[0][i] = charsToShort(bytes[i * 4 + 1 + 44],
				bytes[i * 4 + 44]) * CONVMYFLT;
		wavFile->buffers[1][i] = charsToShort(bytes[i * 4 + 3 + 44],
				bytes[i * 4 + 2 + 44]) * CONVMYFLT;
	}
	__android_log_print(ANDROID_LOG_ERROR, "in wavfile create", "after buffer copy");
	//free(bytes);
	__android_log_print(ANDROID_LOG_ERROR, "in wavfile create", "after free bytes");
	return wavFile;
}

void wavfile_reset(WavFile *config) {
	if (config->reverse)
		config->currSample = config->loopEnd;
	else
		config->currSample = config->loopBegin;
}

void wavfile_destroy(void *p) {
	WavFile *config = (WavFile *)p;
	free(config->buffers[0]);
	free(config->buffers[1]);
	free(config->buffers);
	free(config);
}

void Java_com_kh_beatbot_manager_PlaybackManager_toggleLooping(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *)track->generator->config;
	wavFile->looping = !wavFile->looping;
}

jboolean Java_com_kh_beatbot_manager_PlaybackManager_isLooping(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *)track->generator->config;
	return wavFile->looping;
}

void Java_com_kh_beatbot_manager_PlaybackManager_setLoopWindow(JNIEnv *env,
		jclass clazz, jint trackNum, jint loopBeginSample, jint loopEndSample) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *)track->generator->config;
	if (wavFile->loopBegin == loopBeginSample && wavFile->loopEnd == loopEndSample)
		return;
	wavFile->loopBegin = loopBeginSample;
	wavFile->loopEnd = loopEndSample;
	if (wavFile->currSample >= wavFile->loopEnd)
		wavFile->currSample = wavFile->loopBegin;
	updateAdsr((AdsrConfig *) track->adsr->config,
			loopEndSample - loopBeginSample);
}

