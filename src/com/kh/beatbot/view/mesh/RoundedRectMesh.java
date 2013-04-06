package com.kh.beatbot.view.mesh;

import javax.microedition.khronos.opengles.GL10;

import android.util.FloatMath;

import com.kh.beatbot.global.Colors;

public class RoundedRectMesh extends Mesh2D {

	private float width, height, cornerRadius;
	
	public RoundedRectMesh(GL10 gl, float width, float height, float cornerRadius, int resolution) {
		super(gl, resolution * 4, true);
		this.width = width;
		this.height = height;
		this.cornerRadius = cornerRadius;
		addVertices();
	}

	private void addVertices() {
		float theta = 0, addX, addY;
		for (int i = 0; i < vertices.length / 2; i++) {
			theta += 4 * ¹ / vertices.length;
			if (theta < ¹ / 2) { // lower right
				addX = width / 2 - cornerRadius;
				addY = height / 2 - cornerRadius;
			} else if (theta < ¹) { // lower left
				addX = -width / 2 + cornerRadius;
				addY = height / 2 - cornerRadius;
			} else if (theta < 3 * ¹ / 2) { // upper left
				addX = -width / 2 + cornerRadius;
				addY = -height / 2 + cornerRadius;
			} else { // upper right
				addX = width / 2 - cornerRadius;
				addY = -height / 2 + cornerRadius;
			}
			color(Colors.GREEN);
			vertex(FloatMath.cos(theta) * cornerRadius + addX, FloatMath.sin(theta) * cornerRadius + addY); 
		}
	}
}
