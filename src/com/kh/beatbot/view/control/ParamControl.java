package com.kh.beatbot.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.view.TextView;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;

public class ParamControl extends TouchableBBView {
	public static final float ¹ = (float) Math.PI;
	
	private Param param;
	
	public Knob knob;
	private TextView label, valueLabel;
	
	public ParamControl(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public ParamControl(TouchableSurfaceView parent, Param param) {
		super(parent);
		this.param = param;
	}
	
	public void setParam(Param param) {
		this.param = param;
		knob.setViewLevel(param.viewLevel);
		setLabel(param.name);
		updateValue();
	}
	
	public void loadIcons() {
		setParam(param);
	}
	
	public void updateValue() {
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
		knob = new Knob((TouchableSurfaceView)root);
		label = new TextView(root);
		valueLabel = new TextView(root);
		addChild(knob);
		addChild(label);
		addChild(valueLabel);
	}

	@Override
	public void layoutChildren() {
		label.layout(this, 0, 0, width, height / 4);
		knob.layout(this, 0, height / 4, width, height / 2);
		valueLabel.layout(this, 0, 4 * height / 5, width, height / 5);
	}
}
