package com.kh.beatbot.view.helper;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.bean.MidiViewBean;

public class TickWindowHelper {
	MidiViewBean viewBean;
	// leftmost tick to display
	private long tickOffset;
	// number of ticks within the window
	private long numTicks;

	private int minLinesDisplayed = 8;
	private int maxLinesDisplayed = 32;

	private long minTicks;
	private long maxTicks;

	private float granularity = 1;

	public TickWindowHelper(MidiViewBean viewBean, long tickOffset, long numTicks) {
		this.viewBean = viewBean;
		this.tickOffset = tickOffset;
		this.numTicks = numTicks;
		minTicks = viewBean.getAllTicks() / 8;
		maxTicks = viewBean.getAllTicks() * 10;
		updateGranularity();
	}

	public long getMaxTicks() {
		return maxTicks;
	}
	
	public void zoom(float leftX, float rightX) {
		long ZLAT = viewBean.getZoomLeftAnchorTick();
		long ZRAT = viewBean.getZoomRightAnchorTick();
		long newTOS = (long) ((ZRAT * leftX - ZLAT
				* rightX) / (leftX - rightX));
		long newNumTicks = (long) ((ZLAT - newTOS) * viewBean.getWidth() / leftX);

		if (newTOS < 0 && newTOS + newNumTicks > maxTicks
				|| newNumTicks < minTicks) {
			return;
		}
		if (newTOS >= 0 && newTOS + newNumTicks <= maxTicks) {
			tickOffset = newTOS;
			numTicks = newNumTicks;
		} else if (newTOS < 0) {
			tickOffset = 0;
			numTicks = (long) (ZRAT * viewBean.getWidth() / rightX);
			numTicks = numTicks > maxTicks ? maxTicks : numTicks;
		} else if (newTOS + newNumTicks > maxTicks) {
			numTicks = (long) ((ZLAT - maxTicks) / (leftX / viewBean.getWidth() - 1));
			tickOffset = maxTicks - numTicks;
		}
		updateGranularity();
	}

	public void scroll() {
		long sv = viewBean.getScrollVelocity();
		if (!viewBean.isScrolling() && sv != 0) {
			viewBean.setScrollVelocity(sv *= 0.95);
			setTickOffset((long) (tickOffset + sv));
			if (sv == 0) {
				viewBean.setScrollViewEndTime(System.currentTimeMillis());
			}
		}
	}

	public long scroll(float x) {
		long newTickOffset = viewBean.getScrollAnchorTick() - (long) ((numTicks * x) / viewBean.getWidth());
		long diff = newTickOffset - tickOffset;
		setTickOffset(newTickOffset);
		return diff;
	}

	public void updateGranularity() {
		// x-coord width of one eight note
		float spacing = ((float) viewBean.getAllTicks() * viewBean.getWidth()) / (numTicks * 8);
		// after algebra, this condition says: if more than maxLines will
		// display, reduce the granularity by one half, else if less than
		// maxLines will display, increase the granularity by one half
		// so that (minLinesDisplayed <= lines-displayed <=
		// maxLinesDisplayed) at all times
		if ((maxLinesDisplayed * spacing) / granularity < viewBean.getWidth())
			granularity /= 2;
		else if ((minLinesDisplayed * spacing) / granularity > viewBean.getWidth()
				&& granularity < 4)
			granularity *= 2;
		GlobalVars.currBeatDivision = granularity * 2;
	}

	public long getTickOffset() {
		return tickOffset;
	}
	
	public void setTickOffset(long tickOffset) {
		if (tickOffset + numTicks <= maxTicks && tickOffset >= 0) {
			this.tickOffset = tickOffset;
		} else if (tickOffset < 0)
			this.tickOffset = 0;
		else if (tickOffset + numTicks > maxTicks)
			this.tickOffset = maxTicks - numTicks;
	}

	public long getNumTicks() {
		return numTicks;
	}
	
	public boolean setNumTicks(long numTicks) {
		if (numTicks <= maxTicks && numTicks >= minTicks) {
			this.numTicks = numTicks;
			updateGranularity();
			return true;
		}
		return false;
	}

	public float getCurrentBeatDivision() {
		// currently, granularity is relative to an eight note; x2 for quarter
		// note
		return granularity * 2;
	}
	
	public long getMajorTickSpacing() {
		return (long) (minTicks / granularity);
	}

	public long getMajorTickToLeftOf(long tick) {
		return tick - tick % getMajorTickSpacing();
	}

	public long getPrimaryTickToLeftOf(long tick) {
		return tick - tick % (getMajorTickSpacing() * 4);
	}

	// translates the tickOffset to ensure that leftTick and rightTick are
	// in view
	// returns the amount translated
	// TODO: to fix select region prob, also update numTicks if need be,
	// i.e. fit everything!
	public void updateView(long leftTick, long rightTick) {
		// if we are dragging out of view, scroll appropriately
		// if the right is out but the left is in, just scroll
		if (leftTick < tickOffset && rightTick < tickOffset + numTicks) {
			setTickOffset(leftTick);

			// if the left is out but the right is in, just scroll
		} else if (rightTick > tickOffset + numTicks && leftTick > tickOffset) {
			setTickOffset(rightTick - numTicks);

			// if both left and right are out of view,
		} else if (leftTick <= tickOffset && rightTick >= tickOffset + numTicks) {
			setTickOffset(leftTick);
			setNumTicks(rightTick - leftTick);
		}
	}
}
