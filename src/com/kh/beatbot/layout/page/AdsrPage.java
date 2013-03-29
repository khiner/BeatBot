package com.kh.beatbot.layout.page;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.listener.Level1dListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.AdsrView;
import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.Button;
import com.kh.beatbot.view.TextView;
import com.kh.beatbot.view.ToggleButton;
import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.control.ControlViewBase;
import com.kh.beatbot.view.control.Seekbar;

public class AdsrPage extends Page implements BBOnClickListener, Level1dListener {

	private ToggleButton[] adsrButtons;
	private AdsrView adsrView;
	private Seekbar levelBar;
	private TextView valueLabel, paramLabel;
	
	public AdsrPage(TouchableSurfaceView parent) {
		super(parent);
	}
	
	@Override
	public void init() {
		updateLevelBar();
		updateLabels();
	}

	@Override
	public void update() {
		adsrView.update();
		updateLevelBar();
		updateLabels();
	}
	
	private void check(ToggleButton btn) {
		btn.setChecked(true);
		for (ToggleButton otherBtn : adsrButtons) {
			if (otherBtn != btn) {
				otherBtn.setChecked(false);
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
	public void onClick(Button button) {
		check((ToggleButton)button);
		int paramId = button.getId();
		// set the current parameter so we know what to do with SeekBar events.
		TrackManager.currTrack.adsr.setCurrParam(paramId);
		updateLabels();
		updateLevelBar();
	}

	@Override
	public void onLevelChange(ControlViewBase levelListenable, float level) {
		TrackManager.currTrack.adsr.setCurrParamLevel(level);
		// update everything except level bar, since it is the notifier
		adsrView.update();
		updateLabels();
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
		adsrButtons[0].setIconSource(new BBIconSource(R.drawable.attack_icon, R.drawable.attack_icon_selected));
		adsrButtons[1].setIconSource(new BBIconSource(R.drawable.decay_icon, R.drawable.decay_icon_selected));
		adsrButtons[2].setIconSource(new BBIconSource(R.drawable.sustain_icon, R.drawable.sustain_icon_selected));
		adsrButtons[3].setIconSource(new BBIconSource(R.drawable.release_icon, R.drawable.release_icon_selected));
		adsrButtons[4].setIconSource(new BBIconSource(R.drawable.start_icon, R.drawable.start_icon_selected));
		adsrButtons[5].setIconSource(new BBIconSource(R.drawable.peak_icon, R.drawable.peak_icon_selected));
	}

	@Override
	public void draw() {
		// parent view, no drawing of its own
	}

	@Override
	protected void createChildren() {
		adsrView = new AdsrView((TouchableSurfaceView)root);
		levelBar = new Seekbar((TouchableSurfaceView)root);
		levelBar.addLevelListener(this);
		paramLabel = new TextView((TouchableSurfaceView)root);
		valueLabel = new TextView((TouchableSurfaceView)root);
		adsrButtons = new ToggleButton[6];
		adsrButtons[ADSR.ATTACK_ID] = new ToggleButton((TouchableSurfaceView)root);
		adsrButtons[ADSR.DECAY_ID] = new ToggleButton((TouchableSurfaceView)root);
		adsrButtons[ADSR.SUSTAIN_ID] = new ToggleButton((TouchableSurfaceView)root);
		adsrButtons[ADSR.RELEASE_ID] = new ToggleButton((TouchableSurfaceView)root);
		adsrButtons[ADSR.START_ID] = new ToggleButton((TouchableSurfaceView)root);
		adsrButtons[ADSR.PEAK_ID] = new ToggleButton((TouchableSurfaceView)root);
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i].setId(i);
			adsrButtons[i].setOnClickListener(this);
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
