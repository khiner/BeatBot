package com.kh.beatbot.ui.color;

import com.kh.beatbot.ui.IconResource.State;

public class ColorSet {
	public float[] defaultColor, pressedColor, selectedColor, disabledColor;

	public ColorSet(float[] defaultColor) {
		this(defaultColor, defaultColor);
	}

	public ColorSet(float[] defaultColor, float[] pressedColor) {
		this(defaultColor, pressedColor, null);
	}

	public ColorSet(float[] defaultColor, float[] pressedColor,
			float[] selectedColor) {
		this(defaultColor, pressedColor, selectedColor, null);
	}

	public ColorSet(float[] defaultColor, float[] pressedColor,
			float[] selectedColor, float[] disabledColor) {
		this.defaultColor = defaultColor;
		this.pressedColor = pressedColor;
		this.selectedColor = selectedColor;
		this.disabledColor = disabledColor;
	}

	public float[] getColor(State state) {
		switch (state) {
		case DEFAULT:
			return defaultColor;
		case PRESSED:
			return pressedColor;
		case SELECTED:
			return selectedColor;
		case DISABLED:
			return disabledColor;
		default:
			return defaultColor;
		}
	}
}
