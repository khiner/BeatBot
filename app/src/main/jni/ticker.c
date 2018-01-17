#include "all.h"

void initTicker() {
    currSample = loopBeginSample = loopEndSample = 0;
}

jlong Java_com_odang_beatbot_manager_MidiManager_getCurrTick(JNIEnv *env,
                                                             jclass clazz) {
    return (jlong) (currSample / samplesPerTick);
}

void Java_com_odang_beatbot_manager_MidiManager_setNativeMSPT(JNIEnv *env,
                                                              jclass clazz, jlong MSPT) {
    float prevCurrTick = currSample / samplesPerTick;
    // MSPT = microseconds-per-tick
    samplesPerTick = ((float) MSPT * SAMPLE_RATE) / (float) 1000000;
    // Invariant: current tick must move forward only, until end of loop
    currSample = (long) (prevCurrTick * samplesPerTick);
}

void Java_com_odang_beatbot_manager_MidiManager_setLoopTicksNative(JNIEnv *env,
                                                                   jclass clazz,
                                                                   jlong _loopBeginTick,
                                                                   jlong _loopEndTick) {
    if (_loopBeginTick >= _loopEndTick)
        return;
    loopBeginSample = (long) (_loopBeginTick * samplesPerTick);
    loopEndSample = (long) (_loopEndTick * samplesPerTick);
    if (!isPlaying()) {
        currSample = loopBeginSample;
    }
}
