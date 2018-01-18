#include "../all.h"
#include "libsndfile/sndfile.h"

void filegen_setSampleFile(FileGen *config, const char *sampleFileName) {
    SNDFILE *infile, *infileBufferCopy;
    SF_INFO sfinfo;

    sf_close(config->sampleFile);
    sf_close(config->sampleFileBufferCopy);

    if (!(infile = sf_open(sampleFileName, SFM_READ, &sfinfo)) || sfinfo.channels > MAX_CHANNELS)
        /* Open failed so print an error message. */
        return;

    if (!(infileBufferCopy = sf_open(sampleFileName, SFM_READ, &sfinfo)) ||
        sfinfo.channels > MAX_CHANNELS)
        /* Open failed so print an error message. */
        return;

    config->channels = sfinfo.channels;
    config->frames = sfinfo.frames;
    config->sampleFile = infile;
    config->sampleFileBufferCopy = infileBufferCopy;
    config->buffer = malloc(BUFF_SIZE_FLOATS);
    config->loopBegin = 0;
    config->loopEnd = config->frames;
    if (config->currFrame > config->loopEnd) {
        config->currFrame = config->loopEnd;
    } else if (config->currFrame < config->loopBegin) {
        config->currFrame = config->loopBegin;
    }
    config->loopLength = config->loopEnd - config->loopBegin;
    config->bufferStartFrame = LONG_MIN; // forces the buffer to be reloaded
}

FileGen *filegen_create() {
    FileGen *fileGen = (FileGen *) malloc(sizeof(FileGen));
    fileGen->currFrame = 0;
    fileGen->gain = 1;
    fileGen->sampleFile = NULL;
    fileGen->sampleFileBufferCopy = NULL;
    fileGen->looping = fileGen->reverse = false;
    fileGen->adsr = adsrconfig_create();
    fileGen->sampleRate = 1;
    return fileGen;
}

void filegen_setLoopWindow(FileGen *fileGen, long loopBeginSample,
                           long loopEndSample) {
    if (fileGen->loopBegin == loopBeginSample
        && fileGen->loopEnd == loopEndSample)
        return;
    fileGen->loopBegin = loopBeginSample;
    fileGen->loopEnd = loopEndSample;
    fileGen->loopLength = fileGen->loopEnd - fileGen->loopBegin;
}

void filegen_setReverse(FileGen *fileGen, bool reverse) {
    fileGen->reverse = reverse;
    // if the track is not looping, the fileGen generator will not loop to the beginning/end
    // after enabling/disabling reverse
    if (reverse && fileGen->currFrame == fileGen->loopBegin)
        fileGen->currFrame = fileGen->loopEnd;
    else if (!reverse && fileGen->currFrame == fileGen->loopEnd)
        fileGen->currFrame = fileGen->loopBegin;
}

void filegen_start(FileGen *config) {
    config->currFrame = config->reverse ? config->loopEnd : config->loopBegin;
    resetAdsr(config->adsr);
}

void filegen_reset(FileGen *config) {
    config->adsr->stoppedSample = config->adsr->currSample;
}


void filegen_destroy(void *p) {
    FileGen *fileGen = (FileGen *) p;
    if (fileGen->sampleFile != NULL) {
        sf_close(fileGen->sampleFile);
        fileGen->sampleFile = NULL;
    }
    if (fileGen->sampleFileBufferCopy != NULL) {
        sf_close(fileGen->sampleFileBufferCopy);
        fileGen->sampleFileBufferCopy = NULL;
    }
    adsrconfig_destroy(fileGen->adsr);
    fileGen->adsr = NULL;
    free(fileGen);
    fileGen = NULL;
}
