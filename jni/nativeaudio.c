#include "nativeaudio.h"

// __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");

static int trackCount = 0;

// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine = NULL;
static AAssetManager* assetManager = NULL;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;

//static float zeroBuffer[2][BUFF_SIZE];

// create the engine and output mix objects
void Java_com_kh_beatbot_BeatBotActivity_createEngine(JNIEnv *env, jclass clazz,
		jobject _assetManager) {
	SLresult result;
	(void *) clazz; // avoid warnings about unused paramaters
	initTicker();

	// create engine
	result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);

	// realize the engine
	result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);

	// get the engine interface, which is needed in order to create other objects
	result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE,
			&engineEngine);

	// create output mix, with volume specified as a non-required interface
	const SLInterfaceID ids[1] = { SL_IID_VOLUME };
	const SLboolean req[1] = { SL_BOOLEAN_FALSE };
	result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1,
			ids, req);

	// realize the output mix
	result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);

	// use asset manager to open asset by filename
	assetManager = AAssetManager_fromJava(env, _assetManager);
}

MidiEvent* initEvent(long onTick, long offTick, float volume, float pan,
		float pitch) {
	MidiEvent *event = (MidiEvent *) malloc(sizeof(MidiEvent));
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
	track->nextEventNode = NULL;
  	track->currBufferFloat = (float **)malloc(2*sizeof(float *));
  	track->currBufferFloat[0] = (float *)calloc(BUFF_SIZE, sizeof(float));
  	track->currBufferFloat[1] = (float *)calloc(BUFF_SIZE, sizeof(float));
	track->armed = false;
	track->playing = false;
	track->mute = track->solo = false;
	track->volume = .8f;
	track->pan = track->pitch = .5f;
	track->generator = malloc(sizeof(Generator));
	initGenerator(track->generator, wavfile_create(asset), wavfile_reset,
			wavfile_generate, wavfile_destroy);
	initEffect(&(track->effects[VOL_PAN_ID]), true,
			volumepanconfig_create(.8f, .5f), volumepanconfig_set,
			volumepan_process, volumepanconfig_destroy);
	initEffect(&(track->effects[PITCH_ID]), false, pitchconfig_create(),
			pitchconfig_setShift, pitch_process, pitchconfig_destroy);
	initEffect(&(track->effects[DECIMATE_ID]), false,
			decimateconfig_create(4.0f, 0.5f), decimateconfig_set,
			decimate_process, decimateconfig_destroy);
	initEffect(&(track->effects[TREMELO_ID]), false,
			tremeloconfig_create(0.5f, 0.5f), tremeloconfig_set,
			tremelo_process, tremeloconfig_destroy);
	initEffect(&(track->effects[LP_FILTER_ID]), false,
			filterconfig_create(11050.0f, 0.5f), filterconfig_setLp,
			filter_process, filterconfig_destroy);
	initEffect(&(track->effects[HP_FILTER_ID]), false,
			filterconfig_create(11050.0f, 0.5f), filterconfig_setHp,
			filter_process, filterconfig_destroy);
	initEffect(&(track->effects[CHORUS_ID]), false,
			chorusconfig_create(.5f, .5f), chorusconfig_set, chorus_process,
			chorusconfig_destroy);
//  	initEffect(&(track->effects[DYNAMIC_PITCH_ID]), false, true, pitchconfig_create(),
//  			   pitchconfig_setShift, pitch_process, pitchconfig_destroy);
	initEffect(&(track->effects[DELAY_ID]), false,
			delayconfigi_create(.5f, .5f, SAMPLE_RATE), delayconfigi_set,
			delayi_process, delayconfigi_destroy);
	initEffect(&(track->effects[FLANGER_ID]), false, flangerconfig_create(),
			flangerconfig_set, flanger_process, flangerconfig_destroy);
	initEffect(&(track->effects[REVERB_ID]), false,
			reverbconfig_create(.5f, .5f), reverbconfig_set, reverb_process,
			reverbconfig_destroy);
	initEffect(&(track->effects[ADSR_ID]), false,
			adsrconfig_create(((WavFile *) (track->generator->config))->totalSamples),
			NULL, adsr_process, adsrconfig_destroy);
}

