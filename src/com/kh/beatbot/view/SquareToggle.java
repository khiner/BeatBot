package com.kh.beatbot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

public class SquareToggle extends ToggleButton {
	public SquareToggle(Context context) {
		super(context);
	}
	
	public SquareToggle(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int width, int height) {
		super.onMeasure(height, height);
	}
}
