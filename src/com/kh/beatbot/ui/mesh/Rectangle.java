package com.kh.beatbot.ui.mesh;


public class Rectangle extends Shape {
	
	protected Rectangle(ShapeGroup group) {
		super(group);
	}

	protected int getNumFillVertices() {
		return 6;
	}
	
	protected int getNumStrokeVertices() {
		return 8;
	}

	/********
	 * ^--^ *
	 * |1/| *
	 * |/2| *
	 * ^--^ *
	 ********/
	protected synchronized void updateVertices() {
		// fill triangle 1
		fillVertex(x, y);
		fillVertex(x + width, y);
		fillVertex(x, y + height);
		// fill triangle 2
		fillVertex(x, y + height);
		fillVertex(x + width, y);
		fillVertex(x + width, y + height);
		
		// outline
		strokeVertex(x, y);
		strokeVertex(x + width, y);
		strokeVertex(x + width, y);
		strokeVertex(x + width, y + height);
		strokeVertex(x + width, y + height);
		strokeVertex(x, y + height);
		strokeVertex(x, y + height);
		strokeVertex(x, y);
	}
}
