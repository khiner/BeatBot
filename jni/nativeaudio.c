#include "nativeaudio.h"

// __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");
// #include <android/log.h>

static int sampleCount = 0;

// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine = NULL;
static AAssetManager* assetManager = NULL;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;

// create the engine and output mix objects
void Java_com_kh_beatbot_BeatBotActivity_createEngine(JNIEnv* env, jclass clazz, jobject _assetManager, jint _numSamples) {
    SLresult result;

	numSamples = _numSamples;
	playState = 0;
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

MidiEvent* initEvent(long onTick, long offTick, float volume, float pan, float pitch) {
	MidiEvent *event = malloc(sizeof(MidiEvent));
	event->onTick = onTick;
	event->offTick = offTick;
	event->volume = volume;
	event->pan = pan;
	event->pitch = pitch;
    return event;
}

void initSample(Sample *sample, AAsset *asset) {
	// asset->getLength() returns size in bytes.  need size in shorts, minus 22 shorts of .wav header
	sample->totalSamples = AAsset_getLength(asset)/2 - 22;
	sample->buffer = calloc(sample->totalSamples, sizeof(float));
	sample->playing = false;
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
    if (sample->playing && sample->currSample < sample->totalSamples) {
		int nextSize; // how many samples to copy from the source
		if (sample->currSample + BUFF_SIZE >= sample->totalSamples) {
			// at the end of the sample - copy all samples that are left
			nextSize = sample->totalSamples - sample->currSample;
		} else {
			nextSize = BUFF_SIZE; // plenty of samples left to copy :)		
		}					   
		// copy the next block of data from the scratch buffer into the current float buffer for streaming
		memcpy(sample->currBufferFlt, &(sample->buffer[sample->currSample]), nextSize*sizeof(float));
		// if we are at the end of the sample, reset the sample pointer, otherwise increment it
		sample->currSample += nextSize;
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
	if (!playState) {
		// global play is off.  no sound
		return;
	}
	
	Sample *sample = (Sample *)(context);
	SLresult result;
	
	// calculate the next buffer
	calcNextBuffer(sample);
	
	// enqueue the buffer
    result = (*bq)->Enqueue(bq, sample->currBufferShort, BUFF_SIZE*sizeof(short));
    assert(SL_RESULT_SUCCESS == result);
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

/****************************************************************************************
 Local versions of playSample and stopSample, to be called by the native MIDI ticker
 ****************************************************************************************/
void playSample(int sampleNum, float volume, float pan, float pitch) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[sampleNum];
	sample->playing = true;
	sample->currSample = 0;
	sample->volume = volume;
	sample->pan = pan;
}

void stopSample(int sampleNum) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[sampleNum];
	sample->playing = false;
	sample->currSample = 0;
}


void stopAll() {
	int i;
	for (i = 0; i < sampleCount; i++) {
		samples[i].playing = false;
	}
}

/****************************************************************************************
 Java PlaybackManager JNI methods
 ****************************************************************************************/
void Java_com_kh_beatbot_manager_PlaybackManager_openSlPlay(JNIEnv* env, jclass clazz) {
	playState = 1;
	int i;
	// trigger buffer queue callback to begin writing data to tracks
	for (i = 0; i < sampleCount; i++) {
		bufferQueueCallback(samples[i].outputBufferQueue, &(samples[i]));
	}
}

void Java_com_kh_beatbot_manager_PlaybackManager_openSlStop(JNIEnv* env, jclass clazz) {
	playState = 0;
}

void Java_com_kh_beatbot_manager_PlaybackManager_playSample(JNIEnv* env,
															jclass clazz, jint sampleNum, jfloat volume, jfloat pan, jfloat pitch) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[sampleNum];
	sample->playing = true;	
	sample->currSample = 0;	
	sample->volume = volume;
	sample->pan = pan;
}

