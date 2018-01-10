#include "../all.h"

TremeloConfig *tremeloconfig_create() {
    TremeloConfig *config = malloc(sizeof(TremeloConfig));
    config->mod[0] = sinewave_create();
    config->mod[1] = sinewave_create();
    tremeloconfig_setFrequency(config, .5f);
    tremeloconfig_setDepth(config, .5f);
    return config;
}

void tremeloconfig_setFrequency(TremeloConfig *config, float freq) {
    sinewave_setFrequency(config->mod[0], freq);
    sinewave_setFrequency(config->mod[1], freq);
}

void tremeloconfig_setPhase(TremeloConfig *config, float phase) {
    sinewave_addPhaseOffset(config->mod[1], phase);
}

void tremeloconfig_setDepth(TremeloConfig *config, float depth) {
    config->depth = depth;
}

void tremeloconfig_destroy(void *p) {
    TremeloConfig *config = (TremeloConfig *) p;
    free(config->mod[0]);
    free(config->mod[1]);
    free(config);
}

void tremeloconfig_setParam(void *p, float paramNumFloat, float param) {
    TremeloConfig *config = (TremeloConfig *) p;
    switch ((int) paramNumFloat) {
        case 0: // mod frequency
            tremeloconfig_setFrequency(config, param);
            break;
        case 1: // phase
            tremeloconfig_setPhase(config, param);
            break;
        case 2: // depth
            tremeloconfig_setDepth(config, param);
            break;
    }
}
