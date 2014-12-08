#include "jni_load.h"

jclass trackClass = NULL;
jclass recordManagerClass = NULL;
jmethodID getNextMidiNote = NULL;
jmethodID notifyRecordSourceBufferFilled = NULL;
jmethodID startRecordingJava = NULL;
JavaVM* javaVm = NULL;

JNIEnv *getJniEnv() {
	JNIEnv* env;
	if ((*javaVm)->GetEnv(javaVm, (void**) &env, JNI_VERSION_1_6)
			== JNI_EDETACHED) {
		(*javaVm)->AttachCurrentThread(javaVm, &env, NULL);
	}
	return env;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_6) != JNI_OK)
		return -1;

	trackClass = (*env)->NewGlobalRef(env,
			(*env)->FindClass(env, "com/kh/beatbot/manager/TrackManager"));
	getNextMidiNote = (*env)->GetStaticMethodID(env, trackClass,
			"getNextMidiNote", "(IJ)Lcom/kh/beatbot/midi/MidiNote;");

	recordManagerClass = (*env)->NewGlobalRef(env,
			(*env)->FindClass(env, "com/kh/beatbot/manager/RecordManager"));
	notifyRecordSourceBufferFilled = (*env)->GetStaticMethodID(env,
			recordManagerClass, "notifyRecordSourceBufferFilled", "(F)V");

	startRecordingJava = (*env)->GetStaticMethodID(env, recordManagerClass,
			"startRecording", "()Ljava/lang/String;");

	javaVm = vm;
	return JNI_VERSION_1_6;
}

jclass getTrackClass() {

}

jclass getRecordManagerClass() {

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
