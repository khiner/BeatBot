#ifndef TRACK_H
#define TRACK_H

typedef struct OpenSlOut_ {
	float **currBufferFloat;
	short currBufferShort[BUFF_SIZE * 2];bool armed;
	SLObjectItf outputPlayerObject;
	SLPlayItf outputPlayerPlay;
	SLMuteSoloItf outputPlayerMuteSolo;
	SLAndroidSimpleBufferQueueItf outputBufferQueue;
	pthread_mutex_t trackMutex;
} OpenSlOut;

/*
 * Hold levels and effects, used for both normal tracks and master track
 */
typedef struct Levels_ {
	float volume, pan, pitch;
	EffectNode *effectHead;
	Effect *volPan;
	// mutex for effects since insertion/setting/removing effects happens on diff thread than processing
	pthread_mutex_t effectMutex;
} Levels;

typedef struct Track_ {
	float tempSample[2];
	float **currBufferFloat;
	Levels *levels;
	Generator *generator;
	MidiEvent *nextEvent;
	long nextStartSample, nextStopSample;
	int num; bool mute;bool solo;bool shouldSound;
} Track;

typedef struct TrackNode_t {
	Track *track;
	struct TrackNode_t *next;
} TrackNode;

Levels *masterLevels;
TrackNode *trackHead;
OpenSlOut *openSlOut;
int trackCount;

jfloatArray makejFloatArray(JNIEnv * env, float floatAry[], int size);

TrackNode *getTrackNode(int trackNum);

Track *getTrack(JNIEnv *env, jclass clazz, int trackNum);

Levels *getLevels(JNIEnv *env, jclass clazz, int trackNum);

void addEffect(Levels *levels, Effect *effect);

void freeMidiEvents(Track *track);

void printTracks(TrackNode *head);

void addTrack(Track *track);

TrackNode *removeTrack(int trackNum);

void updateLevels(int trackNum);

void setPreviewLevels(Track *track);

void updateAllLevels();

void freeEffects(Levels *levels);

void freeTracks();

Levels *initLevels();

Track *initTrack();

#endif // TRACK_H
