package com.kh.beatbot.effect;

import java.util.HashSet;
import java.util.Set;

import com.kh.beatbot.listener.ParamListener;

public class Param {
	public Set<ParamListener> listeners = new HashSet<ParamListener>();
	public Set<ParamListener> ignoredListeners = new HashSet<ParamListener>();

	public int id;
	public String unitString, name, format = "%.2f";
	public float level, viewLevel, addValue, scaleValue;
	
	public Param(int id, String name, String unitString) {
		this(id, name, unitString, 0, 1);
	}
	
	public Param(int id, String name, String unitString, float addValue, float scaleValue) {
		this.id = id;
		this.name = name;
		this.addValue = addValue;
		this.scaleValue = scaleValue;
		this.unitString = unitString;
		setLevel(0.5f);
	}
	
	public void setLevel(float level) {
		viewLevel = level;
		this.level = addValue + scaleValue * level;
		notifyListeners();
	}
	
	public float getLevel(float value) {
		return addValue + scaleValue * value;
	}
		
	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getFormattedValue() {
		String formattedValue = String.format(format, level);
		if (!unitString.isEmpty()) {
			formattedValue += " " + unitString;
		}
		return formattedValue;
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
}
