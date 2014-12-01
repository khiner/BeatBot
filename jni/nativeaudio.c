#include "all.h"

// __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");

// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine = NULL;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;

static bool playing = false;
static bool recording = false;
static FILE *recordOutFile = NULL;
static pthread_mutex_t recordMutex, bufferFillMutex;
static pthread_cond_t bufferFillCond = PTHREAD_COND_INITIALIZER;
static JNIEnv *env;

bool isPlaying() {
	return playing;
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
	short scaled;
	for (i = 0; i < size; i++) {
		// write the chars of the short to file, little endian
		scaled = buffer[i] * 2;
		fputc((char) scaled & 0xff, out);
		fputc((char) (scaled >> 8) & 0xff, out);
	}
}

static inline void processEffects(Levels *levels, float **floatBuffer) {
	levels->volPan->process(levels->volPan->config, floatBuffer, BUFF_SIZE);
	pthread_mutex_lock(&levels->effectMutex);
	EffectNode *effectNode = levels->effectHead;
	while (effectNode != NULL ) {
		if (effectNode->effect != NULL && effectNode->effect->on) {
			effectNode->effect->process(effectNode->effect->config, floatBuffer,
					BUFF_SIZE);
		}
		effectNode = effectNode->next;
	}
	pthread_mutex_unlock(&levels->effectMutex);
}

static inline void processEffectsForAllTracks() {
	TrackNode *cur_ptr = trackHead;
	while (cur_ptr != NULL ) {
		processEffects(cur_ptr->track->levels, cur_ptr->track->currBufferFloat);
		cur_ptr = cur_ptr->next;
	}
}

static inline void processMasterEffects() {
	processEffects(masterLevels, openSlOut->currBufferFloat);
}

static inline void mixTracks() {
	openSlOut->maxFrameInCurrentBuffer = 0;
	int channel, samp;
	float total;
	for (channel = 0; channel < 2; channel++) {
		for (samp = 0; samp < BUFF_SIZE; samp++) {
			total = 0;
			TrackNode *cur_ptr = trackHead;
			while (cur_ptr != NULL ) {
				if (cur_ptr->track->shouldSound) {
					total += cur_ptr->track->currBufferFloat[channel][samp]
							* cur_ptr->track->levels->volume;
				}
				cur_ptr = cur_ptr->next;
			}
			total *= masterLevels->volume;
			openSlOut->currBufferFloat[channel][samp] =
					total > -1 ? (total < 1 ? total : 1) : -1;
			if (total > openSlOut->maxFrameInCurrentBuffer) {
				openSlOut->maxFrameInCurrentBuffer = total;
			}
		}
	}
}

void Java_com_kh_beatbot_Track_previewTrack(JNIEnv *env, jclass clazz,
		jint trackNum) {
	previewTrack(getTrack(env, clazz, trackNum));
}

void Java_com_kh_beatbot_Track_stopPreviewingTrack(JNIEnv *env, jclass clazz,
		jint trackNum) {
	stopPreviewingTrack(getTrack(env, clazz, trackNum));
}

void Java_com_kh_beatbot_Track_stopTrack(JNIEnv *env, jclass clazz,
		jint trackNum) {
	stopTrack(getTrack(env, clazz, trackNum));
}

void stopAllTracks() {
	currSample = loopBeginSample;
	TrackNode *cur_ptr = trackHead;
	while (cur_ptr != NULL ) {
		stopTrack(cur_ptr->track);
		cur_ptr = cur_ptr->next;
	}
}

void disarm() {
	stopAllTracks();
	openSlOut->armed = false;
}

static inline void generateNextBuffer() {
	int samp, channel;
	for (samp = 0; samp < BUFF_SIZE; samp++) {
		if (currSample > loopEndSample) {
			stopAllTracks();
		}
		TrackNode *cur_ptr = trackHead;
		while (cur_ptr != NULL ) {
			Track *track = cur_ptr->track;
			if (playing && currSample == track->nextStartSample) {
				playTrack(track);
			} else if (currSample == track->nextStopSample) {
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
			currSample++;
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
			openSlOut->currBufferFloat[1], openSlOut->currBufferShort,
			BUFF_SIZE);
	pthread_mutex_unlock(&openSlOut->trackMutex);
}

// this callback handler is called every time a buffer finishes playing
void bufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
	// enqueue the buffer
	if (openSlOut->armed) {
		(*bq)->Enqueue(bq, openSlOut->currBufferShort,
				BUFF_SIZE * 2 * sizeof(short));
	}

	fillBuffer();

	// write to record out file if recording
	if (recording && recordOutFile != NULL
			&& openSlOut->recordBufferShort == openSlOut->currBufferShort) {
		pthread_mutex_lock(&recordMutex);
		writeBytesToFile(openSlOut->currBufferShort, BUFF_SIZE * 2,
				recordOutFile);
		pthread_mutex_unlock(&recordMutex);
	}
}

// this callback handler is called every time a buffer finishes playing
void micBufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
	// enqueue the buffer
	(*bq)->Enqueue(bq, openSlOut->micBufferShort,
			BUFF_SIZE * 2 * sizeof(short));

	// write to record out file if recording
	if (recording && recordOutFile != NULL
			&& openSlOut->recordBufferShort == openSlOut->micBufferShort) {
		pthread_mutex_lock(&recordMutex);
		writeBytesToFile(openSlOut->micBufferShort, BUFF_SIZE * 2,
				recordOutFile);
		pthread_mutex_unlock(&recordMutex);
	}
}

