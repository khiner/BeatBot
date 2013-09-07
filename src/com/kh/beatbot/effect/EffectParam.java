package com.kh.beatbot.effect;

import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.listener.ParamToggleListener;
import com.kh.beatbot.manager.MidiManager;

public class EffectParam extends Param {
	public float logScaleValue;
	public int topBeatNum = 1, bottomBeatNum = 1;
	public boolean hz, beatSyncable, logScale;
	private boolean beatSync;

	public EffectParam(int id, String name, String unitString, float addValue,
			float scaleValue, float logScaleValue, boolean logScale,
			boolean beatSyncable) {
		super(id, name, unitString, addValue, scaleValue);
		this.name = name;
		this.unitString = unitString;
		this.addValue = addValue;
		this.scaleValue = scaleValue;
		this.beatSync = this.beatSyncable = beatSyncable;
		this.logScale = logScale;
		this.logScaleValue = logScaleValue;
		this.hz = unitString.equalsIgnoreCase("hz");
		setLevel(0.5f);
	}

	public EffectParam(int id, String name, String unitString,
			boolean logScale, boolean beatSyncable) {
		this(id, name, unitString, 0, 1, 8, logScale, beatSyncable);
	}

	public String getFormattedValue() {
		if (beatSync) {
			return topBeatNum + (bottomBeatNum == 1 ? "" : "/" + bottomBeatNum);
		} else {
			return super.getFormattedValue();
		}
	}

	public void setLevel(float level) {
		viewLevel = level;
		if (beatSync) {
			this.level = quantizeToBeat(level);
		} else {
			this.level = addValue + scaleValue
					* (logScale ? logScaleLevel(level) : level);
		}
		notifyListeners();
	}

	public boolean isBeatSync() {
		return beatSync;
	}

	public void toggle(boolean state) {
		beatSync = state;
		notifyToggleListeners();
		notifyListeners();
	}

	private float logScaleLevel(float level) {
		float scaled = (float) (Math.pow(logScaleValue + 1, level) - 1)
				/ logScaleValue;
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

	private synchronized void notifyToggleListeners() {
		for (ParamListener listener : listeners) {
			if (listener instanceof ParamToggleListener
					&& !ignoredListeners.contains(listener)) {
				((ParamToggleListener) listener).onParamToggled(this);
			}
		}
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