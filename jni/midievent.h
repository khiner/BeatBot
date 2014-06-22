#ifndef MIDIEVENT_H
#define MIDIEVENT_H

typedef struct MidiEvent_ {
	unsigned char volume;
	unsigned char pan;
	unsigned char pitch;
} MidiEvent;

MidiEvent *previewEvent;

#endif // MIDIEVENT_H
