package com.kh.beatbot;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ToggleButton;

import com.kh.beatbot.EffectActivity.EffectParam;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.view.SampleWaveformView;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.bean.MidiViewBean;

public class SampleEditActivity extends Activity implements LevelListener {
	private SampleWaveformView sampleWaveformView = null;
	// private EditLevelsView editLevelsView = null;
	private TronSeekbar volumeLevel, panLevel, pitchLevel;

	private enum Effect {
		BITCRUSH, CHORUS, DELAY, FLANGER, FILTER, REVERB, TREMELO
	};

	private int trackNum;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sample_edit);
		trackNum = getIntent().getExtras().getInt("trackNum");
		sampleWaveformView = ((SampleWaveformView) findViewById(R.id.sample_waveform_view));
		// editLevelsView = ((EditLevelsView)
		// findViewById(R.id.edit_levels_view));
		sampleWaveformView.setTrackNum(trackNum);
		// editLevelsView.setActivity(this);
		// editLevelsView.setTrackNum(trackNum);
		initLevels();
		// numSamples should be in shorts, so divide by two
		sampleWaveformView.setSamples(getSamples(trackNum));
		Managers.playbackManager.armTrack(trackNum);
		((ToggleButton) findViewById(R.id.loop_toggle))
				.setChecked(Managers.playbackManager.isLooping(trackNum));
	}

	public static void quantizeEffectParams() {
		for (int trackNum = 0; trackNum < GlobalVars.params.length; trackNum++) {
			for (int effectNum = 0; effectNum < GlobalVars.NUM_EFFECTS; effectNum++) {
				List<EffectParam> params = GlobalVars.params[trackNum][effectNum];
				for (int paramNum = 0; paramNum < params.size(); paramNum++) {
					EffectParam param = params.get(paramNum);
					if (param.beatSync) {
						EffectActivity.setParamLevel(param, param.viewLevel);
						EffectActivity.setParamNative(paramNum, param.level);
					}
				}
			}
		}
	}

	public void toggleLoop(View view) {
		Managers.playbackManager.toggleLooping(trackNum);
	}

	public void toggleAdsr(View view) {
		boolean on = ((ToggleButton) view).isChecked();
		sampleWaveformView.setShowAdsr(on);
		setAdsrOn(trackNum, on);
	}

	public void reverse(View view) {
		setReverse(trackNum, ((ToggleButton) view).isChecked());
	}

	public void normalize(View view) {
		sampleWaveformView.setSamples(normalize(trackNum));
	}

	public void bitcrush(View view) {
		launchIntent(Effect.BITCRUSH);
	}

	public void chorus(View view) {
		launchIntent(Effect.CHORUS);
	}

	public void delay(View view) {
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

	public void tremelo(View view) {
		launchIntent(Effect.TREMELO);
	}

	private void launchIntent(Effect effect) {
		Intent intent = new Intent();
		switch (effect) {
		case BITCRUSH:
			intent.setClass(this, DecimateActivity.class);
			break;
		case CHORUS:
			intent.setClass(this, ChorusActivity.class);
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
		case TREMELO:
			intent.setClass(this, TremeloActivity.class);
		}
		intent.putExtra("trackNum", trackNum);
		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (Managers.playbackManager.getState() != PlaybackManager.State.PLAYING)
			// if not currently playing, disarm the track
			Managers.playbackManager.disarmTrack(trackNum);
	}

	private void initLevels() {
		volumeLevel = ((TronSeekbar) findViewById(R.id.volumeLevel));
		panLevel = ((TronSeekbar) findViewById(R.id.panLevel));
		pitchLevel = ((TronSeekbar) findViewById(R.id.pitchLevel));
		volumeLevel.addLevelListener(this);
		panLevel.addLevelListener(this);
		pitchLevel.addLevelListener(this);
	}

	@Override
	public void setLevel(LevelListenable levelBar, float level) {
		if (levelBar.equals(volumeLevel)) {
			setPrimaryVolume(trackNum, level);
			GlobalVars.trackVolume[trackNum] = level;
		} else if (levelBar.equals(panLevel)) {
			setPrimaryPan(trackNum, level);
			GlobalVars.trackPan[trackNum] = level;
		} else if (levelBar.equals(pitchLevel)) {
			setPrimaryPitch(trackNum, level);
			GlobalVars.trackPitch[trackNum] = level;
		}
	}

	@Override
	public void notifyInit(LevelListenable levelBar) {
		if (levelBar.equals(volumeLevel)) {
			volumeLevel.setLevelColor(MidiViewBean.VOLUME_COLOR);
			volumeLevel.setLevel(GlobalVars.trackVolume[trackNum]);
		} else if (levelBar.equals(panLevel)) {
			panLevel.setLevelColor(MidiViewBean.PAN_COLOR);
			panLevel.setLevel(GlobalVars.trackPan[trackNum]);
		} else if (levelBar.equals(pitchLevel)) {
			pitchLevel.setLevelColor(MidiViewBean.PITCH_COLOR);
			pitchLevel.setLevel(GlobalVars.trackPitch[trackNum]);
		}
	}

	@Override
	public void notifyPressed(LevelListenable levelBar, boolean pressed) {
		if (levelBar.equals(volumeLevel)) {
			((ToggleButton) findViewById(R.id.volumeView)).setChecked(pressed);
		} else if (levelBar.equals(panLevel)) {
			((ToggleButton) findViewById(R.id.panView)).setChecked(pressed);
		} else if (levelBar.equals(pitchLevel)) {
			((ToggleButton) findViewById(R.id.pitchView)).setChecked(pressed);
		}
	}

	@Override
	public void notifyClicked(LevelListenable levelListenable) {
		// do nothing when levels are clicked
	}

	// get the audio data in floats
	public native float[] getSamples(int trackNum);

	// set play mode to reverse
	public native void setReverse(int trackNum, boolean reverse);

	// scale all samples so that the sample with the highest amplitude is at 1
	public native float[] normalize(int trackNum);

	public native void setPrimaryVolume(int trackNum, float volume);

	public native void setPrimaryPan(int trackNum, float pan);

	public native void setPrimaryPitch(int trackNum, float pitch);

	public native void setAdsrOn(int trackNum, boolean on);

	@Override
	public void setLevel(LevelListenable levelListenable, float levelX,
			float levelY) {
		// for 2d seekbar. nothing to do
	}
}
