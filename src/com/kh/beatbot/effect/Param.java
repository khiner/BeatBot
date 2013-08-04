package com.kh.beatbot.effect;

public class Param {
	public String unitString, name, format = "%.2f";
	public float level, viewLevel, addValue, scaleValue;
	
	public Param(String name, String unitString) {
		this(name, unitString, 0, 1);
	}
	
	public Param(String name, String unitString, float addValue, float scaleValue) {
		this.name = name;
		this.addValue = addValue;
		this.scaleValue = scaleValue;
		this.unitString = unitString;
		viewLevel = 0.5f;
		setLevel(viewLevel);
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public void setLevel(float level) {
		this.level = addValue + scaleValue * level;
	}
	
	public float getLevel(float value) {
		return addValue + scaleValue * value;
	}
		
	public String getFormattedValue() {
		String formattedValue = String.format(format, level);
		if (!unitString.isEmpty()) {
			formattedValue += " " + unitString;
		}
		return formattedValue;
	}
}
