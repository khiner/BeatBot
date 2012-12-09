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

void Java_com_kh_beatbot_manager_MidiManager_setNativeBPM(JNIEnv *env,
		jclass clazz, jfloat _BPM) {
	BPM = _BPM;
}

void Java_com_kh_beatbot_manager_MidiManager_setNativeMSPT(JNIEnv *env,
		jclass clazz, jlong _MSPT) {
	MSPT = _MSPT;
	SPT = (MSPT * SAMPLE_RATE) / 1000000;
	loopBeginSample = tickToSample(loopBeginTick);
	loopEndSample = tickToSample(loopEndTick);
	//updateNextNoteSamples();
}

jlong Java_com_kh_beatbot_manager_MidiManager_getLoopBeginTick(JNIEnv *env,
		jclass clazz) {
	return loopBeginTick;
}

void Java_com_kh_beatbot_manager_MidiManager_setLoopBeginTick(JNIEnv *env,
		jclass clazz, jlong _loopBeginTick) {
	if (_loopBeginTick >= loopEndTick || _loopBeginTick == loopBeginTick)
		return;

	loopBeginTick = _loopBeginTick;
	loopBeginSample = tickToSample(loopBeginTick);
	if (!getTrack(NULL, NULL, 0)->armed) {
		currSample = tickToSample(loopBeginTick);
	}
}

jlong Java_com_kh_beatbot_manager_MidiManager_getLoopEndTick(JNIEnv *env,
		jclass clazz) {
	return loopEndTick;
}

void Java_com_kh_beatbot_manager_MidiManager_setLoopEndTick(JNIEnv *env,
		jclass clazz, jlong _loopEndTick) {
	if (_loopEndTick <= loopBeginTick || _loopEndTick == loopEndTick)
		return;
	loopEndTick = _loopEndTick;
	loopEndSample = tickToSample(loopEndTick);
}

void Java_com_kh_beatbot_manager_MidiManager_setLoopTicks(JNIEnv *env,
		jclass clazz, jlong _loopBeginTick, jlong _loopEndTick) {
	if (_loopBeginTick >= _loopEndTick)
		return;
	loopBeginTick = _loopBeginTick;
	loopEndTick = _loopEndTick;
	loopBeginSample = tickToSample(loopBeginTick);
	loopEndSample = tickToSample(loopEndTick);
	if (!getTrack(NULL, NULL, 0)->armed) {
		currSample = tickToSample(loopBeginTick);
	}
}

void Java_com_kh_beatbot_manager_MidiManager_reset(JNIEnv *env, jclass clazz) {
	currSample = tickToSample(loopBeginTick);
}

