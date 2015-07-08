package com.kh.beatbot.event.effect;

import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.event.Temporal;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.View;

public class EffectParamsChangeEvent extends EffectEvent implements Stateful, Temporal {
	private Levels initialLevels, finalLevels;

	public EffectParamsChangeEvent(int trackId, int effectPosition) {
		super(trackId, effectPosition);
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
			updateUi();
			getEffect().setLevels(levels);
		}

		private void updateUi() {
			TrackManager.getTrackById(trackId).select();
			View.mainPage.launchEffect(getEffect());
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
