package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.EffectParam;
import com.kh.beatbot.listener.Level1dListener;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.control.ControlView1dBase;

public abstract class LevelParamControl extends ParamControl implements Level1dListener {
	protected static ShapeGroup shapeGroup;
	
	protected ControlView1dBase levelControl;
	
	@Override
	public void setId(int id) {
		super.setId(id);
		levelControl.setId(id);
	}
	
	public void setParam(EffectParam param) {
		super.setParam(param);
		levelControl.setViewLevel(param.viewLevel);
	}
	
	public void setViewLevel(float viewLevel) {
		super.setViewLevel(viewLevel);
		levelControl.setViewLevel(viewLevel);
	}

	public void addLevelListener(Level1dListener listener) {
		super.addLevelListener(listener);
		levelControl.addLevelListener(listener);
	}
}
