package com.kh.beatbot.manager;

import java.io.File;

import android.os.Environment;

public class AudioClassificationManager {
	private static AudioClassificationManager instance = null;
	String FEATURE_VAL_FOLDER = "BeatBot/Feature_Values/featureValues.txt";
	String FEATURE_DEF_FOLDER = "BeatBot/Feature_Definitions/featureDefs";

	public static AudioClassificationManager getInstance() {
		if (instance == null) {
			instance = new AudioClassificationManager();
		}
		return instance;
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
