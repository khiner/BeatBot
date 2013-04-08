package com.kh.beatbot.global;

import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.mesh.RoundedRectMesh;
import com.kh.beatbot.view.mesh.RoundedRectOutlineMesh;

public class RoundedRectIcon extends Icon {

	private RoundedRectMesh roundedRectMesh;
	private RoundedRectOutlineMesh roundedRectOutlineMesh;
	private float width, height;
	
	public RoundedRectIcon(float x, float y, float width, float height, float cornerRadius, float[] bgColor, float[] borderColor) {
		this.width = width;
		this.height = height;
		roundedRectMesh = new RoundedRectMesh(x, y, width, height, cornerRadius, 16, bgColor);
		roundedRectOutlineMesh = new RoundedRectOutlineMesh(x, y, width, height, cornerRadius, 32, borderColor);
	}
	
	@Override
	public void draw(float x, float y) {
		draw(x, y, width, height);
	}

	@Override
	public void draw(float x, float y, float width, float height) {
		roundedRectMesh.render();
		BBView.gl.glLineWidth(1);
		roundedRectOutlineMesh.render();
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public float getHeight() {
		return height;
	}
}
