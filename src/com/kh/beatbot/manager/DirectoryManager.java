package com.kh.beatbot.manager;

import java.io.File;

import android.os.Environment;

public class DirectoryManager {
	public static String appDirectoryPath;

	public static File audioDirectory, midiDirectory, recordDirectory,
			beatRecordDirectory, drumsDirectory;

	public static void init() {
		initDataDir();
		audioDirectory = new File(appDirectoryPath + "audio");
		midiDirectory = new File(appDirectoryPath + "midi");
		drumsDirectory = new File(audioDirectory.getPath() + "/drums");

		recordDirectory = new File(audioDirectory.getPath() + "/recorded");
		beatRecordDirectory = new File(recordDirectory.getPath() + "/beats");
	}

	public static void clearTempFiles() {
		clearTempFiles(audioDirectory);
	}

	public static void clearTempFiles(File directory) {
		for (File sampleFile : directory.listFiles()) {
			if (sampleFile.getAbsolutePath().contains(".raw")) {
				sampleFile.delete();
			}
		}
		for (File child : directory.listFiles()) {
			clearTempFiles(child);
		}
	}

	private static void initDataDir() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// we can read and write to external storage
			String extStorageDir = Environment.getExternalStorageDirectory()
					.toString();
			appDirectoryPath = extStorageDir + "/BeatBot/";
		} else { // we need read AND write access for this app - default to
					// internal storage
			// appDirectoryPath = getFilesDir().toString() + "/";
			// TODO throw / catch exception - need External SD Card!
		}
	}
}
