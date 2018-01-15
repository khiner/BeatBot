package com.odang.beatbot.ui.view.page.main;

import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.effect.Effect.LevelType;
import com.odang.beatbot.listener.FileListener;
import com.odang.beatbot.listener.MidiNoteListener;
import com.odang.beatbot.listener.TrackLevelsEventListener;
import com.odang.beatbot.listener.TrackListener;
import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.view.MidiTrackView;
import com.odang.beatbot.ui.view.MidiView;
import com.odang.beatbot.ui.view.TouchableView;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.ViewPager;
import com.odang.beatbot.ui.view.group.ControlButtonGroup;
import com.odang.beatbot.ui.view.group.MidiViewGroup;
import com.odang.beatbot.ui.view.menu.MainMenu;

import java.io.File;

public class MainPage extends TouchableView implements MidiNoteListener, TrackListener,
        TrackLevelsEventListener, FileListener {
    public ControlButtonGroup controlButtonGroup;
    public ViewPager mainPageFlipper;
    public EditPage editPage;
    public EffectPage effectPage;
    public MainMenu slideMenu;

    public MainPage(View view) {
        super(view);
    }

    @Override
    protected synchronized void createChildren() {
        controlButtonGroup = new ControlButtonGroup(this);
        mainPageFlipper = new ViewPager(this);
        editPage = new EditPage(null);
        effectPage = new EffectPage(null);
        mainPageFlipper.addPage(editPage);
        mainPageFlipper.addPage(effectPage);
        controlButtonGroup.hideEffectToggle();
        slideMenu = new MainMenu(this, null);

        // ORDER IS IMPORTANT! (add child page after parent)
        context.getTrackManager().addTrackLevelsEventListener(this);
        context.getTrackManager().addTrackListener(this);
        context.getFileManager().addListener(this);

        context.getTrackManager().addTrackLevelsEventListener(editPage.pageSelectGroup);
        context.getTrackManager().addTrackListener(editPage.pageSelectGroup);
        context.getFileManager().addListener(editPage.pageSelectGroup);

        context.getMidiManager().addMidiNoteListener(this);
        context.getMidiManager().addMidiNoteListener(controlButtonGroup);
        context.getMidiManager().addMidiNoteListener(getMidiView());
    }

    @Override
    public void layoutChildren() {
        float controlButtonHeight = height / 10;
        View.BG_OFFSET = height / 150;
        mainPageFlipper.layout(this, 0, controlButtonHeight, width, height - controlButtonHeight);
        float trackControlWidth = getMidiViewGroup().getTrackControlWidth();
        controlButtonGroup.layout(this, trackControlWidth, 0, width - trackControlWidth,
                controlButtonHeight);
        slideMenu.layout(this, -width, 0, getMidiViewGroup().getTrackControlWidth(), height);
    }

    @Override
    public synchronized void drawAll() {
        controlButtonGroup.drawAll();
        mainPageFlipper.drawAll();
        renderGroup.draw();
        slideMenu.drawAll();
    }

    public MidiViewGroup getMidiViewGroup() {
        return editPage.midiViewGroup;
    }

    public MidiView getMidiView() {
        return getMidiViewGroup().midiView;
    }

    public MidiTrackView getMidiTrackView() {
        return getMidiViewGroup().midiTrackView;
    }

    public void expandMenu() {
        slideMenu.expand();
    }

    public Effect getCurrEffect() {
        return effectPage.getEffect();
    }

    public boolean effectIsShowing() {
        return mainPageFlipper.getCurrPage().equals(effectPage);
    }

    public void hideEffect() {
        mainPageFlipper.setPage(editPage);
        controlButtonGroup.hideEffectToggle();
    }

    public void selectEditPage() {
        if (effectIsShowing())
            hideEffect();
    }

    public void launchEffect(Effect effect) {
        if (!(effectIsShowing() && effect.equals(getCurrEffect()))) {
            mainPageFlipper.setPage(effectPage);
            effectPage.setEffect(effect);
        }
        controlButtonGroup.updateEffectToggle(effect);
    }

    @Override
    public void onCreate(MidiNote note) {
        selectEditPage();
    }

    @Override
    public void onDestroy(MidiNote note) {
        selectEditPage();
    }

    @Override
    public void onMove(MidiNote note, int beginNoteValue, long beginOnTick, long beginOffTick,
                       int endNoteValue, long endOnTick, long endOffTick) {
        selectEditPage();
    }

    @Override
    public void onSelectStateChange(MidiNote note) {
        selectEditPage();
    }

    @Override
    public void beforeLevelChange(MidiNote note) {
        // no-op
    }

    @Override
    public void onLevelChange(MidiNote note, LevelType type) {
        selectEditPage();
    }

    @Override
    public void onNameChange(File file, File newFile) {
        selectEditPage();
    }

    @Override
    public void onCreate(Track track) {
        selectEditPage();
    }

    @Override
    public void onDestroy(Track track) {
        selectEditPage();
    }

    @Override
    public void onSelect(BaseTrack track) {
        selectEditPage();
    }

    @Override
    public void onSampleChange(Track track) {
        selectEditPage();
    }

    @Override
    public void onMuteChange(Track track, boolean mute) {
        selectEditPage();
    }

    @Override
    public void onSoloChange(Track track, boolean solo) {
        selectEditPage();
    }

    @Override
    public void onReverseChange(Track track, boolean reverse) {
    }

    @Override
    public void onLoopChange(Track track, boolean loop) {
    }

    @Override
    public void onTrackLevelsChange(BaseTrack track) {
        selectEditPage();
    }

    @Override
    public void onSampleLoopWindowChange(Track track) {
        selectEditPage();
    }

    @Override
    public void onEffectCreate(BaseTrack track, Effect effect) {
        launchEffect(effect);
    }

    @Override
    public void onEffectDestroy(BaseTrack track, Effect effect) {
        selectEditPage();
    }

    @Override
    public void onEffectOrderChange(BaseTrack track, int initialEffectPosition,
                                    int endEffectPosition) {
        selectEditPage();
    }
}
