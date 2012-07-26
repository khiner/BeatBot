#include "volpan.h"

VolumePanConfig *volumepanconfig_create(float volume, float pan) {
	VolumePanConfig *p = (VolumePanConfig *) malloc(sizeof(VolumePanConfig));
	p->volume = volume;
	p->pan = pan;
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
