package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.kh.beatbot.manager.PageManager;
import com.kh.beatbot.manager.PageSelect;

public class MainPageSelect extends PageSelect {
	private ImageButton addTrackButton;
	
	public MainPageSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
		firstPageLabelIndex = 1;
	}

	public void init() {
		super.init();
		addTrackButton = (ImageButton)getChildAt(0);
	}
	
	protected void update() {
		// nothing
	}
	
	public void selectPage(final int pageNum) {
		PageManager.selectMainPage(pageNum);
	}
}
