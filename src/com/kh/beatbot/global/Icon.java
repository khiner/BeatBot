package com.kh.beatbot.global;


public abstract class Icon {
	public abstract void draw(float x, float y);
	public abstract void draw(float x, float y, float width, float height);
	public abstract float getWidth();
	public abstract float getHeight();
}
