#include "all.h"

void initTicker() {
	currSample = loopBeginTick = loopBeginSample = 0;
	loopEndTick = loopEndSample = 0;
}

jlong Java_com_kh_beatbot_manager_MidiManager_getCurrTick(JNIEnv *env,
		jclass clazz) {
	return sampleToTick(currSample);
}

void Java_com_kh_beatbot_manager_MidiManager_setCurrTick(JNIEnv *env,
		jclass clazz, jlong currTick) {
	currSample = tickToSample(currTick);
}

void Java_com_kh_beatbot_manager_MidiManager_setNativeMSPT(JNIEnv *env,
		jclass clazz, jlong _MSPT) {
	MSPT = _MSPT;
	SPT = (MSPT * SAMPLE_RATE) / 1000000;
	loopBeginSample = tickToSample(loopBeginTick);
	loopEndSample = tickToSample(loopEndTick);
}

void Java_com_kh_beatbot_manager_MidiManager_setLoopTicksNative(JNIEnv *env,
		jclass clazz, jlong _loopBeginTick, jlong _loopEndTick) {
	if (_loopBeginTick >= _loopEndTick)
		return;
	loopBeginTick = _loopBeginTick;
	loopEndTick = _loopEndTick;
	loopBeginSample = tickToSample(loopBeginTick);
	loopEndSample = tickToSample(loopEndTick);
	if (!isPlaying()) {
		currSample = tickToSample(loopBeginTick);
	}
}


