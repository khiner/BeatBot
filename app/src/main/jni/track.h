#ifndef TRACK_H
#define TRACK_H

#define MASTER_TRACK_ID -1

typedef struct OpenSlOut_ {
    bool armed;
    int recordSourceId;
    float **currBufferFloat;
    short globalBufferShort[BUFF_SIZE_SHORTS];
    short micBufferShort[BUFF_SIZE_SHORTS];
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
    float scaleChannels[2];
    float volume, pan, pitchSteps;
    EffectNode *effectHead;
    // mutex for effects since insertion/setting/removing effects happens on diff thread than processing
    pthread_mutex_t effectMutex;
} Levels;

typedef struct Track_ {
    float tempSample[2];
    float **currBufferFloat;
    Levels *levels;
    Generator *generator;
    MidiEvent *nextEvent;
    long nextStartTick, nextStopTick;
    int num;
    bool mute;
    bool solo;
    bool shouldSound;
} Track;

typedef struct TrackNode_t {
    Track *track;
    struct TrackNode_t *next;
} TrackNode;

Levels *masterLevels;
TrackNode *trackHead;
OpenSlOut *openSlOut;

TrackNode *getTrackNode(int trackId);

Track *getTrack(int trackId);

Levels *getLevels(int trackId);

void fillTempSample(Track *track);

void soundTrack(Track *track);

void stopSoundingTrack(Track *track);

void stopTrack(Track *track);

void playTrack(Track *track);

void previewTrack(Track *track);

void stopPreviewingTrack(Track *track);

void addEffect(Levels *levels, Effect *effect);

void printTracks();

void removeTrack(TrackNode *trackNode);

void freeEffects(Levels *levels);

void destroyTracks();

Levels *initLevels();

Track *initTrack(int trackId);

#endif // TRACK_H
