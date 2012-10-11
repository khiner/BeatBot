#include "wavfile.h"
#include "../track.h"

//TODO remove
static int count = 0;

static inline float bytesToFloat(unsigned char firstByte,
		unsigned char secondByte) {
	// convert two bytes to one short (little endian)
	short s = (secondByte << 8) | firstByte;
	// convert to range from -1 to (just below) 1
	return s / 32768.0;
}

/**
 * Populate the wavFile float buffers with raw PCM Wav bytes.
 * Works with mono/stereo (for mono, duplicate each sample to the left/right channels)
 * Method for detecting/adapting to stereo/mono adapted from
 * http://stackoverflow.com/questions/8754111/how-to-read-the-data-in-a-wav-file-to-an-array
 */
void wavfile_setBytes(WavFile *wavFile, char *wav, int length) {
	// Determine if mono or stereo
	int channels = wav[22]; // Forget byte 23 as 99.999% of WAVs are 1 or 2 channels

	// Get past all the other sub chunks to get to the data subchunk:
	int pos = 12; // First Subchunk ID from 12 to 16

	// Keep iterating until we find the data chunk (i.e. 64 61 74 61 ...... (i.e. 100 97 116 97 in decimal))
	while (wav[pos] != 100 || wav[pos + 1] != 97 || wav[pos + 2] != 116
			|| wav[pos + 3] != 97) {
		pos += 4;
		int chunkSize = wav[pos] + wav[pos + 1] * 256 + wav[pos + 2] * 65536
				+ wav[pos + 3] * 16777216;
		pos += 4 + chunkSize;
	}
	pos += 8;

	if (channels == 1) {
		length -= 32; // don't know why the end of mono sample is garbage
	}

	// pos is now positioned to start of actual sound data.
	wavFile->totalSamples = (length - pos) / 2; // 2 bytes per sample (16 bit sound mono)
	if (channels == 2)
		wavFile->totalSamples /= 2; // 4 bytes per sample (16 bit stereo)

	// TODO : change to passed-in file name - also want to write all files in the beginning,
	// not dynamically
	char sampleName[30];
	char suffix[10];
	sprintf(suffix, "%d", count++);
	strcpy(sampleName, "/mnt/sdcard/BeatBot/");
	strcat(sampleName, suffix);
	wavFile->sampleFile = fopen(sampleName, "wb+");
	int i = 0;
	while (pos < length) {
		float val = bytesToFloat(wav[pos], wav[pos + 1]);
		fwrite(&val, 1, sizeof(float), wavFile->sampleFile);
		pos += 2;
		if (channels == 2) {
			pos += 2;
		}
		val = bytesToFloat(wav[pos], wav[pos + 1]);
		fwrite(&val, 1, sizeof(float), wavFile->sampleFile);
		i++;
	}
	fflush(wavFile->sampleFile);
	fclose(wavFile->sampleFile);
	wavFile->sampleFile = fopen(sampleName, "rb");
	// init loop / currSample position data
	wavFile->loopBegin = wavFile->currSample = 0;
	wavFile->loopEnd = wavFile->totalSamples - 2;
	wavFile->loopLength = wavFile->loopEnd - wavFile->loopBegin;
	// free(wav); //taken care of by Release JNI method?
	//wav = NULL;
}

WavFile *wavfile_create(char *bytes, int length) {
	WavFile *wavFile = (WavFile *) malloc(sizeof(WavFile));
	pthread_mutex_init(&wavFile->bufferMutex, NULL);
	wavfile_setBytes(wavFile, bytes, length);
	wavFile->looping = wavFile->reverse = false;
	return wavFile;
}

void wavfile_reset(WavFile *config) {
	config->currSample = config->reverse ? config->loopEnd : config->loopBegin;
	fseek(config->sampleFile, config->currSample * TWO_FLOAT_SZ, SEEK_SET);
}

void wavfile_destroy(void *p) {
	WavFile *config = (WavFile *) p;
	fflush(config->sampleFile);
	fclose(config->sampleFile);
	free(config);
	config = NULL;
}
