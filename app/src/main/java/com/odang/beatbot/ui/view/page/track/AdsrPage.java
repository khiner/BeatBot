package com.odang.beatbot.ui.view.page.track;

import com.odang.beatbot.effect.ADSR;
import com.odang.beatbot.effect.Param;
import com.odang.beatbot.event.effect.EffectParamsChangeEvent;
import com.odang.beatbot.listener.MultiViewTouchTracker;
import com.odang.beatbot.listener.OnReleaseListener;
import com.odang.beatbot.listener.ParamListener;
import com.odang.beatbot.listener.TouchableViewsListener;
import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.icon.IconResourceSet;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.AdsrView;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.Button;
import com.odang.beatbot.ui.view.control.ToggleButton;
import com.odang.beatbot.ui.view.control.param.SeekbarParamControl;

public class AdsrPage extends TrackPage implements OnReleaseListener, ParamListener, TouchableViewsListener {
    private ToggleButton[] adsrButtons;
    private AdsrView adsrView;
    private SeekbarParamControl paramControl;
    private int currParamId = 1;

    public AdsrPage(View view) {
        super(view);
    }

    @Override
    public void onSelect(BaseTrack baseTrack) {
        Track track = (Track) baseTrack;
        adsrView.onSelect(track);
        for (int i = 0; i < ADSR.NUM_PARAMS; i++) {
            track.getAdsrParam(i).removeListener(this);
            track.getAdsrParam(i).addListener(this);
        }
        setParam(track.getActiveAdsrParam().id);
    }

    private void updateParamView() {
        // update the displayed param label, value and checked button
        for (ToggleButton adsrButton : adsrButtons) {
            adsrButton.setChecked(adsrButton.getId() == currParamId);
        }
        Track track = (Track) context.getTrackManager().getCurrTrack();
        paramControl.setParam(track.getActiveAdsrParam());
    }

    @Override
    public void onRelease(Button button) {
        setParam(button.getId());
    }

    private IconResourceSet whichAdsrIconResource(int adsrParamId) {
        switch (adsrParamId) {
            case ADSR.ATTACK_ID:
                return IconResourceSets.ATTACK;
            case ADSR.DECAY_ID:
                return IconResourceSets.DECAY;
            case ADSR.SUSTAIN_ID:
                return IconResourceSets.SUSTAIN;
            case ADSR.RELEASE_ID:
                return IconResourceSets.RELEASE;
            default:
                return null;
        }
    }

    @Override
    protected void createChildren() {
        adsrView = new AdsrView(this);
        paramControl = new SeekbarParamControl(this);
        adsrButtons = new ToggleButton[ADSR.NUM_PARAMS];
        for (int i = 0; i < adsrButtons.length; i++) {
            adsrButtons[i] = new ToggleButton(this).withRoundedRect().withIcon(
                    IconResourceSets.INSTRUMENT_BASE);
            adsrButtons[i].setResourceId(whichAdsrIconResource(i));
            adsrButtons[i].setId(i);
            adsrButtons[i].setOnReleaseListener(this);
        }

        adsrButtons[ADSR.START_ID].setText("S");
        adsrButtons[ADSR.PEAK_ID].setText("P");

        new MultiViewTouchTracker(this).monitorViews(adsrView, paramControl);
    }

    @Override
    public void layoutChildren() {
        float thirdHeight = height / 3;
        float pos = width - thirdHeight * (adsrButtons.length + 1);
        adsrView.layout(this, BG_OFFSET, BG_OFFSET, pos - BG_OFFSET * 2, height - BG_OFFSET * 2);

        pos += thirdHeight / 2;
        paramControl.layout(this, pos, thirdHeight, adsrButtons.length * thirdHeight, 2 * thirdHeight);
        for (int i = 0; i < adsrButtons.length; i++) {
            adsrButtons[i].layout(this, pos, 0, thirdHeight, thirdHeight);
            pos += thirdHeight;
        }
    }

    @Override
    public void onParamChange(Param param) {
        if (param.id != currParamId && param.id != ADSR.SUSTAIN_ID && param.id != ADSR.PEAK_ID) {
            // sustain & peak are both controlled by the same 'dots' as attack and decay
            // to avoid switching back and forth a ton, we only switch on attack and decay changes
            setParam(param.id);
        }
    }

    private void setParam(int paramId) {
        currParamId = paramId;
        // set the current parameter so we know what to do with SeekBar events.
        final Track track = (Track) context.getTrackManager().getCurrTrack();
        track.setActiveAdsrParam(currParamId);
        updateParamView();
    }

    private EffectParamsChangeEvent effectParamsChangeEvent;

    @Override
    public void onFirstPress() {
        final ADSR adsr = ((Track) context.getTrackManager().getCurrTrack()).getAdsr();
        effectParamsChangeEvent = new EffectParamsChangeEvent(adsr.getTrackId(),
                adsr.getPosition());
        effectParamsChangeEvent.begin();
    }

    @Override
    public void onLastRelease() {
        effectParamsChangeEvent.end();
    }
}
