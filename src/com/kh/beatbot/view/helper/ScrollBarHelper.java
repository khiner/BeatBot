package com.kh.beatbot.view.helper;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.SurfaceViewBase;

public class ScrollBarHelper {
	private static final float DAMP_CONSTANT = 0.9f;
	private static final int CORNER_RESOLUTION = 15;

	private static float[] innerScrollBarColor = { 1, 1, 1, .8f };
	private static float[] outerScrollBarColor = Colors.VOLUME_COLOR
			.clone();

	private static float translateX = 0;
	private static float translateY = 0;

	private static long scrollViewStartTime = 0;
	private static long scrollViewEndTime = Long.MAX_VALUE;
	private static int innerScrollBarHeight = 20;
	private static int outerScrollBarHeight = 30;
	private static int innerScrollBarCornerRadius = 7;
	private static int outerScrollBarCornerRadius = 10;

	public static boolean scrolling = false;
	public static float scrollVelocity = 0;

	private static FloatBuffer innerScrollBarVb = null;
	private static FloatBuffer outerScrollBarVb = null;
	private static FloatBuffer scrollBarLinesVb = null;

	private static boolean shouldDrawScrollView() {
		return scrolling
				|| scrollVelocity != 0
				|| Math.abs(System.currentTimeMillis() - scrollViewEndTime) <= GlobalVars.DOUBLE_TAP_TIME * 2;
	}

	public static void startScrollView() {
		long now = System.currentTimeMillis();
		if (now - scrollViewEndTime > GlobalVars.DOUBLE_TAP_TIME * 2)
			scrollViewStartTime = now;
		else
			scrollViewEndTime = Long.MAX_VALUE;
		scrolling = true;
	}

	public static void drawScrollView(float parentWidth, float parentHeight) {
		if (!shouldDrawScrollView())
			return;
		updateScrollBar(parentWidth, parentHeight);
		// if scrolling is still in progress, elapsed time is relative to the
		// time of scroll start,
		// otherwise, elapsed time is relative to scroll end time
		boolean scrollingEnded = scrollViewStartTime < scrollViewEndTime;
		long elapsedTime = scrollingEnded ? System.currentTimeMillis()
				- scrollViewEndTime : System.currentTimeMillis()
				- scrollViewStartTime;

		float alpha = .8f;
		if (!scrollingEnded && elapsedTime <= GlobalVars.DOUBLE_TAP_TIME)
			alpha *= elapsedTime / (float) GlobalVars.DOUBLE_TAP_TIME;
		else if (scrollingEnded && elapsedTime > GlobalVars.DOUBLE_TAP_TIME)
			alpha *= (GlobalVars.DOUBLE_TAP_TIME * 2 - elapsedTime)
					/ (float) GlobalVars.DOUBLE_TAP_TIME;
		innerScrollBarColor[3] = alpha;
		outerScrollBarColor[3] = alpha * .6f;
		SurfaceViewBase.translate(0, translateY);
		SurfaceViewBase.drawLines(scrollBarLinesVb, outerScrollBarColor, 3,
				GL10.GL_LINES);
		SurfaceViewBase.translate(translateX, 0);
		SurfaceViewBase.drawTriangleFan(outerScrollBarVb, outerScrollBarColor);
		SurfaceViewBase.drawTriangleFan(innerScrollBarVb, innerScrollBarColor);
		SurfaceViewBase.translate(-translateX, -translateY);
	}

	public static void tickScrollVelocity() {
		scrollVelocity *= DAMP_CONSTANT; // dampen velocity
		if (Math.abs(scrollVelocity) < 1) {
			scrollVelocity = 0;
			scrollViewEndTime = System.currentTimeMillis();
		}
	}

	public static void updateScrollBar(float parentWidth, float parentHeight) {
		float x1 = TickWindowHelper.getTickOffset() * parentWidth
				/ TickWindowHelper.MAX_TICKS;
		float x2 = (TickWindowHelper.getTickOffset() + TickWindowHelper
				.getNumTicks()) * parentWidth / TickWindowHelper.MAX_TICKS;
		float outerWidth = x2 - x1;
		float innerWidth = outerWidth - 10;
		translateX = (x2 + x1) / 2;
		translateY = parentHeight - outerScrollBarHeight / 2;
		innerScrollBarVb = SurfaceViewBase.makeRoundedCornerRectBuffer(
				innerWidth, innerScrollBarHeight, innerScrollBarCornerRadius,
				CORNER_RESOLUTION);
		outerScrollBarVb = SurfaceViewBase.makeRoundedCornerRectBuffer(
				outerWidth, outerScrollBarHeight, outerScrollBarCornerRadius,
				CORNER_RESOLUTION);
		scrollBarLinesVb = SurfaceViewBase.makeFloatBuffer(new float[] { 0, 0,
				x1, 0, x2, 0, parentWidth, 0 });
	}

	public static void handleActionUp() {
		scrolling = false;
		tickScrollVelocity();
	}
}
