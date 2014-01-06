package com.kh.beatbot.ui.view.page;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.effect.Chorus;
import com.kh.beatbot.effect.Decimate;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.effect.Tremelo;
import com.kh.beatbot.listener.DraggableLabelListListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.TextView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.Seekbar;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.list.DraggableLabelList;
import com.kh.beatbot.ui.view.list.LabelList;

public abstract class LevelsFXPage extends TouchableView {

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
				for (int i = 0; i < Effect.MAX_EFFECTS_PER_TRACK; i++) {
					Effect effect = getCurrTrack().findEffectByPosition(i);
					if (effect != null)
						labelList.setLabelOn(i, effect.isOn());
				}
			} else {
				for (int i = 0; i < Effect.MAX_EFFECTS_PER_TRACK; i++) {
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
			if (text.isEmpty() || text.equalsIgnoreCase("Add")) {
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
	protected Seekbar levelBar;
	protected ToggleButton volumeToggle, panToggle, pitchToggle;
	protected TextView effectLabel;
	protected boolean masterMode = false;

	// effects attrs
	protected DraggableLabelList effectLabelList;
	protected String[] effectNames;

	@Override
	public synchronized void update() {
		updateLevels();
		updateEffects();
	}

	public void setMasterMode(boolean masterMode) {
		this.masterMode = masterMode;
	}

	public BaseTrack getCurrTrack() {
		return masterMode ? TrackManager.masterTrack : TrackManager.currTrack;
	}

	private void updateLevels() {
		setMasterMode(masterMode);
		Param currParam = getCurrTrack().getCurrentLevelParam();
		levelBar.setParam(currParam);
		levelBar.setLevelColor(getLevelColor(currParam),
				getLevelColorTrans(currParam));
		deselectAll();
		selectLevel(currParam);
	}

	private void updateEffects() {
		if (effectLabelList.numChildren() <= 0)
			return;
		for (int i = 0; i < Effect.MAX_EFFECTS_PER_TRACK; i++) {
			Effect effect = getCurrTrack().findEffectByPosition(i);
			if (effect == null) {
				effectLabelList.setLabelText(i, "");
			} else {
				effectLabelList.setLabelText(i, effect.getName());
				effectLabelList.setLabelOn(i, effect.isOn());
			}
		}
	}

	private void deselectAll() {
		volumeToggle.setChecked(false);
		panToggle.setChecked(false);
		pitchToggle.setChecked(false);
	}

	private void selectLevel(Param currParam) {
		if (currParam.equals(getCurrTrack().volumeParam)) {
			volumeToggle.setChecked(true);
		} else if (currParam.equals(getCurrTrack().panParam)) {
			panToggle.setChecked(true);
		} else if (currParam.equals(getCurrTrack().pitchParam)) {
			pitchToggle.setChecked(true);
		}
	}

	private float[] getLevelColor(Param currParam) {
		if (currParam.equals(getCurrTrack().volumeParam)) {
			return Colors.VOLUME;
		} else if (currParam.equals(getCurrTrack().panParam)) {
			return Colors.PAN;
		} else if (currParam.equals(getCurrTrack().pitchParam)) {
			return Colors.PITCH;
		}
		return Colors.VOLUME;
	}

	private float[] getLevelColorTrans(Param currParam) {
		if (currParam.equals(getCurrTrack().volumeParam)) {
			return Colors.VOLUME_TRANS;
		} else if (currParam.equals(getCurrTrack().panParam)) {
			return Colors.PAN_TRANS;
		} else if (currParam.equals(getCurrTrack().pitchParam)) {
			return Colors.PITCH_TRANS;
		}
		return Colors.VOLUME_TRANS;
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
		BeatBotActivity.mainActivity.launchEffect(effect);
	}

	private Effect getEffect(String effectName, int position) {
		Effect effect = getCurrTrack().findEffectByPosition(position);
		if (effect != null)
			return effect;
		BaseTrack track = getCurrTrack();
		if (effectName.equals(Decimate.NAME))
			effect = new Decimate(track, position);
		else if (effectName.equals(Chorus.NAME))
			effect = new Chorus(track, position);
		else if (effectName.equals(Delay.NAME))
			effect = new Delay(track, position);
		else if (effectName.equals(Flanger.NAME))
			effect = new Flanger(track, position);
		else if (effectName.equals(Filter.NAME))
			effect = new Filter(track, position);
		else if (effectName.equals(Reverb.NAME))
			effect = new Reverb(track, position);
		else if (effectName.equals(Tremelo.NAME))
			effect = new Tremelo(track, position);
		getCurrTrack().addEffect(effect);
		return effect;
	}

	@Override
	protected synchronized void initIcons() {
		effectLabel.setText("Effects");
		volumeToggle.setText("Vol");
		panToggle.setText("Pan");
		pitchToggle.setText("Pit");
		volumeToggle.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.volumeFillColorSet, Colors.volumeStrokeColorSet));
		panToggle.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.panFillColorSet, Colors.panStrokeColorSet));
		pitchToggle.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.pitchFillColorSet, Colors.pitchStrokeColorSet));
	}

	@Override
	public void draw() {
		shapeGroup.draw(this);
	}

	@Override
	protected synchronized void createChildren() {
		effectLabel = new TextView(shapeGroup);
		levelBar = new Seekbar(shapeGroup);
		volumeToggle = new ToggleButton(shapeGroup);
		panToggle = new ToggleButton(shapeGroup);
		pitchToggle = new ToggleButton(shapeGroup);
		volumeToggle.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button button) {
				getCurrTrack().setLevelType(Effect.LevelType.VOLUME);
				updateLevels();
			}
		});
		panToggle.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button button) {
				getCurrTrack().setLevelType(Effect.LevelType.PAN);
				updateLevels();
			}
		});
		pitchToggle.setOnReleaseListener(new OnReleaseListener() {
			public void onRelease(Button button) {
				getCurrTrack().setLevelType(Effect.LevelType.PITCH);
				updateLevels();
			}
		});
		// effects
		effectNames = BeatBotActivity.mainActivity.getResources()
				.getStringArray(R.array.effect_names);
		effectLabelList = new DraggableLabelList();
		effectLabelList.setListener(new EffectLabelListListener(
				BeatBotActivity.mainActivity));

		addChildren(effectLabel, levelBar, volumeToggle, panToggle,
				pitchToggle, effectLabelList);
	}
}
