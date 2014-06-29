#ifndef REVERB_H
#define REVERB_H

#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include <errno.h>

/* Indexes when setting or getting all params */
#define REVPARAM_REVERBVOL 0
#define REVPARAM_SIZE 1
#define REVPARAM_DECAY 2
#define REVPARAM_DENSITY 3
#define REVPARAM_PREDELAY 4
#define REVPARAM_EARLYLATE 5
#define REVPARAM_DAMPINGFREQ 6
#define REVPARAM_BANDWIDTHFREQ 7
#define REV_NUM_PARAMS 8

struct AUDIO_FILTER {
	float low, high, band /* , Notch */;
	float frequency;
	float q;
	float f;
};

// ============================== Delay =============================

struct AUDIO_DELAY {
	float * buffer;
	unsigned int length;
	float feedback;
	unsigned int numTaps;
	unsigned int index[1];
};

struct AUDIO_DELAY3 {
	float *buffer;
	unsigned int length;
	float feedback;
	unsigned int numTaps;
	unsigned int index[3];
};

struct AUDIO_DELAY4 {
	float *buffer;
	unsigned int length;
	float feedback;
	unsigned int numTaps;
	unsigned int index[4];
};

struct AUDIO_DELAY8 {
	float *buffer;
	unsigned int length;
	float feedback;
	unsigned int numTaps;
	unsigned int index[8];
};

// ============================= Reverb =============================

#define REVSIZE_PREDELAY	60000

// REVERB->Runtime[]
#define REVRUN_SizeSmooth		0
#define REVRUN_DampingSmooth	1
#define REVRUN_DensitySmooth	2
#define REVRUN_BandwidthSmooth	3
#define REVRUN_DecaySmooth		4
#define REVRUN_PredelaySmooth	5
#define REVRUN_EarlyLateSmooth	6
#define REVRUN_Density2			7
#define REVRUN_PrevLeftTank		8
#define REVRUN_PrevRightTank	9
#define REVRUN_NUM_PARAMS		10

typedef struct ReverbConfig_t {
	struct AUDIO_DELAY8 earlyReflectionsDelay[2];
	struct AUDIO_FILTER bandwidthFilter[2];
	struct AUDIO_FILTER dampingFilter[2];
	struct AUDIO_DELAY preDelay;
	struct AUDIO_DELAY allpass[6];
	struct AUDIO_DELAY3 allpass3Tap[2];
	struct AUDIO_DELAY4 staticDelay[4];
	float *bufferPtr;
	float settings[REV_NUM_PARAMS];
	float runtime[REVRUN_NUM_PARAMS];
	unsigned int bufferSize;
} ReverbConfig;

#define FILTER_OVERSAMPLECOUNT 4

static const float EarlyReflectionsFactors[] = { 0.f, 0.0199f, 0.0219f, 0.0354f,
		0.0389f, 0.0414f, 0.0692f, 0.f, 0.f, 0.0099f, 0.011f, 0.0182f, 0.0189f,
		0.0213f, 0.0431f, 0.f };

typedef void ReverbProcessPtr(ReverbConfig *, const void *, void *,
		unsigned int);
typedef void ReverbResetPtr(ReverbConfig *);
typedef void ReverbSetParamsPtr(ReverbConfig *, unsigned int, unsigned int *);
typedef ReverbConfig *ReverbAllocPtr(void);
typedef void ReverbFreePtr(ReverbConfig *);

void reverbReset(ReverbConfig *);
void reverbconfig_setParam(void *p, float paramNum, float param);

// ============================== Filter ==============================

static inline float revfilter_process(struct AUDIO_FILTER *filter,
		float input) {
	unsigned int i;

	for (i = 0; i < FILTER_OVERSAMPLECOUNT; i++) {
		filter->low += (filter->f * filter->band) + 1e-25;
		filter->high = input - filter->low - filter->q * filter->band;
		filter->band += filter->f * filter->high;
	}

	return filter->low;
}

