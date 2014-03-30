package com.kh.beatbot.ui.view.helper;

public class ScrollBarHelper {
	private static final float DAMP_CONSTANT = 0.9f;

	public static boolean scrolling = false;
	public static float scrollXVelocity = 0, scrollYVelocity = 0;

	public static void startScrollView() {
		scrolling = true;
	}

	public static void tickScrollVelocity() {
		// dampen x/y velocity
		scrollXVelocity *= DAMP_CONSTANT;
		scrollYVelocity *= DAMP_CONSTANT;
		if (Math.abs(scrollXVelocity) < 1) {
			scrollXVelocity = 0;
		}
		if (Math.abs(scrollYVelocity) < 1) {
			scrollYVelocity = 0;
		}
	}

	public static void handleActionUp() {
		scrolling = false;
	}
}
