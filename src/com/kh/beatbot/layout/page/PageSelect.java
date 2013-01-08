package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kh.beatbot.global.GlobalVars;

public abstract class PageSelect extends LinearLayout {
	protected TextView[] pageLabels;
	protected int firstPageLabelIndex = 0;

	public PageSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init() {
		pageLabels = new TextView[getChildCount() - firstPageLabelIndex];
		for (int i = 0; i < pageLabels.length; i++) {
			final int pageNum = i;
			final TextView pageLabel = (TextView) getChildAt(i
					+ firstPageLabelIndex);
			pageLabel.setTypeface(GlobalVars.font);
			pageLabel.setGravity(Gravity.CENTER);
			pageLabel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (TextView otherPageLabel : pageLabels) {
						otherPageLabel.setSelected(pageLabel
								.equals(otherPageLabel));
					}
					selectPage(pageNum);
				}
			});
			pageLabels[i] = pageLabel;
		}
	}

	public abstract void selectPage(final int pageNum);

	protected abstract void update();

	/*******
	 * Override onLayout AND onMeasure to ensure cheap (no weight) even spacing
	 * AND text centering
	 ******/
	
	protected void onLayout(boolean changed, int l, int t, int r, int b, int firstPageLabel) {
		super.onLayout(changed, l, t, r, b);
		int len = getChildCount() - firstPageLabel;
		int h = b - t;
		int w = (r - l - h) / len;
		getChildAt(0).layout(0, 0, h, h);
		int pos = h;
		for (int i = firstPageLabel; i < getChildCount(); i++) {
			View c = getChildAt(i);
			c.layout(pos, 0, pos + w, h);
			pos += w;
		}
	}
	
	protected void onMeasure(int w, int h, int firstPageLabel) {
		super.onMeasure(w, h);
		int wspec = MeasureSpec.makeMeasureSpec(
				(getMeasuredWidth() - getMeasuredHeight())
						/ (getChildCount() - firstPageLabel),
				MeasureSpec.EXACTLY);
		int hspec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(),
				MeasureSpec.EXACTLY);
		for (int i = 0; i < firstPageLabel; i++) {
			getChildAt(i).measure(hspec, hspec);
		}
		for (int i = firstPageLabel; i < getChildCount(); i++) {
			View v = getChildAt(i);
			v.measure(wspec, hspec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		onLayout(changed, l, t, r, b, firstPageLabelIndex);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		onMeasure(widthMeasureSpec, heightMeasureSpec, firstPageLabelIndex);
	}
}
