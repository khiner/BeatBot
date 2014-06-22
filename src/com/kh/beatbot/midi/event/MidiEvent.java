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

package com.kh.beatbot.midi.event;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.kh.beatbot.midi.event.meta.MetaEvent;
import com.kh.beatbot.midi.util.VariableLengthInt;

public abstract class MidiEvent implements Comparable<MidiEvent> {
	public static final byte MAX_LEVEL = 127, HALF_LEVEL = 64;

	protected long mTick;
	protected VariableLengthInt mDelta;

	public MidiEvent(long tick, long delta) {
		mTick = tick;
		mDelta = new VariableLengthInt((int) delta);
	}

	public void setTick(long tick) {
		mTick = tick;
	}

	public long getTick() {
		return mTick;
	}

	public long getDelta() {
		return mDelta.getValue();
	}

	public void setDelta(long d) {
		mDelta.setValue((int) d);
	}

	protected abstract int getEventSize();

	public int getSize() {
		return getEventSize() + mDelta.getByteCount();
	}

	public boolean requiresStatusByte(MidiEvent prevEvent) {
		if (prevEvent == null) {
			return true;
		}
		if (this instanceof MetaEvent) {
			return true;
		}
		if (this.getClass().equals(prevEvent.getClass())) {
			return false;
		}
		return true;
	}

	public void writeToFile(OutputStream out, boolean writeType) throws IOException {
		out.write(mDelta.getBytes());
	}

	private static int sId = -1;
	private static int sType = -1;
	private static int sChannel = -1;

	public static final MidiEvent parseEvent(long tick, long delta, InputStream in)
			throws IOException {

		in.mark(1);

		int id = in.read();
		if (!verifyIdentifier(id)) {
			in.reset();
		}

		if (sType >= 0x8 && sType <= 0xE) {

			return ChannelEvent.parseChannelEvent(tick, delta, sType, sChannel, in);
		} else if (sId == 0xFF) {

			return MetaEvent.parseMetaEvent(tick, delta, in);
		}

		return null;
	}

	private static boolean verifyIdentifier(int id) {

		int type = id >> 4;
		int channel = id & 0x0F;

		if (type >= 0x8 && type <= 0xE) {
			sId = id;
			sType = type;
			sChannel = channel;
		} else if (id == 0xFF) {
			sId = id;
			sType = -1;
			sChannel = -1;
		} else if (type == 0xF) {
			sId = id;
			sType = type;
			sChannel = -1;
		} else {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "" + mTick + " (" + mDelta.getValue() + "): " + this.getClass().getSimpleName();
	}

	public static byte clip(byte value) {
		return value > 0 ? (value < MAX_LEVEL ? value : MAX_LEVEL) : 0;
	}
}
