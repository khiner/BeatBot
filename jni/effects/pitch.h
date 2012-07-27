#ifndef PITCH_H
#define PITCH_H

#include "effects.h"
#include "delay.h"

#define MAX_PITCH_DELAY_SAMPS 5024

typedef struct PitchConfig_t {
	DelayConfigI *delayLine[2];
	float delaySamples[2];
	float env[2];
	float rate;
	float wet;
	unsigned long delayLength;
	unsigned long halfLength;
} PitchConfig;

PitchConfig *pitchconfig_create();
void pitchconfig_setShift(PitchConfig *config, float shift);

static inline float pitch_tick(PitchConfig *config, float in, int channel) {
	// Calculate the two delay length values, keeping them within the
	// range 12 to maxDelay-12.
	config->delaySamples[0] += config->rate;
	while (config->delaySamples[0] > MAX_PITCH_DELAY_SAMPS - 12)
		config->delaySamples[0] -= config->delayLength;
	while (config->delaySamples[0] < 12)
		config->delaySamples[0] += config->delayLength;

	config->delaySamples[1] = config->delaySamples[0] + config->halfLength;
	while (config->delaySamples[1] > MAX_PITCH_DELAY_SAMPS - 12)
		config->delaySamples[1] -= config->delayLength;
	while (config->delaySamples[1] < 12)
		config->delaySamples[1] += config->delayLength;

	// Set the new delay line lengths.
	delayconfigi_setDelaySamples(config->delayLine[0], config->delaySamples[0],
			config->delaySamples[0]);
	delayconfigi_setDelaySamples(config->delayLine[1], config->delaySamples[1],
			config->delaySamples[1]);

	// Calculate a triangular envelope.
	config->env[1] = fabs(
			(config->delaySamples[0] - config->halfLength + 12)
					* (1.0 / (config->halfLength + 12)));
	config->env[0] = 1.0 - config->env[1];

	// Delay input and apply envelope.
	float out = config->env[0] * delayi_tick(config->delayLine[0], in, channel);
	out += config->env[1] * delayi_tick(config->delayLine[1], in, channel);

	// Compute effect mix and output.
	out *= config->wet;
	out += (1.0 - config->wet) * in;

	return out;
}

static inline void pitch_process(PitchConfig *config, float **buffers, int size) {
	int channel, samp;
	for (channel = 0; channel < 2; channel++) {
		for (samp = 0; samp < size; samp++) {
			buffers[channel][samp] = pitch_tick(config, buffers[channel][samp],
					channel);
		}
	}
}

void pitchconfig_destroy(void *config);

#endif // PITCH_H
