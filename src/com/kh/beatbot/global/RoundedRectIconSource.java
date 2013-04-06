package com.kh.beatbot.global;

import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.mesh.RoundedRectMesh;


public class RoundedRectIconSource extends IconSource {
	
	public RoundedRectIconSource(float width, float height, ColorSet bgColorSet, ColorSet borderColorSet) {
		this(width, height, width > height ? height / 5 : width / 5, bgColorSet, borderColorSet);
	}
	
	public RoundedRectIconSource(float width, float height, float cornerRadius, ColorSet bgColorSet, ColorSet borderColorSet) {
		RoundedRectMesh roundedRectMesh = new RoundedRectMesh(BBView.gl, width, height, cornerRadius, 16);
		this.defaultIcon = new RoundedRectIcon(roundedRectMesh, width, height, 1, bgColorSet.defaultColor, borderColorSet.defaultColor);
		this.pressedIcon = new RoundedRectIcon(roundedRectMesh, width, height, 1, bgColorSet.pressedColor, borderColorSet.pressedColor);
		this.selectedIcon = new RoundedRectIcon(roundedRectMesh, width, height, 3, bgColorSet.selectedColor, borderColorSet.selectedColor);
	}
}
