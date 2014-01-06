package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.ColorSet;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public class ValueLabel extends ControlView1dBase {
	private float anchorY = 0, anchorLevel;
	private boolean enabled = false;

	private static ColorSet fillColorSet = Colors.valueLabelFillColorSet;

	public ValueLabel(ShapeGroup shapeGroup) {
		super(shapeGroup);
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

	public void onParamChanged(Param param) {
		setText(param.getFormattedValue());
	}

	@Override
	public synchronized void createChildren() {
		initBgRect(true, fillColorSet);
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
		super.handleActionDown(id, x, y);
	}

	public void handleActionUp(int id, float x, float y) {
		if (!enabled)
			return;
		super.handleActionUp(id, x, y);
	}
}
