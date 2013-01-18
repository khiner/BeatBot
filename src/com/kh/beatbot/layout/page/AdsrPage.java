package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.AdsrView;
import com.kh.beatbot.view.BBSeekbar;
import com.kh.beatbot.view.BBTextView;

public class AdsrPage extends Page implements OnClickListener, LevelListener {
	
	private ToggleButton[] adsrButtons = new ToggleButton[6];
	private AdsrView adsrView = null;
	private BBSeekbar levelBar = null;
	private BBTextView valueLabel = null, paramLabel = null;
	
	public AdsrPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void init() {
		adsrView = (AdsrView)findViewById(R.id.adsrView);
		levelBar = (BBSeekbar)findViewById(R.id.adsrBar);
		levelBar.addLevelListener(this);
		paramLabel = (BBTextView)findViewById(R.id.paramLabel);
		valueLabel = (BBTextView)findViewById(R.id.valueLabel);
		adsrButtons[ADSR.ATTACK_ID] = (ToggleButton)findViewById(R.id.attackButton);
		adsrButtons[ADSR.DECAY_ID] = (ToggleButton)findViewById(R.id.decayButton);
		adsrButtons[ADSR.SUSTAIN_ID] = (ToggleButton)findViewById(R.id.sustainButton);
		adsrButtons[ADSR.RELEASE_ID] = (ToggleButton)findViewById(R.id.releaseButton);
		adsrButtons[ADSR.START_ID] = (ToggleButton)findViewById(R.id.startButton);
		adsrButtons[ADSR.PEAK_ID] = (ToggleButton)findViewById(R.id.peakButton);
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i].setTag(i);
			adsrButtons[i].setOnClickListener(this);
		}
		updateLabels();
	}

	@Override
	public void update() {
		adsrView.update();
		updateLevelBar();
		updateLabels();
	}
	
	@Override
	public void setVisibilityCode(int code) {
		adsrView.setVisibility(code);
		levelBar.setVisibility(code);
		valueLabel.setVisibility(code);
		paramLabel.setVisibility(code);
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
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		int width = (r - l);
		int height = (b - t);
		int halfHeight = height / 2;
		adsrView.layout(0, 0, width - 9 * halfHeight, b);
		int pos = width - 9 * halfHeight;
		paramLabel.layout(pos, 0, pos + 2 * height, halfHeight);
		valueLabel.layout(pos, halfHeight, pos + 2 * height, b);
		pos += 2 * height;
		levelBar.layout(pos, halfHeight, width, b);
		for (int i = 0; i < 4; i++) { // adsrBtns
			adsrButtons[i].layout(pos, 0, pos + halfHeight, halfHeight);
			pos += halfHeight;
		}
		//start / peak btns
		adsrButtons[4].layout(pos, halfHeight / 2, pos + halfHeight, halfHeight);
		adsrButtons[5].layout(pos, 0, pos + halfHeight, halfHeight / 2);
	}
	
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		int width = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
		int height = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
		int halfHeight = height / 2;
		adsrView.measure(width / 2, height);
		for (int i = 0; i < 4; i++) { // adsrBtns
			adsrButtons[i].measure(halfHeight, halfHeight);
		}
		for (int i = 4; i < adsrButtons.length; i++) {
			adsrButtons[i].measure(halfHeight, height / 4);
		}
		paramLabel.measure(2 * height, halfHeight);
		valueLabel.measure(2 * height, halfHeight);
		levelBar.measure(3 * halfHeight, halfHeight);
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
	public void onClick(View v) {
		check((ToggleButton)v);
		int paramId = (Integer)v.getTag();
		// set the current parameter so we know what to do with SeekBar events.
		TrackManager.currTrack.adsr.setCurrParam(paramId);
		updateLabels();
		updateLevelBar();
	}

	@Override
	public void notifyInit(LevelListenable levelListenable) {
		updateLevelBar();
	}

	@Override
	public void notifyPressed(LevelListenable levelListenable, boolean pressed) {
		// nothing to do
	}

	@Override
	public void notifyClicked(LevelListenable levelListenable) {
		// nothing to do
	}

	@Override
	public void setLevel(LevelListenable levelListenable, float level) {
		TrackManager.currTrack.adsr.setCurrParamLevel(level);
		// update everything except level bar, since it is the notifier
		adsrView.update();
		updateLabels();
	}

	@Override
	public void setLevel(LevelListenable levelListenable, float levelX,
			float levelY) {
		// for 2d view.  not applicable
	}
}
