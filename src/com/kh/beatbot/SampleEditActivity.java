package com.kh.beatbot;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.view.EditLevelsView;
import com.kh.beatbot.view.SampleWaveformView;


public class SampleEditActivity extends Activity {
	private PlaybackManager playbackManager = null;
	private SampleWaveformView sampleWaveformView = null;
	private EditLevelsView editLevelsView = null;
	private int sampleNum;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample_edit);
		GlobalVars gv = (GlobalVars)getApplicationContext();
		playbackManager = gv.getPlaybackManager();
		sampleNum = getIntent().getExtras().getInt("sampleNum");
		sampleWaveformView = ((SampleWaveformView)findViewById(R.id.sample_waveform_view));
		editLevelsView = ((EditLevelsView)findViewById(R.id.edit_levels_view));
		sampleWaveformView.setPlaybackManager(playbackManager);
		sampleWaveformView.setTrackNum(sampleNum);
		editLevelsView.setActivity(this);
		editLevelsView.setTrackNum(sampleNum);
		// numSamples should be in shorts, so divide by two
		sampleWaveformView.setSamples(getSamples(sampleNum));
		playbackManager.armTrack(sampleNum);
		((ToggleButton)findViewById(R.id.loop_toggle)).setChecked(playbackManager.isLooping(sampleNum));
	}

	public void toggleLoop(View view) {
		playbackManager.toggleLooping(sampleNum);
	}
	
	public void reverse(View view) {
		sampleWaveformView.setSamples(reverse(sampleNum));
	}
	
	public void normalize(View view) {
		sampleWaveformView.setSamples(normalize(sampleNum));
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (playbackManager.getState() != PlaybackManager.State.PLAYING)
			// if not currently playing, disarm the track
			playbackManager.disarmTrack(sampleNum);
	}	
	
	// get the audio data in floats
	public native float[] getSamples(int trackNum);
	// reverse the sample from loop begin to loop end
	public native float[] reverse(int trackNum);
	// scale all samples so that the sample with the highest amplitude is at 1
	public native float[] normalize(int trackNum);
}
