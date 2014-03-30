package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.ui.shape.ShapeGroup;

public abstract class ControlView1dBase extends ControlViewBase implements ParamListener {

	protected Param param;

	protected abstract float posToLevel(Position pos);

	public ControlView1dBase(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

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
	public void handleActionDown(int id, Position pos) {
		super.handleActionDown(id, pos);
		param.setLevel(posToLevel(pos));
	}

	@Override
	public void handleActionMove(int id, Position pos) {
		super.handleActionMove(id, pos);
		if (!selected)
			return;
		param.setLevel(posToLevel(pos));
	}

	@Override
	public abstract void onParamChanged(Param param);
}
