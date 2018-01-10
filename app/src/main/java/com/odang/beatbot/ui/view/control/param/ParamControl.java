package com.odang.beatbot.ui.view.control.param;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.listener.TouchableViewListener;
import com.odang.beatbot.ui.icon.IconResourceSet;
import com.odang.beatbot.ui.icon.IconResourceSet.State;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.TouchableView;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.ValueLabel;

public class ParamControl extends TouchableView implements TouchableViewListener {
    protected View label;
    protected ValueLabel valueLabel;
    protected TouchableViewListener touchListener;

    public ParamControl(View view) {
        super(view);
    }

    @Override
    public void createChildren() {
        valueLabel = new ValueLabel(this);
        valueLabel.setShrinkable(true);
        valueLabel.setListener(this);
        label = new View(this).withRoundedRect().withIcon(IconResourceSets.CONTROL_LABEL);
        label.setShrinkable(true);
    }

    public void setTouchListener(TouchableViewListener listener) {
        touchListener = listener;
    }

    public ParamControl withLabelIcon(IconResourceSet icon) {
        label.setIcon(icon);
        return this;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        valueLabel.setId(id);
    }

    public void setParam(Param param) {
        if (param == null) {
            setState(State.DISABLED);
        }
        valueLabel.setParam(param);
        label.setText(param == null ? "" : param.getName());
    }

    public void setLabelText(String text) {
        label.setText(text);
    }

    @Override
    public void layoutChildren() {
        label.layout(this, 0, 0, width / 2, height);
        valueLabel.layout(this, width / 2, 0, width / 2, height);
    }

    public float getLevel() {
        return valueLabel.getLevel();
    }

    @Override
    public void onPress(TouchableView view) {
        if (!isEnabled())
            return;
        label.press();
        if (null != touchListener) {
            touchListener.onPress(view);
        }
    }

    @Override
    public void onRelease(TouchableView view) {
        if (!isEnabled())
            return;
        label.release();
        if (null != touchListener) {
            touchListener.onRelease(view);
        }
    }
}
