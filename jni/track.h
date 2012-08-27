#ifndef TRACK_H
#define TRACK_H

#include "generators/generators.h"

#define NUM_TRACKS 6

typedef struct OpenSlOut_ {
	float **currBufferFloat;
	short currBufferShort[BUFF_SIZE * 2];
	bool armed;
	SLObjectItf outputPlayerObject;
	SLPlayItf outputPlayerPlay;
	SLMuteSoloItf outputPlayerMuteSolo;
	SLPlaybackRateItf outputPlayerPitch;
	SLAndroidSimpleBufferQueueItf outputBufferQueue;
} OpenSlOut;

typedef struct Track_ {
	Effect effects[NUM_EFFECTS];
	float **currBufferFloat;
	Generator *generator;
	MidiEventNode *eventHead;

	MidiEventNode *nextEventNode;

	long nextStartSample, nextStopSample;
	float noteVolume, notePan, notePitch,
		  primaryVolume, primaryPan, primaryPitch;

	int num;
	bool armed;
	bool playing;
	bool previewing;
	bool mute;
	bool solo;
	bool shouldSound;
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

static inline void freeLinkedList(MidiEventNode *head) {
	MidiEventNode *cur_ptr = head;
	while (cur_ptr != NULL) {
		free(cur_ptr->event); // free the event
		MidiEventNode *prev_ptr = cur_ptr;
		cur_ptr = cur_ptr->next;
		free(prev_ptr); // free the entire Node
	}
}

static inline void freeTracks() {
	// destroy all tracks
	int i, j;
	for (i = 0; i < NUM_TRACKS; i++) {
		Track *track = getTrack(NULL, NULL, i);
		free(track->currBufferFloat[0]);
		free(track->currBufferFloat[1]);
		free(track->currBufferFloat);
		track->generator->destroy(track->generator->config);
		for (j = 0; j < NUM_EFFECTS; j++) {
			track->effects[i].destroy(track->effects[i].config);
		}
		free(track->effects);
		freeLinkedList(track->eventHead);
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
