package com.kh.beatbot.global;

public class GeneralUtils {
	public static float distanceFromPointSquared(float pointX, float pointY, float x, float y) {
		return (x - pointX)*(x - pointX) + (y - pointY)*(y - pointY);
	}
	
	public static float dbToUnit(float db) {
		// db range = -60 - 0, need range 0-1
		return db <= -60 ? 0 : db/60 + 1;
	}
}
