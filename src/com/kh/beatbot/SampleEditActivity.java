package com.kh.beatbot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ToggleButton;

import com.kh.beatbot.effect.Chorus;
import com.kh.beatbot.effect.Decimate;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Effect.EffectParam;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.effect.Tremelo;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listenable.LabelListListenable;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.view.SampleWaveformView;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.bean.MidiViewBean;

public class SampleEditActivity extends Activity implements LevelListener {
	class SampleLabelListListener implements LabelListListener {
		SampleLabelListListener(Context c) {
			
		}

		@Override
		public void labelListInitialized(LabelListListenable labelList) {
			
		}
		
		@Override
		public int labelAdded(int labelNum) {
			
			return 0;
		}

		@Override
		public void labelRemoved(int id) {

		}

		@Override
		public void labelMoved(int id, int oldPosition, int newPosition) {

		}

		@Override
		public void labelSelected(int id) {
			
		}
	}

	class EffectLabelListListener implements LabelListListener {
		private AlertDialog chooseEffectAlert = null;
		private LabelListListenable labelList;
		
		EffectLabelListListener(Context c) {
			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setTitle("Choose Effect");
			builder.setItems(effectNames, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					labelList.addLabel(effectNames[item], item);
					launchEffectIntent(effectNames[item]);
				}
			});
			chooseEffectAlert = builder.create();
		}

		@Override
		public void labelListInitialized(LabelListListenable labelList) {
			this.labelList = labelList;
		}
		
		@Override
		public int labelAdded(int labelNum) {
			chooseEffectAlert.show();
			return 0;
		}

		@Override
		public void labelRemoved(int id) {

		}

		@Override
		public void labelMoved(int id, int oldPosition, int newPosition) {
		}

		@Override
		public void labelSelected(int id) {

		}
	}
	
	private static SampleWaveformView sampleWaveformView = null;
	// private EditLevelsView editLevelsView = null;
	private static TronSeekbar volumeLevel, panLevel, pitchLevel;
	private static LabelListListenable effectLabelList = null, sampleLabelList = null;
	
	private int trackNum;
	private static String[] effectNames;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		trackNum = getIntent().getExtras().getInt("trackNum");
		// remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sample_edit);
		initLevels();
		initSampleLabelList();
		initEffectLabelList();
		initSampleWaveformView();
		
		Managers.playbackManager.armTrack(trackNum);
		((ToggleButton) findViewById(R.id.loop_toggle))
				.setChecked(Managers.playbackManager.isLooping(trackNum));
	}

	private void initEffectLabelList() {
		effectNames = getResources().getStringArray(R.array.effect_names);
		effectLabelList = (LabelListListenable)findViewById(R.id.effectList); 
		effectLabelList.setListener(new EffectLabelListListener(this));
	}
	
	private void initSampleLabelList() {
		sampleLabelList = (LabelListListenable)findViewById(R.id.sampleList); 
		sampleLabelList.setListener(new SampleLabelListListener(this));
	}
	
	private void initSampleWaveformView() {
		sampleWaveformView = ((SampleWaveformView) findViewById(R.id.sample_waveform_view));
		sampleWaveformView.setTrackNum(trackNum);
		// numSamples should be in shorts, so divide by two
		sampleWaveformView.setSamples(getSamples(trackNum));
	}
	
	public static void quantizeEffectParams() {
		for (int trackNum = 0; trackNum < GlobalVars.params.length; trackNum++) {
			for (Effect effect : GlobalVars.effects[trackNum]) {
				for (int paramNum = 0; paramNum < effect.getNumParams(); paramNum++) { 
					EffectParam param = effect.getParam(paramNum);
					if (param.beatSync) {
						effect.setParamLevel(param, param.viewLevel);
						effect.setParamNative(paramNum, param.viewLevel);
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

	private void launchEffectIntent(String effectName) {
		Intent intent = new Intent();
		intent.setClass(this, EffectActivity.class);
		Effect effect = null;
		if (effectName.equals(getString(R.string.decimate)))
			effect = new Decimate(effectName, trackNum);
		else if (effectName.equals(getString(R.string.chorus)))
			effect = new Chorus(effectName, trackNum);
		else if (effectName.equals(getString(R.string.delay)))
			effect = new Delay(effectName, trackNum);
		else if (effectName.equals(getString(R.string.flanger)))
			effect = new Flanger(effectName, trackNum);
		else if (effectName.equals(getString(R.string.filter)))
			effect = new Filter(effectName, trackNum);
		else if (effectName.equals(getString(R.string.reverb)))
			effect = new Reverb(effectName, trackNum);
		else if (effectName.equals(getString(R.string.tremelo)))
			effect = new Tremelo(effectName, trackNum);
		
		intent.putExtra("effectNum", effect.effectNum);
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
	public static native float[] getSamples(int trackNum);

	// set play mode to reverse
	public static native void setReverse(int trackNum, boolean reverse);

	// scale all samples so that the sample with the highest amplitude is at 1
	public static native float[] normalize(int trackNum);

	public static native void setPrimaryVolume(int trackNum, float volume);

	public static native void setPrimaryPan(int trackNum, float pan);

	public static native void setPrimaryPitch(int trackNum, float pitch);

	public static native void setAdsrOn(int trackNum, boolean on);

	@Override
	public void setLevel(LevelListenable levelListenable, float levelX,
			float levelY) {
		// for 2d seekbar. nothing to do
	}
}
