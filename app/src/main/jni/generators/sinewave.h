#ifndef SINEWAVE_H
#define SINEWAVE_H

#include <ticker.h>

typedef struct SineWave_t {
    float table[TABLE_SIZE];
    float alpha, frequency, phaseOffset, rate, time;
    unsigned int iIndex;
} SineWave;

SineWave *sinewave_create();

void sinewave_setFrequency(SineWave *config, float frequency);

void sinewave_setPhaseUnitScale(SineWave *config, float phaseOffset);

static inline float sinewave_valueAtOffset(SineWave *config, int offset) {
    long absCurrSample = currSample + offset;
    config->time = absCurrSample * config->rate + config->phaseOffset;
    config->iIndex = (unsigned int) config->time;
    config->alpha = config->time - config->iIndex;

    return (config->table[config->iIndex % TABLE_SIZE] +
            config->alpha * config->table[(config->iIndex + 1) % TABLE_SIZE]) /
           (1 + config->alpha);
}

#endif // SINEWAVE_H
