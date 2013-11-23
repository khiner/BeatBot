package com.kh.beatbot.ui.transition;

public class ColorTransition extends Transition {

	private float[] color, beginColor, endColor;

	public ColorTransition(long durationInFrames, long waitFrames,
			float[] beginColor, float[] endColor) {
		super(durationInFrames, waitFrames);
		this.beginColor = beginColor;
		this.endColor = endColor;
		this.color = beginColor.clone();
	}

	public float[] getColor() {
		return color;
	}

	@Override
	protected void update() {
		for (int i = 0; i < color.length; i++) {
			color[i] = beginColor[i] + (endColor[i] - beginColor[i]) * position;
		}
	}
}
