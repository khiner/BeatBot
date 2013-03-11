package com.kh.beatbot.layout;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Filter;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.global.BBToggleButton;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.control.ControlViewBase;
import com.kh.beatbot.view.control.Knob;
import com.kh.beatbot.view.control.ParamControl;
import com.kh.beatbot.view.control.Seekbar2d;

public class EffectLayout extends TouchableBBView implements LevelListener,
	View.OnClickListener {

	private Effect effect = null;
	private BBIconSource toggleButtonIcon = null;
	private BBToggleButton[] filterToggles = new BBToggleButton[3];
	private BBToggleButton toggleButton = null;
	private BBToggleButton linkToggle = null;

	private List<ParamControl> paramControls = new ArrayList<ParamControl>();
	private ParamControl xParamControl = null, yParamControl = null;
	private Seekbar2d level2d = null;
		
	public EffectLayout(TouchableSurfaceView parent, Effect effect) {
		this.effect = effect;
		this.root = parent;
		createChildren();
	}
	
	private void initEffectToggleButton() {
		if (effect instanceof Filter) {
			filterToggles[0] = new BBToggleButton((TouchableSurfaceView)root);
			filterToggles[1] = new BBToggleButton((TouchableSurfaceView)root);
			filterToggles[2] = new BBToggleButton((TouchableSurfaceView)root);
		}
		toggleButton = new BBToggleButton((TouchableSurfaceView)root);
	}

	private void initDelayKnobs() {
		// since left/right delay times are linked by default,
		// xy view is set to x = left channel, y = feedback
		xParamControl = paramControls.get(0);
		yParamControl = effect.paramsLinked() ? paramControls.get(2)
				: paramControls.get(1);
		linkToggle.setOn(effect.paramsLinked());
	}
	
	protected void initParamControls() {
		paramControls = new ArrayList<ParamControl>();
		for (int i = 0; i < effect.getNumParams(); i++) {
			ParamControl pc = new ParamControl((TouchableSurfaceView)root, effect.getParam(i));
			paramControls.add(pc);
			pc.knob.setId(i);
			pc.knob.removeAllListeners();
			pc.knob.addLevelListener(this);
			updateParamValueLabel(i);
		}
		level2d = new Seekbar2d((TouchableSurfaceView)root);
		level2d.removeAllListeners();
		level2d.addLevelListener(this);
		xParamControl = paramControls.get(0);
		yParamControl = paramControls.get(1);
		if (effect instanceof Delay) {
			initDelayKnobs();
		}
	}

	public void updateParamValueLabel(int paramNum) {
		paramControls.get(paramNum).updateValue();
	}

	public void updateXYViewLevel() {
		level2d.setViewLevelX(xParamControl.knob.getLevel());
		level2d.setViewLevelY(yParamControl.knob.getLevel());
	}
	
	public void link(View view) {
		ParamControl leftChannelControl = paramControls.get(0);
		ParamControl rightChannelControl = paramControls.get(1);
		float newRightChannelLevel = rightChannelControl.knob.getLevel();
		boolean newRightChannelSynced = rightChannelControl.knob.isBeatSync();

		effect.setParamsLinked(((ToggleButton) view).isChecked());

		if (effect.paramsLinked()) {
			// y = feedback when linked
			yParamControl = paramControls.get(2);
			((Delay) effect).rightChannelLevelMemory = rightChannelControl.knob
					.getLevel();
			((Delay) effect).rightChannelBeatSyncMemory = rightChannelControl.knob
					.isBeatSync();
			newRightChannelLevel = leftChannelControl.knob.getLevel();
			newRightChannelSynced = leftChannelControl.knob.isBeatSync();
		} else {
			// y = right delay time when not linked
			yParamControl = paramControls.get(1);
			newRightChannelSynced = ((Delay) effect).rightChannelBeatSyncMemory;
			if (((Delay) effect).rightChannelLevelMemory > 0)
				newRightChannelLevel = ((Delay) effect).rightChannelLevelMemory;
		}
		effect.getParam(1).beatSync = newRightChannelSynced;
		rightChannelControl.knob.setBeatSync(newRightChannelSynced);
		rightChannelControl.knob.setLevel(newRightChannelLevel);
	}
	
	@Override
	public void setLevel(ControlViewBase levelListenable, float level) {
		int paramNum = levelListenable.getId();
		effect.setParamLevel(paramNum, level);
		updateXYViewLevel();
		updateParamValueLabel(paramNum);
		if (effect.paramsLinked()) {
			if (levelListenable.getId() == 0) {
				effect.setParamLevel(1, level);
				paramControls.get(1).knob.setViewLevel(level);
			} else if (levelListenable.getId() == 1) {
				paramControls.get(0).knob.setLevel(level);
			}
		}
	}

	@Override
	public void setLevel(ControlViewBase level2d, float levelX, float levelY) {
		xParamControl.knob.setLevel(levelX);
		yParamControl.knob.setLevel(levelY);
		updateParamValueLabel(xParamControl.knob.getId());
		updateParamValueLabel(yParamControl.knob.getId());
	}

	@Override
	public void notifyPressed(ControlViewBase listenable, boolean pressed) {
		// do nothing
	}

	@Override
	public void notifyClicked(ControlViewBase listenable) {
		if (listenable instanceof Seekbar2d) {
			return;
		}
		int paramNum = listenable.getId();
		Param param = effect.getParam(paramNum);
		param.beatSync = ((Knob) listenable).isBeatSync();
		listenable.setLevel(param.viewLevel);
		if (effect.paramsLinked()) {
			if (paramNum == 0) {
				effect.getParam(1).beatSync = param.beatSync;
				paramControls.get(1).knob.setBeatSync(param.beatSync);
				paramControls.get(1).knob.setLevel(param.viewLevel);
			} else if (paramNum == 1) {
				effect.getParam(0).beatSync = param.beatSync;
				paramControls.get(0).knob.setBeatSync(param.beatSync);
				paramControls.get(0).knob.setLevel(param.viewLevel);
			}
		}
	}

	@Override
	public void notifyInit(final ControlViewBase listenable) {
		if (!(listenable instanceof Seekbar2d)) {
			Param param = effect.getParam(listenable.getId());
			listenable.setLevel(param.viewLevel);
		}
		if (effect.paramsLinked() && !(listenable instanceof Seekbar2d)) {
			Param param = effect.getParam(listenable.getId());
			((Knob) listenable).setBeatSync(param.beatSync);
		}
	}

	public void selectFilterMode(View view) {
		for (int i = 0; i < filterToggles.length; i++) {
			if (view.equals(filterToggles[i])) {
				((Filter) effect).setMode(i);
				filterToggles[i].setOn(true);
			} else
				filterToggles[i].setOn(false);
		}
	}
	
	@Override
	public void onClick(View view) {
		toggleOn(view);
	}

	public void toggleOn(View view) {
		effect.setOn(((ToggleButton) view).isChecked());
	}
	
	@Override
	protected void loadIcons() {
		toggleButtonIcon = new BBIconSource(-1, effect.getOffDrawableId(), effect.getOnDrawableId());
		toggleButton.setIconSource(toggleButtonIcon);
		toggleButton.setOn(effect.isOn());
		if (effect instanceof Filter) {
			filterToggles[0].setIconSource(new BBIconSource(-1, R.drawable.lowpass_filter_icon, R.drawable.lowpass_filter_icon_selected));
			filterToggles[1].setIconSource(new BBIconSource(-1, R.drawable.bandpass_filter_icon, R.drawable.bandpass_filter_icon_selected));
			filterToggles[2].setIconSource(new BBIconSource(-1, R.drawable.highpass_filter_icon, R.drawable.highpass_filter_icon_selected));
			filterToggles[((Filter) effect).getMode()].setOn(true);
			for (BBToggleButton filterToggle : filterToggles) {
				addChild(filterToggle);
			}
		}
	}

	@Override
	public void init() {
		// parent - nothing to init expect children
	}

	@Override
	public void draw() {
		// parent - nothing to draw except children
	}

	@Override
	protected void createChildren() {
		// TODO won't work for filter
		initEffectToggleButton();
		initParamControls();
		addChild(level2d);
		for (ParamControl paramControl : paramControls) {
			addChild(paramControl);
		}
	}

	@Override
	public void layoutChildren() {
		float paramW = (height / 2 - 10) / 2;
		paramControls.get(0).layout(this, width / 30, height / 4, paramW, paramW * 2);
		paramControls.get(1).layout(this, width - height - width / 30 - paramW, height / 4, paramW, paramW * 2);
		level2d.layout(this, width - height, 0, height, height);
	}
}
