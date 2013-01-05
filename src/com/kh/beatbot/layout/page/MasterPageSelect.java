package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;

import com.kh.beatbot.manager.PageManager;
import com.kh.beatbot.manager.PageSelect;

public class MasterPageSelect extends PageSelect {

	public MasterPageSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
		firstPageLabelIndex = 0;
	}

	@Override
	public void init() {
		super.init();
	}
	
	@Override
	public void selectPage(int pageNum) {
		PageManager.selectMasterPage(pageNum);
	}

	@Override
	protected void update() {
		// nothing
	}
}
