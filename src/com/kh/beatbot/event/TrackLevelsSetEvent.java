package com.kh.beatbot.event;

import com.kh.beatbot.BaseTrack;

public class TrackLevelsSetEvent implements Stateful, Temporal {
	private BaseTrack track;
	private Levels initialLevels, finalLevels;

	public TrackLevelsSetEvent(BaseTrack track) {
		this.track = track;
	}

	@Override
	public void begin() {
		initialLevels = new Levels(track);
	}

	@Override
	public void end() {
		finalLevels = new Levels(track);

		if (finalLevels.compareTo(initialLevels) != 0) {
			EventManager.eventCompleted(this);
		}
	}

	@Override
	public void doUndo() {
		initialLevels.setTrackLevels(track);
	}

	@Override
	public void doRedo() {
		finalLevels.setTrackLevels(track);
	}

	private class Levels implements Comparable<Levels> {
		float volume, pan, pitch;

		public Levels(BaseTrack track) {
			this.volume = track.volumeParam.viewLevel;
			this.pan = track.panParam.viewLevel;
			this.pitch = track.pitchParam.viewLevel;
		}

		public void setTrackLevels(BaseTrack track) {
			track.setLevels(volume, pan, pitch);
		}

		@Override
		public int compareTo(Levels another) {
			if (this.volume == another.volume && this.pan == another.pan
					&& this.pitch == another.pitch) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}
