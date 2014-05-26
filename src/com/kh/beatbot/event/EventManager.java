package com.kh.beatbot.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.kh.beatbot.listener.StatefulEventListener;

public class EventManager {
	private static final int MAX_EVENTS = 100;
	private static List<Stateful> events = new ArrayList<Stateful>();
	private static int currEventIndex = -1;
	private static Set<StatefulEventListener> listeners = new HashSet<StatefulEventListener>();

	public static void addListener(StatefulEventListener listener) {
		listeners.add(listener);
	}

	public static boolean hasUndo() {
		return currEventIndex >= 0;
	}
	
	public static boolean hasRedo() {
		return currEventIndex < events.size() - 1;
	}

	public static final void undo() {
		if (hasUndo()) {
			events.get(currEventIndex--).doUndo();	
		}
	}

	public static final void redo() {
		if (hasRedo()) {
			events.get(++currEventIndex).doRedo();
		}
	}

	public static void eventCompleted(Stateful event) {
		currEventIndex++;
		while (events.size() > currEventIndex) {
			events.remove(events.size() - 1);
		}
		events.add(event);
		if (events.size() > MAX_EVENTS) {
			events.remove(0); // drop the oldest event to save space
			currEventIndex--;
		}
		notifyEventCompleted();
	}
	
	private static void notifyEventCompleted() {
		for (StatefulEventListener listener : listeners) {
			listener.onEventCompleted();
		}
	}
}
