package com.odang.beatbot.event;

import com.odang.beatbot.listener.StatefulEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventManager {
    private List<Stateful> events = new ArrayList<Stateful>();
    private int currEventIndex = -1;
    private Set<StatefulEventListener> listeners = new HashSet<StatefulEventListener>();

    public Stateful getLastEvent() {
        return !events.isEmpty() && currEventIndex >= 0 && currEventIndex < events.size() ? events.get(currEventIndex) : null;
    }

    public void clearEvents() {
        events.clear();
        currEventIndex = -1;
        notifyEventCompleted(null);
    }

    public void addListener(StatefulEventListener listener) {
        listeners.add(listener);
    }

    public boolean canUndo() {
        return currEventIndex >= 0;
    }

    public boolean canRedo() {
        return currEventIndex < events.size() - 1;
    }

    public final void undo() {
        if (canUndo()) {
            events.get(currEventIndex--).undo();
            notifyEventCompleted(null);
        }
    }

    public final void redo() {
        if (canRedo()) {
            events.get(++currEventIndex).apply();
            notifyEventCompleted(null);
        }
    }

    public final void jumpTo(int eventIndex) {
        if (eventIndex < 0 || eventIndex >= events.size())
            return;

        while (currEventIndex != eventIndex) {
            if (currEventIndex > eventIndex)
                undo();
            else
                redo();
        }
    }

    public void eventCompleted(Stateful event) {
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

    private void notifyEventCompleted(Stateful event) {
        for (StatefulEventListener listener : listeners) {
            listener.onEventCompleted(event);
        }
    }
}
