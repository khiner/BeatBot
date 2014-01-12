package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.control.ControlView1dBase;

public abstract class LevelParamControl extends ParamControl {
	protected ControlView1dBase levelControl;

	public LevelParamControl() {
		super();
	}

	public LevelParamControl(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	@Override
	public void setId(int id) {
		super.setId(id);
		levelControl.setId(id);
	}
	
	public void setParam(Param param) {
		super.setParam(param);
		levelControl.setParam(param);
	}
}
