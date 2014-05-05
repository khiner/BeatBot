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
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.effect.Tremolo;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.list.LabelList;
import com.kh.beatbot.ui.view.list.LabelList.LabelState;

public class EffectsPage extends TrackPage {

	class EffectLabelListListener implements LabelListListener {
		private AlertDialog selectEffectAlert = null;
		private int lastClickedPos = -1;

		EffectLabelListListener(Context c) {
			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setTitle("Choose Effect");
			builder.setItems(effectNames, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if (effectNames[item].toLowerCase().equals("none")) {
						Effect effect = getCurrTrack().findEffectByPosition(lastClickedPos);
						if (effect != null) {
							effect.removeEffect();
						}
					} else {
						launchEffect(effectNames[item], lastClickedPos, true);
					}
					updateEffectLabels();
				}
			});
			selectEffectAlert = builder.create();
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
				launchEffect(text, position, false);
			}
		}

		@Override
		public void labelLongClicked(int position) {
			lastClickedPos = position;
			selectEffectAlert.show();
		}
	}

	protected boolean masterMode = false;

	// effects attrs
	protected LabelList effectLabelList;
	protected String[] effectNames;

	@Override
	public void onSelect(BaseTrack track) {
		updateEffectLabels();
	}

	public void setMasterMode(boolean masterMode) {
		this.masterMode = masterMode;
	}

	public BaseTrack getCurrTrack() {
		return masterMode ? TrackManager.masterTrack : TrackManager.currTrack;
	}

	private void updateEffectLabels() {
		if (effectLabelList.numChildren() <= 0)
			return;
		for (int i = 0; i < Effect.MAX_EFFECTS_PER_TRACK; i++) {
			Effect effect = getCurrTrack().findEffectByPosition(i);
			if (effect == null) {
				effectLabelList.setLabelText(i, "");
			} else {
				effectLabelList.setLabelText(i, effect.getName());
				effectLabelList.setLabelState(i, effect.isOn() ? LabelState.ON : LabelState.OFF);
			}
		}
	}

	// effects methods
	private void launchEffect(String effectName, int effectPosition, boolean setOn) {
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
		else if (effectName.equals(Tremolo.NAME))
			effect = new Tremolo(track, position);
		getCurrTrack().addEffect(effect);
		return effect;
	}

	@Override
	protected synchronized void createChildren() {
		effectNames = BeatBotActivity.mainActivity.getResources().getStringArray(
				R.array.effect_names);
		effectLabelList = new LabelList(renderGroup);
		effectLabelList.setListener(new EffectLabelListListener(BeatBotActivity.mainActivity));

		for (int i = 0; i < Effect.MAX_EFFECTS_PER_TRACK; i++) {
			effectLabelList.addLabel("", false);
		}

		addChildren(effectLabelList);
	}

	@Override
	public synchronized void layoutChildren() {
		effectLabelList.layout(this, 0, height / 4, width, height / 2);
	}
}
