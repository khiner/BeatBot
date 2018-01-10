#include "all.h"
#include "jni_load.h"

// __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");

// engine interfaces
SLObjectItf engineObject = NULL;
SLEngineItf engineEngine = NULL;

// output mix interfaces
SLObjectItf outputMixObject = NULL;

bool playing = false;
bool recordArmed = false;
bool listening = false;
bool recording = false;
float thresholdLevel = 0;
FILE *recordOutFile = NULL;
pthread_mutex_t recordMutex, bufferFillMutex;
pthread_cond_t bufferFillCond = PTHREAD_COND_INITIALIZER;

bool isPlaying() {
    return playing;
}

static inline void interleaveFloatsToShorts(float left[], float right[],
                                            short interleaved[], int size) {
    int i;
    for (i = 0; i < size; i++) {
        interleaved[i * 2] = left[i] * FLOAT_TO_SHORT;
        interleaved[i * 2 + 1] = right[i] * FLOAT_TO_SHORT;
    }
}

// write the chars of the short to file, little endian
void writeShortToFile(short s, FILE *file) {
    fputc((char) s & 0xff, file);
    fputc((char) (s >> 8) & 0xff, file);
}

void writeShortBufferToRecordFile(short buffer[]) {
    pthread_mutex_lock(&recordMutex);
    int i = 0;
    for (i = 0; i < BUFF_SIZE_SHORTS; i++) {
        writeShortToFile(buffer[i], recordOutFile);
    }
    pthread_mutex_unlock(&recordMutex);
}

void writeFloatBufferToRecordFile(float left[], float right[], int size) {
    pthread_mutex_lock(&recordMutex);
    int i = 0;
    for (i = 0; i < size; i++) {
        writeShortToFile(left[i] * FLOAT_TO_SHORT, recordOutFile);
        writeShortToFile(right[i] * FLOAT_TO_SHORT, recordOutFile);
    }
    pthread_mutex_unlock(&recordMutex);
}

void startRecording() {
    pthread_mutex_lock(&recordMutex);
    recordArmed = false;
    JNIEnv *env = getJniEnv();
    jstring recordFilePath = (*env)->CallObjectMethod(env, getRecordManager(),
                                                      getStartRecordingJavaMethod());

    const char *cRecordFilePath = (*env)->GetStringUTFChars(env, recordFilePath, 0);

    // append to end of file, since header is written in Java
    recordOutFile = fopen(cRecordFilePath, "a+");
    recording = true;
    pthread_mutex_unlock(&recordMutex);
}

void notifyMaxFrame(float maxFrame) {
    JNIEnv *env = getJniEnv();
    (*env)->CallVoidMethod(env, getRecordManager(),
                           getNotifyRecordSourceBufferFilledMethod(), maxFrame);
    if (recordArmed && maxFrame > thresholdLevel) {
        startRecording();
    }
}

void processEffects(Levels *levels, float **floatBuffer) {
    pthread_mutex_lock(&levels->effectMutex);
    EffectNode *effectNode = levels->effectHead;
    while (effectNode != NULL) {
        if (effectNode->effect != NULL && effectNode->effect->on) {
            effectNode->effect->process(effectNode->effect->config, floatBuffer,
                                        BUFF_SIZE_FRAMES);
        }
        effectNode = effectNode->next;
    }
    pthread_mutex_unlock(&levels->effectMutex);
}

void processEffectsForAllTracks() {
    TrackNode *cur_ptr = trackHead;
    while (cur_ptr != NULL) {
        processEffects(cur_ptr->track->levels, cur_ptr->track->currBufferFloat);
        cur_ptr = cur_ptr->next;
    }
}

void processMasterEffects() {
    processEffects(masterLevels, openSlOut->currBufferFloat);
}

