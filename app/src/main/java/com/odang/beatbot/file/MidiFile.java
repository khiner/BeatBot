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

package com.odang.beatbot.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.odang.beatbot.midi.MidiTrack;
import com.odang.beatbot.midi.util.MidiUtil;

public class MidiFile {
	public static final int HEADER_SIZE = 14, DEFAULT_RESOLUTION = 320;
	public static final byte[] IDENTIFIER = { 'M', 'T', 'h', 'd' };

	private int type, trackCount, resolution;
	private List<MidiTrack> tracks;

	public MidiFile() {
		this(DEFAULT_RESOLUTION);
	}

	public MidiFile(int resolution) {
		this(resolution, new ArrayList<MidiTrack>());
	}

	public MidiFile(final int resolution, List<MidiTrack> tracks) {
		this.resolution = resolution >= 0 ? resolution : DEFAULT_RESOLUTION;
		this.tracks = tracks != null ? tracks : new ArrayList<MidiTrack>();
		trackCount = tracks.size();
		type = trackCount > 1 ? 1 : 0;
	}

	public MidiFile(File fileIn) throws FileNotFoundException, IOException {
		this(new FileInputStream(fileIn));
	}

	public MidiFile(final InputStream rawIn) throws IOException {
		final BufferedInputStream in = new BufferedInputStream(rawIn);

		final byte[] buffer = new byte[HEADER_SIZE];
		in.read(buffer);
		initFromBuffer(buffer);

		tracks = new ArrayList<MidiTrack>(trackCount);
		for (int i = 0; i < trackCount; i++) {
			tracks.add(new MidiTrack(in));
		}
	}

	public void setType(final int type) {
		if (type < 0) {
			this.type = 0;
		} else if (type > 2 || (type == 0 && trackCount > 1)) {
			this.type = 1;
		} else {
			this.type = type;
		}
	}

	public int getType() {
		return type;
	}

	public int getTrackCount() {
		return trackCount;
	}

	public void setResolution(final int res) {
		if (res >= 0)
			resolution = res;
	}

	public int getResolution() {
		return resolution;
	}

	public long getLengthInTicks() {
		long maxLength = 0;
		for (final MidiTrack track : tracks) {
			if (track.getLengthInTicks() > maxLength) {
				maxLength = track.getLengthInTicks();
			}
		}
		return maxLength;
	}

	public List<MidiTrack> getTracks() {
		return tracks;
	}

	public void addTrack(final MidiTrack track) {
		addTrack(track, tracks.size());
	}

	public void addTrack(final MidiTrack track, int pos) {
		if (pos > tracks.size()) {
			pos = tracks.size();
		} else if (pos < 0) {
			pos = 0;
		}

		tracks.add(pos, track);
		trackCount = tracks.size();
		type = trackCount > 1 ? 1 : 0;
	}

	public void removeTrack(final int pos) {
		if (pos < 0 || pos >= tracks.size())
			return;

		tracks.remove(pos);
		trackCount = tracks.size();
		type = trackCount > 1 ? 1 : 0;
	}

	public void writeToFile(final File outFile) throws FileNotFoundException, IOException {
		final FileOutputStream fout = new FileOutputStream(outFile);

		fout.write(IDENTIFIER);
		fout.write(MidiUtil.intToBytes(6, 4));
		fout.write(MidiUtil.intToBytes(type, 2));
		fout.write(MidiUtil.intToBytes(trackCount, 2));
		fout.write(MidiUtil.intToBytes(resolution, 2));

		for (final MidiTrack track : tracks) {
			track.writeToFile(fout);
		}

		fout.flush();
		fout.close();
	}

	private void initFromBuffer(byte[] buffer) {
		if (!MidiUtil.bytesEqual(buffer, IDENTIFIER, 0, 4)) {
			System.out.println("File identifier not MThd. Exiting");
			type = 0;
			trackCount = 0;
			resolution = DEFAULT_RESOLUTION;
			return;
		}

		type = MidiUtil.bytesToInt(buffer, 8, 2);
		trackCount = MidiUtil.bytesToInt(buffer, 10, 2);
		resolution = MidiUtil.bytesToInt(buffer, 12, 2);
	}
}
