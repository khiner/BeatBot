#ifndef TREMELO_H
#define TREMELO_H

typedef struct TremeloConfig_t {
    SineWave *mod[2];
    float depth;
} TremeloConfig;

TremeloConfig *tremeloconfig_create();

void tremeloconfig_setParam(void *p, float paramNum, float param);

void tremeloconfig_setFrequency(TremeloConfig *config, float freq);

void tremeloconfig_setPhase(TremeloConfig *config, float phase);

void tremeloconfig_setDepth(TremeloConfig *config, float depth);

static inline void tremelo_process(TremeloConfig *config, float **buffers, int size) {
    int channel, samp;
    for (channel = 0; channel < 2; channel++) {
        for (samp = 0; samp < size; samp++) {
            // TODO : how to scale amplitude from 0 to 1 without halving amp when depth is 0
            buffers[channel][samp] *=
                    .75f * (1 + sinewave_tick(config->mod[channel]) * config->depth);
        }
    }
}

void tremeloconfig_destroy(void *p);

#endif // TREMELO_H
