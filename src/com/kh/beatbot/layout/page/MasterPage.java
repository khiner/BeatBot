package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

public class MasterPage extends Page {
	public ViewFlipper pageFlipper;
	
	private LevelsPage levelsPage = null;
	private EffectsPage effectsPage = null;
	
	private Page[] pages = null;
	
	private int currPageNum = 0;
	
	public MasterPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void init() {
		pages = new Page[] {levelsPage, effectsPage};
	}

	@Override
	public void update() {
		effectsPage.setMasterMode(true);
		levelsPage.setMasterMode(true);
		for (Page masterPage : pages) {
			masterPage.update();
		}
	}

	@Override
	public void setVisibilityCode(int code) {
		
	}
	
	public void selectPage(final int pageNum) {
		int prevPageNum = this.currPageNum;
		this.currPageNum = pageNum;
		if (prevPageNum == pageNum)
			return;
		getPage(prevPageNum).setVisible(false);
		pageFlipper.setDisplayedChild(pageNum);
		getPage(pageNum).setVisible(true);
	}
	
	public Page getPage(final int pageNum) {
		return pages[pageNum];
	}
}
