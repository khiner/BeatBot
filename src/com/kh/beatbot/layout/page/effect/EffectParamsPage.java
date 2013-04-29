package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.listener.Level1dListener;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.ControlViewBase;
import com.kh.beatbot.view.control.ParamControl;
import com.kh.beatbot.view.control.ToggleButton;
import com.kh.beatbot.view.control.ToggleKnob;

public abstract class EffectParamsPage extends TouchableBBView implements
		Level1dListener, OnReleaseListener {
	protected ParamControl[] paramControls;
	protected Effect effect;
	protected int xParamIndex = 0, yParamIndex = 1;

	protected abstract int getNumParams();

	protected abstract ParamData[] getParamsData();

	public final void setXLevel(float level) {
		getXParamControl().knob.setLevel(level);
	}

	public final void setYLevel(float level) {
		getYParamControl().knob.setLevel(level);
	}

	private final ParamControl getXParamControl() {
		return paramControls[xParamIndex];
	}

	public final ParamControl getYParamControl() {
		return paramControls[yParamIndex];
	}

	public void setEffect(Effect effect) {
		this.effect = effect;
		for (ParamControl paramControl : paramControls) {
			paramControl.setParam(effect.getParam(paramControl.knob.getId()));
		}
	}

	public Effect getEffect() {
		return effect;
	}
	
	@Override
	protected void loadIcons() {
		// parent
	}

	@Override
	public void init() {
		// parent
	}

	@Override
	public void draw() {
		// parent
	}

	@Override
	public void createChildren() {
		createParamControls();
		for (ParamControl paramControl : paramControls) {
			addChild(paramControl);
		}
	}

	@Override
	public void layoutChildren() {
		int halfParams = (getNumParams() + 1) / 2;
		float paramW = getNumParams() <= 3 ? width / getNumParams() : width
				/ halfParams;
		float paramH = 3 * paramW / 2;
		float y = getNumParams() <= 3 ? height / 2 - paramH / 2 : height / 2
				- paramH;
		for (int i = 0; i < getNumParams(); i++) {
			if (i == 3)
				y += paramH;
			int index = getNumParams() <= 3 ? i : i % halfParams;
			paramControls[i].layout(this, index * paramW, y, paramW, paramH);
		}
	}

	@Override
	public void onLevelChange(ControlViewBase levelListenable, float level) {
		int paramNum = levelListenable.getId();
		effect.setParamLevel(paramNum, level);
		paramControls[paramNum].updateValueLabel(effect.getParam(paramNum));
		if (effect.paramsLinked()) {
			if (levelListenable.getId() == 0) {
				effect.setParamLevel(1, level);
				paramControls[1].knob.setViewLevel(level);
				paramControls[1].updateValueLabel(effect.getParam(paramNum));
			} else if (levelListenable.getId() == 1) {
				paramControls[0].knob.setLevel(level);
			}
		}

		if (paramNum == xParamIndex) {
			GlobalVars.effectPage.getLevel2d().setViewLevelX(level);
		} else if (paramNum == yParamIndex) {
			GlobalVars.effectPage.getLevel2d().setViewLevelY(level);
		}
	}

	@Override
	public void onRelease(Button button) {
		int paramNum = button.getId();
		Param param = effect.getParam(paramNum);
		param.beatSync = ((ToggleButton) button).isChecked();
		paramControls[paramNum].knob.setLevel(param.viewLevel);
		if (effect.paramsLinked()) {
			if (paramNum == 0) {
				effect.getParam(1).beatSync = param.beatSync;
				((ToggleKnob) paramControls[1].knob)
						.setBeatSync(param.beatSync);
				((ToggleKnob) paramControls[1].knob).setLevel(param.viewLevel);
			} else if (paramNum == 1) {
				effect.getParam(0).beatSync = param.beatSync;
				((ToggleKnob) paramControls[0].knob)
						.setBeatSync(param.beatSync);
				paramControls[0].knob.setLevel(param.viewLevel);
			}
		}
	}

	private void createParamControls() {
		paramControls = new ParamControl[getNumParams()];
		for (int i = 0; i < paramControls.length; i++) {
			paramControls[i] = new ParamControl(getParamsData()[i].beatSyncable);
			paramControls[i].knob.setId(i);
			paramControls[i].knob.addLevelListener(this);
		}
	}
}
