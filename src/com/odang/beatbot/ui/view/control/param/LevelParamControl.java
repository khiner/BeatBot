package com.odang.beatbot.ui.view.control.param;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.ui.view.TouchableView;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.ControlView1dBase;

public abstract class LevelParamControl extends ParamControl {
	protected ControlView1dBase levelControl;

	public LevelParamControl(View view) {
		super(view);
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

	public void setLevelColor(float[] levelColor, float[] levelColorTrans) {
		levelControl.setLevelColor(levelColor, levelColorTrans);
	}

	@Override
	public void onPress(TouchableView view) {
		super.onPress(view);
		valueLabel.press();
		levelControl.press();
		if (null != touchListener) {
			touchListener.onPress(view);
		}
	}

	@Override
	public void onRelease(TouchableView view) {
		super.onRelease(view);
		valueLabel.release();
		levelControl.release();
		if (null != touchListener) {
			touchListener.onRelease(view);
		}
	}
}
