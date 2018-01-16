#include "all.h"
#include "libsndfile/sndfile.h"
#include "jni_load.h"

jfloatArray makejFloatArray(JNIEnv *env, float floatAry[], int size) {
    jfloatArray result = (*env)->NewFloatArray(env, size);
    (*env)->SetFloatArrayRegion(env, result, 0, size, floatAry);
    return result;
}

void printTracks() {
    __android_log_print(ANDROID_LOG_ERROR, "tracks", "Elements:");
    TrackNode *cur_ptr = trackHead;
    while (cur_ptr != NULL) {
        if (cur_ptr->track != NULL)
            __android_log_print(ANDROID_LOG_ERROR, "track num = ", "%d, ",
                                cur_ptr->track->num);
        else
            __android_log_print(ANDROID_LOG_ERROR, "track", "blank");
        cur_ptr = cur_ptr->next;
    }
}

void addEffect(Levels *levels, Effect *effect) {
    EffectNode *new = (EffectNode *) malloc(sizeof(EffectNode));
    new->effect = effect;
    new->next = NULL;

    pthread_mutex_lock(&levels->effectMutex);
    // check for first insertion
    if (levels->effectHead == NULL) {
        levels->effectHead = new;
    } else {
        // insert as last effect
        EffectNode *cur_ptr = levels->effectHead;
        while (cur_ptr->next != NULL) {
            cur_ptr = cur_ptr->next;
        }
        cur_ptr->next = new;
    }
    pthread_mutex_unlock(&levels->effectMutex);
}

TrackNode *getTrackNode(int trackId) {
    TrackNode *trackNode = trackHead;
    while (trackNode != NULL) {
        if (trackNode->track != NULL && trackNode->track->num == trackId) {
            return trackNode;
        }
        trackNode = trackNode->next;
    }
    return NULL;
}

Track *getTrack(int trackId) {
    TrackNode *trackNode = getTrackNode(trackId);
    return trackNode->track;
}

Levels *getLevels(int trackId) {
    if (trackId == MASTER_TRACK_ID) {
        return masterLevels;
    }
    Track *track = getTrack(trackId);
    return track->levels;
}

void createTrack(Track *track) {
    TrackNode *new = (TrackNode *) malloc(sizeof(TrackNode));
    new->track = track;
    new->next = NULL;
    // check for first insertion
    if (trackHead == NULL) {
        trackHead = new;
    } else {
        // insert as last track
        TrackNode *cur_ptr = trackHead;
        while (cur_ptr->next != NULL) {
            cur_ptr = cur_ptr->next;
        }
        cur_ptr->next = new;
    }
}

void destroyTrack(Track *track) {
    free(track->currBufferFloat[0]);
    free(track->currBufferFloat[1]);
    free(track->currBufferFloat);
    freeEffects(track->levels);
    if (track->generator != NULL)
        track->generator->destroy(track->generator->config);
}

void removeTrack(TrackNode *trackNode) {
    TrackNode *one_back;
    TrackNode *node = trackNode;
    if (node == trackHead) {
        trackHead = trackHead->next;
    } else {
        one_back = trackHead;
        while (one_back->next != node) {
            one_back = one_back->next;
        }
        one_back->next = node->next;
    }
    destroyTrack(trackNode->track);
}

void freeEffects(Levels *levels) {
    EffectNode *cur_ptr = levels->effectHead;
    while (cur_ptr != NULL) {
        if (cur_ptr->effect != NULL && cur_ptr->effect->destroy != NULL) {
            cur_ptr->effect->destroy(cur_ptr->effect->config);
        }
        EffectNode *prev_ptr = cur_ptr;
        cur_ptr = cur_ptr->next;
        free(prev_ptr); // free the entire Node
    }
}

void destroyTracks() {    // destroy all tracks
    TrackNode *cur_ptr = trackHead;
    while (cur_ptr != NULL) {
        destroyTrack(cur_ptr->track);
        TrackNode *prev_ptr = cur_ptr;
        cur_ptr = cur_ptr->next;
        free(prev_ptr); // free the entire Node
    }
    freeEffects(masterLevels);
}

