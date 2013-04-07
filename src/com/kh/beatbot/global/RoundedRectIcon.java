package com.kh.beatbot.global;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.mesh.RoundedRectMesh;

public class RoundedRectIcon extends Icon {

	private RoundedRectMesh roundedRectMesh;
	private float borderWeight;
	private float[] borderColor;
	private float width, height;
	
	public RoundedRectIcon(float x, float y, float width, float height, float cornerRadius, float borderWeight, float[] bgColor, float[] borderColor) {
		roundedRectMesh = new RoundedRectMesh(BBView.gl, x, y, width, height, cornerRadius, 16, bgColor);
		this.width = width;
		this.height = height;
		this.borderWeight = borderWeight;
		this.borderColor = borderColor;
	}
	
	@Override
	public void draw(float x, float y) {
		draw(x, y, width, height);
	}

	@Override
	public void draw(float x, float y, float width, float height) {
		roundedRectMesh.render(GL10.GL_TRIANGLES);
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
