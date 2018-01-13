#ifndef SINEWAVE_H
#define SINEWAVE_H

typedef struct SineWave_t {
    float table[TABLE_SIZE];
    float alpha, frequency, phaseOffset, rate, time;
    unsigned int iIndex;
} SineWave;

SineWave *sinewave_create();

void sinewave_setFrequency(SineWave *config, float frequency);

void sinewave_addPhaseOffset(SineWave *config, float phaseOffset);

static inline float sinewave_tick(SineWave *config) {
    while (config->time >= TABLE_SIZE) {
        config->time -= TABLE_SIZE;
    }

    config->iIndex = (unsigned int) config->time;
    config->alpha = config->time - config->iIndex;

    config->time += config->rate;

    return (config->table[config->iIndex] +
            config->alpha * config->table[(config->iIndex + 1) % TABLE_SIZE]) /
           (1 + config->alpha);
}

#endif // SINEWAVE_H