// ============================== Delay =============================

// For "Static" filters without feedback
static inline float delay_process(struct AUDIO_DELAY *delay, float input) {
	float output = delay->buffer[delay->index[0]];
	delay->buffer[delay->index[0]] = input;

	unsigned int i;
	for (i = 0; i < delay->numTaps; i++) {
		if (++delay->index[i] >= delay->length) {
			delay->index[i] = 0;
		}
	}

	return output;
}

// For "All Pass" filters with feedback
static inline float delayallpass_process(struct AUDIO_DELAY *delay,
		float input) {
	float bufout = delay->buffer[delay->index[0]];
	float temp = input * (-delay->feedback);
	float output = bufout + temp;

	delay->buffer[delay->index[0]] = input
			+ ((bufout + temp) * delay->feedback);

	unsigned int i;
	for (i = 0; i < delay->numTaps; i++) {
		if (++delay->index[i] >= delay->length) {
			delay->index[i] = 0;
		}
	}

	return output;
}

static inline float delayGetIndex(struct AUDIO_DELAY * delay,
		unsigned int which) {
	return delay->buffer[delay->index[which]];
}

static inline void delayClear(struct AUDIO_DELAY * delay) {
	unsigned int i;

//	memset(delay->buffer, 0, delay->length * sizeof(float));
	for (i = 0; i < delay->numTaps; i++) {
		delay->index[i] = 0;
	}
}

/****************** ReverbProcess() *****************
 * Adds reverb to the waveform data in the input buffer,
 * and stores the new data in the output buffer.
 *
 * inputs =			Pointer to input buffer.
 * outputs =		Pointer to output buffer.
 * sampleFrames =	How many sample frames (not bytes)
 *					to process. For example, 16 frames
 *					of stereo 16-bit data means 64 bytes
 *					(16 frames * 2 channels * sizeof(short)).
 *
 * NOTES: "inputs" and "outputs" can both point to the
 * same buffer if desired. This means the original waveform
 * data is modified.
 *
 * Output buffer must be big enough for the processed data.
 */

