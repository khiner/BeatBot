package com.kh.beatbot.global;

import com.kh.beatbot.view.mesh.RoundedRectMesh;
import com.kh.beatbot.view.mesh.RoundedRectOutlineMesh;

public class RoundedRectIconSource extends IconSource {

	public RoundedRectIconSource(float x, float y, float width, float height,
			ColorSet bgColorSet, ColorSet borderColorSet) {
		this(x, y, width, height, width > height ? height / 5 : width / 5,
				bgColorSet, borderColorSet);
	}

	public RoundedRectIconSource(float x, float y, float width, float height,
			float cornerRadius, ColorSet bgColorSet, ColorSet borderColorSet) {

		float centerX = width / 2;
		float centerY = height / 2;

		float scaledW = width - 2;
		float scaledH = height - 2;

		float dim = Math.min(width, height);
		float downW = scaledW - dim * .15f;
		float downH = scaledH - dim * .15f;

		defaultIcon = new RoundedRectIcon(x + 1, y + 1, scaledW, scaledH,
				cornerRadius, bgColorSet.defaultColor,
				borderColorSet.defaultColor);
		pressedIcon = new RoundedRectIcon(x + centerX - downW / 2, y + centerY
				- downH / 2, downW, downH, cornerRadius,
				bgColorSet.pressedColor, borderColorSet.pressedColor);

		// copy vertices from pressed mesh into selected mesh (same size) but
		// use different colors
		RoundedRectMesh pressedMesh = ((RoundedRectIcon) pressedIcon).roundedRectMesh;
		RoundedRectOutlineMesh pressedMeshOutline = ((RoundedRectIcon) pressedIcon).roundedRectOutlineMesh;

		RoundedRectMesh selectedMesh = new RoundedRectMesh(
				pressedMesh.getVertices(), bgColorSet.selectedColor);
		RoundedRectOutlineMesh selectedOutlineMesh = new RoundedRectOutlineMesh(
				pressedMeshOutline.getVertices(), borderColorSet.selectedColor);

		selectedIcon = new RoundedRectIcon(downW, downH, selectedMesh,
				selectedOutlineMesh);
	}
}
