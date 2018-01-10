package com.odang.beatbot.ui.view.control;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.midi.util.GeneralUtils;
import com.odang.beatbot.ui.icon.IconResourceSet.State;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.view.View;

public class ValueLabel extends ControlView1dBase {
    private float anchorY = 0, anchorLevel;

    public ValueLabel(View view) {
        super(view);
    }

    public ValueLabel(View view, RenderGroup renderGroup) {
        super(view, renderGroup);
    }

    @Override
    protected float posToLevel(Pointer pos) {
        return GeneralUtils.clipToUnit(anchorLevel + (anchorY - pos.y) / (getTotalHeight() * 2));
    }

    public void onParamChange(Param param) {
        setText(param.getFormattedValue());
    }

    @Override
    public void createChildren() {
        setIcon(IconResourceSets.VALUE_LABEL);
        initRoundedRect();
    }

    @Override
    public void setParam(Param param) {
        super.setParam(param);
        if (param == null) {
            setState(State.DISABLED);
            setText("");
        } else {
            setState(State.DEFAULT);
        }
    }

    @Override
    public void handleActionDown(int id, Pointer pos) {
        if (getState() == State.DISABLED)
            return;
        anchorY = pos.y;
        anchorLevel = param.viewLevel;
        super.handleActionDown(id, pos);
    }

    public void handleActionUp(int id, Pointer pos) {
        if (getState() == State.DISABLED)
            return;
        super.handleActionUp(id, pos);
    }
}
