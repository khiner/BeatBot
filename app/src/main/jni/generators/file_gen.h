#ifndef FILEGEN_H
#define FILEGEN_H

#include "libsndfile/sndfile.h"

typedef struct FileGen_t {
    // mutex for buffer since setting the data happens on diff thread than processing
    SNDFILE *sampleFile;
    SNDFILE *sampleFileBufferCopy;
    AdsrConfig *adsr;
    float tempSample[2];
    float otherTempSample[2];
    float *buffer;
    float currFrame;
    long frames;
    long loopBegin;
    long loopEnd;
    long loopLength;
    long bufferStartFrame;
    bool looping;
    bool reverse;
    float sampleRate;
    float gain;
    int channels, channel;
} FileGen;

FileGen *filegen_create();

static inline void
filegen_getBufferCopySample(FileGen *fileGen, float *ptr, int channel, long startFrame,
                            int numFrames) {
    sf_seek(fileGen->sampleFileBufferCopy, startFrame, SEEK_SET);
    sf_readf_float(fileGen->sampleFileBufferCopy, ptr, numFrames);
}

void filegen_setSampleFile(FileGen *fileGen, const char *sampleFileName);

void filegen_setLoopWindow(FileGen *fileGen, long loopBeginSample,
                           long loopEndSample);

void filegen_setReverse(FileGen *fileGen, bool reverse);

void filegen_start(FileGen *config);

void filegen_reset(FileGen *config);

static inline void filegen_sndFileRead(FileGen *config, long frame,
                                       float *sample) {
    if (frame >= config->bufferStartFrame + BUFF_SIZE_FRAMES
        || frame < config->bufferStartFrame) {
        long seekTo =
                !config->reverse ?
                frame :
                (frame - BUFF_SIZE_FRAMES >= 0 ?
                 frame - BUFF_SIZE_FRAMES : 0);
        sf_seek(config->sampleFile, seekTo, SEEK_SET);
        sf_readf_float(config->sampleFile, config->buffer, BUFF_SIZE_FRAMES);
        config->bufferStartFrame = seekTo;
    }
    frame -= config->bufferStartFrame;

    if (config->channels == 1) {
        sample[0] = config->buffer[frame];
    } else {
        sample[0] = config->buffer[frame * 2];
        sample[1] = config->buffer[frame * 2 + 1];
    }
}

static inline void filegen_tick(FileGen *config, float *sample) {
    // wrap sample around loop window
    if (config->looping) {
        if (config->currFrame >= config->loopEnd) {
            config->currFrame -= config->loopLength;
        } else if (config->currFrame <= config->loopBegin) {
            config->currFrame += config->loopLength;
        }
    }

    if (config->currFrame > config->loopEnd
        || config->currFrame < config->loopBegin) {
        sample[0] = sample[1] = 0;
        return;
    }

    // perform linear interpolation on the next two samples
    // (ignoring wrapping around loop - this is close enough and we avoid an extra
    //  read from disk)
    long frame, nextFrame;
    float remainder;
    if (config->reverse) {
        frame = (long) ceil(config->currFrame);
        remainder = frame - config->currFrame;
        nextFrame = frame - 1;
    } else {
        frame = (long) floor(config->currFrame);
        remainder = config->currFrame - frame;
        nextFrame = frame + 1;
    }

    // read next two samples from current sample (rounded down)
    filegen_sndFileRead(config, frame, config->tempSample);
    filegen_sndFileRead(config, nextFrame, config->otherTempSample);

    for (config->channel = 0; config->channel < config->channels; config->channel++) {
        float samp1 = config->tempSample[config->channel];
        float samp2 = config->otherTempSample[config->channel];
        sample[config->channel] = (1.0f - remainder) * samp1 + remainder * samp2;
    }

    // copy left channel to right channel if mono
    if (config->channels == 1) {
        sample[1] = sample[0];
    }

    // get next sample.  if reverse, go backwards, else go forwards
    if (config->reverse) {
        config->currFrame -= config->sampleRate;
    } else {
        config->currFrame += config->sampleRate;
    }

    float gain = adsr_tick(config->adsr) * config->gain;
    sample[0] *= gain;
    sample[1] *= gain;
}

static inline void filegen_generate(FileGen *config, float **inBuffer,
                                    int size) {
    int i;
    for (i = 0; i < size; i++) {
        filegen_tick(config, config->tempSample);
        inBuffer[0][i] = config->tempSample[0];
        inBuffer[1][i] = config->tempSample[1];
    }
}

void filegen_destroy(void *config);

#endif // FILEGEN_H