void mixTracks() {
    float maxFrame = 0, total = 0;
    int channel, samp;
    for (channel = 0; channel < 2; channel++) {
        for (samp = 0; samp < BUFF_SIZE_FRAMES; samp++) {
            total = 0;
            TrackNode *trackNode = trackHead;
            while (trackNode != NULL) {
                Track *track = trackNode->track;
                if (track->shouldSound) {
                    float trackLevel = track->currBufferFloat[channel][samp]
                                       * track->levels->scaleChannels[channel];
                    if (openSlOut->recordSourceId == trackNode->track->num
                        && trackLevel > maxFrame)
                        maxFrame = trackLevel;
                    total += trackLevel;
                }
                trackNode = trackNode->next;
            }

            total = clipTo(total * masterLevels->scaleChannels[channel], -1, 1);
            openSlOut->currBufferFloat[channel][samp] = total;
            if (openSlOut->recordSourceId == MASTER_TRACK_ID
                && total > maxFrame)
                // XXX at this stage, we are post-FX on each track but pre-FX on the master track,
                // so we're listening at different stages for different sources
                maxFrame = total;
        }
    }
    if (listening && openSlOut->recordSourceId != RECORD_SOURCE_MICROPHONE) {
        notifyMaxFrame(maxFrame);
    }
}

void Java_com_odang_beatbot_track_Track_previewTrack(JNIEnv *env, jclass clazz,
                                                     jint trackId) {
    previewTrack(getTrack(trackId));
}

void Java_com_odang_beatbot_track_Track_stopPreviewingTrack(JNIEnv *env,
                                                            jclass clazz, jint trackId) {
    stopPreviewingTrack(getTrack(trackId));
}

void Java_com_odang_beatbot_track_Track_stopTrack(JNIEnv *env, jclass clazz,
                                                  jint trackId) {
    stopTrack(getTrack(trackId));
}

void stopAllTracks() {
    currTick = loopBeginTick;
    TrackNode *cur_ptr = trackHead;
    while (cur_ptr != NULL) {
        stopTrack(cur_ptr->track);
        cur_ptr = cur_ptr->next;
    }
}

void disarm() {
    stopAllTracks();
    openSlOut->armed = false;
}

void generateNextBuffer() {
    int samp, channel;
    for (samp = 0; samp < BUFF_SIZE_FRAMES; samp++) {
        if (currTick > loopEndTick) {
            stopAllTracks();
        }
        TrackNode *cur_ptr = trackHead;
        while (cur_ptr != NULL) {
            Track *track = cur_ptr->track;
            if (playing && currTick == track->nextStartTick) {
                playTrack(track);
            } else if (currTick == track->nextStopTick) {
                stopTrack(track);
            }
            fillTempSample(track);
            for (channel = 0; channel < 2; channel++) {
                track->currBufferFloat[channel][samp] =
                        track->tempSample[channel];
            }
            cur_ptr = cur_ptr->next;
        }
        if (playing) {
            if (++currSample >= samplesPerTick) {
                currTick++;
                currSample = 0;
            }
        }
    }
}

// Generate each track's buffer (using the track's generator),
// Process all effects for all tracks
// Mix all tracks together into the OpenSL byte buffer
void fillBuffer() {
    pthread_mutex_lock(&openSlOut->trackMutex);
    generateNextBuffer();
    processEffectsForAllTracks();
    mixTracks();
    processMasterEffects();
    // combine the two channels of floats into one buffer of shorts,
    // interleaving L and R samples
    interleaveFloatsToShorts(openSlOut->currBufferFloat[0],
                             openSlOut->currBufferFloat[1], openSlOut->globalBufferShort,
                             BUFF_SIZE_FRAMES);
    pthread_mutex_unlock(&openSlOut->trackMutex);
}

