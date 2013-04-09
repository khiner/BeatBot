package com.kh.beatbot.global;

import com.kh.beatbot.view.mesh.RoundedRectMesh;
import com.kh.beatbot.view.mesh.RoundedRectOutlineMesh;

public class RoundedRectIcon extends Icon {

	public RoundedRectMesh roundedRectMesh;
	public RoundedRectOutlineMesh roundedRectOutlineMesh;
	private float width, height;
	
	public RoundedRectIcon(float x, float y, float width, float height, float cornerRadius, float[] bgColor, float[] borderColor) {
		this.width = width;
		this.height = height;
		roundedRectMesh = new RoundedRectMesh(x, y, width, height, cornerRadius, 16, bgColor);
		roundedRectOutlineMesh = new RoundedRectOutlineMesh(x, y, width, height, cornerRadius, 16, borderColor);
	}
	
	public RoundedRectIcon(float width, float height, RoundedRectMesh roundedRectMesh,
			RoundedRectOutlineMesh roundedRectOutlineMesh) {
		this.width = width;
		this.height = height;
		this.roundedRectMesh = roundedRectMesh;
		this.roundedRectOutlineMesh = roundedRectOutlineMesh; 
	}
	
	@Override
	public void draw(float x, float y) {
		draw(x, y, width, height);
	}

	@Override
	public void draw(float x, float y, float width, float height) {
		// rendering of meshes is done globally
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