void interleaveFloatsToShorts(float left[], float right[], short interleaved[],
		int size) {
	int i;
	for (i = 0; i < size; i++) {
		interleaved[i * 2] = left[i] * CONV16BIT;
		interleaved[i * 2 + 1] = right[i] * CONV16BIT;
	}
}

void calcNextBuffer(Track *track) {
	// start with all zeros
	memset(track->currBufferFloat[0], 0, BUFF_SIZE * sizeof(float));
	memset(track->currBufferFloat[1], 0, BUFF_SIZE * sizeof(float));
	if (track->playing) {
		track->generator->generate(track->generator->config, track->currBufferFloat, BUFF_SIZE);
	}
}

void processEffects(Track *track) {
	int i;
	for (i = 0; i < NUM_EFFECTS; i++) {
		Effect effect = track->effects[i];
		if (effect.on)
			effect.process(effect.config, track->currBufferFloat, BUFF_SIZE);
	}
	// combine the two channels of floats into one buffer of shorts,
	// interleaving L and R samples
	interleaveFloatsToShorts(track->currBufferFloat[0],
			track->currBufferFloat[1], track->currBufferShort, BUFF_SIZE);
}

// this callback handler is called every time a buffer finishes playing
void bufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
	Track *track = (Track *) (context);
	if (!track->armed) { // track is not armed. don't play any sound.
		return;
	}
	SLresult result;
	// calculate the next buffer
	calcNextBuffer(track);
	processEffects(track);

	// enqueue the buffer
	result = (*bq)->Enqueue(bq, track->currBufferShort,
			BUFF_SIZE * 2 * sizeof(short));
}

MidiEvent *findEvent(Track *track, long tick) {
	MidiEventNode *cur_ptr = track->eventHead;
	while (cur_ptr != NULL) {
		if (cur_ptr->event->onTick == tick || cur_ptr->event->offTick == tick)
			return cur_ptr->event;
		cur_ptr = cur_ptr->next;
	}
	return NULL;
}

MidiEventNode *findNextEvent(Track *track) {
	if (track->eventHead == NULL)
		return NULL; // no midi notes in this track
   	// find event right after current tick
	MidiEventNode *ptr = track->eventHead;
   	while (ptr->next != NULL && ptr->event->offTick <= currTick)
   		ptr = ptr->next;
   	if (ptr->event->offTick <= currTick || ptr->event->onTick >= loopEndTick) {
   		// no events after curr tick, or next event after loop end.
   		// return fist event after loop begin
    	ptr = track->eventHead;
    	while (ptr->next != NULL && ptr->event->onTick < loopBeginTick)
    		ptr = ptr->next;
    }
    return ptr;
}

// add a midi event to the track's event linked list, inserting in order of onTick
MidiEventNode *addEvent(Track *track, MidiEvent *event) {
	MidiEventNode *temp = (MidiEventNode *) malloc(sizeof(MidiEventNode));
	temp->event = event;
	MidiEventNode *cur_ptr = track->eventHead;
	while (cur_ptr != NULL) {
		if (temp->event->onTick > cur_ptr->event->onTick &&
				(cur_ptr->next == NULL || temp->event->onTick < cur_ptr->next->event->onTick)) {
			temp->next = cur_ptr->next;
			cur_ptr->next = temp;
			return temp;
		}
		cur_ptr = cur_ptr->next;
	}
	// if we get here, the new event is before any other event, or is the first event
	temp->next = track->eventHead;
	track->eventHead = temp;
	return temp;
}

