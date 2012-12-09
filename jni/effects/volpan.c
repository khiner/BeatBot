#include "../all.h"

VolumePanConfig *volumepanconfig_create() {
	VolumePanConfig *p = (VolumePanConfig *) malloc(sizeof(VolumePanConfig));
	p->volume = .8f;
	p->pan = .5f;
	return p;
}

void volumepanconfig_set(void *p, float volume, float pan) {
	VolumePanConfig *config = (VolumePanConfig *) p;
	config->volume = volume;
	config->pan = pan;
}

void volumepanconfig_destroy(void *p) {
	if (p != NULL)
		free((VolumePanConfig *) p);
}
