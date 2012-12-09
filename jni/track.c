#include "track.h"
#include "effects/adsr.h"
#include "effects/volpan.h"

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
	track->effectHead = NULL;
	track->generator = NULL;
	track->nextEvent = malloc(sizeof(MidiEvent));
	track->nextStartSample = track->nextStopSample = -1;
	track->currBufferFloat = (float **) malloc(2 * sizeof(float *));
	track->currBufferFloat[0] = (float *) calloc(BUFF_SIZE, sizeof(float));
	track->currBufferFloat[1] = (float *) calloc(BUFF_SIZE, sizeof(float));
	track->armed = false;
	track->playing = track->previewing = false;
	track->mute = track->solo = false;
	track->shouldSound = true;
	track->primaryVolume = track->nextEvent->volume = .8f;
	track->primaryPan = track->primaryPitch = track->nextEvent->pitch =
			track->nextEvent->pan = .5f;
	int effectNum;
	for (effectNum = 0; effectNum < MAX_EFFECTS_PER_TRACK; effectNum++) {
		addEffect(track, NULL);
	}
	track->volPan = initEffect(-1, true, volumepanconfig_create(),
			volumepanconfig_set, volumepan_process, volumepanconfig_destroy);
	return track;
}

void initSample(Track *track, const char *sampleName) {
	track->generator = malloc(sizeof(Generator));
	initGenerator(track->generator, wavfile_create(sampleName), wavfile_reset,
			wavfile_generate, wavfile_destroy);
	track->adsr = initEffect(-1, false,
			adsrconfig_create(
					((WavFile *) (track->generator->config))->totalSamples),
			NULL, adsr_process, adsrconfig_destroy);
}

void setSample(Track *track, const char *sampleName) {
	WavFile *wavConfig = (WavFile *) track->generator->config;
	wavfile_setSampleFile(wavConfig, sampleName);
	adsrconfig_setNumSamples(track->adsr->config,
			((WavFile *) track->generator->config)->totalSamples);
}

void Java_com_kh_beatbot_global_Track_disarmTrack(JNIEnv *env, jclass clazz,
		jint trackNum) {
	getTrack(env, clazz, trackNum)->armed = openSlOut->anyTrackArmed = false;
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

void updateLevels(Track *track) {
	MidiEvent *midiEvent = track->nextEvent;
	track->volPan->set(track->volPan->config,
			midiEvent->volume,
			(masterPan + track->primaryPan + midiEvent->pan) / 3);
	((WavFile *) track->generator->config)->sampleRate = masterPitch
			* track->primaryPitch * midiEvent->pitch * 8;
}

void updateAllLevels() {
	TrackNode *cur_ptr = trackHead;
	while (cur_ptr != NULL) {
		updateLevels(cur_ptr->track);
		cur_ptr = cur_ptr->next;
	}
}

void Java_com_kh_beatbot_global_Track_setPrimaryVolume(JNIEnv *env,
		jclass clazz, jint trackNum, jfloat volume) {
	Track *track = getTrack(env, clazz, trackNum);
	track->primaryVolume = volume;
	updateLevels(track);
}

void Java_com_kh_beatbot_global_Track_setPrimaryPan(JNIEnv *env, jclass clazz,
		jint trackNum, jfloat pan) {
	Track *track = getTrack(env, clazz, trackNum);
	track->primaryPan = pan;
	updateLevels(track);
}

void Java_com_kh_beatbot_global_Track_setPrimaryPitch(JNIEnv *env, jclass clazz,
		jint trackNum, jfloat pitch) {
	Track *track = getTrack(env, clazz, trackNum);
	track->primaryPitch = pitch;
	updateLevels(track);
}

void Java_com_kh_beatbot_global_Track_setAdsrOn(JNIEnv *env, jclass clazz,
		jint trackNum, jboolean on) {
	Track *track = getTrack(env, clazz, trackNum);
	track->adsr->on = on;
}

void Java_com_kh_beatbot_global_Track_setAdsrPoint(JNIEnv *env, jclass clazz,
		jint trackNum, jint adsrPointNum, jfloat x, jfloat y) {
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

void Java_com_kh_beatbot_activity_BeatBotActivity_addTrack(JNIEnv *env,
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

void Java_com_kh_beatbot_global_Track_setTrackLoopWindow(JNIEnv *env,
		jclass clazz, jint trackNum, jlong loopBeginSample, jlong loopEndSample) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *) track->generator->config;
	if (wavFile->loopBegin == loopBeginSample
			&& wavFile->loopEnd == loopEndSample)
		return;
	wavFile->loopBegin = loopBeginSample;
	wavFile->loopEnd = loopEndSample;
	wavFile->loopLength = wavFile->loopEnd - wavFile->loopBegin;
	updateAdsr((AdsrConfig *) track->adsr->config,
			loopEndSample - loopBeginSample);
}

void Java_com_kh_beatbot_global_Track_setTrackReverse(JNIEnv *env, jclass clazz,
		jint trackNum, jboolean reverse) {
	Track *track = getTrack(env, clazz, trackNum);
	WavFile *wavFile = (WavFile *) track->generator->config;
	wavFile->reverse = reverse;
	// if the track is not looping, the wavFile generator will not loop to the beginning/end
	// after enaabling/disabling reverse
	if (reverse && wavFile->currSample == wavFile->loopBegin)
		wavFile->currSample = wavFile->loopEnd;
	else if (!reverse && wavFile->currSample == wavFile->loopEnd)
		wavFile->currSample = wavFile->loopBegin;
}
