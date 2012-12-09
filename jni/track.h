#ifndef TRACK_H
#define TRACK_H

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
	Effect *volPan;
	float **currBufferFloat;
	Generator *generator;
	MidiEvent *nextEvent;

	// mutex for effects since insertion/setting/removing effects happens on diff thread than processing
	pthread_mutex_t effectMutex;
	long nextStartSample, nextStopSample;
	float primaryVolume, primaryPan, primaryPitch;

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
