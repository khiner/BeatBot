#include "../all.h"

Effect *initEffect(void *config, void (*set), void (*process),
                   void (*destroy)) {
    Effect *effect = malloc(sizeof(Effect));
    effect->on = false;
    effect->config = config;
    effect->set = set;
    effect->process = process;
    effect->destroy = destroy;
    return effect;
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

Effect *createEffect(int effectId) {
    switch (effectId) {
        case CHORUS:
            return initEffect(chorusconfig_create(), chorusconfig_setParam,
                              chorus_process, chorusconfig_destroy);
        case DECIMATE:
            return initEffect(decimateconfig_create(),
                              decimateconfig_setParam, decimate_process,
                              decimateconfig_destroy);
        case DELAY:
            return initEffect(delayconfigi_create(), delayconfigi_setParam,
                              delayi_process, delayconfigi_destroy);
        case FILTER:
            return initEffect(filterconfig_create(), filterconfig_setParam,
                              filter_process, filterconfig_destroy);
        case FLANGER:
            return initEffect(flangerconfig_create(), flangerconfig_setParam,
                              flanger_process, flangerconfig_destroy);
        case REVERB:
            return initEffect(reverbconfig_create(), reverbconfig_setParam,
                              reverb_process, reverbconfig_destroy);
        case TREMELO:
            return initEffect(tremeloconfig_create(), tremeloconfig_setParam,
                              tremelo_process, tremeloconfig_destroy);
    }
    return NULL;
}

void insertEffect(Levels *levels, int position, EffectNode *node) {
    EffectNode *cur_ptr = levels->effectHead;
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
        node->next = levels->effectHead;
        levels->effectHead = node;
    }
}

EffectNode *removeEffect(Levels *levels, int effectPosition) {
    EffectNode *one_back;
    EffectNode *node = findEffectNodeByPosition(levels, effectPosition);
    if (node == levels->effectHead) {
        levels->effectHead = levels->effectHead->next;
    } else {
        one_back = levels->effectHead;
        while (one_back->next != node) {
            one_back = one_back->next;
        }
        one_back->next = node->next;
    }
    return node;
}

/********* JNI METHODS **********/
void Java_com_odang_beatbot_effect_Effect_addEffect(JNIEnv *env, jclass clazz,
                                                    jint trackId, jint effectId, jint position) {
    Levels *levels = getLevels(trackId);
    EffectNode *cur_ptr = findEffectNodeByPosition(levels, position);
    cur_ptr->effect = createEffect(effectId);
}

void Java_com_odang_beatbot_effect_Effect_removeEffect(JNIEnv *env, jclass clazz,
                                                       jint trackId, jint position) {
    Levels *levels = getLevels(trackId);
    EffectNode *effectNode = findEffectNodeByPosition(levels, position);
    pthread_mutex_lock(&levels->effectMutex);
    effectNode->effect->destroy(effectNode->effect->config);
    free(effectNode->effect);
    effectNode->effect = NULL;
    pthread_mutex_unlock(&levels->effectMutex);
}

void Java_com_odang_beatbot_effect_Effect_setEffectPosition(JNIEnv *env,
                                                            jclass clazz, jint trackId,
                                                            jint oldPosition, jint newPosition) {
    // find node currently at the desired position
    Levels *levels = getLevels(trackId);
    pthread_mutex_lock(&levels->effectMutex);
    EffectNode *node = removeEffect(levels, oldPosition);
    insertEffect(levels, newPosition, node);
    pthread_mutex_unlock(&levels->effectMutex);
}

void Java_com_odang_beatbot_effect_Effect_setEffectOn(JNIEnv *env, jclass clazz,
                                                      jint trackId, jint effectPosition,
                                                      jboolean on) {
    Levels *levels = getLevels(trackId);
    EffectNode *effectNode = findEffectNodeByPosition(levels, effectPosition);
    if (effectNode != NULL) {
        effectNode->effect->on = on;
    }
}

void Java_com_odang_beatbot_effect_Effect_setEffectParam(JNIEnv *env, jclass clazz,
                                                         jint trackId, jint effectPosition,
                                                         jint paramNum, jfloat paramValue) {
    if (effectPosition == -1) { // -1 == ADSR
        Track *track = getTrack(trackId);
        if (track->generator == NULL) {
            return;
        }
        adsrconfig_setParam(((FileGen *) track->generator->config)->adsr, (float) paramNum,
                            paramValue);
        return;
    }
    Levels *levels = getLevels(trackId);
    EffectNode *effectNode = findEffectNodeByPosition(levels, effectPosition);
    if (effectNode != NULL) {
        effectNode->effect->set(effectNode->effect->config, (float) paramNum,
                                paramValue);
    }
}
