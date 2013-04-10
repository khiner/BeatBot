package com.kh.beatbot.view.mesh;

import android.util.Log;



public class RoundedRect extends Shape {
	private float cornerRadius;
	
	public RoundedRect(ShapeGroup group, float x, float y, float width, float height, float cornerRadius, float[] bgColor, float[] borderColor) {
		super(group, x, y, width, height, new Mesh2D(16 * 4 * 3), new Mesh2D(16 * 4 * 2));
		this.cornerRadius = cornerRadius;
		createVertices(bgColor, borderColor);
		Log.e("Rounded Rect", "fill: " + fillMesh.getNumVertices() + ", " + fillMesh.index);
		Log.e("Rounded Rect", "outline: " + outlineMesh.getNumVertices() + ", " + outlineMesh.index);
	}
	
	public RoundedRect(ShapeGroup group, float x, float y, float width, float height, Mesh2D fillMesh, Mesh2D outlineMesh) {
		super(group, x, y, width, height, fillMesh, outlineMesh);
	}

	private void createVertices(float[] fillColor, float[] outlineColor) {
		float theta = 0, addX, addY;
		float centerX = x + width / 2;
		float centerY = y + height / 2;
		float lastX = 0, lastY = 0;
		for (int i = 0; i < outlineMesh.getNumVertices() / 2; i++) {
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
				fillMesh.vertex(vertexX, vertexY, fillColor);
				fillMesh.vertex(lastX, lastY, fillColor);
				fillMesh.vertex(centerX, centerY, fillColor);
				outlineMesh.vertex(vertexX, vertexY, outlineColor);
				outlineMesh.vertex(lastX, lastY, outlineColor);
			}
			lastX = vertexX;
			lastY = vertexY;
			theta += 4 * ¹ / outlineMesh.getNumVertices();
		}
		fillMesh.vertex(fillMesh.getVertices()[0], fillMesh.getVertices()[1], fillColor);
		fillMesh.vertex(lastX, lastY, fillColor);
		fillMesh.vertex(centerX, centerY, fillColor);
		outlineMesh.vertex(outlineMesh.getVertices()[0], outlineMesh.getVertices()[1], outlineColor);
		outlineMesh.vertex(lastX, lastY, outlineColor);
	}
}
