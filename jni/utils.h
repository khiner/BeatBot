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

#endif /* UTILS_H_ */
