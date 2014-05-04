package com.kh.beatbot.ui.view.group;

import android.widget.Toast;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class ControlButtonGroup extends TouchableView {

	public ToggleButton playButton, recordButton, copyButton;
	public Button stopButton, undoButton, redoButton, deleteButton, quantizeButton;

	public ControlButtonGroup(RenderGroup renderGroup) {
		super(renderGroup);
	}

	@Override
	protected synchronized void createChildren() {
		playButton = new ToggleButton(renderGroup);
		stopButton = new Button(renderGroup);
		recordButton = new ToggleButton(renderGroup).oscillating();
		copyButton = new ToggleButton(renderGroup);
		deleteButton = new Button(renderGroup);
		quantizeButton = new Button(renderGroup);
		undoButton = new Button(renderGroup);
		redoButton = new Button(renderGroup);

		playButton.setIcon(IconResourceSets.PLAY);
		stopButton.setIcon(IconResourceSets.STOP);
		recordButton.setIcon(IconResourceSets.RECORD);
		copyButton.setIcon(IconResourceSets.COPY);
		deleteButton.setIcon(IconResourceSets.DELETE_NOTE);
		quantizeButton.setIcon(IconResourceSets.QUANTIZE);
		undoButton.setIcon(IconResourceSets.UNDO);
		redoButton.setIcon(IconResourceSets.REDO);

		playButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				PlaybackManager.play();
			}
		});

		recordButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				if (RecordManager.isRecording()) {
					// Managers.recordManager.stopListening();
					String fileName = RecordManager.stopRecording();

					Toast.makeText(BeatBotActivity.mainActivity.getApplicationContext(),
							"Recorded file to " + fileName, Toast.LENGTH_SHORT).show();
				} else {
					mainPage.midiViewGroup.midiView.reset();
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
				Toast.makeText(BeatBotActivity.mainActivity.getApplicationContext(), msg,
						Toast.LENGTH_SHORT).show();
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

		addChildren(playButton, stopButton, recordButton, copyButton, deleteButton, quantizeButton,
				undoButton, redoButton);

		setEditIconsEnabled(false);
		notifyMidiChange();
	}

	public void setEditIconsEnabled(final boolean enabled) {
		deleteButton.setEnabled(enabled);
		copyButton.setEnabled(enabled);
	}

	public void notifyMidiChange() {
		quantizeButton.setEnabled(!MidiManager.getMidiNotes().isEmpty());
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
	public synchronized void layoutChildren() {
		// left-aligned buttons
		playButton.layout(this, 0, 0, height, height);
		stopButton.layout(this, height, 0, height, height);
		recordButton.layout(this, 2 * height, 0, height, height);

		float rightMargin = 10;
		// right-aligned buttons
		quantizeButton.layout(this, width - 5 * height - rightMargin, 0, height, height);
		copyButton.layout(this, width - 4 * height - rightMargin, 0, height, height);
		undoButton.layout(this, width - 3 * height - rightMargin, 0, height, height);
		redoButton.layout(this, width - 2 * height - rightMargin, 0, height, height);
		deleteButton.layout(this, width - height - rightMargin, 0, height, height);
	}
}
