package com.odang.beatbot.ui.icon;

public class IconResource {
	public int resourceId;
	public float[] fillColor;
	public float[] strokeColor;
	public float[] textColor;
	public float[] iconColor;

	public IconResource(float[] fillColor, float[] strokeColor) {
		this(-1, fillColor, strokeColor);
	}

	public IconResource(int resourceId, float[] fillColor, float[] strokeColor) {
		this(resourceId, fillColor, strokeColor, null, null);
	}
	
	public IconResource(int resourceId, float[] fillColor, float[] strokeColor, float[] textColor) {
		this(resourceId, fillColor, strokeColor, textColor, null);
	}

	public IconResource(int resourceId, float[] fillColor, float[] strokeColor, float[] textColor, float[] iconColor) {
		this.resourceId = resourceId;
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
		this.textColor = textColor;
		this.iconColor = iconColor;
	}

	public IconResource copy() {
		return new IconResource(resourceId, fillColor == null ? null : fillColor.clone(),
				strokeColor == null ? null : strokeColor.clone(), textColor == null ? null
						: textColor.clone(), iconColor == null ? null : iconColor.clone());
	}
}
