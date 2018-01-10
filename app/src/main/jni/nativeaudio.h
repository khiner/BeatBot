#ifndef NATIVEAUDIO_H
#define NATIVEAUDIO_H

#define RECORD_SOURCE_MICROPHONE -2

#include "ticker.h"
#include "track.h"

static SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 2,
                                      SL_SAMPLINGRATE_44_1, SL_PCMSAMPLEFORMAT_FIXED_16,
                                      SL_PCMSAMPLEFORMAT_FIXED_16, SL_SPEAKER_FRONT_LEFT
                                                                   | SL_SPEAKER_FRONT_RIGHT,
                                      SL_BYTEORDER_LITTLEENDIAN};

static inline long tickToSample(long tick) {
    return tick * samplesPerTick;
}

static inline long sampleToTick(long sample) {
    return sample / samplesPerTick;
}

bool isPlaying();

void updateNextNoteTicks();

void updateNextNote(Track *track);

void arm();

void stopTrack(Track *track);

void stopAll();

#endif // NATIVEAUDIO_H
