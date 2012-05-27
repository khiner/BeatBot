/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* This is a JNI example where we use native methods to play sounds
 * using OpenSL ES. See the corresponding Java source file located at:
 *
 *   src/com/example/nativeaudio/NativeAudio/NativeAudio.java
 */

#include <assert.h>
#include <jni.h>
#include <string.h>
#include <stdlib.h>

// for __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");
// #include <android/log.h>

// for native audio
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

// for native asset manager
#include <sys/types.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

typedef struct Sample_ {
	// buffer to hold sample
	short *buffer;		
	int totalSamples;

	SLObjectItf outputPlayerObject;	
	SLPlayItf outputPlayerPlay;	
	SLMuteSoloItf outputPlayerMuteSolo;		
	// output buffer interfaces
	SLAndroidSimpleBufferQueueItf outputBufferQueue;		
} Sample;

static Sample *samples;
static int numSamples;
static int sampleCount = 0;

// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine;
static AAssetManager* assetManager = NULL;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;

static SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1,
									  SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
									  SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
								      SL_BYTEORDER_LITTLEENDIAN};

// create the engine and output mix objects
void Java_com_kh_beatbot_BeatBotActivity_createEngine(JNIEnv* env, jclass clazz, jobject _assetManager, jint _numSamples)
{
    SLresult result;

	numSamples = (int)_numSamples;
	samples = (Sample*)malloc(sizeof(Sample)*numSamples);
	
    // create engine
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    assert(SL_RESULT_SUCCESS == result);

    // realize the engine
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);

    // get the engine interface, which is needed in order to create other objects
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    assert(SL_RESULT_SUCCESS == result);

	// create output mix, with volume specified as a non-required interface
    const SLInterfaceID ids[1] = {SL_IID_VOLUME};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, ids, req);
    assert(SL_RESULT_SUCCESS == result);

    // realize the output mix
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
	
    // use asset manager to open asset by filename
    assetManager = AAssetManager_fromJava(env, _assetManager);
    assert(NULL != assetManager);
}

short charsToShort(char first, char second) {
	return (first << 8) | second;
}

// create asset audio player
jboolean Java_com_kh_beatbot_BeatBotActivity_createAssetAudioPlayer(JNIEnv* env, jclass clazz,
        jstring filename)
{
	if (sampleCount >= numSamples) {
		return JNI_FALSE;
	}

    // convert Java string to UTF-8
    const jbyte *utf8 = (*env)->GetStringUTFChars(env, filename, NULL);
    assert(NULL != utf8);

	AAsset* asset = AAssetManager_open(assetManager, (const char *) utf8, AASSET_MODE_UNKNOWN);	

    // release the Java string and UTF-8
    (*env)->ReleaseStringUTFChars(env, filename, utf8);
	
    // the asset might not be found
    if (NULL == asset) {
        return JNI_FALSE;
    }

	Sample *sample = &samples[sampleCount];
    SLresult result;
	
    // open asset as file descriptor
    off_t start, length;
    int fd = AAsset_openFileDescriptor(asset, &start, &length);
    assert(0 <= fd);

	// asset->getLength() returns size in bytes.  we need size in shorts
	sample->totalSamples = AAsset_getLength(asset)/2;
	char *charBuf = malloc(sizeof(char)*sample->totalSamples*2);
	charBuf = (char *)AAsset_getBuffer(asset);
	sample->buffer = (short *)malloc(sizeof(short)*sample->totalSamples);
	int i;
	for (i = 0; i < sample->totalSamples; i++) {
		sample->buffer[i] = charsToShort(charBuf[i*2 + 1], charBuf[i*2]);
	}
	free(charBuf);
    AAsset_close(asset);
	
    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};
	
	// config audio source for output buffer (source is a SimpleBufferQueue)
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
	SLDataSource outputAudioSrc = {&loc_bufq, &format_pcm};

	// create audio player for output buffer queue
    const SLInterfaceID ids1[] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE, SL_IID_MUTESOLO};
    const SLboolean req1[] = {SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &(sample->outputPlayerObject), &outputAudioSrc, &audioSnk,
												   2, ids1, req1);
	
	// realize the output player
    result = (*(sample->outputPlayerObject))->Realize(sample->outputPlayerObject, SL_BOOLEAN_FALSE);
    assert(result == SL_RESULT_SUCCESS);

    // get the play interface
	result = (*(sample->outputPlayerObject))->GetInterface(sample->outputPlayerObject, SL_IID_PLAY, &(sample->outputPlayerPlay));
	assert(result == SL_RESULT_SUCCESS);

    // get the mute/solo interface
	result = (*(sample->outputPlayerObject))->GetInterface(sample->outputPlayerObject, SL_IID_MUTESOLO, &(sample->outputPlayerMuteSolo));
	assert(result == SL_RESULT_SUCCESS);
	
	// get the buffer queue interface for output
    result = (*(sample->outputPlayerObject))->GetInterface(sample->outputPlayerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
													   &(sample->outputBufferQueue));
    assert(result == SL_RESULT_SUCCESS);	
			
      // set the player's state to playing
	result = (*(sample->outputPlayerPlay))->SetPlayState(sample->outputPlayerPlay, SL_PLAYSTATE_PLAYING);
	assert(result == SL_RESULT_SUCCESS);
	
	// all done! increment sample count
	sampleCount++;

    return JNI_TRUE;
}

