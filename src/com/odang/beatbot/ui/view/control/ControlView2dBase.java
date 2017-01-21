package com.odang.beatbot.ui.view.control;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.listener.ParamListener;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.view.View;

public abstract class ControlView2dBase extends ControlViewBase implements ParamListener {

	public ControlView2dBase(View view) {
		super(view);
	}
	
	public ControlView2dBase(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	protected Param[] params = new Param[2];

	protected abstract float xToLevel(float x);

	protected abstract float yToLevel(float y);

	public void setParams(Param xParam, Param yParam) {
		for (int i = 0; i < params.length; i++) {
			if (params[i] != null) {
				params[i].removeListener(this);
			}
			params[i] = (i == 0) ? xParam : (i == 1 ? yParam : null);
			if (params[i] != null) {
				params[i].addListener(this);
			}
		}
		onParamChange(params[0]);
	}

	@Override
	public void handleActionMove(int id, Pointer pos) {
		params[0].setLevel(xToLevel(pos.x));
		params[1].setLevel(yToLevel(pos.y));
	}
}
