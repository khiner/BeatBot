package com.kh.beatbot.view.helper;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.view.bean.MidiViewBean;

public class TickWindowHelper {
	public static MidiViewBean viewBean;

	public static final float MIN_TICKS = MidiManager.TICKS_IN_ONE_MEASURE / 8;
	public static final float MAX_TICKS = MidiManager.TICKS_IN_ONE_MEASURE * 4;
	public static final int MIN_LINES_DISPLAYED = 8;
	public static final int MAX_LINES_DISPLAYED = 32;

	// leftmost tick to display
	private static float currTickOffset = 0;
	// current number of ticks within the window
	private static float currNumTicks = MidiManager.TICKS_IN_ONE_MEASURE - 1;

	private static float granularity = 1;

	public static float getNumTicks() {
		return currNumTicks;
	}

	public static float getTickOffset() {
		return currTickOffset;
	}

	public static void zoom(float leftX, float rightX) {
		float ZLAT = viewBean.getZoomLeftAnchorTick();
		float ZRAT = viewBean.getZoomRightAnchorTick();
		float newTOS = (ZRAT * leftX - ZLAT * rightX) / (leftX - rightX);
		float newNumTicks = (ZLAT - newTOS) * viewBean.getWidth() / leftX;

		if (newTOS < 0 && newTOS + newNumTicks > MAX_TICKS
				|| newNumTicks < MIN_TICKS) {
			return;
		}
		if (newTOS >= 0 && newTOS + newNumTicks <= MAX_TICKS) {
			currTickOffset = newTOS;
			currNumTicks = newNumTicks;
		} else if (newTOS < 0) {
			currTickOffset = 0;
			currNumTicks = ZRAT * viewBean.getWidth() / rightX;
			currNumTicks = currNumTicks > MAX_TICKS ? MAX_TICKS : currNumTicks;
		} else if (newTOS + newNumTicks > MAX_TICKS) {
			currNumTicks = (ZLAT - MAX_TICKS) / (leftX
					/ viewBean.getWidth() - 1);
			currTickOffset = MAX_TICKS - currNumTicks;
		}
		updateGranularity();
	}

	public static void scroll() {
		float sv = ScrollBarHelper.scrollVelocity;
		if (!ScrollBarHelper.scrolling && sv != 0) {
			ScrollBarHelper.tickScrollVelocity();
			setTickOffset(currTickOffset + sv);
		}
	}

	public static void scroll(float x) {
		float newTickOffset = viewBean.getScrollAnchorTick()
				- (currNumTicks * x) / viewBean.getWidth();
		float diff = newTickOffset - currTickOffset;
		setTickOffset(newTickOffset);
		ScrollBarHelper.scrollVelocity = diff;
	}

	public static void updateGranularity() {
		// x-coord width of one eight note
		float spacing = ((float) MidiManager.TICKS_IN_ONE_MEASURE * viewBean
				.getWidth()) / (currNumTicks * 8);
		// after algebra, this condition says: if more than maxLines will
		// display, reduce the granularity by one half, else if less than
		// maxLines will display, increase the granularity by one half
		// so that (minLinesDisplayed <= lines-displayed <=
		// maxLinesDisplayed) at all times
		if ((MAX_LINES_DISPLAYED * spacing) / granularity < viewBean.getWidth())
			granularity /= 2;
		else if ((MIN_LINES_DISPLAYED * spacing) / granularity > viewBean
				.getWidth() && granularity < 4)
			granularity *= 2;
		GlobalVars.currBeatDivision = granularity * 2;
	}

	public static void setTickOffset(float tickOffset) {
		if (tickOffset + currNumTicks <= MAX_TICKS && tickOffset >= 0) {
			currTickOffset = tickOffset;
		} else if (tickOffset < 0)
			currTickOffset = 0;
		else if (tickOffset + currNumTicks > MAX_TICKS)
			currTickOffset = MAX_TICKS - currNumTicks;
	}

	public static boolean setNumTicks(float numTicks) {
		if (numTicks <= MAX_TICKS && numTicks >= MIN_TICKS) {
			currNumTicks = numTicks;
			updateGranularity();
			return true;
		}
		return false;
	}

	public static float getCurrentBeatDivision() {
		// currently, granularity is relative to an eight note; x2 for quarter
		// note
		return granularity * 2;
	}

	public static float getMajorTickSpacing() {
		return MIN_TICKS / granularity;
	}

	public static float getMajorTickToLeftOf(float tick) {
		return tick - tick % getMajorTickSpacing();
	}

	public static float getPrimaryTickToLeftOf(float tick) {
		return tick - tick % (getMajorTickSpacing() * 4);
	}

	// translates the tickOffset to ensure that leftTick and rightTick are
	// in view
	// @returns the amount translated
	public static void updateView(float leftTick, float rightTick) {
		// if we are dragging out of view, scroll appropriately
		// if the right is out but the left is in, just scroll
		if (leftTick < currTickOffset
				&& rightTick < currTickOffset + currNumTicks) {
			setTickOffset(leftTick);

			// if the left is out but the right is in, just scroll
		} else if (rightTick > currTickOffset + currNumTicks
				&& leftTick > currTickOffset) {
			setTickOffset(rightTick - currNumTicks);

			// if both left and right are out of view,
		} else if (leftTick <= currTickOffset
				&& rightTick >= currTickOffset + currNumTicks) {
			setTickOffset(leftTick);
			setNumTicks(rightTick - leftTick);
		}
	}
}
