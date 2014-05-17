package com.kh.beatbot.ui.view.page.effect;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.listener.ParamToggleListener;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.param.KnobParamControl;
import com.kh.beatbot.ui.view.control.param.ParamControl;

public class EffectParamsPage extends TouchableView implements ParamListener, ParamToggleListener {
	protected KnobParamControl[] paramControls;
	protected Effect effect;

	public EffectParamsPage(View view) {
		super(view);
	}

	public Effect getEffect() {
		return effect;
	}

	public EffectParamsPage withEffect(Effect effect) {
		if (null == this.effect) {
			paramControls = new KnobParamControl[effect.getNumParams()];
			for (int i = 0; i < paramControls.length; i++) {
				paramControls[i] = new KnobParamControl(this).withBeatSync(effect.getParam(i)
						.isBeatSyncable());
				paramControls[i].setId(i);
			}
		}

		if (this.effect != null) {
			for (ParamControl paramControl : paramControls) {
				effect.getParam(paramControl.getId()).removeListener(this);
			}
		}
		this.effect = effect;
		for (ParamControl paramControl : paramControls) {
			Param param = effect.getParam(paramControl.getId());
			paramControl.setParam(param);
			param.addListener(this);
		}
		return this;
	}

	@Override
	public synchronized void layoutChildren() {
		int halfParams = (effect.getNumParams() + 1) / 2;
		float paramW = effect.getNumParams() <= 3 ? width / effect.getNumParams() : width
				/ halfParams;
		float paramH = 3 * paramW / 2;
		float y = effect.getNumParams() <= 3 ? height / 2 - paramH / 2 : height / 2 - paramH;
		for (int i = 0; i < effect.getNumParams(); i++) {
			if (i == 3)
				y += paramH;
			int index = effect.getNumParams() <= 3 ? i : i % halfParams;
			paramControls[i].layout(this, index * paramW, y, paramW, paramH);
		}
	}

	@Override
	public void onParamChanged(Param param) {
		if (effect.paramsLinked()) {
			if (param.id == 0) {
				effect.getParam(1).setLevel(param.viewLevel);
			} else if (param.id == 1) {
				effect.getParam(0).ignoreListener(this);
				effect.getParam(0).setLevel(param.viewLevel);
				effect.getParam(0).unignoreListener(this);
			}
		}
	}

	@Override
	public void onParamToggled(Param param) {
		if (effect.paramsLinked()) {
			if (param.id == 0) {
				effect.getParam(1).toggle(param.isBeatSync());
			} else if (param.id == 1) {
				effect.getParam(0).ignoreListener(this);
				effect.getParam(0).toggle(param.isBeatSync());
				effect.getParam(0).unignoreListener(this);
			}
		}
	}
}
