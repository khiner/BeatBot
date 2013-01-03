package com.kh.beatbot.manager;

import android.app.Activity;
import android.widget.ViewFlipper;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.layout.page.Page;
import com.kh.beatbot.layout.page.SampleEditPage;
import com.kh.beatbot.layout.page.TrackEffectsPage;
import com.kh.beatbot.layout.page.TrackLevelsPage;

public class PageManager {
	private static int trackPageNum = 0;
	
	private static TrackLevelsPage trackLevelsPage = null;
	private static SampleEditPage sampleEditPage = null;
	private static TrackEffectsPage trackEffectsPage = null;
	
	private static Page[] trackPages;
	
	public static void init(Activity context) {
		trackLevelsPage = (TrackLevelsPage)context.findViewById(R.id.trackLevelsPage);
		sampleEditPage = (SampleEditPage)context.findViewById(R.id.sampleEditPage);
		trackEffectsPage = (TrackEffectsPage)context.findViewById(R.id.trackEffectsPage);
		trackPages = new Page[] {trackLevelsPage, sampleEditPage, trackEffectsPage };
		for (Page trackPage : trackPages) {
			trackPage.init();
		}
	}
	
	public static void selectTrackPage(int trackPageNum) {
		int prevPageNum = PageManager.trackPageNum;
		PageManager.trackPageNum = trackPageNum;
		if (prevPageNum == trackPageNum)
			return;
		getTrackPage(prevPageNum).setVisible(false);
		((ViewFlipper) GlobalVars.mainActivity.findViewById(R.id.trackFlipper))
				.setDisplayedChild(trackPageNum);
		getTrackPage(trackPageNum).setVisible(true);
	}
	
	public static Page getTrackPage(int trackPageNum) {
		return trackPages[trackPageNum];
	}
	
	public static void updateTrackPages() {
		for (Page trackPage : trackPages) {
			trackPage.update();
		}
	}
}