void Java_com_kh_beatbot_manager_PlaybackManager_stopSample(JNIEnv* env,
															jclass clazz, jint sampleNum) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[sampleNum];
	sample->playing = false;
	sample->currSample = 0;
}

void Java_com_kh_beatbot_manager_PlaybackManager_muteSample(JNIEnv* env,
															jclass clazz, jint sampleNum) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[sampleNum];
	if (sample->outputPlayerMuteSolo != NULL) {
		(*(sample->outputPlayerMuteSolo))->SetChannelMute(sample->outputPlayerMuteSolo, 0, SL_BOOLEAN_TRUE);
		(*(sample->outputPlayerMuteSolo))->SetChannelMute(sample->outputPlayerMuteSolo, 1, SL_BOOLEAN_TRUE);	
	}
}

void Java_com_kh_beatbot_manager_PlaybackManager_unmuteSample(JNIEnv* env,
															jclass clazz, jint sampleNum) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[sampleNum];
	(*(sample->outputPlayerMuteSolo))->SetChannelMute(sample->outputPlayerMuteSolo, 0, SL_BOOLEAN_FALSE);
	(*(sample->outputPlayerMuteSolo))->SetChannelMute(sample->outputPlayerMuteSolo, 1, SL_BOOLEAN_FALSE);		
}

void Java_com_kh_beatbot_manager_PlaybackManager_soloSample(JNIEnv* env,
															  jclass clazz, jint sampleNum) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;	
	Sample *sample = &samples[sampleNum];
	(*(sample->outputPlayerMuteSolo))->SetChannelSolo(sample->outputPlayerMuteSolo, 0, SL_BOOLEAN_TRUE);
	(*(sample->outputPlayerMuteSolo))->SetChannelSolo(sample->outputPlayerMuteSolo, 1, SL_BOOLEAN_TRUE);		
}

/****************************************************************************************
 Java MidiManager JNI methods
 ****************************************************************************************/
MidiEvent *findEvent(MidiEventNode *head, long tick) {
	MidiEventNode *cur_ptr = head;
	while (cur_ptr != NULL) {
		if (cur_ptr->event->onTick == tick || cur_ptr->event->offTick == tick)
			return cur_ptr->event;
		cur_ptr = cur_ptr->next;
	}
	return NULL;
}

//Adding a Node at the end of the list  
MidiEventNode *addEvent(MidiEventNode *head, MidiEvent *event) {
	MidiEventNode *temp = malloc(sizeof(MidiEventNode));
	temp->event = event;
	temp->next = head;
	head = temp;
	return head;
}

// Deleting a node from List depending upon the data in the node.
MidiEventNode *removeEvent(MidiEventNode *head, long onTick, long offTick) {  
	MidiEventNode *prev_ptr, *cur_ptr = head;  	
	
	while(cur_ptr != NULL) {
		if(cur_ptr->event->onTick == onTick && cur_ptr->event->offTick == offTick) {
			if(cur_ptr == head) {
				head = cur_ptr->next;
				free(cur_ptr->event);
				free(cur_ptr);				
				cur_ptr = NULL;				
				return head;
			} else {
				prev_ptr->next = cur_ptr->next;
				free(cur_ptr->event);
				free(cur_ptr);				
				cur_ptr = NULL;
				return head;
			}
		} else {
			prev_ptr = cur_ptr;
			cur_ptr = cur_ptr->next;
		}
	}
	return head;
}

void freeLinkedList(MidiEventNode *head) {
	MidiEventNode *cur_ptr = head;
	while (cur_ptr != NULL) {
		free(cur_ptr->event); // free the event
		MidiEventNode *prev_ptr = cur_ptr;
		cur_ptr = cur_ptr->next;
		free(prev_ptr); // free the entire Node
	}	
}

