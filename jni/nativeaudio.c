#include "nativeaudio.h"

// __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");
// #include <android/log.h>

static int trackCount = 0;

// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine = NULL;
static AAssetManager* assetManager = NULL;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;

// create the engine and output mix objects
void Java_com_kh_beatbot_BeatBotActivity_createEngine(JNIEnv* env, jclass clazz, jobject _assetManager, jint _numTracks) {
    SLresult result;

	numTracks = _numTracks;
	playState = 0;
	tracks = (Track*)malloc(sizeof(Track)*numTracks);
	
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
	event->selected = false;
	event->levelSelected = false;	
	event->muted = false;
	event->onTick = onTick;
	event->offTick = offTick;
	event->volume = volume;
	event->pan = pan;
	event->pitch = pitch;
    return event;
}

void initTrack(Track *track, AAsset *asset) {
	// asset->getLength() returns size in bytes.  need size in shorts, minus 22 shorts of .wav header
	track->totalSamples = AAsset_getLength(asset)/2 - 22;
	track->buffer = calloc(track->totalSamples, sizeof(float));
	track->playing = false;
	track->currSample = 0;
	track->volume = .5f;
	track->pan = .5f;
	track->pitch = .5f;		
	track->delayLine = delayline_create(0.8, 0.7);
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

void calcNextBuffer(Track *track) {
	// start with all zeros
	memset(track->currBufferFlt, 0, BUFF_SIZE*sizeof(float));
    if (track->playing && track->currSample < track->totalSamples) {
		int nextSize; // how many tracks to copy from the source
		if (track->currSample + BUFF_SIZE >= track->totalSamples) {
			// at the end of the track - copy all tracks that are left
			nextSize = track->totalSamples - track->currSample;
		} else {
			nextSize = BUFF_SIZE; // plenty of tracks left to copy :)		
		}					   
		// copy the next block of data from the scratch buffer into the current float buffer for streaming
		memcpy(track->currBufferFlt, &(track->buffer[track->currSample]), nextSize*sizeof(float));
		// if we are at the end of the track, reset the track pointer, otherwise increment it
		track->currSample += nextSize;
    }
	// calc volume/pan
	volumePanFilter(track->currBufferFlt, track->currBufferFlt, BUFF_SIZE, track->volume, track->pan);
	// calc delay
	//delayline_process(track->delayLine, track->currBufferFlt, BUFF_SIZE);
	// convert floats to shorts
	floatArytoShortAry(track->currBufferFlt, track->currBufferShort, BUFF_SIZE);	
}

// this callback handler is called every time a buffer finishes playing
void bufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
	if (!playState) {
		// global play is off.  no sound
		return;
	}
	
	Track *track = (Track *)(context);
	SLresult result;
	
	// calculate the next buffer
	calcNextBuffer(track);
	
	// enqueue the buffer
    result = (*bq)->Enqueue(bq, track->currBufferShort, BUFF_SIZE*sizeof(short));
    assert(SL_RESULT_SUCCESS == result);
}

// create asset audio player
jboolean Java_com_kh_beatbot_BeatBotActivity_createAssetAudioPlayer(JNIEnv* env, jclass clazz,
        jstring filename)
{
	if (trackCount >= numTracks) {
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

	Track *track = &tracks[trackCount];
    SLresult result;
	
    // open asset as file descriptor
    off_t start, length;
	
	initTrack(track, asset);
	
	unsigned char *charBuf = (unsigned char *)AAsset_getBuffer(asset);
	int i;
	for (i = 0; i < track->totalSamples; i++) {
		// first 44 bytes of a wav file are header
		track->buffer[i] = charsToShort(charBuf[i*2 + 1 + 44], charBuf[i*2 + 44])*CONVMYFLT;
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
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &(track->outputPlayerObject), &outputAudioSrc, &audioSnk,
												   3, ids1, req1);
	
	// realize the output player
    result = (*(track->outputPlayerObject))->Realize(track->outputPlayerObject, SL_BOOLEAN_FALSE);
    assert(result == SL_RESULT_SUCCESS);

    // get the play interface
	result = (*(track->outputPlayerObject))->GetInterface(track->outputPlayerObject, SL_IID_PLAY, &(track->outputPlayerPlay));
	assert(result == SL_RESULT_SUCCESS);

	// get the volume interface
	result = (*(track->outputPlayerObject))->GetInterface(track->outputPlayerObject, SL_IID_VOLUME, &(track->outputPlayerVolume));
	assert(result == SL_RESULT_SUCCESS);
	
    // get the mute/solo interface
	result = (*(track->outputPlayerObject))->GetInterface(track->outputPlayerObject, SL_IID_MUTESOLO, &(track->outputPlayerMuteSolo));
	assert(result == SL_RESULT_SUCCESS);
	
	// get the buffer queue interface for output
    result = (*(track->outputPlayerObject))->GetInterface(track->outputPlayerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
													   &(track->outputBufferQueue));
    assert(result == SL_RESULT_SUCCESS);	

    // register callback on the buffer queue
    result = (*track->outputBufferQueue)->RegisterCallback(track->outputBufferQueue, bufferQueueCallback, track);
	
      // set the player's state to playing
	result = (*(track->outputPlayerPlay))->SetPlayState(track->outputPlayerPlay, SL_PLAYSTATE_PLAYING);
	assert(result == SL_RESULT_SUCCESS);
	
	// all done! increment track count
	trackCount++;

    return JNI_TRUE;
}

/****************************************************************************************
 Local versions of playTrack and stopTrack, to be called by the native MIDI ticker
 ****************************************************************************************/
void playTrack(int trackNum, float volume, float pan, float pitch) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;
	Track *track = &tracks[trackNum];
	track->currSample = 0;
	track->volume = volume;
	track->pan = pan;
	track->playing = true;
}

