package com.odang.beatbot.ui.view.control.param;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.ui.view.TouchableView;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.Dial;
import com.odang.beatbot.ui.view.control.ToggleDial;

public class DialParamControl extends LevelParamControl {
    private TouchableView levelControlView;

    public DialParamControl(View view) {
        super(view);
    }

    public DialParamControl withBeatSync(boolean beatSync) {
        levelControlView = beatSync ? new ToggleDial(this) : new Dial(this);
        if (beatSync) {
            levelControl = ((ToggleDial) levelControlView).getDial();
        } else {
            levelControl = (Dial) levelControlView;
        }

        levelControl.setListener(this);
        return this;
    }

    @Override
    public void setParam(Param param) {
        super.setParam(param);
        if (levelControlView instanceof ToggleDial) {
            final ToggleDial toggleDial = (ToggleDial) levelControlView;
            param.addToggleListener(toggleDial);
            toggleDial.onParamToggle(param);
        }
    }

    @Override
    public void layoutChildren() {
        label.layout(this, 0, 0, width, height / 5);

        final float dialDim = 3 * height / 5;
        levelControlView.layout(this, width / 2 - dialDim / 2, height / 5, dialDim, dialDim);
        valueLabel.layout(this, width / 2 - dialDim / 2, 4 * height / 5 - View.BG_OFFSET * 4, dialDim, height / 5);
    }
}
