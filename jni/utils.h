/*

 *
 *  Created on: Jun 22, 2014
 *      Author: khiner
 */
#ifndef UTILS_H_
#define UTILS_H_

#include <math.h>

static inline float clipTo(float value, float min, float max) {
	return value > min ? (value < max ? value : max) : min;
}

static inline float byteToLinear(unsigned char byte) {
	return (float) byte / 124.0f;
}

static inline unsigned char linearToByte(float linear) {
	return (unsigned char) linear * 124;
}

static inline float dbToLinear(float db) {
	return pow(10, db / 20);
}

static inline unsigned char dbToByte(float db) {
	return linearToByte(dbToLinear(db));
}

static inline float transposeToScaleValue(float transpose) {
	return pow(2, transpose / 12.0);
}

static inline float panToScaleValue(float pan) {
	// pan is in [-1,1] range.  translate to [0,1]
	return (clipTo(pan, -1, 1) + 1.0f) / 2.0f;
}

#endif /* UTILS_H_ */