void setRecordBuffer(short *recordBuffer) {
	pthread_mutex_lock(&recordMutex);
	openSlOut->recordBufferShort = recordBuffer;
	pthread_mutex_unlock(&recordMutex);
}

void Java_com_kh_beatbot_manager_PlaybackManager_playNative(JNIEnv *env,
		jclass clazz) {
	stopAllTracks();
	playing = true;
}

void Java_com_kh_beatbot_manager_PlaybackManager_stopNative(JNIEnv *env,
		jclass clazz) {
	playing = false;
	stopAllTracks();
}

// create the engine and output mix objects
void Java_com_kh_beatbot_activity_BeatBotActivity_createEngine(JNIEnv *env,
		jclass clazz) {
	SLresult result;
	(void *) env;
	(void *) clazz; // avoid warnings about unused paramaters
	initTicker();

	// create engine
	result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL );

	// realize the engine
	result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE );

	// get the engine interface, which is needed in order to create other objects
	result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE,
			&engineEngine);

	// create output mix, with volume specified as a non-required interface
	const SLInterfaceID ids[1] = { SL_IID_VOLUME };
	const SLboolean req[1] = { SL_BOOLEAN_FALSE };
	result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1,
			ids, req);

	// realize the output mix
	result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE );
	trackHead = NULL;
	masterLevels = initLevels();
	previewEvent = malloc(sizeof(MidiEvent));
	previewEvent->volume = .8f;
	previewEvent->pitch = previewEvent->pan = .5f;
}

jboolean Java_com_kh_beatbot_activity_BeatBotActivity_createAudioPlayer(
		JNIEnv *env, jclass clazz) {
	openSlOut = malloc(sizeof(OpenSlOut));
	openSlOut->currBufferFloat = (float **) malloc(2 * sizeof(float *));
	openSlOut->currBufferFloat[0] = (float *) calloc(BUFF_SIZE, sizeof(float));
	openSlOut->currBufferFloat[1] = (float *) calloc(BUFF_SIZE, sizeof(float));
	memset(openSlOut->currBufferShort, 0, sizeof(openSlOut->currBufferShort));
	memset(openSlOut->micBufferShort, 0, sizeof(openSlOut->micBufferShort));
	setRecordBuffer(openSlOut->currBufferShort);

	openSlOut->armed = false;
	pthread_mutex_init(&openSlOut->trackMutex, NULL );

	// configure audio sink
	SLDataLocator_OutputMix outputMix = { SL_DATALOCATOR_OUTPUTMIX,
			outputMixObject };
	SLDataSink outputAudioSink = { &outputMix, NULL };

	// config audio source for output buffer (source is a SimpleBufferQueue)
	SLDataLocator_AndroidSimpleBufferQueue outputBufferQueue = {
			SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2 };
	SLDataSource outputAudioSrc = { &outputBufferQueue, &format_pcm };

	// create audio player for output buffer queue
	const SLInterfaceID ids1[] = { SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
			SL_IID_PLAYBACKRATE, SL_IID_MUTESOLO };
	const SLboolean req[] = { SL_BOOLEAN_TRUE };
	(*engineEngine)->CreateAudioPlayer(engineEngine,
			&(openSlOut->outputPlayerObject), &outputAudioSrc, &outputAudioSink,
			3, ids1, req);

	// realize the output player
	(*(openSlOut->outputPlayerObject))->Realize(openSlOut->outputPlayerObject,
			SL_BOOLEAN_FALSE );

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
			SL_PLAYSTATE_PLAYING );

	SLDataLocator_IODevice loc_dev = { SL_DATALOCATOR_IODEVICE,
			SL_IODEVICE_AUDIOINPUT, SL_DEFAULTDEVICEID_AUDIOINPUT, NULL };
	SLDataSource audioSrc = { &loc_dev, NULL };
	SLDataSink recordAudioSink = { &outputBufferQueue, &format_pcm };

	// XXX might be able to reuse above
	const SLInterfaceID id[1] = { SL_IID_ANDROIDSIMPLEBUFFERQUEUE };

	(*engineEngine)->CreateAudioRecorder(engineEngine,
			&(openSlOut->recorderObject), &audioSrc, &recordAudioSink, 1, id,
			req);

	(*openSlOut->recorderObject)->Realize(openSlOut->recorderObject,
			SL_BOOLEAN_FALSE );

	(*openSlOut->recorderObject)->GetInterface(openSlOut->recorderObject,
			SL_IID_RECORD, &(openSlOut->recordInterface));

	(*openSlOut->recorderObject)->GetInterface(openSlOut->recorderObject,
			SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &(openSlOut->micBufferQueue));

	(*openSlOut->micBufferQueue)->RegisterCallback(openSlOut->micBufferQueue,
			micBufferQueueCallback, openSlOut);

	return JNI_TRUE;
}

