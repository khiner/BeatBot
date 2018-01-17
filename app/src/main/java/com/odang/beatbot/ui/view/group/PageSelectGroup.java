package com.odang.beatbot.ui.view.group;

import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.effect.Effect.LevelType;
import com.odang.beatbot.listener.FileListener;
import com.odang.beatbot.listener.OnReleaseListener;
import com.odang.beatbot.listener.PagerListener;
import com.odang.beatbot.listener.TempoListener;
import com.odang.beatbot.listener.TrackLevelsEventListener;
import com.odang.beatbot.listener.TrackListener;
import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.SwappingViewPager;
import com.odang.beatbot.ui.view.TouchableView;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.Button;
import com.odang.beatbot.ui.view.control.ToggleButton;
import com.odang.beatbot.ui.view.page.track.AdsrPage;
import com.odang.beatbot.ui.view.page.track.BrowsePage;
import com.odang.beatbot.ui.view.page.track.EffectSelectPage;
import com.odang.beatbot.ui.view.page.track.NoteLevelsPage;
import com.odang.beatbot.ui.view.page.track.RecordPage;
import com.odang.beatbot.ui.view.page.track.SampleEditPage;
import com.odang.beatbot.ui.view.page.track.TempoPage;
import com.odang.beatbot.ui.view.page.track.TrackLevelsPage;

import java.io.File;

public class PageSelectGroup extends TouchableView implements TrackListener,
        TrackLevelsEventListener, FileListener, PagerListener, TempoListener {

    private static final String TRACK_PAGE_ID = "track";

    private NoteLevelsPage noteLevelsPage;
    private TrackLevelsPage levelsPage;
    private EffectSelectPage effectSelectPage;
    private BrowsePage browsePage;
    private SampleEditPage sampleEditPage;
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
        return sampleEditPage;
    }

    public void selectEditPage() {
        ToggleButton editButton = trackButtonRow.getEditButton();
        if (!editButton.isChecked()) {
            editButton.trigger(true);
        }
    }

    public void selectTempoPage() {
        buttonRowPager.setPage(masterButton);
        ToggleButton tempoButton = masterButtonRow.getTempoButton();
        if (!tempoButton.isChecked()) {
            tempoButton.trigger(true);
        }
    }

    @Override
    protected void createChildren() {
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
        sampleEditPage = new SampleEditPage(pager);
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
        pager.addPage(trackButtonRow.getEditButton(), sampleEditPage);
        pager.addPage(trackButtonRow.getAdsrButton(), adsrPage);
        pager.addPage(trackButtonRow.getNoteLevelsButton(), noteLevelsPage);
        pager.addPage(trackButtonRow.getRecordButton(), recordPage);

        pager.addPage(masterButtonRow.getLevelsButton(), levelsPage);
        pager.addPage(masterButtonRow.getEffectsButton(), effectSelectPage);
        pager.addPage(masterButtonRow.getTempoButton(), tempoPage);

        masterButton.setText(context.getTrackManager().getMasterTrack().getFormattedName());
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
    public void layoutChildren() {
        float labelHeight = getLabelHeight();
        masterButton.layout(this, 0, BG_OFFSET, context.getMainPage().getMidiViewGroup()
                .getTrackControlWidth(), labelHeight);
        buttonRowPager.layout(this, masterButton.width, BG_OFFSET, width - masterButton.width,
                labelHeight);
        pager.layout(this, BG_OFFSET, labelHeight + BG_OFFSET, width - 2 * BG_OFFSET, height
                - labelHeight - 2 * BG_OFFSET);
    }

    @Override
    public void onCreate(Track track) {

    }

    @Override
    public void onDestroy(Track track) {
    }

    @Override
    public void onSelect(BaseTrack track) {
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
    public void onSampleChange(Track track) {
        ToggleButton browsePageButton = trackButtonRow.getBrowseButton();
        if (browsePageButton.isChecked()) {
            browsePage.onSampleChange(track);
        }
        ToggleButton sampleEditPageButton = trackButtonRow.getEditButton();
        if (sampleEditPageButton.isChecked()) {
            sampleEditPage.onSampleChange(track);
        }

        trackButtonRow.update();
    }

    @Override
    public void onMuteChange(Track track, boolean mute) {
    }

    @Override
    public void onSoloChange(Track track, boolean solo) {
    }

    @Override
    public void onReverseChange(Track track, boolean reverse) {
        selectEditPage();
        sampleEditPage.onReverseChange(track, reverse);
    }

    @Override
    public void onLoopChange(Track track, boolean loop) {
        selectEditPage();
        sampleEditPage.onLoopChange(track, loop);
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

    @Override
    public void onTempoChange(final float bpm) {
        selectTempoPage();
    }
}
