package com.kh.beatbot.activity;

import java.util.Collections;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
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
import com.kh.beatbot.global.GeneralUtils;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Track;
import com.kh.beatbot.listenable.LabelListListenable;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.view.TronSeekbar;

public class SampleEditActivity extends Activity implements LevelListener {
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
								Effect effect = currTrack
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
				if (currTrack.findEffectById(id) == null)
					uniqueIds.add(id);
			}
			return uniqueIds;
		}

		@Override
		public void labelListInitialized(LabelListListenable labelList) {
			this.labelList = labelList;
			Stack<Integer> uniqueIds = getUniqueIds();
			if (labelList.noLabels()) {
				for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
					Effect effect = currTrack.findEffectByPosition(i);
					if (effect != null) {
						this.labelList.addLabel(effect.name, effect.getId(),
								effect.on);
					} else {
						this.labelList.addLabel("", uniqueIds.pop(), false);
					}
				}
			} else {
				for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
					Effect effect = currTrack.findEffectByPosition(i);
					if (effect != null)
						this.labelList.setLabelOn(effect.getId(), effect.on);
				}
			}
		}

		@Override
		public void labelMoved(int id, int oldPosition, int newPosition) {
			Effect effect = currTrack.findEffectById(id);
			if (effect != null) {
				effect.setPosition(newPosition);
				for (Effect other : currTrack.effects) {
					if (other.equals(effect))
						continue;
					if (other.getPosition() >= newPosition
							&& other.getPosition() < oldPosition) {
						other.incPosition();
					}
				}
				Collections.sort(currTrack.effects);
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

	// private EditLevelsView editLevelsView = null;
	private static TronSeekbar volumeLevel, panLevel, pitchLevel;
	private static LabelListListenable effectLabelList = null,
			sampleLabelList = null;

	private static Track currTrack;

	private static String[] effectNames;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int trackId = getIntent().getExtras().getInt("trackId");
		currTrack = GlobalVars.tracks.get(trackId);
		GeneralUtils.initAndroidSettings(this);
		setContentView(R.layout.sample_edit);
		// set text font
		((TextView) findViewById(R.id.effectsLabel))
				.setTypeface(GlobalVars.font);
		initLevels();
		initSampleLabelList();
		initEffectLabelList();

		currTrack.arm();
		((ToggleButton) findViewById(R.id.loop_toggle)).setChecked(currTrack.isLooping());
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

	public static void quantizeEffectParams() {
		for (int trackId = 0; trackId < GlobalVars.tracks.size(); trackId++) {
			for (Effect effect : GlobalVars.tracks.get(trackId).effects) {
				for (int paramNum = 0; paramNum < effect.getNumParams(); paramNum++) {
					EffectParam param = effect.getParam(paramNum);
					if (param.beatSync) {
						effect.setParamLevel(param, param.viewLevel);
						effect.setEffectParam(trackId, effect.getId(),
								paramNum, param.level);
					}
				}
			}
		}
	}

	// public void normalize(View view) {
	// sampleWaveformView.setSamples(normalize(currTrack.getId()));
	// }

	private Effect getEffect(String effectName, int id, int position) {
		Effect effect = currTrack.findEffectById(id);
		if (effect != null)
			return effect;
		if (effectName.equals(getString(R.string.decimate)))
			effect = new Decimate(id, effectName, currTrack.getId(), position);
		else if (effectName.equals(getString(R.string.chorus)))
			effect = new Chorus(id, effectName, currTrack.getId(), position);
		else if (effectName.equals(getString(R.string.delay)))
			effect = new Delay(id, effectName, currTrack.getId(), position);
		else if (effectName.equals(getString(R.string.flanger)))
			effect = new Flanger(id, effectName, currTrack.getId(), position);
		else if (effectName.equals(getString(R.string.filter)))
			effect = new Filter(id, effectName, currTrack.getId(), position);
		else if (effectName.equals(getString(R.string.reverb)))
			effect = new Reverb(id, effectName, currTrack.getId(), position);
		else if (effectName.equals(getString(R.string.tremelo)))
			effect = new Tremelo(id, effectName, currTrack.getId(), position);
		currTrack.effects.add(effect);
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
		intent.putExtra("trackId", currTrack.getId());

		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (Managers.playbackManager.getState() != PlaybackManager.State.PLAYING)
			// if not currently playing, disarm the track
			currTrack.disarm();
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
			currTrack.setPrimaryVolume(level);
		} else if (levelBar.equals(panLevel)) {
			currTrack.setPrimaryPan(level);
		} else if (levelBar.equals(pitchLevel)) {
			currTrack.setPrimaryPitch(level);
		}
	}

	@Override
	public void notifyInit(LevelListenable levelBar) {
		if (levelBar.equals(volumeLevel)) {
			volumeLevel.setLevelColor(Colors.VOLUME_COLOR);
			volumeLevel.setLevel(currTrack.volume);
		} else if (levelBar.equals(panLevel)) {
			panLevel.setLevelColor(Colors.PAN_COLOR);
			panLevel.setLevel(currTrack.pan);
		} else if (levelBar.equals(pitchLevel)) {
			pitchLevel.setLevelColor(Colors.PITCH_COLOR);
			pitchLevel.setLevel(currTrack.pitch);
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

	@Override
	public void setLevel(LevelListenable levelListenable, float levelX,
			float levelY) {
		// for 2d seekbar. nothing to do
	}
}
