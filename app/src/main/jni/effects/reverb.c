#include "../all.h"

// Copyright (c) 2010 Martin Eastwood
// This code is distributed under the terms of the GNU General Public License

// MVerb is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// at your option, any later version.
//
// MVerb is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this MVerb. If not, see <http://www.gnu.org/licenses/>.
//
// This is the C code version, derived from the original C++ code (with
// some minor design changes to minimize code and memory usage), by Jeff
// Glatt.

// ============================= Reverb =============================

static void getNewRevLengths(ReverbConfig *rev, unsigned int *lengths) {
    float factor = SAMPLE_RATE * rev->settings[REVPARAM_SIZE];
    *(lengths)++ = (unsigned int) (0.020f * factor);
    *(lengths)++ = (unsigned int) (0.030f * factor);
    *(lengths)++ = (unsigned int) (0.060f * factor);
    *(lengths)++ = (unsigned int) (0.089f * factor);
    *(lengths)++ = (unsigned int) (0.15f * factor);
    *(lengths)++ = (unsigned int) (0.12f * factor);
    *(lengths)++ = (unsigned int) (0.14f * factor);
    *lengths = (unsigned int) (0.11f * factor);
}

static void setNewRevLengths(ReverbConfig *rev, unsigned int *lengths) {
    rev->allpass[4].length = *(lengths)++;
    rev->allpass[5].length = *(lengths)++;
    rev->allpass3Tap[0].length = *(lengths)++;
    rev->allpass3Tap[1].length = *(lengths)++;

    unsigned int i;
    for (i = 0; i < 4; i++) {
        rev->staticDelay[i].length = *(lengths)++;
        delayClear((struct AUDIO_DELAY *) &rev->staticDelay[i]);
    }

    float factor = SAMPLE_RATE * rev->settings[REVPARAM_SIZE];

    rev->allpass3Tap[0].index[0] = rev->allpass3Tap[1].index[0] =
    rev->allpass[4].index[0] = rev->allpass[5].index[0] = 0;
    rev->allpass3Tap[0].index[1] = (unsigned int) (0.006f * factor);
    rev->allpass3Tap[0].index[2] = (unsigned int) (0.041f * factor);
    rev->allpass3Tap[1].index[1] = (unsigned int) (0.031f * factor);
    rev->allpass3Tap[1].index[2] = (unsigned int) (0.011f * factor);

    rev->staticDelay[0].index[1] = (unsigned int) (0.067f * factor);
    rev->staticDelay[0].index[2] = (unsigned int) (0.011f * factor);
    rev->staticDelay[0].index[3] = (unsigned int) (0.121f * factor);
    rev->staticDelay[1].index[1] = (unsigned int) (0.036f * factor);
    rev->staticDelay[1].index[2] = (unsigned int) (0.089f * factor);
    rev->staticDelay[2].index[1] = (unsigned int) (0.0089f * factor);
    rev->staticDelay[2].index[2] = (unsigned int) (0.099f * factor);
    rev->staticDelay[3].index[1] = (unsigned int) (0.067f * factor);
    rev->staticDelay[3].index[2] = (unsigned int) (0.0041f * factor);
}

static void allocRevBuffers(ReverbConfig *rev, unsigned int *lengths) {
    unsigned int totalLen;
    unsigned int i;
    float *buffer;

    // Calc how many floats we need to store
    totalLen = REVSIZE_PREDELAY;
    for (i = 0; i < 14; i++) {
        totalLen += lengths[i];
    }
    // Do we need a larger buffer? If so, malloc it, with a little extra room to minimize future realloc
    if (totalLen > rev->bufferSize) {
        totalLen += 1000;

        if (rev->bufferPtr) {
            free(rev->bufferPtr);
        }

        rev->bufferPtr = buffer = (float *) calloc(totalLen, sizeof(float));
        rev->bufferSize = totalLen;
        // Update all ptrs to buffers
        rev->preDelay.buffer = buffer;
        buffer += (REVSIZE_PREDELAY * sizeof(float));
        for (i = 0; i < 2; i++) {
            rev->earlyReflectionsDelay[i].buffer = (float *) buffer;
            buffer += (*(lengths)++ * sizeof(float));
        }
        for (i = 0; i < 6; i++) {
            rev->allpass[i].buffer = (float *) buffer;
            buffer += (*(lengths)++ * sizeof(float));
        }
        for (i = 0; i < 2; i++) {
            rev->allpass3Tap[i].buffer = (float *) buffer;
            buffer += (*(lengths)++ * sizeof(float));
        }
        for (i = 0; i < 4; i++) {
            rev->staticDelay[i].buffer = (float *) buffer;
            buffer += (*(lengths)++ * sizeof(float));
        }
    }
}

void filterReset(struct AUDIO_FILTER *filter) {
    filter->low = filter->high = filter->band = 0;
}