void updateChannelScaleValues(Levels *levels, MidiEvent *midiEvent) {
    float balance =
            midiEvent != NULL ?
            (2 * levels->pan * midiEvent->pan) : levels->pan;
    // 0db at center, linear stereo balance control
    // http://www.kvraudio.com/forum/viewtopic.php?t=148865
    if (balance < 0.5f) {
        levels->scaleChannels[0] = 1;
        levels->scaleChannels[1] = balance * 2;
    } else {
        levels->scaleChannels[0] = (1 - balance) * 2;
        levels->scaleChannels[1] = 1;
    }

    int channel;
    for (channel = 0; channel < 2; channel++) {
        float volume =
                midiEvent != NULL ?
                levels->volume * midiEvent->volume : levels->volume;
        levels->scaleChannels[channel] *= volume;
    }
}

void setPitch(Track *track, MidiEvent *midiEvent) {
    if (NULL != track->generator) {
        float totalPitchSteps = masterLevels->pitchSteps + track->levels->pitchSteps;
        if (midiEvent != NULL)
            totalPitchSteps += midiEvent->pitchSteps;
        float sampleRate = transposeStepsToScaleValue(totalPitchSteps);
        ((FileGen *) track->generator->config)->sampleRate = sampleRate;
    }
}

void updatePitch(int trackId) {
    if (trackId == MASTER_TRACK_ID) {
        // master track - update all pitches
        TrackNode *cur_ptr = trackHead;
        while (cur_ptr != NULL) {
            updatePitch(cur_ptr->track->num);
            cur_ptr = cur_ptr->next;
        }
    } else {
        Track *track = getTrack(trackId);
        setPitch(track, track->nextEvent);
    }
}

Levels *initLevels() {
    Levels *levels = malloc(sizeof(Levels));
    pthread_mutex_init(&levels->effectMutex, NULL);
    levels->effectHead = NULL;
    levels->volume = dbToLinear(0);
    levels->pan = panToScaleValue(0);
    levels->pitchSteps = 0;

    updateChannelScaleValues(levels, NULL);

    int effectNum;
    for (effectNum = 0; effectNum < MAX_EFFECTS_PER_TRACK; effectNum++) {
        addEffect(levels, NULL);
    }
    return levels;
}

Track *initTrack(int trackId) {
    Track *track = malloc(sizeof(Track));
    track->num = trackId;
    track->generator = NULL;
    track->levels = initLevels();
    track->nextStartSample = track->nextStopSample = -1;
    track->currBufferFloat = (float **) malloc(2 * sizeof(float *));
    track->currBufferFloat[0] = (float *) calloc(BUFF_SIZE_FRAMES,
                                                 ONE_FLOAT_SIZE);
    track->currBufferFloat[1] = (float *) calloc(BUFF_SIZE_FRAMES,
                                                 ONE_FLOAT_SIZE);
    track->mute = track->solo = false;
    track->shouldSound = true;
    track->nextEvent = malloc(sizeof(MidiEvent));
    track->nextEvent->volume = dbToLinear(0);
    track->nextEvent->pan = panToScaleValue(0);
    track->nextEvent->pitchSteps = 0;
    return track;
}

void setSample(Track *track, const char *sampleName) {
    FileGen *fileGen = (FileGen *) track->generator->config;
    filegen_setSampleFile(fileGen, sampleName);
}

void fillTempSample(Track *track) {
    if (track->generator != NULL) {
        filegen_tick((FileGen *) track->generator->config, track->tempSample);
    } else {
        track->tempSample[0] = track->tempSample[1] = 0;
    }
}

void soundTrack(Track *track) {
    if (track->generator != NULL) {
        updatePitch(track->num);
        updateChannelScaleValues(track->levels, track->nextEvent);
        filegen_start((FileGen *) track->generator->config);
    }
}

void stopSoundingTrack(Track *track) {
    if (track->generator != NULL) {
        filegen_reset((FileGen *) track->generator->config);
    }
}

void stopTrack(Track *track) {
    // update next track
    updateNextNote(track);
    stopSoundingTrack(track);
}

void playTrack(Track *track) {
    stopSoundingTrack(track);
    soundTrack(track);
}

void previewTrack(Track *track) {
    updatePitch(track->num);
    updateChannelScaleValues(track->levels, previewEvent);
    if (track->generator != NULL) {
        filegen_start((FileGen *) track->generator->config);
    }
}

