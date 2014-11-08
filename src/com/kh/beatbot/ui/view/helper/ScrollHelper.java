package com.kh.beatbot.ui.view.helper;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.util.GeneralUtils;
import com.kh.beatbot.ui.view.TouchableView.Pointer;

public class ScrollHelper {
	private static final float DAMP_CONSTANT = 0.9f;

	private Scrollable scrollable;

	public float numTicks = MidiManager.TICKS_PER_MEASURE, scrollXVelocity = 0,
			scrollYVelocity = 0, scrollAnchorX = 0, scrollAnchorY = 0, xOffset = 0, yOffset = 0;
	public int scrollPointerId = -1;

	public ScrollHelper(Scrollable scrollable) {
		this.scrollable = scrollable;
	}

	public void tick() {
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

	public boolean isScrolling() {
		return scrollPointerId != -1;
	}

	public void setScrollAnchor(int id, float tick) {
		setScrollAnchor(id, tick, scrollAnchorY);
	}

	public void setScrollAnchor(int id, float x, float y) {
		scrollPointerId = id;
		scrollAnchorX = x;
		scrollAnchorY = y;
	}

	public void handleActionUp() {
		scrollPointerId = -1;
	}

	public void pointerUp(int id) {
		if (id == scrollPointerId) {
			scrollPointerId = -1;
		}
	}

	public void scroll(Pointer pos) {
		float newXOffset = scrollAnchorX - numTicks * pos.x / scrollable.width();
		float newYOffset = scrollAnchorY - pos.y;
		scrollXVelocity = newXOffset - xOffset;
		scrollYVelocity = newYOffset - yOffset;
		setXOffset(newXOffset);
		setYOffset(newYOffset);
	}

	public void scroll() {
		if (!isScrolling()) {
			if (scrollXVelocity != 0) {
				tick();
				setXOffset(xOffset + scrollXVelocity);
			}
			if (scrollYVelocity != 0) {
				setYOffset(yOffset + scrollYVelocity);
			}
		}
	}

	public void zoom(float leftX, float rightX, float ZLAT, float ZRAT) {
		leftX = leftX == 0 ? 1 : leftX; // avoid divide by zero
		float newXOffset = (ZRAT * leftX - ZLAT * rightX) / (leftX - rightX);
		float newNumTicks = (ZLAT - newXOffset) * scrollable.width() / leftX;

		if (newXOffset < 0) {
			setXOffset(0);
			setNumTicks(ZRAT * scrollable.width() / rightX);
		} else if (newNumTicks > MidiManager.MAX_TICKS) {
			setXOffset(newXOffset);
			setNumTicks(MidiManager.MAX_TICKS - xOffset);
		} else if (newNumTicks < MidiManager.MIN_TICKS) {
			setXOffset(newXOffset);
			setNumTicks(MidiManager.MIN_TICKS);
		} else if (newXOffset + newNumTicks > MidiManager.MAX_TICKS) {
			setNumTicks(((ZLAT - MidiManager.MAX_TICKS) * scrollable.width())
					/ (leftX - scrollable.width()));
			setXOffset(MidiManager.MAX_TICKS - numTicks);
		} else {
			setXOffset(newXOffset);
			setNumTicks(newNumTicks);
		}
	}

	public void updateView(float tick) {
		// if we are dragging out of view, scroll appropriately
		if (tick < xOffset) {
			setXOffset(tick);
		} else if (tick > xOffset + numTicks) {
			setXOffset(tick - numTicks);
		}
	}

	public void updateView(float leftTick, float rightTick) {
		// if we are dragging out of view, scroll appropriately
		if (leftTick <= xOffset && rightTick >= xOffset + numTicks) {
			setXOffset(leftTick);
			setNumTicks(rightTick - leftTick);
		} else if (leftTick < xOffset) {
			setXOffset(leftTick);
		} else if (rightTick > xOffset + numTicks) {
			setXOffset(rightTick - numTicks);
		}
	}

	// translates the tickOffset to ensure that tick, topY and bottomY are all in view
	public void updateView(float tick, float topY, float bottomY) {
		updateView(tick);
		if (topY < yOffset && bottomY < yOffset + scrollable.unscaledHeight()) {
			setYOffset(topY);
		} else if (bottomY > yOffset + scrollable.unscaledHeight() && topY > yOffset) {
			setYOffset(bottomY - scrollable.unscaledHeight());
		}
	}

	public void setXOffset(float xOffset) {
		this.xOffset = GeneralUtils.clipTo(xOffset, 0, MidiManager.MAX_TICKS - numTicks);
		scrollable.onScrollX();
	}

	public void setYOffset(float yOffset) {
		this.yOffset = GeneralUtils.clipTo(yOffset, 0,
				scrollable.unscaledHeight() - scrollable.height());
		scrollable.onScrollY();
	}

	private void setNumTicks(float numTicks) {
		this.numTicks = GeneralUtils.clipTo(numTicks, MidiManager.MIN_TICKS, MidiManager.MAX_TICKS);
		scrollable.onScaleX();
	}
}
