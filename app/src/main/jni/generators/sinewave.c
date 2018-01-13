#include "../all.h"

SineWave *sinewave_create() {
    SineWave *sineWave = (SineWave *) malloc(sizeof(SineWave));
    sinewave_setFrequency(sineWave, 1);
    sineWave->time = 0;
    sineWave->phaseOffset = 0;
    int i;
    for (i = 0; i < TABLE_SIZE; i++) {
        sineWave->table[i] = (float) sin(2 * M_PI * i * INV_TABLE_SIZE);
    }
    return sineWave;
}

void sinewave_setFrequency(SineWave *config, float frequency) {
    config->frequency = frequency;
    config->rate = frequency * INV_SAMPLE_RATE * TABLE_SIZE;
}

void sinewave_addPhaseOffset(SineWave *config, float phaseOffset) {
    config->time += (phaseOffset - config->phaseOffset) * TABLE_SIZE;
    config->phaseOffset = phaseOffset;
}
