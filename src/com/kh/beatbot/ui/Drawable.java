package com.kh.beatbot.ui;


public abstract class Drawable {
	public float x, y, width, height;
	
	public abstract void draw(float x, float y, float width, float height);
	
	public void draw() {
		draw(x, y, width, height);
	}
	
	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void layout(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}
