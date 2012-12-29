package com.kh.beatbot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class SquareImageButton extends ImageButton {
	double ratio = 1;

	public SquareImageButton(Context context) {
		super(context);
	}

	public SquareImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		ratio = (double) this.getBackground().getIntrinsicWidth()
				/ (double) this.getBackground().getIntrinsicHeight();
	}

	@Override
	protected void onMeasure(int width, int height) {
		super.onMeasure((int) ratio * height, height);
	}
}
