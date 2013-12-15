#include "../all.h"
#include "libsndfile/sndfile.h"

static const char* rawExtension = ".raw";
static float data[BUFF_SIZE];

static inline int fileExists(char *filename) {
	struct stat buffer;
	return (stat(filename, &buffer) == 0);
}

void filegen_freeBuffers(FileGen *fileGen) {
	// if this sample was short enough, it was also loaded into memory, and will be non-null
	if (fileGen->samples != NULL ) {
		// use temp sample so there is no period when fileGen->samples is non-NULL but has freed memory
		float **temp = fileGen->samples;
		fileGen->samples = NULL;
		free(temp[0]);
		free(temp[1]);
		free(temp);
		temp = NULL;
	}
}

void filegen_setSampleFile(FileGen *config, const char *sampleFileName) {
	SNDFILE *infile;
	SF_INFO sfinfo;
	int readcount, samp = 0, chan = 0, i = 0;

	if (!(infile = sf_open(sampleFileName, SFM_READ, &sfinfo))) { /* Open failed so print an error message. */
		__android_log_print(ANDROID_LOG_ERROR, "sndfile error",
				"Not able to open input file %s: %s.\n", sampleFileName,
				sf_strerror(NULL ));
		return;
	};

	if (sfinfo.channels > MAX_CHANNELS) {
		__android_log_print(ANDROID_LOG_ERROR, "filegen",
				"Not able to process more than %d channels\n", MAX_CHANNELS);
		return;
	};

	config->channels = sfinfo.channels;
	config->frames = sfinfo.frames;

	// if a different sample was already loaded, destroy it.
	filegen_freeBuffers(config);

	if (false) {
		/** allocate memory to hold samples (memory is freed in filegen_destroy)
		 *
		 * NOTE: We don't directly write to fileGen sample buffer because we want to
		 * completely load all samples from file before making fileGen->samples non-NULL.
		 * That way, we can still immediately read from file on a per-sample basis, until
		 * fileGen->samples is non-null, at which point we can read directly from memory.
		 */
		float **tempSamples = malloc(2 * sizeof(void *));
		tempSamples[0] = malloc(config->frames * ONE_FLOAT_SZ);
		// (right channel will be null if only mono sound)
		tempSamples[1] =
				config->channels == 2 ?
						malloc(config->frames * ONE_FLOAT_SZ) : NULL;

		// write to fileGen float buffers
		while ((readcount = sf_read_float(infile, data, BUFF_SIZE))) {
			for (i = 0; i < readcount;) {
				for (chan = 0; chan < config->channels; chan++) {
					tempSamples[chan][samp] = data[i++];
				}
				samp++;
			}
		};
		// make fileGen->samples point to fully-loaded sample buffer in memory
		config->samples = tempSamples;
		// cleanup
		tempSamples = NULL;
	} else { // file too big for memory. write to temp file in external storage
		// concat sampleFileName with ".raw" extension
		config->sampleFileName = malloc(strlen(sampleFileName) + 1 + 4);
		strcpy(config->sampleFileName, sampleFileName); /* copy name into the new var */
		strcat(config->sampleFileName, rawExtension); /* add the extension */

		if (!fileExists(config->sampleFileName)) {
			// open *.raw file next to sample file - we will read directly from this file
			FILE *tempFile = fopen(config->sampleFileName, "wb");

			while ((readcount = sf_read_float(infile, data, BUFF_SIZE))) {
				for (i = 0; i < readcount; i++) {
					fwrite(&data[i], ONE_FLOAT_SZ, 1, tempFile);
				}
			};
			fflush(tempFile);
			fclose(tempFile);
		} else {
			__android_log_print(ANDROID_LOG_INFO, "filegen",
					"Reusing existing sample");
		}
		config->sampleFile = fopen(config->sampleFileName, "rb");
	}

	sf_close(infile);

	// init loop / currSample position data
	config->loopBegin = 0;
	config->loopEnd = config->frames;
	if (config->currSample >= config->loopEnd) {
		// TODO maybe should be currSample = loopEnd
		// TODO this could cause unwanted repeat
		config->currSample = 0;
	}
	config->loopLength = config->loopEnd - config->loopBegin;
}

FileGen *filegen_create(const char *sampleName) {
	FileGen *fileGen = (FileGen *) malloc(sizeof(FileGen));
	fileGen->currSample = 0;
	fileGen->gain = 1;
	fileGen->samples = NULL;
	fileGen->sampleFile = NULL;
	fileGen->looping = fileGen->reverse = false;
	filegen_setSampleFile(fileGen, sampleName);
	fileGen->adsr = adsrconfig_create();
	return fileGen;
}

float filegen_getSample(FileGen *fileGen, int sampleIndex, int channel) {
	float ret;
	if (fileGen->samples != NULL ) {
		ret = fileGen->samples[channel][sampleIndex];
	} else {
		fseek(fileGen->sampleFile,
				sampleIndex * fileGen->channels * ONE_FLOAT_SZ, SEEK_SET);
		fread(&ret, 1, ONE_FLOAT_SZ, fileGen->sampleFile);
	}
	return ret * fileGen->gain;
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
	if (reverse && fileGen->currSample == fileGen->loopBegin)
		fileGen->currSample = fileGen->loopEnd;
	else if (!reverse && fileGen->currSample == fileGen->loopEnd)
		fileGen->currSample = fileGen->loopBegin;
}

void filegen_reset(FileGen *config) {
	config->currSample = config->reverse ? config->loopEnd : config->loopBegin;
	resetAdsr(config->adsr);
}

void filegen_destroy(void *p) {
	FileGen *fileGen = (FileGen *) p;
	if (fileGen->sampleFile != NULL ) {
		fflush(fileGen->sampleFile);
		fclose(fileGen->sampleFile);
		fileGen->sampleFile = NULL;
		free(fileGen->sampleFileName);
	}
	filegen_freeBuffers(fileGen);
	adsrconfig_destroy(fileGen->adsr);
	free(fileGen);
	fileGen = NULL;
}
