#include "all.h"

// __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");

// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine = NULL;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;

static bool recording = false;
static FILE* recordOutFile = NULL;
static pthread_mutex_t recordMutex, bufferFillMutex;
static pthread_cond_t bufferFillCond = PTHREAD_COND_INITIALIZER;

static jclass midiClass = NULL;
static jmethodID getNextMidiNote = NULL;
static JavaVM* javaVm = NULL;
static JNIEnv* currEnv = NULL;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_6) != JNI_OK)
		return -1;

	midiClass = (*env)->NewGlobalRef(env,
			(*env)->FindClass(env, "com/kh/beatbot/manager/MidiManager"));
	getNextMidiNote = (*env)->GetStaticMethodID(env, midiClass,
			"getNextMidiNote", "(IJ)Lcom/kh/beatbot/midi/MidiNote;");

	javaVm = vm;
	return JNI_VERSION_1_6;
}

JNIEnv *getJniEnv() {
	JNIEnv* env;
	if ((*javaVm)->GetEnv(javaVm, (void**) &env,
			JNI_VERSION_1_6) == JNI_EDETACHED) {
		(*javaVm)->DetachCurrentThread(NULL);
		(*javaVm)->AttachCurrentThread(javaVm, &env, NULL);
	}
	currEnv = env;
	return env;
}

static inline void interleaveFloatsToShorts(float left[], float right[],
		short interleaved[], int size) {
	int i;
	for (i = 0; i < size; i++) {
		interleaved[i * 2] = left[i] * CONV16BIT;
		interleaved[i * 2 + 1] = right[i] * CONV16BIT;
	}
}

static inline void writeBytesToFile(short buffer[], int size, FILE *out) {
	int i = 0;
	for (i = 0; i < size; i++) {
		// write the chars of the short to file, little endian
		fputc((char) buffer[i] & 0xff, out);
		fputc((char) (buffer[i] >> 8) & 0xff, out);
	}
}

static inline void processEffects(Track *track) {
	track->volPan->process(track->volPan->config, track->currBufferFloat,
			BUFF_SIZE);
	pthread_mutex_lock(&track->effectMutex);
	EffectNode *effectNode = track->effectHead;
	while (effectNode != NULL) {
		if (effectNode->effect != NULL && effectNode->effect->on) {
			effectNode->effect->process(effectNode->effect->config,
					track->currBufferFloat, BUFF_SIZE);
		}
		effectNode = effectNode->next;
	}
	pthread_mutex_unlock(&track->effectMutex);
}

static inline void processEffectsForAllTracks() {
	TrackNode *cur_ptr = trackHead;
	while (cur_ptr != NULL) {
		processEffects(cur_ptr->track);
		cur_ptr = cur_ptr->next;
	}
}

static inline void mixTracks() {
	int channel, samp;
	float total;
	for (channel = 0; channel < 2; channel++) {
		for (samp = 0; samp < BUFF_SIZE; samp++) {
			total = 0;
			TrackNode *cur_ptr = trackHead;
			while (cur_ptr != NULL) {
				if (cur_ptr->track->shouldSound) {
					total += cur_ptr->track->currBufferFloat[channel][samp]
							* cur_ptr->track->primaryVolume;
				}
				cur_ptr = cur_ptr->next;
			}
			total *= masterVolume;
			openSlOut->currBufferFloat[channel][samp] =
					total  > -1 ? (total < 1 ? total : 1) : -1;
		}
	}
	// combine the two channels of floats into one buffer of shorts,
	// interleaving L and R samples
	interleaveFloatsToShorts(openSlOut->currBufferFloat[0],
			openSlOut->currBufferFloat[1], openSlOut->currBufferShort,
			BUFF_SIZE);
}

void setNextNoteInfo(Track *track, jlong onTick, jlong offTick, jfloat vol,
		jfloat pan, jfloat pitch) {
	track->nextStartSample = onTick;
	track->nextStopSample = offTick;
	track->nextEvent->volume = vol;
	track->nextEvent->pan = pan;
	track->nextEvent->pitch = pitch;
}

