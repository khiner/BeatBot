package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.listener.OnLongPressListener;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.icon.IconResourceSet;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.LongPressableView;
import com.kh.beatbot.ui.view.View;

public class Button extends LongPressableView {
	private OnPressListener pressListener;
	private OnReleaseListener releaseListener;
	private OnLongPressListener longPressListener;

	public Button(View view) {
		super(view);
		setShrinkable(true);
	}

	public Button(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
		setShrinkable(true);
	}

	public final OnPressListener getOnPressListener() {
		return pressListener;
	}

	public final OnReleaseListener getOnReleaseListener() {
		return releaseListener;
	}

	public final void setOnPressListener(OnPressListener pressListener) {
		this.pressListener = pressListener;
	}

	public final void setOnReleaseListener(OnReleaseListener releaseListener) {
		this.releaseListener = releaseListener;
	}

	public final void setOnLongPressListener(OnLongPressListener longPressListener) {
		this.longPressListener = longPressListener;
	}

	@Override
	public void press() {
		super.press();
		notifyPressed(); // always notify press events
	}

	@Override
	public void release() {
		releaseLongPress();
		super.release();
	}

	/*
	 * Trigger a touch event (calls the onReleaseListener())
	 */
	public void trigger() {
		release();
		notifyReleased();
	}

	protected void notifyPressed() {
		if (null != pressListener) {
			pressListener.onPress(this);
		}
	}

	protected void notifyReleased() {
		if (null != releaseListener) {
			releaseListener.onRelease(this);
		}
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		if (!isEnabled())
			return;

		super.handleActionDown(id, pos);
		press();
	}

	@Override
	public void handleActionUp(int id, Pointer pos) {
		if (!isEnabled())
			return;
		if (isPressed() && (isLongPressing() || longPressListener == null)) {
			// only release if long press hasn't happened yet
			notifyReleased();
		}
		super.handleActionUp(id, pos);
	}

	@Override
	public void handleActionMove(int id, Pointer pos) {
		if (!isEnabled())
			return;
		super.handleActionMove(id, pos);
		checkPointerExit(id, pos);
	}

	@Override
	protected void longPress(int id, Pointer pos) {
		if (null != longPressListener) {
			longPressListener.onLongPress(this);
		}
	}
	
	@Override
	public synchronized Button withIcon(IconResourceSet resourceSet) {
		return (Button) super.withIcon(resourceSet);
	}

	@Override
	public synchronized Button withRect() {
		return (Button) super.withRect();
	}
	
	@Override
	public synchronized Button withRoundedRect() {
		return (Button) super.withRoundedRect();
	}
}
