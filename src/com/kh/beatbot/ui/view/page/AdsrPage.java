package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.AdsrView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.control.param.SeekbarParamControl;

public class AdsrPage extends Page implements OnReleaseListener {

	private ShapeGroup iconGroup = new ShapeGroup();
	private ToggleButton[] adsrButtons;
	private AdsrView adsrView;
	private SeekbarParamControl paramControl;

	@Override
	public void init() {
		super.init();
		updateParamView();
	}

	@Override
	public void update() {
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

	@Override
	protected void loadIcons() {
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i]
					.setBgIcon(new RoundedRectIcon(iconGroup,
							Colors.instrumentBgColorSet,
							Colors.buttonRowStrokeColorSet));
			if (i < adsrButtons.length - 2) {
				adsrButtons[i].setIcon(new Icon(whichAdsrIconResource(i)));
			}
		}
		adsrButtons[ADSR.START_ID].setText("S");
		adsrButtons[ADSR.PEAK_ID].setText("P");
	}

	private IconResource whichAdsrIconResource(int adsrParamId) {
		switch (adsrParamId) {
		case ADSR.ATTACK_ID:
			return IconResources.ATTACK;
		case ADSR.DECAY_ID:
			return IconResources.DECAY;
		case ADSR.SUSTAIN_ID:
			return IconResources.SUSTAIN;
		case ADSR.RELEASE_ID:
			return IconResources.RELEASE;
		default:
			return null;
		}
	}

	@Override
	public void draw() {
		iconGroup.draw(this, 1);
	}

	@Override
	protected void createChildren() {
		adsrView = new AdsrView();
		paramControl = new SeekbarParamControl();
		adsrButtons = new ToggleButton[ADSR.NUM_PARAMS];
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i] = new ToggleButton();
			adsrButtons[i].setId(i);
			adsrButtons[i].setOnReleaseListener(this);
		}
		addChild(adsrView);
		addChild(paramControl);
		for (ToggleButton adsrButton : adsrButtons) {
			addChild(adsrButton);
		}
	}

	@Override
	public void layoutChildren() {
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