void stopPreviewingTrack(Track *track) {
    stopSoundingTrack(track);
}

int getSoloingTrackNum() {
    TrackNode *cur_ptr = trackHead;
    while (cur_ptr != NULL) {
        if (cur_ptr->track->solo) {
            return cur_ptr->track->num;
        }
        cur_ptr = cur_ptr->next;
    }
    return MASTER_TRACK_ID;
}

void Java_com_odang_beatbot_track_Track_muteTrack(JNIEnv *env, jclass clazz,
                                                  jint trackId, jboolean mute) {
    Track *track = getTrack(trackId);
    if (mute) {
        track->shouldSound = false;
    } else {
        int soloingTrackNum = getSoloingTrackNum();
        if (soloingTrackNum == MASTER_TRACK_ID || soloingTrackNum == trackId)
            track->shouldSound = true;
    }
    track->mute = mute;
}

void Java_com_odang_beatbot_track_Track_soloTrack(JNIEnv *env, jclass clazz,
                                                  jint trackId, jboolean solo) {
    Track *track = getTrack(trackId);
    track->solo = solo;
    if (solo) {
        if (!track->mute) {
            track->shouldSound = true;
        }
        TrackNode *cur_ptr = trackHead;
        while (cur_ptr != NULL) {
            if (cur_ptr->track->num != trackId) {
                cur_ptr->track->shouldSound = false;
                cur_ptr->track->solo = false;
            }
            cur_ptr = cur_ptr->next;
        }
    } else {
        TrackNode *cur_ptr = trackHead;
        while (cur_ptr != NULL) {
            if (!cur_ptr->track->mute) {
                cur_ptr->track->shouldSound = true;
            }
            cur_ptr = cur_ptr->next;
        }
    }
}

void setNextNoteInfo(Track *track, jlong onTick, jlong offTick, jbyte volume,
                     jbyte pan, jbyte pitchSteps) {
    track->nextStartSample = (long) (onTick * samplesPerTick);
    track->nextStopSample = (long) (offTick * samplesPerTick);
    track->nextEvent->volume = byteToLinear((unsigned char) volume);
    track->nextEvent->pan = byteToLinear((unsigned char) pan);
    track->nextEvent->pitchSteps = (float) pitchSteps - HALF_BYTE_VALUE;
}

void setNextNote(Track *track, jobject obj) {
    JNIEnv *env = getJniEnv();
    if (obj == NULL) {
        track->nextStartSample = track->nextStopSample = -1;
        return;
    }
    jclass cls = (*env)->GetObjectClass(env, obj);

    long onTick = (*env)->CallLongMethod(env, obj,
                                         (*env)->GetMethodID(env, cls, "getOnTick", "()J"));
    long offTick = (*env)->CallLongMethod(env, obj,
                                          (*env)->GetMethodID(env, cls, "getOffTick", "()J"));
    jbyte volume = (*env)->CallByteMethod(env, obj,
                                          (*env)->GetMethodID(env, cls, "getVelocity", "()B"));
    jbyte pan = (*env)->CallByteMethod(env, obj,
                                       (*env)->GetMethodID(env, cls, "getPan", "()B"));
    jbyte pitch = (*env)->CallByteMethod(env, obj,
                                         (*env)->GetMethodID(env, cls, "getPitch", "()B"));

    setNextNoteInfo(track, onTick, offTick, volume, pan, pitch);
    (*env)->DeleteLocalRef(env, cls);
}

void updateNextNote(Track *track) {
    JNIEnv *env = getJniEnv();
    jobject obj = (*env)->CallObjectMethod(env, getTrackManager(),
                                           getNextMidiNoteMethod(), track->num,
                                           (jlong) (currSample / samplesPerTick));
    setNextNote(track, obj);
    (*env)->DeleteLocalRef(env, obj);
}

void Java_com_odang_beatbot_track_Track_setNextNote(JNIEnv *env, jclass clazz,
                                                    jint trackId, jobject midiNote) {
    Track *track = getTrack(trackId);
    setNextNote(track, midiNote);
}

