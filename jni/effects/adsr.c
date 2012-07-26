#include "adsr.h"

void initAdsrPoints(AdsrConfig *config) {
	config->adsrPoints[0].sampleCents = 0;
	config->adsrPoints[1].sampleCents = 0.25f;
	config->adsrPoints[2].sampleCents = 0.5f;
	config->adsrPoints[3].sampleCents = 0.75f;
	config->adsrPoints[4].sampleCents = 1;
}

AdsrConfig *adsrconfig_create(int totalSamples) {
	AdsrConfig *config = (AdsrConfig *) malloc(sizeof(AdsrConfig));
	initAdsrPoints(config);
	config->active = false;
	config->initial = config->end = 0.0001f;
	config->sustain = 0.6f;
	config->peak = 1.0f;
	resetAdsr(config);
	updateAdsr(config, totalSamples);
	return config;
}

void adsrconfig_destroy(void *p) {
	AdsrConfig *config = (AdsrConfig *) p;
	free(config->adsrPoints);
	free(config);
}

void updateAdsr(AdsrConfig *config, int totalSamples) {
	config->totalSamples = totalSamples;
	int i, length;
	for (i = 0; i < 5; i++) {
		config->adsrPoints[i].sampleNum =
				(int) (config->adsrPoints[i].sampleCents * totalSamples);
	}
	for (i = 0; i < 4; i++) {
		length = config->adsrPoints[i + 1].sampleNum
				- config->adsrPoints[i].sampleNum;
		if (i == 0)
			config->attackCoeff = (1.0f - config->initial) / (length + 1);
		else if (i == 1)
			config->decayCoeff = 1.0f / (length + 1);
		else if (i == 3)
			config->releaseCoeff = (1.0f - config->end) / (length + 1);
	}
	config->gateSample = config->adsrPoints[3].sampleNum;
}

void resetAdsr(AdsrConfig *config) {
	config->currSampleNum = 0;
	config->currLevel = config->initial;
	config->rising = true;
}

/********* JNI METHODS **********/
void Java_com_kh_beatbot_view_SampleWaveformView_setAdsrPoint(JNIEnv *env,
		jclass clazz, jint trackNum, jint adsrPointNum, jfloat x, jfloat y) {
	Track *track = getTrack(env, clazz, trackNum);
	AdsrConfig *config = (AdsrConfig *) track->effects[ADSR_ID].config;
	config->adsrPoints[adsrPointNum].sampleCents = x;
	if (adsrPointNum == 0)
		config->initial = y;
	else if (adsrPointNum == 1)
		config->peak = y;
	else if (adsrPointNum == 2)
		config->sustain = y;
	else if (adsrPointNum == 4)
		config->end = y + 0.00001f;
	updateAdsr(config, config->totalSamples);
}

void Java_com_kh_beatbot_SampleEditActivity_setAdsrOn(JNIEnv *env, jclass clazz,
		jint trackNum, jboolean on) {
	Track *track = getTrack(env, clazz, trackNum);
	track->effects[ADSR_ID].on = on;
}
