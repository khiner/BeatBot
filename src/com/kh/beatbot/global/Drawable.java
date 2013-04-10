package com.kh.beatbot.global;


public interface Drawable {
	public abstract void draw();
	public abstract void draw(float x, float y);
	public abstract void draw(float x, float y, float width, float height);
	public abstract float getX();
	public abstract float getY();
	public abstract float getWidth();
	public abstract float getHeight();
}
