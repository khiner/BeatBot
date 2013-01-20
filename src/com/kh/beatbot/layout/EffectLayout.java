package com.kh.beatbot.layout;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.BBSeekbar2d;
import com.kh.beatbot.view.ParamControl;

public class EffectLayout extends RelativeLayout implements LevelListener,
	View.OnClickListener {

	private Effect effect = null;
	private ToggleButton toggleButton = null;
	private ToggleButton[] filterButtons = new ToggleButton[3];
	private List<ParamControl> paramControls = new ArrayList<ParamControl>();
	private ParamControl xParamControl = null, yParamControl = null;
	private BBSeekbar2d level2d = null;
	
	public EffectLayout(Context context, AttributeSet as) {
		super(context, as);
		// should never get called, but is here to avoid warnings
	}
	
	public EffectLayout(Context context, Effect effect) {
		super(context);
		this.effect = effect;
		setBackgroundColor(getResources().getColor(R.color.background));
		// TODO won't work for filter
		toggleButton = (ToggleButton)initEffectToggleButton();
		initParamControls();
		addView(toggleButton);
		addView(level2d);
		for (ParamControl paramControl : paramControls) {
			addView(paramControl);
		}
	}

	private View initEffectToggleButton() {
		if (effect instanceof Filter) {
			LinearLayout filterTypesLayout = (LinearLayout) LayoutInflater
					.from(getContext()).inflate(
							R.layout.filter_types_layout, this, false);
			filterButtons[0] = (ToggleButton) filterTypesLayout
					.findViewById(R.id.lp_toggle);
			filterButtons[1] = (ToggleButton) filterTypesLayout
					.findViewById(R.id.hp_toggle);
			filterButtons[2] = (ToggleButton) filterTypesLayout
					.findViewById(R.id.bp_toggle);
			filterButtons[((Filter) effect).getMode()].setChecked(true);
			((ToggleButton) filterTypesLayout.findViewById(R.id.effectToggleOn))
					.setChecked(effect.isOn());
			return filterTypesLayout;
		} else {
			ToggleButton effectToggleButton = new ToggleButton(getContext());
			StateListDrawable drawable = new StateListDrawable();
			drawable.addState(new int[] { android.R.attr.state_checked },
					getResources().getDrawable(effect.getOnDrawableId()));
			drawable.addState(new int[] {},
					getResources().getDrawable(effect.getOffDrawableId()));
			effectToggleButton.setBackgroundDrawable(drawable);
			effectToggleButton.setTextOn("");
			effectToggleButton.setTextOff("");
			effectToggleButton.setOnClickListener(this);
			effectToggleButton.setChecked(effect.isOn());
			return effectToggleButton;
		}
	}

	private void initDelayKnobs() {
		// since left/right delay times are linked by default,
		// xy view is set to x = left channel, y = feedback
		xParamControl = paramControls.get(0);
		yParamControl = effect.paramsLinked() ? paramControls.get(2)
				: paramControls.get(1);
		((ToggleButton) findViewById(R.id.linkButton)).setChecked(effect
				.paramsLinked());
	}
	
	protected void initParamControls() {
		paramControls = new ArrayList<ParamControl>();
		for (int i = 0; i < effect.getNumParams(); i++) {
			ParamControl pc = new ParamControl(getContext(), effect.getParam(i));
			paramControls.add(pc);
			pc.setId(i);
			pc.removeAllListeners();
			pc.addLevelListener(this);
		}
		level2d = new BBSeekbar2d(getContext());
		level2d.removeAllListeners();
		level2d.addLevelListener(this);
		xParamControl = paramControls.get(0);
		yParamControl = paramControls.get(1);
		for (ParamControl paramControl : paramControls) {
			int paramNum = paramControl.getId();
			updateParamValueLabel(paramNum);
		}
		if (effect instanceof Delay) {
			initDelayKnobs();
		}
	}

	public void updateParamValueLabel(int paramNum) {
		paramControls.get(paramNum).updateValue();
	}

	public void updateXYViewLevel() {
		level2d.setViewLevelX(xParamControl.getLevel());
		level2d.setViewLevelY(yParamControl.getLevel());
	}
	
	public void link(View view) {
		ParamControl leftChannelControl = paramControls.get(0);
		ParamControl rightChannelControl = paramControls.get(1);
		float newRightChannelLevel = rightChannelControl.getLevel();
		boolean newRightChannelSynced = rightChannelControl.isBeatSync();

		effect.setParamsLinked(((ToggleButton) view).isChecked());

		if (effect.paramsLinked()) {
			// y = feedback when linked
			yParamControl = paramControls.get(2);
			((Delay) effect).rightChannelLevelMemory = rightChannelControl
					.getLevel();
			((Delay) effect).rightChannelBeatSyncMemory = rightChannelControl
					.isBeatSync();
			newRightChannelLevel = leftChannelControl.getLevel();
			newRightChannelSynced = leftChannelControl.isBeatSync();
		} else {
			// y = right delay time when not linked
			yParamControl = paramControls.get(1);
			newRightChannelSynced = ((Delay) effect).rightChannelBeatSyncMemory;
			if (((Delay) effect).rightChannelLevelMemory > 0)
				newRightChannelLevel = ((Delay) effect).rightChannelLevelMemory;
		}
		effect.getParam(1).beatSync = newRightChannelSynced;
		rightChannelControl.setBeatSync(newRightChannelSynced);
		rightChannelControl.setLevel(newRightChannelLevel);
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
				paramControls.get(1).setViewLevel(level);
//				paramControls.get(1).setValueLabel(
//						paramControls.get(0).getValueLabel());
			} else if (levelListenable.getId() == 1) {
				paramControls.get(0).setLevel(level);
			}
		}
	}

	@Override
	public void setLevel(LevelListenable level2d, float levelX, float levelY) {
		xParamControl.setLevel(levelX);
		yParamControl.setLevel(levelY);
		updateParamValueLabel(xParamControl.getId());
		updateParamValueLabel(yParamControl.getId());
	}

	@Override
	public void notifyPressed(LevelListenable listenable, boolean pressed) {
		// do nothing
	}

	@Override
	public void notifyClicked(LevelListenable listenable) {
		if (listenable instanceof BBSeekbar2d) {
			return;
		}
		int paramNum = listenable.getId();
		Param param = effect.getParam(paramNum);
		param.beatSync = ((ParamControl) listenable).isBeatSync();
		listenable.setLevel(param.viewLevel);
		if (effect.paramsLinked()) {
			if (paramNum == 0) {
				effect.getParam(1).beatSync = param.beatSync;
				paramControls.get(1).setBeatSync(param.beatSync);
				paramControls.get(1).setLevel(param.viewLevel);
			} else if (paramNum == 1) {
				effect.getParam(0).beatSync = param.beatSync;
				paramControls.get(0).setBeatSync(param.beatSync);
				paramControls.get(0).setLevel(param.viewLevel);
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
				if (!(listenable instanceof BBSeekbar2d)) {
					Param param = effect.getParam(listenable.getId());
					listenable.setLevel(param.viewLevel);
				}
			}
		});
		if (effect.paramsLinked() && !(listenable instanceof BBSeekbar2d)) {
			Param param = effect.getParam(listenable.getId());
			((ParamControl) listenable).setBeatSync(param.beatSync);
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
	
	@Override
	public void onClick(View view) {
		toggleOn(view);
	}

	public void toggleOn(View view) {
		effect.setOn(((ToggleButton) view).isChecked());
	}
	
	/**
	 * Effect Control views and 2D Seekbar measured and laid out dynamically,
	 * based on number of effect params 
	 */
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		int w = r - l;
		int h = b - t;
		level2d.layout(r - h, 0, r, b);
		int paramContainerW = w - h;
		toggleButton.layout(0, 0, w / 3, h / 10);
		int pl = 0;
		for (ParamControl paramControl : paramControls) {
			paramControl.layout(pl, h / 10, pl + h / 4, h / 10 + (int)((h / 4) * 1.4));
			pl += h / 4;
		}
	}
	
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		int width = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
		int height = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
		level2d.measure(height, height);
		toggleButton.measure(width / 3, height / 10);
		for (ParamControl paramControl : paramControls) {
			paramControl.measure(height / 4, (int)((height / 4) * 1.4));
		}
	}
}