void Java_com_odang_beatbot_track_BaseTrack_setTrackVolume(JNIEnv *env,
                                                           jclass clazz, jint trackId,
                                                           jfloat dbVolume) {
    Levels *levels = getLevels(trackId);
    levels->volume = dbToLinear(dbVolume);
    MidiEvent *midiEvent =
            trackId != MASTER_TRACK_ID ? getTrack(trackId)->nextEvent : NULL;
    updateChannelScaleValues(levels, midiEvent);
}

void Java_com_odang_beatbot_track_BaseTrack_setTrackPan(JNIEnv *env, jclass clazz,
                                                        jint trackId, jfloat pan) {
    Levels *levels = getLevels(trackId);
    levels->pan = panToScaleValue(pan);
    MidiEvent *midiEvent =
            trackId != MASTER_TRACK_ID ? getTrack(trackId)->nextEvent : NULL;
    updateChannelScaleValues(levels, midiEvent);
}

void Java_com_odang_beatbot_track_BaseTrack_setTrackPitch(JNIEnv *env,
                                                          jclass clazz, jint trackId,
                                                          jfloat pitchSteps) {
    Levels *levels = getLevels(trackId);
    levels->pitchSteps = pitchSteps;
    updatePitch(trackId);
}

jstring Java_com_odang_beatbot_track_Track_setSample(JNIEnv *env, jclass clazz,
                                                     jint trackId, jstring sampleName) {
    Track *track = getTrack(trackId);
    pthread_mutex_lock(&openSlOut->trackMutex);
    if (track->generator == NULL) {
        track->generator = malloc(sizeof(Generator));
        initGenerator(track->generator, filegen_create(), filegen_reset,
                      filegen_generate, filegen_destroy);
    }

    FileGen *fileGen = (FileGen *) track->generator->config;
    fileGen->gain = dbToLinear(0); // reset gain to 0
    const char *nativeSampleName = (*env)->GetStringUTFChars(env, sampleName, 0);
    setSample(track, nativeSampleName);
    pthread_mutex_unlock(&openSlOut->trackMutex);

    // release string memory
    (*env)->ReleaseStringUTFChars(env, sampleName, nativeSampleName);

    return (*env)->NewStringUTF(env, sf_strerror(NULL));
}

void Java_com_odang_beatbot_manager_TrackManager_createTrackNative(JNIEnv *env,
                                                                   jclass clazz, jint trackId) {
    Track *track = initTrack(trackId);
    pthread_mutex_lock(&openSlOut->trackMutex);
    createTrack(track);
    pthread_mutex_unlock(&openSlOut->trackMutex);
}

void Java_com_odang_beatbot_track_Track_deleteTrack(JNIEnv *env, jclass clazz,
                                                    int trackId) {
    TrackNode *trackNode = getTrackNode(trackId);
    pthread_mutex_lock(&openSlOut->trackMutex);
    removeTrack(trackNode);
    pthread_mutex_unlock(&openSlOut->trackMutex);
}

void Java_com_odang_beatbot_track_Track_toggleTrackLooping(JNIEnv *env,
                                                           jclass clazz, jint trackId) {
    Track *track = getTrack(trackId);
    if (track->generator == NULL)
        return;
    FileGen *fileGen = (FileGen *) track->generator->config;
    fileGen->looping = !fileGen->looping;
}

jboolean Java_com_odang_beatbot_track_Track_isTrackPlaying(JNIEnv *env,
                                                           jclass clazz, jint trackId) {
    Track *track = getTrack(trackId);
    if (track->generator == NULL) {
        return false;
    }

    return (jboolean) (currSample >= track->nextStartSample && currSample <= track->nextStopSample);
}

jboolean Java_com_odang_beatbot_track_Track_isTrackLooping(JNIEnv *env,
                                                           jclass clazz, jint trackId) {
    Track *track = getTrack(trackId);
    if (track->generator == NULL) {
        return false;
    }
    FileGen *fileGen = (FileGen *) track->generator->config;
    return (jboolean) fileGen->looping;
}

void Java_com_odang_beatbot_track_Track_notifyNoteRemoved(JNIEnv *env,
                                                          jclass clazz, jint trackId,
                                                          jlong onTick) {
    Track *track = getTrack(trackId);
    if (track->nextStartSample == (long) (onTick * samplesPerTick))
        stopTrack(track);
}

