package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.widget.Toast;

import com.kh.beatbot.midi.util.WavFileUtil;

public class RecordManager {
	public static enum State {
		LISTENING, RECORDING, INITIALIZING
	};

	private static Context context = null;
	private static String currRecordFileName = null;
	private static State state = State.INITIALIZING;
	private static int currFileNum = 0;

	public static void init(Context context) {
		RecordManager.context = context;
	}

	public static boolean isRecording() {
		return state == State.RECORDING;
	}

	public static void startRecording() {
		if (isRecording())
			return;
		updateFileNames();
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

	private static void updateFileNames() {
		currRecordFileName = FileManager.beatRecordDirectory.getPath() + "/R" + (currFileNum++)
				+ ".wav";
	}

	public static native void startRecordingNative(String recordFileName);

	public static native void stopRecordingNative();
}