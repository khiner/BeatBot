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

void printEffects(Levels *levels) {
	EffectNode *cur_ptr = levels->effectHead;
	int count = 0;
	while (cur_ptr != NULL) {
		if (cur_ptr->effect != NULL) {
			__android_log_print(ANDROID_LOG_INFO, "effects", "pos %d occupied",
					count);
		} else {
			__android_log_print(ANDROID_LOG_INFO, "effects", "pos %d empty",
					count);
		}
		cur_ptr = cur_ptr->next;
		count++;
	}
}

EffectNode *findEffectNodeByPosition(Levels *levels, int position) {
	EffectNode *cur_ptr = levels->effectHead;
	int count = 0;
	while (count < position && cur_ptr != NULL) {
		cur_ptr = cur_ptr->next;
		count++;
	}
	return cur_ptr;
}

Effect *createEffect(int effectNum) {
	switch (effectNum) {
	case CHORUS:
		return initEffect(true, chorusconfig_create(), chorusconfig_setParam,
				chorus_process, chorusconfig_destroy);
	case DECIMATE:
		return initEffect(true, decimateconfig_create(),
				decimateconfig_setParam, decimate_process,
				decimateconfig_destroy);
	case DELAY:
		return initEffect(true, delayconfigi_create(), delayconfigi_setParam,
				delayi_process, delayconfigi_destroy);
	case FILTER:
		return initEffect(true, filterconfig_create(), filterconfig_setParam,
				filter_process, filterconfig_destroy);
	case FLANGER:
		return initEffect(true, flangerconfig_create(), flangerconfig_setParam,
				flanger_process, flangerconfig_destroy);
	case REVERB:
		return initEffect(true, reverbconfig_create(), reverbconfig_setParam,
				reverb_process, reverbconfig_destroy);
	case TREMELO:
		return initEffect(true, tremeloconfig_create(), tremeloconfig_setParam,
				tremelo_process, tremeloconfig_destroy);
	}
	return NULL;
}

void setEffect(Levels *levels, int position, Effect *effect) {
	EffectNode *cur_ptr = levels->effectHead;
	int count = 0;
	pthread_mutex_lock(&levels->effectMutex);
	while (count < position && cur_ptr != NULL) {
		cur_ptr = cur_ptr->next;
		count++;
	}
	cur_ptr->effect = effect;
	pthread_mutex_unlock(&levels->effectMutex);
}

void insertEffect(Levels *levels, int position, EffectNode *node) {
	EffectNode *cur_ptr = levels->effectHead;
	EffectNode *prev_ptr = NULL;
	int count = 0;
	pthread_mutex_lock(&levels->effectMutex);
	while (count < position && cur_ptr != NULL) {
		prev_ptr = cur_ptr;
		cur_ptr = cur_ptr->next;
		count++;
	}
	if (prev_ptr != NULL) {
		prev_ptr->next = node;
		node->next = cur_ptr;
	} else {
		node->next = levels->effectHead;
		levels->effectHead = node;
	}
	pthread_mutex_unlock(&levels->effectMutex);
}

EffectNode *removeEffect(Levels *levels, int effectPosition) {
	EffectNode *one_back;
	EffectNode *node = findEffectNodeByPosition(levels, effectPosition);
	pthread_mutex_lock(&levels->effectMutex);
	if (node == levels->effectHead) {
		levels->effectHead = levels->effectHead->next;
	} else {
		one_back = levels->effectHead;
		while (one_back->next != node) {
			one_back = one_back->next;
		}
		one_back->next = node->next;
	}
	pthread_mutex_unlock(&levels->effectMutex);
	return node;
}

/********* JNI METHODS **********/
void Java_com_kh_beatbot_effect_Effect_addEffect(JNIEnv *env, jclass clazz,
		jint trackNum, jint effectNum, jint position) {
	Levels *levels = getLevels(env, clazz, trackNum);
	Effect *effect = createEffect(effectNum);
	setEffect(levels, position, effect);
}

void Java_com_kh_beatbot_effect_Effect_removeEffect(JNIEnv *env, jclass clazz,
		jint trackNum, jint position) {
	Levels *levels = getLevels(env, clazz, trackNum);
	EffectNode *effectNode = findEffectNodeByPosition(levels, position);
	free(effectNode->effect->config);
	free(effectNode->effect);
	effectNode->effect = NULL;
}

void Java_com_kh_beatbot_effect_Effect_setEffectPosition(JNIEnv *env,
		jclass clazz, jint trackNum, jint oldPosition, jint newPosition) {
	// find node currently at the desired position
	Levels *levels = getLevels(env, clazz, trackNum);
	EffectNode *node = removeEffect(levels, oldPosition);
	insertEffect(levels, newPosition, node);
}

void Java_com_kh_beatbot_effect_Effect_setEffectOn(JNIEnv *env, jclass clazz,
		jint trackNum, jint effectPosition, jboolean on) {
	Levels *levels = getLevels(env, clazz, trackNum);
	EffectNode *effectNode = findEffectNodeByPosition(levels, effectPosition);
	if (effectNode != NULL) {
		effectNode->effect->on = on;
	}
}

void Java_com_kh_beatbot_effect_Effect_setEffectParam(JNIEnv *env, jclass clazz,
		jint trackNum, jint effectPosition, jint paramNum, jfloat paramValue) {
	if (effectPosition == -1) { // -1 == ADSR
		adsrconfig_setParam(((WavFile *)getTrack(env, clazz, trackNum)->generator->config)->adsr, (float) paramNum, paramValue);
		return;
	}
	Levels *levels = getLevels(env, clazz, trackNum);
	EffectNode *effectNode = findEffectNodeByPosition(levels, effectPosition);
	if (effectNode != NULL) {
		effectNode->effect->set(effectNode->effect->config, (float) paramNum,
				paramValue);
	}
}
