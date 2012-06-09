package com.kh.beatbot;

import java.io.IOException;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.view.SampleWaveformView;


public class SampleEditActivity extends Activity {
	private PlaybackManager playbackManager = null;
	private SampleWaveformView sampleWaveformView = null;	
	private int sampleNum;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample_edit);
		GlobalVars gv = (GlobalVars)getApplicationContext();
		playbackManager = gv.getPlaybackManager();
		sampleNum = getIntent().getExtras().getInt("sampleNum");
		sampleWaveformView = ((SampleWaveformView)findViewById(R.id.sample_waveform_view));
		sampleWaveformView.setPlaybackManager(playbackManager);
		sampleWaveformView.setTrackNum(sampleNum);
		String sampleName = getIntent().getExtras().getString("sampleName");
		// numSamples should be in shorts, so divide by two
		sampleWaveformView.setSampleBytes(loadSampleBytes(sampleName));
		playbackManager.armTrack(sampleNum);
		((ToggleButton)findViewById(R.id.loop_toggle)).setChecked(playbackManager.isLooping(sampleNum));
	}

	private byte[] loadSampleBytes(String sampleName) {
		try {
			AssetFileDescriptor fd = getAssets().openFd(sampleName);
			// the first 44 bytes of a .wav file are header - skip them
			byte[] sampleBytes = new byte[(int) fd.getLength() - 44];
			fd.createInputStream().read(sampleBytes, 44, sampleBytes.length - 44);
			return sampleBytes;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void toggleLoop(View view) {
		playbackManager.toggleLooping(sampleNum);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (playbackManager.getState() != PlaybackManager.State.PLAYING)
			// if not currently playing, disarm the track
			playbackManager.disarmTrack(sampleNum);
	}		
}
