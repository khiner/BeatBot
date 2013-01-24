package com.kh.beatbot.manager;

import android.app.Activity;
import android.widget.ViewFlipper;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.layout.page.AdsrPage;
import com.kh.beatbot.layout.page.LevelsFXPage;
import com.kh.beatbot.layout.page.MainPageSelect;
import com.kh.beatbot.layout.page.NoteLevelsPage;
import com.kh.beatbot.layout.page.Page;
import com.kh.beatbot.layout.page.SampleEditPage;

public class PageManager {
	private static int mainPageNum = 0;
	
	private static Page[] pages;
	
	private static MainPageSelect mainPageSelect = null;
	private static NoteLevelsPage levelsPage = null;
	private static LevelsFXPage masterLevelsFxPage = null, trackLevelsFxPage = null;
	private static SampleEditPage sampleEditPage = null;
	private static AdsrPage adsrPage = null;
	
	public static void init(Activity context) {
		mainPageSelect = (MainPageSelect)context.findViewById(R.id.mainPageSelect);
		mainPageSelect.init();
		masterLevelsFxPage = (LevelsFXPage)context.findViewById(R.id.masterLevelsFxPage);
		trackLevelsFxPage = (LevelsFXPage)context.findViewById(R.id.trackLevelsFxPage);
		sampleEditPage = (SampleEditPage)context.findViewById(R.id.sampleEditPage);
		levelsPage = (NoteLevelsPage)context.findViewById(R.id.levelsPage);
		adsrPage = (AdsrPage)context.findViewById(R.id.adsrPage);
		pages = new Page[] {masterLevelsFxPage, levelsPage, sampleEditPage, trackLevelsFxPage, adsrPage};
		
		for (Page page : pages) {
			page.init();
		}
	}
	
	public static void selectPage(int mainPageNum) {
		int prevPageNum = PageManager.mainPageNum;
		PageManager.mainPageNum = mainPageNum;
		if (prevPageNum == mainPageNum)
			return;
		pages[prevPageNum].setVisible(false);
		((ViewFlipper) GlobalVars.mainActivity.findViewById(R.id.mainFlipper))
				.setDisplayedChild(mainPageNum);
		pages[mainPageNum].setVisible(true);
	}
	
	public static AdsrPage getAdsrPage() {
		return adsrPage;
	}
	
	public static void notifyTrackChanged() {
		mainPageSelect.update();
		trackLevelsFxPage.update();
		sampleEditPage.update();
		adsrPage.update();
	}
}