void setNextNote(Track *track, jobject obj) {
	JNIEnv* env = getJniEnv();
	if (obj == NULL) {
		track->nextStartSample = -1;
		track->nextStopSample = -1;
		return;
	}
	jclass cls = (*env)->GetObjectClass(env, obj);

	long onTick = tickToSample(
			(*env)->CallLongMethod(env, obj,
					(*env)->GetMethodID(env, cls, "getOnTick", "()J")));
	long offTick = tickToSample(
			(*env)->CallLongMethod(env, obj,
					(*env)->GetMethodID(env, cls, "getOffTick", "()J")));
	float vol = (*env)->CallFloatMethod(env, obj,
			(*env)->GetMethodID(env, cls, "getVelocity", "()F"));
	float pan = (*env)->CallFloatMethod(env, obj,
			(*env)->GetMethodID(env, cls, "getPan", "()F"));
	float pitch = (*env)->CallFloatMethod(env, obj,
			(*env)->GetMethodID(env, cls, "getPitch", "()F"));

	setNextNoteInfo(track, onTick, offTick, vol, pan, pitch);
	(*env)->DeleteLocalRef(env, cls);
}

void updateNextNote(Track *track) {
	JNIEnv* env = getJniEnv();
	jobject obj = (*env)->CallStaticObjectMethod(env, midiClass,
			getNextMidiNote, track->num, (jlong) sampleToTick(currSample));
	setNextNote(track, obj);
	(*env)->DeleteLocalRef(env, obj);
}

void soundTrack(Track *track) {
	updateLevels(track);
	AdsrConfig *adsrConfig = ((WavFile *)track->generator->config)->adsr;
	adsrConfig->active = true;
	resetAdsr(adsrConfig);
}

void playTrack(Track *track) {
	track->playing = true;
	soundTrack(track);
}

void stopSoundingTrack(Track *track) {
	wavfile_reset((WavFile *) track->generator->config);
}

void stopTrack(Track *track) {
	// update next track
	updateNextNote(track);
	if (!track->playing)
		return;
	track->playing = false;
	stopSoundingTrack(track);
}

void previewTrack(int trackNum) {
	Track *track = getTrack(NULL, NULL, trackNum);
	track->previewing = true;
	if (!track->playing) {
		soundTrack(track);
	}
}

void stopPreviewingTrack(int trackNum) {
	Track *track = getTrack(NULL, NULL, trackNum);
	track->previewing = false;
	if (!track->playing)
		stopSoundingTrack(track);
}

void Java_com_kh_beatbot_global_Track_playTrack(JNIEnv *env, jclass clazz,
		jint trackNum) {
	previewTrack(trackNum);
}

void Java_com_kh_beatbot_global_Track_stopTrack(JNIEnv *env, jclass clazz,
		jint trackNum) {
	stopPreviewingTrack(trackNum);
}

void stopAllTracks() {
	TrackNode *cur_ptr = trackHead;
	while (cur_ptr != NULL) {
		stopTrack(cur_ptr->track);
		cur_ptr = cur_ptr->next;
	}
}

void Java_com_kh_beatbot_manager_PlaybackManager_disarmAllTracks(JNIEnv *env,
		jclass clazz) {
	TrackNode *cur_ptr = trackHead;
	stopAllTracks();
	while (cur_ptr != NULL) {
		cur_ptr->track->armed = false;
		cur_ptr = cur_ptr->next;
	}
	openSlOut->armed = openSlOut->anyTrackArmed = false;
}

static inline void generateNextBuffer() {
	int samp, channel;
	for (samp = 0; samp < BUFF_SIZE; samp++) {
		if (currSample > loopEndSample) {
			stopAllTracks();
			currSample = loopBeginSample;
		}
		TrackNode *cur_ptr = trackHead;
		while (cur_ptr != NULL) {
			Track *track = cur_ptr->track;
			if (!track->armed) {
				cur_ptr = cur_ptr->next;
				continue;
			}
			if (currSample == track->nextStartSample) {
				playTrack(track);
			} else if (currSample == track->nextStopSample) {
				stopTrack(track);
			}
			if (track->playing || track->previewing) {
				wavfile_tick((WavFile *) track->generator->config,
						track->tempSample);
			} else {
				track->tempSample[0] = track->tempSample[1] = 0;
			}
			for (channel = 0; channel < 2; channel++) {
				track->currBufferFloat[channel][samp] =
						track->tempSample[channel];
			}
			cur_ptr = cur_ptr->next;
		}
		if (openSlOut->armed)
			currSample++;
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
	pthread_mutex_unlock(&openSlOut->trackMutex);
}

// this callback handler is called every time a buffer finishes playing
void bufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
	// enqueue the buffer
	if (openSlOut->anyTrackArmed) {
		(*bq)->Enqueue(bq, openSlOut->currBufferShort,
				BUFF_SIZE * 2 * sizeof(short));
	}
	// fill the buffer
	fillBuffer();
	// write to wav file if recording
	if (recording && recordOutFile != NULL) {
		pthread_mutex_lock(&recordMutex);
		writeBytesToFile(openSlOut->currBufferShort, BUFF_SIZE * 2,
				recordOutFile);
		pthread_mutex_unlock(&recordMutex);
	}
}

