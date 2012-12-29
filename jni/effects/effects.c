#include "../all.h"

Effect *initEffect(bool on, void *config, void (*set), void (*process),
		void (*destroy)) {
	Effect *effect = malloc(sizeof(Effect));
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

void printEffects(Track *track) {
	EffectNode *cur_ptr = track->effectHead;
	int count = 0;
	while (cur_ptr != NULL) {
		if (cur_ptr->effect != NULL) {
			__android_log_print(ANDROID_LOG_INFO, "effects", "pos %d occupied", count);
		} else {
			__android_log_print(ANDROID_LOG_INFO, "effects", "pos %d empty", count);
		}
		cur_ptr = cur_ptr->next;
		count++;
	}
}

EffectNode *findEffectNodeByPosition(int trackNum, int position) {
	Track *track = getTrack(NULL, NULL, trackNum);
	EffectNode *cur_ptr = track->effectHead;
	int count = 0;
	while (count < position && cur_ptr != NULL) {
		cur_ptr = cur_ptr->next;
		count++;
	}
	return cur_ptr;
}

EffectNode *findPrevNode(int trackNum, EffectNode *effectNode) {
	Track *track = getTrack(NULL, NULL, trackNum);
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

Effect *createEffect(int effectNum) {
	switch (effectNum) {
	case CHORUS:
		return initEffect(true, chorusconfig_create(),
				chorusconfig_setParam, chorus_process, chorusconfig_destroy);
	case DECIMATE:
		return initEffect(true, decimateconfig_create(),
				decimateconfig_setParam, decimate_process,
				decimateconfig_destroy);
	case DELAY:
		return initEffect(true, delayconfigi_create(),
				delayconfigi_setParam, delayi_process, delayconfigi_destroy);
	case FILTER:
		return initEffect(true, filterconfig_create(),
				filterconfig_setParam, filter_process, filterconfig_destroy);
	case FLANGER:
		return initEffect(true, flangerconfig_create(),
				flangerconfig_setParam, flanger_process, flangerconfig_destroy);
	case REVERB:
		return initEffect(true, reverbconfig_create(),
				reverbconfig_setParam, reverb_process, reverbconfig_destroy);
	case TREMELO:
		return initEffect(true, tremeloconfig_create(),
				tremeloconfig_setParam, tremelo_process, tremeloconfig_destroy);
	}
	return NULL;
}

void setEffect(Track *track, int position, Effect *effect) {
	EffectNode *cur_ptr = track->effectHead;
	int count = 0;
	pthread_mutex_lock(&track->effectMutex);
	while (count < position && cur_ptr != NULL) {
		cur_ptr = cur_ptr->next;
		count++;
	}
	cur_ptr->effect = effect;
	pthread_mutex_unlock(&track->effectMutex);
}

void insertEffect(Track *track, int position, EffectNode *node) {
	EffectNode *cur_ptr = track->effectHead;
	EffectNode *prev_ptr = NULL;
	int count = 0;
	pthread_mutex_lock(&track->effectMutex);
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
	pthread_mutex_unlock(&track->effectMutex);
}

EffectNode *removeEffect(int trackNum, int effectPosition) {
	EffectNode *one_back;
	Track *track = getTrack(NULL, NULL, trackNum);
	EffectNode *node = findEffectNodeByPosition(trackNum, effectPosition);
	pthread_mutex_lock(&track->effectMutex);
	if (node == track->effectHead) {
		track->effectHead = track->effectHead->next;
	} else {
		one_back = track->effectHead;
		while (one_back->next != node) {
			one_back = one_back->next;
		}
		one_back->next = node->next;
	}
	pthread_mutex_unlock(&track->effectMutex);
	return node;
}

/********* JNI METHODS **********/
void Java_com_kh_beatbot_effect_Effect_addEffect(JNIEnv *env, jclass clazz,
		jint trackNum, jint effectNum, jint position) {
	Track *track = getTrack(env, clazz, trackNum);
	Effect *effect = createEffect(effectNum);
	setEffect(track, position, effect);
	printEffects(track);
}

void Java_com_kh_beatbot_effect_Effect_removeEffect(JNIEnv *env, jclass clazz,
		jint trackNum, jint position) {
	EffectNode *effectNode = findEffectNodeByPosition(trackNum, position);
	free(effectNode->effect->config);
	free(effectNode->effect);
	effectNode->effect = NULL;
	__android_log_print(ANDROID_LOG_INFO, "effects", "removing pos from %d", position);
	printEffects(getTrack(env, clazz, trackNum));
}

void Java_com_kh_beatbot_effect_Effect_setEffectPosition(JNIEnv *env,
		jclass clazz, jint trackNum, jint oldPosition, jint newPosition) {
	// find node currently at the desired position
	Track *track = getTrack(env, clazz, trackNum);
	EffectNode *node = removeEffect(trackNum, oldPosition);
	insertEffect(track, newPosition, node);
	__android_log_print(ANDROID_LOG_INFO, "effects", "setting pos from %d to %d", oldPosition, newPosition);
	printEffects(track);
}

void Java_com_kh_beatbot_effect_Effect_setEffectOn(JNIEnv *env, jclass clazz,
		jint trackNum, jint effectPosition, jboolean on) {
	EffectNode *effectNode = findEffectNodeByPosition(trackNum, effectPosition);
	if (effectNode != NULL) {
		effectNode->effect->on = on;
	}
}

void Java_com_kh_beatbot_effect_Effect_setEffectParam(JNIEnv *env, jclass clazz,
		jint trackNum, jint effectPosition, jint paramNum, jfloat paramLevel) {
	EffectNode *effectNode = findEffectNodeByPosition(trackNum, effectPosition);
	if (effectNode != NULL) {
		effectNode->effect->set(effectNode->effect->config, (float) paramNum, paramLevel);
	}
}
