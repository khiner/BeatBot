package com.kh.beatbot;

public class DecimateActivity extends EffectActivity {

	@Override
	public void setXValue(float xValue) {
		super.setXValue(xValue);
		setBits(trackNum, xValue);
	}
	
	@Override
	public void setYValue(float yValue) {
		super.setYValue(yValue);
		setRate(trackNum, yValue);
	}
	
	public native void setBits(int trackNum, float bits);
	public native void setRate(int trackNum, float rate);
}
