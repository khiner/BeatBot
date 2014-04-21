package com.kh.beatbot.ui.view.group;

import java.io.File;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.Track;
import com.kh.beatbot.event.FileListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.ViewPager;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.page.AdsrPage;
import com.kh.beatbot.ui.view.page.BrowsePage;
import com.kh.beatbot.ui.view.page.EffectsPage;
import com.kh.beatbot.ui.view.page.LevelsPage;
import com.kh.beatbot.ui.view.page.NoteLevelsPage;
import com.kh.beatbot.ui.view.page.SampleEditPage;

public class PageSelectGroup extends TouchableView implements TrackListener, FileListener {

	private static final String TRACK_PAGE_ID = "track";

	public static NoteLevelsPage noteLevelsPage;
	public static LevelsPage levelsPage;
	public static EffectsPage effectsPage;
	public static BrowsePage browsePage;
	public static SampleEditPage editPage;
	public static AdsrPage adsrPage;
	public static ToggleButton masterButton;

	public static ViewPager pager, buttonRowPager;
	public static TrackPageButtonRow trackButtonRow;
	public static MasterPageButtonRow masterButtonRow;

	public PageSelectGroup(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	public void setBPM(float bpm) {
		masterButtonRow.setBPM(bpm);
	}

	public void updateAdsrPage() {
		adsrPage.update();
	}

	public void updateLevelsFXPage() {
		levelsPage.update();
		effectsPage.update();
	}

	public void selectBrowsePage() {
		trackButtonRow.getBrowseButton().trigger(true);
	}

	public void selectNoteLevelsPage() {
		trackButtonRow.getNoteLevelsButton().trigger(true);
	}

	public void selectLevelsPage() {
		trackButtonRow.getLevelsButton().trigger(true);
	}

	public void updateAll() {
		trackButtonRow.update();
		browsePage.update();
		updateLevelsFXPage();
		editPage.update();
		adsrPage.update();
		noteLevelsPage.update();
	}

	@Override
	protected synchronized void createChildren() {
		masterButton = new ToggleButton(shapeGroup, false);

		masterButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				TrackManager.masterTrack.select();
			}
		});

		levelsPage = new LevelsPage();
		effectsPage = new EffectsPage();
		browsePage = new BrowsePage();
		editPage = new SampleEditPage();
		adsrPage = new AdsrPage();
		noteLevelsPage = new NoteLevelsPage();

		pager = new ViewPager();
		buttonRowPager = new ViewPager();

		trackButtonRow = new TrackPageButtonRow(null, pager);
		masterButtonRow = new MasterPageButtonRow(null, pager);

		buttonRowPager.addPage(masterButton, masterButtonRow);
		buttonRowPager.addPage(TRACK_PAGE_ID, trackButtonRow);

		pager.addPage(trackButtonRow.getBrowseButton(), browsePage);
		pager.addPage(trackButtonRow.getLevelsButton(), levelsPage);
		pager.addPage(trackButtonRow.getEffectsButton(), effectsPage);
		pager.addPage(trackButtonRow.getEditButton(), editPage);
		pager.addPage(trackButtonRow.getAdsrButton(), adsrPage);
		pager.addPage(trackButtonRow.getNoteLevelsButton(), noteLevelsPage);

		pager.addPage(masterButtonRow.getLevelsButton(), levelsPage);
		pager.addPage(masterButtonRow.getEffectsButton(), effectsPage);

		masterButton.setIcon(IconResourceSets.INSTRUMENT_BASE);
		masterButton.setText("Master");

		addChildren(masterButton, pager, buttonRowPager);
	}

	@Override
	public synchronized void layoutChildren() {
		float labelYOffset = 2;

		masterButton.layout(this, 0, labelYOffset,
				View.mainPage.midiViewGroup.getTrackControlWidth(), LABEL_HEIGHT);
		buttonRowPager.layout(this, masterButton.width, labelYOffset, width - masterButton.width,
				LABEL_HEIGHT);
		pager.layout(this, 0, LABEL_HEIGHT + 2 * labelYOffset, width, height - LABEL_HEIGHT - 2
				* labelYOffset);
	}

	@Override
	public void onCreate(Track track) {
		updateAll();
	}

	@Override
	public void onDestroy(Track track) {
	}

	@Override
	public synchronized void onSelect(BaseTrack track) {
		boolean isMaster = !(track instanceof Track);
		buttonRowPager.setPage(isMaster ? masterButton : TRACK_PAGE_ID);
		masterButton.setChecked(isMaster);
		levelsPage.setMasterMode(isMaster);
		effectsPage.setMasterMode(isMaster);
		((PageButtonRow) buttonRowPager.getCurrPage()).currPage.trigger();
		updateAll();
	}

	@Override
	public synchronized void onSampleChange(Track track) {
		editPage.update();
		trackButtonRow.update();
		trackButtonRow.getBrowseButton().trigger();
	}

	@Override
	public void onMuteChange(Track track, boolean mute) {
	}

	@Override
	public void onSoloChange(Track track, boolean solo) {
	}

	@Override
	public void onNameChange(File file, File newFile) {
		ToggleButton browseButton = trackButtonRow.getBrowseButton();
		if (browseButton.getText().equals(FileManager.formatSampleName(file.getName()))) {
			browseButton.setText(FileManager.formatSampleName(newFile.getName()));
		}
		browsePage.update();
		trackButtonRow.getBrowseButton().trigger();
	}
}