// this callback handler is called every time a buffer finishes playing
void bufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    // enqueue the buffer
    if (openSlOut->armed) {
        (*bq)->Enqueue(bq, openSlOut->globalBufferShort, BUFF_SIZE_BYTES);
    }

    fillBuffer();
    if (recording && recordOutFile != NULL) {
        if (openSlOut->recordSourceId == MASTER_TRACK_ID) {
            writeShortBufferToRecordFile(openSlOut->globalBufferShort);
        } else if (openSlOut->recordSourceId != RECORD_SOURCE_MICROPHONE) {
            Track *track = getTrack(openSlOut->recordSourceId);
            if (track != NULL) {
                writeFloatBufferToRecordFile(track->currBufferFloat[0],
                                             track->currBufferFloat[1], BUFF_SIZE_FRAMES);
            }
        }
    }
}

// this callback handler is called every time a buffer fills
void micBufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    if (listening && openSlOut->recordSourceId == RECORD_SOURCE_MICROPHONE) {
        short maxFrameShort = 0;
        int i;
        for (i = 0; i < BUFF_SIZE_SHORTS; i++) {
            if (openSlOut->micBufferShort[i] > maxFrameShort) {
                maxFrameShort = openSlOut->micBufferShort[i];
            }
        }
        notifyMaxFrame((float) maxFrameShort * SHORT_TO_FLOAT);
        if (recording && recordOutFile != NULL) {
            writeShortBufferToRecordFile(openSlOut->micBufferShort);
        }
    }

    // re-enqueue the buffer
    (*bq)->Enqueue(bq, openSlOut->micBufferShort, BUFF_SIZE_BYTES);
}

void Java_com_odang_beatbot_manager_PlaybackManager_playNative(JNIEnv *env,
                                                               jclass clazz) {
    stopAllTracks();
    playing = true;
}

void Java_com_odang_beatbot_manager_PlaybackManager_stopNative(JNIEnv *env,
                                                               jclass clazz) {
    playing = false;
    stopAllTracks();
}

// create the engine and output mix objects
void Java_com_odang_beatbot_activity_BeatBotActivity_createEngine(JNIEnv *env,
                                                                  jclass clazz) {
    SLresult result;
    initTicker();

    // create engine
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);

    // realize the engine
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);

    // get the engine interface, which is needed in order to create other objects
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE,
                                           &engineEngine);

    // create output mix, with volume specified as a non-required interface
    const SLInterfaceID ids[1] = {SL_IID_VOLUME};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1,
                                              ids, req);

    // realize the output mix
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    trackHead = NULL;
    masterLevels = initLevels();
    previewEvent = malloc(sizeof(MidiEvent));
    previewEvent->volume = dbToLinear(0);
    previewEvent->pan = panToScaleValue(0);
    previewEvent->pitchSteps = .5f;
}

