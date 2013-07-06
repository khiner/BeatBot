package com.kh.beatbot.ui.view.page;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.listener.Level1dListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.AdsrView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ControlViewBase;
import com.kh.beatbot.ui.view.control.Seekbar;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class AdsrPage extends Page implements OnReleaseListener,
		Level1dListener {

	private static ShapeGroup iconGroup = new ShapeGroup();

	private ToggleButton[] adsrButtons;
	private AdsrView adsrView;
	private Seekbar levelBar;
	private ToggleButton valueLabel, paramLabel;

	@Override
	public void init() {
		updateLevelBar();
		updateParamView();
	}

	@Override
	public void update() {
		adsrView.update();
		updateLevelBar();
		updateParamView();
	}

	public void updateLevelBar() {
		levelBar.setViewLevel(TrackManager.currTrack.adsr.getCurrParam().viewLevel);
	}

	public void updateParamView() {
		// update the displayed param label, value and checked button
		int paramId = TrackManager.currTrack.adsr.getCurrParamId();
		for (ToggleButton adsrButton : adsrButtons) {
			adsrButton.setChecked(adsrButton.getId() == paramId);
		}
		updateLabel();
		updateValueLabel();
	}

	private void updateLabel() {
		paramLabel.setText(TrackManager.currTrack.adsr.getCurrParam().getName());
	}

	private void updateValueLabel() {
		valueLabel.setText(TrackManager.currTrack.adsr.getCurrParam()
				.getFormattedValueString());
	}

	@Override
	public void onRelease(Button button) {
		int paramId = button.getId();
		// set the current parameter so we know what to do with SeekBar events.
		TrackManager.currTrack.adsr.setCurrParam(paramId);
		updateLevelBar();
		updateParamView();
	}

	@Override
	public void onLevelChange(ControlViewBase levelListenable, float level) {
		TrackManager.currTrack.adsr.setCurrParamLevel(level);
		adsrView.update();
		updateValueLabel();
	}

	/**
	 * Layout and Measure handled here since nested weights are needed, and they
	 * are very expensive. ___________________________________________ | |Label|
	 * A | D | S | R |PEAK_| | ADSR |_____|___|___|___|___|START| | VIEW | VAL
	 * |=========<>--------- | |_______________|_____|_____________________|
	 */

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
		// draw all icon background rects in one call
		push();
		translate(-absoluteX, -absoluteY);
		iconGroup.draw((GL11) View.gl, 1);
		pop();
	}

	@Override
	protected void createChildren() {
		adsrView = new AdsrView();
		levelBar = new Seekbar();
		levelBar.addLevelListener(this);
		paramLabel = new ToggleButton();
		valueLabel = new ToggleButton();
		adsrButtons = new ToggleButton[ADSR.NUM_PARAMS];
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i] = new ToggleButton();
			adsrButtons[i].setId(i);
			adsrButtons[i].setOnReleaseListener(this);
		}
		addChild(adsrView);
		addChild(levelBar);
		addChild(paramLabel);
		addChild(valueLabel);
		for (ToggleButton adsrButton : adsrButtons)
			addChild(adsrButton);
	}

	public void drawAll() {
		draw();
		for (View child : children) {
			push();
			translate(child.x, child.y);
			child.drawAll();
			pop();
		}
	}

	@Override
	public void layoutChildren() {
		float thirdHeight = height / 3;
		float pos = width - thirdHeight * adsrButtons.length;
		float labelWidth = adsrButtons.length * thirdHeight / 2;
		adsrView.layout(this, 0, 0, pos, height);
		paramLabel.layout(this, pos, thirdHeight, labelWidth, thirdHeight);
		valueLabel.layout(this, pos + labelWidth, thirdHeight, labelWidth,
				thirdHeight);
		levelBar.layout(this, pos, thirdHeight * 2, labelWidth * 2, thirdHeight);
		pos = width - thirdHeight * adsrButtons.length;
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i].layout(this, pos, 0, thirdHeight, thirdHeight);
			pos += thirdHeight;
		}
	}
}
