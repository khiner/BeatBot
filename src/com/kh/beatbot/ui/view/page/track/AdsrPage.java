package com.kh.beatbot.ui.view.page.track;

import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.icon.IconResourceSet;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.AdsrView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.control.param.SeekbarParamControl;

public class AdsrPage extends TrackPage implements OnReleaseListener, ParamListener {
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
		updateParamView();
	}

	private void updateParamView() {
		// update the displayed param label, value and checked button
		for (ToggleButton adsrButton : adsrButtons) {
			adsrButton.setChecked(adsrButton.getId() == currParamId);
		}
		Track track = (Track) TrackManager.getCurrTrack();
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
			return IconResourceSets.INSTRUMENT_BASE;
		}
	}

	@Override
	protected synchronized void createChildren() {
		adsrView = new AdsrView(this);
		paramControl = new SeekbarParamControl(this);
		adsrButtons = new ToggleButton[ADSR.NUM_PARAMS];
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i] = new ToggleButton(this).withRoundedRect().withIcon(
					whichAdsrIconResource(i));
			adsrButtons[i].setId(i);
			adsrButtons[i].setOnReleaseListener(this);
		}

		adsrButtons[ADSR.START_ID].setText("S");
		adsrButtons[ADSR.PEAK_ID].setText("P");
	}

	@Override
	public synchronized void layoutChildren() {
		float thirdHeight = height / 3;
		float pos = width - thirdHeight * (adsrButtons.length + 1);
		adsrView.layout(this, BG_OFFSET, BG_OFFSET, pos - BG_OFFSET * 2, height - BG_OFFSET * 2);
		paramControl.layout(this, pos, thirdHeight, width - pos, 2 * thirdHeight);
		pos += thirdHeight / 2;
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i].layout(this, pos, 0, thirdHeight, thirdHeight);
			pos += thirdHeight;
		}
	}

	@Override
	public void onParamChanged(Param param) {
		if (param.id != currParamId && param.id != ADSR.SUSTAIN_ID && param.id != ADSR.PEAK_ID) {
			// sustain & peak are both controlled by the same 'dots' as attack and decay
			// to avoid switching back and forth a ton, we only switch on attack and decay changes
			setParam(param.id);
		}
	}

	private void setParam(int paramId) {
		currParamId = paramId;
		// set the current parameter so we know what to do with SeekBar events.
		Track track = (Track) TrackManager.getCurrTrack();
		track.setActiveAdsrParam(currParamId);
		updateParamView();
	}
}
