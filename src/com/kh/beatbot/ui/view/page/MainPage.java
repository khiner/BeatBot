package com.kh.beatbot.ui.view.page;

import java.io.File;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.listener.FileListener;
import com.kh.beatbot.listener.MidiNoteListener;
import com.kh.beatbot.listener.TrackLevelsEventListener;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.view.MidiTrackView;
import com.kh.beatbot.ui.view.MidiView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.ViewFlipper;
import com.kh.beatbot.ui.view.group.ControlButtonGroup;
import com.kh.beatbot.ui.view.group.EditGroup;
import com.kh.beatbot.ui.view.group.MidiViewGroup;
import com.kh.beatbot.ui.view.group.PageSelectGroup;
import com.kh.beatbot.ui.view.menu.MainMenu;
import com.kh.beatbot.ui.view.page.effect.EffectPage;

public class MainPage extends TouchableView implements MidiNoteListener, TrackListener,
		TrackLevelsEventListener, FileListener {
	public ControlButtonGroup controlButtonGroup;
	public ViewFlipper mainContentFlipper;
	public EditGroup editGroup;
	public EffectPage effectPage;
	public MainMenu slideMenu;

	public MainPage(View view) {
		super(view);
	}

	@Override
	protected synchronized void createChildren() {
		controlButtonGroup = new ControlButtonGroup(this);
		mainContentFlipper = new ViewFlipper(this);
		editGroup = new EditGroup(null);
		effectPage = new EffectPage(null);
		mainContentFlipper.addPage(editGroup);
		mainContentFlipper.addPage(effectPage);
		mainContentFlipper.setPage(editGroup);
		controlButtonGroup.hideEffectToggle();
		slideMenu = new MainMenu(this, null);

		// ORDER IS IMPORTANT! (add child page after parent)
		TrackManager.addTrackLevelsEventListener(this);
		TrackManager.addTrackListener(this);
		FileManager.addListener(this);

		TrackManager.addTrackLevelsEventListener(getPageSelectGroup());
		TrackManager.addTrackListener(getPageSelectGroup());
		FileManager.addListener(getPageSelectGroup());
		
		MidiManager.addMidiNoteListener(this);
		MidiManager.addMidiNoteListener(controlButtonGroup);
		MidiManager.addMidiNoteListener(getMidiView());
	}

	@Override
	public synchronized void layoutChildren() {
		float controlButtonHeight = height / 10;
		View.LABEL_HEIGHT = height / 12;
		View.BG_OFFSET = height / 180;
		mainContentFlipper
				.layout(this, 0, controlButtonHeight, width, height - controlButtonHeight);
		float trackControlWidth = getMidiViewGroup().getTrackControlWidth();
		controlButtonGroup.layout(this, trackControlWidth, 0, width - trackControlWidth,
				controlButtonHeight);
		slideMenu.layout(this, -width, 0, getMidiViewGroup().getTrackControlWidth(), height);
	}

	@Override
	public synchronized void drawAll() {
		controlButtonGroup.drawAll();
		mainContentFlipper.drawAll();
		renderGroup.draw();
		slideMenu.drawAll();
	}

	public MidiViewGroup getMidiViewGroup() {
		return editGroup.midiViewGroup;
	}

	public PageSelectGroup getPageSelectGroup() {
		return editGroup.pageSelectGroup;
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
		return mainContentFlipper.getCurrPage().equals(effectPage);
	}

	public void hideEffect() {
		mainContentFlipper.setPage(editGroup);
		controlButtonGroup.hideEffectToggle();
	}

	public void selectEditPage() {
		if (effectIsShowing())
			hideEffect();
	}

	public void launchEffect(Effect effect) {
		mainContentFlipper.setPage(effectPage);
		effectPage.setEffect(effect);
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
	public void onLevelChanged(MidiNote note, LevelType type) {
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
	public void onTrackLevelsChange(BaseTrack track) {
		selectEditPage();
	}

	@Override
	public void onSampleLoopWindowChange(Track track) {
		selectEditPage();
	}
}
