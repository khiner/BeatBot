package com.kh.beatbot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.view.SampleWaveformView;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.bean.MidiViewBean;

public class SampleEditActivity extends Activity implements LevelListener {
	private PlaybackManager playbackManager = null;
	private SampleWaveformView sampleWaveformView = null;
	//private EditLevelsView editLevelsView = null;
	private TronSeekbar volumeLevel, panLevel, pitchLevel;
	private int trackNum;
	private enum Effect {BITCRUSH, DELAY, FLANGER, FILTER, REVERB};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample_edit);
		playbackManager = GlobalVars.getPlaybackManager();
		trackNum = getIntent().getExtras().getInt("trackNum");
		sampleWaveformView = ((SampleWaveformView) findViewById(R.id.sample_waveform_view));
		//editLevelsView = ((EditLevelsView) findViewById(R.id.edit_levels_view));
		sampleWaveformView.setPlaybackManager(playbackManager);
		sampleWaveformView.setTrackNum(trackNum);
		//editLevelsView.setActivity(this);
		//editLevelsView.setTrackNum(trackNum);
		initLevels();
		// numSamples should be in shorts, so divide by two
		sampleWaveformView.setSamples(getSamples(trackNum));
		playbackManager.armTrack(trackNum);
		((ToggleButton) findViewById(R.id.loop_toggle))
				.setChecked(playbackManager.isLooping(trackNum));
	}

	public void toggleLoop(View view) {
		playbackManager.toggleLooping(trackNum);
	}

	public void toggleAdsr(View view) {
		boolean on = ((ToggleButton)view).isChecked();
		sampleWaveformView.setShowAdsr(on);
		setAdsrOn(trackNum, on);
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

	public void flanger(View view) {
		launchIntent(Effect.FLANGER);
	}
	
	public void filter(View view) {
		launchIntent(Effect.FILTER);
	}
	
	public void reverb(View view) {
		launchIntent(Effect.REVERB);
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
		case FLANGER:
			intent.setClass(this, FlangerActivity.class);
			break;
		case FILTER:
			intent.setClass(this, FilterActivity.class);
			break;
		case REVERB:
			intent.setClass(this, ReverbActivity.class);
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

	private void initLevels() {
		volumeLevel = ((TronSeekbar)findViewById(R.id.volumeLevel));
		panLevel = ((TronSeekbar)findViewById(R.id.panLevel));
		pitchLevel = ((TronSeekbar)findViewById(R.id.pitchLevel));
		volumeLevel.addLevelListener(this);
		panLevel.addLevelListener(this);
		pitchLevel.addLevelListener(this);
	}
	
	@Override
	public void setLevel(TronSeekbar levelBar, float level) {
		if (levelBar.equals(volumeLevel)) {
			setPrimaryVolume(trackNum, level);
		} else if (levelBar.equals(panLevel)) {
			setPrimaryPan(trackNum, level);			
		} else if (levelBar.equals(pitchLevel)) {
			setPrimaryPitch(trackNum, level);			
		}
	}
	
	@Override
	public void notifyInit(TronSeekbar levelBar) {
		if (levelBar.equals(volumeLevel)) {
			volumeLevel.setLevelColor(MidiViewBean.VOLUME_COLOR);
			volumeLevel.setViewLevel(getPrimaryVolume(trackNum));
		} else if (levelBar.equals(panLevel)) {
			panLevel.setLevelColor(MidiViewBean.PAN_COLOR);
			panLevel.setViewLevel(getPrimaryPan(trackNum));
		} else if (levelBar.equals(pitchLevel)) {
			pitchLevel.setLevelColor(MidiViewBean.PITCH_COLOR);
			pitchLevel.setViewLevel(getPrimaryPitch(trackNum));
		}
	}	
	
	@Override
	public void notifyChecked(TronSeekbar levelBar, boolean checked) {
		if (levelBar.equals(volumeLevel)) {
			((ToggleButton) findViewById(R.id.volumeView))
					.setChecked(checked);
		} else if (levelBar.equals(panLevel)) {
			((ToggleButton) findViewById(R.id.panView))
					.setChecked(checked);
		} else if (levelBar.equals(pitchLevel)) {
			((ToggleButton) findViewById(R.id.pitchView))
					.setChecked(checked);
		}	
	}
	
	// get the audio data in floats
	public native float[] getSamples(int trackNum);

	// reverse the sample from loop begin to loop end
	public native float[] reverse(int trackNum);

	// scale all samples so that the sample with the highest amplitude is at 1
	public native float[] normalize(int trackNum);
	
	public native float getPrimaryVolume(int trackNum);

	public native float getPrimaryPan(int trackNum);

	public native float getPrimaryPitch(int trackNum);

	public native void setPrimaryVolume(int trackNum, float volume);

	public native void setPrimaryPan(int trackNum, float pan);

	public native void setPrimaryPitch(int trackNum, float pitch);
	
	public native void setAdsrOn(int trackNum, boolean on);
}