void stopTrack(int trackNum) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;
	Track *track = &tracks[trackNum];
	track->playing = false;
	track->currSample = 0;
}

void stopAll() {
	int i;
	for (i = 0; i < trackCount; i++) {
		tracks[i].playing = false;
	}
}

/****************************************************************************************
 Java PlaybackManager JNI methods
 ****************************************************************************************/
void Java_com_kh_beatbot_manager_PlaybackManager_openSlPlay(JNIEnv* env, jclass clazz) {
	playState = 1;
	int i;
	// trigger buffer queue callback to begin writing data to tracks
	for (i = 0; i < trackCount; i++) {
		bufferQueueCallback(tracks[i].outputBufferQueue, &(tracks[i]));
	}
}

void Java_com_kh_beatbot_manager_PlaybackManager_openSlStop(JNIEnv* env, jclass clazz) {
	playState = 0;
}

void Java_com_kh_beatbot_manager_PlaybackManager_playTrack(JNIEnv* env,
															jclass clazz, jint trackNum, jfloat volume, jfloat pan, jfloat pitch) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;
	Track *track = &tracks[trackNum];
	track->currSample = 0;
	track->volume = volume;
	track->pan = pan;
	track->playing = true;
}

void Java_com_kh_beatbot_manager_PlaybackManager_stopTrack(JNIEnv* env,
															jclass clazz, jint trackNum) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;
	Track *track = &tracks[trackNum];
	track->playing = false;
	track->currSample = 0;
}

void Java_com_kh_beatbot_manager_PlaybackManager_muteTrack(JNIEnv* env,
															jclass clazz, jint trackNum) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;
	Track *track = &tracks[trackNum];
	if (track->outputPlayerMuteSolo != NULL) {
		(*(track->outputPlayerMuteSolo))->SetChannelMute(track->outputPlayerMuteSolo, 0, SL_BOOLEAN_TRUE);
		(*(track->outputPlayerMuteSolo))->SetChannelMute(track->outputPlayerMuteSolo, 1, SL_BOOLEAN_TRUE);	
	}
}

void Java_com_kh_beatbot_manager_PlaybackManager_unmuteTrack(JNIEnv* env,
															jclass clazz, jint trackNum) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;
	Track *track = &tracks[trackNum];
	(*(track->outputPlayerMuteSolo))->SetChannelMute(track->outputPlayerMuteSolo, 0, SL_BOOLEAN_FALSE);
	(*(track->outputPlayerMuteSolo))->SetChannelMute(track->outputPlayerMuteSolo, 1, SL_BOOLEAN_FALSE);		
}

