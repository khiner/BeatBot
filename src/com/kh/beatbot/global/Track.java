package com.kh.beatbot.global;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.effect.Effect;

public class Track {
	private int id;
	private Instrument instrument;
	public List<Effect> effects;
	public float volume = .8f;
	public float pan = .5f;
	public float pitch = .5f;
	public float[][] adsrPoints;
	public float sampleLoopBegin = 0;
	public float sampleLoopEnd = 0;
	
	public Track(int id, Instrument instrument) {
		this.id = id;
		this.instrument = instrument;
		effects = new ArrayList<Effect>();
		initDefaultAdsrPoints();
	}
	
	public int getId() {
		return id;
	}
	
	public void initDefaultAdsrPoints() {
		adsrPoints = new float[5][2];
		for (int i = 0; i < 5; i++) {
			// x coords
			adsrPoints[i][0] = i / 4f;
		}
		// y coords
		adsrPoints[0][1] = 0;
		adsrPoints[1][1] = 1;
		adsrPoints[2][1] = .60f;
		adsrPoints[3][1] = .60f;
		adsrPoints[4][1] = 0;
	}
	
	public Effect findEffectById(int effectId) {
		for (Effect effect : effects) {
			if (effect.getId() == effectId) {
				return effect;
			}
		}
		return null;
	}
	
	public Effect findEffectByPosition(int position) {
		for (Effect effect : effects) {
			if (effect.getPosition() == position) {
				return effect;
			}
		}
		return null;
	}
	
	public Instrument getInstrument() {
		return instrument;
	}
}
