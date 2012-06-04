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
		sampleWaveformView.setSampleNum(sampleNum);
		String sampleName = getIntent().getExtras().getString("sampleName");
		loadSampleBytes(sampleName);
		playbackManager.armTrack(sampleNum);
		((ToggleButton)findViewById(R.id.loop_toggle)).setChecked(playbackManager.isLooping(sampleNum));
	}

	private void loadSampleBytes(String sampleName) {
		try {
			AssetFileDescriptor fd = getAssets().openFd(sampleName);
			byte[] sampleBytes = new byte[(int) fd.getLength()];
			// read in the sample bytes
			fd.createInputStream().read(sampleBytes);
			// set the view sample bytes
			((SampleWaveformView) findViewById(R.id.sample_waveform_view)).setSampleBytes(sampleBytes);			
		} catch (IOException e) {
			e.printStackTrace();
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
