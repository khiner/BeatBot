package com.kh.beatbot.view.mesh;

import android.util.Log;


public class RoundedRectOutlineMesh extends Mesh2D {

	private float x, y, width, height, cornerRadius;

	private float[] mColor;
	
	public RoundedRectOutlineMesh(float x, float y, float width, float height,
			float cornerRadius, int resolution, float[] color) {
		super(resolution * 4 * 2);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.cornerRadius = cornerRadius;
		mColor = color;
		createVertices();
		if (index < numVertices) {
			Log.e("RoundedRectOutlineMesh", "Did not fill vertices! " + index + ", " + numVertices);
		}
	}

	public RoundedRectOutlineMesh(float[] vertices, float[] color) {
		super(vertices, color);
	}
	
	private void createVertices() {
		float theta = 0, addX, addY;
		float lastX = 0, lastY = 0;
		for (int i = 0; i < vertices.length / 4; i++) {
			if (theta < ¹ / 2) { // lower right
				addX = width - cornerRadius;
				addY = height - cornerRadius;
			} else if (theta < ¹) { // lower left
				addX = cornerRadius;
				addY = height - cornerRadius;
			} else if (theta < 3 * ¹ / 2) { // upper left
				addX = cornerRadius;
				addY = cornerRadius;
			} else { // upper right
				addX = width - cornerRadius;
				addY = cornerRadius;
			}
			float vertexX = (float) Math.cos(theta) * cornerRadius + addX + x;
			float vertexY = (float) Math.sin(theta) * cornerRadius + addY + y;
			if (lastX != 0 && lastY != 0) {
				vertex(vertexX, vertexY, mColor);
				vertex(lastX, lastY, mColor);
			}
			lastX = vertexX;
			lastY = vertexY;
			theta += 4 * ¹ / (vertices.length / 2);
		}
		vertex(vertices[0], vertices[1], mColor);
		vertex(lastX, lastY, mColor);
	}
}