void Java_com_kh_beatbot_global_Track_armTrack(JNIEnv *env, jclass clazz,
		jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	track->armed = openSlOut->anyTrackArmed = true;
	// start writing zeros to the track's audio out
	bufferQueueCallback(openSlOut->outputBufferQueue, NULL);
}

void Java_com_kh_beatbot_manager_PlaybackManager_armAllTracks(JNIEnv *env,
		jclass clazz) {
	// arm each track, and
	// trigger buffer queue callback to begin writing data to tracks
	TrackNode *cur_ptr = trackHead;
	while (cur_ptr != NULL) {
		Track *track = cur_ptr->track;
		track->armed = true;
		cur_ptr = cur_ptr->next;
	}
	openSlOut->armed = openSlOut->anyTrackArmed = true;
	// we need to fill the buffer once before calling the OpenSL callback
	fillBuffer();
	bufferQueueCallback(openSlOut->outputBufferQueue, NULL);
}

// create the engine and output mix objects
void Java_com_kh_beatbot_activity_BeatBotActivity_createEngine(JNIEnv *env,
		jclass clazz) {
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
	trackHead = NULL;
	//pthread_create(&bufferFillThread, NULL, fillBuffer, (void *)bufferFillThreadId);
}

jboolean Java_com_kh_beatbot_activity_BeatBotActivity_createAudioPlayer(
		JNIEnv *env, jclass clazz) {
	openSlOut = malloc(sizeof(OpenSlOut));
	openSlOut->currBufferFloat = (float **) malloc(2 * sizeof(float *));
	openSlOut->currBufferFloat[0] = (float *) calloc(BUFF_SIZE, sizeof(float));
	openSlOut->currBufferFloat[1] = (float *) calloc(BUFF_SIZE, sizeof(float));
	memset(openSlOut->currBufferShort, 0, sizeof(openSlOut->currBufferShort));
	openSlOut->armed = openSlOut->anyTrackArmed = false;
	pthread_mutex_init(&openSlOut->trackMutex, NULL);

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
	(*engineEngine)->CreateAudioPlayer(engineEngine,
			&(openSlOut->outputPlayerObject), &outputAudioSrc, &audioSnk, 3,
			ids1, req1);

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

	return JNI_TRUE;
}

void Java_com_kh_beatbot_activity_BeatBotActivity_setMasterVolume(JNIEnv *env,
		jclass clazz, jfloat level) {
	masterVolume = level;
	updateAllLevels();
}

void Java_com_kh_beatbot_activity_BeatBotActivity_setMasterPan(JNIEnv *env,
		jclass clazz, jfloat level) {
	masterPan = level;
	updateAllLevels();
}

void Java_com_kh_beatbot_activity_BeatBotActivity_setMasterPitch(JNIEnv *env,
		jclass clazz, jfloat level) {
	masterPitch = level;
	updateAllLevels();
}

// shut down the native audio system
void Java_com_kh_beatbot_activity_BeatBotActivity_shutdown(JNIEnv *env,
		jclass clazz) {
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
	pthread_mutex_destroy(&bufferFillMutex);
	pthread_cond_destroy(&bufferFillCond);
}

/****************************************************************************************
 Java MidiManager JNI methods
 ****************************************************************************************/

void Java_com_kh_beatbot_manager_MidiManager_setNextNote(JNIEnv *env,
		jclass clazz, jint trackNum, jobject midiNote) {
	Track *track = getTrack(env, clazz, trackNum);
	setNextNote(track, midiNote);
}

/****************************************************************************************
 Java RecordManager JNI methods
 ****************************************************************************************/
void Java_com_kh_beatbot_manager_RecordManager_startRecordingNative(JNIEnv *env,
		jclass clazz, jstring recordFilePath) {
	const char *cRecordFilePath = (*env)->GetStringUTFChars(env, recordFilePath,
			0);
	// append to end of file, since header is written in Java
	recordOutFile = fopen(cRecordFilePath, "a+");
	recording = true;
}

void Java_com_kh_beatbot_manager_RecordManager_stopRecordingNative(JNIEnv *env,
		jclass clazz) {
	// stop recording
	recording = false;
	// file cleanup
	pthread_mutex_lock(&recordMutex);
	fflush(recordOutFile);
	fclose(recordOutFile);
	recordOutFile = NULL;
	pthread_mutex_unlock(&recordMutex);
}