void Java_com_kh_beatbot_activity_BeatBotActivity_arm(JNIEnv *_env,
		jclass clazz) {
	if (openSlOut->armed)
		return; // only need to arm once
	env = _env;
	openSlOut->armed = true;
	// we need to fill the buffer once before calling the OpenSL callback
	fillBuffer();
	bufferQueueCallback(openSlOut->outputBufferQueue, NULL );
}

// shut down the native audio system
void Java_com_kh_beatbot_activity_BeatBotActivity_nativeShutdown(JNIEnv *env,
		jclass clazz) {
	playing = false;
	stopAllTracks();

	// lock the mutex, so openSL doesn't try to grab from empty buffers
	pthread_mutex_lock(&openSlOut->trackMutex);

	if (openSlOut->outputBufferQueue != NULL ) {
		(*openSlOut->outputBufferQueue)->Clear(openSlOut->outputBufferQueue);
		openSlOut->outputBufferQueue = NULL;
	}

	if (openSlOut->outputPlayerObject != NULL ) {
		(*openSlOut->outputPlayerObject)->Destroy(
				openSlOut->outputPlayerObject);
		openSlOut->outputPlayerPlay = NULL;
	}

	if (openSlOut->micBufferQueue != NULL ) {
		(*openSlOut->micBufferQueue)->Clear(openSlOut->micBufferQueue);
		openSlOut->micBufferQueue = NULL;
	}

	if (openSlOut->recorderObject != NULL ) {
		(*openSlOut->recorderObject)->Destroy(openSlOut->recorderObject);
		openSlOut->recorderObject = NULL;
	}

	// destroy output mix object, and invalidate all associated interfaces
	if (outputMixObject != NULL ) {
		(*outputMixObject)->Destroy(outputMixObject);
		outputMixObject = NULL;
	}
	// destroy engine object, and invalidate all associated interfaces
	if (engineObject != NULL ) {
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
void Java_com_kh_beatbot_manager_RecordManager_startRecordingNative(JNIEnv *env,
		jclass clazz, jstring recordFilePath) {
	pthread_mutex_lock(&recordMutex);
	const char *cRecordFilePath = (*env)->GetStringUTFChars(env, recordFilePath,
			0);
	cRecordFilePath = (*env)->GetStringUTFChars(env, recordFilePath, 0);
	// append to end of file, since header is written in Java
	recordOutFile = fopen(cRecordFilePath, "a+");
	recording = true;
	pthread_mutex_unlock(&recordMutex);

	if (openSlOut->recordBufferShort == openSlOut->micBufferShort) {
		// record from microphone.
		SLuint32 state;
		(*openSlOut->recordInterface)->GetRecordState(
				openSlOut->recordInterface, &state);
		// check for good state (should be stopped before recording)
		if (state != SL_RECORDSTATE_RECORDING ) {
			(*openSlOut->recordInterface)->SetRecordState(
					openSlOut->recordInterface, SL_RECORDSTATE_RECORDING );
			micBufferQueueCallback(openSlOut->micBufferQueue, NULL );
		}
	}
}

void Java_com_kh_beatbot_manager_RecordManager_stopRecordingNative(JNIEnv *env,
		jclass clazz) {
	pthread_mutex_lock(&recordMutex);
	// stop recording
	recording = false;
	// file cleanup
	fflush(recordOutFile);
	fclose(recordOutFile);
	recordOutFile = NULL;

	if (openSlOut->recordBufferShort == openSlOut->micBufferShort) {
		// currently recording from microphone.
		SLuint32 state;
		(*openSlOut->recordInterface)->GetRecordState(
				openSlOut->recordInterface, &state);
		// if we're recording, stop
		if (state == SL_RECORDSTATE_RECORDING ) {
			(*openSlOut->recordInterface)->SetRecordState(
					openSlOut->recordInterface, SL_RECORDSTATE_STOPPED );
		}
	}
	pthread_mutex_unlock(&recordMutex);
}

void Java_com_kh_beatbot_manager_RecordManager_setRecordSourceNative(
		JNIEnv *_env, jclass clazz, jint recordSourceId) {
	env = _env;
	switch (recordSourceId) {
	case RECORD_SOURCE_GLOBAL:
		setRecordBuffer(openSlOut->currBufferShort);
		break;
	case RECORD_SOURCE_MICROPHONE:
		setRecordBuffer(openSlOut->micBufferShort);
		break;
	}
}

jfloat Java_com_kh_beatbot_manager_RecordManager_getMaxFrameInRecordSourceBuffer(
		JNIEnv *env, jclass clazz) {
	return openSlOut->maxFrameInCurrentBuffer;
}
