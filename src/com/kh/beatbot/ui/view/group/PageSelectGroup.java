package com.kh.beatbot.ui.view.group;

import java.io.File;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.listener.FileListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.PagerListener;
import com.kh.beatbot.listener.TrackLevelsEventListener;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.SwappingViewPager;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.page.track.AdsrPage;
import com.kh.beatbot.ui.view.page.track.BrowsePage;
import com.kh.beatbot.ui.view.page.track.EffectSelectPage;
import com.kh.beatbot.ui.view.page.track.NoteLevelsPage;
import com.kh.beatbot.ui.view.page.track.RecordPage;
import com.kh.beatbot.ui.view.page.track.SampleEditPage;
import com.kh.beatbot.ui.view.page.track.TempoPage;
import com.kh.beatbot.ui.view.page.track.TrackLevelsPage;

public class PageSelectGroup extends TouchableView implements TrackListener,
		TrackLevelsEventListener, FileListener, PagerListener {

	private static final String TRACK_PAGE_ID = "track";

	private NoteLevelsPage noteLevelsPage;
	private TrackLevelsPage levelsPage;
	private EffectSelectPage effectSelectPage;
	private BrowsePage browsePage;
	private SampleEditPage editPage;
	private AdsrPage adsrPage;
	private RecordPage recordPage;
	private TempoPage tempoPage;
	
	private ToggleButton masterButton;

	private SwappingViewPager pager, buttonRowPager;
	private TrackPageButtonRow trackButtonRow;
	private MasterPageButtonRow masterButtonRow;

	public PageSelectGroup(View view) {
		super(view);
	}

	public void selectEffectPage() {
		ToggleButton effectsButton = trackButtonRow.getEffectsButton();
		if (!effectsButton.isChecked()) {
			effectsButton.trigger(true);
		}
	}

	public void selectBrowsePage() {
		ToggleButton browseButton = trackButtonRow.getBrowseButton();
		if (!browseButton.isChecked()) {
			browseButton.trigger(true);
		}
	}

	public void selectLevelsPage() {
		PageButtonRow pageButtonRow = ((PageButtonRow) buttonRowPager.getCurrPage());
		if (null == pageButtonRow)
			return;
		ToggleButton levelsButton = pageButtonRow.getLevelsButton();
		if (!levelsButton.isChecked()) {
			levelsButton.trigger();
		}
	}

	public SampleEditPage getEditPage() {
		return editPage;
	}

	public void selectEditPage() {
		ToggleButton editButton = trackButtonRow.getEditButton();
		if (!editButton.isChecked()) {
			editButton.trigger(true);
		}
	}

	@Override
	protected synchronized void createChildren() {
		masterButton = new ToggleButton(this).withRoundedRect().withIcon(
				IconResourceSets.INSTRUMENT_BASE);

		masterButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				context.getTrackManager().getMasterTrack().select();
			}
		});

		buttonRowPager = new SwappingViewPager(this);
		pager = new SwappingViewPager(this);

		trackButtonRow = new TrackPageButtonRow(buttonRowPager);
		masterButtonRow = new MasterPageButtonRow(buttonRowPager);
		trackButtonRow.setPager(pager);
		masterButtonRow.setPager(pager);

		levelsPage = new TrackLevelsPage(pager);
		effectSelectPage = new EffectSelectPage(pager);
		browsePage = new BrowsePage(pager, null);
		browsePage.setClip(true);
		editPage = new SampleEditPage(pager);
		adsrPage = new AdsrPage(pager);
		noteLevelsPage = new NoteLevelsPage(pager);
		recordPage = new RecordPage(pager);

		tempoPage = new TempoPage(pager);

		pager.addListener(this);

		buttonRowPager.addPage(masterButton, masterButtonRow);
		buttonRowPager.addPage(TRACK_PAGE_ID, trackButtonRow);

		pager.addPage(trackButtonRow.getBrowseButton(), browsePage);
		pager.addPage(trackButtonRow.getLevelsButton(), levelsPage);
		pager.addPage(trackButtonRow.getEffectsButton(), effectSelectPage);
		pager.addPage(trackButtonRow.getEditButton(), editPage);
		pager.addPage(trackButtonRow.getAdsrButton(), adsrPage);
		pager.addPage(trackButtonRow.getNoteLevelsButton(), noteLevelsPage);
		pager.addPage(trackButtonRow.getRecordButton(), recordPage);

		pager.addPage(masterButtonRow.getLevelsButton(), levelsPage);
		pager.addPage(masterButtonRow.getEffectsButton(), effectSelectPage);
		pager.addPage(masterButtonRow.getTempoButton(), tempoPage);

		masterButton.setText("Master");
	}

	public int getCurrPageIndex() {
		PageButtonRow currPage = ((PageButtonRow) buttonRowPager.getCurrPage());
		return currPage.indexOf((View) pager.getCurrPageId());
	}

	public void selectPage(int pageIndex) {
		PageButtonRow currPage = ((PageButtonRow) buttonRowPager.getCurrPage());
		((ToggleButton) currPage.getChild(pageIndex)).trigger();
	}

	@Override
	public synchronized void layoutChildren() {
		masterButton.layout(this, 0, BG_OFFSET, context.getMainPage().getMidiViewGroup()
				.getTrackControlWidth(), LABEL_HEIGHT);
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
		effectSelectPage.setMasterMode(isMaster);
		((PageButtonRow) buttonRowPager.getCurrPage()).currPage.trigger();
		((TrackListener) pager.getCurrPage()).onSelect(track);
		if (!isMaster) {
			trackButtonRow.update();
		}
	}

	@Override
	public synchronized void onSampleChange(Track track) {
		selectBrowsePage();
		browsePage.onSampleChange(track);
	}

	@Override
	public void onMuteChange(Track track, boolean mute) {
	}

	@Override
	public void onSoloChange(Track track, boolean solo) {
	}

	@Override
	public void onNameChange(File file, File newFile) {
		if (masterButton.isChecked())
			// make sure *some* track is selected.
			context.getTrackManager().getTrackByNoteValue(0).select();
		else {
			trackButtonRow.update();
		}
		selectBrowsePage();
		browsePage.update();
	}

	@Override
	public void onPageChange(SwappingViewPager pager, View prevPage, View newPage) {
		((TrackListener) newPage).onSelect(context.getTrackManager().getCurrTrack());
	}

	public void onNoteLevelsChange(MidiNote note, LevelType type) {
		final Track track = context.getTrackManager().getTrack(note);
		if (!track.isSelected())
			track.select();
		// select note levels page whenever a note levels change event occurs
		ToggleButton noteLevelsButton = trackButtonRow.getNoteLevelsButton();
		if (!noteLevelsButton.isChecked()) {
			noteLevelsButton.trigger(true);
		}
		noteLevelsPage.setLevelType(type);
	}

	@Override
	public void onTrackLevelsChange(BaseTrack track) {
		selectLevelsPage();
	}

	@Override
	public void onSampleLoopWindowChange(Track track) {
		selectEditPage();
	}

	@Override
	public void onEffectCreate(BaseTrack track, Effect effect) {
		selectEffectPage();
		effectSelectPage.onEffectCreate(track, effect);
	}

	@Override
	public void onEffectDestroy(BaseTrack track, Effect effect) {
		selectEffectPage();
		effectSelectPage.onEffectDestroy(track, effect);
	}

	@Override
	public void onEffectOrderChange(BaseTrack track, int initialEffectPosition,
			int endEffectPosition) {
		selectEffectPage();
		effectSelectPage.onEffectOrderChange(track, initialEffectPosition, endEffectPosition);
	}
}
