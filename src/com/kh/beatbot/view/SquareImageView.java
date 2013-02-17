package com.kh.beatbot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareImageView extends ImageView {
	double ratio = 1;

	public SquareImageView(Context context) {
		super(context);
	}

	public SquareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ratio = (double) this.getBackground().getIntrinsicWidth()
				/ (double) this.getBackground().getIntrinsicHeight();
	}

	@Override
	protected void onMeasure(int width, int height) {
		super.onMeasure((int) ratio * height, height);
	}
}