static inline void reverb_process(ReverbConfig *rev, float **buffers,
		int sampleFrames) {
	float inverseSampleFrames = 1.0f / sampleFrames;

	float earlyLateDelta = (rev->settings[REVPARAM_EARLYLATE]
			- rev->runtime[REVRUN_EarlyLateSmooth]) * inverseSampleFrames;
	float bandwidthDelta = (((rev->settings[REVPARAM_BANDWIDTHFREQ] * 18400.f)
			+ 100.f) - rev->runtime[REVRUN_BandwidthSmooth])
			* inverseSampleFrames;
	float dampingDelta = (((rev->settings[REVPARAM_DAMPINGFREQ] * 18400.f)
			+ 100.f) - rev->runtime[REVRUN_DampingSmooth])
			* inverseSampleFrames;
	float predelayDelta = ((rev->settings[REVPARAM_PREDELAY] * 200.f
			* (SAMPLE_RATE / 1000.f)) - rev->runtime[REVRUN_PredelaySmooth])
			* inverseSampleFrames;
	float sizeDelta = (rev->settings[REVPARAM_SIZE]
			- rev->runtime[REVRUN_SizeSmooth]) * inverseSampleFrames;
	float decayDelta = (((0.7995f * rev->settings[REVPARAM_DECAY]) + 0.005f)
			- rev->runtime[REVRUN_DecaySmooth]) * inverseSampleFrames;
	float densityDelta = (((0.7995f * rev->settings[REVPARAM_DENSITY]) + 0.005f)
			- rev->runtime[REVRUN_DensitySmooth]) * inverseSampleFrames;

	do {
		float smearedInput, earlyReflectionsL, earlyReflectionsR;

		float left = buffers[0][sampleFrames];
		float right = buffers[1][sampleFrames];

		if (rev->bufferPtr) {
			rev->runtime[REVRUN_EarlyLateSmooth] += earlyLateDelta;
			rev->runtime[REVRUN_BandwidthSmooth] += bandwidthDelta;
			rev->runtime[REVRUN_DampingSmooth] += dampingDelta;
			rev->runtime[REVRUN_PredelaySmooth] += predelayDelta;
			rev->runtime[REVRUN_SizeSmooth] += sizeDelta;
			rev->runtime[REVRUN_DecaySmooth] += decayDelta;
			rev->runtime[REVRUN_DensitySmooth] += densityDelta;
			if (rev->runtime[REVRUN_PredelaySmooth] > REVSIZE_PREDELAY)
				rev->runtime[REVRUN_PredelaySmooth] = REVSIZE_PREDELAY;
			rev->preDelay.length =
					(unsigned int) rev->runtime[REVRUN_PredelaySmooth];

			rev->allpass[4].feedback = rev->allpass[5].feedback =
					rev->settings[REVPARAM_DENSITY];

			rev->runtime[REVRUN_Density2] = rev->runtime[REVRUN_DecaySmooth]
					+ 0.15f;

			if (rev->runtime[REVRUN_Density2] > 0.5f)
				rev->runtime[REVRUN_Density2] = 0.5f;
			if (rev->runtime[REVRUN_Density2] < 0.25f)
				rev->runtime[REVRUN_Density2] = 0.25f;
			rev->allpass3Tap[0].feedback = rev->allpass3Tap[1].feedback =
					rev->runtime[REVRUN_Density2];

			float bandwidthLeft = revfilter_process(&rev->bandwidthFilter[0],
					left);
			float bandwidthRight = revfilter_process(&rev->bandwidthFilter[1],
					right);
			float *buffer = rev->earlyReflectionsDelay[0].buffer;

			unsigned int *index = &rev->earlyReflectionsDelay[0].index[0];
			earlyReflectionsL = delay_process(
					(struct AUDIO_DELAY *) &rev->earlyReflectionsDelay[0],
					bandwidthLeft * 0.5f + bandwidthRight * 0.3f)
					+ buffer[index[2]] * 0.6f + buffer[index[3]] * 0.4f
					+ buffer[index[4]] * 0.3f + buffer[index[5]] * 0.3f
					+ buffer[index[6]] * 0.1f + buffer[index[7]] * 0.1f
					+ (bandwidthLeft * 0.4f + bandwidthRight * 0.2f) * 0.5f;
			buffer = rev->earlyReflectionsDelay[1].buffer;
			index = &rev->earlyReflectionsDelay[1].index[0];
			earlyReflectionsR = delay_process(
					(struct AUDIO_DELAY *) &rev->earlyReflectionsDelay[1],
					bandwidthLeft * 0.3f + bandwidthRight * 0.5f)
					+ buffer[index[2]] * 0.6f + buffer[index[3]] * 0.4f
					+ buffer[index[4]] * 0.3f + buffer[index[5]] * 0.3f
					+ buffer[index[6]] * 0.1f + buffer[index[7]] * 0.1f
					+ (bandwidthLeft * 0.2f + bandwidthRight * 0.4f) * 0.5f;
			smearedInput = delay_process(&rev->preDelay,
					(bandwidthRight + bandwidthLeft) * 0.5f);

			unsigned int i;
			for (i = 0; i < 4; i++) {
				smearedInput = delayallpass_process(&rev->allpass[i],
						smearedInput);
			}

			float leftTank = delayallpass_process(
					(struct AUDIO_DELAY *) &rev->allpass[4],
					smearedInput + rev->runtime[REVRUN_PrevRightTank]);
			float rightTank = delayallpass_process(
					(struct AUDIO_DELAY *) &rev->allpass[5],
					smearedInput + rev->runtime[REVRUN_PrevLeftTank]);

			leftTank = delay_process(
					(struct AUDIO_DELAY *) &rev->staticDelay[0], leftTank);
			leftTank = revfilter_process(&rev->dampingFilter[0], leftTank);
			leftTank = delayallpass_process(
					(struct AUDIO_DELAY *) &rev->allpass3Tap[0], leftTank);
			rev->runtime[REVRUN_PrevLeftTank] = delay_process(
					(struct AUDIO_DELAY *) &rev->staticDelay[1], leftTank)
					* rev->runtime[REVRUN_DecaySmooth];
			rightTank = delay_process(
					(struct AUDIO_DELAY *) &rev->staticDelay[2], rightTank);
			rightTank = revfilter_process(&rev->dampingFilter[1], rightTank);
			rightTank = delayallpass_process(
					(struct AUDIO_DELAY *) &rev->allpass3Tap[1], rightTank);
			rev->runtime[REVRUN_PrevRightTank] = delay_process(
					(struct AUDIO_DELAY *) &rev->staticDelay[3], rightTank)
					* rev->runtime[REVRUN_DecaySmooth];

			float factor = 0.6f;

			float accumulatorL = (factor
					* delayGetIndex((struct AUDIO_DELAY *) &rev->staticDelay[2],
							1))
					+ (factor
							* delayGetIndex(
									(struct AUDIO_DELAY *) &rev->staticDelay[2],
									2))
					- (factor
							* delayGetIndex(
									(struct AUDIO_DELAY *) &rev->allpass3Tap[1],
									1))
					+ (factor
							* delayGetIndex(
									(struct AUDIO_DELAY *) &rev->staticDelay[3],
									1))
					- (factor
							* delayGetIndex(
									(struct AUDIO_DELAY *) &rev->staticDelay[0],
									1))
					- (factor
							* delayGetIndex(
									(struct AUDIO_DELAY *) &rev->allpass3Tap[0],
									1))
					- (factor
							* delayGetIndex(
									(struct AUDIO_DELAY *) &rev->staticDelay[1],
									1));

			float accumulatorR = (factor
					* delayGetIndex((struct AUDIO_DELAY *) &rev->staticDelay[0],
							2))
					+ (factor
							* delayGetIndex(
									(struct AUDIO_DELAY *) &rev->staticDelay[0],
									3))
					- (factor
							* delayGetIndex(
									(struct AUDIO_DELAY *) &rev->allpass3Tap[0],
									2))
					+ (factor
							* delayGetIndex(
									(struct AUDIO_DELAY *) &rev->staticDelay[1],
									2))
					- (factor
							* delayGetIndex(
									(struct AUDIO_DELAY *) &rev->staticDelay[2],
									3))
					- (factor
							* delayGetIndex(
									(struct AUDIO_DELAY *) &rev->allpass3Tap[1],
									2))
					- (factor
							* delayGetIndex(
									(struct AUDIO_DELAY *) &rev->staticDelay[3],
									2));
			accumulatorL = accumulatorL * rev->settings[REVPARAM_EARLYLATE]
					+ (1.f - rev->settings[REVPARAM_EARLYLATE])
							* earlyReflectionsL;
			accumulatorR = accumulatorR * rev->settings[REVPARAM_EARLYLATE]
					+ (1.f - rev->settings[REVPARAM_EARLYLATE])
							* earlyReflectionsR;
			if (rev->settings[REVPARAM_REVERBVOL]) {
				left += ((accumulatorL - left)
						* rev->settings[REVPARAM_REVERBVOL]);
				right += ((accumulatorR - right)
						* rev->settings[REVPARAM_REVERBVOL]);
			} else {
				left = accumulatorL - left;
				right = accumulatorR - right;
			}
		}

		buffers[0][sampleFrames] = (float) left;
		buffers[1][sampleFrames] = (float) right;
	} while (--sampleFrames);
}

ReverbConfig *reverbconfig_create();
void reverbconfig_destroy(void *config);
void reverbconfig_setParam(void *p, float paramNum, float param);

#endif // REVERB_H
