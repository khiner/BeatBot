package com.kh.beatbot.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.effect.Effect;

public class Track {
	public String instrumentName;
	public List<Effect> effects;
	public BeatBotIconSource instrumentIcon;
	public float volume = .8f;
	public float pan = .5f;
	public float pitch = .5f;
	public float[][] adsrPoints;
	public float sampleLoopBegin = 0;
	public float sampleLoopEnd = 0;
	public File[] samples;
	public String[] sampleNames;
	
	public Track(String instrumentName, BeatBotIconSource instrumentIcon) {
		this.instrumentName = instrumentName;
		this.instrumentIcon = instrumentIcon;
		File dir = new File(GlobalVars.appDirectory + instrumentName);
		effects = new ArrayList<Effect>();
		samples = dir.listFiles();
		sampleNames = dir.list();
		initDefaultAdsrPoints();
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
	
	public byte[] getSampleBytes(int sampleNum) {
		byte[] bytes = null;
		try {
			File sampleFile = samples[sampleNum];
			FileInputStream in = new FileInputStream(sampleFile);
			bytes = new byte[(int) sampleFile.length()];
			in.read(bytes);
			in.close();
			in = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
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
}
