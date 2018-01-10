package com.odang.beatbot.ui.view.control;

import com.odang.beatbot.listener.OnLongPressListener;
import com.odang.beatbot.listener.OnPressListener;
import com.odang.beatbot.listener.OnReleaseListener;
import com.odang.beatbot.ui.icon.IconResourceSet;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.view.LongPressableView;
import com.odang.beatbot.ui.view.View;

public class Button extends LongPressableView {
    private OnPressListener pressListener;
    private OnReleaseListener releaseListener;
    private OnLongPressListener longPressListener;

    private boolean releaseOnDragExit = false;

    public Button(View view) {
        super(view);
        setShrinkable(true);
    }

    public Button(View view, RenderGroup renderGroup) {
        super(view, renderGroup);
        setShrinkable(true);
    }

    public Button withReleaseOnDragExit() {
        this.releaseOnDragExit = true;
        return this;
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
    public void dragRelease() {
        super.dragRelease();
        if (releaseOnDragExit) {
            notifyReleased();
        }
    }

    @Override
    public Button withIcon(IconResourceSet resourceSet) {
        return (Button) super.withIcon(resourceSet);
    }

    @Override
    public Button withRect() {
        return (Button) super.withRect();
    }

    @Override
    public Button withRoundedRect() {
        return (Button) super.withRoundedRect();
    }

    @Override
    public Button withCornerRadius(float cornerRadius) {
        return (Button) super.withCornerRadius(cornerRadius);
    }
}
