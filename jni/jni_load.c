#include "jni_load.h"
#include <android/log.h>

jobject trackManager = NULL;
jobject recordManager = NULL;
jmethodID getNextMidiNote = NULL;
jmethodID notifyRecordSourceBufferFilled = NULL;
jmethodID startRecordingJava = NULL;
JavaVM* javaVm = NULL;
pthread_key_t current_jni_env;

void detach_current_thread(void *env) {
  __android_log_print(ANDROID_LOG_INFO, "jni", "detaching");
  (*javaVm)->DetachCurrentThread(javaVm);
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
  JNIEnv *env = NULL;
  javaVm = vm;
  if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_6) != JNI_OK) {
    return -1;
  }

  pthread_key_create(&current_jni_env, detach_current_thread);
  return JNI_VERSION_1_6;
}

JNIEnv *getJniEnv() {
 JNIEnv *env;
 if ((env = pthread_getspecific(current_jni_env)) == NULL) {
	 (*javaVm)->AttachCurrentThread(javaVm, &env, NULL );
     pthread_setspecific(current_jni_env, env);
 }
 return env;
}

jobject getTrackManager() {
	return trackManager;
}

jobject getRecordManager() {
	return recordManager;
}

jmethodID getNextMidiNoteMethod() {
	return getNextMidiNote;
}

jmethodID getNotifyRecordSourceBufferFilledMethod() {
	return notifyRecordSourceBufferFilled;
}

jmethodID getStartRecordingJavaMethod() {
	return startRecordingJava;
}

JavaVM* getJavaVm() {
	return javaVm;
}

void Java_com_odang_beatbot_activity_BeatBotActivity_onRecordManagerInit(
		JNIEnv *env, jclass clazz, jobject _recordManager) {
	recordManager = (jobject) (*env)->NewGlobalRef(env, _recordManager);
	jclass recordManagerClass = (*env)->GetObjectClass(env, recordManager);
	notifyRecordSourceBufferFilled = (*env)->GetMethodID(env,
			recordManagerClass, "notifyRecordSourceBufferFilled", "(F)V");
	startRecordingJava = (*env)->GetMethodID(env, recordManagerClass,
			"startRecording", "()Ljava/lang/String;");
}

void Java_com_odang_beatbot_activity_BeatBotActivity_onTrackManagerInit(
		JNIEnv *env, jclass clazz, jobject _trackManager) {
	trackManager = (jobject) (*env)->NewGlobalRef(env, _trackManager);
	jclass trackManagerClass = (*env)->GetObjectClass(env, trackManager);
	getNextMidiNote = (*env)->GetMethodID(env, trackManagerClass,
			"getNextMidiNote", "(IJ)Lcom/odang/beatbot/midi/MidiNote;");
}