void reverbReset(ReverbConfig *rev) {
    unsigned int i;
    unsigned int lengths[14];

    lengths[0] = (unsigned int) (0.089f * SAMPLE_RATE);
    lengths[1] = (unsigned int) (0.069f * SAMPLE_RATE);
    lengths[2] = (unsigned int) (0.0048f * SAMPLE_RATE);
    lengths[3] = (unsigned int) (0.0036f * SAMPLE_RATE);
    lengths[4] = (unsigned int) (0.0127f * SAMPLE_RATE);
    lengths[5] = (unsigned int) (0.0093f * SAMPLE_RATE);

    getNewRevLengths(rev, &lengths[6]);
    allocRevBuffers(rev, &lengths[0]);
    rev->controlRateCounter = 0;
    memset(&rev->runtime[0], 0, REVRUN_NUM_PARAMS * sizeof(float));
    for (i = 0; i < 2; i++) {
        filterReset(&rev->bandwidthFilter[i]);
        filterReset(&rev->dampingFilter[i]);
        rev->bandwidthFilter[i].f = 2.f
                                    * (float) sin(
                M_PI * rev->bandwidthFilter[i].frequency
                / (SAMPLE_RATE * FILTER_OVERSAMPLECOUNT));
        rev->dampingFilter[i].f = 2.f
                                  * (float) sin(
                M_PI * rev->dampingFilter[i].frequency
                / (SAMPLE_RATE * FILTER_OVERSAMPLECOUNT));
        rev->earlyReflectionsDelay[i].length = lengths[i];
    }

    for (i = 0; i < 4; i++) {
        rev->allpass[i].length = lengths[i + 2];
        delayClear((struct AUDIO_DELAY *) &rev->allpass[i]);
    }

    rev->preDelay.length = (unsigned int) rev->settings[REVPARAM_PREDELAY];
    delayClear(&rev->preDelay);

    for (i = 0; i < 8; i++) {
        rev->earlyReflectionsDelay[0].index[i] =
                (unsigned int) (EarlyReflectionsFactors[i] * SAMPLE_RATE);
        rev->earlyReflectionsDelay[1].index[i] =
                (unsigned int) (EarlyReflectionsFactors[i + 8] * SAMPLE_RATE);
    }

    rev->allpass[4].feedback = rev->allpass[5].feedback =
            rev->settings[REVPARAM_DENSITY];
    rev->allpass3Tap[0].feedback = rev->allpass3Tap[1].feedback =
            rev->runtime[REVRUN_Density2];

    setNewRevLengths(rev, &lengths[6]);
}

/****************** ReverbSetParams() ****************
 * Sets the current value of the specified parameter(s).
 *
 * flags =	Which params to set. OR'ed bitmask of the
 *			REVPARAM_ #defines.
 * values =	An array containing the new values.
 *
 * RETURNS: 0 if success, or an error number.
 */

void reverbconfig_setParam(void *p, float paramNumFloat, float value) {
    ReverbConfig *rev = (ReverbConfig *) p;

    int paramNum = (int) paramNumFloat;
    if (paramNum == REVPARAM_SIZE) {
        // size needs to be rounded to .05 or algorithm adds "distortion"
        value = ((((int) (100 * value)) / 5) * 5) / 100.0f;
        if (value == 0.0f)
            value = 0.05f;
        else if (value >= 0.90f)
            value = 0.90f;
    } else if (value > 1.f) {
        value = 1.f;
    }

    pthread_mutex_lock(&rev->mutex);

    rev->settings[paramNum] =
            paramNum == REVPARAM_DAMPINGFREQ ? 1.f - value : value;

    if (paramNum == REVPARAM_SIZE) {
        unsigned int lengths[14];

        lengths[0] = rev->earlyReflectionsDelay[0].length;
        lengths[1] = rev->earlyReflectionsDelay[1].length;
        lengths[2] = rev->allpass[0].length;
        lengths[3] = rev->allpass[1].length;
        lengths[4] = rev->allpass[2].length;
        lengths[5] = rev->allpass[3].length;
        getNewRevLengths(rev, &lengths[6]);
        allocRevBuffers(rev, &lengths[0]);
        setNewRevLengths(rev, &lengths[6]);
    }
    pthread_mutex_unlock(&rev->mutex);
}

/******************** ReverbAlloc() ******************
 * Allocs/initializes a struct REVERB.
 */

ReverbConfig *reverbconfig_create() {
    ReverbConfig *rev = malloc(sizeof(ReverbConfig));
    pthread_mutex_init(&rev->mutex, NULL);

    rev->controlRate = (unsigned char) (SAMPLE_RATE / 1000);

    unsigned int i;
    for (i = 0; i < 2; i++) {
        rev->bandwidthFilter[i].frequency = rev->dampingFilter[i].frequency =
                1000.f;
        rev->bandwidthFilter[i].q = rev->dampingFilter[i].q = 2.f;
        rev->earlyReflectionsDelay[i].numTaps = 8;
        rev->allpass3Tap[i].numTaps = 3;
        rev->allpass[i + 4].feedback = rev->allpass3Tap[i].feedback = 0.5;
    }

    rev->preDelay.numTaps = 1;
    for (i = 0; i < 4; i++)
        rev->staticDelay[i].numTaps = 4;
    for (i = 0; i < 6; i++)
        rev->allpass[i].numTaps = 1;

    rev->allpass[0].feedback = rev->allpass[1].feedback = 0.75f;
    rev->allpass[2].feedback = rev->allpass[3].feedback = 0.625f;

    for (i = 0; i < REV_NUM_PARAMS; i++) {
        rev->settings[i] = 0.5f;
    }
    rev->settings[REVPARAM_PREDELAY] = .25f;

    rev->bufferSize = 0;
    rev->bufferPtr = 0;
    reverbReset(rev);
    return rev;
}

/* Frees a struct REVERB's resources. */
void reverbconfig_destroy(void *p) {
    ReverbConfig *rev = (ReverbConfig *) p;

    if (rev->bufferPtr) {
        free(rev->bufferPtr);
    }
    free(rev);
}
