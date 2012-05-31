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

typedef struct Sample_ {
	float currBufferFlt[BUFF_SIZE];
	short currBufferShort[BUFF_SIZE];
	
	// buffer to hold sample
	float *buffer;
	
	int totalSamples;
	int currSample;
	
	float volume;
	float pan;
	
	unsigned int playing;
	
	Hashmap *midiEvents;
	
	DELAYLINE *delayLine;
	
	SLObjectItf outputPlayerObject;
	SLPlayItf outputPlayerPlay;
	SLVolumeItf outputPlayerVolume;
	SLMuteSoloItf outputPlayerMuteSolo;
	// output buffer interfaces
	SLAndroidSimpleBufferQueueItf outputBufferQueue;
} Sample;

typedef struct MidiEvent_ {
	int eventType;
	float volume;
	float pan;
	float pitch;
} MidiEvent;

unsigned int playState;
Sample *samples;
int numSamples;

void playSample(int sampleNum, float volume, float pan, float pitch);
void stopSample(int sampleNum);
void stopAll();

#endif // NATIVEAUDIO_H
