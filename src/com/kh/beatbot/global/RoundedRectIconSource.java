package com.kh.beatbot.global;

public class RoundedRectIconSource extends IconSource {

	public RoundedRectIconSource(float x, float y, float width, float height,
			ColorSet bgColorSet, ColorSet borderColorSet) {
		this(x, y, width, height, width > height ? height / 5 : width / 5,
				bgColorSet, borderColorSet);
	}

	public RoundedRectIconSource(float x, float y, float width, float height,
			float cornerRadius, ColorSet bgColorSet, ColorSet borderColorSet) {
		
		// TODO replace magic numbers with values scaled by SCREEN width and height
		float centerX = width / 2;
		float centerY = height / 2;
		
		float scaledW = width - 2;
		float scaledH = height - 2;
		
		float pressW = scaledW - 7;
		float pressH = scaledH - 7;
		
		float selectW = scaledW - 4;
		float selectH = scaledH - 4;
		
		this.defaultIcon = new RoundedRectIcon(x + 2, y + 2, scaledW, scaledH,
				cornerRadius, bgColorSet.defaultColor, borderColorSet.defaultColor);
		this.pressedIcon = new RoundedRectIcon(x + centerX - pressW / 2, y + centerY
				- pressH / 2, pressW, pressH, cornerRadius,
				bgColorSet.pressedColor, borderColorSet.pressedColor);
		this.selectedIcon = new RoundedRectIcon(x + centerX - selectW / 2, y + centerY
				- selectH / 2, selectW, selectH, cornerRadius,
				bgColorSet.selectedColor, borderColorSet.selectedColor);
	}
}
