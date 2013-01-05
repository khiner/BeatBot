package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public abstract class Page extends LinearLayout {

	public Page(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public abstract void init();
	public abstract void update();
	public abstract void setVisibilityCode(int code);

	public void setVisible(boolean visible) {
		setVisibilityCode(visible ? View.VISIBLE : View.INVISIBLE);
	}
}
