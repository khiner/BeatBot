#ifndef MIDIEVENT_H
#define MIDIEVENT_H

typedef struct MidiEvent_ {
	float volume;
	float pan;
	float pitch;
	long onTick;
	long offTick;
	bool muted;
} MidiEvent;

typedef struct MidiEventNode_ {
	MidiEvent *event;
	struct MidiEventNode_ *next;
} MidiEventNode;

#endif // MIDIEVENT_H
