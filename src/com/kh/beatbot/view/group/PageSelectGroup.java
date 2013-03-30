package com.kh.beatbot.view.group;


import com.kh.beatbot.R;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.layout.page.AdsrPage;
import com.kh.beatbot.layout.page.LevelsFXPage;
import com.kh.beatbot.layout.page.NoteLevelsPage;
import com.kh.beatbot.layout.page.SampleEditPage;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.Button;
import com.kh.beatbot.view.ImageButton;
import com.kh.beatbot.view.TextButton;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;

public class PageSelectGroup extends TouchableBBView {
	private static NoteLevelsPage levelsPage;
	private static LevelsFXPage masterLevelsFxPage, trackLevelsFxPage;
	private static SampleEditPage sampleEditPage;
	private static AdsrPage adsrPage;
	private static BBViewPager pager;
	
	private static final int LEVELS_FX_PAGE_ID = 0;
	private static final int EDIT_PAGE_ID = 1;
	private static final int ADSR_PAGE_ID = 2;
	private static final int MASTER_PAGE_ID = 3;
	private static final int NOTE_LEVELS_PAGE_ID = 4;
	
	ImageButton addTrackButton;
	ImageButton instrumentSelectButton;
	TextButton sampleSelectButton;
	TextButton levelsFxButton;
	TextButton editButton;
	TextButton adsrButton;
	TextButton masterButton;
	ImageButton levelsButton;
	
	public PageSelectGroup(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public void update() {
		updateInstrumentIcon();
		updateSampleText();
	}
	
	public void updateAdsrPage() {
		adsrPage.updateLevelBar();
		adsrPage.updateLabels();
	}
	
	public void updateLevelsFXPage() {
		masterLevelsFxPage.update();
		trackLevelsFxPage.update();
	}
	public void notifyTrackChanged() {
		if (!initialized)
			return;
		update();
		trackLevelsFxPage.update();
		sampleEditPage.update();
		adsrPage.update();
	}
	
	private void updateInstrumentIcon() {
		// update the track pager instrument icon
		if (TrackManager.currTrack.getInstrument().getIconSource().defaultIcon == null) {
			return;
		}
		instrumentSelectButton
				.setIconSource(TrackManager.currTrack.getInstrument().getIconSource());
	}

	private void updateSampleText() {
		// update sample label text
		// TODO handle all extensions
		String formattedName = TrackManager.currTrack.getSampleName().replace(".bb", "")
				.toUpperCase();
		sampleSelectButton.setText(formattedName);
	}

	@Override
	public void draw() {
		// Parent
	}

	@Override
	protected void createChildren() {
		addTrackButton = new ImageButton((TouchableSurfaceView) root);
		instrumentSelectButton = new ImageButton((TouchableSurfaceView) root);
		sampleSelectButton = new TextButton((TouchableSurfaceView) root);
		levelsFxButton = new TextButton((TouchableSurfaceView) root);
		editButton = new TextButton((TouchableSurfaceView) root);
		adsrButton = new TextButton((TouchableSurfaceView) root);
		masterButton = new TextButton((TouchableSurfaceView) root);
		levelsButton = new ImageButton((TouchableSurfaceView) root);
		
		addTrackButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				Managers.directoryManager.showAddTrackAlert();
			}
		});
		
		instrumentSelectButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				Managers.directoryManager.showInstrumentSelectAlert();
			}
		});
		
		sampleSelectButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				Managers.directoryManager.showSampleSelectAlert();
			}
		});
		
		levelsFxButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				pager.setPage(LEVELS_FX_PAGE_ID);
			}
		});
		
		editButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				pager.setPage(EDIT_PAGE_ID);
			}
		});
		
		adsrButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				pager.setPage(ADSR_PAGE_ID);
			}
		});
		
		masterButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				pager.setPage(MASTER_PAGE_ID);
			}
		});
		
		levelsButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				pager.setPage(NOTE_LEVELS_PAGE_ID);
			}
		});
		
		trackLevelsFxPage = new LevelsFXPage((TouchableSurfaceView) root);
		sampleEditPage = new SampleEditPage((TouchableSurfaceView) root);
		adsrPage = new AdsrPage((TouchableSurfaceView) root);
		masterLevelsFxPage = new LevelsFXPage((TouchableSurfaceView) root);
		levelsPage = new NoteLevelsPage((TouchableSurfaceView) root);
		
		masterLevelsFxPage.setMasterMode(true);
		trackLevelsFxPage.setMasterMode(false);
		
		pager = new BBViewPager((TouchableSurfaceView) root);
		pager.addPage(trackLevelsFxPage);
		pager.addPage(sampleEditPage);
		pager.addPage(adsrPage);
		pager.addPage(masterLevelsFxPage);
		pager.addPage(levelsPage);
		pager.setPage(LEVELS_FX_PAGE_ID);
		
		addChild(addTrackButton);
		addChild(instrumentSelectButton);
		addChild(sampleSelectButton);
		addChild(levelsFxButton);
		addChild(editButton);
		addChild(adsrButton);
		addChild(masterButton);
		addChild(levelsButton);
		
		addChild(pager);
	}

	@Override
	public void layoutChildren() {
		float buttonHeight = height / 4;
		float labelWidth = (width - 2 * buttonHeight) / 5;
		addTrackButton.layout(this, 0, 0, buttonHeight, buttonHeight);
		instrumentSelectButton.layout(this, buttonHeight, 0, buttonHeight, buttonHeight);
		sampleSelectButton.layout(this, buttonHeight * 2, 0, labelWidth, buttonHeight);
		levelsFxButton.layout(this, buttonHeight * 2 + labelWidth, 0, labelWidth, buttonHeight);
		editButton.layout(this, buttonHeight * 2 + 2 * labelWidth, 0, labelWidth, buttonHeight);
		adsrButton.layout(this, buttonHeight * 2 + 3 * labelWidth, 0, labelWidth, buttonHeight);
		masterButton.layout(this, buttonHeight * 2 + 4 * labelWidth, 0, labelWidth, buttonHeight);
		levelsButton.layout(this, buttonHeight * 2 + 5 * labelWidth, 0, buttonHeight, buttonHeight);
		pager.layout(this, 0, buttonHeight, width, height - buttonHeight);
	}

	@Override
	protected void loadIcons() {
		addTrackButton.setIconSource(new BBIconSource(R.drawable.plus_outline, R.drawable.plus_outline));
		levelsFxButton.setText("FX");
		editButton.setText("EDIT");
		adsrButton.setText("ADSR");
		masterButton.setText("MASTER");
		levelsButton.setIconSource(new BBIconSource(R.drawable.levels_icon, R.drawable.levels_icon_selected));
	}

	@Override
	public void init() {
		// Parent
	}
}
