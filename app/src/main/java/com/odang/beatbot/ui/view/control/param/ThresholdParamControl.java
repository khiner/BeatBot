package com.odang.beatbot.ui.view.control.param;

import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.ThresholdBar;

public class ThresholdParamControl extends LevelParamControl {
    public ThresholdParamControl(View view) {
        super(view);
        levelControl = new ThresholdBar(this);
        levelControl.setListener(this);
    }

    @Override
    public void layoutChildren() {
        label.layout(this, 0, 0, height * 3, height);
        levelControl.layout(this, height * 3, 0, width - height * 5, height);
        valueLabel.layout(this, width - height * 2, 0, height * 2, height);
    }

    public void setLevel(float level) {
        ((ThresholdBar) levelControl).setLevelNormalized(level);
    }

    public void resetLevel() {
        ((ThresholdBar) levelControl).resetLevel();
    }
}
