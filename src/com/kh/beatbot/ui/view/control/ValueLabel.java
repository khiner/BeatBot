package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.ColorSet;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.mesh.Shape.Type;

public class ValueLabel extends ControlView1dBase {
	private float anchorY = 0, anchorLevel;
	private boolean enabled = false;

	private static ColorSet fillColorSet = Colors.valueLabelFillColorSet;

	public ValueLabel(ShapeGroup shapeGroup) {
		initBgRect(Type.ROUNDED_RECT, shapeGroup, fillColorSet.disabledColor,
				Colors.VOLUME);
	}

	@Override
	public synchronized void init() {
		super.init();
		setStrokeColor(Colors.BLACK);
	}

	@Override
	protected float posToLevel(float x, float y) {
		return GeneralUtils.clipToUnit(anchorLevel + (anchorY - y)
				/ (root.getHeight() * 2));
	}

	public void setViewLevel(float level) {
		if (param != null) {
			setText(param.getFormattedValue());
		}
	}

	@Override
	public synchronized void setParam(Param param) {
		super.setParam(param);
		if (param == null) {
			enabled = false;
			bgRect.setFillColor(fillColorSet.disabledColor);
			setText("");
		} else {
			enabled = true;
			bgRect.setFillColor(fillColorSet.defaultColor);
		}
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		if (!enabled)
			return;
		anchorY = y;
		anchorLevel = param.viewLevel;
		bgRect.setFillColor(fillColorSet.pressedColor);
		super.handleActionDown(id, x, y);
	}

	public void handleActionUp(int id, float x, float y) {
		if (!enabled)
			return;
		bgRect.setFillColor(fillColorSet.defaultColor);
		super.handleActionUp(id, x, y);
	}
}
