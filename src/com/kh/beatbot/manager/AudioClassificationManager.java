package com.kh.beatbot.manager;

import java.io.File;

import android.os.Environment;

public class AudioClassificationManager {
	String FEATURE_VAL_FOLDER = "BeatBot/Feature_Values/featureValues.txt";
	String FEATURE_DEF_FOLDER = "BeatBot/Feature_Definitions/featureDefs";

	public AudioClassificationManager() {
	}

	public void extractFeatures(File rawInputFile) {
	}
	
	private String createSavePath(String saveFolder) {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, saveFolder);

		if (!file.exists()) {
			file.mkdirs();
		}
		file.delete();
		return file.getAbsolutePath();
	}

}
