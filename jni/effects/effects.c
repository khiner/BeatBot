#include "effects.h"

void initEffect(Effect *effect, bool on, void *config,
		void (*set), void (*process), void (*destroy)) {
	effect->on = on;
	effect->config = config;
	effect->set = set;
	effect->process = process;
	effect->destroy = destroy;
}

void swap(float *a, float *b) {
	float tmp;
	tmp = *a;
	(*a) = (*b);
	(*b) = tmp;
}

void reverse(float buffer[], int begin, int end) {
	int i, j;
	//swap 1st with last, then 2nd with last-1, etc.  Till we reach the middle of the string.
	for (i = begin, j = end - 1; i < j; i++, j--) {
		swap(&buffer[i], &buffer[j]);
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

jfloatArray makejFloatArray(JNIEnv * env, float floatAry[], int size) {
	jfloatArray result = (*env)->NewFloatArray(env, size);
	(*env)->SetFloatArrayRegion(env, result, 0, size, floatAry);
	return result;
}

/********* JNI METHODS **********/

jfloatArray Java_com_kh_beatbot_SampleEditActivity_getSamples(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	return makejFloatArray(env, track->buffers[0], track->totalSamples);
}

jfloatArray Java_com_kh_beatbot_SampleEditActivity_reverse(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	reverse(track->buffers[0], track->loopBegin, track->loopEnd);
	reverse(track->buffers[1], track->loopBegin, track->loopEnd);
	return makejFloatArray(env, track->buffers[0], track->totalSamples);
}

jfloatArray Java_com_kh_beatbot_SampleEditActivity_normalize(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	normalize(track->buffers[0], track->totalSamples);
	normalize(track->buffers[1], track->totalSamples);
	return makejFloatArray(env, track->buffers[0], track->totalSamples);
}
