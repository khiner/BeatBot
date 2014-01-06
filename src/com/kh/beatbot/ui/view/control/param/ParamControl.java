package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.TextView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.ValueLabel;

public class ParamControl extends TouchableView {
	protected ValueLabel valueLabel;
	protected TextView label;

	public ParamControl(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	@Override
	public synchronized void createChildren() {
		valueLabel = new ValueLabel(shapeGroup);
		label = new TextView(shapeGroup);
		addChildren(valueLabel, label);
	}

	@Override
	public void setId(int id) {
		super.setId(id);
		valueLabel.setId(id);
	}

	public void setParam(Param param) {
		valueLabel.setParam(param);
		label.setText(param == null ? "" : param.name);
	}

	public void setLabelText(String text) {
		label.setText(text);
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
