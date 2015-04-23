package com.kh.beatbot.ui.view.group;

import java.io.File;

import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.listener.FileListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.PagerListener;
import com.kh.beatbot.listener.TrackLevelsEventListener;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.icon.IconResourceSets;
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
import com.kh.beatbot.ui.view.page.RecordPage;
import com.kh.beatbot.ui.view.page.SampleEditPage;

public class PageSelectGroup extends TouchableView implements TrackListener,
		TrackLevelsEventListener, FileListener, PagerListener {

	private static final String TRACK_PAGE_ID = "track";

	public static NoteLevelsPage noteLevelsPage;
	public static LevelsPage levelsPage;
	public static EffectsPage effectsPage;
	public static BrowsePage browsePage;
	public static SampleEditPage editPage;
	public static AdsrPage adsrPage;
	public static RecordPage recordPage;

	public static ToggleButton masterButton;

	public static ViewPager pager, buttonRowPager;
	public static TrackPageButtonRow trackButtonRow;
	public static MasterPageButtonRow masterButtonRow;

	public PageSelectGroup(View view) {
		super(view);
		TrackManager.addTrackLevelsEventListener(this);
	}

	public void setBPM(float bpm) {
		masterButtonRow.setBPM(bpm);
	}

	public void selectBrowsePage() {
		trackButtonRow.getBrowseButton().trigger(true);
	}

	public void selectLevelsPage() {
		((PageButtonRow) buttonRowPager.getCurrPage()).getLevelsButton().trigger();
	}

	@Override
	protected synchronized void createChildren() {
		masterButton = new ToggleButton(this).withRoundedRect().withIcon(
				IconResourceSets.INSTRUMENT_BASE);

		masterButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				TrackManager.getMasterTrack().select();
			}
		});

		buttonRowPager = new ViewPager(this);
		pager = new ViewPager(this);

		trackButtonRow = new TrackPageButtonRow(buttonRowPager);
		masterButtonRow = new MasterPageButtonRow(buttonRowPager);
		trackButtonRow.setPager(pager);
		masterButtonRow.setPager(pager);

		levelsPage = new LevelsPage(pager);
		effectsPage = new EffectsPage(pager);
		browsePage = new BrowsePage(pager, null);
		browsePage.setClip(true);
		editPage = new SampleEditPage(pager);
		adsrPage = new AdsrPage(pager);
		noteLevelsPage = new NoteLevelsPage(pager);
		recordPage = new RecordPage(pager);

		pager.addListener(this);

		buttonRowPager.addPage(masterButton, masterButtonRow);
		buttonRowPager.addPage(TRACK_PAGE_ID, trackButtonRow);

		pager.addPage(trackButtonRow.getBrowseButton(), browsePage);
		pager.addPage(trackButtonRow.getLevelsButton(), levelsPage);
		pager.addPage(trackButtonRow.getEffectsButton(), effectsPage);
		pager.addPage(trackButtonRow.getEditButton(), editPage);
		pager.addPage(trackButtonRow.getAdsrButton(), adsrPage);
		pager.addPage(trackButtonRow.getNoteLevelsButton(), noteLevelsPage);
		pager.addPage(trackButtonRow.getRecordButton(), recordPage);

		pager.addPage(masterButtonRow.getLevelsButton(), levelsPage);
		pager.addPage(masterButtonRow.getEffectsButton(), effectsPage);

		masterButton.setText("Master");
	}

	@Override
	public synchronized void layoutChildren() {
		masterButton.layout(this, 0, BG_OFFSET, View.mainPage.midiViewGroup.getTrackControlWidth(),
				LABEL_HEIGHT);
		buttonRowPager.layout(this, masterButton.width, BG_OFFSET, width - masterButton.width,
				LABEL_HEIGHT);
		pager.layout(this, BG_OFFSET, LABEL_HEIGHT + BG_OFFSET, width - 2 * BG_OFFSET, height
				- LABEL_HEIGHT - 2 * BG_OFFSET);
	}

	@Override
	public void onCreate(Track track) {
		TrackListener currPage = (TrackListener) pager.getCurrPage();
		if (null != currPage) {
			currPage.onSelect(track);
		}
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
		((TrackListener) pager.getCurrPage()).onSelect(track);
		if (!isMaster) {
			trackButtonRow.update();
		}
	}

	@Override
	public synchronized void onSampleChange(Track track) {
		trackButtonRow.update();
		((TrackListener) pager.getCurrPage()).onSelect(track);
	}

	@Override
	public void onMuteChange(Track track, boolean mute) {
	}

	@Override
	public void onSoloChange(Track track, boolean solo) {
	}

	@Override
	public void onNameChange(File file, File newFile) {
		trackButtonRow.update();
		((TrackListener) pager.getCurrPage()).onSelect(TrackManager.currTrack);
	}

	@Override
	public void onPageChange(ViewPager pager, View prevPage, View newPage) {
		((TrackListener) newPage).onSelect(TrackManager.currTrack);
	}

	public void onNoteLevelsChange(MidiNote note, LevelType type) {
		TrackManager.getTrack(note).select();
		// select note levels page whenever a note levels change event occurs
		trackButtonRow.getNoteLevelsButton().trigger(true);
		noteLevelsPage.setLevelType(type);
	}

	@Override
	public void onTrackLevelsChange(BaseTrack track) {
		selectLevelsPage();
	}

	@Override
	public void onSampleLoopWindowChange(Track track) {
		trackButtonRow.getEditButton().trigger(true);
	}

	public void updateEffectsPage() {
		effectsPage.updateEffectLabels();
	}
}