// Deleting a node from List depending upon the data in the node.
MidiEventNode *removeEvent(Track *track, long onTick, bool muted) {
	MidiEventNode *prev_ptr = NULL, *cur_ptr = track->eventHead;

	while (cur_ptr != NULL) {
		if ((muted && cur_ptr->event->muted)
				|| cur_ptr->event->onTick == onTick) {
			if (cur_ptr == track->eventHead) {
				track->eventHead = cur_ptr->next;
				free(cur_ptr->event);
				free(cur_ptr);
				cur_ptr = NULL;
				return track->eventHead;
			} else {
				prev_ptr->next = cur_ptr->next;
				free(cur_ptr->event);
				free(cur_ptr);
				cur_ptr = NULL;
				return track->eventHead;
			}
		} else {
			prev_ptr = cur_ptr;
			cur_ptr = cur_ptr->next;
		}
	}
	return track->eventHead;
}

void printLinkedList(MidiEventNode *head) {
	__android_log_print(ANDROID_LOG_DEBUG, "LL", "Elements:");
	MidiEventNode *cur_ptr = head;
	while (cur_ptr != NULL) {
		__android_log_print(ANDROID_LOG_DEBUG, "LL Element", "onTick = %ld",
				cur_ptr->event->onTick);
		__android_log_print(ANDROID_LOG_DEBUG, "LL Element", "offTick = %ld",
				cur_ptr->event->offTick);
		cur_ptr = cur_ptr->next;
	}
}

// create asset audio player
jboolean Java_com_kh_beatbot_BeatBotActivity_createAssetAudioPlayer(
		JNIEnv * env, jclass clazz, jstring filename) {
	if (trackCount >= NUM_TRACKS) {
		return JNI_FALSE;
	}

	// convert Java string to UTF-8
	const char *utf8 = (*env)->GetStringUTFChars(env, filename, NULL);

	AAsset* asset = AAssetManager_open(assetManager, utf8, AASSET_MODE_UNKNOWN);

	// release the Java string and UTF-8
	(*env)->ReleaseStringUTFChars(env, filename, utf8);

	// the asset might not be found
	if (NULL == asset) {
		return JNI_FALSE;
	}

	Track *track = getTrack(env, clazz, trackCount);
	SLresult result;

	initTrack(track, asset);

	// configure audio sink
	SLDataLocator_OutputMix loc_outmix = { SL_DATALOCATOR_OUTPUTMIX,
			outputMixObject };
	SLDataSink audioSnk = { &loc_outmix, NULL };

	// config audio source for output buffer (source is a SimpleBufferQueue)
	SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {
			SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2 };
	SLDataSource outputAudioSrc = { &loc_bufq, &format_pcm };

	// create audio player for output buffer queue
	const SLInterfaceID ids1[] = { SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
			SL_IID_PLAYBACKRATE, SL_IID_MUTESOLO };
	const SLboolean req1[] = { SL_BOOLEAN_TRUE };
	result = (*engineEngine)->CreateAudioPlayer(engineEngine,
			&(track->outputPlayerObject), &outputAudioSrc, &audioSnk, 3, ids1,
			req1);

	// realize the output player
	result = (*(track->outputPlayerObject))->Realize(track->outputPlayerObject,
			SL_BOOLEAN_FALSE);

	// get the play interface
	result = (*(track->outputPlayerObject))->GetInterface(
			track->outputPlayerObject, SL_IID_PLAY, &(track->outputPlayerPlay));

	// get the pitch interface
	result = (*(track->outputPlayerObject))->GetInterface(
			track->outputPlayerObject, SL_IID_PLAYBACKRATE,
			&(track->outputPlayerPitch));

	//if (track->outputPlayerPitch)
	//(*(track->outputPlayerPitch))->SetRate(track->outputPlayerPitch, 1000);

	// get the mute/solo interface
	result = (*(track->outputPlayerObject))->GetInterface(
			track->outputPlayerObject, SL_IID_MUTESOLO,
			&(track->outputPlayerMuteSolo));

	// get the buffer queue interface for output
	result = (*(track->outputPlayerObject))->GetInterface(
			track->outputPlayerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
			&(track->outputBufferQueue));

	// register callback on the buffer queue
	result = (*track->outputBufferQueue)->RegisterCallback(
			track->outputBufferQueue, bufferQueueCallback, track);

	// set the player's state to playing
	result = (*(track->outputPlayerPlay))->SetPlayState(track->outputPlayerPlay,
			SL_PLAYSTATE_PLAYING);

	// all done! increment track count
	trackCount++;
	return JNI_TRUE;
}

