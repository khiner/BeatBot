package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.RecordStateListener;
import com.kh.beatbot.midi.util.WavFileUtil;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;

public class RecordManager {
	static class RecordSourceButtonListener implements OnReleaseListener {
		private AlertDialog selectRecordSourceAlert = null;
		private Button button;

		public RecordSourceButtonListener(final RecordManager recordManager, final Context c) {
			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setTitle("Choose record source");
			builder.setItems(getRecordSources(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					recordManager.setRecordSource(item);
					if (button != null) {
						button.setText(recordManager.recordSource);
					}
				}
			});
			selectRecordSourceAlert = builder.create();
		}

		@Override
		public void onRelease(Button button) {
			this.button = button;
			selectRecordSourceAlert.show();
		}
	}

	public static enum State {
		OFF, LISTENING, ARMED, RECORDING
	};

	public static String GLOBAL_RECORD_SOURCE = "Global", MICROPHONE_RECORD_SOURCE = "Microphone";
	private static final List<String> RECORD_SOURCES = Arrays.asList(new String[] {
			GLOBAL_RECORD_SOURCE, MICROPHONE_RECORD_SOURCE });

	private Context context;
	private String recordSource = GLOBAL_RECORD_SOURCE;
	private String currRecordFileName = null;
	private State state = State.OFF;
	private int currFileNum = 0;

	private RecordStateListener listener;
	private RecordSourceButtonListener recordSourceButtonListener;

	public RecordManager(Context context) {
		this.context = context;
		recordSourceButtonListener = new RecordSourceButtonListener(this, context);
	}

	public void setListener(final RecordStateListener listener) {
		this.listener = listener;
	}

	public OnReleaseListener getRecordSourceButtonListener() {
		return recordSourceButtonListener;
	}

	public synchronized boolean isOff() {
		return state == State.OFF;
	}

	public synchronized boolean isListening() {
		return state == State.LISTENING;
	}

	public synchronized boolean isArmed() {
		return state == State.ARMED;
	}

	public synchronized boolean isRecording() {
		return state == State.RECORDING;
	}

	// in LISTEN mode, recorder listens to (polls) the RecordSource to display the current level
	public synchronized void startListening() {
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
	public synchronized void arm() {
		if (!isListening())
			return;

		state = State.ARMED;
		armNative();
		if (listener != null) {
			listener.onRecordArmed();
		}
	}

	public synchronized void disarm() {
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
		
		String recordDirectory = View.context.getFileManager().recordPathForSource(recordSource);
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

	public synchronized File stopRecording() {
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

	public synchronized void stopListening() {
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

	public static String[] getRecordSources() {
		return (String[]) RECORD_SOURCES.toArray();
	}

	public String getRecordSource() {
		return recordSource;
	}

	public void setRecordSource(final int recordSourceIndex) {
		recordSource = RECORD_SOURCES.get(recordSourceIndex);
		setRecordSourceNative(recordSourceIndex);
		if (recordSource.equals(MICROPHONE_RECORD_SOURCE)) {
			startListeningNative();
		}
	}

	public static native void setThresholdLevel(float thresholdLevel);

	public static native void setRecordSourceNative(int recordSourceId);

	public static native void startListeningNative();

	public static native void stopListeningNative();

	public static native void stopRecordingNative();

	public static native void armNative();

	public static native void disarmNative();
}