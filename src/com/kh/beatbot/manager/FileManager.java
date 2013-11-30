package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.AssetManager;
import android.os.Environment;

import com.kh.beatbot.activity.BeatBotActivity;

public class FileManager {

	public static File rootDirectory, audioDirectory, midiDirectory,
			recordDirectory, drumsDirectory, beatRecordDirectory,
			sampleRecordDirectory;

	private static AssetManager assetManager;
	private static byte[] copyBuffer = new byte[1024];
	private static String appDirectoryPath;

	public static void init() {
		initDataDir();

		rootDirectory = new File("/");
		audioDirectory = new File(appDirectoryPath + "/audio");
		midiDirectory = new File(appDirectoryPath + "/midi");
		drumsDirectory = new File(audioDirectory.getPath() + "/drums");
		recordDirectory = new File(audioDirectory.getPath() + "/recorded");
		beatRecordDirectory = new File(recordDirectory.getPath() + "/beats");
		sampleRecordDirectory = new File(recordDirectory.getPath() + "/samples");

		copyAllSamplesToStorage();
	}

	public static void clearTempFiles() {
		clearTempFiles(audioDirectory);
	}

	private static void clearTempFiles(File directory) {
		for (File sampleFile : directory.listFiles()) {
			if (sampleFile.getAbsolutePath().contains(".raw")) {
				sampleFile.delete();
			}
		}
		for (File child : directory.listFiles()) {
			if (child.isDirectory()) {
				clearTempFiles(child);
			}
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

	private static void copyFile(InputStream in, OutputStream out)
			throws IOException {
		int read;
		while ((read = in.read(copyBuffer)) != -1) {
			out.write(copyBuffer, 0, read);
		}
		in.close();
		in = null;
		out.flush();
		out.close();
		out = null;
	}

	private static void copyFromAssetsToExternal(String newDirectoryPath) {
		File newDirectory = new File(newDirectoryPath);
		if (newDirectory.listFiles() == null
				|| newDirectory.listFiles().length > 0) {
			// only copy files into this dir if it is empty
			// files can be renamed, so we can't make assumptions
			// about whether an individual file already exists
			return;
		}

		// create the dir (we know it doesn't exist yet at this point)
		newDirectory.mkdirs();

		try {
			String assetPath = newDirectoryPath.replace(
					FileManager.audioDirectory.getPath(), "");
			assetPath = assetPath.substring(0, assetPath.length() - 1);
			for (String filePath : assetManager.list(assetPath)) {
				// copy audio file exactly from assets to sdcard
				InputStream in = assetManager.open(assetPath + "/" + filePath);
				FileOutputStream rawOut = new FileOutputStream(newDirectoryPath
						+ filePath);
				copyFile(in, rawOut);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void copyAllSamplesToStorage() {
		assetManager = BeatBotActivity.mainActivity.getAssets();

		for (File drumDirectory : FileManager.drumsDirectory.listFiles()) {
			// the sample folder for this sample type does not yet exist.
			// create it and write all assets of this type to the folder
			copyFromAssetsToExternal(drumDirectory.getPath());
		}
	}
}
