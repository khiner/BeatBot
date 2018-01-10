package com.odang.beatbot.ui.view.control.param;

import com.odang.beatbot.ui.icon.IconResourceSet;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.Seekbar;

public class SeekbarParamControl extends LevelParamControl {
    public enum SeekbarPosition {
        BOTTOM, CENTER
    }

    ;

    private final SeekbarPosition seekbarPosition;

    public SeekbarParamControl(View view) {
        this(view, SeekbarPosition.BOTTOM, Seekbar.BasePosition.LEFT);
    }

    public SeekbarParamControl(View view, SeekbarPosition seekbarPosition,
                               Seekbar.BasePosition basePosition) {
        super(view);
        levelControl = new Seekbar(this, basePosition);
        levelControl.setListener(this);
        this.seekbarPosition = seekbarPosition;
    }

    public SeekbarParamControl withLabelIcon(IconResourceSet icon) {
        return (SeekbarParamControl) super.withLabelIcon(icon);
    }

    @Override
    public void layoutChildren() {
        switch (seekbarPosition) {
            case BOTTOM:
                layoutWithBottomSeekbar();
                return;
            case CENTER:
                layoutWithCenterSeekbar();
                return;
        }
    }

    private void layoutWithBottomSeekbar() {
        label.layout(this, 0, 0, width / 2, height / 2);
        valueLabel.layout(this, width / 2, 0, width / 2, height / 2);
        levelControl.layout(this, 0, height / 2, width, height / 2);
    }

    private void layoutWithCenterSeekbar() {
        label.layout(this, 0, 0, height * 2, height);
        levelControl.layout(this, height * 2, 0, width - height * 4, height);
        valueLabel.layout(this, width - height * 2, 0, height * 2, height);
    }
}
