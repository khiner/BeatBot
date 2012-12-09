#ifndef TRACK_H
#define TRACK_H

#include "generators/generators.h"
#include "midievent.h"
#include "effects/effects.h"

#define MAX_EFFECTS_PER_TRACK 3 // also need to change GlobalVars.MAX_EFFECT_PER_TRACK

typedef struct OpenSlOut_ {
	float **currBufferFloat;
	short currBufferShort[BUFF_SIZE * 2];bool armed;bool anyTrackArmed;
	SLObjectItf outputPlayerObject;
	SLPlayItf outputPlayerPlay;
	SLMuteSoloItf outputPlayerMuteSolo;
	SLAndroidSimpleBufferQueueItf outputBufferQueue;
	pthread_mutex_t trackMutex;
} OpenSlOut;

typedef struct Track_ {
	float tempSample[2];
	EffectNode *effectHead;
	Effect *volPan, *adsr;
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

typedef struct TrackNode_t {
	Track *track;
	struct TrackNode_t *next;
} TrackNode;

TrackNode *trackHead;
OpenSlOut *openSlOut;
int trackCount;
float masterVolume, masterPan, masterPitch;

jfloatArray makejFloatArray(JNIEnv * env, float floatAry[], int size);

TrackNode *getTrackNode(int trackNum);

Track *getTrack(JNIEnv *env, jclass clazz, int trackNum);

void addEffect(Track *track, Effect *effect);

void freeMidiEvents(Track *track);

void printTracks(TrackNode *head);

void addTrack(Track *track);

TrackNode *removeTrack(int trackNum);

void updateLevels(Track *track);

void updateAllLevels();

void freeEffects(Track *track);

void freeTracks();

Track *initTrack();

#endif // TRACK_H
