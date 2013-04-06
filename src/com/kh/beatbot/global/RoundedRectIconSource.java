package com.kh.beatbot.global;

import java.nio.FloatBuffer;

import com.kh.beatbot.view.BBView;


public class RoundedRectIconSource extends IconSource {
	
	public RoundedRectIconSource(float width, float height, ColorSet bgColorSet, ColorSet borderColorSet) {
		this(width, height, width > height ? height / 5 : width / 5, bgColorSet, borderColorSet);
	}
	
	public RoundedRectIconSource(float width, float height, float cornerRadius, ColorSet bgColorSet, ColorSet borderColorSet) {
		FloatBuffer roundedRectBuffer = BBView.makeRoundedCornerRectBuffer(width - 2, height - 2, cornerRadius, 16);
		this.defaultIcon = new RoundedRectIcon(roundedRectBuffer, width, height, 1, bgColorSet.defaultColor, borderColorSet.defaultColor);
		this.pressedIcon = new RoundedRectIcon(roundedRectBuffer, width, height, 1, bgColorSet.pressedColor, borderColorSet.pressedColor);
		this.selectedIcon = new RoundedRectIcon(roundedRectBuffer, width, height, 3, bgColorSet.selectedColor, borderColorSet.selectedColor);
	}
}
