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
	public void undo() {
		initialLevels.setTrackLevels(track);
	}

	@Override
	public void redo() {
		finalLevels.setTrackLevels(track);
	}

	private class Levels implements Comparable<Levels> {
		float volume, pan, pitchStep, pitchCent;

		public Levels(BaseTrack track) {
			this.volume = track.volumeParam.viewLevel;
			this.pan = track.panParam.viewLevel;
			this.pitchStep = track.pitchStepParam.viewLevel;
			this.pitchCent = track.pitchCentParam.viewLevel;
		}

		public void setTrackLevels(BaseTrack track) {
			track.setLevels(volume, pan, pitchStep, pitchCent);
		}

		@Override
		public int compareTo(Levels another) {
			if (this.volume == another.volume && this.pan == another.pan
					&& this.pitchStep == another.pitchStep && this.pitchCent == another.pitchCent) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}
