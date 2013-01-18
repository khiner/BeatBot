package com.kh.beatbot.manager;

import android.app.Activity;
import android.widget.ViewFlipper;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.layout.page.AdsrPage;
import com.kh.beatbot.layout.page.MainPageSelect;
import com.kh.beatbot.layout.page.MasterPage;
import com.kh.beatbot.layout.page.NoteLevelsPage;
import com.kh.beatbot.layout.page.Page;
import com.kh.beatbot.layout.page.TrackPage;

public class PageManager {
	private static int mainPageNum = 0;
	
	private static Page[] mainPages;
	
	private static MasterPage masterPage = null;
	private static TrackPage trackPage = null;
	private static NoteLevelsPage levelsPage = null;
	private static AdsrPage adsrPage = null;
	
	public static void init(Activity context) {
		((MainPageSelect)context.findViewById(R.id.mainPageSelect)).init();
		masterPage = (MasterPage)context.findViewById(R.id.masterPage);
		trackPage = (TrackPage)context.findViewById(R.id.trackPage);
		levelsPage = (NoteLevelsPage)context.findViewById(R.id.levelsPage);
		adsrPage = (AdsrPage)context.findViewById(R.id.adsrPage);
		mainPages = new Page[] {masterPage, trackPage, levelsPage, adsrPage};
		
		for (Page mainPage : mainPages) {
			mainPage.init();
		}
	}
	
	public static void selectMainPage(int mainPageNum) {
		int prevPageNum = PageManager.mainPageNum;
		PageManager.mainPageNum = mainPageNum;
		if (prevPageNum == mainPageNum)
			return;
		mainPages[prevPageNum].setVisible(false);
		((ViewFlipper) GlobalVars.mainActivity.findViewById(R.id.mainFlipper))
				.setDisplayedChild(mainPageNum);
		mainPages[mainPageNum].setVisible(true);
	}
	
	public static void selectMasterPage(int masterPageNum) {
		masterPage.selectPage(masterPageNum);
	}
	
	public static void selectTrackPage(int trackPageNum) {
		trackPage.selectPage(trackPageNum);
	}
	
	public static Page getTrackPage(int trackPageNum) {
		return trackPage.getPage(trackPageNum);
	}
	
	public static AdsrPage getAdsrPage() {
		return adsrPage;
	}
	
	public static void updateTrackPages() {
		trackPage.update();
	}
}
