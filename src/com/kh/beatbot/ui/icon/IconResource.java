package com.kh.beatbot.ui.icon;

public class IconResource {
	public int resourceId;
	public float[] fillColor;
	public float[] strokeColor;
	public float[] textColor;

	public IconResource(int resourceId, float[] fillColor, float[] strokeColor) {
		this(resourceId, fillColor, strokeColor, null);
	}

	public IconResource(int resourceId, float[] fillColor, float[] strokeColor, float[] textColor) {
		this.resourceId = resourceId;
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
		this.textColor = textColor;
	}

	public IconResource copy() {
		return new IconResource(resourceId, fillColor == null ? null : fillColor.clone(),
				strokeColor == null ? null : strokeColor.clone(), textColor == null ? null
						: textColor.clone());
	}
}
