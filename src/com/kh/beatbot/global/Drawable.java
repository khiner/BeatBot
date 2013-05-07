package com.kh.beatbot.global;


public abstract class Drawable {
	protected float x, y, width, height;
	
	public abstract void draw(float x, float y, float width, float height);
	
	public void draw() {
		draw(x, y, width, height);
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getWidth() {
		return width;
	}
	
	public float getHeight() {
		return height;
	}
	
	public void layout(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}
