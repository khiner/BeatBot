package com.kh.beatbot.ui.view.page.track;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.event.effect.EffectChangeEvent;
import com.kh.beatbot.event.effect.EffectCreateEvent;
import com.kh.beatbot.event.effect.EffectDestroyEvent;
import com.kh.beatbot.event.effect.EffectMoveEvent;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.list.LabelList;
import com.kh.beatbot.ui.view.list.LabelList.LabelState;

public class EffectSelectPage extends TrackPage {

	public EffectSelectPage(View view) {
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
						Effect effect = context.getTrackManager().getCurrTrack()
								.findEffectByPosition(lastClickedPos);
						if (effect != null) {
							destroyEffect(effect);
						}
					} else {
						launchEffect(effectNames[item], lastClickedPos, true);
					}
				}
			});
			selectEffectAlert = builder.create();
		}

		@Override
		public void labelMoved(int oldPosition, int newPosition) {
			int trackId = context.getTrackManager().getCurrTrack().getId();
			new EffectMoveEvent(trackId, oldPosition, newPosition).execute();
		}

		@Override
		public void labelClicked(String text, int position) {
			lastClickedPos = position;
			if (text.isEmpty() || text.equalsIgnoreCase(Effect.NEW_EFFECT_LABEL)) {
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

	@Override
	public void onEffectCreate(BaseTrack track, Effect effect) {
		updateEffectLabels();
	}

	@Override
	public void onEffectDestroy(BaseTrack track, Effect effect) {
		updateEffectLabels();
	}

	@Override
	public void onEffectOrderChange(BaseTrack track, int initialEffectPosition,
			int endEffectPosition) {
		updateEffectLabels();
	}

	public void setMasterMode(boolean masterMode) {
		this.masterMode = masterMode;
	}

	private void updateEffectLabels() {
		if (effectLabelList.numChildren() <= 0)
			return;
		for (int i = 0; i < Effect.MAX_EFFECTS_PER_TRACK; i++) {
			Effect effect = context.getTrackManager().getCurrTrack().findEffectByPosition(i);
			if (effect == null) {
				effectLabelList.setLabelText(i, "");
			} else {
				effectLabelList.setLabelText(i, effect.getName());
				effectLabelList.setLabelState(i, effect.isOn() ? LabelState.ON : LabelState.OFF);
			}
		}
	}

	// effects methods
	private void launchEffect(String effectName, int position, boolean setOn) {
		BaseTrack track = context.getTrackManager().getCurrTrack();
		Effect effect = track.findEffectByPosition(position);
		if (effect == null) {
			new EffectCreateEvent(track.getId(), position, effectName).execute();
		} else if (effectName != effect.getName()) { // replace effect
			new EffectChangeEvent(track.getId(), position, effect.getName(), effectName).execute();
		} else { // select effect
			context.getMainPage().launchEffect(effect);
		}
	}

	private void destroyEffect(Effect effect) {
		final BaseTrack track = context.getTrackManager().getCurrTrack();
		new EffectDestroyEvent(track.getId(), effect.getPosition()).execute();
	}

	@Override
	protected void createChildren() {
		effectNames = context.getResources().getStringArray(R.array.effect_names);
		effectLabelList = new LabelList(this);
		effectLabelList.setListener(new EffectLabelListListener(context));

		for (int i = 0; i < Effect.MAX_EFFECTS_PER_TRACK; i++) {
			effectLabelList.addLabel("", false);
		}
	}

	@Override
	public void layoutChildren() {
		effectLabelList.layout(this, BG_OFFSET, height / 4, width - BG_OFFSET * 2, height / 2);
	}
}
