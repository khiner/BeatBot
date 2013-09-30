package com.kh.beatbot.event;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.ui.view.page.Page;

public abstract class Event {
	private static final int MAX_EVENTS = 150;
	private static List<Event> events = new ArrayList<Event>();
	private static int currEventIndex = -1;

	public static final void undo() {
		if (events.isEmpty() || events.size() <= currEventIndex) {
			return;
		}

		events.get(currEventIndex--).doUndo();
		if (currEventIndex < 0) {
			Page.mainPage.controlButtonGroup.setUndoIconEnabled(false);
		}
	}

	public static final void redo() {
		if (events.isEmpty() || currEventIndex >= events.size() - 1) {
			return;
		}
		
		events.get(++currEventIndex).doExecute();
		if (currEventIndex >= events.size()) {
			Page.mainPage.controlButtonGroup.setRedoIconEnabled(false);	
		}
	}

	protected static void eventCompleted(Event event) {
		currEventIndex++;
		while (events.size() > currEventIndex) {
			events.remove(events.size() - 1);
		}
		events.add(event);
		if (events.size() > MAX_EVENTS) {
			events.remove(0); // drop the oldest event to save space
		}
		Page.mainPage.controlButtonGroup.setUndoIconEnabled(true);
		Page.mainPage.controlButtonGroup.setRedoIconEnabled(false);
	}

	protected abstract void doExecute();
	protected abstract void doUndo();
	protected abstract Event opposite();
}
