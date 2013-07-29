package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public class ValueLabel extends ControlView1dBase {

	float anchorY = 0, anchorLevel;
	ShapeGroup shapeGroup;
	
	public ValueLabel(ShapeGroup shapeGroup) {
		this.shapeGroup = shapeGroup;
		initBgRect(shapeGroup, Colors.LABEL_VERY_LIGHT, Colors.BLACK);
	}
	
	@Override
	public void init() {
		super.init();
		setStrokeColor(Colors.BLACK);
		setText(formatLevel());
	}
	
	@Override
	protected float posToLevel(float x, float y) {
		return anchorLevel + (anchorY - y) / (root.getHeight() * 4);
	}

	public void setViewLevel(float level) {
		super.setViewLevel(level);
		setText(formatLevel());
	}

	@Override
	protected void loadIcons() {
		if (bgRect != null)
			bgRect.setFillColor(Colors.LABEL_VERY_LIGHT);
	}
	
	@Override
	public void handleActionDown(int id, float x, float y) {
		anchorY = y;
		anchorLevel = level;
		bgRect.setFillColor(Colors.VOLUME_LIGHT);
		super.handleActionDown(id, x, y);
	}
	
	public void handleActionUp(int id, float x, float y) {
		bgRect.setFillColor(Colors.LABEL_VERY_LIGHT);
		super.handleActionUp(id, x, y);
	}
	
	private String formatLevel() {
		return String.format("%.2f", level);
	}
}
