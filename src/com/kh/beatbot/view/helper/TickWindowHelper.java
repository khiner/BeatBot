package com.kh.beatbot.view.helper;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.view.MidiView;

public class TickWindowHelper {

	private static MidiView midiView = null;

	public static final float MIN_TICKS = MidiManager.TICKS_IN_ONE_MEASURE / 8;
	public static final float MAX_TICKS = MidiManager.TICKS_IN_ONE_MEASURE * 4;
	public static final int MIN_LINES_DISPLAYED = 8;
	public static final int MAX_LINES_DISPLAYED = 32;

	// leftmost tick to display
	private static float currTickOffset = 0;
	private static float currYOffset = 0;

	// current number of ticks within the window
	private static float currNumTicks = MidiManager.TICKS_IN_ONE_MEASURE + 4;

	private static float granularity = 1;

	public static float getNumTicks() {
		return currNumTicks;
	}

	public static float getTickOffset() {
		return currTickOffset;
	}

	public static float getYOffset() {
		return currYOffset;
	}

	public static void init(MidiView _midiView) {
		midiView = _midiView;
		updateGranularity();
	}

	public static void zoom(float leftX, float rightX) {
		float ZLAT = MidiView.zoomLeftAnchorTick;
		float ZRAT = MidiView.zoomRightAnchorTick;
		float newTOS = (ZRAT * leftX - ZLAT * rightX) / (leftX - rightX);
		float newNumTicks = (ZLAT - newTOS) * midiView.width / leftX;

		if (newTOS < 0) {
			currTickOffset = 0;
			currNumTicks = ZRAT * midiView.width / rightX;
			currNumTicks = currNumTicks > MAX_TICKS ? MAX_TICKS : currNumTicks;
		} else if (newNumTicks > MAX_TICKS) {
			currTickOffset = newTOS;
			currNumTicks = MAX_TICKS - currTickOffset;
		} else if (newNumTicks < MIN_TICKS) {
			currTickOffset = newTOS;
			currNumTicks = MIN_TICKS;
		} else if (newTOS + newNumTicks > MAX_TICKS) {
			currNumTicks = ((ZLAT - MAX_TICKS) * midiView.width) / (leftX - midiView.width);
			currTickOffset = MAX_TICKS - currNumTicks;
		} else {
			currTickOffset = newTOS;
			currNumTicks = newNumTicks;
		}
		updateGranularity();
	}

	public static void scroll() {
		if (ScrollBarHelper.scrolling) {
			return;
		}
		if (ScrollBarHelper.scrollXVelocity != 0) {
			ScrollBarHelper.tickScrollVelocity();
			setTickOffset(currTickOffset + ScrollBarHelper.scrollXVelocity);
		}
		if (ScrollBarHelper.scrollYVelocity != 0) {
			setYOffset(currYOffset + ScrollBarHelper.scrollYVelocity);
		}
	}

	public static void scroll(float x, float y) {
		float newTickOffset = MidiView.scrollAnchorTick - currNumTicks * x / midiView.width;
		float newYOffset = MidiView.scrollAnchorY - y;
		ScrollBarHelper.scrollXVelocity = newTickOffset - currTickOffset;
		ScrollBarHelper.scrollYVelocity = newYOffset - currYOffset;
		setTickOffset(newTickOffset);
		setYOffset(newYOffset);
	}

	public static void updateGranularity() {
		// x-coord width of one eight note
		float spacing = (MidiManager.TICKS_IN_ONE_MEASURE * midiView.width) / (currNumTicks * 8);
		// after algebra, this condition says: if more than maxLines will
		// display, reduce the granularity by one half, else if less than
		// maxLines will display, increase the granularity by one half
		// so that (minLinesDisplayed <= lines-displayed <=
		// maxLinesDisplayed) at all times
		if ((MAX_LINES_DISPLAYED * spacing) / granularity < midiView.width)
			granularity /= 2;
		else if ((MIN_LINES_DISPLAYED * spacing) / granularity > midiView.width && granularity < 4)
			granularity *= 2;
		GlobalVars.currBeatDivision = granularity * 2;
	}

	public static void setTickOffset(float tickOffset) {
		if (tickOffset < 0) {
			currTickOffset = 0;
		} else if (tickOffset + currNumTicks > MAX_TICKS) {
			currTickOffset = MAX_TICKS - currNumTicks;
		} else {
			currTickOffset = tickOffset;
		}
	}

	public static void setYOffset(float yOffset) {
		if (yOffset < 0) {
			currYOffset = 0;
		} else if (yOffset + midiView.getMidiHeight() > MidiView.allTracksHeight) {
			currYOffset = MidiView.allTracksHeight - midiView.getMidiHeight();
		} else {
			currYOffset = yOffset;
		}
	}

	public static void setNumTicks(float numTicks) {
		if (numTicks > MAX_TICKS || numTicks < MIN_TICKS) {
			return;
		}
		currNumTicks = numTicks;
		updateGranularity();
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

	public static void updateView(float leftTick, float rightTick) {
		// if we are dragging out of view, scroll appropriately
		if (leftTick < currTickOffset
				&& rightTick > currTickOffset + currNumTicks) {
			// both left and right are out of view, change view to fit
			setTickOffset(leftTick);
			setNumTicks(rightTick - leftTick);
		} else if (leftTick < currTickOffset) {
			// if the left is out but the right is in, just scroll
			setTickOffset(leftTick);
		} else if (rightTick > currTickOffset + currNumTicks) {
			// if the right is out but the left is in, just scroll
			setTickOffset(rightTick - currNumTicks);
		}
	}

	// translates the tickOffset to ensure that leftTick, rightTick, topY and
	// bottomY are all in view
	public static void updateView(float leftTick, float rightTick, float topY,
			float bottomY) {
		updateView(leftTick, rightTick);
		if (topY < currYOffset
				&& bottomY < currYOffset + midiView.getMidiHeight()) {
			setYOffset(topY);
		} else if (bottomY > currYOffset + midiView.getMidiHeight()
				&& topY > currYOffset) {
			setYOffset(bottomY - midiView.getMidiHeight());
		}
	}
}