jboolean Java_com_odang_beatbot_activity_BeatBotActivity_createAudioPlayer(
        JNIEnv *env, jclass clazz) {
    openSlOut = malloc(sizeof(OpenSlOut));
    openSlOut->currBufferFloat = (float **) malloc(2 * sizeof(float *));
    openSlOut->currBufferFloat[0] = (float *) calloc(BUFF_SIZE_FRAMES,
                                                     ONE_FLOAT_SIZE);
    openSlOut->currBufferFloat[1] = (float *) calloc(BUFF_SIZE_FRAMES,
                                                     ONE_FLOAT_SIZE);
    memset(openSlOut->globalBufferShort, 0,
           sizeof(openSlOut->globalBufferShort));
    memset(openSlOut->micBufferShort, 0, sizeof(openSlOut->micBufferShort));
    openSlOut->recordSourceId = MASTER_TRACK_ID;
    openSlOut->armed = false;
    pthread_mutex_init(&openSlOut->trackMutex, NULL);

    // configure audio sink
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX,
                                         outputMixObject};
    SLDataSink outputAudioSink = {&outputMix, NULL};

    // config audio source for output buffer (source is a SimpleBufferQueue)
    SLDataLocator_AndroidSimpleBufferQueue outputBufferQueue = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataSource outputAudioSrc = {&outputBufferQueue, &format_pcm};

    // create audio player for output buffer queue
    const SLInterfaceID ids1[] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                  SL_IID_PLAYBACKRATE, SL_IID_MUTESOLO};
    const SLboolean req[] = {SL_BOOLEAN_TRUE};
    (*engineEngine)->CreateAudioPlayer(engineEngine,
                                       &(openSlOut->outputPlayerObject), &outputAudioSrc,
                                       &outputAudioSink,
                                       3, ids1, req);

    // realize the output player
    (*(openSlOut->outputPlayerObject))->Realize(openSlOut->outputPlayerObject,
                                                SL_BOOLEAN_FALSE);

    // get the play interface
    (*(openSlOut->outputPlayerObject))->GetInterface(
            openSlOut->outputPlayerObject, SL_IID_PLAY,
            &(openSlOut->outputPlayerPlay));

    // get the mute/solo interface
    (*(openSlOut->outputPlayerObject))->GetInterface(
            openSlOut->outputPlayerObject, SL_IID_MUTESOLO,
            &(openSlOut->outputPlayerMuteSolo));

    // get the buffer queue interface for output
    (*(openSlOut->outputPlayerObject))->GetInterface(
            openSlOut->outputPlayerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
            &(openSlOut->outputBufferQueue));

    // register callback on the buffer queue
    (*openSlOut->outputBufferQueue)->RegisterCallback(
            openSlOut->outputBufferQueue, bufferQueueCallback, openSlOut);

    // set the player's state to playing
    (*(openSlOut->outputPlayerPlay))->SetPlayState(openSlOut->outputPlayerPlay,
                                                   SL_PLAYSTATE_PLAYING);

    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE,
                                      SL_IODEVICE_AUDIOINPUT, SL_DEFAULTDEVICEID_AUDIOINPUT, NULL};
    SLDataSource audioSrc = {&loc_dev, NULL};
    SLDataSink recordAudioSink = {&outputBufferQueue, &format_pcm};

    // XXX might be able to reuse above
    const SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};

    (*engineEngine)->CreateAudioRecorder(engineEngine,
                                         &(openSlOut->recorderObject), &audioSrc, &recordAudioSink,
                                         1, id,
                                         req);

    (*openSlOut->recorderObject)->Realize(openSlOut->recorderObject,
                                          SL_BOOLEAN_FALSE);

    (*openSlOut->recorderObject)->GetInterface(openSlOut->recorderObject,
                                               SL_IID_RECORD, &(openSlOut->recordInterface));

    (*openSlOut->recorderObject)->GetInterface(openSlOut->recorderObject,
                                               SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                               &(openSlOut->micBufferQueue));

    (*openSlOut->micBufferQueue)->RegisterCallback(openSlOut->micBufferQueue,
                                                   micBufferQueueCallback, openSlOut);

    return JNI_TRUE;
}

void Java_com_odang_beatbot_activity_BeatBotActivity_arm(JNIEnv *_env,
                                                         jclass clazz) {
    if (openSlOut->armed)
        return; // only need to arm once

    openSlOut->armed = true;
    // we need to fill the buffer once before calling the OpenSL callback
    fillBuffer();
    bufferQueueCallback(openSlOut->outputBufferQueue, NULL);
}

// shut down the native audio system
void Java_com_odang_beatbot_activity_BeatBotActivity_nativeShutdown(JNIEnv *env,
                                                                    jclass clazz) {
    playing = false;
    stopAllTracks();

    // lock the mutex, so openSL doesn't try to grab from empty buffers
    pthread_mutex_lock(&openSlOut->trackMutex);

    if (openSlOut->outputBufferQueue != NULL) {
        (*openSlOut->outputBufferQueue)->Clear(openSlOut->outputBufferQueue);
        openSlOut->outputBufferQueue = NULL;
    }

    if (openSlOut->outputPlayerObject != NULL) {
        (*openSlOut->outputPlayerObject)->Destroy(
                openSlOut->outputPlayerObject);
        openSlOut->outputPlayerPlay = NULL;
    }

    if (openSlOut->micBufferQueue != NULL) {
        (*openSlOut->micBufferQueue)->Clear(openSlOut->micBufferQueue);
        openSlOut->micBufferQueue = NULL;
    }

    if (openSlOut->recorderObject != NULL) {
        (*openSlOut->recorderObject)->Destroy(openSlOut->recorderObject);
        openSlOut->recorderObject = NULL;
    }

    // destroy output mix object, and invalidate all associated interfaces
    if (outputMixObject != NULL) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
    }

    // destroy engine object, and invalidate all associated interfaces
    if (engineObject != NULL) {
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }

    destroyTracks();

    pthread_mutex_destroy(&openSlOut->trackMutex);
    pthread_mutex_destroy(&bufferFillMutex);
    pthread_cond_destroy(&bufferFillCond);
}

