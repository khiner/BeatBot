package com.kh.beatbot.manager;

import jAudioFeatureExtractor.Aggregators.AggregatorContainer;
import jAudioFeatureExtractor.AudioFeatures.Compactness;
import jAudioFeatureExtractor.AudioFeatures.Derivative;
import jAudioFeatureExtractor.AudioFeatures.FeatureExtractor;
import jAudioFeatureExtractor.AudioFeatures.Mean;
import jAudioFeatureExtractor.AudioFeatures.RMS;
import jAudioFeatureExtractor.AudioFeatures.SpectralCentroid;
import jAudioFeatureExtractor.AudioFeatures.SpectralFlux;
import jAudioFeatureExtractor.AudioFeatures.SpectralRolloffPoint;
import jAudioFeatureExtractor.AudioFeatures.SpectralVariability;
import jAudioFeatureExtractor.AudioFeatures.StandardDeviation;
import jAudioFeatureExtractor.AudioFeatures.StrongestFrequencyViaFFTMax;
import jAudioFeatureExtractor.AudioFeatures.StrongestFrequencyViaSpectralCentroid;
import jAudioFeatureExtractor.AudioFeatures.StrongestFrequencyViaZeroCrossings;
import jAudioFeatureExtractor.AudioFeatures.ZeroCrossings;
import jAudioFeatureExtractor.jAudioTools.FeatureProcessor;

import java.io.File;
import java.io.FileOutputStream;

import android.os.Environment;

public class AudioClassificationManager {
	FeatureProcessor featureProcessor;
	FeatureExtractor[] features;
	String FEATURE_VAL_FOLDER = "BeatBot/Feature_Values/featureValues.xml";
	String FEATURE_DEF_FOLDER = "BeatBot/Feature_Definitions/featureDefs";

	public AudioClassificationManager() {
		features = new FeatureExtractor[24];
		features[0] = new SpectralCentroid();
		features[1] = new StandardDeviation().defineFeature(features[0]);
		features[2] = new SpectralRolloffPoint();
		features[3] = new StandardDeviation().defineFeature(features[2]);
		features[4] = new SpectralFlux();
		features[5] = new StandardDeviation().defineFeature(features[4]);
		features[6] = new Compactness();
		features[7] = new StandardDeviation().defineFeature(features[6]);
		features[8] = new SpectralVariability();
		features[9] = new StandardDeviation().defineFeature(features[8]);
		features[10] = new RMS();
		features[11] = new StandardDeviation().defineFeature(features[10]);
		features[12] = new Derivative().defineFeature(new Mean()
				.defineFeature(features[10]));
		features[13] = new Derivative().defineFeature(features[11]);
		features[14] = new ZeroCrossings();
		features[15] = new StandardDeviation().defineFeature(features[14]);
		features[16] = new Derivative().defineFeature(new Mean()
				.defineFeature(features[14]));
		features[17] = new Derivative().defineFeature(features[15]);
		features[18] = new StrongestFrequencyViaZeroCrossings();
		features[19] = new StandardDeviation().defineFeature(features[18]);
		features[20] = new StrongestFrequencyViaSpectralCentroid();
		features[21] = new StandardDeviation().defineFeature(features[20]);
		features[22] = new StrongestFrequencyViaFFTMax();
		features[23] = new StandardDeviation().defineFeature(features[22]);

		boolean[] features_to_save = new boolean[24];
		for (int i = 0; i < features_to_save.length; i++) {
			features_to_save[i] = true;
		}
		try {
			featureProcessor = new FeatureProcessor(512, 0, 44100, true,
					features, features_to_save, false, true,
					new FileOutputStream(createSavePath(FEATURE_VAL_FOLDER)),
					new FileOutputStream(createSavePath(FEATURE_DEF_FOLDER)),
					0, new AggregatorContainer());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void extractFeatures(File rawInputFile) {
		try {
			featureProcessor.extractFeatures(rawInputFile);
		} catch(Exception e) {
			e.printStackTrace();
		}
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
