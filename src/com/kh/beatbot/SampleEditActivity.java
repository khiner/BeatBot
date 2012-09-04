package com.kh.beatbot;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.kh.beatbot.EffectActivity.EffectParam;
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
		public int labelAdded() {
			
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
					launchIntent(effectNames[item]);
				}
			});
			chooseEffectAlert = builder.create();
		}

		@Override
		public void labelListInitialized(LabelListListenable labelList) {
			this.labelList = labelList;
		}
		
		@Override
		public int labelAdded() {
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
	
	private SampleWaveformView sampleWaveformView = null;
	// private EditLevelsView editLevelsView = null;
	private TronSeekbar volumeLevel, panLevel, pitchLevel;

	private int trackNum;
	private static String[] effectNames;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sample_edit);
		effectNames = getResources().getStringArray(R.array.effect_names);
		((LabelListListenable)findViewById(R.id.effectList)).setListener(new EffectLabelListListener(this));
		((LabelListListenable)findViewById(R.id.sampleList)).setListener(new SampleLabelListListener(this));
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

	private void launchIntent(String effectName) {
		Intent intent = new Intent();
		if (effectName.equals(getString(R.string.decimate)))
			intent.setClass(this, DecimateActivity.class);
		else if (effectName.equals(getString(R.string.chorus)))
			intent.setClass(this, ChorusActivity.class);
		else if (effectName.equals(getString(R.string.delay)))
			intent.setClass(this, DelayActivity.class);
		else if (effectName.equals(getString(R.string.flanger)))
			intent.setClass(this, FlangerActivity.class);
		else if (effectName.equals(getString(R.string.filter)))
			intent.setClass(this, FilterActivity.class);
		else if (effectName.equals(getString(R.string.reverb)))
			intent.setClass(this, ReverbActivity.class);
		else if (effectName.equals(getString(R.string.tremelo)))
			intent.setClass(this, TremeloActivity.class);

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
