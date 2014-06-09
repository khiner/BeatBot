package com.kh.beatbot.effect;

import java.util.HashSet;
import java.util.Set;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.listener.ParamToggleListener;
import com.kh.beatbot.manager.MidiManager;

public class Param {
	public static final float DEFAULT_LOG_SCALE = 8, DB_LOG_SCALE = 10 * 10, MAX_DB = 24,
			DB_SCALE = dbToLinear(MAX_DB);

	public int id;
	public float level = 0, viewLevel = 0;

	public float minViewLevel = 0, maxViewLevel = 1;
	public boolean hz = false;

	private int topBeatNum = 1, bottomBeatNum = 1;
	private float addValue = 0, scaleValue = 1, logScaleValue = 8;
	private boolean beatSync, beatSyncable, logScale;
	private String unitString = "", name = "", format = "%.2f";

	private Set<ParamListener> listeners = new HashSet<ParamListener>();
	private Set<ParamListener> ignoredListeners = new HashSet<ParamListener>();

	public Param(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public Param withUnits(final String units) {
		this.unitString = units;
		this.hz = unitString.equalsIgnoreCase("hz");
		if (isDb()) {
			logScaleValue = DB_LOG_SCALE;
		}
		return this;
	}

	public Param withFormat(String format) {
		this.format = format;
		return this;
	}

	public Param add(float addValue) {
		this.addValue = addValue;
		return this;
	}

	public Param scale(float scaleValue) {
		this.scaleValue = scaleValue;
		return this;
	}

	public Param logScale() {
		return logScale(DEFAULT_LOG_SCALE);
	}

	public Param logScale(float logScaleValue) {
		this.logScale = true;
		this.logScaleValue = logScaleValue;
		return this;
	}

	public Param beatSyncable() {
		beatSync = beatSyncable = true;
		return this;
	}

	public Param withLevel(float level) {
		setLevel(level);
		return this;
	}

	public String getName() {
		return name;
	}

	public boolean isBeatSyncable() {
		return beatSyncable;
	}

	public void setLevel(float level) {
		level = GeneralUtils.clipTo(level, minViewLevel, maxViewLevel);
		viewLevel = level;
		if (isDb()) {
			this.level = linearToDb(DB_SCALE * logScaleLevel(level));
		} else if (beatSync) {
			this.level = quantizeToBeat(level);
		} else {
			this.level = addValue + scaleValue * (logScale ? logScaleLevel(level) : level);
		}
		notifyListeners();
	}

	public float getLevel(float value) {
		return addValue + scaleValue * value;
	}

	public float getViewLevel(float value) {
		return (value - addValue) / scaleValue;
	}

	public String getFormattedValue() {
		if (beatSync) {
			return topBeatNum + (bottomBeatNum == 1 ? "" : "/" + bottomBeatNum);
		} else {
			String formattedValue = String.format(format, level);
			if (!unitString.isEmpty()) {
				formattedValue += " " + unitString;
			}
			return formattedValue;
		}
	}

	public synchronized void addListener(ParamListener listener) {
		listeners.add(listener);
	}

	public synchronized void removeListener(ParamListener listener) {
		listeners.remove(listener);
	}

	public synchronized void ignoreListener(ParamListener listener) {
		ignoredListeners.add(listener);
	}

	public synchronized void unignoreListener(ParamListener listener) {
		ignoredListeners.remove(listener);
	}

	protected synchronized void notifyListeners() {
		for (ParamListener listener : listeners) {
			if (!ignoredListeners.contains(listener)) {
				listener.onParamChanged(this);
			}
		}
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

	private synchronized void notifyToggleListeners() {
		for (ParamListener listener : listeners) {
			if (listener instanceof ParamToggleListener && !ignoredListeners.contains(listener)) {
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

	private boolean isDb() {
		return unitString.toLowerCase().equals("db");
	}

	public static float dbToView(float db) {
		return (float) Math.log((DB_LOG_SCALE * dbToLinear(db)) / DB_SCALE + 1)
				/ (float) Math.log(DB_LOG_SCALE + 1);
	}

	public static float dbToLinear(float db) {
		return (float) Math.pow(10, db / 20);
	}

	public static float linearToDb(float linear) {
		return 20 * (float) Math.log10(linear);
	}
}
