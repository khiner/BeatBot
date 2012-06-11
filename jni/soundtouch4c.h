/*
 * soundtouch4c (A wrapper for soundtouch so you can use it in C programs)
 * Copyright (c) 2006-2009 J.A. Roberts Tunney and Anthony Minessale II
 *
 */

#ifndef SOUNDTOUCH4C_H
#define SOUNDTOUCH4C_H
#include <sys/types.h>

#include <android/log.h>

#ifdef __cplusplus

#include <stdio.h>
#include <soundtouch/SoundTouch.h>
using namespace std;
using namespace soundtouch;

extern "C" {

#else 

/* this name was changed in 0.4 -> 0.5, provide backward compatability
   for C libraries as they won't clash with SoundTouch's namespace */
#define soundtouch soundtouch4c

#define SETTING_USE_AA_FILTER       0
#define SETTING_AA_FILTER_LENGTH    1
#define SETTING_USE_QUICKSEEK       2
#define SETTING_SEQUENCE_MS         3
#define SETTING_SEEKWINDOW_MS       4
#define SETTING_OVERLAP_MS          5

#endif /* #ifdef __cplusplus */

struct soundtouch4c;
struct soundtouch4c *SoundTouch_construct(void);
void SoundTouch_destruct(struct soundtouch4c *st);
void SoundTouch_setRate(struct soundtouch4c *st, float newRate);
void SoundTouch_setTempo(struct soundtouch4c *st, float newTempo);
void SoundTouch_setRateChange(struct soundtouch4c *st, float newRate);
void SoundTouch_setTempoChange(struct soundtouch4c *st, float newTempo);
void SoundTouch_setPitch(struct soundtouch4c *st, float newPitch);
void SoundTouch_setPitchOctaves(struct soundtouch4c *st, float newPitch);
void SoundTouch_setPitchSemiTonesInt(struct soundtouch4c *st, int newPitch);
void SoundTouch_setPitchSemiTonesFloat(struct soundtouch4c *st, float newPitch);
void SoundTouch_setChannels(struct soundtouch4c *st, uint numChannels);
void SoundTouch_setSampleRate(struct soundtouch4c *st, uint srate);
void SoundTouch_flush(struct soundtouch4c *st);
void SoundTouch_putSamples(struct soundtouch4c *st, short *samples, uint numSamples);
void SoundTouch_clear(struct soundtouch4c *st);
int SoundTouch_setSetting(struct soundtouch4c *st, uint settingId, uint value);
uint SoundTouch_getSetting(struct soundtouch4c *st, uint settingId);
uint SoundTouch_numUnprocessedSamples(struct soundtouch4c *st);
uint SoundTouch_receiveSamplesEx(struct soundtouch4c *st, int16_t *output, uint maxSamples);
uint SoundTouch_receiveSamples(struct soundtouch4c *st, uint maxSamples);
uint SoundTouch_numSamples(struct soundtouch4c *st);
int SoundTouch_isEmpty(struct soundtouch4c *st);

#ifdef __cplusplus
}
#endif

#endif /* #ifndef SOUNDTOUCH4C_H */
 
/** EMACS **
 * Local variables:
 * mode: c++
 * tab-width: 4
 * indent-tabs-mode: t
 * c-basic-offset: 4
 * End:
 */
