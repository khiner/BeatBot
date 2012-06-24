package com.kh.beatbot;

import android.app.Activity;
import android.content.Intent;
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
	private int trackNum;
	private enum Effect {BITCRUSH, DELAY, FILTER};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample_edit);
		playbackManager = GlobalVars.getPlaybackManager();
		trackNum = getIntent().getExtras().getInt("trackNum");
		sampleWaveformView = ((SampleWaveformView) findViewById(R.id.sample_waveform_view));
		editLevelsView = ((EditLevelsView) findViewById(R.id.edit_levels_view));
		sampleWaveformView.setPlaybackManager(playbackManager);
		sampleWaveformView.setTrackNum(trackNum);
		editLevelsView.setActivity(this);
		editLevelsView.setTrackNum(trackNum);
		// numSamples should be in shorts, so divide by two
		sampleWaveformView.setSamples(getSamples(trackNum));
		playbackManager.armTrack(trackNum);
		((ToggleButton) findViewById(R.id.loop_toggle))
				.setChecked(playbackManager.isLooping(trackNum));
	}

	public void toggleLoop(View view) {
		playbackManager.toggleLooping(trackNum);
	}

	public void reverse(View view) {
		sampleWaveformView.setSamples(reverse(trackNum));
	}

	public void normalize(View view) {
		sampleWaveformView.setSamples(normalize(trackNum));
	}

	public void bitcrush(View view) {
		launchIntent(Effect.BITCRUSH);
	}

	public void delay(View veiw) {
		launchIntent(Effect.DELAY);
	}

	public void filter(View view) {
		launchIntent(Effect.FILTER);
	}

	private void launchIntent(Effect effect) {
		Intent intent = new Intent();		
		switch (effect) {
		case BITCRUSH:
			intent.setClass(this, DecimateActivity.class);
			break;
		case DELAY:
			intent.setClass(this, DelayActivity.class);
			break;
		case FILTER:
			intent.setClass(this, FilterActivity.class);
			break;
		}
		intent.putExtra("trackNum", trackNum);
		startActivity(intent);		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (playbackManager.getState() != PlaybackManager.State.PLAYING)
			// if not currently playing, disarm the track
			playbackManager.disarmTrack(trackNum);
	}

	// get the audio data in floats
	public native float[] getSamples(int trackNum);

	// reverse the sample from loop begin to loop end
	public native float[] reverse(int trackNum);

	// scale all samples so that the sample with the highest amplitude is at 1
	public native float[] normalize(int trackNum);
}
