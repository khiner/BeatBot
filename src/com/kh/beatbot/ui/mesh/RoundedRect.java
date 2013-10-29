package com.kh.beatbot.ui.mesh;

public class RoundedRect extends Shape {
	public float cornerRadius = -1;

	public RoundedRect(ShapeGroup group, float[] fillColor) {
		super(group, new Mesh2D(16 * 4 * 3, fillColor));
	}

	public RoundedRect(ShapeGroup group, float[] fillColor, float[] outlineColor) {
		super(group, new Mesh2D(16 * 4 * 3, fillColor), new Mesh2D(16 * 4 * 2,
				outlineColor));
	}

	public void setCornerRadius(float cornerRadius) {
		this.cornerRadius = cornerRadius;
	}

	protected void createVertices(float[] fillColor) {
		createVertices(fillColor, null);
	}

	protected void createVertices(float[] fillColor, float[] outlineColor) {
		float theta = 0, addX, addY;
		float centerX = x + width / 2;
		float centerY = y + height / 2;
		float lastX = 0, lastY = 0;
		for (int i = 0; i < fillMesh.getNumVertices() / 3; i++) {
			if (theta < ¹ / 2) { // lower right
				addX = width - cornerRadius;
				addY = height - cornerRadius;
			} else if (theta < ¹) { // lower left
				addX = cornerRadius;
				addY = height - cornerRadius;
			} else if (theta < 3 * ¹ / 2) { // upper left
				addX = addY = cornerRadius;
			} else { // upper right
				addX = width - cornerRadius;
				addY = cornerRadius;
			}
			float vertexX = (float) Math.cos(theta) * cornerRadius + addX + x;
			float vertexY = (float) Math.sin(theta) * cornerRadius + addY + y;
			if (lastX != 0 && lastY != 0) {
				fillMesh.vertex(vertexX, vertexY);
				fillMesh.vertex(lastX, lastY);
				fillMesh.vertex(centerX, centerY);
				if (outlineColor != null) {
					outlineMesh.vertex(vertexX, vertexY);
					outlineMesh.vertex(lastX, lastY);
				}
			}
			lastX = vertexX;
			lastY = vertexY;
			theta += 6 * ¹ / fillMesh.getNumVertices();
		}
		fillMesh.vertex(fillMesh.getVertices()[0], fillMesh.getVertices()[1]);
		fillMesh.vertex(lastX, lastY);
		fillMesh.vertex(centerX, centerY);
		if (outlineColor != null) {
			outlineMesh.vertex(outlineMesh.getVertices()[0],
					outlineMesh.getVertices()[1]);
			outlineMesh.vertex(lastX, lastY);
		}

		fillMesh.setColor(fillColor);
		if (outlineColor != null) {
			outlineMesh.setColor(outlineColor);
		}
	}

	@Override
	public void layout(float x, float y, float width, float height) {
		if (cornerRadius < 0) {
			cornerRadius = width > height ? height / 5 : width / 5;
		}
		super.layout(x, y, width, height);
	}
}
