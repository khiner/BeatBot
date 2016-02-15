package com.kh.beatbot.event.track;

import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.event.Temporal;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.ui.view.View;

public class TrackLevelsSetEvent implements Stateful, Temporal {
	private int trackId;
	private Levels initialLevels, finalLevels;

	public TrackLevelsSetEvent(int trackId) {
		this.trackId = trackId;
	}

	@Override
	public void begin() {
		initialLevels = new Levels(trackId);
	}

	@Override
	public void end() {
		finalLevels = new Levels(trackId);

		if (!finalLevels.equals(initialLevels)) {
			EventManager.eventCompleted(this);
		}
	}

	@Override
	public void undo() {
		initialLevels.setTrackLevels(trackId);
	}

	@Override
	public void apply() {
		finalLevels.setTrackLevels(trackId);
	}

	private class Levels {
		float volume, pan, pitchStep, pitchCent;

		public Levels(int trackId) {
			final BaseTrack track = View.context.getTrackManager().getBaseTrackById(trackId);
			this.volume = track.volumeParam.viewLevel;
			this.pan = track.panParam.viewLevel;
			this.pitchStep = track.pitchStepParam.viewLevel;
			this.pitchCent = track.pitchCentParam.viewLevel;
		}

		public void setTrackLevels(int trackId) {
			final BaseTrack track = View.context.getTrackManager().getBaseTrackById(trackId);
			track.setLevels(volume, pan, pitchStep, pitchCent);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Levels))
				return false;
			Levels other = (Levels) obj;
			return (this.volume == other.volume && this.pan == other.pan
					&& this.pitchStep == other.pitchStep && this.pitchCent == other.pitchCent);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}
}
