package com.kh.beatbot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareImageView extends ImageView {
	public SquareImageView(Context context) {
		super(context);
	}
	
	public SquareImageView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int width, int height) {
		super.onMeasure(height, height);
	}
}
