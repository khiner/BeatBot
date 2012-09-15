package com.kh.beatbot;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Effect.EffectParam;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.layout.EffectControlLayout;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.TronKnob;
import com.kh.beatbot.view.TronSeekbar2d;

public class EffectActivity extends Activity implements LevelListener,
		View.OnClickListener {
	protected Effect effect;
	private ToggleButton[] filterButtons = new ToggleButton[3];
	protected List<EffectControlLayout> paramControls = new ArrayList<EffectControlLayout>();
	protected TronKnob xParamKnob = null, yParamKnob = null;
	protected TronSeekbar2d level2d = null;

	private View initEffectToggleButton(ViewGroup parent) {
		if (effect instanceof Filter) {
			LinearLayout filterTypesLayout = (LinearLayout) LayoutInflater
					.from(getBaseContext()).inflate(
							R.layout.filter_types_layout, parent, false);
			filterButtons[0] = (ToggleButton) filterTypesLayout
					.findViewById(R.id.lp_toggle);
			filterButtons[1] = (ToggleButton) filterTypesLayout
					.findViewById(R.id.hp_toggle);
			filterButtons[2] = (ToggleButton) filterTypesLayout
					.findViewById(R.id.bp_toggle);
			filterButtons[((Filter) effect).getMode()].setChecked(true);
			((ToggleButton) filterTypesLayout.findViewById(R.id.effectToggleOn))
					.setChecked(effect.on);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			filterTypesLayout.setLayoutParams(layoutParams);
			filterTypesLayout.setId(5);
			return filterTypesLayout;
		} else {
			ToggleButton effectToggleButton = new ToggleButton(this);
			StateListDrawable drawable = new StateListDrawable();
			drawable.addState(new int[] { android.R.attr.state_checked },
					getResources().getDrawable(effect.getOnDrawableId()));
			drawable.addState(new int[] {},
					getResources().getDrawable(effect.getOffDrawableId()));
			effectToggleButton.setBackgroundDrawable(drawable);
			effectToggleButton.setTextOn("");
			effectToggleButton.setTextOff("");
			effectToggleButton.setOnClickListener(this);
			effectToggleButton.setChecked(effect.on);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					effectToggleButton.getBackground().getIntrinsicWidth(),
					effectToggleButton.getBackground().getIntrinsicHeight());
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			effectToggleButton.setLayoutParams(layoutParams);
			effectToggleButton.setId(5);
			return effectToggleButton;
		}
	}

	private View initEffectLayout(ViewGroup parent) {
		View effectParamLayout = LayoutInflater.from(getBaseContext()).inflate(
				effect.getParamLayoutId(), parent, false);
		View effectToggleButton = initEffectToggleButton(parent);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.BELOW, effectToggleButton.getId());
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

		effectParamLayout.setLayoutParams(layoutParams);
		RelativeLayout paramWrapperLayout = new RelativeLayout(this);
		paramWrapperLayout.setId(2);
		// in landscape view, take up all vertical, but need space to the
		// right for xy control
		paramWrapperLayout.setLayoutParams(new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		paramWrapperLayout.addView(effectToggleButton);
		paramWrapperLayout.addView(effectParamLayout);
		return paramWrapperLayout;
	}

	private void initDelayKnobs() {
		// since left/right delay times are linked by default,
		// xy view is set to x = left channel, y = feedback
		xParamKnob = paramControls.get(0).getKnob();
		yParamKnob = effect.paramsLinked() ? paramControls.get(2).getKnob()
				: paramControls.get(1).getKnob();
		((ToggleButton) findViewById(R.id.linkButton)).setChecked(effect
				.paramsLinked());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // remove title bar
		int effectId = getIntent().getExtras().getInt("effectId");
		int trackNum = getIntent().getExtras().getInt("trackNum");
		effect = GlobalVars.findEffectById(effectId, trackNum);
		setContentView(R.layout.effect_layout);
		ViewGroup parent = (ViewGroup) findViewById(R.id.effect_layout);
		View paramWrapperLayout = initEffectLayout(parent);
		// set layout params for XY view
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.RIGHT_OF,
				paramWrapperLayout.getId());
		findViewById(R.id.xyParamBar).setLayoutParams(layoutParams);
		parent.addView(paramWrapperLayout);
		initParamControls();

		xParamKnob = paramControls.get(0).getKnob();
		yParamKnob = paramControls.get(1).getKnob();
		for (EffectControlLayout paramControl : paramControls) {
			int paramNum = paramControl.getKnob().getId();
			updateParamValueLabel(paramNum);
		}
		if (effect instanceof Delay) {
			initDelayKnobs();
		}
	}

	protected void initParamControls() {
		int numParams = effect.getNumParams();
		paramControls = new ArrayList<EffectControlLayout>();
		paramControls.add((EffectControlLayout) findViewById(R.id.param1));
		paramControls.add((EffectControlLayout) findViewById(R.id.param2));
		if (numParams > 2) {
			paramControls.add((EffectControlLayout) findViewById(R.id.param3));
		}
		if (numParams > 3) {
			paramControls.add((EffectControlLayout) findViewById(R.id.param4));
		}
		if (numParams > 4) {
			paramControls.add((EffectControlLayout) findViewById(R.id.param5));
		}
		if (numParams > 5) {
			paramControls.add((EffectControlLayout) findViewById(R.id.param6));
		}
		for (int i = 0; i < paramControls.size(); i++) {
			EffectControlLayout ecl = paramControls.get(i);
			ecl.getKnob().setId(i);
			ecl.getKnob().removeAllListeners();
			ecl.getKnob().addLevelListener(this);
		}
		level2d = (TronSeekbar2d) findViewById(R.id.xyParamBar);
		level2d.removeAllListeners();
		level2d.addLevelListener(this);
	}

	public void updateParamValueLabel(int paramNum) {
		paramControls.get(paramNum).setValueLabel(
				effect.getParamValueString(paramNum));
	}

	public void updateXYViewLevel() {
		level2d.setViewLevelX(xParamKnob.getLevel());
		level2d.setViewLevelY(yParamKnob.getLevel());
	}

	@Override
	public void onClick(View view) {
		toggleOn(view);
	}

	public void link(View view) {
		TronKnob leftChannelKnob = paramControls.get(0).getKnob();
		TronKnob rightChannelKnob = paramControls.get(1).getKnob();
		float newRightChannelLevel = rightChannelKnob.getLevel();
		boolean newRightChannelSynced = rightChannelKnob.isBeatSync();

		effect.setParamsLinked(((ToggleButton) view).isChecked());

		if (effect.paramsLinked()) {
			// y = feedback when linked
			yParamKnob = paramControls.get(2).getKnob();
			((Delay) effect).rightChannelLevelMemory = rightChannelKnob
					.getLevel();
			((Delay) effect).rightChannelBeatSyncMemory = rightChannelKnob
					.isBeatSync();
			newRightChannelLevel = leftChannelKnob.getLevel();
			newRightChannelSynced = leftChannelKnob.isBeatSync();
		} else {
			// y = right delay time when not linked
			yParamKnob = paramControls.get(1).getKnob();
			newRightChannelSynced = ((Delay) effect).rightChannelBeatSyncMemory;
			if (((Delay) effect).rightChannelLevelMemory > 0)
				newRightChannelLevel = ((Delay) effect).rightChannelLevelMemory;
		}
		effect.getParam(1).beatSync = newRightChannelSynced;
		rightChannelKnob.setBeatSync(newRightChannelSynced);
		rightChannelKnob.setLevel(newRightChannelLevel);
	}

	public void toggleOn(View view) {
		effect.setOn(((ToggleButton) view).isChecked());
	}

	@Override
	public void setLevel(LevelListenable levelListenable, float level) {
		int paramNum = levelListenable.getId();
		effect.setParamLevel(paramNum, level);
		updateXYViewLevel();
		updateParamValueLabel(paramNum);
		if (effect.paramsLinked()) {
			if (levelListenable.getId() == 0) {
				effect.setParamLevel(1, level);
				paramControls.get(1).getKnob().setViewLevel(level);
				paramControls.get(1).setValueLabel(
						paramControls.get(0).getValueLabel());
			} else if (levelListenable.getId() == 1) {
				paramControls.get(0).getKnob().setLevel(level);
			}
		}
	}

	@Override
	public void setLevel(LevelListenable level2d, float levelX, float levelY) {
		xParamKnob.setLevel(levelX);
		yParamKnob.setLevel(levelY);
		updateParamValueLabel(xParamKnob.getId());
		updateParamValueLabel(yParamKnob.getId());
	}

	@Override
	public void notifyPressed(LevelListenable listenable, boolean pressed) {
		// do nothing
	}

	@Override
	public void notifyClicked(LevelListenable listenable) {
		if (!(listenable instanceof TronSeekbar2d)) {
			int paramNum = listenable.getId();
			EffectParam param = effect.getParam(paramNum);
			param.beatSync = ((TronKnob) listenable).isBeatSync();
			listenable.setLevel(param.viewLevel);
			if (effect.paramsLinked()) {
				if (paramNum == 0) {
					effect.getParam(1).beatSync = param.beatSync;
					paramControls.get(1).getKnob().setBeatSync(param.beatSync);
					paramControls.get(1).getKnob().setLevel(param.viewLevel);
				} else if (paramNum == 1) {
					effect.getParam(0).beatSync = param.beatSync;
					paramControls.get(0).getKnob().setBeatSync(param.beatSync);
					paramControls.get(0).getKnob().setLevel(param.viewLevel);
				}
			}
		}
	}

	@Override
	public void notifyInit(final LevelListenable listenable) {
		// need to use the thread that created the text label view to update
		// label
		// (which is done when listenable notifies after 'setLevel')
		Handler refresh = new Handler(Looper.getMainLooper());
		refresh.post(new Runnable() {
			public void run() {
				if (!(listenable instanceof TronSeekbar2d)) {
					EffectParam param = effect.getParam(listenable.getId());
					listenable.setLevel(param.viewLevel);
				}
			}
		});
		if (effect.paramsLinked() && !(listenable instanceof TronSeekbar2d)) {
			EffectParam param = effect.getParam(listenable.getId());
			((TronKnob) listenable).setBeatSync(param.beatSync);
		}
	}

	public void selectFilterMode(View view) {
		for (int i = 0; i < filterButtons.length; i++) {
			if (view.equals(filterButtons[i])) {
				((Filter) effect).setMode(i);
				filterButtons[i].setChecked(true);
			} else
				filterButtons[i].setChecked(false);
		}
	}
}
