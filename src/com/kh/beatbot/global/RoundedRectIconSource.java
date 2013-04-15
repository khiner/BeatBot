package com.kh.beatbot.global;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.mesh.Mesh2D;
import com.kh.beatbot.view.mesh.RoundedRect;
import com.kh.beatbot.view.mesh.ShapeGroup;

public class RoundedRectIconSource extends ShapeIconSource {

	private ShapeGroup shapeGroup;
	private boolean shouldDraw;
	
	public RoundedRectIconSource(ShapeGroup shapeGroup, float x, float y,
			float width, float height, ColorSet bgColorSet,
			ColorSet borderColorSet) {
		this(shapeGroup, x, y, width, height, width > height ? height / 5
				: width / 5, bgColorSet, borderColorSet);
	}

	public RoundedRectIconSource(ShapeGroup shapeGroup, float x, float y,
			float width, float height, float cornerRadius, ColorSet bgColorSet,
			ColorSet borderColorSet) {

		// if there is already a global group, then it will be drawn elsewhere.
		// otherwise, we create a new group to share amongst all icons
		shouldDraw = shapeGroup == null;
		this.shapeGroup = shouldDraw ? new ShapeGroup() : shapeGroup;
		
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
		pressedIcon = new RoundedRect(this.shapeGroup, x + centerX - downW / 2, y
				+ centerY - downH / 2, downW, downH, cornerRadius,
				bgColorSet.pressedColor, borderColorSet.pressedColor);

		// copy vertices from pressed mesh into selected mesh (same size) but
		// use different colors
		Mesh2D pressedMesh = ((RoundedRect) pressedIcon).getFillMesh();
		Mesh2D pressedOutlineMesh = ((RoundedRect) pressedIcon)
				.getOutlineMesh();

		Mesh2D selectedMesh = new Mesh2D(pressedMesh.getVertices(),
				bgColorSet.selectedColor);
		Mesh2D selectedOutlineMesh = new Mesh2D(
				pressedOutlineMesh.getVertices(), borderColorSet.selectedColor);

		selectedIcon = new RoundedRect(this.shapeGroup, pressedIcon.getX(),
				pressedIcon.getY(), downW, downH, selectedMesh,
				selectedOutlineMesh);

		setState(State.DEFAULT);
	}
	
	@Override
	public void draw(float x, float y, float width, float height) {
		if (shouldDraw) {
			shapeGroup.draw((GL11) BBView.gl, 1);
		}
	}
}
