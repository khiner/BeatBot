package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.icon.IconResourceSet.State;
import com.kh.beatbot.ui.shape.ShapeGroup;

public class ValueLabel extends ControlView1dBase {
	private float anchorY = 0, anchorLevel;

	public ValueLabel(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	@Override
	protected float posToLevel(float x, float y) {
		return GeneralUtils.clipToUnit(anchorLevel + (anchorY - y) / (root.getHeight() * 2));
	}

	public void onParamChanged(Param param) {
		setText(param.getFormattedValue());
	}

	@Override
	public synchronized void createChildren() {
		setIcon(IconResourceSets.VALUE_LABEL);
		initRoundedRect();
	}

	@Override
	public synchronized void setParam(Param param) {
		super.setParam(param);
		if (param == null) {
			setState(State.DISABLED);
			setText("");
		} else {
			setState(State.DEFAULT);
		}
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		if (getState() == State.DISABLED)
			return;
		anchorY = y;
		anchorLevel = param.viewLevel;
		super.handleActionDown(id, x, y);
	}

	public void handleActionUp(int id, float x, float y) {
		if (getState() == State.DISABLED)
			return;
		super.handleActionUp(id, x, y);
	}
}
