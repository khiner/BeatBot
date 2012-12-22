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
		float newNumTicks = (ZLAT - newTOS) * midiView.getMidiWidth() / leftX;

		if (newTOS < 0) {
			currTickOffset = 0;
			currNumTicks = ZRAT * midiView.getMidiWidth() / rightX;
			currNumTicks = currNumTicks > MAX_TICKS ? MAX_TICKS : currNumTicks;
		} else if (newNumTicks > MAX_TICKS) {
			currTickOffset = newTOS;
			currNumTicks = MAX_TICKS - currTickOffset;
		} else if (newNumTicks < MIN_TICKS) {
			currTickOffset = newTOS;
			currNumTicks = MIN_TICKS;
		} else if (newTOS + newNumTicks <= MAX_TICKS) {
			currTickOffset = newTOS;
			currNumTicks = newNumTicks;
		} else {
			currNumTicks = (ZLAT - MAX_TICKS) / (leftX
					/ midiView.getMidiWidth() - 1);
			currTickOffset = MAX_TICKS - currNumTicks;
		}
		updateGranularity();
	}

	public static void scroll() {
		if (ScrollBarHelper.scrolling) {
			return;
		}
		ScrollBarHelper.tickScrollVelocity();
		if (ScrollBarHelper.scrollXVelocity != 0) {
			setTickOffset(currTickOffset + ScrollBarHelper.scrollXVelocity);
		}
		if (ScrollBarHelper.scrollYVelocity != 0) {
			setYOffset(currYOffset + ScrollBarHelper.scrollYVelocity);
		}
	}

	public static void scroll(float x, float y) {
		float newTickOffset = MidiView.scrollAnchorTick
				- (currNumTicks * x) / midiView.getMidiWidth();
		float newYOffset = MidiView.scrollAnchorY - y;
		float xDiff = newTickOffset - currTickOffset;
		float yDiff = newYOffset - currYOffset;
		setTickOffset(newTickOffset);
		setYOffset(newYOffset);
		ScrollBarHelper.scrollXVelocity = xDiff;
		ScrollBarHelper.scrollYVelocity = yDiff;
	}

	public static void updateGranularity() {
		// x-coord width of one eight note
		float spacing = ((float) MidiManager.TICKS_IN_ONE_MEASURE * midiView.getMidiWidth()) / (currNumTicks * 8);
		// after algebra, this condition says: if more than maxLines will
		// display, reduce the granularity by one half, else if less than
		// maxLines will display, increase the granularity by one half
		// so that (minLinesDisplayed <= lines-displayed <=
		// maxLinesDisplayed) at all times
		if ((MAX_LINES_DISPLAYED * spacing) / granularity < midiView.getMidiWidth())
			granularity /= 2;
		else if ((MIN_LINES_DISPLAYED * spacing) / granularity > midiView.getMidiWidth() && granularity < 4)
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
	
	public static void setYOffset(float yOffset) {
		if (yOffset + midiView.getMidiHeight() <= MidiView.allTracksHeight && yOffset >= 0) {
			currYOffset = yOffset;
		} else if (yOffset < 0)
			currYOffset = 0;
		else if (yOffset + midiView.getMidiHeight() > MidiView.allTracksHeight)
			currYOffset = MidiView.allTracksHeight - midiView.getMidiHeight();
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
	
	// translates the tickOffset to ensure that leftTick, rightTick, topY and bottomY are all in view
	public static void updateView(float leftTick, float rightTick, float topY, float bottomY) {
		updateView(leftTick, rightTick);
		if (topY < currYOffset && bottomY < currYOffset + midiView.getMidiHeight()) {
			setYOffset(topY);
		} else if (bottomY > currYOffset + midiView.getMidiHeight() && topY > currYOffset) {
			setYOffset(bottomY - midiView.getMidiHeight());
		}
	}
}
