package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.ParamListener;


public abstract class ControlView1dBase extends ControlViewBase implements ParamListener {

	protected Param param;
	protected abstract void setViewLevel(float viewLevel);
	protected abstract float posToLevel(float x, float y);
	
	public synchronized void setParam(Param param) {
		if (this.param != null) {
			this.param.removeListener(this);
		}
		this.param = param;
		if (this.param != null) {
			this.param.addListener(this);
			onParamChanged(param);
		}
	}
	
	public float getLevel() {
		return param.level;
	}
	
	public Param getParam() {
		return param;
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		param.setLevel(posToLevel(x, y));
	}
	
	@Override
	public void handleActionMove(int id, float x, float y) {
		super.handleActionMove(id, x, y);
		if (!selected)
			return;
		param.setLevel(posToLevel(x, y));
	}
	
	@Override
	public void onParamChanged(Param param) {
		setViewLevel(param.viewLevel);
	}
}
