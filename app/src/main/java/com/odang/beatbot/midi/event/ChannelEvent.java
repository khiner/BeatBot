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

package com.odang.beatbot.midi.event;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChannelEvent extends MidiEvent {

    protected int mType, mChannel, note;
    protected byte velocity, pan, pitch;

    protected ChannelEvent(long tick, int type, int channel, int note, byte velocity, byte pan,
                           byte pitch) {
        this(tick, 0, type, channel, note, velocity, pan, pitch);
    }

    protected ChannelEvent(long tick, long delta, int type, int channel, int note, byte velocity,
                           byte pan, byte pitch) {
        super(tick, delta);

        mType = type & 0x0F;
        mChannel = channel & 0x0F;
        this.note = note & 0xFF;
        this.velocity = velocity;
        this.pan = pan;
        this.pitch = pitch;
    }

    public int getType() {
        return mType;
    }

    public int getNoteValue() {
        return note;
    }

    public byte getVelocity() {
        return velocity;
    }

    public byte getPan() {
        return pan;
    }

    public byte getPitch() {
        return pitch;
    }

    public void setNoteValue(int p) {
        note = p;
    }

    public void setVelocity(byte velocity) {
        this.velocity = clip(velocity);
    }

    public void setPan(byte pan) {
        this.pan = clip(pan);
    }

    public void setPitch(byte pitch) {
        this.pitch = clip(pitch);
    }

    public void setChannel(int c) {
        if (c < 0) {
            c = 0;
        } else if (c > 15) {
            c = 15;
        }
        mChannel = c;
    }

    public int getChannel() {
        return mChannel;
    }

    protected int getEventSize() {
        return 3;
    }

    @Override
    public int compareTo(MidiEvent other) {

        if (mTick != other.getTick()) {
            return mTick < other.getTick() ? -1 : 1;
        }
        if (mDelta.getValue() != other.mDelta.getValue()) {
            return mDelta.getValue() < other.mDelta.getValue() ? 1 : -1;
        }

        if (!(other instanceof ChannelEvent)) {
            return 1;
        }

        ChannelEvent o = (ChannelEvent) other;
        if (mType != o.getType()) {
            return 1;
        }
        if (note != o.note) {
            return note < o.note ? -1 : 1;
        }
        if (velocity != o.velocity) {
            return velocity < o.velocity ? -1 : 1;
        }
        if (mChannel != o.getChannel()) {
            return mChannel < o.getChannel() ? -1 : 1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) mTick;
        result = prime * result + mChannel;
        result = prime * result + mType;
        result = prime * result + note;
        result = prime * result + pan;
        result = prime * result + pitch;
        result = prime * result + velocity;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChannelEvent other = (ChannelEvent) obj;
        if (mTick != other.mTick)
            return false;
        if (mChannel != other.mChannel)
            return false;
        if (mType != other.mType)
            return false;
        if (note != other.note)
            return false;
        if (pan != other.pan)
            return false;
        if (pitch != other.pitch)
            return false;
        if (velocity != other.velocity)
            return false;
        return true;
    }

    @Override
    public boolean requiresStatusByte(MidiEvent prevEvent) {
        if (prevEvent == null) {
            return true;
        }
        if (!(prevEvent instanceof ChannelEvent)) {
            return true;
        }

        ChannelEvent ce = (ChannelEvent) prevEvent;
        return !(mType == ce.getType() && mChannel == ce.getChannel());
    }

    @Override
    public void writeToFile(OutputStream out, boolean writeType) throws IOException {
        super.writeToFile(out, writeType);

        if (writeType) {
            int typeChannel = (mType << 4) + mChannel;
            out.write(typeChannel);
        }

        out.write(note);
        if (mType != PROGRAM_CHANGE && mType != CHANNEL_AFTERTOUCH) {
            int int_vel = (int) velocity & 0xFF;
            int int_pan = (int) pan & 0xFF;
            out.write(int_vel);
            out.write(int_pan);
        }
    }

    public static ChannelEvent parseChannelEvent(long tick, long delta, int type, int channel,
                                                 InputStream in) throws IOException {

        int note = in.read();
        byte velocity = (byte) in.read();
        byte pan = (byte) in.read();
        byte pitch = 64;

        switch (type) {
            case NOTE_OFF:
                return new NoteOff(tick, delta, channel, note, velocity, pan, pitch);
            case NOTE_ON:
                return new NoteOn(tick, delta, channel, note, velocity, pan, pitch);
            default:
                return new ChannelEvent(tick, delta, type, channel, note, velocity, pan, pitch);
        }
    }

    public static final int NOTE_OFF = 0x8;
    public static final int NOTE_ON = 0x9;
    public static final int NOTE_AFTERTOUCH = 0xA;
    public static final int CONTROLLER = 0xB;
    public static final int PROGRAM_CHANGE = 0xC;
    public static final int CHANNEL_AFTERTOUCH = 0xD;
    public static final int PITCH_BEND = 0xE;
}
