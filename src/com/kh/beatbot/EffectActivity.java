package com.kh.beatbot;

import android.app.Activity;
import android.os.Bundle;

import com.KarlHiner.BeatBot.R;

public abstract class EffectActivity extends Activity {
	
	protected float xValue = 0, yValue = 0;
	protected int trackNum;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.effect_layout);
		trackNum = getIntent().getExtras().getInt("trackNum");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	public void setXValue(float xValue) {
		this.xValue = xValue;
	}
	
	public void setYValue(float yValue) {
		this.yValue = yValue;
	}
	
	public float getXValue() {
		return xValue;
	}
	
	public float getYValue() {
		return yValue;
	}
	
}
