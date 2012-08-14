#include "wavfile.h"

static inline short charsToShort(unsigned char first, unsigned char second) {
	return (first << 8) | second;
}

WavFile *wavfile_create(AAsset *asset) {
	WavFile *wavFile = (WavFile *)malloc(sizeof(WavFile));
	wavFile->totalSamples = AAsset_getLength(asset) / 2 - 22;
	wavFile->totalSamples /= 2; // 1 sample has one short for each channel (2 channels)
	wavFile->buffers = (float **) malloc(2 * sizeof(float *));
	wavFile->buffers[0] = (float *) calloc(wavFile->totalSamples, sizeof(float));
	wavFile->buffers[1] = (float *) calloc(wavFile->totalSamples, sizeof(float));
	wavFile->looping = wavFile->reverse = false;
	wavFile->loopBegin = 0;
	wavFile->loopEnd = wavFile->totalSamples;
	wavFile->currSample = 0;

	unsigned char *charBuf = (unsigned char *) AAsset_getBuffer(asset);
	int i;
	for (i = 0; i < wavFile->totalSamples; i++) {
		// first 44 bytes of a wav file are header
		wavFile->buffers[0][i] = charsToShort(charBuf[i * 4 + 1 + 44],
				charBuf[i * 4 + 44]) * CONVMYFLT;
		wavFile->buffers[1][i] = charsToShort(charBuf[i * 4 + 3 + 44],
				charBuf[i * 4 + 2 + 44]) * CONVMYFLT;
	}
	free(charBuf);
	AAsset_close(asset);
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
	updateAdsr((AdsrConfig *) track->effects[ADSR_ID].config,
			loopEndSample - loopBeginSample);
}

