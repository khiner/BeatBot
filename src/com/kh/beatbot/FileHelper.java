package com.kh.beatbot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.AssetManager;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.manager.DirectoryManager;

public class FileHelper {

	private static AssetManager assetManager;
	
	private static byte[] copyBuffer = new byte[1024];
	
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
					DirectoryManager.getAudioPath(), "");
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

	public static void copyAllSamplesToStorage() {
		assetManager = BeatBotActivity.mainActivity.getAssets();
		for (int i = 0; i < DirectoryManager.drumNames.length; i++) {
			String drumPath = DirectoryManager.getDrumInstrument(i).getPath();
			// the sample folder for this sample type does not yet exist.
			// create it and write all assets of this type to the folder
			copyFromAssetsToExternal(drumPath);
		}
	}

}
