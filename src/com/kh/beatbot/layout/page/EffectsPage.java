package com.kh.beatbot.layout.page;

import java.util.Collections;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
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
import com.kh.beatbot.global.BaseTrack;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listenable.LabelListListenable;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.manager.TrackManager;

public class EffectsPage extends Page {
	private BaseTrack currTrack = null;
	private LabelListListenable effectLabelList = null;
	private String[] effectNames;

	public EffectsPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init() {
		currTrack = TrackManager.currTrack;
		effectNames = getContext().getResources().getStringArray(
				R.array.effect_names);
		effectLabelList = (LabelListListenable) findViewById(R.id.effectList);
		effectLabelList.setListener(new EffectLabelListListener(getContext()));
		((TextView) findViewById(R.id.effectsLabel))
				.setTypeface(GlobalVars.font);
	}

	@Override
	public void update() {
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

	public void setMasterMode(boolean masterMode) {
		currTrack = masterMode ? TrackManager.masterTrack
				: TrackManager.currTrack;
	}

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
