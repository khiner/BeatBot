package com.kh.beatbot.layout.page;

import java.util.Collections;
import java.util.Stack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

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
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listenable.LabelListListenable;
import com.kh.beatbot.listener.LabelListListener;

public class EffectsPage extends TrackPage {
	private static LabelListListenable effectLabelList = null;
	private static String[] effectNames;

	public EffectsPage(Context context, View layout) {
		super(context, layout);
		effectNames = context.getResources().getStringArray(
				R.array.effect_names);
		effectLabelList = (LabelListListenable) layout
				.findViewById(R.id.effectList);
		effectLabelList.setListener(new EffectLabelListListener(context));
		((TextView) layout.findViewById(R.id.effectsLabel))
				.setTypeface(GlobalVars.font);
	}

	@Override
	protected void update() {
		if (!effectLabelList.isRunning())
			return;
		for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
			Effect effect = track.findEffectByPosition(i);
			if (effect != null) {
				effectLabelList.setLabelText(effect.getId(), effect.getName());
				effectLabelList.setLabelOn(effect.getId(), effect.on);
			} else {
				effectLabelList.setLabelTextByPosition(i, "");
			}
		}
	}

	@Override
	public void setVisibilityCode(int code) {
		effectLabelList.setVisibility(code);
	}

	class EffectLabelListListener implements LabelListListener {
		private AlertDialog selectEffectAlert = null;
		private int lastClickedId = -1;
		private int lastClickedPos = -1;

		EffectLabelListListener(Context c) {
			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setTitle("Choose Effect");
			builder.setItems(effectNames,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							if (effectNames[item].equals("NONE")) {
								effectLabelList.setLabelText(lastClickedId, "");
								Effect effect = track
										.findEffectById(lastClickedId);
								if (effect != null) {
									effect.removeEffect();
								}
							} else {
								effectLabelList.setLabelText(lastClickedId,
										effectNames[item]);
								effectLabelList.setLabelOn(lastClickedId, true);
								launchEffectIntent(effectNames[item],
										lastClickedId, lastClickedPos);
							}
						}
					});
			selectEffectAlert = builder.create();
		}

		@Override
		public void labelListInitialized(LabelListListenable labelList) {
			effectLabelList = labelList;
			for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
				labelList.addLabel("", i, false);
			}
		}

		@Override
		public void labelMoved(int oldPosition, int newPosition) {
			Effect effect = track.findEffectByPosition(oldPosition);
			if (effect != null) {
				effect.setPosition(newPosition);
				for (Effect other : track.effects) {
					if (other.equals(effect))
						continue;
					if (other.getPosition() >= newPosition
							&& other.getPosition() < oldPosition) {
						other.incPosition();
					}
				}
				Collections.sort(track.effects);
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
		intent.setClass(context, EffectActivity.class);
		intent.putExtra("effectId", effect.getId());
		intent.putExtra("trackId", track.getId());

		context.startActivity(intent);
	}

	private Effect getEffect(String effectName, int id, int position) {
		Effect effect = track.findEffectById(id);
		if (effect != null)
			return effect;
		if (effectName.equals(context.getString(R.string.decimate)))
			effect = new Decimate(id, effectName, track.getId(), position);
		else if (effectName.equals(context.getString(R.string.chorus)))
			effect = new Chorus(id, effectName, track.getId(), position);
		else if (effectName.equals(context.getString(R.string.delay)))
			effect = new Delay(id, effectName, track.getId(), position);
		else if (effectName.equals(context.getString(R.string.flanger)))
			effect = new Flanger(id, effectName, track.getId(), position);
		else if (effectName.equals(context.getString(R.string.filter)))
			effect = new Filter(id, effectName, track.getId(), position);
		else if (effectName.equals(context.getString(R.string.reverb)))
			effect = new Reverb(id, effectName, track.getId(), position);
		else if (effectName.equals(context.getString(R.string.tremelo)))
			effect = new Tremelo(id, effectName, track.getId(), position);
		track.effects.add(effect);
		return effect;
	}
}
