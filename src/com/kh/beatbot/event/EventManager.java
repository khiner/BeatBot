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

	public static List<Stateful> getEvents() {
		return events;
	}

	public static void addListener(StatefulEventListener listener) {
		listeners.add(listener);
	}

	public static boolean canUndo() {
		return currEventIndex >= 0;
	}
	
	public static boolean canRedo() {
		return currEventIndex < events.size() - 1;
	}

	public static final void undo() {
		if (canUndo()) {
			events.get(currEventIndex--).undo();	
		}
	}

	public static final void redo() {
		if (canRedo()) {
			events.get(++currEventIndex).redo();
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
		notifyEventCompleted(event);
	}

	private static void notifyEventCompleted(Stateful event) {
		for (StatefulEventListener listener : listeners) {
			listener.onEventCompleted(event);
		}
	}
}
