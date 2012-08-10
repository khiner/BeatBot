#ifndef NATIVEAUDIO_H
#define NATIVEAUDIO_H

#include "effects/effects.h"
#include "effects/adsr.h"
#include "effects/chorus.h"
#include "effects/decimate.h"
#include "effects/delay.h"
#include "effects/filter.h"
#include "effects/flanger.h"
#include "effects/pitch.h"
#include "effects/reverb.h"
#include "effects/tremelo.h"
#include "effects/volpan.h"

#define CONV16BIT 32768

static SLDataFormat_PCM format_pcm = { SL_DATAFORMAT_PCM, 2,
		SL_SAMPLINGRATE_44_1, SL_PCMSAMPLEFORMAT_FIXED_16,
		SL_PCMSAMPLEFORMAT_FIXED_16, SL_SPEAKER_FRONT_LEFT
				| SL_SPEAKER_FRONT_RIGHT, SL_BYTEORDER_LITTLEENDIAN };

void printLinkedList(MidiEventNode *head);
MidiEventNode *findNextEvent(Track *track);
void playTrack(int trackNum, float volume, float pan, float pitch);
void stopTrack(int trackNum);
void stopAll();
void syncAll(); // sync all BPM-syncable events to (new) BPM

#endif // NATIVEAUDIO_H
