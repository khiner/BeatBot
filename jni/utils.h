/*

 *
 *  Created on: Jun 22, 2014
 *      Author: khiner
 */
#ifndef UTILS_H_
#define UTILS_H_

#include <math.h>

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

static inline float pitchToTranspose(unsigned char pitch) {
	return pow(2, (((int) pitch - 64) / 12.0));
}

#endif /* UTILS_H_ */
