package com.kh.beatbot;

import java.util.Collections;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listenable.LabelListListenable;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.view.SampleWaveformView;
import com.kh.beatbot.view.TronSeekbar;

public class SampleEditActivity extends Activity implements LevelListener {
	class SampleLabelListListener implements LabelListListener {
		SampleLabelListListener(Context c) {

		}

		@Override
		public void labelListInitialized(LabelListListenable labelList) {

		}

		@Override
		public void labelMoved(int id, int oldPosition, int newPosition) {

		}

		@Override
		public void labelClicked(String text, int id, int position) {

		}

		@Override
		public void labelLongClicked(int id, int position) {

		}
	}

	class EffectLabelListListener implements LabelListListener {
		private AlertDialog selectEffectAlert = null;
		private LabelListListenable labelList;
		private int lastClickedId = -1;
		private int lastClickedPos = -1;

		EffectLabelListListener(Context c) {
			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setTitle("Choose Effect");
			builder.setItems(effectNames,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							if (!effectNames[item].equals("NONE")) {
								labelList.setLabelText(lastClickedId,
										effectNames[item]);
								labelList.setLabelOn(lastClickedId, true);
								launchEffectIntent(effectNames[item],
										lastClickedId, lastClickedPos);
							} else {
								labelList.setLabelText(lastClickedId, "");
								Effect effect = GlobalVars.tracks.get(trackNum)
										.findEffectById(lastClickedId);
								if (effect != null) {
									effect.removeEffect();
								}
							}
						}
					});
			selectEffectAlert = builder.create();
		}

		private Stack<Integer> getUniqueIds() {
			Stack<Integer> uniqueIds = new Stack<Integer>();
			for (int id = 0; id < 4; id++) {
				if (GlobalVars.tracks.get(trackNum).findEffectById(id) == null)
					uniqueIds.add(id);
			}
			return uniqueIds;
		}

		@Override
		public void labelListInitialized(LabelListListenable labelList) {
			this.labelList = labelList;
			Stack<Integer> uniqueIds = getUniqueIds();
			if (labelList.noLabels()) {
				for (int i = 0; i < 4; i++) {
					Effect effect = GlobalVars.tracks.get(trackNum)
							.findEffectByPosition(i);
					if (effect != null) {
						this.labelList.addLabel(effect.name, effect.getId(),
								effect.on);
					} else {
						this.labelList.addLabel("", uniqueIds.pop(), false);
					}
				}
			} else {
				for (int i = 0; i < 4; i++) {
					Effect effect = GlobalVars.tracks.get(trackNum)
							.findEffectByPosition(i);
					if (effect != null)
						this.labelList.setLabelOn(effect.getId(), effect.on);
				}
			}
		}

		@Override
		public void labelMoved(int id, int oldPosition, int newPosition) {
			Effect effect = GlobalVars.tracks.get(trackNum).findEffectById(id);
			if (effect != null) {
				effect.setPosition(newPosition);
				for (Effect other : GlobalVars.tracks.get(trackNum).effects) {
					if (other.equals(effect))
						continue;
					if (other.getPosition() >= newPosition
							&& other.getPosition() < oldPosition) {
						other.incPosition();
					}
				}
				Collections.sort(GlobalVars.tracks.get(trackNum).effects);
			}
		}

		@Override
		public void labelClicked(String text, int id, int position) {
			lastClickedId = id;
			lastClickedPos = position;
			if (text.isEmpty()) {
				selectEffectAlert.show();
			} else {
				launchEffectIntent(text, id, position);
			}
		}

		@Override
		public void labelLongClicked(int id, int position) {
			lastClickedId = id;
			lastClickedPos = position;
			selectEffectAlert.show();
		}
	}

	private static AlertDialog selectInstrumentAlert = null;
	private AlertDialog selectSampleAlert = null;

	private static SampleWaveformView sampleWaveformView = null;
	// private EditLevelsView editLevelsView = null;
	private static TronSeekbar volumeLevel, panLevel, pitchLevel;
	private static LabelListListenable effectLabelList = null,
			sampleLabelList = null;

	private int trackNum, currInstrumentNum;

	private static String[] effectNames;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		trackNum = currInstrumentNum = getIntent().getExtras().getInt(
				"trackNum");
		// remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sample_edit);
		// set text font
		((TextView) findViewById(R.id.effectsLabel))
				.setTypeface(GlobalVars.font);
		((Button) findViewById(R.id.sampleSelect)).setTypeface(GlobalVars.font);
		// init alerts
		if (selectInstrumentAlert == null) {
			// only one, static list of instruments
			initSelectInstrumentAlert();
		}
		initSelectSampleAlert();
		// set the instrument icon
		((ImageButton) findViewById(R.id.instrumentButton))
				.setBackgroundResource(GlobalVars.tracks.get(currInstrumentNum).instrumentIcon);
		// set the instrument text
		((Button) findViewById(R.id.sampleSelect)).setText(GlobalVars.tracks
				.get(currInstrumentNum).sampleNames[0]);
		initLevels();
		initSampleLabelList();
		initEffectLabelList();
		initSampleWaveformView();

		Managers.playbackManager.armTrack(trackNum);
		((ToggleButton) findViewById(R.id.loop_toggle))
				.setChecked(Managers.playbackManager.isLooping(trackNum));
	}

	private void setSample(int instrumentNum, int sampleNum) {
		setSampleBytes(trackNum, GlobalVars.tracks.get(instrumentNum)
				.getSampleBytes(sampleNum));
		((Button) findViewById(R.id.sampleSelect)).setText(GlobalVars.tracks
				.get(instrumentNum).sampleNames[sampleNum]);
		sampleWaveformView.setSamples(getSamples(trackNum));
	}

	private void initSelectInstrumentAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose Instrument");
		builder.setItems(GlobalVars.currentInstruments.toArray(new String[GlobalVars.currentInstruments.size()]),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						setSample(item, 0);
						currInstrumentNum = item;
						// update instrument icon to reflect the change
						((ImageButton) findViewById(R.id.instrumentButton))
								.setBackgroundResource(GlobalVars.tracks
										.get(currInstrumentNum).instrumentIcon);
						// update the sample select alert names with the new
						// instrument samples
						initSelectSampleAlert();
					}
				});
		selectInstrumentAlert = builder.create();
	}

	private void initSelectSampleAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose Sample");
		builder.setItems(GlobalVars.tracks.get(currInstrumentNum).sampleNames,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						setSample(currInstrumentNum, item);
					}
				});
		selectSampleAlert = builder.create();
	}

	private void initEffectLabelList() {
		effectNames = getResources().getStringArray(R.array.effect_names);
		effectLabelList = (LabelListListenable) findViewById(R.id.effectList);
		effectLabelList.setListener(new EffectLabelListListener(this));
	}

	private void initSampleLabelList() {
		// sampleLabelList = (LabelListListenable)
		// findViewById(R.id.sampleList);
		// sampleLabelList.setListener(new SampleLabelListListener(this));
	}

	private void initSampleWaveformView() {
		sampleWaveformView = ((SampleWaveformView) findViewById(R.id.sample_waveform_view));
		sampleWaveformView.setTrackNum(trackNum);
		sampleWaveformView.setSamples(getSamples(trackNum));
	}

	public static void quantizeEffectParams() {
		for (int trackNum = 0; trackNum < GlobalVars.tracks.size(); trackNum++) {
			for (Effect effect : GlobalVars.tracks.get(trackNum).effects) {
				for (int paramNum = 0; paramNum < effect.getNumParams(); paramNum++) {
					EffectParam param = effect.getParam(paramNum);
					if (param.beatSync) {
						effect.setParamLevel(param, param.viewLevel);
						effect.setEffectParam(trackNum, effect.getId(),
								paramNum, param.level);
					}
				}
			}
		}
	}

	public void selectInstrument(View view) {
		selectInstrumentAlert.show();
	}

	public void selectSample(View view) {
		selectSampleAlert.show();
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

	private Effect getEffect(String effectName, int id, int position) {
		Effect effect = GlobalVars.tracks.get(trackNum).findEffectById(id);
		if (effect != null)
			return effect;
		if (effectName.equals(getString(R.string.decimate)))
			effect = new Decimate(id, effectName, trackNum, position);
		else if (effectName.equals(getString(R.string.chorus)))
			effect = new Chorus(id, effectName, trackNum, position);
		else if (effectName.equals(getString(R.string.delay)))
			effect = new Delay(id, effectName, trackNum, position);
		else if (effectName.equals(getString(R.string.flanger)))
			effect = new Flanger(id, effectName, trackNum, position);
		else if (effectName.equals(getString(R.string.filter)))
			effect = new Filter(id, effectName, trackNum, position);
		else if (effectName.equals(getString(R.string.reverb)))
			effect = new Reverb(id, effectName, trackNum, position);
		else if (effectName.equals(getString(R.string.tremelo)))
			effect = new Tremelo(id, effectName, trackNum, position);
		GlobalVars.tracks.get(trackNum).effects.add(effect);
		return effect;
	}

	private void launchEffectIntent(String effectName, int effectId,
			int effectPosition) {
		Effect effect = getEffect(effectName, effectId, effectPosition);
		if (effectName != effect.name) {
			// different effect being added to the effect slot. need to replace
			// it
			effect.removeEffect();
			effect = getEffect(effectName, effectId, effectPosition);
		}
		Intent intent = new Intent();
		intent.setClass(this, EffectActivity.class);
		intent.putExtra("effectId", effect.getId());
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
			GlobalVars.tracks.get(trackNum).volume = level;
		} else if (levelBar.equals(panLevel)) {
			setPrimaryPan(trackNum, level);
			GlobalVars.tracks.get(trackNum).pan = level;
		} else if (levelBar.equals(pitchLevel)) {
			setPrimaryPitch(trackNum, level);
			GlobalVars.tracks.get(trackNum).pitch = level;
		}
	}

	@Override
	public void notifyInit(LevelListenable levelBar) {
		if (levelBar.equals(volumeLevel)) {
			volumeLevel.setLevelColor(Colors.VOLUME_COLOR);
			volumeLevel.setLevel(GlobalVars.tracks.get(trackNum).volume);
		} else if (levelBar.equals(panLevel)) {
			panLevel.setLevelColor(Colors.PAN_COLOR);
			panLevel.setLevel(GlobalVars.tracks.get(trackNum).pan);
		} else if (levelBar.equals(pitchLevel)) {
			pitchLevel.setLevelColor(Colors.PITCH_COLOR);
			pitchLevel.setLevel(GlobalVars.tracks.get(trackNum).pitch);
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

	public static native void setSampleBytes(int trackNum, byte[] bytes);

	@Override
	public void setLevel(LevelListenable levelListenable, float levelX,
			float levelY) {
		// for 2d seekbar. nothing to do
	}
}
