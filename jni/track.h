#ifndef TRACK_H
#define TRACK_H

typedef struct OpenSlOut_ {
	bool armed;
	short *recordBufferShort;
	float **currBufferFloat;
	short currBufferShort[BUFF_SIZE * 2];
	short micBufferShort[BUFF_SIZE * 2];
	SLObjectItf outputPlayerObject;
	SLObjectItf recorderObject;
	SLPlayItf outputPlayerPlay;
	SLRecordItf recordInterface;
	SLMuteSoloItf outputPlayerMuteSolo;
	SLAndroidSimpleBufferQueueItf outputBufferQueue;
	SLAndroidSimpleBufferQueueItf micBufferQueue;
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

jfloatArray makejFloatArray(JNIEnv * env, float floatAry[], int size);

TrackNode *getTrackNode(int trackNum);

Track *getTrack(JNIEnv *env, jclass clazz, int trackNum);

Levels *getLevels(JNIEnv *env, jclass clazz, int trackNum);

void fillTempSample(Track *track);

void soundTrack(Track *track);

void stopSoundingTrack(Track *track);

void stopTrack(Track *track);

void playTrack(Track *track);

void previewTrack(Track *track);

void stopPreviewingTrack(Track *track);

void addEffect(Levels *levels, Effect *effect);

void freeMidiEvents(Track *track);

void printTracks();

void addTrack(Track *track);

void removeTrack(TrackNode *trackNode);

void updateLevels(int trackNum);

void setPreviewLevels(Track *track);

void updateAllLevels();

void freeEffects(Levels *levels);

void destroyTracks();

Levels *initLevels();

Track *initTrack();

#endif // TRACK_H
