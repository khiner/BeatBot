package com.kh.beatbot.effect;

import com.kh.beatbot.manager.MidiManager;

public class Param {
	public float addValue, scaleValue, logScaleValue;
	public boolean hz, beatSyncable, logScale;
	public String unitString, name;
	
	public float level, viewLevel;
	public int topBeatNum = 1, bottomBeatNum = 1;
	public boolean beatSync;

	public Param(String name, boolean logScale, boolean beatSyncable,
			float addValue, float scaleValue, float logScaleValue, String unitString) {
		this.name = name;
		this.beatSync = beatSyncable;
		this.logScale = logScale;
		this.logScaleValue = logScaleValue;
		this.addValue = addValue;
		this.scaleValue = scaleValue;
		this.unitString = unitString;
		this.hz = unitString.equalsIgnoreCase("hz");
		viewLevel = 0.5f;
		setLevel(viewLevel);
	}

	public Param(String name, boolean logScale, boolean beatSyncable,
			String unitString) {
		this(name, logScale, beatSyncable, 0, 1, 8, unitString);
	}
	
	public String getName() {
		return name;
	}
	
	public String getFormattedValueString() {
		if (beatSync) {
			return topBeatNum + (bottomBeatNum == 1 ? "" : "/" + bottomBeatNum);
		} else {
			return String.format("%.2f", level) + " " + unitString;
		}
	}

	public void setLevel(float level) {
		if (beatSync) {
			this.level = quantizeToBeat(level);
		} else if (logScale) {
			this.level = addValue + scaleValue * logScaleLevel(level);
		} else {
			this.level = addValue + scaleValue * level;
		}
	}

	private float logScaleLevel(float level) {
		float scaled = (float) (Math.pow(logScaleValue + 1, level) - 1) / logScaleValue; 
		if (hz)
			scaled *= 32;
		return scaled;
	}

	private float quantizeToBeat(float level) {
		topBeatNum = getTopBeatNum((int) Math.ceil(level * 14));
		bottomBeatNum = getBottomBeatNum((int) Math.ceil(level * 14));
		float quantized = (60f / (MidiManager.getBPM()) * ((float) topBeatNum / (float) bottomBeatNum));
		return hz ? 1 / quantized : quantized;
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