// shut down the native audio system
void Java_com_kh_beatbot_BeatBotActivity_shutdown(JNIEnv *env, jclass clazz) {
	freeTracks();
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

/****************************************************************************************
 Local versions of playTrack and stopTrack, to be called by the native MIDI ticker
 ****************************************************************************************/
void playTrack(int trackNum, float volume, float pan, float pitch) {
	Track *track = getTrack(NULL, NULL, trackNum);
	track->playing = true;
	track->effects[VOL_PAN_ID].set(track->effects[VOL_PAN_ID].config,
			track->volume * volume, track->pan * pan);
	//pitchconfig_setShift((PitchConfig *)track->effects[DYNAMIC_PITCH_ID].config, pitch*2 - 1);
	AdsrConfig *adsrConfig = (AdsrConfig *) track->effects[ADSR_ID].config;
	adsrConfig->active = true;
	resetAdsr(adsrConfig);
	//if (track->outputPlayerPitch != NULL) {
	//	(*(track->outputPlayerPitch))->SetRate(track->outputPlayerPitch, (short)((pitch + track->primaryPitch)*750 + 500));
	//}
}

void stopTrack(int trackNum) {
	Track *track = getTrack(NULL, NULL, trackNum);
	track->playing = false;
	wavfile_reset((WavFile *) track->generator->config);
	((AdsrConfig *) track->effects[ADSR_ID].config)->active = false;
	// update next track
	track->nextEventNode = findNextEvent(track);
}

void stopAll() {
	int i;
	for (i = 0; i < trackCount; i++) {
		stopTrack(i);
	}
}

/****************************************************************************************
 Java PlaybackManager JNI methods
 ****************************************************************************************/
void Java_com_kh_beatbot_manager_PlaybackManager_armAllTracks(JNIEnv *env,
		jclass clazz) {
	int i;
	// arm each track, and
	// trigger buffer queue callback to begin writing data to tracks
	for (i = 0; i < trackCount; i++) {
		Track *track = getTrack(env, clazz, i);
		if (track->armed)
			continue;
		track->armed = true;
		// start writing zeros to the track's audio out
		bufferQueueCallback(tracks[i].outputBufferQueue, &(tracks[i]));
	}
}

void Java_com_kh_beatbot_manager_PlaybackManager_armTrack(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	if (track->armed)
		return;
	// arm the track
	track->armed = true;
	// start writing zeros to the track's audio out
	bufferQueueCallback(track->outputBufferQueue, track);
}

void Java_com_kh_beatbot_manager_PlaybackManager_disarmAllTracks(JNIEnv *env,
		jclass clazz) {
	// disarm all tracks
	int i;
	for (i = 0; i < trackCount; i++) {
		getTrack(env, clazz, i)->armed = false;
	}
}

void Java_com_kh_beatbot_manager_PlaybackManager_disarmTrack(JNIEnv *env,
		jclass clazz, jint trackNum) {
	// disarm the track
	getTrack(env, clazz, trackNum)->armed = false;
}

void Java_com_kh_beatbot_manager_PlaybackManager_playTrack(JNIEnv *env,
		jclass clazz, jint trackNum) {
	playTrack(trackNum, 1, 1, 1);
}

void Java_com_kh_beatbot_manager_PlaybackManager_stopTrack(JNIEnv *env,
		jclass clazz, jint trackNum) {
	stopTrack(trackNum);
}

void setTrackMute(Track *track, bool mute) {
	if (track->outputPlayerMuteSolo == NULL)
		return;

	if (mute) {
		(*(track->outputPlayerMuteSolo))->SetChannelMute(
				track->outputPlayerMuteSolo, 0, SL_BOOLEAN_TRUE);
		(*(track->outputPlayerMuteSolo))->SetChannelMute(
				track->outputPlayerMuteSolo, 1, SL_BOOLEAN_TRUE);
	} else {
		(*(track->outputPlayerMuteSolo))->SetChannelMute(
				track->outputPlayerMuteSolo, 0, SL_BOOLEAN_FALSE);
		(*(track->outputPlayerMuteSolo))->SetChannelMute(
				track->outputPlayerMuteSolo, 1, SL_BOOLEAN_FALSE);
	}
}

int getSoloingTrackNum() {
	int i;
	for (i = 0; i < NUM_TRACKS; i++) {
		if (getTrack(NULL, NULL, i)->solo) {
			return i;
		}
	}
	return -1;
}

void Java_com_kh_beatbot_manager_PlaybackManager_muteTrack(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	setTrackMute(track, true);
	track->mute = true;
}

void Java_com_kh_beatbot_manager_PlaybackManager_unmuteTrack(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	int soloingTrackNum = getSoloingTrackNum();
	if (soloingTrackNum == -1 || soloingTrackNum == trackNum)
		setTrackMute(track, false);
	track->mute = false;
}

void Java_com_kh_beatbot_manager_PlaybackManager_soloTrack(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	track->solo = true;
	int i;
	if (!track->mute) {
		setTrackMute(track, false);
	}
	for (i = 0; i < NUM_TRACKS; i++) {
		if (i != trackNum) {
			track = getTrack(env, clazz, i);
			setTrackMute(track, true);
			track->solo = false;
		}
	}
}

void Java_com_kh_beatbot_manager_PlaybackManager_unsoloTrack(JNIEnv *env,
		jclass clazz, jint trackNum) {
	int i;
	Track *track = getTrack(env, clazz, trackNum);
	track->solo = false;
	for (i = 0; i < NUM_TRACKS; i++) {
		track = getTrack(env, clazz, i);
		if (!track->mute) {
			setTrackMute(track, false);
		}
	}

}

/****************************************************************************************
 Java MidiManager JNI methods
 ****************************************************************************************/

void Java_com_kh_beatbot_manager_MidiManager_addMidiNote(JNIEnv *env,
		jclass clazz, jint trackNum, jlong onTick, jlong offTick, jfloat volume,
		jfloat pan, jfloat pitch) {
	Track *track = getTrack(env, clazz, trackNum);
	MidiEvent *event = initEvent(onTick, offTick, volume, pan, pitch);

	MidiEventNode *created = addEvent(track, event);
	track->nextEventNode = findNextEvent(track);
}

void Java_com_kh_beatbot_manager_MidiManager_deleteMidiNote(JNIEnv *env,
		jclass clazz, jint trackNum, jlong tick) {
	Track *track = getTrack(env, clazz, trackNum);
	removeEvent(track, tick, false);
	track->nextEventNode = findNextEvent(track);
}

void Java_com_kh_beatbot_manager_MidiManager_moveMidiNoteTicks(JNIEnv *env,
		jclass clazz, jint trackNum, jlong prevOnTick, jlong newOnTick,
		jlong newOffTick) {
	Track *track = getTrack(env, clazz, trackNum);
	MidiEvent *event = findEvent(track, prevOnTick);
	if (event != NULL) {
		event->onTick = newOnTick;
		event->offTick = newOffTick;
	}
	track->nextEventNode = findNextEvent(track);
}

void Java_com_kh_beatbot_manager_MidiManager_moveMidiNote(JNIEnv *env,
		jclass clazz, jint trackNum, jlong tick, jint newTrackNum) {
	if (trackNum < 0 || trackNum >= NUM_TRACKS || newTrackNum < 0
			|| newTrackNum >= NUM_TRACKS)
		return;
	Track *prevTrack = getTrack(env, clazz, trackNum);
	Track *newTrack = getTrack(env, clazz, newTrackNum);
	MidiEvent *event = findEvent(prevTrack, tick);
	if (event != NULL) {
		float volume = event->volume;
		float pan = event->pan;
		float pitch = event->pitch;
		int onTick = event->onTick;
		int offTick = event->offTick;
		if (prevTrack->playing && currTick >= onTick && currTick <= offTick) {
			stopTrack(trackNum);
		}
		removeEvent(prevTrack, tick, false);
		MidiEvent *newEvent = initEvent(onTick, offTick, volume, pan, pitch);
		addEvent(newTrack, newEvent);
	}
	prevTrack->nextEventNode = findNextEvent(prevTrack);
	newTrack->nextEventNode = findNextEvent(newTrack);
}

void Java_com_kh_beatbot_manager_MidiManager_setNoteMute(JNIEnv *env,
		jclass clazz, jint trackNum, jlong tick, jboolean muted) {
	Track *track = getTrack(env, clazz, trackNum);
	MidiEvent *event = findEvent(track, tick);
	event->muted = muted;
}

void Java_com_kh_beatbot_manager_MidiManager_clearMutedNotes(JNIEnv *env,
		jclass clazz) {
	int i;
	for (i = 0; i < NUM_TRACKS; i++) {
		Track *track = getTrack(env, clazz, i);
		MidiEventNode *head = track->eventHead;
		removeEvent(head, -1, true);
		track->nextEventNode = findNextEvent(track);
	}
}

/****************************************************************************************
 Java MidiNote JNI methods
 ****************************************************************************************/
void Java_com_kh_beatbot_midi_MidiNote_setVolume(JNIEnv *env, jclass clazz,
		jint trackNum, jlong onTick, jfloat volume) {
	Track *track = getTrack(env, clazz, trackNum);
	MidiEvent *event = findEvent(track, onTick);
	if (event != NULL) {
		event->volume = volume;
	}
}

void Java_com_kh_beatbot_midi_MidiNote_setPan(JNIEnv *env, jclass clazz,
		jint trackNum, jlong onTick, jfloat pan) {
	Track *track = getTrack(env, clazz, trackNum);
	MidiEvent *event = findEvent(track, onTick);
	if (event != NULL) {
		event->pan = pan;
	}
}

void Java_com_kh_beatbot_midi_MidiNote_setPitch(JNIEnv *env, jclass clazz,
		jint trackNum, jlong onTick, jfloat pitch) {
	Track *track = getTrack(env, clazz, trackNum);
	MidiEvent *event = findEvent(track, onTick);
	if (event != NULL) {
		event->pitch = pitch;
	}
}

/****************************************************************************************
 Java SampleEditActivity JNI methods
 ****************************************************************************************/

void Java_com_kh_beatbot_SampleEditActivity_setPrimaryVolume(JNIEnv *env,
		jclass clazz, jint trackNum, jfloat volume) {
	Track *track = getTrack(env, clazz, trackNum);
	track->volume = volume;
}

void Java_com_kh_beatbot_SampleEditActivity_setPrimaryPan(JNIEnv *env,
		jclass clazz, jint trackNum, jfloat pan) {
	Track *track = getTrack(env, clazz, trackNum);
	track->pan = pan;
}

void Java_com_kh_beatbot_SampleEditActivity_setPrimaryPitch(JNIEnv *env,
		jclass clazz, jint trackNum, jfloat pitch) {
	Track *track = getTrack(env, clazz, trackNum);
	track->pitch = pitch;
	if (track->outputPlayerPitch != NULL) {
		//(*(track->outputPlayerPitch))->SetRate(track->outputPlayerPitch, (short)((track->pitch + track->primaryPitch)*750 + 500));
	}
}
