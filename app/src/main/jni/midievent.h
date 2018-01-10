#ifndef MIDIEVENT_H
#define MIDIEVENT_H

typedef struct MidiEvent_ {
	float volume;
	float pan;
	float pitchSteps;
} MidiEvent;

MidiEvent *previewEvent;

#endif // MIDIEVENT_H
