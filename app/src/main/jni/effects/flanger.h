#ifndef FLANGER_H
#define FLANGER_H

typedef struct FlangerConfig_t {
    DelayConfigI *delayConfig; // delay line
    SineWave *mod[2]; // table-based sine wave generator for modulation
    float baseTime; // center time for delay modulation
    float modAmt; // modulation depth
} FlangerConfig;

FlangerConfig *flangerconfig_create();

void flangerconfig_setParam(void *p, float paramNum, float param);

void flangerconfig_setBaseTime(FlangerConfig *config, float baseTime);

void flangerconfig_setFeedback(FlangerConfig *config, float feedback);

void flangerconfig_setModFreq(FlangerConfig *config, float modFreq);

void flangerconfig_setModAmt(FlangerConfig *config, float modAmt);

void flangerconfig_setPhaseShift(FlangerConfig *config, float phaseShift);

static inline void flanger_process(FlangerConfig *config, float **buffers, int size) {
    int channel, samp;
    for (samp = 0; samp < size; samp++) {
        float dTimeL = config->baseTime
                       * (1.01f + config->modAmt * sinewave_tick(config->mod[0]));
        float dTimeR = config->baseTime
                       * (1.01f + config->modAmt * sinewave_tick(config->mod[1]));
        delayconfigi_setDelaySamples(config->delayConfig, dTimeL, dTimeR);
        pthread_mutex_lock(&config->delayConfig->mutex);
        for (channel = 0; channel < 2; channel++) {
            buffers[channel][samp] = delayi_tick(config->delayConfig,
                                                 buffers[channel][samp], channel);
        }
        pthread_mutex_unlock(&config->delayConfig->mutex);
    }
}

void flangerconfig_destroy(void *config);

#endif // FLANGER_H
