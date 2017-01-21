//////////////////////////////////////////////////////////////////////////////
//	Copyright 2011 Alex Leffelelman
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

package com.odang.beatbot.midi.event.meta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.odang.beatbot.midi.event.MidiEvent;
import com.odang.beatbot.midi.util.VariableLengthInt;

public abstract class MetaEvent extends MidiEvent {

	protected int mType;
	protected VariableLengthInt mLength;

	protected MetaEvent(long tick, long delta, int type, VariableLengthInt length) {
		super(tick, delta);

		mType = type & 0xFF;
		mLength = length;
	}

	protected abstract int getEventSize();

	@Override
	public void writeToFile(OutputStream out, boolean writeType) throws IOException {
		writeToFile(out);
	}

	protected void writeToFile(OutputStream out) throws IOException {
		super.writeToFile(out, true);
		out.write(0xFF);
		out.write(mType);
	}

	public static MetaEvent parseMetaEvent(long tick, long delta, InputStream in)
			throws IOException {

		int type = in.read();

		switch (type) {
		case MIDI_CHANNEL_PREFIX:
			return MidiChannelPrefix.parseMidiChannelPrefix(tick, delta, in);
		case END_OF_TRACK:
			in.read(); // Size = 0;
			return new EndOfTrack(tick, delta);
		case TEMPO:
			return Tempo.parseTempo(tick, delta, in);
		case TIME_SIGNATURE:
			return TimeSignature.parseTimeSignature(tick, delta, in);
		}
		System.out.println("Completely broken in MetaEvent.parseMetaEvent()");
		return null;
	}

	public static final int SEQUENCE_NUMBER = 0;
	public static final int TEXT_EVENT = 1;
	public static final int COPYRIGHT_NOTICE = 2;
	public static final int TRACK_NAME = 3;
	public static final int INSTRUMENT_NAME = 4;
	public static final int LYRICS = 5;
	public static final int MARKER = 6;
	public static final int CUE_POINT = 7;
	public static final int MIDI_CHANNEL_PREFIX = 0x20;
	public static final int END_OF_TRACK = 0x2F;
	public static final int TEMPO = 0x51;
	public static final int SMPTE_OFFSET = 0x54;
	public static final int TIME_SIGNATURE = 0x58;
	public static final int KEY_SIGNATURE = 0x59;
	public static final int SEQUENCER_SPECIFIC = 0x7F;
}
