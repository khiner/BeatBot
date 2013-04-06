package com.kh.beatbot.global;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.mesh.RoundedRectMesh;

public class RoundedRectIcon extends Icon {

	private RoundedRectMesh roundedRectMesh;
	private float borderWeight;
	private float[] bgColor;
	private float[] borderColor;
	private float width, height, xScaleFactor, yScaleFactor;
	
	public RoundedRectIcon(RoundedRectMesh roundedRectMesh, float width, float height, float borderWeight, float[] bgColor, float[] borderColor) {
		this.roundedRectMesh = roundedRectMesh;
		this.width = width;
		this.height = height;
		this.borderWeight = borderWeight;
		this.bgColor = bgColor;
		this.borderColor = borderColor;
		xScaleFactor = width / (borderWeight + width + 1);
		yScaleFactor = height / (borderWeight + height + 1);
	}
	
	@Override
	public void draw(float x, float y) {
		draw(x, y, width, height);
	}

	@Override
	public void draw(float x, float y, float width, float height) {
		BBView.push();
		BBView.translate(width / 2, height / 2);
		BBView.scale(xScaleFactor, yScaleFactor);
		//BBView.setColor(bgColor);
		roundedRectMesh.render(GL10.GL_TRIANGLE_FAN);
		//BBView.setColor(borderColor);
		roundedRectMesh.render(GL10.GL_LINE_LOOP);
		BBView.pop();
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
