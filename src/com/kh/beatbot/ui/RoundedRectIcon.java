package com.kh.beatbot.ui;

import com.kh.beatbot.ui.color.ColorSet;
import com.kh.beatbot.ui.mesh.Shape;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public class RoundedRectIcon extends ShapeIcon {

	public RoundedRectIcon(ShapeGroup shapeGroup, ColorSet bgColorSet) {
		super(shapeGroup, Shape.Type.ROUNDED_RECT, bgColorSet);
	}

	public RoundedRectIcon(ShapeGroup shapeGroup, ColorSet bgColorSet,
			ColorSet strokeColorSet) {
		super(shapeGroup, Shape.Type.ROUNDED_RECT, bgColorSet, strokeColorSet);
	}
}
