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

#include "effects.h"

// __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");
// #include <android/log.h>

// for native audio
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

// for native asset manager
#include <sys/types.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define CONV16BIT 32768
#define CONVMYFLT (1./32768.)
#define BUFF_SIZE 2048


typedef struct Sample_ {
	float currBufferFlt[BUFF_SIZE];
	short currBufferShort[BUFF_SIZE];
	
	// buffer to hold sample
	float *buffer;	
	float *scratchBuffer;

	int totalSamples;
	int currSample;

	float volume;
	float pan;
	
	unsigned int playing;

	DELAYLINE *delayLine;
	
	SLObjectItf outputPlayerObject;
	SLPlayItf outputPlayerPlay;
	SLVolumeItf outputPlayerVolume;
	SLMuteSoloItf outputPlayerMuteSolo;
	// output buffer interfaces
	SLAndroidSimpleBufferQueueItf outputBufferQueue;
} Sample;

static Sample *samples;
static int numSamples;
static int sampleCount = 0;
static unsigned int playing = 0;

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
void Java_com_kh_beatbot_BeatBotActivity_createEngine(JNIEnv* env, jclass clazz, jobject _assetManager, jint _numSamples) {
    SLresult result;

	numSamples = _numSamples;
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

short charsToShort(unsigned char first, unsigned char second) {
	return (first << 8) | second;
}

void initSample(Sample *sample, AAsset *asset) {
	// asset->getLength() returns size in bytes.  need size in shorts, minus 22 shorts of .wav header
	sample->totalSamples = AAsset_getLength(asset)/2 - 22;
	sample->buffer = calloc(sample->totalSamples, sizeof(float));
	sample->scratchBuffer = calloc(sample->totalSamples, sizeof(float));
	sample->playing = 0;
	sample->currSample = 0;
	sample->delayLine = delayline_create(0.8, 0.7);
}

void volumePanFilter(float inBuffer[], float outBuffer[], int size, float volume, float pan) {
	float leftVolume = (1 - pan)*volume;
	float rightVolume = pan*volume;
	int i;
	for (i = 0; i < size; i+=2) {
		outBuffer[i] = inBuffer[i]*leftVolume;
		outBuffer[i+1] = inBuffer[i + 1]*rightVolume;
	}
}

void floatArytoShortAry(float inBuffer[], short outBuffer[], int size) {
	int i;
	for (i = 0; i < size; i++) {
		outBuffer[i] = (short)(inBuffer[i]*CONV16BIT);
	}
}

void calcNextBuffer(Sample *sample) {
	// start with all zeros
	memset(sample->currBufferFlt, 0, BUFF_SIZE*sizeof(float));
    if (sample->playing) {
		int nextSize; // how many samples to copy from the source
		if (sample->currSample + BUFF_SIZE >= sample->totalSamples) {
			// at the end of the sample - copy all samples that are left
			nextSize = sample->totalSamples - sample->currSample;
			// end of sample - stop!
			sample->playing = 0;
		} else {
			nextSize = BUFF_SIZE; // plenty of samples left to copy :)		
		}					   
		// copy the next block of data from the scratch buffer into the current float buffer for streaming
		memcpy(sample->currBufferFlt, &(sample->buffer[sample->currSample]), nextSize*sizeof(float));
		// if we are at the end of the sample, reset the sample pointer, otherwise increment it
		sample->currSample = (nextSize < BUFF_SIZE) ? 0 : sample->currSample + BUFF_SIZE;
    }
	// calc volume/pan
	volumePanFilter(sample->currBufferFlt, sample->currBufferFlt, BUFF_SIZE, sample->volume, sample->pan);
	// calc delay
	//delayline_process(sample->delayLine, sample->currBufferFlt, BUFF_SIZE);
	// convert floats to shorts
	floatArytoShortAry(sample->currBufferFlt, sample->currBufferShort, BUFF_SIZE);	
}

// this callback handler is called every time a buffer finishes playing
void bufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
	if (!playing) {
		// global play is off.  no sound
		return;
	}
	
	Sample *sample = (Sample *)(context);
	SLresult result;
	
	// enqueue another buffer
    result = (*bq)->Enqueue(bq, sample->currBufferShort, BUFF_SIZE*sizeof(short));
//	short shortBuffer[sample->totalSamples];	
//	floatArytoShortAry(sample->buffer, shortBuffer, sample->totalSamples);	
//	result = (*bq)->Enqueue(bq, shortBuffer, sample->totalSamples*sizeof(short));
    assert(SL_RESULT_SUCCESS == result);
	// calculate the next buffer
	calcNextBuffer(sample);
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
	
	initSample(sample, asset);
	
	unsigned char *charBuf = (unsigned char *)AAsset_getBuffer(asset);
	int i;
	for (i = 0; i < sample->totalSamples; i++) {
		// first 44 bytes of a wav file are header
		sample->buffer[i] = charsToShort(charBuf[i*2 + 1 + 44], charBuf[i*2 + 44])*CONVMYFLT;
	}
	free(charBuf);
    AAsset_close(asset);
	memcpy(sample->scratchBuffer, sample->buffer, sample->totalSamples*sizeof(float));
	
    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};
	
	// config audio source for output buffer (source is a SimpleBufferQueue)
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
	SLDataSource outputAudioSrc = {&loc_bufq, &format_pcm};

	// create audio player for output buffer queue
    const SLInterfaceID ids1[] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE, SL_IID_VOLUME, SL_IID_MUTESOLO};
    const SLboolean req1[] = {SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &(sample->outputPlayerObject), &outputAudioSrc, &audioSnk,
												   3, ids1, req1);
	
	// realize the output player
    result = (*(sample->outputPlayerObject))->Realize(sample->outputPlayerObject, SL_BOOLEAN_FALSE);
    assert(result == SL_RESULT_SUCCESS);

    // get the play interface
	result = (*(sample->outputPlayerObject))->GetInterface(sample->outputPlayerObject, SL_IID_PLAY, &(sample->outputPlayerPlay));
	assert(result == SL_RESULT_SUCCESS);

	// get the volume interface
	result = (*(sample->outputPlayerObject))->GetInterface(sample->outputPlayerObject, SL_IID_VOLUME, &(sample->outputPlayerVolume));
	assert(result == SL_RESULT_SUCCESS);
	
    // get the mute/solo interface
	result = (*(sample->outputPlayerObject))->GetInterface(sample->outputPlayerObject, SL_IID_MUTESOLO, &(sample->outputPlayerMuteSolo));
	assert(result == SL_RESULT_SUCCESS);
	
	// get the buffer queue interface for output
    result = (*(sample->outputPlayerObject))->GetInterface(sample->outputPlayerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
													   &(sample->outputBufferQueue));
    assert(result == SL_RESULT_SUCCESS);	

    // register callback on the buffer queue
    result = (*sample->outputBufferQueue)->RegisterCallback(sample->outputBufferQueue, bufferQueueCallback, sample);
	
      // set the player's state to playing
	result = (*(sample->outputPlayerPlay))->SetPlayState(sample->outputPlayerPlay, SL_PLAYSTATE_PLAYING);
	assert(result == SL_RESULT_SUCCESS);
	
	// all done! increment sample count
	sampleCount++;

    return JNI_TRUE;
}

