package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.RecordStateListener;
import com.kh.beatbot.midi.util.WavFileUtil;
import com.kh.beatbot.ui.view.control.Button;

public class RecordManager {
	static class RecordSourceButtonListener implements OnReleaseListener {
		private AlertDialog selectRecordSourceAlert = null;
		private Button button;

		public RecordSourceButtonListener(final Context c) {
			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setTitle("Choose record source");
			builder.setItems(getRecordSources(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					setRecordSource(item);
					if (button != null) {
						button.setText(currRecordSource);
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

	static class RecordSourceAudioListener extends TimerTask {
		@Override
		public void run() {
			currentLevel = getMaxFrameInRecordSourceBuffer();
			if (currentLevel > thresholdLevel) {
				startRecording();
			}
		}
	}

	public static enum State {
		LISTENING, RECORDING, INITIALIZING
	};

	public static String GLOBAL_RECORD_SOURCE = "Global", MICROPHONE_RECORD_SOURCE = "Microphone";

	private static Context context = null;
	private static String currRecordFileName = null;
	private static State state = State.INITIALIZING;
	private static int currFileNum = 0;
	private static final List<String> RECORD_SOURCES = Arrays.asList(new String[] {
			GLOBAL_RECORD_SOURCE, MICROPHONE_RECORD_SOURCE });
	private static RecordStateListener listener;
	private static RecordSourceButtonListener recordSourceButtonListener;
	private static TimerTask thresholdListenerTask;
	private static Timer timer = new Timer(true);
	private static String currRecordSource = GLOBAL_RECORD_SOURCE;
	private static float currentLevel = 0, thresholdLevel = 0;

	public static void init(Context context) {
		RecordManager.context = context;
		recordSourceButtonListener = new RecordSourceButtonListener(context);
	}

	public static void setListener(RecordStateListener listener) {
		RecordManager.listener = listener;
	}

	public static OnReleaseListener getRecordSourceButtonListener() {
		return recordSourceButtonListener;
	}

	public synchronized static boolean isListening() {
		return state == State.LISTENING;
	}

	public synchronized static boolean isRecording() {
		return state == State.RECORDING;
	}

	public static void setThresholdLevel(float thresholdLevel) {
		RecordManager.thresholdLevel = thresholdLevel;
	}

	public static float getCurrentLevel() {
		return currentLevel;
	}

	// in LISTEN mode, recorder waits for RecordSource to exceed threshold before recording
	public synchronized static void startListening() {
		if (isListening())
			return;

		// running timer task as daemon thread
		thresholdListenerTask = new RecordSourceAudioListener();
		timer.scheduleAtFixedRate(thresholdListenerTask, 100, 100); // poll threshold every 0.1 sec
		state = State.LISTENING;

		if (listener != null) {
			listener.onListenStart();
		}
	}

	public synchronized static void stopListening() {
		if (!isListening())
			return;

		thresholdListenerTask.cancel();
		state = State.INITIALIZING;
		if (listener != null) {
			listener.onListenStop();
		}
	}

	public synchronized static void startRecording() {
		if (isRecording())
			return;
		if (isListening()) {
			thresholdListenerTask.cancel();
		}

		String recordDirectory = FileManager.recordPathForSource(currRecordSource);
		currRecordFileName = recordDirectory + "/R" + (currFileNum++) + ".wav";
		try {
			FileOutputStream out = WavFileUtil.writeWavFileHeader(currRecordFileName, 0, 0);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		startRecordingNative(currRecordFileName);
		state = State.RECORDING;
		if (listener != null) {
			listener.onRecordStart();
		}
	}

	public synchronized static File stopRecording() {
		if (isListening()) {
			stopListening();
			return null;
		}
		if (!isRecording()) {
			return null;
		}
		stopRecordingNative();
		File file = WavFileUtil.insertLengthDataIntoWavFile(currRecordFileName);
		state = State.INITIALIZING;
		if (listener != null) {
			listener.onRecordStop(file);
		}
		Toast.makeText(context, "Recorded file to " + currRecordFileName, Toast.LENGTH_SHORT)
				.show();
		return file;
	}

	public static String[] getRecordSources() {
		return (String[]) RECORD_SOURCES.toArray();
	}

	public static void setRecordSource(final int recordSourceIndex) {
		currRecordSource = RECORD_SOURCES.get(recordSourceIndex);
		setRecordSourceNative(recordSourceIndex);
	}

	public static native float getMaxFrameInRecordSourceBuffer();

	public static native void setRecordSourceNative(int recordSourceId);

	public static native void startRecordingNative(String recordFileName);

	public static native void stopRecordingNative();
}