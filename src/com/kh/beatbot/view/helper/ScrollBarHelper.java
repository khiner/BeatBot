package com.kh.beatbot.view.helper;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.ClickableBBView;

public class ScrollBarHelper {
	private static final float DAMP_CONSTANT = 0.9f;
	private static final int CORNER_RESOLUTION = 15;

	private static float[] innerScrollBarColor = { 1, 1, 1, .8f };
	private static float[] outerScrollBarColor = Colors.VOLUME.clone();

	private static float translateX = 0;
	private static float translateY = 0;

	private static long scrollViewStartTime = 0;
	private static long scrollViewEndTime = Long.MAX_VALUE;
	private static int innerScrollBarHeight = 20;
	private static int outerScrollBarHeight = 30;
	private static int innerScrollBarCornerRadius = 7;
	private static int outerScrollBarCornerRadius = 10;

	public static boolean scrolling = false;
	public static float scrollXVelocity = 0;
	public static float scrollYVelocity = 0;

	private static FloatBuffer innerScrollBarVb = null;
	private static FloatBuffer outerScrollBarVb = null;
	private static FloatBuffer scrollBarLinesVb = null;

	private static boolean shouldDrawScrollView() {
		return scrolling
				|| scrollXVelocity != 0
				|| Math.abs(System.currentTimeMillis() - scrollViewEndTime) <= ClickableBBView.DOUBLE_TAP_TIME * 2;
	}

	public static void startScrollView() {
		long now = System.currentTimeMillis();
		if (now - scrollViewEndTime > ClickableBBView.DOUBLE_TAP_TIME * 2)
			scrollViewStartTime = now;
		else
			scrollViewEndTime = Long.MAX_VALUE;
		scrolling = true;
	}

	public static void drawScrollView(BBView view) {
		if (!shouldDrawScrollView())
			return;
		updateScrollBar(view);
		// if scrolling is still in progress, elapsed time is relative to the
		// time of scroll start,
		// otherwise, elapsed time is relative to scroll end time
		boolean scrollingEnded = scrollViewStartTime < scrollViewEndTime;
		long elapsedTime = scrollingEnded ? System.currentTimeMillis()
				- scrollViewEndTime : System.currentTimeMillis()
				- scrollViewStartTime;

		float alpha = .8f;
		if (!scrollingEnded && elapsedTime <= ClickableBBView.DOUBLE_TAP_TIME)
			// fade in
			alpha *= elapsedTime / (float) ClickableBBView.DOUBLE_TAP_TIME;
		else if (scrollingEnded && elapsedTime > ClickableBBView.DOUBLE_TAP_TIME)
			// fade out
			alpha *= 2 - elapsedTime / (float) ClickableBBView.DOUBLE_TAP_TIME;
		innerScrollBarColor[3] = alpha;
		outerScrollBarColor[3] = alpha * .6f;
		BBView.translate(0, translateY);
		BBView.drawLines(scrollBarLinesVb, outerScrollBarColor, 3,
				GL10.GL_LINES);
		BBView.translate(translateX, 0);
		BBView.drawTriangleFan(outerScrollBarVb, outerScrollBarColor);
		BBView.drawTriangleFan(innerScrollBarVb, innerScrollBarColor);
		BBView.translate(-translateX, -translateY);
	}

	public static void tickScrollVelocity() {
		// dampen x/y velocity
		scrollXVelocity *= DAMP_CONSTANT;
		scrollYVelocity *= DAMP_CONSTANT;
		if (Math.abs(scrollXVelocity) < 1) {
			scrollXVelocity = 0;
			scrollViewEndTime = System.currentTimeMillis();
		}
		if (Math.abs(scrollYVelocity) < 1) {
			scrollYVelocity = 0;
		}
	}

	public static void updateScrollBar(BBView view) {
		float x1 = TickWindowHelper.getTickOffset() * view.width
				/ TickWindowHelper.MAX_TICKS;
		float x2 = (TickWindowHelper.getTickOffset() + TickWindowHelper
				.getNumTicks()) * view.width / TickWindowHelper.MAX_TICKS;
		float outerWidth = x2 - x1;
		float innerWidth = outerWidth - 10;
		translateX = (x2 + x1) / 2;
		translateY = view.height - outerScrollBarHeight / 2;
		innerScrollBarVb = BBView.makeRoundedCornerRectBuffer(
				innerWidth, innerScrollBarHeight, innerScrollBarCornerRadius,
				CORNER_RESOLUTION);
		outerScrollBarVb = BBView.makeRoundedCornerRectBuffer(
				outerWidth, outerScrollBarHeight, outerScrollBarCornerRadius,
				CORNER_RESOLUTION);
		scrollBarLinesVb = BBView.makeFloatBuffer(new float[] { 0, 0, x1, 0, x2, 0, view.width, 0 });
	}

	public static void handleActionUp() {
		scrolling = false;
		tickScrollVelocity();
	}
}
