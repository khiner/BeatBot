package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.view.control.ControlView1dBase;

public abstract class LevelParamControl extends ParamControl {
	protected ControlView1dBase levelControl;
	
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
