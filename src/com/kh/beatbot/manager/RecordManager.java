package com.kh.beatbot.manager;

import java.io.FileOutputStream;
import java.io.IOException;

import com.kh.beatbot.midi.util.WavFileUtil;

public class RecordManager {
	public static enum State {
		LISTENING, RECORDING, INITIALIZING
	};

	private static String currRecordFileName = null;
	private static State state = State.INITIALIZING;
	private static int currFileNum = 0;

	public static boolean isRecording() {
		return state == State.RECORDING;
	}

	public static void startRecording() {
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

	public static String stopRecording() {
		stopRecordingNative();
		WavFileUtil.insertLengthDataIntoWavFile(currRecordFileName);
		state = State.INITIALIZING;
		return currRecordFileName;
	}

	private static void updateFileNames() {
		currRecordFileName = FileManager.beatRecordDirectory.getPath() + "/R" + (currFileNum++)
				+ ".wav";
	}

	public static native void startRecordingNative(String recordFileName);

	public static native void stopRecordingNative();
}