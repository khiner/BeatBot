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
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.BBSeekbar2d;
import com.kh.beatbot.view.ParamControl;
import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.window.TouchableViewWindow;

public class EffectLayout extends TouchableViewWindow implements LevelListener,
	View.OnClickListener {

	private Effect effect = null;
	private BBIconSource toggleButtonIcon = null;
	private BBIconSource[] filterIcons = new BBIconSource[3];
	private BBToggleButton[] filterToggles = new BBToggleButton[3];
	private BBToggleButton toggleButton = null;
	private BBToggleButton linkToggle = null;

	private List<ParamControl> paramControls = new ArrayList<ParamControl>();
	private ParamControl xParamControl = null, yParamControl = null;
	private BBSeekbar2d level2d = null;
	
	public EffectLayout(TouchableSurfaceView parent, Effect effect) {
		super(parent);
		this.effect = effect;
	}
	
	private void initEffectToggleButton() {
		if (effect instanceof Filter) {
			filterToggles[0] = new BBToggleButton((TouchableSurfaceView)parent);
			filterToggles[1] = new BBToggleButton((TouchableSurfaceView)parent);
			filterToggles[2] = new BBToggleButton((TouchableSurfaceView)parent);
		}
		toggleButton = new BBToggleButton((TouchableSurfaceView)parent);
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
			ParamControl pc = new ParamControl((TouchableSurfaceView)parent, effect.getParam(i));
			paramControls.add(pc);
			pc.setId(i);
			pc.removeAllListeners();
			pc.addLevelListener(this);
		}
		level2d = new BBSeekbar2d((TouchableSurfaceView)parent);
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
		if (!(listenable instanceof BBSeekbar2d)) {
			Param param = effect.getParam(listenable.getId());
			listenable.setLevel(param.viewLevel);
		}
		if (effect.paramsLinked() && !(listenable instanceof BBSeekbar2d)) {
			Param param = effect.getParam(listenable.getId());
			((ParamControl) listenable).setBeatSync(param.beatSync);
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
		filterIcons[0] = new BBIconSource(-1, R.drawable.lowpass_filter_icon, R.drawable.lowpass_filter_icon_selected);
		filterIcons[1] = new BBIconSource(-1, R.drawable.bandpass_filter_icon, R.drawable.bandpass_filter_icon_selected);
		filterIcons[2] = new BBIconSource(-1, R.drawable.highpass_filter_icon, R.drawable.highpass_filter_icon_selected);
		for (int i = 0; i < 3; i++) {
			filterToggles[i].setIconSource(filterIcons[i]);
		}
		filterToggles[((Filter) effect).getMode()].setOn(true);
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
		
	}

	@Override
	protected void layoutChildren() {
		// TODO Auto-generated method stub
		
	}
}
