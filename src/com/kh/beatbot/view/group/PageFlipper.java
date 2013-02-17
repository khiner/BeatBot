package com.kh.beatbot.view.group;

import android.content.Context;
import android.util.AttributeSet;

import com.kh.beatbot.layout.page.AdsrPage;
import com.kh.beatbot.layout.page.LevelsFXPage;
import com.kh.beatbot.layout.page.NoteLevelsPage;
import com.kh.beatbot.layout.page.SampleEditPage;

public class PageFlipper extends GLSurfaceViewFlipper {
	private static NoteLevelsPage levelsPage = null;
	private static LevelsFXPage masterLevelsFxPage = null, trackLevelsFxPage = null;
	private static SampleEditPage sampleEditPage = null;
	private static AdsrPage adsrPage = null;
	
	public PageFlipper(Context context, AttributeSet attr) {
		super(context, attr);
	}
	
	public void createPages() {
		masterLevelsFxPage = new LevelsFXPage(this);
		trackLevelsFxPage = new LevelsFXPage(this);
		sampleEditPage = new SampleEditPage(this);
		levelsPage = new NoteLevelsPage(this);
		adsrPage = new AdsrPage(this);
		masterLevelsFxPage.setMasterMode(true);
		trackLevelsFxPage.setMasterMode(false);
		addPage(masterLevelsFxPage);
		addPage(levelsPage);
		addPage(sampleEditPage);
		addPage(trackLevelsFxPage);
		addPage(adsrPage);
	}
	
	public static AdsrPage getAdsrPage() {
		return adsrPage;
	}
	
	public void notifyTrackChanged() {
		trackLevelsFxPage.update();
		sampleEditPage.update();
		adsrPage.update();
	}
}
