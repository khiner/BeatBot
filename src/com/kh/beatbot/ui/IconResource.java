package com.kh.beatbot.ui;

public class IconResource {
	public int resourceId;
	public float[] fillColor;
	public float[] strokeColor;

	public IconResource(int resourceId, float[] fillColor, float[] strokeColor) {
		this.resourceId = resourceId;
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
	}

	public IconResource copy() {
		return new IconResource(resourceId, fillColor == null ? null : fillColor.clone(),
				strokeColor == null ? null : strokeColor.clone());
	}
}
