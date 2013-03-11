package com.kh.beatbot.layout.page;

import java.util.Collections;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

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
import com.kh.beatbot.global.BBButton;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.global.BBToggleButton;
import com.kh.beatbot.global.BaseTrack;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.GlobalVars.LevelType;
import com.kh.beatbot.listenable.LabelList;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.listener.DraggableLabelListListener;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.BBSeekbar;
import com.kh.beatbot.view.BBTextView;
import com.kh.beatbot.view.EffectLabelList;
import com.kh.beatbot.view.TouchableSurfaceView;

public class LevelsFXPage extends Page implements LevelListener {

	class EffectLabelListListener implements DraggableLabelListListener {
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
								Effect effect = getCurrTrack()
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
		public void labelListInitialized(LabelList labelList) {
			effectLabelList = (EffectLabelList)labelList;
			if (effectLabelList.anyLabels()) {
				for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
					Effect effect = getCurrTrack().findEffectByPosition(i);
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
			Effect effect = getCurrTrack().findEffectByPosition(oldPosition);
			if (effect != null) {
				effect.setPosition(newPosition);
			}
			for (Effect other : getCurrTrack().effects) {
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
			Collections.sort(getCurrTrack().effects);

			Effect.setEffectPosition(getCurrTrack().getId(), oldPosition,
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
	private BBSeekbar levelBar;
	private BBToggleButton volumeToggle, panToggle, pitchToggle;
	private BBTextView effectLabel;
	private boolean masterMode = false;
	
	// effects attrs
	private EffectLabelList effectLabelList;
	private String[] effectNames;
	
	public LevelsFXPage(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public void init() {
		effectLabel.setText("EFFECTS");
	}
	
	@Override
	public void update() {
		// levels
		setMasterMode(masterMode);
		deselectAll();
		selectActiveLevel();
		levelBar.setLevelColor(getActiveLevelColor());
		levelBar.setLevel(getActiveLevel());
		
		// effects
		if (!effectLabelList.anyLabels())
			return;
		for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
			Effect effect = getCurrTrack().findEffectByPosition(i);
			if (effect != null) {
				effectLabelList.setLabelText(i, effect.getName());
				effectLabelList.setLabelOn(i, effect.isOn());
			} else {
				effectLabelList.setLabelText(i, "");
			}
		}
	}

	@Override
	public void setLevel(LevelListenable levelBar, float level) {
		switch (getCurrTrack().activeLevelType) {
		case VOLUME:
			getCurrTrack().setVolume(level);
			break;
		case PAN:
			getCurrTrack().setPan(level);
			break;
		case PITCH:
			getCurrTrack().setPitch(level);
			break;
		}
	}

	public void setMasterMode(boolean masterMode) {
		this.masterMode = masterMode;
	}
	
	public BaseTrack getCurrTrack() {
		return masterMode ? TrackManager.masterTrack : TrackManager.currTrack;
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
		volumeToggle.setOn(false);
		panToggle.setOn(false);
		pitchToggle.setOn(false);
	}

	private void selectActiveLevel() {
		switch (getCurrTrack().activeLevelType) {
		case VOLUME:
			volumeToggle.setOn(true);
			return;
		case PAN:
			panToggle.setOn(true);
			return;
		case PITCH:
			pitchToggle.setOn(true);
			return;
		}
	}

	private float[] getActiveLevelColor() {
		switch (getCurrTrack().activeLevelType) {
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
		switch (getCurrTrack().activeLevelType) {
		case VOLUME:
			return getCurrTrack().volume;
		case PAN:
			return getCurrTrack().pan;
		case PITCH:
			return getCurrTrack().pitch;
		}
		return getCurrTrack().volume;
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
		intent.setClass(GlobalVars.mainActivity, EffectActivity.class);
		intent.putExtra("effectPosition", effect.getPosition());
		intent.putExtra("trackId", getCurrTrack().getId());
		intent.putExtra("setOn", setOn);
		GlobalVars.mainActivity.startActivity(intent);
	}


	private Effect getEffect(String effectName, int position) {
		Effect effect = getCurrTrack().findEffectByPosition(position);
		Context c = GlobalVars.mainActivity;
		if (effect != null)
			return effect;
		if (effectName.equals(c.getString(R.string.decimate)))
			effect = new Decimate(effectName, getCurrTrack().getId(), position);
		else if (effectName.equals(c.getString(R.string.chorus)))
			effect = new Chorus(effectName, getCurrTrack().getId(), position);
		else if (effectName.equals(c.getString(R.string.delay)))
			effect = new Delay(effectName, getCurrTrack().getId(), position);
		else if (effectName.equals(c.getString(R.string.flanger)))
			effect = new Flanger(effectName, getCurrTrack().getId(), position);
		else if (effectName.equals(c.getString(R.string.filter)))
			effect = new Filter(effectName, getCurrTrack().getId(), position);
		else if (effectName.equals(c.getString(R.string.reverb)))
			effect = new Reverb(effectName, getCurrTrack().getId(), position);
		else if (effectName.equals(c.getString(R.string.tremelo)))
			effect = new Tremelo(effectName, getCurrTrack().getId(), position);
		getCurrTrack().effects.add(effect);
		return effect;
	}

	@Override
	protected void loadIcons() {
		volumeToggle.setIconSource(new BBIconSource(-1, R.drawable.volume_icon, R.drawable.volume_icon_selected));
		panToggle.setIconSource(new BBIconSource(-1, R.drawable.pan_icon, R.drawable.pan_icon_selected));
		pitchToggle.setIconSource(new BBIconSource(-1, R.drawable.pitch_icon, R.drawable.pitch_selected_icon));
	}

	@Override
	public void draw() {
		// parent view - no drawing
	}
	
	@Override
	protected void createChildren() {
		effectLabel = new BBTextView((TouchableSurfaceView)root);
		levelBar = new BBSeekbar((TouchableSurfaceView)root);
		levelBar.addLevelListener(this);
		volumeToggle = new BBToggleButton((TouchableSurfaceView)root);
		panToggle = new BBToggleButton((TouchableSurfaceView)root);
		pitchToggle = new BBToggleButton((TouchableSurfaceView)root);
		volumeToggle.setOnClickListener(new BBOnClickListener() {
			public void onClick(BBButton button) {
				getCurrTrack().activeLevelType = LevelType.VOLUME;
				update();
			}
		});
		panToggle.setOnClickListener(new BBOnClickListener() {
			public void onClick(BBButton button) {
				getCurrTrack().activeLevelType = LevelType.PAN;
				update();
			}
		});
		pitchToggle.setOnClickListener(new BBOnClickListener() {
			public void onClick(BBButton button) {
				getCurrTrack().activeLevelType = LevelType.PITCH;
				update();
			}
		});
		// effects
		effectNames = GlobalVars.mainActivity.getResources().getStringArray(
				R.array.effect_names);
		effectLabelList = new EffectLabelList((TouchableSurfaceView)root);
		effectLabelList.setListener(new EffectLabelListListener(GlobalVars.mainActivity));
		
		addChild(effectLabel);
		addChild(levelBar);
		addChild(volumeToggle);
		addChild(panToggle);
		addChild(pitchToggle);
		addChild(effectLabelList);
	}

	@Override
	public void layoutChildren() {
		float thirdHeight = height / 3;
		float levelHeight = height / 12;
		float effectHeight = height - height / 12 - thirdHeight;
		
		volumeToggle.layout(this, 0, levelHeight, 2 * thirdHeight, thirdHeight);
		panToggle.layout(this, 2 * thirdHeight, levelHeight, 2 * thirdHeight, thirdHeight);
		pitchToggle.layout(this, 4 * thirdHeight, levelHeight, 2 * thirdHeight, thirdHeight);
		levelBar.layout(this, 2 * height, levelHeight, width - 2 * height, thirdHeight);
		effectLabel.layout(this, 0, effectHeight, width / 5, thirdHeight);
		effectLabelList.layout(this, width / 5, effectHeight, 4 * width / 5, thirdHeight);
	}
}