void Java_com_kh_beatbot_manager_PlaybackManager_playSample(JNIEnv* env,
        jclass clazz, jint sampleNum)
{
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[(int)sampleNum];

	(*(sample->outputBufferQueue))->Enqueue(sample->outputBufferQueue,
											sample->buffer, sample->totalSamples*sizeof(short));
}

void Java_com_kh_beatbot_manager_PlaybackManager_stopSample(JNIEnv* env,
													jclass clazz, jint sampleNum)
{
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[(int)sampleNum];
	(*(sample->outputBufferQueue))->Clear(sample->outputBufferQueue);
}

void Java_com_kh_beatbot_manager_PlaybackManager_muteSample(JNIEnv* env,
															jclass clazz, jint sampleNum)
{
	if (sampleNum <= 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[sampleNum];
	if (sample->outputPlayerMuteSolo != NULL) {
		(*(sample->outputPlayerMuteSolo))->SetChannelMute(sample->outputPlayerMuteSolo, 0, SL_BOOLEAN_TRUE);
		(*(sample->outputPlayerMuteSolo))->SetChannelMute(sample->outputPlayerMuteSolo, 1, SL_BOOLEAN_TRUE);	
	}
}

void Java_com_kh_beatbot_manager_PlaybackManager_unmuteSample(JNIEnv* env,
															jclass clazz, jint sampleNum)
{
	if (sampleNum <= 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[sampleNum];
	(*(sample->outputPlayerMuteSolo))->SetChannelMute(sample->outputPlayerMuteSolo, 0, SL_BOOLEAN_FALSE);
	(*(sample->outputPlayerMuteSolo))->SetChannelMute(sample->outputPlayerMuteSolo, 1, SL_BOOLEAN_FALSE);		
}
void Java_com_kh_beatbot_manager_PlaybackManager_soloSample(JNIEnv* env,
															  jclass clazz, jint sampleNum)
{
	if (sampleNum <= 0 || sampleNum >= numSamples)
		return;	
	Sample *sample = &samples[sampleNum];
	(*(sample->outputPlayerMuteSolo))->SetChannelSolo(sample->outputPlayerMuteSolo, 0, SL_BOOLEAN_TRUE);
	(*(sample->outputPlayerMuteSolo))->SetChannelSolo(sample->outputPlayerMuteSolo, 1, SL_BOOLEAN_TRUE);		
}

// shut down the native audio system
void Java_com_kh_beatbot_BeatBotActivity_shutdown(JNIEnv* env, jclass clazz)
{
    // destroy file descriptor audio player object, and invalidate all associated interfaces
	int i;
	for (i = 0; i < numSamples; i++) {
		Sample *sample = &samples[i];
		(*(sample->outputBufferQueue))->Clear(sample->outputBufferQueue);
		sample->outputBufferQueue = NULL;			
		sample->outputPlayerPlay = NULL;
		free(sample->buffer);
	}

	free(samples);
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
}
