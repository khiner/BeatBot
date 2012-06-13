#include "nativeaudio.h"

void initTicker() {
  currTick = 0;
  loopBeginTick = 0;
  loopEndTick = 0;  
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

jlong Java_com_kh_beatbot_manager_MidiManager_getLoopBeginTick(JNIEnv *env, jclass clazz) {
	return loopBeginTick;
}

void Java_com_kh_beatbot_manager_MidiManager_setLoopBeginTick(JNIEnv *env, jclass clazz, jlong _loopBeginTick) {
	if (_loopBeginTick >= loopEndTick)
		return;
		
	loopBeginTick = _loopBeginTick;
	if (!tracks[0].armed) // hack to see if we're playing
		currTick = loopBeginTick;
}

jlong Java_com_kh_beatbot_manager_MidiManager_getLoopEndTick(JNIEnv *env, jclass clazz) {
	return loopEndTick;
}

void Java_com_kh_beatbot_manager_MidiManager_setLoopEndTick(JNIEnv *env, jclass clazz, jlong _loopEndTick) {
	if (_loopEndTick <= loopBeginTick)
		return;
		 
	loopEndTick = _loopEndTick;
}

void Java_com_kh_beatbot_manager_MidiManager_reset(JNIEnv *env, jclass clazz) {
	currTick = loopBeginTick - 1;
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
		if (currTick >= loopEndTick) {
			stopAll();
			//recordManager.notifyLoop();
			currTick = loopBeginTick;
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
