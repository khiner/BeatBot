package com.kh.beatbot.layout.page;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kh.beatbot.global.GeneralUtils;
import com.kh.beatbot.global.GlobalVars;

public abstract class PageSelect extends LinearLayout {
	protected TextView[] pageLabels;
	protected int[] fixedWidthIndices;

	public PageSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private TextView[] getAllTextViews() {
		ArrayList<TextView> textViews = new ArrayList<TextView>();
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (child instanceof TextView)
				textViews.add((TextView)child);
		}
		return textViews.toArray(new TextView[textViews.size()]);
	}
	
	public void init() {
		pageLabels = getAllTextViews();
		for (int i = 0; i < pageLabels.length; i++) {
			final int pageNum = i;
			final TextView pageLabel = pageLabels[i];
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
		}
	}

	public abstract void selectPage(final int pageNum);

	public abstract void update();

	/*******
	 * Override onLayout AND onMeasure to ensure cheap (no weight) even spacing
	 * AND text centering
	 ******/
	
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		int numLabels = getChildCount() - fixedWidthIndices.length;
		int h = b - t;
		int w = (r - l - fixedWidthIndices.length * h) / numLabels;
		int pos = 0;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (GeneralUtils.contains(fixedWidthIndices, i)) {
				child.layout(pos, 0, pos + h, h);
				pos += h;
			} else {
				child.layout(pos, 0, pos + w, h);
				pos += w;
			}
		}
	}
	
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		int wspec = MeasureSpec.makeMeasureSpec(
				(getMeasuredWidth() - getMeasuredHeight() * fixedWidthIndices.length)
						/ (getChildCount() - fixedWidthIndices.length), MeasureSpec.EXACTLY);
		int hspec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(),
				MeasureSpec.EXACTLY);
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (GeneralUtils.contains(fixedWidthIndices, i)) {
				child.measure(hspec, hspec);
			} else {
				child.measure(wspec, hspec);
			}
		}
	}
}
