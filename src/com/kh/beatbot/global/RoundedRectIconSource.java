package com.kh.beatbot.global;

public class RoundedRectIconSource extends IconSource {

	public RoundedRectIconSource(float x, float y, float width, float height,
			ColorSet bgColorSet, ColorSet borderColorSet) {
		this(x, y, width, height, width > height ? height / 5 : width / 5,
				bgColorSet, borderColorSet);
	}

	public RoundedRectIconSource(float x, float y, float width, float height,
			float cornerRadius, ColorSet bgColorSet, ColorSet borderColorSet) {
		float centerX = width / 2;
		float centerY = height / 2;
		
		float scaledW = width - 2;
		float scaledH = height - 2;
		
		float pressScale = 7f / 8f;
		float pressW = scaledW * pressScale;
		float pressH = scaledH * pressScale;
		
		float selectScale = 9f / 10f;
		float selectW = scaledW * selectScale;
		float selectH = scaledH * selectScale;
		
		this.defaultIcon = new RoundedRectIcon(x + 1, y + 1, scaledW, scaledH,
				cornerRadius, 1, bgColorSet.defaultColor,
				borderColorSet.defaultColor);
		this.pressedIcon = new RoundedRectIcon(x + centerX - pressW / 2, y + centerY
				- pressH / 2, pressW, pressH, pressScale * cornerRadius, 1,
				bgColorSet.pressedColor, borderColorSet.pressedColor);
		this.selectedIcon = new RoundedRectIcon(x + centerX - selectW / 2, y + centerY
				- selectH / 2, selectW, selectH, selectScale * cornerRadius, 3,
				bgColorSet.selectedColor, borderColorSet.selectedColor);
	}
}
