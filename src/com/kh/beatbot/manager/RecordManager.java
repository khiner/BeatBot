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

	public static enum State {
		OFF, LISTENING, ARMED, RECORDING
	};

	public static String GLOBAL_RECORD_SOURCE = "Global", MICROPHONE_RECORD_SOURCE = "Microphone";

	private static Context context = null;
	private static String currRecordFileName = null;
	private static State state = State.OFF;
	private static int currFileNum = 0;
	private static final List<String> RECORD_SOURCES = Arrays.asList(new String[] {
			GLOBAL_RECORD_SOURCE, MICROPHONE_RECORD_SOURCE });
	private static RecordStateListener listener;
	private static RecordSourceButtonListener recordSourceButtonListener;
	private static String currRecordSource = GLOBAL_RECORD_SOURCE;

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

	public synchronized static boolean isOff() {
		return state == State.OFF;
	}

	public synchronized static boolean isListening() {
		return state == State.LISTENING;
	}

	public synchronized static boolean isArmed() {
		return state == State.ARMED;
	}

	public synchronized static boolean isRecording() {
		return state == State.RECORDING;
	}

	// in LISTEN mode, recorder listens to (polls) the RecordSource to display the current level
	public synchronized static void startListening() {
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
	public synchronized static void arm() {
		if (!isListening())
			return;

		state = State.ARMED;
		armNative();
		if (listener != null) {
			listener.onRecordArmed();
		}
	}

	public synchronized static void disarm() {
		if (!isArmed())
			return;

		state = State.LISTENING;
		disarmNative();
		if (listener != null) {
			listener.onRecordDisarmed();
		}
	}

	public static String startRecording(FileManager fileManager) {
		if (!isArmed()) {
			return null;
		}
		
		String recordDirectory = fileManager.recordPathForSource(currRecordSource);
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

	public synchronized static File stopRecording() {
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

	public synchronized static void stopListening() {
		if (!isListening())
			return;

		stopListeningNative();
		state = State.OFF;

		if (listener != null) {
			listener.onListenStop();
		}
	}

	public static String[] getRecordSources() {
		return (String[]) RECORD_SOURCES.toArray();
	}

	public static void setRecordSource(final int recordSourceIndex) {
		currRecordSource = RECORD_SOURCES.get(recordSourceIndex);
		setRecordSourceNative(recordSourceIndex);
		if (currRecordSource.equals(MICROPHONE_RECORD_SOURCE)) {
			startListeningNative();
		}
	}

	public static void notifyRecordSourceBufferFilled(float recordSourceMaxFrame) {
		if (listener != null) {
			listener.onRecordSourceBufferFilled(recordSourceMaxFrame);
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