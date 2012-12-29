package com.kh.beatbot.layout.page;

import java.util.Collections;

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
				effectLabelList.setLabelText(i, effect.getName());
				effectLabelList.setLabelOn(i, effect.on);
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
								Effect effect = track
										.findEffectByPosition(lastClickedPos);
								if (effect != null) {
									effect.removeEffect();
								}
							} else {
								effectLabelList.setLabelText(lastClickedPos,
										effectNames[item]);
								effectLabelList
										.setLabelOn(lastClickedPos, true);
								launchEffectIntent(effectNames[item],
										lastClickedPos);
							}
						}
					});
			selectEffectAlert = builder.create();
		}

		@Override
		public void labelListInitialized(LabelListListenable labelList) {
			effectLabelList = labelList;
			for (int i = 0; i < GlobalVars.MAX_EFFECTS_PER_TRACK; i++) {
				labelList.addLabel("", false);
			}
		}

		@Override
		public void labelMoved(int oldPosition, int newPosition) {
			Effect effect = track.findEffectByPosition(oldPosition);
			if (effect != null) {
				effect.setPosition(newPosition);
			}
			for (Effect other : track.effects) {
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
			Collections.sort(track.effects);

			Effect.setEffectPosition(track.getId(), oldPosition, newPosition);
		}

		@Override
		public void labelClicked(String text, int position) {
			lastClickedPos = position;
			if (text.isEmpty()) {
				selectEffectAlert.show();
			} else {
				launchEffectIntent(text, position);
			}
		}

		@Override
		public void labelLongClicked(int position) {
			lastClickedPos = position;
			selectEffectAlert.show();
		}
	}

	private void launchEffectIntent(String effectName, int effectPosition) {
		Effect effect = getEffect(effectName, effectPosition);
		if (effectName != effect.name) {
			// different effect being added to the effect slot. need to replace
			// it
			effect.removeEffect();
			effect = getEffect(effectName, effectPosition);
		}
		Intent intent = new Intent();
		intent.setClass(context, EffectActivity.class);
		intent.putExtra("effectPosition", effect.getPosition());
		intent.putExtra("trackId", track.getId());

		context.startActivity(intent);
	}

	private Effect getEffect(String effectName, int position) {
		Effect effect = track.findEffectByPosition(position);
		if (effect != null)
			return effect;
		if (effectName.equals(context.getString(R.string.decimate)))
			effect = new Decimate(effectName, track.getId(), position);
		else if (effectName.equals(context.getString(R.string.chorus)))
			effect = new Chorus(effectName, track.getId(), position);
		else if (effectName.equals(context.getString(R.string.delay)))
			effect = new Delay(effectName, track.getId(), position);
		else if (effectName.equals(context.getString(R.string.flanger)))
			effect = new Flanger(effectName, track.getId(), position);
		else if (effectName.equals(context.getString(R.string.filter)))
			effect = new Filter(effectName, track.getId(), position);
		else if (effectName.equals(context.getString(R.string.reverb)))
			effect = new Reverb(effectName, track.getId(), position);
		else if (effectName.equals(context.getString(R.string.tremelo)))
			effect = new Tremelo(effectName, track.getId(), position);
		track.effects.add(effect);
		return effect;
	}
}
