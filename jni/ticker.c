#include "nativeaudio.h"

void initTicker() {
	currTick = 0;
	loopBeginTick = 0;
	loopEndTick = 0;
}

jlong Java_com_kh_beatbot_manager_MidiManager_getCurrTick(JNIEnv *env,
		jclass clazz) {
	return currTick;
}

void Java_com_kh_beatbot_manager_MidiManager_setCurrTick(JNIEnv *env,
		jclass clazz, jlong _currTick) {
	currTick = _currTick;
}

void Java_com_kh_beatbot_manager_MidiManager_setNativeBPM(JNIEnv *env,
		jclass clazz, jfloat _BPM) {
	BPM = _BPM;
}

void Java_com_kh_beatbot_manager_MidiManager_setNativeMSPT(JNIEnv *env,
		jclass clazz, jlong MSPT) {
	NSPT = MSPT * 1000;
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
	if (!tracks[0].armed) // hack to see if we're playing
		currTick = loopBeginTick;
	int i;
	for (i = 0; i < NUM_TRACKS; i++)
		tracks[i].nextEventNode = findNextEvent(&tracks[i]);
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
	int i;
	for (i = 0; i < NUM_TRACKS; i++)
		tracks[i].nextEventNode = findNextEvent(&tracks[i]);
}

void Java_com_kh_beatbot_manager_MidiManager_reset(JNIEnv *env, jclass clazz) {
	currTick = loopBeginTick - 1;
}

static inline uint64_t currTimeNano(struct timespec *now) {
	clock_gettime(CLOCK_MONOTONIC, now);
	return now->tv_sec * 1000000000LL + now->tv_nsec;
}

void Java_com_kh_beatbot_manager_MidiManager_startTicking(JNIEnv *env,
		jclass clazz) {
	struct timespec now;

	uint64_t startNano = currTimeNano(&now);
	uint64_t nextTickNano = startNano + NSPT;

	int i;
	while (tracks[0].armed) {
		if (currTimeNano(&now) < nextTickNano) {
			continue;
		}
		for (i = 0; i < NUM_TRACKS; i++) {
			MidiEventNode *nextEventNode = tracks[i].nextEventNode;
			if (nextEventNode == NULL)
				continue;
			if (currTick == nextEventNode->event->offTick) {
				stopTrack(i);
			} else if (currTick == nextEventNode->event->onTick) {
				playTrack(i, nextEventNode->event->volume, nextEventNode->event->pan,
						nextEventNode->event->pitch);
			}
		}
		// update time of next tick
		nextTickNano += NSPT;
		// update currTick
		currTick++;
		if (currTick >= loopEndTick) {
			stopAll();
			currTick = loopBeginTick;
		}
	}
}
