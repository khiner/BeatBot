#ifndef DELAY_H
#define DELAY_H

#define MIN_DELAY_SAMPLES 32

typedef struct DelayConfigI_t {
    float **delayBuffer; // delay buffer for each channel
    float delayTime[2]; // delay time in seconds: 0-1; one for each channel
    float feedback; // feedback amount: 0-1; used for both channels
    float wet; // wet/dry; used for both channels
    float alpha[2];
    float omAlpha[2];
    float delaySamples[2]; // (fractional) delay time in samples: 0 - SAMPLE_RATE; one for each channel
    float out;
    int maxSamples; // maximum size of delay buffer (set to SAMPLE_RATE by default)
    int rp[2], wp[2]; // read & write pointers
    bool linkChannels;
    pthread_mutex_t mutex; // mutex since sets happen on a different thread than processing
} DelayConfigI;

DelayConfigI *delayconfigi_create();

void delayconfigi_setFeedback(DelayConfigI *config, float feedback);

static inline void delayconfigi_setDelaySamples(DelayConfigI *config,
                                                float numSamplesL, float numSamplesR) {
    int *rp, *wp, channel;
    float rpf;
    pthread_mutex_lock(&config->mutex);
    config->delaySamples[0] = numSamplesL < MIN_DELAY_SAMPLES ? MIN_DELAY_SAMPLES : numSamplesL;
    config->delaySamples[1] = numSamplesR < MIN_DELAY_SAMPLES ? MIN_DELAY_SAMPLES : numSamplesR;
    for (channel = 0; channel < 2; channel++) {
        rp = &(config->rp[channel]);
        wp = &(config->wp[channel]);
        rpf = *wp - config->delaySamples[channel]; // read chases write
        while (rpf < 0)
            rpf += config->maxSamples;
        *rp = (int) rpf;
        if (*rp >= config->maxSamples)
            (*rp) = 0;
        config->alpha[channel] = rpf - (*rp);
        config->omAlpha[channel] = 1.0f - config->alpha[channel];
    }
    pthread_mutex_unlock(&config->mutex);
}

static inline void delayconfigi_setDelayTime(DelayConfigI *config, float lDelay,
                                             float rDelay) {
    pthread_mutex_lock(&config->mutex);
    config->delayTime[0] = lDelay;
    config->delayTime[1] = rDelay;
    pthread_mutex_unlock(&config->mutex);
    delayconfigi_setDelaySamples(config, config->delayTime[0] * SAMPLE_RATE,
                                 config->delayTime[1] * SAMPLE_RATE);
}

static inline void delayconfigi_setDelayTimeLeft(DelayConfigI *config, float lDelay) {
    float rDelay = config->delayTime[1];
    delayconfigi_setDelayTime(config, lDelay, rDelay);
}

static inline void delayconfigi_setDelayTimeRight(DelayConfigI *config, float rDelay) {
    float lDelay = config->delayTime[0];
    delayconfigi_setDelayTime(config, lDelay, rDelay);
}

static inline float delayi_tick(DelayConfigI *config, float in, int channel) {
    float interpolated = config->delayBuffer[channel][config->rp[channel]++]
                         * config->omAlpha[channel];
    // make sure we don't go past maxSamples (buffer end) after incrementing read/write pointer
    if (config->rp[channel] >= config->maxSamples) {
        config->rp[channel] = 0;
    }
    if (config->wp[channel] >= config->maxSamples) {
        config->wp[channel] = 0;
    }
    interpolated += config->delayBuffer[channel][config->rp[channel]] * config->alpha[channel];

    config->out = config->wet * (interpolated - in) + in;
    // NOTE: not hard limiting to 1 max for efficiency
    config->delayBuffer[channel][config->wp[channel]++] = in
                                                          + config->out * config->feedback;
    return config->out;
}

static inline void delayi_process(DelayConfigI *config, float **buffers, int size) {
    int channel, samp;
    pthread_mutex_lock(&config->mutex);
    for (channel = 0; channel < 2; channel++) {
        for (samp = 0; samp < size; samp++) {
            buffers[channel][samp] = delayi_tick(config, buffers[channel][samp],
                                                 channel);
        }
    }
    pthread_mutex_unlock(&config->mutex);
}

void delayconfigi_setParam(void *p, float paramNumFloat, float param);

void delayconfigi_destroy(void *p);

#endif // DELAY_H
