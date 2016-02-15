#include "jni_load.h"

jobject trackManager = NULL;
jobject recordManager = NULL;
jmethodID getNextMidiNote = NULL;
jmethodID notifyRecordSourceBufferFilled = NULL;
jmethodID startRecordingJava = NULL;
JavaVM* javaVm = NULL;

JNIEnv *getJniEnv() {
	JNIEnv* env;
	if ((*javaVm)->GetEnv(javaVm, (void**) &env,
			JNI_VERSION_1_6) == JNI_EDETACHED) {
		(*javaVm)->AttachCurrentThread(javaVm, &env, NULL );
	}
	return env;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_6) != JNI_OK)
		return -1;

	javaVm = vm;
	return JNI_VERSION_1_6;
}

jclass getTrackManager() {
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

void Java_com_kh_beatbot_activity_BeatBotActivity_onRecordManagerInit(
		JNIEnv *env, jclass clazz, jobject _recordManager) {
	recordManager = (jobject) (*env)->NewGlobalRef(env, _recordManager);
	jclass recordManagerClass = (*env)->GetObjectClass(env, recordManager);
	notifyRecordSourceBufferFilled = (*env)->GetMethodID(env,
			recordManagerClass, "notifyRecordSourceBufferFilled", "(F)V");
	startRecordingJava = (*env)->GetMethodID(env, recordManagerClass,
			"startRecording", "()Ljava/lang/String;");
}

void Java_com_kh_beatbot_activity_BeatBotActivity_onTrackManagerInit(
		JNIEnv *env, jclass clazz, jobject _trackManager) {
	trackManager = (jobject) (*env)->NewGlobalRef(env, _trackManager);
	jclass trackManagerClass = (*env)->GetObjectClass(env, trackManager);
	getNextMidiNote = (*env)->GetMethodID(env, trackManagerClass,
			"getNextMidiNote", "(IJ)Lcom/kh/beatbot/midi/MidiNote;");
}
