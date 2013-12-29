package com.kh.beatbot.ui;

import com.kh.beatbot.ui.color.ColorSet;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.RoundedRect;
import com.kh.beatbot.ui.mesh.Shape;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public class RoundedRectIcon extends ShapeIcon {

	public RoundedRectIcon(ShapeGroup shapeGroup, ColorSet bgColorSet) {
		super(shapeGroup, bgColorSet);
	}

	public RoundedRectIcon(ShapeGroup shapeGroup, ColorSet bgColorSet,
			ColorSet strokeColorSet) {
		super(shapeGroup, bgColorSet, strokeColorSet);
	}

	@Override
	protected Shape initShape() {
		return new RoundedRect(this.shapeGroup, fillColorSet == null ? null
				: Colors.TRANSPARENT, strokeColorSet == null ? null
				: Colors.TRANSPARENT);
	}
}
