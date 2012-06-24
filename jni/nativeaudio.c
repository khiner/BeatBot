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
  
  initTicker();
  numTracks = _numTracks;
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
  MidiEvent *event = (MidiEvent *)malloc(sizeof(MidiEvent));
  event->muted = false;
  event->onTick = onTick;
  event->offTick = offTick;
  event->volume = volume;
  event->pan = pan;
  event->pitch = pitch;
  return event;
}

void precalculateEffects(Track *track) {
	// start with the raw audio samples
	memcpy(track->scratchBuffer, track->buffer, sizeof(float)*track->totalSamples);
	int i;
	for (i = 0; i < numEffects; i++) {
		Effect effect = track->effects[i];
		if (!effect.dynamic && effect.on)
			effect.process(effect.config, track->scratchBuffer, track->totalSamples);
	}
}

void initTrack(Track *track, AAsset *asset) {
  // asset->getLength() returns size in bytes.  need size in shorts, minus 22 shorts of .wav header
  track->totalSamples = AAsset_getLength(asset)/2 - 22;
  track->buffer = (float *)calloc(track->totalSamples, sizeof(float));
  track->scratchBuffer = (float *)calloc(track->totalSamples, sizeof(float));  
  track->armed = false;
  track->playing = false;
  track->loopMode = false;
  track->loopBegin = 0;
  track->loopEnd = track->totalSamples;
  track->currSample = 0;
  track->primaryPitch = .5f;
  track->pitch = .5f;
  
  initEffect(&(track->effects[STATIC_VOL_PAN_ID]), true, false, volumepanconfig_create(.5, .5),
  			 volumepanconfig_set, volumepan_process, volumepanconfig_destroy);
  initEffect(&(track->effects[DECIMATE_ID]), false, false, decimateconfig_create(4, 0.5), 
  			 decimateconfig_set, decimate_process, decimateconfig_destroy);  			 
  initEffect(&(track->effects[FILTER_ID]), false, false, filterconfig_create(11050, 0.5),
  			 filterconfig_set, filter_process, filterconfig_destroy);
  initEffect(&(track->effects[DYNAMIC_VOL_PAN_ID]), true, true, volumepanconfig_create(.5, .5), 
  			 volumepanconfig_set, volumepan_process, volumepanconfig_destroy);
  initEffect(&(track->effects[DELAY_ID]), false, true, delayconfig_create(.5, .5),
  			 delayconfig_set, delay_process, delayconfig_destroy);
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
  if (track->playing && track->currSample < track->loopEnd) {
    int totalSize = 0;
    int nextSize; // how many samples to copy from the source
    while (totalSize < BUFF_SIZE) {
      if (track->currSample + BUFF_SIZE - totalSize >= track->loopEnd) {
        // at the end of the window - copy all samples that are left
        nextSize = track->loopEnd - track->currSample;
      } else {
        nextSize = BUFF_SIZE - totalSize; // plenty of samples left to copy :)		
      }					   
      // copy the next block of data from the scratch buffer into the current float buffer for streaming
      memcpy(&(track->currBufferFlt[totalSize]), &(track->scratchBuffer[track->currSample]),
             nextSize*sizeof(float));

      totalSize += nextSize;
      // increment sample counter to reflect bytes written so far
      track->currSample += nextSize;
      if (track->currSample >= track->loopEnd) {
        if (track->loopMode) {
          // if we are looping, and we're past the end, loop back to the beginning
          track->currSample = track->loopBegin;
        } else {
          track->playing = false;
          break; // not looping, so we can play less than BUFF_SIZE samples
        }
      } 
    }
  }
}

void processEffects(Track *track) {
  int i;
  for (i = 0; i < numEffects; i++) {
  	Effect effect = track->effects[i];
  	if (effect.dynamic && effect.on)
  		effect.process(effect.config, track->currBufferFlt, BUFF_SIZE);
  }

  // convert floats to shorts
  floatArytoShortAry(track->currBufferFlt, track->currBufferShort, BUFF_SIZE);
}

