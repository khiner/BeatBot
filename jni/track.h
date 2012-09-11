#ifndef TRACK_H
#define TRACK_H

#include "generators/generators.h"

#define NUM_TRACKS 6

typedef struct OpenSlOut_ {
	float **currBufferFloat;
	short currBufferShort[BUFF_SIZE * 2];bool armed;bool anyTrackArmed;
	SLObjectItf outputPlayerObject;
	SLPlayItf outputPlayerPlay;
	SLMuteSoloItf outputPlayerMuteSolo;
	SLPlaybackRateItf outputPlayerPitch;
	SLAndroidSimpleBufferQueueItf outputBufferQueue;
} OpenSlOut;

typedef struct Track_ {
	float tempSample[2];
	EffectNode *effectHead;
	Effect *volPan, *pitch, *adsr;
	float **currBufferFloat;
	Generator *generator;
	MidiEventNode *eventHead;
	MidiEventNode *nextEventNode;

	// mutex for effects since insertion/setting/removing effects happens on diff thread than processing
	pthread_mutex_t effectMutex;
	long nextStartSample, nextStopSample;
	float noteVolume, notePan, notePitch, primaryVolume, primaryPan,
			primaryPitch;

	int num;bool armed;bool playing;bool previewing;bool mute;bool solo;bool shouldSound;
} Track;

Track tracks[NUM_TRACKS];
OpenSlOut *openSlOut;

static inline Track *getTrack(JNIEnv *env, jclass clazz, int trackNum) {
	(void *) env; // avoid warnings about unused paramaters
	(void *) clazz; // avoid warnings about unused paramaters

	if (trackNum < 0 || trackNum >= NUM_TRACKS)
		return NULL;
	return &tracks[trackNum];
}

static inline void freeMidiEvents(Track *track) {
	MidiEventNode *cur_ptr = track->eventHead;
	while (cur_ptr != NULL) {
		free(cur_ptr->event); // free the event
		MidiEventNode *prev_ptr = cur_ptr;
		cur_ptr = cur_ptr->next;
		free(prev_ptr); // free the entire Node
	}
}

static inline void freeEffects(Track *track) {
	EffectNode *cur_ptr = track->effectHead;
	while (cur_ptr != NULL) {
		cur_ptr->effect->destroy(cur_ptr->effect->config);
		EffectNode *prev_ptr = cur_ptr;
		cur_ptr = cur_ptr->next;
		free(prev_ptr); // free the entire Node
	}
}

static inline void freeTracks() {
	// destroy all tracks
	int i;
	for (i = 0; i < NUM_TRACKS; i++) {
		Track *track = getTrack(NULL, NULL, i);
		free(track->currBufferFloat[0]);
		free(track->currBufferFloat[1]);
		free(track->currBufferFloat);
		track->generator->destroy(track->generator->config);
		freeEffects(track);
		freeMidiEvents(track);
	}
	free(openSlOut->currBufferFloat[0]);
	free(openSlOut->currBufferFloat[1]);
	free(openSlOut->currBufferFloat);
	free(openSlOut->currBufferShort);
	(*openSlOut->outputBufferQueue)->Clear(openSlOut->outputBufferQueue);
	openSlOut->outputBufferQueue = NULL;
	openSlOut->outputPlayerPlay = NULL;
}

#endif // TRACK_H
