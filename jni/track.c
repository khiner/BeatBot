#include "all.h"

static jclass trackClass = NULL;
static jmethodID getNextMidiNote = NULL;
static JavaVM* javaVm = NULL;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_6) != JNI_OK)
		return -1;

	trackClass = (*env)->NewGlobalRef(env,
			(*env)->FindClass(env, "com/kh/beatbot/manager/TrackManager"));
	getNextMidiNote = (*env)->GetStaticMethodID(env, trackClass,
			"getNextMidiNote", "(IJ)Lcom/kh/beatbot/midi/MidiNote;");

	javaVm = vm;
	return JNI_VERSION_1_6;
}

JNIEnv *getJniEnv() {
	JNIEnv* env;
	if ((*javaVm)->GetEnv(javaVm, (void**) &env,
			JNI_VERSION_1_6) == JNI_EDETACHED) {
		(*javaVm)->AttachCurrentThread(javaVm, &env, NULL);
	}
	return env;
}

jfloatArray makejFloatArray(JNIEnv * env, float floatAry[], int size) {
	jfloatArray result = (*env)->NewFloatArray(env, size);
	(*env)->SetFloatArrayRegion(env, result, 0, size, floatAry);
	return result;
}

void printTracks(TrackNode *head) {
	__android_log_print(ANDROID_LOG_ERROR, "tracks", "Elements:");
	TrackNode *cur_ptr = head;
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

TrackNode *getTrackNode(int trackNum) {
	TrackNode *trackNode = trackHead;
	while (trackNode != NULL) {
		if (trackNode->track != NULL && trackNode->track->num == trackNum) {
			return trackNode;
		}
		trackNode = trackNode->next;
	}
	return NULL;
}

Track *getTrack(JNIEnv *env, jclass clazz, int trackNum) {
	(void *) env; // avoid warnings about unused paramaters
	(void *) clazz; // avoid warnings about unused paramaters
	TrackNode *trackNode = getTrackNode(trackNum);
	return trackNode->track;
}

Levels *getLevels(JNIEnv *env, jclass clazz, int trackNum) {
	if (trackNum == -1) {
		return masterLevels;
	}
	Track *track = getTrack(env, clazz, trackNum);
	return track->levels;
}

void addTrack(Track *track) {
	TrackNode *new = (TrackNode *) malloc(sizeof(TrackNode));
	new->track = track;
	new->next = NULL;
	// check for first insertion
	if (trackHead == NULL) {
		trackHead = new;
	} else {
		// insert as last effect
		TrackNode *cur_ptr = trackHead;
		while (cur_ptr->next != NULL) {
			cur_ptr = cur_ptr->next;
		}
		cur_ptr->next = new;
	}
}

TrackNode *removeTrack(int trackNum) {
	TrackNode *one_back;
	TrackNode *node = getTrackNode(trackNum);
	if (node == trackHead) {
		trackHead = trackHead->next;
	} else {
		one_back = trackHead;
		while (one_back->next != node) {
			one_back = one_back->next;
		}
		one_back->next = node->next;
	}
	return node;
}

void freeEffects(Levels *levels) {
	EffectNode *cur_ptr = levels->effectHead;
	while (cur_ptr != NULL) {
		cur_ptr->effect->destroy(cur_ptr->effect->config);
		EffectNode *prev_ptr = cur_ptr;
		cur_ptr = cur_ptr->next;
		free(prev_ptr); // free the entire Node
	}
}

void freeTracks() {
	// destroy all tracks
	TrackNode *cur_ptr = trackHead;
	while (cur_ptr != NULL) {
		free(cur_ptr->track->currBufferFloat[0]);
		free(cur_ptr->track->currBufferFloat[1]);
		free(cur_ptr->track->currBufferFloat);
		cur_ptr->track->generator->destroy(cur_ptr->track->generator->config);
		freeEffects(cur_ptr->track->levels);
		TrackNode *prev_ptr = cur_ptr;
		cur_ptr = cur_ptr->next;
		free(prev_ptr); // free the entire Node
	}
	freeEffects(masterLevels);
	free(openSlOut->currBufferFloat[0]);
	free(openSlOut->currBufferFloat[1]);
	free(openSlOut->currBufferFloat);
	free(openSlOut->currBufferShort);
	(*openSlOut->outputBufferQueue)->Clear(openSlOut->outputBufferQueue);
	openSlOut->outputBufferQueue = NULL;
	openSlOut->outputPlayerPlay = NULL;
}

Levels *initLevels() {
	Levels *levels = malloc(sizeof(Levels));
	pthread_mutex_init(&levels->effectMutex, NULL);
	levels->effectHead = NULL;
	levels->volume = .8f;
	levels->pan = levels->pitch = .5f;
	int effectNum;
	for (effectNum = 0; effectNum < MAX_EFFECTS_PER_TRACK; effectNum++) {
		addEffect(levels, NULL);
	}
	levels->volPan = initEffect(true, volumepanconfig_create(),
			volumepanconfig_set, volumepan_process, volumepanconfig_destroy);
	return levels;
}

Track *initTrack() {
	Track *track = malloc(sizeof(Track));
	track->num = trackCount;
	track->generator = NULL;
	track->levels = initLevels();
	track->nextEvent = malloc(sizeof(MidiEvent));
	track->nextStartSample = track->nextStopSample = -1;
	track->currBufferFloat = (float **) malloc(2 * sizeof(float *));
	track->currBufferFloat[0] = (float *) calloc(BUFF_SIZE, sizeof(float));
	track->currBufferFloat[1] = (float *) calloc(BUFF_SIZE, sizeof(float));
	track->playing = track->previewing = false;
	track->mute = track->solo = false;
	track->shouldSound = true;
	track->nextEvent->volume = .8f;
	track->nextEvent->pitch =
				track->nextEvent->pan = .5f;
	return track;
}

void initSample(Track *track, const char *sampleName) {
	track->generator = malloc(sizeof(Generator));
	initGenerator(track->generator, wavfile_create(sampleName), wavfile_reset,
			wavfile_generate, wavfile_destroy);
}

void setSample(Track *track, const char *sampleName) {
	WavFile *wavConfig = (WavFile *) track->generator->config;
	wavfile_setSampleFile(wavConfig, sampleName);
}

int getSoloingTrackNum() {
	TrackNode *cur_ptr = trackHead;
	while (cur_ptr != NULL) {
		if (cur_ptr->track->solo) {
			return cur_ptr->track->num;
		}
		cur_ptr = cur_ptr->next;
	}
	return -1;
}

void Java_com_kh_beatbot_global_Track_muteTrack(JNIEnv *env, jclass clazz,
		jint trackNum, jboolean mute) {
	Track *track = getTrack(env, clazz, trackNum);
	if (mute) {
		track->shouldSound = false;
	} else {
		int soloingTrackNum = getSoloingTrackNum();
		if (soloingTrackNum == -1 || soloingTrackNum == trackNum)
			track->shouldSound = true;
	}
	track->mute = mute;
}

void Java_com_kh_beatbot_global_Track_soloTrack(JNIEnv *env, jclass clazz,
		jint trackNum, jboolean solo) {
	Track *track = getTrack(env, clazz, trackNum);
	track->solo = solo;
	if (solo) {
		if (!track->mute) {
			track->shouldSound = true;
		}
		TrackNode *cur_ptr = trackHead;
		while (cur_ptr != NULL) {
			if (cur_ptr->track->num != trackNum) {
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

	long onSample = tickToSample(
			(*env)->CallLongMethod(env, obj,
					(*env)->GetMethodID(env, cls, "getOnTick", "()J")));
	long offSample = tickToSample(
			(*env)->CallLongMethod(env, obj,
					(*env)->GetMethodID(env, cls, "getOffTick", "()J")));
	float vol = (*env)->CallFloatMethod(env, obj,
			(*env)->GetMethodID(env, cls, "getVelocity", "()F"));
	float pan = (*env)->CallFloatMethod(env, obj,
			(*env)->GetMethodID(env, cls, "getPan", "()F"));
	float pitch = (*env)->CallFloatMethod(env, obj,
			(*env)->GetMethodID(env, cls, "getPitch", "()F"));

	setNextNoteInfo(track, onSample, offSample, vol, pan, pitch);
	(*env)->DeleteLocalRef(env, cls);
}

void updateNextNote(Track *track) {
	JNIEnv* env = getJniEnv();
	jobject obj = (*env)->CallStaticObjectMethod(env, trackClass,
			getNextMidiNote, track->num, (jlong) sampleToTick(currSample));
	setNextNote(track, obj);
	(*env)->DeleteLocalRef(env, obj);
}

void Java_com_kh_beatbot_global_Track_setNextNote(JNIEnv *env, jclass clazz,
		jint trackNum, jobject midiNote) {
	Track *track = getTrack(env, clazz, trackNum);
	setNextNote(track, midiNote);
}

void updateLevels(int trackNum) {
	if (trackNum == -1)
		return; // master track - no update needed

	Track *track = getTrack(NULL, NULL, trackNum);
	MidiEvent *midiEvent = track->nextEvent;
	if (masterLevels == NULL)
		__android_log_print(ANDROID_LOG_ERROR, "in UL", "master levels is NULL!");
	__android_log_print(ANDROID_LOG_ERROR, "in UL", "mp = %f", masterLevels->pan);
	track->levels->volPan->set(track->levels->volPan->config, midiEvent->volume,
			(masterLevels->pan + track->levels->pan + midiEvent->pan) / 3);
	((WavFile *) track->generator->config)->sampleRate = masterLevels->pitch
			* track->levels->pitch * midiEvent->pitch * 8;
}

void updateAllLevels() {
	TrackNode *cur_ptr = trackHead;
	while (cur_ptr != NULL) {
		updateLevels(cur_ptr->track->num);
		cur_ptr = cur_ptr->next;
	}
}

void Java_com_kh_beatbot_global_BaseTrack_setTrackVolume(JNIEnv *env,
		jclass clazz, jint trackNum, jfloat volume) {
	Levels *levels = getLevels(env, clazz, trackNum);
	levels->volume = volume;
	updateLevels(trackNum);
}

void Java_com_kh_beatbot_global_BaseTrack_setTrackPan(JNIEnv *env, jclass clazz,
		jint trackNum, jfloat pan) {
	Levels *levels = getLevels(env, clazz, trackNum);
	levels->pan = pan;
	updateLevels(trackNum);
}

void Java_com_kh_beatbot_global_BaseTrack_setTrackPitch(JNIEnv *env,
		jclass clazz, jint trackNum, jfloat pitch) {
	Levels *levels = getLevels(env, clazz, trackNum);
	levels->pitch = pitch;
	updateLevels(trackNum);
}

void Java_com_kh_beatbot_global_Track_setAdsrOn(JNIEnv *env, jclass clazz,
		jint trackNum, jboolean on) {
	Track *track = getTrack(env, clazz, trackNum);
	((WavFile *) track->generator->config)->adsr->active = on;
}

void Java_com_kh_beatbot_global_Track_setAdsrPoint(JNIEnv *env, jclass clazz,
		jint trackNum, jint adsrPointNum, jfloat x, jfloat y) {
	Track *track = getTrack(env, clazz, trackNum);
	AdsrConfig *config = ((WavFile *) track->generator->config)->adsr;
	config->adsrPoints[adsrPointNum].sampleCents = x;
	if (adsrPointNum == 0)
		config->initial = y;
	else if (adsrPointNum == 1)
		config->peak = y;
	else if (adsrPointNum == 2)
		config->sustain = y;
	else if (adsrPointNum == 4)
		config->end = y + 0.00001f;
	updateAdsr(config, config->totalSamples);
}

void Java_com_kh_beatbot_global_Track_setSample(JNIEnv *env, jclass clazz,
		jint trackNum, jstring sampleName) {
	Track *track = getTrack(env, clazz, trackNum);

	const char *nativeSampleName = (*env)->GetStringUTFChars(env, sampleName,
			0);

	if (track->generator == NULL) {
		initSample(track, nativeSampleName);
	} else {
		setSample(track, nativeSampleName);
	}

	// release string memory
	(*env)->ReleaseStringUTFChars(env, sampleName, nativeSampleName);
}

void Java_com_kh_beatbot_manager_TrackManager_addTrack(JNIEnv *env,
		jclass clazz, jstring sampleName) {
	Track *track = initTrack();
	pthread_mutex_lock(&openSlOut->trackMutex);
	addTrack(track);
	Java_com_kh_beatbot_global_Track_setSample(env, clazz, trackCount,
			sampleName);
	pthread_mutex_unlock(&openSlOut->trackMutex);
	trackCount++;
}

void Java_com_kh_beatbot_global_Track_toggleTrackLooping(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *) track->generator->config;
	wavFile->looping = !wavFile->looping;
}

jboolean Java_com_kh_beatbot_global_Track_isTrackLooping(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *) track->generator->config;
	return wavFile->looping;
}

jboolean Java_com_kh_beatbot_global_Track_isTrackPlaying(JNIEnv *env,
		jclass clazz, jint trackNum) {
	Track *track = getTrack(env, clazz, trackNum);
	return track->playing;
}

void Java_com_kh_beatbot_global_Track_setTrackLoopWindow(JNIEnv *env,
		jclass clazz, jint trackNum, jlong loopBeginSample, jlong loopEndSample) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *) track->generator->config;
	wavfile_setLoopWindow(wavFile, loopBeginSample, loopEndSample);
}

void Java_com_kh_beatbot_global_Track_setTrackReverse(JNIEnv *env, jclass clazz,
		jint trackNum, jboolean reverse) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *) track->generator->config;
	wavfile_setReverse(wavFile, reverse);
}
