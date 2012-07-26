#ifndef NATIVEAUDIO_H
#define NATIVEAUDIO_H

#include <assert.h>
#include <jni.h>
#include <string.h>
#include <math.h>
#include <stdlib.h>
#include <stdbool.h>
#include <sys/time.h>
#include <sys/types.h>

// fast-fourier transform library
#include <fftw3.h>
// for Android logging
#include <android/log.h>
// for native audio
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
// for native asset manager
#include <android/asset_manager_jni.h>
// local .h includes
#include "effects.h"

#define CONV16BIT 32768
#define CONVMYFLT (1./32768.)
#define BUFF_SIZE 512 // 512 samples, each with one short for each channel

static SLDataFormat_PCM format_pcm = { SL_DATAFORMAT_PCM, 2,
		SL_SAMPLINGRATE_44_1, SL_PCMSAMPLEFORMAT_FIXED_16,
		SL_PCMSAMPLEFORMAT_FIXED_16, SL_SPEAKER_FRONT_LEFT
				| SL_SPEAKER_FRONT_RIGHT, SL_BYTEORDER_LITTLEENDIAN };

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
	Effect effects[NUM_EFFECTS];
	float currBufferFlt[BUFF_SIZE * 2];
	short currBufferShort[BUFF_SIZE * 2];
	MidiEventNode *eventHead;
	float **currBuffers;
	// buffer to hold original sample data
	float **buffers;
	int totalSamples;
	int currSample;

	float volume, pan, pitch;

	bool armed;
	bool playing;
	bool loop;
	bool mute;
	bool solo;

	int loopBegin;
	int loopEnd;

	SLObjectItf outputPlayerObject;
	SLPlayItf outputPlayerPlay;
	SLMuteSoloItf outputPlayerMuteSolo;
	SLPlaybackRateItf outputPlayerPitch;
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
void syncAll(); // sync all BPM-syncable events to (new) BPM

#endif // NATIVEAUDIO_H
