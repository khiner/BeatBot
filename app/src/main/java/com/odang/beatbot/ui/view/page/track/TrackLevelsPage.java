package com.odang.beatbot.ui.view.page.track;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.event.track.TrackLevelsSetEvent;
import com.odang.beatbot.listener.MultiViewTouchTracker;
import com.odang.beatbot.listener.TouchableViewsListener;
import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.ui.color.Color;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.TouchableView;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.Seekbar.BasePosition;
import com.odang.beatbot.ui.view.control.ValueLabel;
import com.odang.beatbot.ui.view.control.param.SeekbarParamControl;
import com.odang.beatbot.ui.view.control.param.SeekbarParamControl.SeekbarPosition;

public class TrackLevelsPage extends TrackPage implements TouchableViewsListener {
    protected SeekbarParamControl volumeParamControl, panParamControl;
    protected PitchParamControl pitchParamControl;
    protected boolean masterMode = false;

    public TrackLevelsPage(View view) {
        super(view);
    }

    @Override
    public void onSelect(BaseTrack track) {
        volumeParamControl.setParam(track.getVolumeParam());
        panParamControl.setParam(track.getPanParam());
        pitchParamControl.setParams(track.getPitchParam(), track.getPitchCentParam());
    }

    public void setMasterMode(boolean masterMode) {
        this.masterMode = masterMode;
    }

    @Override
    protected void createChildren() {
        volumeParamControl = new SeekbarParamControl(this, SeekbarPosition.CENTER,
                BasePosition.LEFT).withLabelIcon(IconResourceSets.VOLUME);
        panParamControl = new SeekbarParamControl(this, SeekbarPosition.CENTER, BasePosition.CENTER)
                .withLabelIcon(IconResourceSets.PAN);
        pitchParamControl = new PitchParamControl(this);

        volumeParamControl.setLevelColor(Color.TRON_BLUE, Color.TRON_BLUE_TRANS);
        panParamControl.setLevelColor(Color.PAN, Color.PAN_TRANS);
        pitchParamControl.setLevelColor(Color.PITCH, Color.PITCH_TRANS);

        new MultiViewTouchTracker(this).monitorViews(volumeParamControl, panParamControl,
                pitchParamControl);
    }

    private TrackLevelsSetEvent levelsSetEvent = null;

    @Override
    public void onFirstPress() {
        levelsSetEvent = new TrackLevelsSetEvent(context.getTrackManager().getCurrTrack().getId());
        levelsSetEvent.begin();
    }

    @Override
    public void onLastRelease() {
        levelsSetEvent.end();
    }

    @Override
    public void layoutChildren() {
        float toggleHeight = height / 3;
        volumeParamControl.layout(this, 0, 0, width, toggleHeight);
        panParamControl.layout(this, 0, toggleHeight, width, toggleHeight);
        pitchParamControl.layout(this, 0, toggleHeight * 2, width, toggleHeight);
    }

    // Pitch has two params/value-labels that can be switched between (steps/cents)
    // The last value-label to be touched is what the seekbar controls
    private class PitchParamControl extends SeekbarParamControl {
        private ValueLabel centValueLabel, currentValueLabel;

        public PitchParamControl(View view) {
            super(view, SeekbarPosition.CENTER, BasePosition.CENTER);
        }

        @Override
        public void createChildren() {
            super.createChildren();
            centValueLabel = new ValueLabel(this);
            centValueLabel.setShrinkable(true);
            centValueLabel.setListener(this);
            label.setIcon(IconResourceSets.PITCH);
            currentValueLabel = valueLabel;
        }

        @Override
        public void setId(int id) {
            super.setId(id);
            centValueLabel.setId(id);
        }

        @Override
        public void onPress(TouchableView view) {
            label.press();
            if (view instanceof ValueLabel && !currentValueLabel.equals(view)) {
                currentValueLabel = (ValueLabel) view;
                levelControl.setParam(currentValueLabel.getParam());
            }
            currentValueLabel.press();
            levelControl.press();
            if (null != touchListener) {
                touchListener.onPress(view);
            }
        }

        @Override
        public void onRelease(TouchableView view) {
            label.release();
            currentValueLabel.release();
            levelControl.release();
            if (null != touchListener) {
                touchListener.onRelease(view);
            }
        }

        @Override
        public void layoutChildren() {
            label.layout(this, 0, 0, height * 2, height);
            levelControl.layout(this, height * 2, 0, width - height * 6, height);
            valueLabel.layout(this, width - height * 4, 0, height * 2, height);
            centValueLabel.layout(this, width - height * 2, 0, height * 2, height);
        }

        public void setParams(Param stepParam, Param centParam) {
            label.setText(stepParam.getName());
            valueLabel.setParam(stepParam);
            centValueLabel.setParam(centParam);
            levelControl.setParam(currentValueLabel.getParam());
        }
    }
}