// this callback handler is called every time a buffer finishes playing
void bufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
  Track *track = (Track *)(context);
  if (!track->armed) {
    // track is not armed. don't play any sound.
    return;
  }
	
  SLresult result;
	
  // calculate the next buffer
  calcNextBuffer(track);
  processEffects(track);	
	
  // enqueue the buffer
  result = (*bq)->Enqueue(bq, track->currBufferShort, BUFF_SIZE*sizeof(short));
  assert(SL_RESULT_SUCCESS == result);
}

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
  MidiEventNode *temp = (MidiEventNode *)malloc(sizeof(MidiEventNode));
  temp->event = event;
  temp->next = head;
  head = temp;
  return head;
}

// Deleting a node from List depending upon the data in the node.
MidiEventNode *removeEvent(MidiEventNode *head, long onTick, bool muted) {  
  MidiEventNode *prev_ptr = NULL, *cur_ptr = head;  	
	
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
    __android_log_print(ANDROID_LOG_DEBUG, "LL Element", "onTick = %ld", cur_ptr->event->onTick);
    __android_log_print(ANDROID_LOG_DEBUG, "LL Element", "offTick = %ld", cur_ptr->event->offTick);		
    cur_ptr = cur_ptr->next;
  }	
}

// create asset audio player
jboolean Java_com_kh_beatbot_BeatBotActivity_createAssetAudioPlayer(JNIEnv* env, jclass clazz,
                                                                    jstring filename)
{
  if (trackCount >= numTracks) {
    return JNI_FALSE;
  }

  // convert Java string to UTF-8
  const char *utf8 = (*env)->GetStringUTFChars(env, filename, NULL);
  assert(NULL != utf8);

  AAsset* asset = AAssetManager_open(assetManager, utf8, AASSET_MODE_UNKNOWN);	

  // release the Java string and UTF-8
  (*env)->ReleaseStringUTFChars(env, filename, utf8);
	
  // the asset might not be found
  if (NULL == asset) {
    return JNI_FALSE;
  }

  Track *track = &tracks[trackCount];
  SLresult result;
	
  initTrack(track, asset);
	
  unsigned char *charBuf = (unsigned char *)AAsset_getBuffer(asset);
  int i;
  for (i = 0; i < track->totalSamples; i++) {
    // first 44 bytes of a wav file are header
    track->buffer[i] = charsToShort(charBuf[i*2 + 1 + 44], charBuf[i*2 + 44])*CONVMYFLT;
  }
  free(charBuf);
  AAsset_close(asset);
	
  // prepare the scratch buffer with precalculated effects
  precalculateEffects(track);
  	
  // configure audio sink
  SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
  SLDataSink audioSnk = {&loc_outmix, NULL};
	
  // config audio source for output buffer (source is a SimpleBufferQueue)
  SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
  SLDataSource outputAudioSrc = {&loc_bufq, &format_pcm};

  // create audio player for output buffer queue
  const SLInterfaceID ids1[] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE, SL_IID_PLAYBACKRATE, SL_IID_MUTESOLO};
  const SLboolean req1[] = {SL_BOOLEAN_TRUE};
  result = (*engineEngine)->CreateAudioPlayer(engineEngine, &(track->outputPlayerObject), &outputAudioSrc, &audioSnk,
                                              3, ids1, req1);
	
  // realize the output player
  result = (*(track->outputPlayerObject))->Realize(track->outputPlayerObject, SL_BOOLEAN_FALSE);
  assert(result == SL_RESULT_SUCCESS);

  // get the play interface
  result = (*(track->outputPlayerObject))->GetInterface(track->outputPlayerObject, SL_IID_PLAY, &(track->outputPlayerPlay));
  assert(result == SL_RESULT_SUCCESS);

  // get the pitch interface
  result = (*(track->outputPlayerObject))->GetInterface(track->outputPlayerObject, SL_IID_PLAYBACKRATE, &(track->outputPlayerPitch));
  assert(result == SL_RESULT_SUCCESS);

  //if (track->outputPlayerPitch)
	//(*(track->outputPlayerPitch))->SetRate(track->outputPlayerPitch, 1000);
		
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

