package com.kh.beatbot.global;

public class ColorSet {
	public float[] defaultColor, pressedColor, selectedColor;
	
	public ColorSet(float[] defaultColor, float[] pressedColor) {
		this(defaultColor, pressedColor, null);
	}
	
	public ColorSet(float[] defaultColor, float[] pressedColor, float[] selectedColor) {
		this.defaultColor = defaultColor;
		this.pressedColor = pressedColor;
		this.selectedColor = selectedColor;
	}
}
