package com.kh.beatbot.event;

import com.kh.beatbot.ui.view.View;

public class LoopWindowSetEvent implements Stateful, Temporal {
	private long initialBeginTick, initialEndTick, finalBeginTick, finalEndTick;

	@Override
	public void begin() {
		initialBeginTick = View.context.getMidiManager().getLoopBeginTick();
		initialEndTick = View.context.getMidiManager().getLoopEndTick();
	}

	@Override
	public void end() {
		finalBeginTick = View.context.getMidiManager().getLoopBeginTick();
		finalEndTick = View.context.getMidiManager().getLoopEndTick();

		if (initialBeginTick != finalBeginTick || initialEndTick != finalEndTick) {
			EventManager.eventCompleted(this);
		}
	}

	@Override
	public void undo() {
		View.context.getMidiManager().setLoopTicks(initialBeginTick, initialEndTick);
	}

	@Override
	public void apply() {
		View.context.getMidiManager().setLoopTicks(finalBeginTick, finalEndTick);
	}
}
