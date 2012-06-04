#ifndef NATIVEAUDIO_H
#define NATIVEAUDIO_H

#include <assert.h>
#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <sys/time.h>

// for Android logging
#include <android/log.h>

// for native audio
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

// for native asset manager
#include <sys/types.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#include "effects.h"
#include "hashmap.h"

#define CONV16BIT 32768
#define CONVMYFLT (1./32768.)
#define BUFF_SIZE 512

static SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1,
									  SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
									  SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
									  SL_BYTEORDER_LITTLEENDIAN};

typedef struct MidiEvent_ {
	bool muted;
	long onTick;
	long offTick;	
	float volume;
	float pan;
	float pitch;
} MidiEvent;

typedef struct MidiEventNode_ {
	MidiEvent *event;
	struct MidiEventNode_ *next;
} MidiEventNode;

typedef struct Track_ {
	MidiEventNode *eventHead;
	float currBufferFlt[BUFF_SIZE];
	short currBufferShort[BUFF_SIZE];
	
	// buffer to hold sample
	float *buffer;
	
	int totalSamples;
	int currSample;
	
	float volume;
	float pan;
	float pitch;
	
        bool armed;
	bool playing;
        bool loopMode;

        int loopBegin;
        int loopEnd;

        // effect configs
        VolumePanConfig *volumePanConfig;	
	DelayConfig *delayConfig;
        FilterConfig *filterConfig;
        DecimateConfig *decimateConfig;

	SLObjectItf outputPlayerObject;
	SLPlayItf outputPlayerPlay;
	SLVolumeItf outputPlayerVolume;
	SLMuteSoloItf outputPlayerMuteSolo;
	// output buffer interfaces
	SLAndroidSimpleBufferQueueItf outputBufferQueue;
} Track;

Track *tracks;
int numTracks;

MidiEvent *findEvent(MidiEventNode *midiEventHead, long tick);
void printLinkedList(MidiEventNode *head);
void playTrack(int trackNum, float volume, float pan, float pitch);
void stopTrack(int trackNum);
void stopAll();

#endif // NATIVEAUDIO_H
