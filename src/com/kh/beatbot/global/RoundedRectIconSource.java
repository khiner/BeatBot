package com.kh.beatbot.global;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.mesh.RoundedRect;
import com.kh.beatbot.view.mesh.ShapeGroup;

public class RoundedRectIconSource extends ShapeIconSource {

	public RoundedRectIconSource(ShapeGroup shapeGroup, float x, float y,
			float width, float height, ColorSet bgColorSet,
			ColorSet borderColorSet) {
		this(shapeGroup, x, y, width, height, width > height ? height / 5
				: width / 5, bgColorSet, borderColorSet);
	}
	
	public RoundedRectIconSource(ShapeGroup shapeGroup, float x, float y,
			float width, float height, float cornerRadius, ColorSet bgColorSet,
			ColorSet borderColorSet) {
		super(shapeGroup, x, y, width, height, cornerRadius, bgColorSet, borderColorSet);
		float centerX = width / 2;
		float centerY = height / 2;

		float scaledW = width - 2;
		float scaledH = height - 2;

		float dim = Math.min(width, height);
		float downW = scaledW - dim * .10f;
		float downH = scaledH - dim * .10f;

		defaultIcon = new RoundedRect(this.shapeGroup, x + 1, y + 1, scaledW,
				scaledH, cornerRadius, bgColorSet.defaultColor,
				borderColorSet.defaultColor);
		pressedIcon = new RoundedRect(this.shapeGroup, x + centerX - downW / 2,
				y + centerY - downH / 2, downW, downH, cornerRadius,
				bgColorSet.pressedColor, borderColorSet.pressedColor);

		selectedIcon = new RoundedRect(this.shapeGroup, pressedIcon.getX(),
				pressedIcon.getY(), downW, downH, cornerRadius,
				bgColorSet.selectedColor, borderColorSet.selectedColor);

		setState(State.DEFAULT);
	}

	@Override
	public void draw(float x, float y, float width, float height) {
		if (shouldDraw) {
			shapeGroup.draw((GL11) BBView.gl, 1);
		}
	}

	public void setPosition(float x, float y) {
		// TODO
	}
	
	public void setDimensions(float width, float height) {
		float scaledW = width - 2;
		float scaledH = height - 2;

		float dim = Math.min(width, height);
		float downW = scaledW - dim * .10f;
		float downH = scaledH - dim * .10f;

		setIcon(defaultIcon);
		defaultIcon.setDimensions(scaledW, scaledH);
		setIcon(pressedIcon);
		pressedIcon.setDimensions(downW, downH);
		setIcon(selectedIcon);
		selectedIcon.setDimensions(downW, downH);
		setIcon(defaultIcon);
	}
}
