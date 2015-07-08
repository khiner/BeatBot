package com.kh.beatbot.event;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.ui.view.View;

public class EffectParamsChangeEvent implements Stateful, Temporal {
	private int trackId, effectPosition;
	private Levels initialLevels, finalLevels;

	public EffectParamsChangeEvent(int trackId, int effectPosition) {
		this.trackId = trackId;
		this.effectPosition = effectPosition;
	}

	@Override
	public void begin() {
		initialLevels = new Levels();
	}

	@Override
	public void end() {
		finalLevels = new Levels();

		if (!finalLevels.equals(initialLevels)) {
			EventManager.eventCompleted(this);
		}
	}

	@Override
	public void undo() {
		initialLevels.apply();
	}

	@Override
	public void apply() {
		finalLevels.apply();
	}

	private class Levels {
		float[] levels;

		public Levels() {
			this.levels = getEffect().getLevels();
		}

		public void apply() {
			TrackManager.getTrackById(trackId).select();
			View.mainPage.launchEffect(getEffect());
			getEffect().setLevels(levels);
		}

		private Effect getEffect() {
			BaseTrack track = TrackManager.getBaseTrackById(trackId);
			return track.findEffectByPosition(effectPosition);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Levels))
				return false;
			Levels other = (Levels) obj;
			if (levels.length != other.levels.length)
				return false;
			for (int i = 0; i < levels.length; i++) {
				if (levels[i] != other.levels[i])
					return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}
}
