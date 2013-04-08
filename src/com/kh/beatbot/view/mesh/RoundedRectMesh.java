package com.kh.beatbot.view.mesh;

import javax.microedition.khronos.opengles.GL10;

public class RoundedRectMesh extends Mesh2D {

	private float x, y, width, height, cornerRadius;
	
	private float[] mColor;

	public RoundedRectMesh(float x, float y, float width, float height,
			float cornerRadius, int resolution, float[] color) {
		super(GL10.GL_TRIANGLES, resolution * 4 * 3);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.cornerRadius = cornerRadius;
		mColor = color;
		createVertices();
	}

	private void createVertices() {
		float theta = 0, addX, addY;
		float centerX = x + width / 2;
		float centerY = y + height / 2;
		float lastX = 0, lastY = 0;
		for (int i = 0; i < vertices.length / 6; i++) {
			theta += 4 * ¹ / (vertices.length / 3);
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
				vertex(centerX, centerY, mColor);
			}
			lastX = vertexX;
			lastY = vertexY;

		}
		vertex(vertices[0], vertices[1], mColor);
		vertex(lastX, lastY, mColor);
		vertex(centerX, centerY, mColor);
	}
}
