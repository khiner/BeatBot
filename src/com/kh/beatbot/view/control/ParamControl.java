package com.kh.beatbot.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.view.TextView;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;

public class ParamControl extends TouchableBBView {
	public Knob knob;
	private TextView label, valueLabel;

	public ParamControl(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public void setParam(Param param) {
		knob.setViewLevel(param.viewLevel);
		knob.setBeatSync(param.beatSync);
		setLabel(param.name);
		updateValueLabel(param);
	}
	
	public void loadIcons() {
		// no icons
	}
	
	public void updateValueLabel(Param param) {
		setValueLabel(param.getFormattedValueString());
	}
	
	private void setLabel(String label) {
		this.label.setText(label);
	}
	
	private void setValueLabel(String valueLabel) {
		this.valueLabel.setText(valueLabel);
	}

	@Override
	public void init() {
		
	}

	@Override
	public void draw() {
		
	}

	@Override
	protected void createChildren() {
		label = new TextView(root);
		knob = new Knob((TouchableSurfaceView)root);
		valueLabel = new TextView(root);
		addChild(label);
		addChild(knob);
		addChild(valueLabel);
	}

	@Override
	public void layoutChildren() {
		label.layout(this, 0, 0, width, height / 6);
		knob.layout(this, 0, height / 6, width, width);
		valueLabel.layout(this, 0, 5 * height / 6, width, height / 6);
	}
}
