package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.ParamListener;

public abstract class ControlView2dBase extends ControlViewBase implements
		ParamListener {

	protected Param[] params = new Param[2];

	protected abstract float xToLevel(float x);

	protected abstract float yToLevel(float y);

	public synchronized void setParams(Param xParam, Param yParam) {
		for (int i = 0; i < params.length; i++) {
			if (params[i] != null) {
				params[i].removeListener(this);
			}
			params[i] = (i == 0) ? xParam : (i == 1 ? yParam : null);
			if (params[i] != null) {
				params[i].addListener(this);
			}
		}
		onParamChanged(params[0]);
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		params[0].setLevel(xToLevel(x));
		params[1].setLevel(yToLevel(y));
	}
}
