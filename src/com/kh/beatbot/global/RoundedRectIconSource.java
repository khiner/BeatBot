package com.kh.beatbot.global;

import com.kh.beatbot.view.mesh.RoundedRect;
import com.kh.beatbot.view.mesh.ShapeGroup;

public class RoundedRectIconSource extends ShapeIconSource {

	public RoundedRectIconSource(ShapeGroup shapeGroup, ColorSet bgColorSet) {
		super(shapeGroup, bgColorSet);
		defaultIcon = new RoundedRect(this.shapeGroup, bgColorSet.defaultColor);
		pressedIcon = new RoundedRect(this.shapeGroup, bgColorSet.pressedColor);
		if (bgColorSet.selectedColor != null) {
			selectedIcon = new RoundedRect(this.shapeGroup,
					bgColorSet.selectedColor);
		}
		setState(State.DEFAULT);
	}

	public RoundedRectIconSource(ShapeGroup shapeGroup, ColorSet bgColorSet,
			ColorSet borderColorSet) {
		super(shapeGroup, bgColorSet, borderColorSet);
		defaultIcon = new RoundedRect(this.shapeGroup, bgColorSet.defaultColor,
				borderColorSet.defaultColor);
		pressedIcon = new RoundedRect(this.shapeGroup, bgColorSet.pressedColor,
				borderColorSet.pressedColor);
		if (bgColorSet.selectedColor != null) {
			selectedIcon = new RoundedRect(this.shapeGroup,
					bgColorSet.selectedColor, borderColorSet.selectedColor);
		}
		setState(State.DEFAULT);
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

		Drawable prevIcon = currentIcon;
		setIcon(defaultIcon);
		defaultIcon.layout(this.x + 2, this.y + 2, scaledW, scaledH);
		setIcon(pressedIcon);
		pressedIcon.layout(this.x + centerX - downW / 2, this.y + centerY
				- downH / 2, downW, downH);
		if (selectedIcon != null) {
			setIcon(selectedIcon);
			selectedIcon.layout(pressedIcon.getX(), pressedIcon.getY(), downW,
					downH);
		}
		setIcon(prevIcon);
	}
}
