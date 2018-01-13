#include "all.h"

void initTicker() {
    currSample = currTick = loopBeginTick = loopEndTick = 0;
}

jlong Java_com_odang_beatbot_manager_MidiManager_getCurrTick(JNIEnv *env,
                                                             jclass clazz) {
    return currTick;
}

void Java_com_odang_beatbot_manager_MidiManager_setNativeMSPT(JNIEnv *env,
                                                              jclass clazz, jlong MSPT) {
    // MSPT = microseconds-per-tick
    samplesPerTick = (MSPT * SAMPLE_RATE) / 1000000;
}

void Java_com_odang_beatbot_manager_MidiManager_setLoopTicksNative(JNIEnv *env,
                                                                   jclass clazz,
                                                                   jlong _loopBeginTick,
                                                                   jlong _loopEndTick) {
    if (_loopBeginTick >= _loopEndTick)
        return;
    loopBeginTick = _loopBeginTick;
    loopEndTick = _loopEndTick;
    if (!isPlaying()) {
        currTick = loopBeginTick;
    }
}


