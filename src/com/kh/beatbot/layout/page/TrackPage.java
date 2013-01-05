package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

import com.kh.beatbot.R;
import com.kh.beatbot.manager.TrackManager;

public class TrackPage extends Page {
	public TrackPageSelect trackPageSelect;
	private ViewFlipper pageFlipper;
	
	private Page[] pages;
	
	private LevelsPage levelsPage = null;
	private SampleEditPage sampleEditPage = null;
	private EffectsPage effectsPage = null;
	
	private int currPageNum = 0;
	
	public TrackPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void init() {
		trackPageSelect = (TrackPageSelect)findViewById(R.id.trackPageSelect);
		pageFlipper = (ViewFlipper)findViewById(R.id.trackFlipper);
		trackPageSelect.init();
		levelsPage = (LevelsPage)pageFlipper.findViewById(R.id.trackLevelsPage);
		sampleEditPage = (SampleEditPage)pageFlipper.findViewById(R.id.sampleEditPage);
		effectsPage = (EffectsPage)pageFlipper.findViewById(R.id.trackEffectsPage);
		pages = new Page[] {levelsPage, sampleEditPage, effectsPage };
		for (Page trackPage : pages) {
			trackPage.init();
		}
	}

	@Override
	public void update() {
		effectsPage.setMasterMode(false);
		levelsPage.setMasterMode(false);
		for (Page trackPage : pages) {
			trackPage.update();
		}
	}

	public void selectPage(int pageNum) {
		int prevPageNum = this.currPageNum;
		this.currPageNum = pageNum;
		if (prevPageNum == pageNum)
			return;
		getPage(prevPageNum).setVisible(false);
		pageFlipper.setDisplayedChild(pageNum);
		getPage(pageNum).setVisible(true);
	}
	
	@Override
	public void setVisibilityCode(int code) {
		getPage(currPageNum).setVisibilityCode(code);
	}

	public Page getPage(int trackPageNum) {
		return pages[trackPageNum];
	}
}
