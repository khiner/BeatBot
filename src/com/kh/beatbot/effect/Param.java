package com.kh.beatbot.effect;

import android.util.FloatMath;

import com.kh.beatbot.manager.Managers;

public class Param {
	public float level, viewLevel, scale = 1;
	public int topBeatNum = 1, bottomBeatNum = 1;
	public boolean hz = false;
	public boolean beatSync;
	public boolean logScale;
	public String unitString;
	public String name = null;

	public Param(String name, boolean logScale, boolean beatSync,
			String unitString) {
		this.name = name;
		this.logScale = logScale;
		this.beatSync = beatSync;
		this.unitString = unitString;
		viewLevel = 0.5f;
		setLevel(viewLevel);
	}

	public Param(boolean logScale, boolean beatSync, String unitString) {
		this(null, logScale, beatSync, unitString);
	}

	public String getFormattedValueString() {
		if (beatSync)
			return topBeatNum + (bottomBeatNum == 1 ? "" : "/" + bottomBeatNum);
		else
			return String.format("%.2f", level * scale) + " " + unitString;
	}

	public void setLevel(float level) {
		if (beatSync) {
			quantizeToBeat(level);
		} else if (logScale) {
			logScaleLevel(level);
		} else {
			this.level = level;
		}
	}

	private void logScaleLevel(float level) {
		this.level = (float) (Math.pow(9, level) - 1) / 8;
		if (hz)
			this.level *= 32;
	}

	private void quantizeToBeat(float level) {
		topBeatNum = getTopBeatNum((int) FloatMath.ceil(level * 14));
		bottomBeatNum = getBottomBeatNum((int) FloatMath.ceil(level * 14));
		level = (60f / (Managers.midiManager.getBPM()) * ((float) topBeatNum / (float) bottomBeatNum));
		if (hz)
			level = 1 / level;
	}
	
	private static int getTopBeatNum(int which) {
		switch (which) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
			return 1;
		case 5:
			return 3;
		case 6:
			return 1;
		case 7:
			return 5;
		case 8:
			return 1;
		case 9:
			return 3;
		case 10:
			return 1;
		case 11:
			return 3;
		case 12:
			return 1;
		case 13:
			return 3;
		case 14:
			return 2;
		default:
			return 1;
		}
	}

	private static int getBottomBeatNum(int which) {
		switch (which) {
		case 0:
		case 1:
			return 16;
		case 2:
			return 12;
		case 3:
			return 8;
		case 4:
			return 6;
		case 5:
			return 16;
		case 6:
			return 4;
		case 7:
			return 16;
		case 8:
			return 3;
		case 9:
			return 8;
		case 10:
			return 2;
		case 11:
			return 4;
		case 12:
			return 1;
		case 13:
			return 2;
		case 14:
			return 1;
		default:
			return 1;
		}
	}
}