void printLinkedList(MidiEventNode *head) {
	__android_log_print(ANDROID_LOG_DEBUG, "LL", "Elements:");	
	MidiEventNode *cur_ptr = head;
	while (cur_ptr != NULL) {
		__android_log_print(ANDROID_LOG_DEBUG, "LL Element", "onTick = %d", cur_ptr->event->onTick);
		__android_log_print(ANDROID_LOG_DEBUG, "LL Element", "offTick = %d", cur_ptr->event->offTick);		
		cur_ptr = cur_ptr->next;
	}	
}

void Java_com_kh_beatbot_manager_MidiManager_addMidiEvents(JNIEnv* env, jclass clazz, jint sampleNum,
															   jlong onTick, jlong offTick, jfloat volume,
															   jfloat pan, jfloat pitch) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;
	Sample *sample = &samples[sampleNum];
	MidiEvent *event = initEvent(onTick, offTick, volume, pan, pitch);
	sample->eventHead = addEvent(sample->eventHead, event);
}

void Java_com_kh_beatbot_manager_MidiManager_removeMidiEvents(JNIEnv* env, jclass clazz, jint sampleNum,
															   jlong onTick, jlong offTick) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;	
	Sample *sample = &samples[sampleNum];
	sample->eventHead = removeEvent(sample->eventHead, onTick, offTick);
}

void Java_com_kh_beatbot_manager_MidiManager_moveMidiEventTicks(JNIEnv* env, jclass clazz, jint sampleNum,
															    jlong prevOnTick, jlong newOnTick,
															    jlong prevOffTick, jlong newOffTick) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;		
	Sample *sample = &samples[sampleNum];
	MidiEvent *event = findEvent(sample->eventHead, prevOnTick);
	if (event != NULL) {
		event->onTick = newOnTick;
		event->offTick = newOffTick;
	}
}

void Java_com_kh_beatbot_manager_MidiManager_moveMidiEventNote(JNIEnv* env, jclass clazz, jint sampleNum,
															   jlong onTick, jlong offTick, jint newSampleNum) {
	if (sampleNum < 0 || sampleNum >= numSamples || newSampleNum < 0 || newSampleNum >= numSamples)
		return;
	Sample *prevSample = &samples[sampleNum];
	Sample *newSample = &samples[newSampleNum];	
	MidiEvent *event = findEvent(prevSample->eventHead, onTick);
	if (event != NULL) {	
		float volume = event->volume;
		float pan = event->pan;
		float pitch = event->pitch;
		prevSample->eventHead = removeEvent(prevSample->eventHead, onTick, offTick);
		MidiEvent *newEvent = initEvent(onTick, offTick, volume, pan, pitch);
		newSample->eventHead = addEvent(newSample->eventHead, newEvent);
	}
}

/****************************************************************************************
 Java MidiNote JNI methods
 ****************************************************************************************/
void Java_com_kh_beatbot_midi_MidiNote_setVolume(JNIEnv* env, jclass clazz, jint sampleNum, jlong onTick, jfloat volume) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;		
	Sample *sample = &samples[sampleNum];
	MidiEvent *event = findEvent(sample->eventHead, onTick);	
	if (event != NULL) {
		event->volume = volume;
	}
}

void Java_com_kh_beatbot_midi_MidiNote_setPan(JNIEnv* env, jclass clazz, jint sampleNum, jlong onTick, jfloat pan) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;		
	Sample *sample = &samples[sampleNum];
	MidiEvent *event = findEvent(sample->eventHead, onTick);	
	if (event != NULL) {
		event->pan = pan;
	}
}

void Java_com_kh_beatbot_midi_MidiNote_setPitch(JNIEnv* env, jclass clazz, jint sampleNum, jlong onTick, jfloat pitch) {
	if (sampleNum < 0 || sampleNum >= numSamples)
		return;		
	Sample *sample = &samples[sampleNum];
	MidiEvent *event = findEvent(sample->eventHead, onTick);
	if (event != NULL) {
		event->pitch = pitch;
	}
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
		free(sample->currBufferFlt);
		free(sample->currBufferShort);
		free(sample->delayLine);
		freeLinkedList(sample->eventHead);
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
