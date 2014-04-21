package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.icon.IconResourceSet;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.AdsrView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.control.param.SeekbarParamControl;

public class AdsrPage extends TouchableView implements OnReleaseListener {

	private ToggleButton[] adsrButtons;
	private AdsrView adsrView;
	private SeekbarParamControl paramControl;

	public synchronized void update() {
		adsrView.update();
		updateParamView();
	}

	private void updateParamView() {
		// update the displayed param label, value and checked button
		int paramId = TrackManager.currTrack.adsr.getCurrParamId();
		for (ToggleButton adsrButton : adsrButtons) {
			adsrButton.setChecked(adsrButton.getId() == paramId);
		}
		paramControl.setParam(TrackManager.currTrack.adsr.getCurrParam());
	}

	@Override
	public void onRelease(Button button) {
		int paramId = button.getId();
		// set the current parameter so we know what to do with SeekBar events.
		TrackManager.currTrack.adsr.setCurrParam(paramId);
		updateParamView();
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
	protected synchronized void createChildren() {
		adsrView = new AdsrView(renderGroup);
		paramControl = new SeekbarParamControl(renderGroup);
		adsrButtons = new ToggleButton[ADSR.NUM_PARAMS];
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i] = new ToggleButton(renderGroup, false);
			adsrButtons[i].setId(i);
			adsrButtons[i].setOnReleaseListener(this);
		}

		for (int i = 0; i < adsrButtons.length; i++) {
			if (i < adsrButtons.length - 2) {
				adsrButtons[i].setIcon(whichAdsrIconResource(i));
			} else {
				adsrButtons[i].setIcon(IconResourceSets.INSTRUMENT_BASE);
			}
		}

		adsrButtons[ADSR.START_ID].setText("S");
		adsrButtons[ADSR.PEAK_ID].setText("P");

		addChildren(adsrView, paramControl);
		addChildren(adsrButtons);
	}

	@Override
	public synchronized void layoutChildren() {
		float thirdHeight = height / 3;
		float pos = width - thirdHeight * (adsrButtons.length + 1);
		adsrView.layout(this, 0, 0, pos, height);
		paramControl.layout(this, pos, thirdHeight, width - pos, 2 * thirdHeight);
		pos += thirdHeight / 2;
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i].layout(this, pos, 0, thirdHeight, thirdHeight);
			pos += thirdHeight;
		}
	}
}
