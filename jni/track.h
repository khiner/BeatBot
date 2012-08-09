#ifndef TRACK_H
#define TRACK_H

#include "generators/generators.h"

#define NUM_TRACKS 6

typedef struct Track_ {
	Effect effects[NUM_EFFECTS];
	short currBufferShort[BUFF_SIZE * 2];
	float **currBufferFloat;
	Generator *generator;
	MidiEventNode *eventHead;

	MidiEventNode *nextEventNode;

	float volume, pan, pitch;

	SLObjectItf outputPlayerObject;
	SLPlayItf outputPlayerPlay;
	SLMuteSoloItf outputPlayerMuteSolo;
	SLPlaybackRateItf outputPlayerPitch;
	SLAndroidSimpleBufferQueueItf outputBufferQueue;

	bool armed;
	bool playing;
	bool mute;
	bool solo;
} Track;

Track tracks[NUM_TRACKS];

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
		(*(track->outputBufferQueue))->Clear(track->outputBufferQueue);
		track->outputBufferQueue = NULL;
		track->outputPlayerPlay = NULL;
		free(track->currBufferFloat);
		free(track->currBufferShort);
		track->generator->destroy(track->generator->config);
		for (j = 0; j < NUM_EFFECTS; j++) {
			track->effects[i].destroy(track->effects[i].config);
		}
		free(track->effects);
		freeLinkedList(track->eventHead);
	}
	free(tracks);
}

#endif // TRACK_H
