#include "../all.h"

FilterConfig *filterconfig_create() {
    FilterConfig *config = (FilterConfig *) malloc(sizeof(FilterConfig));
    config->rScale = .7f;
    config->mode = 0;
    config->baseF = SAMPLE_RATE / 4;
    config->r = 0.5f;
    config->modDepth = .5f;
    config->mod = sinewave_create();
    int filterNum, channel;
    for (filterNum = 0; filterNum < 2; filterNum++) {
        InnerFilterConfig *inner = (InnerFilterConfig *) malloc(sizeof(InnerFilterConfig));
        for (channel = 0; channel < 2; channel++) {
            inner->in1[channel] = 0;
            inner->in2[channel] = 0;
            inner->out1[channel] = 0;
            inner->out2[channel] = 0;
        }
        config->inner[filterNum] = inner;
    }
    filterconfig_set(config, config->baseF, config->r);
    return config;
}

void filterconfig_setModRate(FilterConfig *config, float rate) {
    sinewave_setFrequency(config->mod, rate);
}

void filterconfig_setModDepth(FilterConfig *config, float depth) {
    config->modDepth = depth;
}

void filterconfig_destroy(void *p) {
    FilterConfig *config = (FilterConfig *) p;
    free(config->inner[0]);
    free(config->inner[1]);
    free(config);
}

void filterconfig_setParam(void *p, float paramNumFloat, float param) {
    FilterConfig *config = (FilterConfig *) p;
    int paramNum = (int) paramNumFloat;
    if (paramNum == 0) { // frequency
        config->baseF = param;
    } else if (paramNum == 1) { // resonance
        param = 1 - param * config->rScale; // flip and scale to .3 to 1
        config->r = param;
    } else if (paramNum == 2) { // mod rate
        filterconfig_setModRate(config, param);
    } else if (paramNum == 3) { // mod depth
        filterconfig_setModDepth(config, param);
    } else if (paramNum == 4) { // mode
        config->mode = (int) param;
        if ((int) param == LP_MODE || (int) param == HP_MODE) {
            config->rScale = .7f;
        } else if ((int) param == BP_MODE) { // bandpass filter
            // lp and hp are chained, so reduce resonance to avoid clipping
            config->rScale = .4f;
        }
    }
}
