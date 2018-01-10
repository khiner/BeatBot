#ifndef JNI_LOAD_H
#define JNI_LOAD_H

#include <jni.h>
#include <stdlib.h>
#include <pthread.h>

JNIEnv *getJniEnv();
JavaVM* getJavaVm();

jobject getTrackManager();
jobject getRecordManager();
jmethodID getNextMidiNoteMethod();
jmethodID getNotifyRecordSourceBufferFilledMethod();
jmethodID getStartRecordingJavaMethod();

#endif // JNI_LOAD_H
