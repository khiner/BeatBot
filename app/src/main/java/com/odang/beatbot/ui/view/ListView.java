package com.odang.beatbot.ui.view;

import com.odang.beatbot.listener.OnPressListener;
import com.odang.beatbot.ui.view.control.Button;
import com.odang.beatbot.ui.view.control.ToggleButton;

public class ListView extends ScrollableView implements OnPressListener {
    private Button selectedButton;

    public ListView(View view) {
        super(view);
        setScrollable(false, true);
    }

    @Override
    public void layoutChildren() {
        float labelHeight = getLabelHeight();
        float y = yOffset;
        for (View child : children) {
            float height = child.getText().isEmpty() ? width - 2 * labelHeight
                    / 3 : labelHeight;
            child.layout(this, labelHeight / 3, y, width - 2 * labelHeight / 3, height);
            y += height;
        }
        super.layoutChildren();
    }

    @Override
    public void handleActionUp(int index, Pointer pos) {
        super.handleActionUp(index, pos);
        shouldPropagateMoveEvents = true;
    }

    @Override
    public void handleActionMove(int index, Pointer pos) {
        super.handleActionMove(index, pos);
        if (null != selectedButton
                && selectedButton.isPressed()
                && !(selectedButton instanceof ToggleButton && ((ToggleButton) selectedButton)
                .isChecked()) && Math.abs(pos.y - pos.downY) > getLabelHeight() / 2) {
            // scrolling, release the pressed button
            selectedButton.release();
            shouldPropagateMoveEvents = false;
        }
    }

    @Override
    public void onPress(Button button) {
        selectedButton = button;
    }
}