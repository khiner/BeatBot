/*
 * soundtouch4c (A wrapper for soundtouch so you can use it in C programs)
 * Copyright (c) 2006-2009 J.A. Roberts Tunney and Anthony Minessale II
 *
 */

#if HAVE_CONFIG_H
#  include <config.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <soundtouch4c.h>

#define BUFSAMPLES 4096
#define IN_FILE "data/monkeys-8khz-slin.raw"
#define OUT_FILE "data/monkeys-8khz-slin-deepened.raw"

static struct soundtouch4c *create(int hz, float pitch)
{
	struct soundtouch4c *snd;
	snd = SoundTouch_construct();
	if (!snd) {
		fprintf(stderr, "Failed to create SoundTouch object\n");
		exit(1);
	}

	SoundTouch_setChannels(snd, 1);
	SoundTouch_setSampleRate(snd, hz);
	SoundTouch_setPitchSemiTonesFloat(snd, pitch);
	SoundTouch_setSetting(snd, SETTING_USE_QUICKSEEK, 1);
	SoundTouch_setSetting(snd, SETTING_USE_AA_FILTER, 1);

	return snd;
}

int main(int argc, char *argv[])
{
	int n;
	size_t cnt;
	FILE *fin, *fout;
	int16_t buf[BUFSAMPLES];
	struct soundtouch4c *snd;

	snd = create(8000, -5.0); /* deepen voice, telephone quality 8khz */
	fin = fopen(IN_FILE, "rb");
	if (!fin) { fprintf(stderr, "Could not find: %s\n", IN_FILE); exit(1); }
	fout = fopen(OUT_FILE, "wb+");
	if (!fout) { fprintf(stderr, "Could not write: %s\n", OUT_FILE); exit(1); }

	while (1) {
		/* read 16-bit signed samples from file */
		cnt = fread(&buf[0], sizeof(int16_t), BUFSAMPLES, fin);
		if (cnt == 0)
			break;

		/* enqueue samples for voice transformation.  library copies
		 * this out of our buffer so it's safe to re-use */
		SoundTouch_putSamples(snd, &buf[0], cnt);

		/* copy data back.  when dealing with smaller buffer sizes,
		 * you might have to wait through a few passes before this
		 * routine actually starts returning data */
		cnt = SoundTouch_receiveSamplesEx(snd, &buf[0], BUFSAMPLES);

		/* write transformed 16-bit signed samples to output file */
		if (cnt)
			fwrite(&buf[0], sizeof(int16_t), cnt, fout);
	}

	fclose(fin);
	fflush(fout);
	fclose(fout);
	SoundTouch_destruct(snd);
}

/** EMACS **
 * Local variables:
 * mode: c
 * tab-width: 4
 * indent-tabs-mode: t
 * c-basic-offset: 4
 * End:
 */
