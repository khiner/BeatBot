package com.kh.beatbot.ui.view.group;

import android.widget.Toast;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.page.Page;

public class ControlButtonGroup extends TouchableView {

	public ToggleButton playButton, recordButton, copyButton;
	public ImageButton stopButton, undoButton, deleteButton;

	@Override
	public void init() {

	}

	@Override
	public void draw() {
		// parent
	}

	@Override
	protected void createChildren() {
		playButton = new ToggleButton();
		stopButton = new ImageButton();
		recordButton = new ToggleButton();
		copyButton = new ToggleButton();
		deleteButton = new ImageButton();
		undoButton = new ImageButton();

		playButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				((ToggleButton) button).setChecked(true);
				PlaybackManager.play();
			}
		});

		recordButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				if (RecordManager.isRecording()) {
					// Managers.recordManager.stopListening();
					String fileName = RecordManager.stopRecording();

					Toast.makeText(
							BeatBotActivity.mainActivity
									.getApplicationContext(),
							"Recorded file to " + fileName, Toast.LENGTH_SHORT)
							.show();
				} else {
					Page.mainPage.midiView.reset();
					playButton.setChecked(true);
					RecordManager.startRecording();
					if (PlaybackManager.getState() != PlaybackManager.State.PLAYING)
						playButton.getOnReleaseListener().onRelease(playButton);
				}
			}
		});

		stopButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				playButton.setChecked(false);
				if (RecordManager.isRecording()) {
					recordButton.trigger(false);
					playButton.setChecked(false);
				}
				if (PlaybackManager.getState() == PlaybackManager.State.PLAYING) {
					playButton.setChecked(false);
					PlaybackManager.stop();
				}
			}
		});

		undoButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				MidiManager.undo();
			}
		});

		copyButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				String msg = null;
				if (((ToggleButton) button).isChecked()) {
					MidiManager.copy();
					msg = "Tap To Paste";
				} else {
					MidiManager.cancelCopy();
					msg = "Copy Cancelled";
				}
				Toast.makeText(
						BeatBotActivity.mainActivity.getApplicationContext(),
						msg, Toast.LENGTH_SHORT).show();
			}
		});

		deleteButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				MidiManager.deleteSelectedNotes();
			}
		});

		addChild(playButton);
		addChild(stopButton);
		addChild(recordButton);
		addChild(copyButton);
		addChild(deleteButton);
		addChild(undoButton);
	}

	public void setEditIconsEnabled(final boolean enabled) {
		deleteButton.setEnabled(enabled);
		copyButton.setEnabled(enabled);
	}

	public void setUndoIconEnabled(final boolean enabled) {
		undoButton.setEnabled(enabled);
	}
	
	public void uncheckCopyButton() {
		copyButton.setChecked(false);
	}

	@Override
	protected void loadIcons() {
		playButton.setIcon(new Icon(IconResources.PLAY));
		stopButton.setIcon(new Icon(IconResources.STOP));
		recordButton.setIcon(new Icon(IconResources.RECORD));
		copyButton.setIcon(new Icon(IconResources.COPY));
		deleteButton.setIcon(new Icon(IconResources.DELETE_NOTE));
		undoButton.setIcon(new Icon(IconResources.UNDO));

		setEditIconsEnabled(false);
	}

	@Override
	public void layoutChildren() {
		float leftMargin = Page.mainPage.midiTrackView.width;
		// left-aligned buttons
		playButton.layout(this, leftMargin, 0, height, height);
		stopButton.layout(this, leftMargin + height, 0, height, height);
		recordButton.layout(this, leftMargin + 2 * height, 0, height, height);

		float rightMargin = 10;
		// right-aligned buttons
		copyButton.layout(this, width - 3 * height - rightMargin, 0, height,
				height);
		undoButton.layout(this, width - 2 * height - rightMargin, 0, height,
				height);
		deleteButton.layout(this, width - height - rightMargin, 0, height,
				height);
	}
}