void Java_com_odang_beatbot_track_Track_setTrackLoopWindow(JNIEnv *env,
                                                           jclass clazz, jint trackId,
                                                           jlong loopBeginSample,
                                                           jlong loopEndSample) {
    Track *track = getTrack(trackId);
    if (track->generator == NULL)
        return;
    FileGen *fileGen = (FileGen *) track->generator->config;
    filegen_setLoopWindow(fileGen, loopBeginSample, loopEndSample);
}

void Java_com_odang_beatbot_track_Track_setTrackReverse(JNIEnv *env, jclass clazz,
                                                        jint trackId, jboolean reverse) {
    Track *track = getTrack(trackId);
    if (track->generator == NULL)
        return;
    FileGen *fileGen = (FileGen *) track->generator->config;
    filegen_setReverse(fileGen, reverse);
}

void Java_com_odang_beatbot_track_Track_setTrackGain(JNIEnv *env, jclass clazz,
                                                     jint trackId, jfloat dbGain) {
    Track *track = getTrack(trackId);
    if (track->generator == NULL)
        return;
    FileGen *fileGen = (FileGen *) track->generator->config;
    fileGen->gain = dbToLinear(dbGain);
}

float Java_com_odang_beatbot_track_Track_getSample(JNIEnv *env, jclass clazz,
                                                   jint trackId, jlong frame, jint channel) {
    Track *track = getTrack(trackId);
    if (track->generator == NULL)
        return 0;
    FileGen *fileGen = (FileGen *) track->generator->config;
    return filegen_getSample(fileGen, frame, channel);
}

void Java_com_odang_beatbot_track_Track_fillSampleBuffer(JNIEnv *env, jclass clazz,
                                                         jint trackId, jfloatArray sampleBuffer,
                                                         jint startFrame, jint endFrame,
                                                         jint jumpFrames) {
    Track *track = getTrack(trackId);
    if (track->generator == NULL)
        return;
    FileGen *fileGen = (FileGen *) track->generator->config;
    long numFrames = fileGen->frames;
    startFrame = startFrame < 0 ? 0 : startFrame;
    endFrame = (jint) (endFrame > numFrames ? numFrames : endFrame);

    float maxFrameIndexAndSample[] = {0, 0};

    int i = 0;
    int frameIndex;
    for (frameIndex = startFrame; frameIndex < endFrame; frameIndex += jumpFrames) {
        maxFrameIndexAndSample[0] = frameIndex;
        maxFrameIndexAndSample[1] = filegen_getSample(fileGen, frameIndex, 0);

        int j;
        int segmentEndFrame =
                (int) (frameIndex + jumpFrames > numFrames ? numFrames : frameIndex + jumpFrames);
        for (j = frameIndex + 1; j < segmentEndFrame; j++) {
            float candidateSample = filegen_getSample(fileGen, j, 0);
            if (fabs(candidateSample) > fabs(maxFrameIndexAndSample[1])) {
                maxFrameIndexAndSample[0] = (float) j;
                maxFrameIndexAndSample[1] = candidateSample;
            }
        }

        (*env)->SetFloatArrayRegion(env, sampleBuffer, i * 2, 2, maxFrameIndexAndSample);
        i++;
    }

    maxFrameIndexAndSample[0] = (float) -1.0; // end code
    maxFrameIndexAndSample[1] = (float) 0.0;
    (*env)->SetFloatArrayRegion(env, sampleBuffer, i * 2, 2, maxFrameIndexAndSample);
}

float Java_com_odang_beatbot_track_Track_getCurrentFrame(JNIEnv *env, jclass clazz,
                                                         jint trackId) {
    Track *track = getTrack(trackId);
    if (track->generator == NULL)
        return 0;
    FileGen *fileGen = (FileGen *) track->generator->config;
    return fileGen->currFrame;
}

float Java_com_odang_beatbot_track_Track_getFrames(JNIEnv *env, jclass clazz,
                                                   jint trackId) {
    Track *track = getTrack(trackId);
    if (track->generator == NULL)
        return 0;
    return ((FileGen *) track->generator->config)->frames;
}

