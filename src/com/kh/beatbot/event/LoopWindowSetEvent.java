package com.kh.beatbot.event;

import com.kh.beatbot.manager.MidiManager;

public class LoopWindowSetEvent implements Stateful, Temporal {

	private long initialBeginTick, initialEndTick, finalBeginTick, finalEndTick;

	@Override
	public void begin() {
		initialBeginTick = MidiManager.getLoopBeginTick();
		initialEndTick = MidiManager.getLoopEndTick();
	}

	@Override
	public void end() {
		finalBeginTick = MidiManager.getLoopBeginTick();
		finalEndTick = MidiManager.getLoopEndTick();

		if (initialBeginTick != finalBeginTick || initialEndTick != finalEndTick) {
			EventManager.eventCompleted(this);
		}
	}

	@Override
	public void doUndo() {
		MidiManager.setLoopTicks(initialBeginTick, initialEndTick);
	}

	@Override
	public void doRedo() {
		MidiManager.setLoopTicks(finalBeginTick, finalEndTick);
	}
}
