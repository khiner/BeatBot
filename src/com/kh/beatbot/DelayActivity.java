package com.kh.beatbot;

public class DelayActivity extends EffectActivity {

	@Override
	public void setXValue(float xValue) {
		super.setXValue(xValue);
		setDelayTime(trackNum, xValue);
	}
	
	@Override
	public void setYValue(float yValue) {
		super.setYValue(yValue);
		setDelayFeedback(trackNum, yValue);
	}
	
	public native void setDelayTime(int trackNum, float delay);
	public native void setDelayFeedback(int trackNum, float feedback);
}
