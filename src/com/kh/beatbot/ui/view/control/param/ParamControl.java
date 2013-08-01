package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.Level1dListener;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.TextView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.control.ControlView1dBase;
import com.kh.beatbot.ui.view.control.ControlViewBase;
import com.kh.beatbot.ui.view.control.ValueLabel;

public abstract class ParamControl extends TouchableView implements Level1dListener {
	protected static ShapeGroup shapeGroup;
	
	protected ControlView1dBase levelControl;
	protected ValueLabel valueLabel;
	protected TextView label;
	protected Param param;
	
	public ParamControl() {
		if (shapeGroup == null) {
			shapeGroup = new ShapeGroup();
		}
		label = new TextView();
		valueLabel = new ValueLabel(null, param);
		addChild(label);
		addChild(valueLabel);
	}
	
	@Override
	public void setId(int id) {
		super.setId(id);
		levelControl.setId(id);
		valueLabel.setId(id);
	}
	
	public void setParam(Param param) {
		this.param = param;
		valueLabel.setParam(param);
		levelControl.setViewLevel(param.viewLevel);
		label.setText(param.getName());
	}
	
	public void setViewLevel(float viewLevel) {
		levelControl.setViewLevel(viewLevel);
		valueLabel.setViewLevel(viewLevel);
	}
	
	@Override
	public void layoutChildren() {
		label.layout(this, 0, 0, width, height / 6);
		levelControl.layout(this, 0, height / 6, width, width);
		valueLabel.layout(this, 0, 4 * height / 5, width, height / 5);
	}

	public void addLevelListener(Level1dListener listener) {
		levelControl.addLevelListener(listener);
		valueLabel.addLevelListener(listener);
	}
	
	public float getLevel() {
		return levelControl.getLevel();
	}

	public void setLevel(float level) {
		levelControl.setLevel(level); // only need only control to send the actual event
	}

	@Override
	public void onLevelChange(ControlViewBase levelListenable, float level) {
		setViewLevel(level);
	}
}
