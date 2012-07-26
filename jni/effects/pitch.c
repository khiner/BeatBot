#include "pitch.h"

PitchConfig *pitchconfig_create() {
	PitchConfig *config = (PitchConfig *) malloc(sizeof(PitchConfig));
	config->delayLength = MAX_PITCH_DELAY_SAMPS - 24;
	config->halfLength = config->delayLength / 2;
	config->delaySamples[0] = 12;
	config->delaySamples[1] = MAX_PITCH_DELAY_SAMPS / 2;

	config->delayLine[0] = delayconfigi_create(0, 1, MAX_PITCH_DELAY_SAMPS);
	config->delayLine[1] = delayconfigi_create(0, 1, MAX_PITCH_DELAY_SAMPS);
	int channel;
	for (channel = 0; channel < 2; channel++) {
		delayconfigi_setDelaySamples(config->delayLine[0],
				config->delaySamples[0], channel);
		delayconfigi_setDelaySamples(config->delayLine[1],
				config->delaySamples[1], channel);
	}

	config->rate = 1.0f;
	config->wet = 0.5f;
	return config;
}

void pitchconfig_setShift(PitchConfig *config, float shift) {
	if (shift == 1.0) {
		config->rate = 0.0;
		config->delaySamples[0] = config->halfLength + 12;
	} else {
		config->rate = 1.0 - shift;
	}
}

void pitchconfig_destroy(void *p) {
	PitchConfig *config = (PitchConfig *) p;
	delayconfigi_destroy(config->delayLine[0]);
	delayconfigi_destroy(config->delayLine[1]);
	free((PitchConfig *) config);
}