void Java_com_kh_beatbot_manager_PlaybackManager_soloTrack(JNIEnv* env,
															  jclass clazz, jint trackNum) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;	
	Track *track = &tracks[trackNum];
	(*(track->outputPlayerMuteSolo))->SetChannelSolo(track->outputPlayerMuteSolo, 0, SL_BOOLEAN_TRUE);
	(*(track->outputPlayerMuteSolo))->SetChannelSolo(track->outputPlayerMuteSolo, 1, SL_BOOLEAN_TRUE);		
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
MidiEventNode *removeEvent(MidiEventNode *head, long onTick, bool muted) {  
	MidiEventNode *prev_ptr, *cur_ptr = head;  	
	
	while(cur_ptr != NULL) {
		if((muted && cur_ptr->event->muted) ||
		   cur_ptr->event->onTick == onTick) {
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

void Java_com_kh_beatbot_manager_MidiManager_addMidiEvents(JNIEnv* env, jclass clazz, jint trackNum,
															   jlong onTick, jlong offTick, jfloat volume,
															   jfloat pan, jfloat pitch) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;
	Track *track = &tracks[trackNum];
	MidiEvent *event = initEvent(onTick, offTick, volume, pan, pitch);
	track->eventHead = addEvent(track->eventHead, event);
}

void Java_com_kh_beatbot_manager_MidiManager_removeMidiEvents(JNIEnv* env, jclass clazz, jint trackNum,
															   jlong onTick, jlong offTick) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;	
	Track *track = &tracks[trackNum];
	track->eventHead = removeEvent(track->eventHead, onTick, false);
}

void Java_com_kh_beatbot_manager_MidiManager_moveMidiEventTicks(JNIEnv* env, jclass clazz, jint trackNum,
															    jlong prevOnTick, jlong newOnTick,
															    jlong prevOffTick, jlong newOffTick) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;		
	Track *track = &tracks[trackNum];
	MidiEvent *event = findEvent(track->eventHead, prevOnTick);
	if (event != NULL) {
		event->onTick = newOnTick;
		event->offTick = newOffTick;
	}
}

void Java_com_kh_beatbot_manager_MidiManager_moveMidiEventNote(JNIEnv* env, jclass clazz, jint trackNum,
															   jlong onTick, jlong offTick, jint newTrackNum) {
	if (trackNum < 0 || trackNum >= numTracks || newTrackNum < 0 || newTrackNum >= numTracks)
		return;
	Track *prevTrack = &tracks[trackNum];
	Track *newTrack = &tracks[newTrackNum];	
	MidiEvent *event = findEvent(prevTrack->eventHead, onTick);
	if (event != NULL) {	
		float volume = event->volume;
		float pan = event->pan;
		float pitch = event->pitch;
		prevTrack->eventHead = removeEvent(prevTrack->eventHead, onTick, false);
		MidiEvent *newEvent = initEvent(onTick, offTick, volume, pan, pitch);
		newTrack->eventHead = addEvent(newTrack->eventHead, newEvent);
	}
}
void Java_com_kh_beatbot_manager_MidiManager_setEventMute(JNIEnv* env, jclass clazz, jint trackNum,
															   jlong onTick, jlong offTick, jboolean muted) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;		
	Track *track = &tracks[trackNum];
	MidiEvent *event = findEvent(track->eventHead, onTick);
	event->muted = muted;
	if (event->muted)
		__android_log_print(ANDROID_LOG_VERBOSE, "muted", "true");
	else
		__android_log_print(ANDROID_LOG_VERBOSE, "muted", "false");
}

void Java_com_kh_beatbot_manager_MidiManager_clearMutedEvents(JNIEnv* env, jclass clazz) {
	int i;
	for (i = 0; i < numTracks; i++) {
		Track *track = &tracks[i];
		MidiEventNode *head = track->eventHead;
		removeEvent(head, -1, true);	
	}
}

/****************************************************************************************
 Java MidiNote JNI methods
 ****************************************************************************************/
void Java_com_kh_beatbot_midi_MidiNote_setVolume(JNIEnv* env, jclass clazz, jint trackNum, jlong onTick, jfloat volume) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;		
	
	Track *track = &tracks[trackNum];
	MidiEvent *event = findEvent(track->eventHead, onTick);	
	if (event != NULL) {
		event->volume = volume;
	}
}

void Java_com_kh_beatbot_midi_MidiNote_setPan(JNIEnv* env, jclass clazz, jint trackNum, jlong onTick, jfloat pan) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;		
	Track *track = &tracks[trackNum];
	MidiEvent *event = findEvent(track->eventHead, onTick);	
	if (event != NULL) {
		event->pan = pan;
	}
}

void Java_com_kh_beatbot_midi_MidiNote_setPitch(JNIEnv* env, jclass clazz, jint trackNum, jlong onTick, jfloat pitch) {
	if (trackNum < 0 || trackNum >= numTracks)
		return;		
	Track *track = &tracks[trackNum];
	MidiEvent *event = findEvent(track->eventHead, onTick);
	if (event != NULL) {
		event->pitch = pitch;
	}
}


// shut down the native audio system
void Java_com_kh_beatbot_BeatBotActivity_shutdown(JNIEnv* env, jclass clazz)
{
	// destroy all tracks
	int i;
	for (i = 0; i < numTracks; i++) {
		Track *track = &tracks[i];
		(*(track->outputBufferQueue))->Clear(track->outputBufferQueue);
		track->outputBufferQueue = NULL;			
		track->outputPlayerPlay = NULL;
		free(track->buffer);
		free(track->currBufferFlt);
		free(track->currBufferShort);
		free(track->delayLine);
		freeLinkedList(track->eventHead);
	}
	free(tracks);
	
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
