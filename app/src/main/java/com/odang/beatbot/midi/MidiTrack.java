//////////////////////////////////////////////////////////////////////////////
//	Copyright 2011 Alex Leffelman
//	
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//	
//	http://www.apache.org/licenses/LICENSE-2.0
//	
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//////////////////////////////////////////////////////////////////////////////

package com.odang.beatbot.midi;

import com.odang.beatbot.midi.event.MidiEvent;
import com.odang.beatbot.midi.event.meta.EndOfTrack;
import com.odang.beatbot.midi.event.meta.Tempo;
import com.odang.beatbot.midi.event.meta.TimeSignature;
import com.odang.beatbot.midi.util.MidiUtil;
import com.odang.beatbot.midi.util.VariableLengthInt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class MidiTrack {

    public static final byte[] IDENTIFIER = {'M', 'T', 'r', 'k'};

    private int mSize;
    private boolean mSizeNeedsRecalculating;
    private boolean mClosed;

    private ArrayList<MidiEvent> mEvents;

    public static MidiTrack createTempoTrack() {
        final MidiTrack midiTrack = new MidiTrack();
        midiTrack.insertEvent(new TimeSignature());
        midiTrack.insertEvent(new Tempo());

        return midiTrack;
    }

    public MidiTrack() {
        mEvents = new ArrayList<MidiEvent>();
        mSize = 0;
        mSizeNeedsRecalculating = false;
        mClosed = false;
    }

    public MidiTrack(InputStream in) throws IOException {
        this();

        byte[] buffer = new byte[32];
        in.read(buffer, 0, 4);

        if (!MidiUtil.bytesEqual(buffer, IDENTIFIER, 0, 4)) {
            System.err.println("Track identifier did not match MTrk!");
            return;
        }

        in.read(buffer, 0, 4);
        mSize = MidiUtil.bytesToInt(buffer, 0, 4);

        long totalTicks = 0;

        while (true) {
            VariableLengthInt delta = new VariableLengthInt(in);
            totalTicks += delta.getValue();

            MidiEvent event = MidiEvent.parseEvent(totalTicks, delta.getValue(), in);
            if (event == null) {
                System.out.println("Event skipped!");
                continue;
            }

            // Not adding the EndOfTrack event here allows the track to be
            // edited
            // after being read in from file.
            if (event.getClass().equals(EndOfTrack.class)) {
                break;
            }
            mEvents.add(event);
        }
    }

    public ArrayList<MidiEvent> getEvents() {
        return mEvents;
    }

    public int getEventCount() {
        return mEvents.size();
    }

    public int getSize() {
        if (mSizeNeedsRecalculating) {
            recalculateSize();
        }
        return mSize;
    }

    public long getLengthInTicks() {
        if (mEvents.size() == 0) {
            return 0;
        }

        MidiEvent E = mEvents.get(mEvents.size() - 1);
        return E.getTick();
    }

    public void insertEvent(MidiEvent newEvent) {

        if (newEvent == null) {
            return;
        }

        if (mClosed) {
            System.err.println("Error: Cannot add an event to a closed track.");
            return;
        }

        Iterator<MidiEvent> it = mEvents.iterator();
        MidiEvent prev = null, next = null;
        while (it.hasNext()) {
            next = it.next();

            if (next.getTick() > newEvent.getTick()) {
                break;
            }

            prev = next;
            next = null;
        }

        mEvents.add(newEvent);
        mSizeNeedsRecalculating = true;

        // Set its delta time based on the previous event (or itself if no
        // previous event exists)
        if (prev != null) {
            newEvent.setDelta(newEvent.getTick() - prev.getTick());
        } else {
            newEvent.setDelta(newEvent.getTick());
        }

        // Update the next event's delta time relative to the new event.
        if (next != null) {
            next.setDelta(next.getTick() - newEvent.getTick());
        }

        mSize += newEvent.getSize();

        if (newEvent.getClass().equals(EndOfTrack.class)) {
            if (next != null) {
                throw new IllegalArgumentException(
                        "Attempting to insert EndOfTrack before an existing event. Use closeTrack() when finished with MidiTrack.");
            }
            mClosed = true;
        }
    }

    public boolean removeEvent(MidiEvent E) {

        Iterator<MidiEvent> it = mEvents.iterator();
        MidiEvent prev = null, curr = null, next = null;

        while (it.hasNext()) {
            next = it.next();

            if (E.equals(curr)) {
                break;
            }

            prev = curr;
            curr = next;
            next = null;
        }

        if (next == null) {
            return false;
        }

        if (!mEvents.remove(curr)) {
            return false;
        }

        if (prev != null) {
            next.setDelta(next.getTick() - prev.getTick());
        } else {
            next.setDelta(next.getTick());
        }
        return true;
    }

    public void closeTrack() {
        long lastTick = 0;
        if (mEvents.size() > 0) {
            MidiEvent last = mEvents.get(mEvents.size() - 1);
            lastTick = last.getTick() + 1;
        }

        EndOfTrack eot = new EndOfTrack(lastTick, 0);
        insertEvent(eot);
    }

    public void dumpEvents() {
        Iterator<MidiEvent> it = mEvents.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    public void recalculateDeltas() {
        if (mEvents.isEmpty())
            return;
        mEvents.get(0).setDelta(mEvents.get(0).getTick());
        for (int i = 1; i < mEvents.size(); i++) {
            mEvents.get(i).setDelta(mEvents.get(i).getTick() - mEvents.get(i - 1).getTick());
        }
    }

    private void recalculateSize() {

        mSize = 0;

        Iterator<MidiEvent> it = mEvents.iterator();
        MidiEvent last = null;
        while (it.hasNext()) {
            MidiEvent E = it.next();
            mSize += E.getSize();

            // If an event is of the same type as the previous event,
            // no status byte is written.
            if (last != null && !E.requiresStatusByte(last)) {
                mSize--;
            }
            last = E;
        }

        mSizeNeedsRecalculating = false;
    }

    public void writeToFile(OutputStream out) throws IOException {

        if (!mClosed) {
            closeTrack();
        }

        if (mSizeNeedsRecalculating) {
            recalculateSize();
        }

        out.write(IDENTIFIER);
        out.write(MidiUtil.intToBytes(mSize, 4));

        Iterator<MidiEvent> it = mEvents.iterator();
        Class<? extends MidiEvent> lastEventClass = null;

        while (it.hasNext()) {
            MidiEvent event = it.next();

            if (event.getClass().equals(lastEventClass)) {
                event.writeToFile(out, false);
            } else {
                event.writeToFile(out, true);
                lastEventClass = event.getClass();
            }
        }
    }
}
