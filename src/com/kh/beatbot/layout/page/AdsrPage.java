package com.kh.beatbot.layout.page;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.global.RoundedRectIconSource;
import com.kh.beatbot.listener.Level1dListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.AdsrView;
import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.ControlViewBase;
import com.kh.beatbot.view.control.Seekbar;
import com.kh.beatbot.view.control.ToggleButton;
import com.kh.beatbot.view.mesh.ShapeGroup;

public class AdsrPage extends Page implements OnReleaseListener, Level1dListener {

	private ToggleButton[] adsrButtons;
	private AdsrView adsrView;
	private Seekbar levelBar;
	private ToggleButton valueLabel, paramLabel;
	private static ShapeGroup iconGroup = new ShapeGroup();
	
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
		paramLabel.setText(TrackManager.currTrack.adsr.getCurrParam().getName());		
	}
	
	private void updateValueLabel() {
		valueLabel.setText(TrackManager.currTrack.adsr.getCurrParam().getFormattedValueString());
	}
	
	@Override
	public void onRelease(Button button) {
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
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i].setBgIconSource(new RoundedRectIconSource(iconGroup, Colors.instrumentBgColorSet, Colors.instrumentStrokeColorSet));
			if (i < adsrButtons.length - 2) {
				adsrButtons[i].setIconSource(new ImageIconSource(whichAdsrIcon(i)));
			}
		}
		adsrButtons[ADSR.START_ID].setText("S");
		adsrButtons[ADSR.PEAK_ID].setText("P");
	}

	private int whichAdsrIcon(int adsrParamId) {
		switch (adsrParamId) {
		case ADSR.ATTACK_ID: return R.drawable.attack_icon;
		case ADSR.DECAY_ID: return R.drawable.decay_icon;
		case ADSR.SUSTAIN_ID: return R.drawable.sustain_icon;
		case ADSR.RELEASE_ID: return R.drawable.release_icon;
		default: return -1;
		}
	}
	@Override
	public void draw() {
		// draw all icon background rects in one call
		push();
		translate(-absoluteX, -absoluteY);
		iconGroup.draw((GL11)BBView.gl, 1);
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
		float pos = width - thirdHeight * adsrButtons.length;
		float labelWidth = adsrButtons.length * thirdHeight / 2;
		adsrView.layout(this, 0, 0, pos, height);
		paramLabel.layout(this, pos, thirdHeight, labelWidth, thirdHeight);
		valueLabel.layout(this, pos + labelWidth, thirdHeight, labelWidth, thirdHeight);
		levelBar.layout(this, pos, thirdHeight * 2, labelWidth * 2, thirdHeight);
		pos = width - thirdHeight * adsrButtons.length;
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i].layout(this, pos, 0, thirdHeight, thirdHeight);
			pos += thirdHeight;
		}
	}
}
