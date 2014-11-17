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
import com.kh.beatbot.midi.util.WavFileUtil;
import com.kh.beatbot.ui.view.control.Button;

public class RecordManager {
	static class RecordSourceListener implements OnReleaseListener {
		private AlertDialog selectRecordSourceAlert = null;

		public RecordSourceListener(final Context c) {
			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setTitle("Choose Effect");
			builder.setItems(getRecordSources(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					setRecordSource(item);
				}
			});
			selectRecordSourceAlert = builder.create();
		}

		@Override
		public void onRelease(Button button) {
			selectRecordSourceAlert.show();
		}
	}

	public static enum State {
		LISTENING, RECORDING, INITIALIZING
	};

	private static Context context = null;
	private static String currRecordFileName = null;
	private static State state = State.INITIALIZING;
	private static int currFileNum = 0;
	private static List<String> recordSources = Arrays.asList(new String[]{"Global", "Microphone"});
	private static RecordSourceListener recordSourceListener;

	public static void init(Context context) {
		RecordManager.context = context;
		recordSourceListener = new RecordSourceListener(context);
	}

	public static OnReleaseListener getRecordSourceListener() {
		return recordSourceListener;
	}

	public static boolean isRecording() {
		return state == State.RECORDING;
	}

	public static void startRecording() {
		if (isRecording())
			return;
		currRecordFileName = FileManager.beatRecordDirectory.getPath() + "/R" + (currFileNum++)
				+ ".wav";
		try {
			FileOutputStream out = WavFileUtil.writeWavFileHeader(currRecordFileName, 0, 0);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		startRecordingNative(currRecordFileName);
		state = State.RECORDING;
	}

	public static File stopRecording() {
		if (!isRecording())
			return null;
		stopRecordingNative();
		File file = WavFileUtil.insertLengthDataIntoWavFile(currRecordFileName);
		state = State.INITIALIZING;
		Toast.makeText(context, "Recorded file to " + currRecordFileName, Toast.LENGTH_SHORT)
				.show();
		return file;
	}

	public static String[] getRecordSources() {
		return (String[])recordSources.toArray();
	}

	public static void setRecordSource(int recordSourceIndex) {
		setRecordSourceNative(recordSourceIndex);
	}

	public static native void setRecordSourceNative(int recordSourceId);

	public static native void startRecordingNative(String recordFileName);

	public static native void stopRecordingNative();
}