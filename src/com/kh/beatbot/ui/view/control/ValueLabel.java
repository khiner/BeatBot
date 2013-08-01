package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public class ValueLabel extends ControlView1dBase {
	private float anchorY = 0, anchorLevel;
	private Param param;

	public ValueLabel(ShapeGroup shapeGroup, Param param) {
		initBgRect(shapeGroup, Colors.LABEL_VERY_LIGHT, Colors.VOLUME);
		setParam(param);
	}

	@Override
	public void init() {
		super.init();
		setStrokeColor(Colors.BLACK);
		update();
	}

	public void setParam(Param param) {
		this.param = param;
		update();
	}

	public void update() {
		if (param != null) {
			setText(param.getFormattedValueString());
		}
	}

	@Override
	protected float posToLevel(float x, float y) {
		return GeneralUtils.clipToUnit(anchorLevel + (anchorY - y)
				/ (root.getHeight() * 2));
	}

	public void setViewLevel(float level) {
		super.setViewLevel(level);
		if (param != null) {
			param.setLevel(level);
			setText(param.getFormattedValueString());
		}
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		anchorY = y;
		anchorLevel = param.viewLevel;
		bgRect.setFillColor(Colors.LABEL_SELECTED);
		super.handleActionDown(id, x, y);
	}

	public void handleActionUp(int id, float x, float y) {
		bgRect.setFillColor(Colors.LABEL_VERY_LIGHT);
		super.handleActionUp(id, x, y);
	}
}
