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

	@Override
	public void layout(float x, float y, float width, float height) {
		this.x = shouldDraw ? 0 : x;
		this.y = shouldDraw ? 0 : y;
		this.width = width;
		this.height = height;

		float centerX = width / 2;
		float centerY = height / 2;

		float scaledW = width - 4;
		float scaledH = height - 4;

		float dim = Math.min(width, height);
		float downW = scaledW - dim * .10f;
		float downH = scaledH - dim * .10f;

		Drawable prevIcon = currentDrawable;
		if (resource.defaultDrawable != null) {
			setDrawable(resource.defaultDrawable);
			resource.defaultDrawable.layout(this.x + 2, this.y + 2, scaledW,
					scaledH);
		}
		if (resource.pressedDrawable != null) {
			setDrawable(resource.pressedDrawable);
			resource.pressedDrawable.layout(this.x + centerX - downW / 2,
					this.y + centerY - downH / 2, downW, downH);
		}
		if (resource.selectedDrawable != null) {
			setDrawable(resource.selectedDrawable);
			resource.selectedDrawable.layout(resource.pressedDrawable.x,
					resource.pressedDrawable.y, downW, downH);
		}
		setDrawable(prevIcon);
	}
}
