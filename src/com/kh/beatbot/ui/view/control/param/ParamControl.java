package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.Level1dListener;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.TextView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.ControlViewBase;
import com.kh.beatbot.ui.view.control.ValueLabel;

public class ParamControl extends TouchableView implements Level1dListener {
	protected ShapeGroup shapeGroup;
	
	protected ValueLabel valueLabel;
	protected TextView label;
	protected Param param;
	
	public ParamControl() {
		this(null);
	}
	
	public ParamControl(ShapeGroup shapeGroup) {
		this.shapeGroup = shapeGroup == null ? new ShapeGroup() : shapeGroup;
		label = new TextView();
		valueLabel = new ValueLabel(null, param);
		addChild(label);
		addChild(valueLabel);
	}
	
	@Override
	public void setId(int id) {
		super.setId(id);
		valueLabel.setId(id);
	}
	
	public void setParam(Param param) {
		this.param = param;
		valueLabel.setParam(param);
		label.setText(param.name);
	}
	
	public void setViewLevel(float viewLevel) {
		valueLabel.setViewLevel(viewLevel);
	}
	
	@Override
	public void layoutChildren() {
		label.layout(this, 0, 0, width / 2, height);
		valueLabel.layout(this, width / 2, 0, width / 2, height);
	}

	public void addLevelListener(Level1dListener listener) {
		valueLabel.addLevelListener(listener);
	}
	
	public float getLevel() {
		return valueLabel.getLevel();
	}

	public void setLevel(float level) {
		valueLabel.setLevel(level); // only need only control to send the actual event
	}

	@Override
	public void onLevelChange(ControlViewBase levelListenable, float level) {
		setViewLevel(level);
	}
}
