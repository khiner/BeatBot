package com.kh.beatbot.ui.view.page;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Chorus;
import com.kh.beatbot.effect.Crush;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.Flanger;
import com.kh.beatbot.effect.Reverb;
import com.kh.beatbot.effect.Tremolo;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.list.LabelList;
import com.kh.beatbot.ui.view.list.LabelList.LabelState;

public class EffectsPage extends TrackPage {

	public EffectsPage(View view) {
		super(view);
	}

	class EffectLabelListListener implements LabelListListener {
		private AlertDialog selectEffectAlert = null;
		private int lastClickedPos = -1;

		EffectLabelListListener(Context c) {
			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setTitle("Choose effect");
			builder.setItems(effectNames, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if (effectNames[item].toLowerCase().equals("none")) {
						Effect effect = TrackManager.getCurrTrack().findEffectByPosition(
								lastClickedPos);
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
			TrackManager.getCurrTrack().moveEffect(oldPosition, newPosition);
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

	public void updateEffectLabels() {
		if (effectLabelList.numChildren() <= 0)
			return;
		for (int i = 0; i < Effect.MAX_EFFECTS_PER_TRACK; i++) {
			Effect effect = TrackManager.getCurrTrack().findEffectByPosition(i);
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
			// effect.removeEffect();
			// TODO fix
		}

		context.launchEffect(effect);
	}

	private Effect getEffect(String effectName, int position) {
		BaseTrack track = TrackManager.getCurrTrack();
		Effect effect = track.findEffectByPosition(position);
		if (effect != null)
			return effect;
		int trackId = track.getId();
		if (effectName.equals(Crush.NAME))
			effect = new Crush(trackId, position);
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
		else if (effectName.equals(Tremolo.NAME))
			effect = new Tremolo(trackId, position);
		track.addEffect(effect);
		return effect;
	}

	@Override
	protected synchronized void createChildren() {
		effectNames = context.getResources().getStringArray(R.array.effect_names);
		effectLabelList = new LabelList(this);
		effectLabelList.setListener(new EffectLabelListListener(context));

		for (int i = 0; i < Effect.MAX_EFFECTS_PER_TRACK; i++) {
			effectLabelList.addLabel("", false);
		}
	}

	@Override
	public synchronized void layoutChildren() {
		effectLabelList.layout(this, BG_OFFSET, height / 4, width - BG_OFFSET * 2, height / 2);
	}
}
