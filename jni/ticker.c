#include "nativeaudio.h"

void initTicker() {
  currTick = 0;
  loopTick = 0;
}

jlong Java_com_kh_beatbot_manager_MidiManager_getCurrTick(JNIEnv *env, jclass clazz) {
	return currTick;
}

void Java_com_kh_beatbot_manager_MidiManager_setCurrTick(JNIEnv *env, jclass clazz, jlong _currTick) {
	currTick = _currTick;
}

void Java_com_kh_beatbot_manager_MidiManager_setMSPT(JNIEnv *env, jclass clazz, jlong MSPT) {
	NSPT = MSPT*1000;
}

jlong Java_com_kh_beatbot_manager_MidiManager_getLoopTick(JNIEnv *env, jclass clazz) {
	return loopTick;
}

void Java_com_kh_beatbot_manager_MidiManager_setLoopTick(JNIEnv *env, jclass clazz, jlong _loopTick) {
	loopTick = _loopTick;
}

void Java_com_kh_beatbot_manager_MidiManager_reset(JNIEnv *env, jclass clazz) {
	currTick = -1;
}

uint64_t currTimeNano(struct timespec *now) {
	clock_gettime(CLOCK_MONOTONIC, now);
	return now->tv_sec*1000000000LL + now->tv_nsec;
}

void Java_com_kh_beatbot_manager_MidiManager_startTicking(JNIEnv *env, jclass clazz) {
	struct timespec now;
	
	uint64_t startNano = currTimeNano(&now);
	
	uint64_t nextTickNano = startNano + NSPT;
	
	while (tracks[0].armed) {
		if (currTimeNano(&now) < nextTickNano) {
			continue;
		}
		currTick++;
		if (currTick >= loopTick) {
			stopAll();
			//recordManager.notifyLoop();
			currTick = 0;
		}
		int i;
		for (i = 0; i < numTracks; i++) {
			Track *track = &tracks[i];
			MidiEventNode *midiEventHead = track->eventHead;
			MidiEvent *currEvent = findEvent(midiEventHead, currTick);
			if (currEvent == NULL)
				continue;
		    if (currTick == currEvent->offTick && !currEvent->muted) {
				stopTrack(i);
			} else if (currTick == currEvent->onTick && !currEvent->muted) {
				playTrack(i, currEvent->volume, currEvent->pan, currEvent->pitch);
			}
		}
		// update time of next tick
		nextTickNano += NSPT;
	}
}
