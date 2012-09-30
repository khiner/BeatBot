#include "track.h"
#include "effects/adsr.h"
#include "effects/volpan.h"
#include "effects/pitch.h"

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

void addEffect(Track *track, Effect *effect) {
	EffectNode *new = (EffectNode *) malloc(sizeof(EffectNode));
	new->effect = effect;
	new->next = NULL;
	pthread_mutex_lock(&track->effectMutex);
	// check for first insertion
	if (track->effectHead == NULL) {
		track->effectHead = new;
	} else {
		// insert as last effect
		EffectNode *cur_ptr = track->effectHead;
		while (cur_ptr->next != NULL) {
			cur_ptr = cur_ptr->next;
		}
		cur_ptr->next = new;
	}
	pthread_mutex_unlock(&track->effectMutex);
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

void freeMidiEvents(Track *track) {
	MidiEventNode *cur_ptr = track->eventHead;
	while (cur_ptr != NULL) {
		free(cur_ptr->event); // free the event
		MidiEventNode *prev_ptr = cur_ptr;
		cur_ptr = cur_ptr->next;
		free(prev_ptr); // free the entire Node
	}
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

void freeEffects(Track *track) {
	EffectNode *cur_ptr = track->effectHead;
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
		freeEffects(cur_ptr->track);
		freeMidiEvents(cur_ptr->track);
		TrackNode *prev_ptr = cur_ptr;
		cur_ptr = cur_ptr->next;
		free(prev_ptr); // free the entire Node
	}
	free(openSlOut->currBufferFloat[0]);
	free(openSlOut->currBufferFloat[1]);
	free(openSlOut->currBufferFloat);
	free(openSlOut->currBufferShort);
	(*openSlOut->outputBufferQueue)->Clear(openSlOut->outputBufferQueue);
	openSlOut->outputBufferQueue = NULL;
	openSlOut->outputPlayerPlay = NULL;
}

Track *initTrack() {
	Track *track = malloc(sizeof(Track));
	// asset->getLength() returns size in bytes.  need size in shorts, minus 22 shorts of .wav header
	pthread_mutex_init(&track->effectMutex, NULL);
	track->num = trackCount;
	track->eventHead = NULL;
	track->effectHead = NULL;
	track->generator = NULL;
	track->nextEventNode = NULL;
	track->nextStartSample = track->nextStopSample = -1;
	track->currBufferFloat = (float **) malloc(2 * sizeof(float *));
	track->currBufferFloat[0] = (float *) calloc(BUFF_SIZE, sizeof(float));
	track->currBufferFloat[1] = (float *) calloc(BUFF_SIZE, sizeof(float));
	track->armed = false;
	track->playing = track->previewing = false;
	track->mute = track->solo = false;
	track->shouldSound = true;
	track->primaryVolume = track->noteVolume = .8f;
	track->primaryPan = track->primaryPitch = track->notePan =
			track->notePitch = .5f;
	int effectNum;
	for (effectNum = 0; effectNum < 4; effectNum++) {
		addEffect(track, NULL);
	}
	track->volPan = initEffect(-1, true, volumepanconfig_create(),
			volumepanconfig_set, volumepan_process, volumepanconfig_destroy);
	track->pitch = initEffect(-1, false, pitchconfig_create(),
			pitchconfig_setShift, pitch_process, pitchconfig_destroy);
	return track;
}

void initSampleBytes(Track *track, char *bytes, int length) {
	track->generator = malloc(sizeof(Generator));
	initGenerator(track->generator, wavfile_create(bytes, length),
			wavfile_reset, wavfile_generate, wavfile_destroy);
	track->adsr = initEffect(-1, false,
			adsrconfig_create(
					((WavFile *) (track->generator->config))->totalSamples),
			NULL, adsr_process, adsrconfig_destroy);
}

void setSampleBytes(Track *track, char *bytes, int length) {
	WavFile *wavConfig = (WavFile *) track->generator->config;
	freeBuffers(wavConfig);
	wavfile_setBytes(wavConfig, bytes, length);
	adsrconfig_setNumSamples(track->adsr->config,
			((WavFile *)track->generator->config)->totalSamples);
}

void Java_com_kh_beatbot_global_Track_setPrimaryVolume(
		JNIEnv *env, jclass clazz, jint trackNum, jfloat volume) {
	Track *track = getTrack(env, clazz, trackNum);
	track->primaryVolume = volume;
	track->volPan->set(track->volPan->config,
			track->primaryVolume * track->noteVolume,
			track->primaryPan * track->notePan);
}

void Java_com_kh_beatbot_global_Track_setPrimaryPan(JNIEnv *env,
		jclass clazz, jint trackNum, jfloat pan) {
	Track *track = getTrack(env, clazz, trackNum);
	track->primaryPan = pan;
	track->volPan->set(track->volPan->config,
			track->primaryVolume * track->noteVolume,
			track->primaryPan * track->notePan);
}

void Java_com_kh_beatbot_global_Track_setPrimaryPitch(
		JNIEnv *env, jclass clazz, jint trackNum, jfloat pitch) {
	Track *track = getTrack(env, clazz, trackNum);
	track->primaryPitch = pitch;
	//if (track->outputPlayerPitch != NULL) {
	//(*(track->outputPlayerPitch))->SetRate(track->outputPlayerPitch, (short)((track->pitch + track->primaryPitch)*750 + 500));
	//}
}

void Java_com_kh_beatbot_global_Track_setAdsrOn(JNIEnv *env, jclass clazz,
		jint trackNum, jboolean on) {
	Track *track = getTrack(env, clazz, trackNum);
	track->adsr->on = on;
}

void Java_com_kh_beatbot_global_Track_setAdsrPoint(JNIEnv *env,
		jclass clazz, jint trackNum, jint adsrPointNum, jfloat x, jfloat y) {
	Track *track = getTrack(env, clazz, trackNum);
	AdsrConfig *config = (AdsrConfig *) track->adsr->config;
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

void Java_com_kh_beatbot_global_Track_setSampleBytes(JNIEnv *env,
		jclass clazz, jint trackNum, jbyteArray bytes) {
	Track *track = getTrack(env, clazz, trackNum);
	__android_log_print(ANDROID_LOG_ERROR, "in sample set bytes", "before getByteArrayElem");
	jbyte* tempPointer = (*env)->GetByteArrayElements(env, bytes, JNI_FALSE);
	char* data = (char*) tempPointer;
	int length = (int)(*env)->GetArrayLength(env, bytes);

	(*env)->ReleaseByteArrayElements(env, bytes, tempPointer, 0);

	__android_log_print(ANDROID_LOG_ERROR, "in sample set bytes", "before setSampleBytes");
	if (track->generator == NULL) {
		initSampleBytes(track, data, length);
	} else {
		setSampleBytes(track, data, length);
	}
	pthread_mutex_unlock(&((WavFile *)track->generator->config)->bufferMutex);
}

void Java_com_kh_beatbot_activity_BeatBotActivity_addTrack(JNIEnv *env, jclass clazz, jbyteArray bytes) {
	Track *track = initTrack();
	addTrack(track);
	Java_com_kh_beatbot_global_Track_setSampleBytes(env, clazz, trackCount, bytes);
	trackCount++;
}
