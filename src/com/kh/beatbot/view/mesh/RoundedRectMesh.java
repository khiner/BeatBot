package com.kh.beatbot.view.mesh;

import javax.microedition.khronos.opengles.GL10;

import android.util.FloatMath;

public class RoundedRectMesh extends Mesh2D {

	private float x, y, width, height, cornerRadius;
	private float[] color;
	
	public RoundedRectMesh(GL10 gl, float x, float y, float width, float height, float cornerRadius, int resolution, float[] color) {
		super(gl, resolution * 4 * 3, resolution * 4, true);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.cornerRadius = cornerRadius;
		this.color = color;
		addVertices();
	}

	private void addVertices() {
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
				addX =  cornerRadius;
				addY = height - cornerRadius;
			} else if (theta < 3 * ¹ / 2) { // upper left
				addX =  cornerRadius;
				addY =  cornerRadius;
			} else { // upper right
				addX = width - cornerRadius;
				addY = cornerRadius;
			}
			float vertexX = FloatMath.cos(theta) * cornerRadius + addX + x;
			float vertexY = FloatMath.sin(theta) * cornerRadius + addY + y;
			if (lastX != 0 && lastY != 0) {
				vertex(vertexX, vertexY);
				vertex(lastX, lastY);
				vertex(centerX, centerY);
			}
			lastX = vertexX;
			lastY = vertexY;
			outlineVertex(vertexX, vertexY); 
		}
		vertex(vertices[0], vertices[1]);
		vertex(lastX, lastY);
		vertex(centerX, centerY);
		color(color);
	}
}
