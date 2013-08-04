package com.kh.beatbot.ui.view.page.effect;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.EffectParam;
import com.kh.beatbot.listener.Level1dListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ControlViewBase;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.control.param.KnobParamControl;
import com.kh.beatbot.ui.view.control.param.LevelParamControl;
import com.kh.beatbot.ui.view.page.Page;

public class EffectParamsPage extends TouchableView implements
		Level1dListener, OnReleaseListener {
	protected KnobParamControl[] paramControls;
	protected Effect effect;
	protected int xParamIndex = 0, yParamIndex = 1;

	public EffectParamsPage(Effect effect) {
		this.effect = effect;
		createChildren();
	}
	
	public final void setXLevel(float level) {
		getXParamControl().setLevel(level);
	}

	public final void setYLevel(float level) {
		getYParamControl().setLevel(level);
	}

	public Effect getEffect() {
		return effect;
	}

	public void setEffect(Effect effect) {
		this.effect = effect;
		for (LevelParamControl paramControl : paramControls) {
			paramControl.setParam(effect.getParam(paramControl.getId()));
		}
	}

	@Override
	public void createChildren() {
		if (effect == null)
			return;
		createParamControls();
		for (LevelParamControl paramControl : paramControls) {
			addChild(paramControl);
		}
	}

	@Override
	public void layoutChildren() {
		int halfParams = (effect.getNumParams() + 1) / 2;
		float paramW = effect.getNumParams() <= 3 ? width / effect.getNumParams() : width
				/ halfParams;
		float paramH = 3 * paramW / 2;
		float y = effect.getNumParams() <= 3 ? height / 2 - paramH / 2 : height / 2
				- paramH;
		for (int i = 0; i < effect.getNumParams(); i++) {
			if (i == 3)
				y += paramH;
			int index = effect.getNumParams() <= 3 ? i : i % halfParams;
			paramControls[i].layout(this, index * paramW, y, paramW, paramH);
		}
	}

	@Override
	public void onLevelChange(ControlViewBase levelListenable, float level) {
		int paramNum = levelListenable.getId();
		effect.setParamLevel(paramNum, level);
		if (effect.paramsLinked()) {
			if (levelListenable.getId() == 0) {
				effect.setParamLevel(1, level);
				paramControls[1].setViewLevel(level);
			} else if (levelListenable.getId() == 1) {
				paramControls[0].setLevel(level);
			}
		}

		if (paramNum == xParamIndex) {
			Page.effectPage.getLevel2d().setViewLevelX(level);
		} else if (paramNum == yParamIndex) {
			Page.effectPage.getLevel2d().setViewLevelY(level);
		}
	}

	@Override
	public void onRelease(Button button) {
		int paramNum = button.getId();
		EffectParam param = effect.getParam(paramNum);
		param.beatSync = ((ToggleButton) button).isChecked();
		paramControls[paramNum].setLevel(param.viewLevel);
		if (effect.paramsLinked()) {
			if (paramNum == 0) {
				effect.getParam(1).beatSync = param.beatSync;
				paramControls[1].setBeatSync(param.beatSync);
				paramControls[1].setLevel(param.viewLevel);
			} else if (paramNum == 1) {
				effect.getParam(0).beatSync = param.beatSync;
				paramControls[0].setBeatSync(param.beatSync);
				paramControls[0].setLevel(param.viewLevel);
			}
		}
	}

	private void createParamControls() {
		paramControls = new KnobParamControl[effect.getNumParams()];
		for (int i = 0; i < paramControls.length; i++) {
			paramControls[i] = new KnobParamControl(effect.getParam(i).beatSyncable);
			paramControls[i].setId(i);
			paramControls[i].addLevelListener(this);
		}
	}

	private final LevelParamControl getXParamControl() {
		return paramControls[xParamIndex];
	}

	private final LevelParamControl getYParamControl() {
		return paramControls[yParamIndex];
	}

}
