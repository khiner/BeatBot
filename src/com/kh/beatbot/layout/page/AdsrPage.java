package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.view.AdsrView;
import com.kh.beatbot.view.BBSeekbar;
import com.kh.beatbot.view.BBTextView;

public class AdsrPage extends Page implements OnClickListener {
	protected Param[] params = new Param[6];
	
	private final int ATTACK_ID = 0;
	private final int DECAY_ID = 1;
	private final int SUSTAIN_ID = 2;
	private final int RELEASE_ID = 3;
	private final int START_ID = 4;
	private final int PEAK_ID = 5;

	private int currParamId = ATTACK_ID;
	
	private ToggleButton[] adsrButtons = new ToggleButton[6];
	private AdsrView adsrView = null;
	private BBSeekbar adsrBar = null;
	private BBTextView valueLabel = null, paramLabel = null;
	
	public AdsrPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void init() {
		params[ATTACK_ID] = new Param("ATTACK", true, false, "ms");
		params[DECAY_ID] = new Param("DECAY", true, false, "ms");
		params[SUSTAIN_ID] = new Param("SUSTAIN", false, false, "");
		params[RELEASE_ID] = new Param("RELEASE", true, false, "ms");
		params[START_ID] = new Param("START", false, false, "");
		params[PEAK_ID] = new Param("PEAK", false, false, "");
		adsrView = (AdsrView)findViewById(R.id.adsrView);
		adsrBar = (BBSeekbar)findViewById(R.id.adsrBar);
		paramLabel = (BBTextView)findViewById(R.id.paramLabel);
		valueLabel = (BBTextView)findViewById(R.id.valueLabel);
		adsrButtons[ATTACK_ID] = (ToggleButton)findViewById(R.id.attackButton);
		adsrButtons[DECAY_ID] = (ToggleButton)findViewById(R.id.decayButton);
		adsrButtons[SUSTAIN_ID] = (ToggleButton)findViewById(R.id.sustainButton);
		adsrButtons[RELEASE_ID] = (ToggleButton)findViewById(R.id.releaseButton);
		adsrButtons[START_ID] = (ToggleButton)findViewById(R.id.startButton);
		adsrButtons[PEAK_ID] = (ToggleButton)findViewById(R.id.peakButton);
		for (int i = 0; i < adsrButtons.length; i++) {
			adsrButtons[i].setTag(i);
			adsrButtons[i].setOnClickListener(this);
		}
		updateLabels();
	}

	@Override
	public void update() {
	}

	@Override
	public void setVisibilityCode(int code) {
		adsrView.setVisibility(code);
		adsrBar.setVisibility(code);
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
		adsrBar.layout(pos, halfHeight, width, b);
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
		adsrBar.measure(3 * halfHeight, halfHeight);
	}

	private void check(ToggleButton btn) {
		btn.setChecked(true);
		for (ToggleButton otherBtn : adsrButtons) {
			if (otherBtn != btn) {
				otherBtn.setChecked(false);
			}
		}
	}
	
	private void updateLabels() {
		updateLabel();
		updateValueLabel();
	}
	
	private void updateLabel() {
		// update the displayed param name
		paramLabel.setText(params[currParamId].name);		
	}
	
	private void updateValueLabel() {
		valueLabel.setText(params[currParamId].getFormattedValueString());
	}
	
	@Override
	public void onClick(View v) {
		check((ToggleButton)v);
		int paramId = (Integer)v.getTag();
		// set the current parameter so we know what to do with SeekBar events.
		currParamId = paramId;
		updateLabels();
		switch (paramId) {
		case ATTACK_ID:
			break;
		case DECAY_ID:
			break;
		case SUSTAIN_ID:
			break;
		case RELEASE_ID:
			break;
		case START_ID:
			break;
		case PEAK_ID:
			break;
		}
	}
}
