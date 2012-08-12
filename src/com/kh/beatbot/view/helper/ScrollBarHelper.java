package com.kh.beatbot.view.helper;

import java.nio.FloatBuffer;

import com.kh.beatbot.view.SurfaceViewBase;
import com.kh.beatbot.view.bean.MidiViewBean;

public class ScrollBarHelper {
	private static FloatBuffer scrollBarVb = null;
	private static long scrollViewStartTime = 0;
	private static long scrollViewEndTime = Long.MAX_VALUE;
	private static int scrollBarHeight = 20;
	private static float[] scrollBarColor = {1, 1, 1, .8f}; 

	public static boolean scrolling = false;
	public static long scrollVelocity = 0;
	
	private static boolean shouldDrawScrollView() {
		return scrolling
				|| scrollVelocity != 0
				|| Math.abs(System.currentTimeMillis()
						- scrollViewEndTime) <= MidiViewBean.DOUBLE_TAP_TIME * 2;
	}
	
	public static void startScrollView() {
		long now = System.currentTimeMillis();
		if (now - scrollViewEndTime > MidiViewBean.DOUBLE_TAP_TIME * 2)
			scrollViewStartTime = now;
		else
			scrollViewEndTime = Long.MAX_VALUE;
		scrolling = true;
	}
	
	public static void drawScrollView() {
		if (!shouldDrawScrollView())
			return;
		// if scrolling is still in progress, elapsed time is relative to the
		// time of scroll start,
		// otherwise, elapsed time is relative to scroll end time
		boolean scrollingEnded = scrollViewStartTime < scrollViewEndTime;
		long elapsedTime = scrollingEnded ? System.currentTimeMillis()
				- scrollViewEndTime : System.currentTimeMillis()
				- scrollViewStartTime;

		float alpha = .8f;
		if (!scrollingEnded && elapsedTime <= MidiViewBean.DOUBLE_TAP_TIME)
			alpha *= elapsedTime / (float) MidiViewBean.DOUBLE_TAP_TIME;
		else if (scrollingEnded && elapsedTime > MidiViewBean.DOUBLE_TAP_TIME)
			alpha *= (MidiViewBean.DOUBLE_TAP_TIME * 2 - elapsedTime)
					/ (float) MidiViewBean.DOUBLE_TAP_TIME;
		scrollBarColor[3] = alpha;
		SurfaceViewBase.drawTriangleStrip(scrollBarVb, scrollBarColor);
	}
	
	public static void tickScrollVelocity() {
		scrollVelocity *= 0.95;
		if (scrollVelocity == 0) {
			scrollViewEndTime = System.currentTimeMillis();
		}
	}
	
	public static void updateScrollbar(float parentWidth, float parentHeight) {
		float x1 = TickWindowHelper.getTickOffset() * parentWidth
				/ TickWindowHelper.MAX_TICKS;
		float x2 = (TickWindowHelper.getTickOffset() + TickWindowHelper.getNumTicks())
				* parentWidth / TickWindowHelper.MAX_TICKS;
		scrollBarVb = SurfaceViewBase.makeRectFloatBuffer(x1, parentHeight - scrollBarHeight, x2, parentHeight);
	}
	
	public static void handleActionUp() {
		scrolling = false;
		if (scrollVelocity == 0)
			scrollViewEndTime = System.currentTimeMillis();
	}
}