/****************************************************************************************
 Java RecordManager JNI methods
 ****************************************************************************************/
void Java_com_odang_beatbot_manager_RecordManager_startListeningNative(JNIEnv *env,
                                                                       jclass clazz) {
    // listening means to track the max frame of the current record source
    // if the current record source is the microphone, we need to enqueue
    // the micBufferQueue and start listening to the device's mic
    pthread_mutex_lock(&recordMutex);
    if (openSlOut->recordSourceId == RECORD_SOURCE_MICROPHONE) {
        // record from microphone.
        SLuint32 state;
        (*openSlOut->recordInterface)->GetRecordState(
                openSlOut->recordInterface, &state);
        // check for good state (should be stopped before recording)
        if (state != SL_RECORDSTATE_RECORDING) {
            (*openSlOut->recordInterface)->SetRecordState(
                    openSlOut->recordInterface, SL_RECORDSTATE_RECORDING);
            (*openSlOut->micBufferQueue)->Enqueue(openSlOut->micBufferQueue,
                                                  openSlOut->micBufferShort, BUFF_SIZE_BYTES);
        }
    }
    listening = true;
    pthread_mutex_unlock(&recordMutex);
}

void Java_com_odang_beatbot_manager_RecordManager_stopListeningNative(JNIEnv *env,
                                                                      jclass clazz) {
    pthread_mutex_lock(&recordMutex);
    listening = false;
    if (openSlOut->recordSourceId == RECORD_SOURCE_MICROPHONE) {
        SLuint32 state;
        (*openSlOut->recordInterface)->GetRecordState(
                openSlOut->recordInterface, &state);
        // if we're recording from the microphone, stop
        if (state == SL_RECORDSTATE_RECORDING) {
            (*openSlOut->recordInterface)->SetRecordState(
                    openSlOut->recordInterface, SL_RECORDSTATE_STOPPED);
        }
    }
    pthread_mutex_unlock(&recordMutex);
}

void Java_com_odang_beatbot_manager_RecordManager_stopRecordingNative(JNIEnv *env,
                                                                      jclass clazz) {
    pthread_mutex_lock(&recordMutex);
    recordArmed = recording = false;
    fflush(recordOutFile);
    fclose(recordOutFile);
    recordOutFile = NULL;
    pthread_mutex_unlock(&recordMutex);
}

void Java_com_odang_beatbot_manager_RecordManager_setRecordSourceNative(
        JNIEnv *_env, jclass clazz, jint recordSourceId) {
    pthread_mutex_lock(&recordMutex);
    openSlOut->recordSourceId = recordSourceId;
    pthread_mutex_unlock(&recordMutex);
}

void Java_com_odang_beatbot_manager_RecordManager_armNative(JNIEnv *env,
                                                            jclass clazz) {
    recordArmed = true;
}

void Java_com_odang_beatbot_manager_RecordManager_disarmNative(JNIEnv *env,
                                                               jclass clazz) {
    recordArmed = false;
}

void Java_com_odang_beatbot_manager_RecordManager_setThresholdLevel(JNIEnv *env,
                                                                    jclass clazz,
                                                                    jfloat _thresholdLevel) {
    thresholdLevel = _thresholdLevel;
}

