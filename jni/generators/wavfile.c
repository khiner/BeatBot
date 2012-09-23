#include "wavfile.h"
#include "../track.h"

static inline float bytesToFloat(unsigned char firstByte,
		unsigned char secondByte) {
	// convert two bytes to one short (little endian)
	short s = (secondByte << 8) | firstByte;
	// convert to range from -1 to (just below) 1
	return s / 32768.0;
}

/**
 * Populate the wavFile float buffers with raw PCM Wav bytes.
 * Works with mono/stereo (for mono, duplicate each sample to the left/right channels)
 * Method for detecting/adapting to stereo/mono adapted from
 * http://stackoverflow.com/questions/8754111/how-to-read-the-data-in-a-wav-file-to-an-array
 */
void wavfile_setBytes(WavFile *wavFile, char *wav, int length) {
	// Determine if mono or stereo
	int channels = wav[22]; // Forget byte 23 as 99.999% of WAVs are 1 or 2 channels

	// Get past all the other sub chunks to get to the data subchunk:
	int pos = 12; // First Subchunk ID from 12 to 16

	// Keep iterating until we find the data chunk (i.e. 64 61 74 61 ...... (i.e. 100 97 116 97 in decimal))
	while (wav[pos] != 100 || wav[pos + 1] != 97 || wav[pos + 2] != 116
			|| wav[pos + 3] != 97) {
		pos += 4;
		int chunkSize = wav[pos] + wav[pos + 1] * 256 + wav[pos + 2] * 65536
				+ wav[pos + 3] * 16777216;
		pos += 4 + chunkSize;
	}
	pos += 8;

	if (channels == 1) {
		length -= 32; // don't know why the end of mono sample is garbage
	}

	// pos is now positioned to start of actual sound data.
	wavFile->totalSamples = (length - pos) / 2; // 2 bytes per sample (16 bit sound mono)
	if (channels == 2)
		wavFile->totalSamples /= 2; // 4 bytes per sample (16 bit stereo)

	wavFile->buffers = (float **) malloc(2 * sizeof(float *));
	// Allocate memory (right will be null if only mono sound)
	wavFile->buffers[0] = (float *) calloc(wavFile->totalSamples,
			sizeof(float));
	wavFile->buffers[1] = (float *) calloc(wavFile->totalSamples,
			sizeof(float));

	// write to wavFile float buffers
	int i = 0;
	while (pos < length) {
		wavFile->buffers[0][i] = bytesToFloat(wav[pos], wav[pos + 1]);
		pos += 2;
		if (channels == 2) {
			wavFile->buffers[1][i] = bytesToFloat(wav[pos], wav[pos + 1]);
			pos += 2;
		} else {
			wavFile->buffers[1][i] = wavFile->buffers[0][i];
		}
		i++;
	}
	// init loop / currSample position data
	wavFile->loopBegin = 0;
	wavFile->loopEnd = wavFile->totalSamples - 2;
	wavFile->currSample = 0;

	//free(bytes); //taken care of by Release JNI method?
}

WavFile *wavfile_create(char *bytes, int length) {
	WavFile *wavFile = (WavFile *) malloc(sizeof(WavFile));
	wavfile_setBytes(wavFile, bytes, length);
	wavFile->looping = wavFile->reverse = false;
	return wavFile;
}

void wavfile_reset(WavFile *config) {
	if (config->reverse)
		config->currSample = config->loopEnd;
	else
		config->currSample = config->loopBegin;
}

void freeBuffers(WavFile *config) {
	free(config->buffers[0]);
	free(config->buffers[1]);
	free(config->buffers);
}

void wavfile_destroy(void *p) {
	WavFile *config = (WavFile *) p;
	freeBuffers(config);
	free(config);
}

void Java_com_kh_beatbot_manager_PlaybackManager_toggleLooping(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *) track->generator->config;
	wavFile->looping = !wavFile->looping;
}

jboolean Java_com_kh_beatbot_manager_PlaybackManager_isLooping(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *) track->generator->config;
	return wavFile->looping;
}

void Java_com_kh_beatbot_manager_PlaybackManager_setLoopWindow(JNIEnv *env,
		jclass clazz, jint trackNum, jint loopBeginSample, jint loopEndSample) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *) track->generator->config;
	if (wavFile->loopBegin == loopBeginSample
			&& wavFile->loopEnd == loopEndSample)
		return;
	wavFile->loopBegin = loopBeginSample;
	wavFile->loopEnd = loopEndSample;
	if (wavFile->currSample >= wavFile->loopEnd)
		wavFile->currSample = wavFile->loopBegin;
	updateAdsr((AdsrConfig *) track->adsr->config,
			loopEndSample - loopBeginSample);
}

