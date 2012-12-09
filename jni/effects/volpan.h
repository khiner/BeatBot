#ifndef VOLPAN_H
#define VOLPAN_H

typedef struct VolumePanConfig_t {
	float volume;
	float pan;
} VolumePanConfig;

VolumePanConfig *volumepanconfig_create();
void volumepanconfig_set(void *config, float volume, float pan);

static inline void volumepan_process(VolumePanConfig *config, float **buffers, int size) {
	float leftVolume = (1 - config->pan) * config->volume;
	float rightVolume = config->pan * config->volume;
	int i;
	for (i = 0; i < size; i++) {
		buffers[0][i] *= leftVolume; // left channel
	}
	for (i = 0; i < size; i++) {
		buffers[1][i] *= rightVolume; // right channel
	}
}

void volumepanconfig_destroy(void *config);

#endif // VOLPAN_H