// shut down the native audio system
void Java_com_kh_beatbot_BeatBotActivity_shutdown(JNIEnv* env, jclass clazz) {
  // destroy all tracks
  int i, j;
  for (i = 0; i < numTracks; i++) {
    Track *track = &tracks[i];
    (*(track->outputBufferQueue))->Clear(track->outputBufferQueue);
    track->outputBufferQueue = NULL;			
    track->outputPlayerPlay = NULL;
    free(track->buffer);
    free(track->scratchBuffer);
    free(track->currBufferFlt);
    free(track->currBufferShort);
    for (j = 0; j < numEffects; j++) {
  	  track->effects[i].destroy(track->effects[i].config);
  	}
  	free(track->effects);
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

Track *getTrack(int trackNum) {
	if (trackNum < 0 || trackNum >= numTracks)
		return NULL;
	return &tracks[trackNum];
}

/****************************************************************************************
 Local versions of playTrack and stopTrack, to be called by the native MIDI ticker
****************************************************************************************/
void playTrack(int trackNum, float volume, float pan, float pitch) {
  Track *track = getTrack(trackNum);
  track->currSample = track->loopBegin;
  track->effects[DYNAMIC_VOL_PAN_ID].set(track->effects[DYNAMIC_VOL_PAN_ID].config, volume, pan);
  track->pitch = pitch;
  track->playing = true;
  if (track->outputPlayerPitch != NULL) {
	  //(*(track->outputPlayerPitch))->SetRate(track->outputPlayerPitch, (short)((pitch + track->primaryPitch)*750 + 500));
	  //__android_log_print(ANDROID_LOG_INFO, "numbeats", "%d", numBeats);	  
  }	  
}

void stopTrack(int trackNum) {
  Track *track = getTrack(trackNum);
  track->playing = false;
  track->currSample = track->loopBegin;
}

void stopAll() {
  int i;
  for (i = 0; i < trackCount; i++) {
    stopTrack(i);
  }
}

void syncAll() {
	int i;
	for (i = 0; i < numTracks; i++) {
		delayconfig_syncToBPM((DelayConfig *)(getTrack(i)->effects[DELAY_ID].config));
	}	
}

/****************************************************************************************
 Java PlaybackManager JNI methods
****************************************************************************************/
void Java_com_kh_beatbot_manager_PlaybackManager_armAllTracks(JNIEnv* env, jclass clazz) {
  int i;
  // arm each track, and
  // trigger buffer queue callback to begin writing data to tracks
  for (i = 0; i < trackCount; i++) {
    if (tracks[i].armed)
      continue;
    tracks[i].armed = true;
    // start writing zeros to the track's audio out
    bufferQueueCallback(tracks[i].outputBufferQueue, &(tracks[i]));
  }
}

void Java_com_kh_beatbot_manager_PlaybackManager_armTrack(JNIEnv* env, jclass clazz, jint trackNum) {
  if (tracks[trackNum].armed)
    return;
  // arm the track
  tracks[trackNum].armed = true;
  // start writing zeros to the track's audio out
  bufferQueueCallback(tracks[trackNum].outputBufferQueue, &(tracks[trackNum]));
}

void Java_com_kh_beatbot_manager_PlaybackManager_disarmAllTracks(JNIEnv* env, jclass clazz) {
  // disarm all tracks
  int i;
  for (i = 0; i < trackCount; i++) {
    tracks[i].armed = false;
  }
}

void Java_com_kh_beatbot_manager_PlaybackManager_disarmTrack(JNIEnv* env, jclass clazz, jint trackNum) {
  // disarm the track
  tracks[trackNum].armed = false;
}

void Java_com_kh_beatbot_manager_PlaybackManager_playTrack(JNIEnv* env, jclass clazz, jint trackNum) {
  Track *track = getTrack(trackNum);
  track->currSample = track->loopBegin;
  track->playing = true;
}

void Java_com_kh_beatbot_manager_PlaybackManager_stopTrack(JNIEnv* env, jclass clazz, jint trackNum) {
  Track *track = getTrack(trackNum);
  track->playing = false;
  track->currSample = track->loopBegin;
}

void Java_com_kh_beatbot_manager_PlaybackManager_muteTrack(JNIEnv* env, jclass clazz, jint trackNum) {
  Track *track = getTrack(trackNum);
  if (track->outputPlayerMuteSolo != NULL) {
    (*(track->outputPlayerMuteSolo))->SetChannelMute(track->outputPlayerMuteSolo, 0, SL_BOOLEAN_TRUE);
    (*(track->outputPlayerMuteSolo))->SetChannelMute(track->outputPlayerMuteSolo, 1, SL_BOOLEAN_TRUE);	
  }
}

void Java_com_kh_beatbot_manager_PlaybackManager_unmuteTrack(JNIEnv* env, jclass clazz, jint trackNum) {
  Track *track = getTrack(trackNum);
  (*(track->outputPlayerMuteSolo))->SetChannelMute(track->outputPlayerMuteSolo, 0, SL_BOOLEAN_FALSE);
  (*(track->outputPlayerMuteSolo))->SetChannelMute(track->outputPlayerMuteSolo, 1, SL_BOOLEAN_FALSE);		
}

void Java_com_kh_beatbot_manager_PlaybackManager_soloTrack(JNIEnv* env, jclass clazz, jint trackNum) {
  Track *track = getTrack(trackNum);
  (*(track->outputPlayerMuteSolo))->SetChannelSolo(track->outputPlayerMuteSolo, 0, SL_BOOLEAN_TRUE);
  (*(track->outputPlayerMuteSolo))->SetChannelSolo(track->outputPlayerMuteSolo, 1, SL_BOOLEAN_TRUE);		
}

void Java_com_kh_beatbot_manager_PlaybackManager_toggleLooping(JNIEnv* env, jclass clazz, jint trackNum) {
  Track *track = getTrack(trackNum);
  track->loopMode = !track->loopMode;
}

jboolean Java_com_kh_beatbot_manager_PlaybackManager_isLooping(JNIEnv* env, jclass clazz, jint trackNum) {
  return tracks[trackNum].loopMode;
}

void Java_com_kh_beatbot_manager_PlaybackManager_setLoopWindow(JNIEnv* env, jclass clazz,
                                                               jint trackNum, jint loopBeginSample, jint loopEndSample) {
  Track *track = getTrack(trackNum);
  track->loopBegin = loopBeginSample;
  track->loopEnd = loopEndSample;
  if (track->currSample >= track->loopEnd)
    track->currSample = track->loopBegin;
}

/****************************************************************************************
 Java MidiManager JNI methods
****************************************************************************************/

void Java_com_kh_beatbot_manager_MidiManager_addMidiNote(JNIEnv* env, jclass clazz, jint trackNum,
                                                         jlong onTick, jlong offTick, jfloat volume,
                                                         jfloat pan, jfloat pitch) {
  Track *track = getTrack(trackNum);
  MidiEvent *event = initEvent(onTick, offTick, volume, pan, pitch);
  track->eventHead = addEvent(track->eventHead, event);
}

void Java_com_kh_beatbot_manager_MidiManager_removeMidiNote(JNIEnv* env, jclass clazz, jint trackNum,
                                                            jlong tick) {
  Track *track = getTrack(trackNum);
  track->eventHead = removeEvent(track->eventHead, tick, false);
}

void Java_com_kh_beatbot_manager_MidiManager_moveMidiNoteTicks(JNIEnv* env, jclass clazz, jint trackNum,
                                                               jlong prevOnTick, jlong newOnTick,
                                                               jlong prevOffTick, jlong newOffTick) {
  Track *track = getTrack(trackNum);
  MidiEvent *event = findEvent(track->eventHead, prevOnTick);
  if (event != NULL) {
    event->onTick = newOnTick;
    event->offTick = newOffTick;
  }
}

void Java_com_kh_beatbot_manager_MidiManager_moveMidiNote(JNIEnv* env, jclass clazz, jint trackNum,
                                                          jlong tick, jint newTrackNum) {
  if (trackNum < 0 || trackNum >= numTracks || newTrackNum < 0 || newTrackNum >= numTracks)
    return;
  Track *prevTrack = &tracks[trackNum];
  Track *newTrack = &tracks[newTrackNum];	
  MidiEvent *event = findEvent(prevTrack->eventHead, tick);
  if (event != NULL) {
    float volume = event->volume;
    float pan = event->pan;
    float pitch = event->pitch;
    int onTick = event->onTick;
    int offTick = event->offTick;
    if (prevTrack->playing && currTick >= onTick && currTick <= offTick) {
      stopTrack(trackNum);
    }
    prevTrack->eventHead = removeEvent(prevTrack->eventHead, tick, false);
    MidiEvent *newEvent = initEvent(onTick, offTick, volume, pan, pitch);
    newTrack->eventHead = addEvent(newTrack->eventHead, newEvent);
  }
}

void Java_com_kh_beatbot_manager_MidiManager_setNoteMute(JNIEnv* env, jclass clazz, jint trackNum,
                                                         jlong tick, jboolean muted) {
  Track *track = getTrack(trackNum);
  MidiEvent *event = findEvent(track->eventHead, tick);
  event->muted = muted;
}

void Java_com_kh_beatbot_manager_MidiManager_clearMutedNotes(JNIEnv* env, jclass clazz) {
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
void Java_com_kh_beatbot_midi_MidiNote_setVolume(JNIEnv* env, jclass clazz,
                                                 jint trackNum, jlong onTick, jfloat volume) {
  Track *track = getTrack(trackNum);
  MidiEvent *event = findEvent(track->eventHead, onTick);	
  if (event != NULL) {
    event->volume = volume;
  }
}

void Java_com_kh_beatbot_midi_MidiNote_setPan(JNIEnv* env, jclass clazz,
                                              jint trackNum, jlong onTick, jfloat pan) {
  Track *track = getTrack(trackNum);
  MidiEvent *event = findEvent(track->eventHead, onTick);	
  if (event != NULL) {
    event->pan = pan;
  }
}

void Java_com_kh_beatbot_midi_MidiNote_setPitch(JNIEnv* env, jclass clazz,
                                                jint trackNum, jlong onTick, jfloat pitch) {
  Track *track = getTrack(trackNum);
  MidiEvent *event = findEvent(track->eventHead, onTick);
  if (event != NULL) {
    event->pitch = pitch;
  }
}

/****************************************************************************************
 Java SampleEditActivity JNI methods
****************************************************************************************/
jfloatArray makejFloatArray(JNIEnv* env, float floatAry[], int size) {
	jfloatArray result = (*env)->NewFloatArray(env, size);
	(*env)->SetFloatArrayRegion(env, result, 0, size, floatAry);
	return result;
}

jfloatArray Java_com_kh_beatbot_SampleEditActivity_getSamples(JNIEnv* env, jclass clazz, jint trackNum) {
	Track *track = getTrack(trackNum);
	return makejFloatArray(env, track->buffer, track->totalSamples);
}

jfloatArray Java_com_kh_beatbot_SampleEditActivity_reverse(JNIEnv* env, jclass clazz, jint trackNum) {
	Track *track = getTrack(trackNum);
	reverse(track->buffer, track->loopBegin, track->loopEnd);
	precalculateEffects(track);	
	return makejFloatArray(env, track->buffer, track->totalSamples);
}

jfloatArray Java_com_kh_beatbot_SampleEditActivity_normalize(JNIEnv* env, jclass clazz, jint trackNum) {
	Track *track = getTrack(trackNum);
	normalize(track->buffer, track->totalSamples);
	precalculateEffects(track);		
	return makejFloatArray(env, track->buffer, track->totalSamples);
}

/****************************************************************************************
 Java EditLevelsView JNI methods
****************************************************************************************/

jfloat Java_com_kh_beatbot_view_EditLevelsView_getPrimaryVolume(JNIEnv* env, jclass clazz, jint trackNum) {
	Track *track = getTrack(trackNum);
	return ((VolumePanConfig *)track->effects[STATIC_VOL_PAN_ID].config)->volume;
}

jfloat Java_com_kh_beatbot_view_EditLevelsView_getPrimaryPan(JNIEnv* env, jclass clazz, jint trackNum) {
	Track *track = getTrack(trackNum);
	return ((VolumePanConfig *)track->effects[STATIC_VOL_PAN_ID].config)->pan;
}

jfloat Java_com_kh_beatbot_view_EditLevelsView_getPrimaryPitch(JNIEnv* env, jclass clazz, jint trackNum) {
	Track *track = getTrack(trackNum);
	return track->primaryPitch;
}

void Java_com_kh_beatbot_view_EditLevelsView_setPrimaryVolume(JNIEnv* env, jclass clazz,
														 jint trackNum, jfloat volume) {
	Track *track = getTrack(trackNum);
	VolumePanConfig *config = (VolumePanConfig *)track->effects[STATIC_VOL_PAN_ID].config;
	track->effects[STATIC_VOL_PAN_ID].set(config, volume, config->pan);
	precalculateEffects(track);
}

void Java_com_kh_beatbot_view_EditLevelsView_setPrimaryPan(JNIEnv* env, jclass clazz,
													  jint trackNum, jfloat pan) {
	Track *track = getTrack(trackNum);
	VolumePanConfig *config = (VolumePanConfig *)track->effects[STATIC_VOL_PAN_ID].config;
	track->effects[STATIC_VOL_PAN_ID].set(config, config->volume, pan);
	
	precalculateEffects(track);	
}

void Java_com_kh_beatbot_view_EditLevelsView_setPrimaryPitch(JNIEnv* env, jclass clazz,
														jint trackNum, jfloat pitch) {
	Track *track = getTrack(trackNum);
	track->primaryPitch = pitch;
	if (track->outputPlayerPitch != NULL) {
	  //(*(track->outputPlayerPitch))->SetRate(track->outputPlayerPitch, (short)((track->pitch + track->primaryPitch)*750 + 500));	
  	}	
}

/****************************************************************************************
 Java Effects JNI methods
****************************************************************************************/

void Java_com_kh_beatbot_DecimateActivity_setDecimateOn(JNIEnv* env, jclass clazz,
														jint trackNum, jboolean on) {
	Track *track = getTrack(trackNum);
	Effect *decimate = &(track->effects[DECIMATE_ID]);
	decimate->on = on;
	precalculateEffects(track);	
}

void Java_com_kh_beatbot_DecimateActivity_setDecimateBits(JNIEnv* env, jclass clazz,
													jint trackNum, jfloat bits) {
	Track *track = getTrack(trackNum);
	Effect decimate = track->effects[DECIMATE_ID];
	DecimateConfig *decimateConfig = (DecimateConfig *)decimate.config;
	// bits range from 2 to 32
	bits *= 30;
	bits += 2;
	decimate.set(decimateConfig, bits, decimateConfig->rate);
	precalculateEffects(track);
}

void Java_com_kh_beatbot_DecimateActivity_setDecimateRate(JNIEnv* env, jclass clazz,
													jint trackNum, jfloat rate) {
	Track *track = getTrack(trackNum);
	Effect decimate = track->effects[DECIMATE_ID];
	DecimateConfig *decimateConfig = (DecimateConfig *)decimate.config;
	decimate.set(decimateConfig, decimateConfig->bits, rate);
	precalculateEffects(track);	
}

void Java_com_kh_beatbot_DecimateActivity_setDecimateDynamic(JNIEnv* env, jclass clazz,
													jint trackNum, jboolean dynamic) {
	Track *track = getTrack(trackNum);
	Effect *decimate = &(track->effects[DECIMATE_ID]);
	decimate->dynamic = dynamic;
	precalculateEffects(track);	
}

void Java_com_kh_beatbot_FilterActivity_setFilterOn(JNIEnv* env, jclass clazz,
													jint trackNum, jboolean on) {
	Track *track = getTrack(trackNum);
	Effect *filter = &(track->effects[FILTER_ID]);
	filter->on = on;
	precalculateEffects(track);	
}


void Java_com_kh_beatbot_FilterActivity_setFilterDynamic(JNIEnv* env, jclass clazz,
													jint trackNum, jboolean dynamic) {
	Track *track = getTrack(trackNum);
	Effect *filter = &(track->effects[FILTER_ID]);
	filter->dynamic = dynamic;
	precalculateEffects(track);	
}

void Java_com_kh_beatbot_FilterActivity_setFilterCutoff(JNIEnv* env, jclass clazz,
													  jint trackNum, jfloat cutoff) {
	Track *track = getTrack(trackNum);
	// provided cutoff is between 0 and 1.  map this to a value between
	// min_cutoff and max_cutoff
	Effect filter = track->effects[FILTER_ID];
	FilterConfig *filterConfig = (FilterConfig *)filter.config;
	cutoff *= filterConfig->max_cutoff - filterConfig->min_cutoff;
	cutoff /= 5;
	cutoff += filterConfig->min_cutoff;
	
	filter.set(filterConfig, cutoff, filterConfig->q);
	precalculateEffects(track);
}

void Java_com_kh_beatbot_FilterActivity_setFilterQ(JNIEnv* env, jclass clazz,
													  jint trackNum, jfloat q) {
	Track *track = getTrack(trackNum);
	FilterConfig *config = (FilterConfig *)track->effects[FILTER_ID].config; 	
	filterconfig_set(config, config->cutoff, q/2);
	precalculateEffects(track);
}

void Java_com_kh_beatbot_DelayActivity_setDelayOn(JNIEnv* env, jclass clazz,
												  jint trackNum, jboolean on) {
	Track *track = getTrack(trackNum);
	Effect *delay = &(track->effects[DELAY_ID]);
	delay->on = on;
}

void Java_com_kh_beatbot_DelayActivity_setDelayTime(JNIEnv* env, jclass clazz,
													  jint trackNum, jfloat time) {
	Track *track = getTrack(trackNum);
	DelayConfig *config = (DelayConfig *)track->effects[DELAY_ID].config;
	float newTime;
	if (config->beatmatch) {
 		// map float 0-1 to int 1-16 for number of beats
		delayconfig_setNumBeats(config, (int)(time*15) + 1);
	} else {
		newTime = time*2;
		delayconfig_setTime(config, newTime);
	}
}

void Java_com_kh_beatbot_DelayActivity_setDelayBeatmatch(JNIEnv* env, jclass clazz,
												  jint trackNum, jboolean beatmatch) {
	Track *track = getTrack(trackNum);
	DelayConfig *config = (DelayConfig *)track->effects[DELAY_ID].config;
	config->beatmatch = beatmatch;
	Java_com_kh_beatbot_DelayActivity_setDelayTime(NULL, NULL, trackNum, config->time);
}

void Java_com_kh_beatbot_DelayActivity_setDelayFeedback(JNIEnv* env, jclass clazz,
													  jint trackNum, jfloat fdb) {
	Track *track = getTrack(trackNum);
	DelayConfig *config = (DelayConfig *)track->effects[DELAY_ID].config;
	delayconfig_setFeedback(config, fdb);
}
