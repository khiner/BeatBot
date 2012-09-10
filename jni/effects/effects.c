#include "effects.h"
#include "chorus.h"
#include "decimate.h"
#include "delay.h"
#include "filter.h"
#include "flanger.h"
#include "reverb.h"
#include "tremelo.h"

Effect *initEffect(int id, bool on, void *config, void (*set), void (*process),
		void (*destroy)) {
	Effect *effect = malloc(sizeof(Effect));
	effect->id = id;
	effect->on = on;
	effect->config = config;
	effect->set = set;
	effect->process = process;
	effect->destroy = destroy;
	return effect;
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

EffectNode *findEffectNodeById(int trackNum, int id) {
	Track *track = &(tracks[trackNum]);
	EffectNode *effectNode = track->effectHead;
	while (effectNode != NULL) {
		if (effectNode->effect != NULL && effectNode->effect->id == id) {
			return effectNode;
		}
		effectNode = effectNode->next;
	}
	return NULL;
}

EffectNode *findEffectNodeByPosition(int trackNum, int position) {
	Track *track = &(tracks[trackNum]);
	EffectNode *cur_ptr = track->effectHead;
	int count = 0;
	while (count < position && cur_ptr != NULL) {
		cur_ptr = cur_ptr->next;
		count++;
	}
	return cur_ptr;
}

EffectNode *findPrevNode(int trackNum, EffectNode *effectNode) {
	Track *track = &(tracks[trackNum]);
	EffectNode *cur_ptr = track->effectHead;
	if (cur_ptr == effectNode) {
		return NULL; // effectNode is head, no prev node
	}
	while (cur_ptr->next != NULL) {
		if (cur_ptr->next == effectNode) {
			return cur_ptr;
		}
		cur_ptr = cur_ptr->next;
	}
	return NULL; // only happes when effectNode is not in the track's effect list
}

Effect *createEffect(int effectNum, int effectId) {
	switch (effectNum) {
	case CHORUS:
		return initEffect(effectId, true, chorusconfig_create(),
				chorusconfig_setParam, chorus_process, chorusconfig_destroy);
	case DECIMATE:
		return initEffect(effectId, true, decimateconfig_create(),
				decimateconfig_setParam, decimate_process,
				decimateconfig_destroy);
	case DELAY:
		return initEffect(effectId, true, delayconfigi_create(),
				delayconfigi_setParam, delayi_process, delayconfigi_destroy);
	case FILTER:
		return initEffect(effectId, true, filterconfig_create(),
				filterconfig_setParam, filter_process, filterconfig_destroy);
	case FLANGER:
		return initEffect(effectId, true, flangerconfig_create(),
				flangerconfig_setParam, flanger_process, flangerconfig_destroy);
	case REVERB:
		return initEffect(effectId, true, reverbconfig_create(),
				reverbconfig_setParam, reverb_process, reverbconfig_destroy);
	case TREMELO:
		return initEffect(effectId, true, tremeloconfig_create(),
				tremeloconfig_setParam, tremelo_process, tremeloconfig_destroy);
	}
	return NULL;
}

void printEffects(EffectNode *head) {
	__android_log_print(ANDROID_LOG_ERROR, "effects", "Elements:");
	EffectNode *cur_ptr = head;
	while (cur_ptr != NULL) {
		if (cur_ptr->effect != NULL)
			__android_log_print(ANDROID_LOG_ERROR, "effect id = ", "%d, ",
					cur_ptr->effect->id);
		else
			__android_log_print(ANDROID_LOG_ERROR, "effect", "blank");
		cur_ptr = cur_ptr->next;
	}
}

void addEffect(Track *track, Effect *effect) {
	EffectNode *new = (EffectNode *) malloc(sizeof(EffectNode));
	new->effect = effect;
	new->next = NULL;
	// check for first insertion
	if (track->effectHead == NULL) {
		track->effectHead = new;
	} else {
		// insert as last effect
		EffectNode *cur_ptr = track->effectHead;
		while (cur_ptr->next != NULL) {
			cur_ptr = cur_ptr->next;
		}
		cur_ptr->next = new;
	}
}

void setEffect(Track *track, int position, Effect *effect) {
	EffectNode *cur_ptr = track->effectHead;
	int count = 0;
	while (count < position && cur_ptr != NULL) {
		cur_ptr = cur_ptr->next;
		count++;
	}
	cur_ptr->effect = effect;
}

void insertEffect(Track *track, int position, EffectNode *node) {
	EffectNode *cur_ptr = track->effectHead;
	EffectNode *prev_ptr = NULL;
	int count = 0;
	while (count < position && cur_ptr != NULL) {
		prev_ptr = cur_ptr;
		cur_ptr = cur_ptr->next;
		count++;
	}
	if (prev_ptr != NULL) {
		prev_ptr->next = node;
		node->next = cur_ptr;
	} else {
		node->next = track->effectHead;
		track->effectHead = node;
	}
}

EffectNode *removeEffect(int trackNum, int effectId) {
	EffectNode *one_back;
	Track *track = getTrack(NULL, NULL, trackNum);
	EffectNode *node = findEffectNodeById(trackNum, effectId);
	if (node == track->effectHead) {
		track->effectHead = track->effectHead->next;
	} else {
		one_back = track->effectHead;
		while (one_back->next != node) {
			one_back = one_back->next;
		}
		one_back->next = node->next;
	}
	return node;
}

/********* JNI METHODS **********/
jfloatArray makejFloatArray(JNIEnv * env, float floatAry[], int size) {
	jfloatArray result = (*env)->NewFloatArray(env, size);
	(*env)->SetFloatArrayRegion(env, result, 0, size, floatAry);
	return result;
}

void Java_com_kh_beatbot_effect_Effect_addEffect(JNIEnv *env, jclass clazz,
		jint trackNum, jint effectNum, jint effectId, jint position) {
	Track *track = getTrack(env, clazz, trackNum);
	Effect *effect = createEffect(effectNum, effectId);
	setEffect(track, position, effect);
	printEffects(track->effectHead);
}

void Java_com_kh_beatbot_effect_Effect_setEffectPosition(JNIEnv *env,
		jclass clazz, jint trackNum, jint effectId, jint position) {
	// find node currently at the desired position
	Track *track = getTrack(env, clazz, trackNum);
	EffectNode *node = removeEffect(trackNum, effectId);
	insertEffect(track, position, node);
	printEffects(getTrack(env, clazz, trackNum)->effectHead);
}

void Java_com_kh_beatbot_effect_Effect_setEffectOn(JNIEnv *env, jclass clazz,
		jint trackNum, jint effectId, jboolean on) {
	EffectNode *effectNode = findEffectNodeById(trackNum, effectId);
	if (effectNode != NULL) {
		effectNode->effect->on = on;
	}
}

void Java_com_kh_beatbot_effect_Effect_setEffectParam(JNIEnv *env, jclass clazz,
		jint trackNum, jint effectId, jint paramNum, jfloat paramLevel) {
	EffectNode *effectNode = findEffectNodeById(trackNum, effectId);
	if (effectNode != NULL) {
		effectNode->effect->set(effectNode->effect->config, (float) paramNum, paramLevel);
	}
}

jfloatArray Java_com_kh_beatbot_SampleEditActivity_getSamples(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *) track->generator->config;
	return makejFloatArray(env, wavFile->buffers[0], wavFile->totalSamples);
}

void Java_com_kh_beatbot_SampleEditActivity_setReverse(JNIEnv *env,
		jclass clazz, jint trackNum, jboolean reverse) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *) track->generator->config;
	wavFile->reverse = reverse;
	// if the track is not looping, the wavFile generator will not loop to the beginning/end
	// after enaabling/disabling reverse
	if (reverse && wavFile->currSample == wavFile->loopBegin)
		wavFile->currSample = wavFile->loopEnd;
	else if (!reverse && wavFile->currSample == wavFile->loopEnd)
		wavFile->currSample = wavFile->loopBegin;
}

jfloatArray Java_com_kh_beatbot_SampleEditActivity_normalize(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *) track->generator->config;
	normalize(wavFile->buffers[0], wavFile->totalSamples);
	normalize(wavFile->buffers[1], wavFile->totalSamples);
	return makejFloatArray(env, wavFile->buffers[0], wavFile->totalSamples);
}
