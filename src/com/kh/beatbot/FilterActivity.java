package com.kh.beatbot;

public class FilterActivity extends EffectActivity {
	
	@Override
	public void setXValue(float xValue) {
		super.setXValue(xValue);
		setCutoff(trackNum, xValue);
	}
	
	@Override
	public void setYValue(float yValue) {
		super.setYValue(yValue);
		setQ(trackNum, yValue);
	}
	
	public native void setCutoff(int trackNum, float cutoff);
	public native void setQ(int trackNum, float q);
}
