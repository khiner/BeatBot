package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.view.TouchableView;

public class ParamControl extends TouchableView {
	public Knob knob;
	private ImageButton label, valueLabel;
	private boolean beatSync;
	
	public ParamControl(boolean beatSync) {
		this.beatSync = beatSync;
		createChildren();
	}
	
	public void setParam(Param param) {
		knob.setViewLevel(param.viewLevel);
		if (beatSync) {
			((ToggleKnob)knob).setBeatSync(param.beatSync);
		}
		setLabel(param.getName());
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
		label = new ImageButton();
		knob = beatSync ? new ToggleKnob() : new Knob();
		valueLabel = new ImageButton();
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
