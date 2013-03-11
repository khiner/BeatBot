package com.kh.beatbot.layout.page;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.global.BBButton;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.global.BBToggleButton;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.AdsrView;
import com.kh.beatbot.view.BBSeekbar;
import com.kh.beatbot.view.BBTextView;
import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.LevelViewBase;
import com.kh.beatbot.view.TouchableSurfaceView;

public class AdsrPage extends Page implements BBOnClickListener, LevelListener {

	private BBToggleButton[] adsrButtons;
	private AdsrView adsrView;
	private BBSeekbar levelBar;
	private BBTextView valueLabel, paramLabel;
	
	public AdsrPage(TouchableSurfaceView parent) {
		super(parent);
	}
	
	@Override
	public void init() {
		updateLabels();
	}

	@Override
	public void update() {
		adsrView.update();
		updateLevelBar();
		updateLabels();
	}
	
	private void check(BBToggleButton btn) {
		btn.setOn(true);
		for (BBToggleButton otherBtn : adsrButtons) {
			if (otherBtn != btn) {
				otherBtn.setOn(false);
			}
		}
	}
	
	public void updateLevelBar() {
		levelBar.setViewLevel(TrackManager.currTrack.adsr.getCurrParam().viewLevel);
	}
	
	public void updateLabels() {
		updateLabel();
		updateValueLabel();
	}
	
	private void updateLabel() {
		// update the displayed param name
		paramLabel.setText(TrackManager.currTrack.adsr.getCurrParam().name);		
	}
	
	private void updateValueLabel() {
		valueLabel.setText(TrackManager.currTrack.adsr.getCurrParam().getFormattedValueString());
	}
	
	@Override
	public void onClick(BBButton button) {
		check((BBToggleButton)button);
		int paramId = button.getId();
		// set the current parameter so we know what to do with SeekBar events.
		TrackManager.currTrack.adsr.setCurrParam(paramId);
		updateLabels();
		updateLevelBar();
	}

	@Override
	public void notifyInit(LevelViewBase levelListenable) {
		updateLevelBar();
	}

	@Override
	public void notifyPressed(LevelViewBase levelListenable, boolean pressed) {
		// nothing to do
	}

	@Override
	public void notifyClicked(LevelViewBase levelListenable) {
		// nothing to do
	}

	@Override
	public void setLevel(LevelViewBase levelListenable, float level) {
		TrackManager.currTrack.adsr.setCurrParamLevel(level);
		// update everything except level bar, since it is the notifier
		adsrView.update();
		updateLabels();
	}

	@Override
	public void setLevel(LevelViewBase levelListenable, float levelX,
			float levelY) {
		// for 2d view.  not applicable
	}
	
	/**
	 * Layout and Measure handled here since nested weights are needed,
	 * and they are very expensive.
	 * ___________________________________________
	 * |               |Label| A | D | S | R |PEAK_|
	 * |  ADSR         |_____|___|___|___|___|START|
	 * |  VIEW         | VAL |=========<>--------- |
	 * |_______________|_____|_____________________|
	 */

	@Override
	protected void loadIcons() {
		adsrButtons[0].setIconSource(new BBIconSource(-1, R.drawable.attack_icon, R.drawable.attack_icon_selected));
		adsrButtons[1].setIconSource(new BBIconSource(-1, R.drawable.decay_icon, R.drawable.decay_icon_selected));
		adsrButtons[2].setIconSource(new BBIconSource(-1, R.drawable.sustain_icon, R.drawable.sustain_icon_selected));
		adsrButtons[3].setIconSource(new BBIconSource(-1, R.drawable.release_icon, R.drawable.release_icon_selected));
		adsrButtons[4].setIconSource(new BBIconSource(-1, R.drawable.start_icon, R.drawable.start_icon_selected));
		adsrButtons[5].setIconSource(new BBIconSource(-1, R.drawable.peak_icon, R.drawable.peak_icon_selected));
	}

	@Override
	public void draw() {
		// parent view, no drawing of its own
	}

	@Override
	protected void createChildren() {
		adsrView = new AdsrView((TouchableSurfaceView)root);
		levelBar = new BBSeekbar((TouchableSurfaceView)root);
		levelBar.addLevelListener(this);
		paramLabel = new BBTextView((TouchableSurfaceView)root);
		valueLabel = new BBTextView((TouchableSurfaceView)root);
		adsrButtons = new BBToggleButton[6];
		adsrButtons[ADSR.ATTACK_ID] = new BBToggleButton((TouchableSurfaceView)root);
		adsrButtons[ADSR.DECAY_ID] = new BBToggleButton((TouchableSurfaceView)root);
		adsrButtons[ADSR.SUSTAIN_ID] = new BBToggleButton((TouchableSurfaceView)root);
		adsrButtons[ADSR.RELEASE_ID] = new BBToggleButton((TouchableSurfaceView)root);
		adsrButtons[ADSR.START_ID] = new BBToggleButton((TouchableSurfaceView)root);
		adsrButtons[ADSR.PEAK_ID] = new BBToggleButton((TouchableSurfaceView)root);
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i].setId(i);
			adsrButtons[i].setOnClickListener(this);
		}
		addChild(adsrView);
		addChild(levelBar);
		addChild(paramLabel);
		addChild(valueLabel);
		for (BBToggleButton adsrButton : adsrButtons)
			addChild(adsrButton);
	}

	public void drawAll() {
		draw();
		for (BBView child : children) {
			push();
			translate(child.x, child.y);
	 		child.drawAll();
			pop();
		}
	}
	
	@Override
	public void layoutChildren() {
		float thirdHeight = height / 3;
		float pos = width - thirdHeight * 6;
		float labelWidth = 6 * thirdHeight / 2;
		adsrView.layout(this, 0, 0, pos, height);
		paramLabel.layout(this, pos, thirdHeight, labelWidth, thirdHeight);
		valueLabel.layout(this, pos + labelWidth, thirdHeight, labelWidth, thirdHeight);
		levelBar.layout(this, pos, 2 * thirdHeight, 6 * thirdHeight, thirdHeight);
		pos = width - thirdHeight * 5;
		for (int i = 0; i < 4; i++) {
			adsrButtons[i].layout(this, pos, 0, thirdHeight, thirdHeight);
			pos += thirdHeight;
		}
		adsrButtons[4].layout(this, pos, thirdHeight / 2, thirdHeight, thirdHeight / 2);
		adsrButtons[5].layout(this, pos, 0, thirdHeight, thirdHeight / 2);
	}
}
