package com.kh.beatbot.ui.view.group;

import android.widget.Toast;

import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.listener.MidiNoteListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.StatefulEventListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class ControlButtonGroup extends TouchableView implements MidiNoteListener,
		StatefulEventListener {

	private ToggleButton playButton, copyButton;
	private Button stopButton, undoButton, redoButton, deleteButton, quantizeButton;

	public ControlButtonGroup(View view) {
		super(view);
		EventManager.addListener(this);
	}

	@Override
	protected synchronized void createChildren() {
		playButton = new ToggleButton(this).withIcon(IconResourceSets.PLAY);
		stopButton = new Button(this).withIcon(IconResourceSets.STOP);
		copyButton = new ToggleButton(this).withIcon(IconResourceSets.COPY);
		deleteButton = new Button(this).withIcon(IconResourceSets.DELETE_NOTE);
		quantizeButton = new Button(this).withIcon(IconResourceSets.QUANTIZE);
		undoButton = new Button(this).withIcon(IconResourceSets.UNDO);
		redoButton = new Button(this).withIcon(IconResourceSets.REDO);

		playButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				PlaybackManager.play();
			}
		});

		stopButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				playButton.setChecked(false);
				if (RecordManager.isRecording()) {
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
				onEventCompleted(null);
			}
		});

		redoButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				EventManager.redo();
				onEventCompleted(null);
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
				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
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

		setEditIconsEnabled(false);
		undoButton.setEnabled(false);
		redoButton.setEnabled(false);
		quantizeButton.setEnabled(false);
	}

	@Override
	public synchronized void layoutChildren() {
		// left-aligned buttons
		playButton.layout(this, 0, 0, height, height);
		stopButton.layout(this, height, 0, height, height);

		float rightMargin = 10;
		// right-aligned buttons
		quantizeButton.layout(this, width - 5 * height - rightMargin, 0, height, height);
		copyButton.layout(this, width - 4 * height - rightMargin, 0, height, height);
		undoButton.layout(this, width - 3 * height - rightMargin, 0, height, height);
		redoButton.layout(this, width - 2 * height - rightMargin, 0, height, height);
		deleteButton.layout(this, width - height - rightMargin, 0, height, height);
	}

	@Override
	public void onCreate(MidiNote note) {
		quantizeButton.setEnabled(true);
	}

	@Override
	public void onDestroy(MidiNote note) {
		quantizeButton.setEnabled(TrackManager.anyNotes());
		onSelectStateChange(note);
	}

	@Override
	public void onMove(MidiNote note, int beginNoteValue, long beginOnTick, long beginOffTick,
			int endNoteValue, long endOnTick, long endOffTick) {
		// no-op
	}

	@Override
	public void onSelectStateChange(MidiNote note) {
		setEditIconsEnabled(TrackManager.anyNoteSelected());
	}

	private void setEditIconsEnabled(final boolean enabled) {
		deleteButton.setEnabled(enabled);
		copyButton.setEnabled(enabled);
	}

	private void updateStateStackIcons(boolean hasUndo, boolean hasRedo) {
		undoButton.setEnabled(hasUndo);
		redoButton.setEnabled(hasRedo);
	}

	@Override
	public void onEventCompleted(Stateful event) {
		updateStateStackIcons(EventManager.canUndo(), EventManager.canRedo());
	}
}
