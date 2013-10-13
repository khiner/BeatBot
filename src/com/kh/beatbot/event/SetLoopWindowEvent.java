package com.kh.beatbot.event;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.view.helper.TickWindowHelper;
import com.kh.beatbot.ui.view.page.Page;

public class SetLoopWindowEvent implements Stateful, Temporal {

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
		updateUi();
	}

	@Override
	public void doRedo() {
		MidiManager.setLoopTicks(finalBeginTick, finalEndTick);
		updateUi();
	}

	@Override
	public void updateUi() {
		Page.mainPage.midiView.updateLoopUi();
		TickWindowHelper.updateView(finalBeginTick, finalEndTick);
	}
}
