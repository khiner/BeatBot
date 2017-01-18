package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.RecordStateListener;
import com.kh.beatbot.midi.util.WavFileUtil;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;

public class RecordManager {
	static class RecordSourceButtonListener implements OnReleaseListener {
		final RecordManager recordManager;
		final AlertDialog.Builder builder;

		public RecordSourceButtonListener(final RecordManager recordManager, final Context context) {
			this.recordManager = recordManager;
			builder = new AlertDialog.Builder(context);
			builder.setTitle("Choose record source");
		}

		@Override
		public void onRelease(final Button button) {
			builder.setItems(recordManager.getRecordSourceLabels(),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							// first two are microphone and master track
							final int recordSourceId = item - 2;
							recordManager.setRecordSourceId(recordSourceId);
							if (button != null) {
								button.setText(recordManager.getRecordSourceLabel());
							}
						}
					});
			builder.create().show();
		}
	}

	public static enum State {
		OFF, LISTENING, ARMED, RECORDING
	};

	public static final int MICROPHONE_RECORD_SOURCE_ID = -2;
	public static final String MICROPHONE_RECORD_SOURCE_LABEL = "Microphone";

	private BeatBotActivity context;
	private int recordSourceId = TrackManager.MASTER_TRACK_ID;
	private String currRecordFileName = null;
	private State state = State.OFF;
	private int currFileNum = 0;

	private RecordStateListener listener;
	private RecordSourceButtonListener recordSourceButtonListener;

	public RecordManager(BeatBotActivity context) {
		this.context = context;
		recordSourceButtonListener = new RecordSourceButtonListener(this, context);
		context.onRecordManagerInit(this);
	}

	public void setListener(final RecordStateListener listener) {
		this.listener = listener;
	}

	public OnReleaseListener getRecordSourceButtonListener() {
		return recordSourceButtonListener;
	}

	public boolean isOff() {
		return state == State.OFF;
	}

	public boolean isListening() {
		return state == State.LISTENING;
	}

	public boolean isArmed() {
		return state == State.ARMED;
	}

	public boolean isRecording() {
		return state == State.RECORDING;
	}

	// in LISTEN mode, recorder listens to (polls) the RecordSource to display the current level
	public void startListening() {
		if (!isOff())
			return;

		// running timer task as daemon thread
		state = State.LISTENING;
		startListeningNative();

		if (listener != null) {
			listener.onListenStart();
		}
	}

	// in ARMED mode, recorder waits for RecordSource to exceed threshold before recording
	public void arm() {
		if (!isListening())
			return;

		state = State.ARMED;
		armNative();
		if (listener != null) {
			listener.onRecordArmed();
		}
	}

	public void disarm() {
		if (!isArmed())
			return;

		state = State.LISTENING;
		disarmNative();
		if (listener != null) {
			listener.onRecordDisarmed();
		}
	}

	public String startRecording() {
		if (!isArmed()) {
			return null;
		}

		String recordDirectory = View.context.getFileManager().recordPathForSource(recordSourceId);
		currRecordFileName = recordDirectory + "/R" + (currFileNum++) + ".wav";
		try {
			FileOutputStream out = WavFileUtil.writeWavFileHeader(currRecordFileName, 0, 0);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		state = State.RECORDING;
		if (listener != null) {
			listener.onRecordStart();
		}
		return currRecordFileName;
	}

	public File stopRecording() {
		if (isArmed()) {
			disarm();
			return null;
		}
		if (!isRecording()) {
			return null;
		}

		File file = WavFileUtil.insertLengthDataIntoWavFile(currRecordFileName);
		state = State.LISTENING;
		if (listener != null) {
			listener.onRecordStop(file);
		}
		Toast.makeText(context, "Recorded file to " + currRecordFileName, Toast.LENGTH_SHORT)
				.show();
		return file;
	}

	public void stopListening() {
		if (!isListening())
			return;

		stopListeningNative();
		state = State.OFF;

		if (listener != null) {
			listener.onListenStop();
		}
	}

	public void notifyRecordSourceBufferFilled(float recordSourceMaxFrame) {
		if (listener != null) {
			listener.onRecordSourceBufferFilled(recordSourceMaxFrame);
		}
	}

	public int getRecordSourceId() {
		return recordSourceId;
	}

	public void setRecordSourceId(final int recordSourceId) {
		this.recordSourceId = recordSourceId;
		setRecordSourceNative(recordSourceId);
		if (recordSourceId == MICROPHONE_RECORD_SOURCE_ID) {
			startListeningNative();
		}
	}

	public String[] getRecordSourceLabels() {
		final List<String> recordSourceLabels = new ArrayList<String>();
		recordSourceLabels.add(MICROPHONE_RECORD_SOURCE_LABEL);
		recordSourceLabels.add(BaseTrack.MASTER_TRACK_NAME);
		for (final Track track : context.getTrackManager().getTracks()) {
			recordSourceLabels.add(track.getFormattedName());
		}
		
		return recordSourceLabels.toArray(new String[recordSourceLabels.size()]);
	}

	public String getRecordSourceLabel() {
		return recordSourceId == MICROPHONE_RECORD_SOURCE_ID ? MICROPHONE_RECORD_SOURCE_LABEL
				: context.getTrackManager().getBaseTrackById(recordSourceId).getFormattedName();
	}

	public native void setThresholdLevel(float thresholdLevel);

	private native void setRecordSourceNative(int recordSourceId);

	private native void startListeningNative();

	private native void stopListeningNative();

	private native void stopRecordingNative();

	private native void armNative();

	private native void disarmNative();
}