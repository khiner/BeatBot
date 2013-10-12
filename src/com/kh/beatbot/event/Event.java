package com.kh.beatbot.event;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.ui.view.page.Page;

public abstract class Event {
	private static final int MAX_EVENTS = 100;
	private static List<StateEvent> events = new ArrayList<StateEvent>();
	private static int currEventIndex = -1;

	public static final void undo() {
		if (events.isEmpty() || events.size() <= currEventIndex) {
			return;
		}
		events.get(currEventIndex--).doUndo();
		updateUi();
	}

	public static final void redo() {
		if (events.isEmpty() || currEventIndex >= events.size() - 1) {
			return;
		}
		events.get(++currEventIndex).doRedo();
		updateUi();
	}

	protected static void eventCompleted(StateEvent event) {
		currEventIndex++;
		while (events.size() > currEventIndex) {
			events.remove(events.size() - 1);
		}
		events.add(event);
		if (events.size() > MAX_EVENTS) {
			events.remove(0); // drop the oldest event to save space
			currEventIndex--;
		}
		updateUi();
	}

	private static void updateUi() {
		Page.mainPage.controlButtonGroup.setUndoIconEnabled(currEventIndex >= 0);
		Page.mainPage.controlButtonGroup.setRedoIconEnabled(currEventIndex < events.size() - 1);
	}

	public abstract void execute();
}