void Java_com_kh_beatbot_manager_PlaybackManager_openSlPlay(JNIEnv* env, jclass clazz) {
	playing = 1;
	int i;
	// trigger buffer queue callback to begin writing data to tracks
	for (i = 0; i < sampleCount; i++) {
		bufferQueueCallback(samples[i].outputBufferQueue, &(samples[i]));
	}
}

void Java_com_kh_beatbot_manager_PlaybackManager_openSlStop(JNIEnv* env, jclass clazz) {
	playing = 0;
}

void Java_com_kh_beatbot_manager_PlaybackManager_playSample(JNIEnv* env,
        jclass clazz, jint sampleNum, jfloat volume, jfloat pan, jfloat pitch) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[sampleNum];
	sample->playing = 1;	
	sample->currSample = 0;	
	sample->volume = volume;
	sample->pan = pan;
	memset(sample->currBufferFlt, 0, BUFF_SIZE*sizeof(float));
	memset(sample->currBufferShort, 0, BUFF_SIZE*sizeof(short));
}

void Java_com_kh_beatbot_manager_PlaybackManager_stopSample(JNIEnv* env,
													jclass clazz, jint sampleNum) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[sampleNum];
	sample->playing = 0;
	sample->currSample = 0;
}

void Java_com_kh_beatbot_manager_PlaybackManager_muteSample(JNIEnv* env,
															jclass clazz, jint sampleNum)
{
	if (sampleNum < 0 || sampleNum >= numSamples)
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
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[sampleNum];
	(*(sample->outputPlayerMuteSolo))->SetChannelMute(sample->outputPlayerMuteSolo, 0, SL_BOOLEAN_FALSE);
	(*(sample->outputPlayerMuteSolo))->SetChannelMute(sample->outputPlayerMuteSolo, 1, SL_BOOLEAN_FALSE);		
}

void Java_com_kh_beatbot_manager_PlaybackManager_soloSample(JNIEnv* env,
															  jclass clazz, jint sampleNum)
{
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;	
	Sample *sample = &samples[sampleNum];
	(*(sample->outputPlayerMuteSolo))->SetChannelSolo(sample->outputPlayerMuteSolo, 0, SL_BOOLEAN_TRUE);
	(*(sample->outputPlayerMuteSolo))->SetChannelSolo(sample->outputPlayerMuteSolo, 1, SL_BOOLEAN_TRUE);		
}

// shut down the native audio system
void Java_com_kh_beatbot_BeatBotActivity_shutdown(JNIEnv* env, jclass clazz)
{
	// destroy all samples
	int i;
	for (i = 0; i < numSamples; i++) {
		Sample *sample = &samples[i];
		(*(sample->outputBufferQueue))->Clear(sample->outputBufferQueue);
		sample->outputBufferQueue = NULL;			
		sample->outputPlayerPlay = NULL;
		free(sample->buffer);
		free(sample->scratchBuffer);
		free(sample->currBufferFlt);
		free(sample->currBufferShort);
		free(sample->delayLine);
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
