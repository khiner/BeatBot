package com.kh.beatbot.layout.page;

import javax.microedition.khronos.opengles.GL11;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.kh.beatbot.R;
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
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.DraggableLabelListListener;
import com.kh.beatbot.listener.Level1dListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.ControlViewBase;
import com.kh.beatbot.view.control.Seekbar;
import com.kh.beatbot.view.control.TextButton;
import com.kh.beatbot.view.list.DraggableLabelList;
import com.kh.beatbot.view.list.LabelList;
import com.kh.beatbot.view.mesh.ShapeGroup;

public class LevelsFXPage extends Page implements Level1dListener {

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
			effectLabelList = (DraggableLabelList) labelList;
			if (effectLabelList.numChildren() > 0) {
				for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
					Effect effect = getCurrTrack().findEffectByPosition(i);
					if (effect != null)
						labelList.checkLabel(i, effect.isOn());
				}
			} else {
				for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
					labelList.addLabel("", false);
				}
			}
		}

		@Override
		public void labelMoved(int oldPosition, int newPosition) {
			getCurrTrack().moveEffect(oldPosition, newPosition);
		}

		@Override
		public void labelClicked(String text, int position) {
			lastClickedPos = position;
			if (text.isEmpty() || text.equals("ADD")) {
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
	private Seekbar levelBar;
	private TextButton volumeToggle, panToggle, pitchToggle;
	private TextButton effectLabel;
	private boolean masterMode = false;

	private ShapeGroup labelGroup;
	
	// effects attrs
	private DraggableLabelList effectLabelList;
	private String[] effectNames;
	
	public void init() {
	}
	
	@Override
	public void update() {
		updateLevels();
		updateEffects();
	}

	@Override
	public void onLevelChange(ControlViewBase levelBar, float level) {
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

	private void updateLevels() {
		setMasterMode(masterMode);
		deselectAll();
		selectActiveLevel();
		levelBar.setLevelColor(getActiveLevelColor());
		levelBar.setLevel(getActiveLevel());
	}
	
	private void updateEffects() {
		if (effectLabelList.numChildren() <= 0)
			return;
		for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
			Effect effect = getCurrTrack().findEffectByPosition(i);
			if (effect == null) {
				effectLabelList.setLabelText(i, "");
			} else {
				effectLabelList.setLabelText(i, effect.getName());
				effectLabelList.checkLabel(i, effect.isOn());
			}
		}		
	}
	
	private void deselectAll() {
		volumeToggle.setChecked(false);
		panToggle.setChecked(false);
		pitchToggle.setChecked(false);
	}

	private void selectActiveLevel() {
		switch (getCurrTrack().activeLevelType) {
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
	private void launchEffectIntent(String effectName, int effectPosition,
			boolean setOn) {
		Effect effect = getEffect(effectName, effectPosition);
		if (effectName != effect.getName()) {
			// different effect being added to effect slot. need to replace it
			effect.removeEffect();
			effect = getEffect(effectName, effectPosition);
		}
		GlobalVars.mainActivity.launchEffect(effect);
	}

	private Effect getEffect(String effectName, int position) {
		Effect effect = getCurrTrack().findEffectByPosition(position);
		if (effect != null)
			return effect;
		int trackId = getCurrTrack().getId();
		if (effectName.equals(Decimate.NAME))
			effect = new Decimate(trackId, position);
		else if (effectName.equals(Chorus.NAME))
			effect = new Chorus(trackId, position);
		else if (effectName.equals(Delay.NAME))
			effect = new Delay(trackId, position);
		else if (effectName.equals(Flanger.NAME))
			effect = new Flanger(trackId, position);
		else if (effectName.equals(Filter.NAME))
			effect = new Filter(trackId, position);
		else if (effectName.equals(Reverb.NAME))
			effect = new Reverb(trackId, position);
		else if (effectName.equals(Tremelo.NAME))
			effect = new Tremelo(trackId, position);
		getCurrTrack().addEffect(effect);
		return effect;
	}

	@Override
	protected void loadIcons() {
		effectLabel.setText("EFFECTS");
		volumeToggle.setText("VOL");
		panToggle.setText("PAN");
		pitchToggle.setText("PIT");
	}

	@Override
	public void draw() {
		push();
		translate(-absoluteX, -absoluteY);
		labelGroup.draw((GL11)BBView.gl, 2);
		pop();
	}

	@Override
	protected void createChildren() {
		labelGroup = new ShapeGroup();
		effectLabel = new TextButton();
		levelBar = new Seekbar();
		levelBar.addLevelListener(this);
		volumeToggle = new TextButton(labelGroup, Colors.volumeBgColorSet, Colors.volumeStrokeColorSet);
		panToggle = new TextButton(labelGroup, Colors.panBgColorSet, Colors.panStrokeColorSet);
		pitchToggle = new TextButton(labelGroup, Colors.pitchBgColorSet, Colors.pitchStrokeColorSet);
		volumeToggle.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button button) {
				getCurrTrack().activeLevelType = LevelType.VOLUME;
				updateLevels();
			}
		});
		panToggle.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button button) {
				getCurrTrack().activeLevelType = LevelType.PAN;
				updateLevels();
			}
		});
		pitchToggle.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button button) {
				getCurrTrack().activeLevelType = LevelType.PITCH;
				updateLevels();
			}
		});
		// effects
		effectNames = GlobalVars.mainActivity.getResources().getStringArray(
				R.array.effect_names);
		effectLabelList = new DraggableLabelList();
		effectLabelList.setListener(new EffectLabelListListener(
				GlobalVars.mainActivity));

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
		panToggle.layout(this, 2 * thirdHeight, levelHeight, 2 * thirdHeight,
				thirdHeight);
		pitchToggle.layout(this, 4 * thirdHeight, levelHeight, 2 * thirdHeight,
				thirdHeight);
		levelBar.layout(this, 2 * height, levelHeight, width - 2 * height,
				thirdHeight);
		effectLabel.layout(this, 0, effectHeight, width / 5, thirdHeight);
		effectLabelList.layout(this, width / 5, effectHeight, 4 * width / 5,
				thirdHeight);
	}
}
