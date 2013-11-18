package com.kh.beatbot.ui.view.group;

import android.widget.Toast;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.event.EventManager;
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
	public ImageButton stopButton, undoButton, redoButton, deleteButton,
			quantizeButton;

	@Override
	protected synchronized void createChildren() {
		playButton = new ToggleButton();
		stopButton = new ImageButton();
		recordButton = new ToggleButton();
		copyButton = new ToggleButton();
		deleteButton = new ImageButton();
		quantizeButton = new ImageButton();
		undoButton = new ImageButton();
		redoButton = new ImageButton();

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
				EventManager.undo();
			}
		});

		redoButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				EventManager.redo();
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

		quantizeButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				MidiManager.quantize(); // TODO bring back quantize list (1/4,
										// 1/8, 1/16, 1/32)
			}
		});

		addChildren(playButton, stopButton, recordButton, copyButton,
				deleteButton, quantizeButton, undoButton, redoButton);
	}

	public void setEditIconsEnabled(final boolean enabled) {
		deleteButton.setEnabled(enabled);
		copyButton.setEnabled(enabled);
	}

	public void setQuantizeIconEnabled(final boolean enabled) {
		quantizeButton.setEnabled(enabled);
	}

	public void setUndoIconEnabled(final boolean enabled) {
		undoButton.setEnabled(enabled);
	}

	public void setRedoIconEnabled(final boolean enabled) {
		redoButton.setEnabled(enabled);
	}

	public void uncheckCopyButton() {
		copyButton.setChecked(false);
	}

	@Override
	protected synchronized void loadIcons() {
		playButton.setIcon(new Icon(IconResources.PLAY));
		stopButton.setIcon(new Icon(IconResources.STOP));
		recordButton.setIcon(new Icon(IconResources.RECORD));
		copyButton.setIcon(new Icon(IconResources.COPY));
		deleteButton.setIcon(new Icon(IconResources.DELETE_NOTE));
		quantizeButton.setIcon(new Icon(IconResources.QUANTIZE));
		undoButton.setIcon(new Icon(IconResources.UNDO));
		redoButton.setIcon(new Icon(IconResources.REDO));

		setEditIconsEnabled(false);
		setQuantizeIconEnabled(false);
	}

	@Override
	public synchronized void layoutChildren() {
		// left-aligned buttons
		playButton.layout(this, 0, 0, height, height);
		stopButton.layout(this, height, 0, height, height);
		recordButton.layout(this, 2 * height, 0, height, height);

		float rightMargin = 10;
		// right-aligned buttons
		quantizeButton.layout(this, width - 5 * height - rightMargin, 0,
				height, height);
		copyButton.layout(this, width - 4 * height - rightMargin, 0, height,
				height);
		undoButton.layout(this, width - 3 * height - rightMargin, 0, height,
				height);
		redoButton.layout(this, width - 2 * height - rightMargin, 0, height,
				height);
		deleteButton.layout(this, width - height - rightMargin, 0, height,
				height);
	}
}
