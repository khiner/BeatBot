package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.ControlView1dBase;

public abstract class LevelParamControl extends ParamControl {
	protected ControlView1dBase levelControl;

	public LevelParamControl(View view) {
		super(view);
	}

	public LevelParamControl(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
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
