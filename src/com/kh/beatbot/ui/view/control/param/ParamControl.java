package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.TextView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.ValueLabel;

public class ParamControl extends TouchableView {
	protected ShapeGroup shapeGroup;
	
	protected ValueLabel valueLabel;
	protected TextView label;
	
	public ParamControl() {
		this(null);
	}
	
	public ParamControl(ShapeGroup shapeGroup) {
		this.shapeGroup = shapeGroup == null ? new ShapeGroup() : shapeGroup;
		label = new TextView();
		valueLabel = new ValueLabel(null);
		addChildren(label, valueLabel);
	}
	
	@Override
	public void setId(int id) {
		super.setId(id);
		valueLabel.setId(id);
	}
	
	public void setParam(Param param) {
		valueLabel.setParam(param);
		label.setText(param.name);
	}
	
	@Override
	public synchronized void layoutChildren() {
		label.layout(this, 0, 0, width / 2, height);
		valueLabel.layout(this, width / 2, 0, width / 2, height);
	}
	
	public float getLevel() {
		return valueLabel.getLevel();
	}
}
