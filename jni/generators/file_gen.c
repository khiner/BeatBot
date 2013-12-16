#include "../all.h"
#include "libsndfile/sndfile.h"

void filegen_setSampleFile(FileGen *config, const char *sampleFileName) {
	SNDFILE *infile;
	SF_INFO sfinfo;

	sf_close(config->sampleFile);

	if (!(infile = sf_open(sampleFileName, SFM_READ, &sfinfo))) { /* Open failed so print an error message. */
		return;
	};

	if (sfinfo.channels > MAX_CHANNELS) {
		return;
	};

	config->channels = sfinfo.channels;
	config->frames = sfinfo.frames;
	config->sampleFile = infile;
	config->buffer = malloc(BUFF_SIZE * config->channels * ONE_FLOAT_SZ);
	config->loopBegin = 0;
	config->loopEnd = config->frames;
	if (config->currFrame > config->loopEnd) {
		config->currFrame = config->loopEnd;
	} else if (config->currFrame < config->loopBegin) {
		config->currFrame = config->loopBegin;
	}
	config->loopLength = config->loopEnd - config->loopBegin;

	config->bufferStartFrame = LONG_MIN; // forces the buffer to be reloaded
}

FileGen *filegen_create() {
	FileGen *fileGen = (FileGen *) malloc(sizeof(FileGen));
	pthread_mutex_init(&fileGen->fileMutex, NULL );
	fileGen->currFrame = 0;
	fileGen->gain = 1;
	fileGen->sampleFile = NULL;
	fileGen->looping = fileGen->reverse = false;
	fileGen->adsr = adsrconfig_create();
	return fileGen;
}

float filegen_getSample(FileGen *fileGen, long frame, int channel) {
	pthread_mutex_lock(&fileGen->fileMutex);
	sf_seek(fileGen->sampleFile, frame, SEEK_SET);
	sf_readf_float(fileGen->sampleFile, fileGen->tempSample, 1);
	pthread_mutex_unlock(&fileGen->fileMutex);

	return fileGen->tempSample[channel] * fileGen->gain;
}

void filegen_setLoopWindow(FileGen *fileGen, long loopBeginSample,
		long loopEndSample) {
	if (fileGen->loopBegin == loopBeginSample
			&& fileGen->loopEnd == loopEndSample)
		return;
	fileGen->loopBegin = loopBeginSample;
	fileGen->loopEnd = loopEndSample;
	fileGen->loopLength = fileGen->loopEnd - fileGen->loopBegin;
}

void filegen_setReverse(FileGen *fileGen, bool reverse) {
	fileGen->reverse = reverse;
	// if the track is not looping, the fileGen generator will not loop to the beginning/end
	// after enabling/disabling reverse
	if (reverse && fileGen->currFrame == fileGen->loopBegin)
		fileGen->currFrame = fileGen->loopEnd;
	else if (!reverse && fileGen->currFrame == fileGen->loopEnd)
		fileGen->currFrame = fileGen->loopBegin;
}

void filegen_reset(FileGen *config) {
	config->currFrame = config->reverse ? config->loopEnd : config->loopBegin;
	resetAdsr(config->adsr);
}

void filegen_destroy(void *p) {
	FileGen *fileGen = (FileGen *) p;
	pthread_mutex_destroy(&fileGen->fileMutex);
	if (fileGen->sampleFile != NULL ) {
		sf_close(fileGen->sampleFile);
		fileGen->sampleFile = NULL;
	}
	adsrconfig_destroy(fileGen->adsr);
	free(fileGen);
	fileGen = NULL;
}
