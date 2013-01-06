#ifndef MIDIEVENT_H
#define MIDIEVENT_H

typedef struct MidiEvent_ {
	float volume;
	float pan;
	float pitch;
	long onTick;
	long offTick;
} MidiEvent;

MidiEvent *previewEvent;

#endif // MIDIEVENT_H
