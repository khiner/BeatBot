package com.kh.beatbot.layout.page;

import java.util.Collections;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.activity.EffectActivity;
import com.kh.beatbot.effect.Chorus;
import com.kh.beatbot.effect.Decimate;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.effect.Tremelo;
import com.kh.beatbot.global.BaseTrack;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.GlobalVars.LevelType;
import com.kh.beatbot.listenable.LabelListListenable;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.BBSeekbar;

public class LevelsFXPage extends Page implements LevelListener {
	class EffectLabelListListener implements LabelListListener {
		private AlertDialog selectEffectAlert = null;
		private int lastClickedPos = -1;

		EffectLabelListListener(Context c) {
			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setTitle("Choose Effect");
			builder.setItems(effectNames,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							if (effectNames[item].equals("NONE")) {
								effectLabelList
										.setLabelText(lastClickedPos, "");
								Effect effect = currTrack
										.findEffectByPosition(lastClickedPos);
								if (effect != null) {
									effect.removeEffect();
								}
							} else {
								launchEffectIntent(effectNames[item],
										lastClickedPos, true);
								effectLabelList.setLabelText(lastClickedPos,
										effectNames[item]);
							}
						}
					});
			selectEffectAlert = builder.create();
		}

		@Override
		public void labelListInitialized(LabelListListenable labelList) {
			effectLabelList = labelList;
			if (effectLabelList.anyLabels()) {
				for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
					Effect effect = currTrack.findEffectByPosition(i);
					if (effect != null)
						labelList.setLabelOn(i, effect.isOn());
				}
			} else {
				for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
					labelList.addLabel("", false);
				}
			}
		}

		@Override
		public void labelMoved(int oldPosition, int newPosition) {
			Effect effect = currTrack.findEffectByPosition(oldPosition);
			if (effect != null) {
				effect.setPosition(newPosition);
			}
			for (Effect other : currTrack.effects) {
				if (other.equals(effect))
					continue;
				if (other.getPosition() >= newPosition
						&& other.getPosition() < oldPosition) {
					other.setPosition(other.getPosition() + 1);
				} else if (other.getPosition() <= newPosition
						&& other.getPosition() > oldPosition) {
					other.setPosition(other.getPosition() - 1);
				}
			}
			Collections.sort(currTrack.effects);

			Effect.setEffectPosition(currTrack.getId(), oldPosition,
					newPosition);
		}

		@Override
		public void labelClicked(String text, int position) {
			lastClickedPos = position;
			if (text.isEmpty()) {
				selectEffectAlert.show();
			} else {
				launchEffectIntent(text, position, false);
			}
		}

		@Override
		public void labelLongClicked(int position) {
			lastClickedPos = position;
			selectEffectAlert.show();
		}
	}
	
	// levels attrs
	private BaseTrack currTrack = null;
	private BBSeekbar trackLevel;
	private ToggleButton volumeToggle, panToggle, pitchToggle;
	
	// effects attrs
	private LabelListListenable effectLabelList = null;
	private String[] effectNames;
	
	public LevelsFXPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init() {
		currTrack = TrackManager.currTrack;
		
		// levels
		trackLevel = (BBSeekbar) findViewById(R.id.trackLevel);
		trackLevel.addLevelListener(this);
		volumeToggle = (ToggleButton) findViewById(R.id.trackVolumeToggle);
		panToggle = (ToggleButton) findViewById(R.id.trackPanToggle);
		pitchToggle = (ToggleButton) findViewById(R.id.trackPitchToggle);
		volumeToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				currTrack.activeLevelType = LevelType.VOLUME;
				update();
			}
		});
		panToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				currTrack.activeLevelType = LevelType.PAN;
				update();
			}
		});
		pitchToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				currTrack.activeLevelType = LevelType.PITCH;
				update();
			}
		});
		
		// effects
		effectNames = getContext().getResources().getStringArray(
				R.array.effect_names);
		effectLabelList = (LabelListListenable) findViewById(R.id.effectList);
		effectLabelList.setListener(new EffectLabelListListener(getContext()));
		((TextView) findViewById(R.id.effectsLabel))
				.setTypeface(GlobalVars.font);
	}
	
	@Override
	public void update() {
		// levels
		deselectAll();
		selectActiveLevel();
		trackLevel.setLevelColor(getActiveLevelColor());
		trackLevel.setLevel(getActiveLevel());
		
		// effects
		if (!effectLabelList.anyLabels())
			return;
		for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
			Effect effect = currTrack.findEffectByPosition(i);
			if (effect != null) {
				effectLabelList.setLabelText(i, effect.getName());
				effectLabelList.setLabelOn(i, effect.isOn());
			} else {
				effectLabelList.setLabelTextByPosition(i, "");
			}
		}
	}

	@Override
	public void setVisibilityCode(int code) {
		trackLevel.setVisibility(code);
		effectLabelList.setVisibility(code);
	}

	@Override
	public void setLevel(LevelListenable levelBar, float level) {
		switch (currTrack.activeLevelType) {
		case VOLUME:
			currTrack.setVolume(level);
			break;
		case PAN:
			currTrack.setPan(level);
			break;
		case PITCH:
			currTrack.setPitch(level);
			break;
		}
	}

	public void setMasterMode(boolean masterMode) {
		currTrack = masterMode ? TrackManager.masterTrack : TrackManager.currTrack;
	}
	
	@Override
	public void notifyInit(LevelListenable levelBar) {
		// do nothing when levelbar initialized
	}

	@Override
	public void notifyPressed(LevelListenable levelBar, boolean pressed) {
		// do nothing when level pressed
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

	private void deselectAll() {
		volumeToggle.setChecked(false);
		panToggle.setChecked(false);
		pitchToggle.setChecked(false);
	}

	private void selectActiveLevel() {
		switch (currTrack.activeLevelType) {
		case VOLUME:
			volumeToggle.setChecked(true);
			return;
		case PAN:
			panToggle.setChecked(true);
			return;
		case PITCH:
			pitchToggle.setChecked(true);
			return;
		}
	}

	private float[] getActiveLevelColor() {
		switch (currTrack.activeLevelType) {
		case VOLUME:
			return Colors.VOLUME;
		case PAN:
			return Colors.PAN;
		case PITCH:
			return Colors.PITCH;
		}
		return Colors.VOLUME;
	}

	private float getActiveLevel() {
		switch (currTrack.activeLevelType) {
		case VOLUME:
			return currTrack.volume;
		case PAN:
			return currTrack.pan;
		case PITCH:
			return currTrack.pitch;
		}
		return currTrack.volume;
	}
	
	// effects methods
	private void launchEffectIntent(String effectName, int effectPosition, boolean setOn) {
		Effect effect = getEffect(effectName, effectPosition);
		if (effectName != effect.name) {
			// different effect being added to the effect slot. need to replace
			// it
			effect.removeEffect();
			effect = getEffect(effectName, effectPosition);
		}
		Intent intent = new Intent();
		intent.setClass(getContext(), EffectActivity.class);
		intent.putExtra("effectPosition", effect.getPosition());
		intent.putExtra("trackId", currTrack.getId());
		intent.putExtra("setOn", setOn);
		getContext().startActivity(intent);
	}

	private Effect getEffect(String effectName, int position) {
		Effect effect = currTrack.findEffectByPosition(position);
		if (effect != null)
			return effect;
		if (effectName.equals(getContext().getString(R.string.decimate)))
			effect = new Decimate(effectName, currTrack.getId(), position);
		else if (effectName.equals(getContext().getString(R.string.chorus)))
			effect = new Chorus(effectName, currTrack.getId(), position);
		else if (effectName.equals(getContext().getString(R.string.delay)))
			effect = new Delay(effectName, currTrack.getId(), position);
		else if (effectName.equals(getContext().getString(R.string.flanger)))
			effect = new Flanger(effectName, currTrack.getId(), position);
		else if (effectName.equals(getContext().getString(R.string.filter)))
			effect = new Filter(effectName, currTrack.getId(), position);
		else if (effectName.equals(getContext().getString(R.string.reverb)))
			effect = new Reverb(effectName, currTrack.getId(), position);
		else if (effectName.equals(getContext().getString(R.string.tremelo)))
			effect = new Tremelo(effectName, currTrack.getId(), position);
		currTrack.effects.add(effect);
		return effect;
	}
}
