/*
 * soundtouch4c (A wrapper for soundtouch so you can use it in C programs)
 * Copyright (c) 2006-2009 J.A. Roberts Tunney and Anthony Minessale II
 *
 * Here we are implementing a basic wrapper around the SoundTouch
 * library.  Because SoundTouch likes to be tricky and switch between
 * float and short sample types, we're going to provide an interface
 * for shorts only, and translate to the SAMPLETYPE defined by
 * STTypes.h.  This way we shouldn't get that static garbage.
 *
 */

#if HAVE_CONFIG_H
#  include <config.h>
#endif

#include "soundtouch4c.h"

extern "C" {

struct soundtouch4c *SoundTouch_construct()
{
	return (struct soundtouch4c *)(new SoundTouch());
}

void SoundTouch_destruct(struct soundtouch4c *st)
{
	delete (SoundTouch *)st;
}

void SoundTouch_setRate(struct soundtouch4c *st, float newRate)
{
	((SoundTouch *)st)->setRate(newRate);
}

void SoundTouch_setTempo(struct soundtouch4c *st, float newTempo)
{
	((SoundTouch *)st)->setTempo(newTempo);
}

void SoundTouch_setRateChange(struct soundtouch4c *st, float newRate)
{
	((SoundTouch *)st)->setRateChange(newRate);
}

void SoundTouch_setTempoChange(struct soundtouch4c *st, float newTempo)
{
	((SoundTouch *)st)->setTempoChange(newTempo);
}

void SoundTouch_setPitch(struct soundtouch4c *st, float newPitch)
{
	((SoundTouch *)st)->setPitch(newPitch);
}

void SoundTouch_setPitchOctaves(struct soundtouch4c *st, float newPitch)
{
	((SoundTouch *)st)->setPitchOctaves(newPitch);
}

void SoundTouch_setPitchSemiTonesInt(struct soundtouch4c *st, int newPitch)
{
	((SoundTouch *)st)->setPitchSemiTones(newPitch);
}

void SoundTouch_setPitchSemiTonesFloat(struct soundtouch4c *st, float newPitch)
{
	((SoundTouch *)st)->setPitchSemiTones(newPitch);
}

void SoundTouch_setChannels(struct soundtouch4c *st, uint numChannels)
{
	((SoundTouch *)st)->setChannels(numChannels);
}

void SoundTouch_setSampleRate(struct soundtouch4c *st, uint srate)
{
	((SoundTouch *)st)->setSampleRate(srate);
}

void SoundTouch_flush(struct soundtouch4c *st)
{
	((SoundTouch *)st)->flush();
}

void SoundTouch_putSamples(struct soundtouch4c *st, short *samples, uint numSamples)
{
	((SoundTouch *)st)->putSamples((SAMPLETYPE *)samples, numSamples);
}

void SoundTouch_clear(struct soundtouch4c *st)
{
	((SoundTouch *)st)->clear();
}

int SoundTouch_setSetting(struct soundtouch4c *st, uint settingId, uint value)
{
	return ((SoundTouch *)st)->setSetting(settingId, value);
}

uint SoundTouch_getSetting(struct soundtouch4c *st, uint settingId)
{
	return ((SoundTouch *)st)->getSetting(settingId);
}

uint SoundTouch_numUnprocessedSamples(struct soundtouch4c *st)
{
	return ((SoundTouch *)st)->numUnprocessedSamples();
}

uint SoundTouch_receiveSamplesEx(struct soundtouch4c *st, int16_t *output, uint maxSamples)
{
	SAMPLETYPE buf[maxSamples];
	uint n;
	uint c;
	c = ((SoundTouch *)st)->receiveSamples(buf, maxSamples);
	for (n = 0; n < c; n++)
		output[n] = (int16_t)buf[n];
	return c;
}

uint SoundTouch_receiveSamples(struct soundtouch4c *st, uint maxSamples)
{
	return ((SoundTouch *)st)->receiveSamples(maxSamples);
}

uint SoundTouch_numSamples(struct soundtouch4c *st)
{
	return ((SoundTouch *)st)->numSamples();
}

int SoundTouch_isEmpty(struct soundtouch4c *st)
{
	return ((SoundTouch *)st)->isEmpty();
}

}
 
/** EMACS **
 * Local variables:
 * mode: c++
 * tab-width: 4
 * indent-tabs-mode: t
 * c-basic-offset: 4
 * End:
 */
