#ifndef JNI_LOAD_H
#define JNI_LOAD_H

#include <jni.h>
#include <stdlib.h>
#include <pthread.h>

JNIEnv *getJniEnv();

jclass getTrackClass();
jclass getRecordManagerClass();
jmethodID getNextMidiNoteMethod();
jmethodID getNotifyRecordSourceBufferFilledMethod();
jmethodID getStartRecordingJavaMethod();
JavaVM* getJavaVm();

#endif // JNI_LOAD_H
