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

public class NoteOn extends ChannelEvent {
	public NoteOn(long tick, int channel, int note, byte velocity, byte pan, byte pitch) {
		super(tick, ChannelEvent.NOTE_ON, channel, note, velocity, pan, pitch);
	}

	public NoteOn(long tick, long delta, int channel, int note, byte velocity, byte pan, byte pitch) {
		super(tick, delta, ChannelEvent.NOTE_ON, channel, note, velocity, pan, pitch);
	}
}
