package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.ValueLabel;

public class ParamControl extends TouchableView {
	protected View label;
	protected ValueLabel valueLabel;

	public ParamControl(View view) {
		super(view);
	}

	public ParamControl(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	@Override
	public synchronized void createChildren() {
		valueLabel = new ValueLabel(this, renderGroup);
		valueLabel.setShrinkable(true);
		label = new View(this, renderGroup);
	}

	@Override
	public void setId(int id) {
		super.setId(id);
		valueLabel.setId(id);
	}

	public void setParam(Param param) {
		valueLabel.setParam(param);
		label.setText(param == null ? "" : param.getName());
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
