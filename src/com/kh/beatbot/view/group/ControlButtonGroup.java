package com.kh.beatbot.view.group;

import android.widget.Toast;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.view.BpmView;
import com.kh.beatbot.view.Button;
import com.kh.beatbot.view.ToggleButton;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;

public class ControlButtonGroup extends TouchableBBView {

	ToggleButton playButton, recordButton, copyButton, stopButton;
	Button deleteButton, undoButton, bpmTapButton;
	BpmView bpmView;

	public ControlButtonGroup(TouchableSurfaceView parent) {
		super(parent);
	}
	
	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public void draw() {
		// parent
	}

	@Override
	protected void createChildren() {
		playButton = new ToggleButton((TouchableSurfaceView) root);
		stopButton = new ToggleButton((TouchableSurfaceView) root);
		recordButton = new ToggleButton((TouchableSurfaceView) root);
		copyButton = new ToggleButton((TouchableSurfaceView) root);
		deleteButton = new Button((TouchableSurfaceView) root);
		undoButton = new Button((TouchableSurfaceView) root);
		bpmTapButton = new Button((TouchableSurfaceView) root);
		bpmView = new BpmView((TouchableSurfaceView) root);

		playButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				((ToggleButton) button).setOn(true);
				if (Managers.playbackManager.getState() == PlaybackManager.State.PLAYING) {
					Managers.playbackManager.reset();
					Managers.midiManager.reset();
				} else if (Managers.playbackManager.getState() == PlaybackManager.State.STOPPED) {
					Managers.playbackManager.play();
				}
			}
		});

		recordButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				if (Managers.recordManager.getState() != RecordManager.State.INITIALIZING) {
					// Managers.recordManager.stopListening();
					String fileName = Managers.recordManager
							.stopRecordingAndWriteWav();
					// make sure the recorded instrument shows the newly
					// recorded beat
					Managers.directoryManager.updateDirectories();

					Toast.makeText(
							GlobalVars.mainActivity.getApplicationContext(),
							"Recorded file to " + fileName, Toast.LENGTH_SHORT)
							.show();
				} else {
					GlobalVars.mainPage.midiView.reset();
					playButton.setOn(true);
					Managers.recordManager.startRecordingNative();
					if (Managers.playbackManager.getState() != PlaybackManager.State.PLAYING)
						playButton.getOnClickListener().onClick(playButton);
				}
			}
		});

		stopButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				playButton.setOn(false);
				stopButton.setOn(false);
				if (Managers.recordManager.getState() != RecordManager.State.INITIALIZING) {
					recordButton.setOn(false);
					playButton.getOnClickListener().onClick(playButton);
					recordButton.getOnClickListener().onClick(recordButton);
				}
				if (Managers.playbackManager.getState() == PlaybackManager.State.PLAYING) {
					playButton.setOn(false);
					Managers.playbackManager.stop();
					Managers.midiManager.reset();
				}
			}
		});

		undoButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				Managers.midiManager.undo();
			}
		});

		copyButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				String msg = null;
				if (((ToggleButton) button).isOn()) {
					Managers.midiManager.copy();
					msg = "Tap To Paste";
				} else {
					Managers.midiManager.cancelCopy();
					msg = "Copy Cancelled";
				}
				Toast.makeText(GlobalVars.mainActivity.getApplicationContext(),
						msg, Toast.LENGTH_SHORT).show();
			}
		});

		deleteButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				Managers.midiManager.deleteSelectedNotes();
			}
		});

		bpmTapButton.setOnClickListener(new BBOnClickListener() {
			private long lastTapTime = 0;

			@Override
			public void onClick(Button button) {
				long tapTime = System.currentTimeMillis();
				float millisElapsed = tapTime - lastTapTime;
				lastTapTime = tapTime;
				float bpm = 60000 / millisElapsed;
				bpmView.setText(String.valueOf(Managers.midiManager.setBPM(bpm)));
			}
		});
		
		addChild(playButton);
		addChild(stopButton);
		addChild(recordButton);
		addChild(copyButton);
		addChild(deleteButton);
		addChild(undoButton);
		addChild(bpmTapButton);
		addChild(bpmView);
	}
	
	public void setEditIconsEnabled(final boolean enabled) {
		deleteButton.setEnabled(enabled);
		copyButton.setEnabled(enabled);
	}

	public void uncheckCopyButton() {
		copyButton.setOn(false);
	}

	@Override
	public void layoutChildren() {
		// left-aligned buttons
		playButton.layout(this, 0, 0, height, height);
		stopButton.layout(this, height, 0, height, height);
		recordButton.layout(this, height * 2, 0, height, height);
		
		// right-aligned buttons
		bpmView.layout(this, width - 2 * height, 0, 2 * height, height);
		bpmTapButton.layout(this, width - 3 * height, 0, height, height);
		undoButton.layout(this, width - 4 * height, 0, height, height);
		deleteButton.layout(this, width - 5 * height, 0, height, height);
		copyButton.layout(this, width - 6 * height, 0, height, height);
	}

	@Override
	protected void loadIcons() {
		BBIconSource playButtonIcon = new BBIconSource(R.drawable.play_icon, R.drawable.play_icon_selected);
		BBIconSource stopButtonIcon = new BBIconSource(R.drawable.stop_icon, R.drawable.stop_icon_selected);
		BBIconSource recButtonIcon = new BBIconSource(R.drawable.rec_off_icon, R.drawable.rec_on_icon_selected);
		BBIconSource copyButtonIcon = new BBIconSource(R.drawable.copy_icon, R.drawable.copy_icon_selected);
		BBIconSource deleteButtonIcon = new BBIconSource(R.drawable.delete_icon, R.drawable.delete_icon_selected);
		BBIconSource undoButtonIcon = new BBIconSource(R.drawable.undo_icon, R.drawable.undo_icon_selected);
		BBIconSource bpmTapButtonIcon = new BBIconSource(R.drawable.bpm_tap_icon, R.drawable.bpm_tap_icon_selected);
		
		playButtonIcon.setPressedIcon(R.drawable.play_icon_pressed);
		recButtonIcon.setPressedIcon(R.drawable.rec_icon_pressed);
		copyButtonIcon.setDisabledIcon(R.drawable.copy_icon_inactive);
		deleteButtonIcon.setDisabledIcon(R.drawable.delete_icon_inactive);
		
		playButton.setIconSource(playButtonIcon);
		stopButton.setIconSource(stopButtonIcon);
		recordButton.setIconSource(recButtonIcon);
		copyButton.setIconSource(copyButtonIcon);
		deleteButton.setIconSource(deleteButtonIcon);
		undoButton.setIconSource(undoButtonIcon);
		bpmTapButton.setIconSource(bpmTapButtonIcon);
	}
}
