package com.kh.beatbot.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kh.beatbot.event.midinotes.MidiNotesDiffEvent;
import com.kh.beatbot.listener.StatefulEventListener;

@SuppressWarnings("rawtypes")
public class EventManager {
	
	private static final List<Class> serializableEventClasses = new ArrayList<Class>() {
		{
			add(MidiNotesDiffEvent.class);
			add(SampleLoopWindowSetEvent.class);
		}
	};

	private static Map<String, Class<Stateful>> serializableClassFromName = new HashMap<String, Class<Stateful>>() {
		{
			for (Class<Stateful> serializableEventClass : serializableEventClasses) {
				put(serializableEventClass.getName(), serializableEventClass);
			}
		}
	};

	private static List<Stateful> events = new ArrayList<Stateful>();
	private static int currEventIndex = -1;
	private static Set<StatefulEventListener> listeners = new HashSet<StatefulEventListener>();

	public static List<Stateful> getEvents() {
		return events;
	}

	// some events (such as sample-rename) can be in the undo-stack, but not saved
	// (permanent since they affect the filesystem)
	public static List<Stateful> getSerializableEvents() {
		List<Stateful> serializableEvents = new ArrayList<Stateful>();
		for (Stateful event : events) {
			if (isSerializable(event)) {
				serializableEvents.add(event);
			}
		}

		return serializableEvents;
	}

	public static int getCurrentEventIndex() {
		return currEventIndex;
	}

	// some undoable events are not serializable
	public static int getCurrentSerializableEventIndex() {
		if (currEventIndex < 0)
			return -1;

		Stateful currEvent = events.get(currEventIndex);

		int currSerializableEventIndex = -1;
		for (Stateful event: events) {
			if (isSerializable(event))
				currSerializableEventIndex++;
			if (event.equals(currEvent))
				break;
		}
		
		return currSerializableEventIndex;
	}

	public static void clearEvents() {
		events.clear();
		currEventIndex = -1;
		notifyEventCompleted(null);
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
			notifyEventCompleted(null);
		}
	}

	public static final void redo() {
		if (canRedo()) {
			events.get(++currEventIndex).apply();
			notifyEventCompleted(null);
		}
	}

	public static final void jumpTo(int eventIndex) {
		if (eventIndex < 0 || eventIndex >= events.size())
			return;

		while (currEventIndex != eventIndex) {
			if (currEventIndex > eventIndex)
				undo();
			else
				redo();
		}
	}

	public static void eventCompleted(Stateful event) {
		currEventIndex++;
		while (events.size() > currEventIndex) {
			events.remove(events.size() - 1);
		}
		events.add(event);

		// Not limiting events since project saves depend on full history being here
		// if (events.size() > MAX_EVENTS) {
		// events.remove(0); // drop the oldest event to save space
		// currEventIndex--;
		// }
		notifyEventCompleted(event);
	}

	public static boolean isSerializable(Stateful event) {
		return event != null && serializableEventClasses.contains(event.getClass());
	}

	public static Class<Stateful> getSerializableClass(String className) {
		return serializableClassFromName.get(className);
	}

	private static void notifyEventCompleted(Stateful event) {
		for (StatefulEventListener listener : listeners) {
			listener.onEventCompleted(event);
		}
	}
}
