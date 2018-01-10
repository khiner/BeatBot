package com.odang.beatbot.ui.view.group.effect;

import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.effect.Param;
import com.odang.beatbot.ui.view.TouchableView;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.param.DialParamControl;
import com.odang.beatbot.ui.view.control.param.ParamControl;

public class EffectParamsGroup extends TouchableView {
	protected DialParamControl[] paramControls;
	protected Effect effect;

	public EffectParamsGroup(View view) {
		super(view);
	}

	public Effect getEffect() {
		return effect;
	}

	public EffectParamsGroup withEffect(Effect effect) {
		if (null == this.effect) {
			paramControls = new DialParamControl[effect.getNumParams()];
			for (int i = 0; i < paramControls.length; i++) {
				paramControls[i] = new DialParamControl(this).withBeatSync(effect.getParam(i)
						.isBeatSyncable());
				paramControls[i].setId(i);
			}
		}
		this.effect = effect;
		for (ParamControl paramControl : paramControls) {
			Param param = effect.getParam(paramControl.getId());
			paramControl.setParam(param);
		}
		return this;
	}

	@Override
	public void layoutChildren() {
		if (effect.getNumParams() < 8) { // two rows
			int halfParams = (effect.getNumParams() + 1) / 2;
			float paramW = effect.getNumParams() <= 3 ? width / effect.getNumParams() : width
					/ halfParams;
			float paramH = 3 * paramW / 2;

			float x = 0;
			float y = effect.getNumParams() < 3 ? height / 2 - paramH / 2 : height / 2 - paramH;
			for (int i = 0; i < effect.getNumParams(); i++) {
				if (effect.getNumParams() > 2 && i == halfParams) { // next row
					x = effect.getNumParams() % 2 != 0 ? paramW / 2 : 0;
					y += paramH;
				}
				paramControls[i].layout(this, effect.getNumParams() == 3 ? x + paramW / 2 : x, y,
						paramW, paramH);
				x += paramW;
			}
		} else {
			float paramW = width / 3;
			float paramH = height / 3;
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					int index = i * 3 + j;
					if (index < effect.getNumParams()) {
						paramControls[index].layout(this, j * paramW, i * paramH, paramW, paramH);
					}
				}
			}
		}
	}
}
