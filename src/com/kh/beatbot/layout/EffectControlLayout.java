package com.kh.beatbot.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kh.beatbot.R;
import com.kh.beatbot.view.TronKnob;

public class EffectControlLayout extends LinearLayout {
	private TextView label = null;
	private TextView valueLabel = null;
	private TronKnob knob = null;
	
    public EffectControlLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        
        LayoutInflater.from(context).inflate(R.layout.effect_control, this, true);
        
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.effect_control_styleable, 0, 0);
        
        String paramLabel = array.getString(R.styleable.effect_control_styleable_param_name);
        String paramValue = array.getString(R.styleable.effect_control_styleable_param_value);
        boolean beatSyncEnabled = array.getBoolean(R.styleable.effect_control_styleable_beatsync_enabled, false);
        
        array.recycle();
        
        if (paramLabel == null) paramLabel = "Param";
        if (paramValue == null) paramValue = "0";
        
        label = (TextView)findViewById(R.id.param_label);
        valueLabel = (TextView)findViewById(R.id.param_value_label);
        knob = (TronKnob)findViewById(R.id.param_knob);
        
        label.setText(paramLabel);
        valueLabel.setText(paramValue);
        knob.setBeatSync(beatSyncEnabled);
    }

    public TronKnob getKnob() {
    	return knob;
    }
    
    public float getLevel() {
    	return knob.getLevel();
    }
    
    public String getValueLabel() {
    	return valueLabel.getText().toString();
    }
    
    public void setLevel(float level) {
    	knob.setViewLevel(level);
    }
    
    public void setValueLabel(String valueLabelText) {
    	valueLabel.setText(valueLabelText);
    }
}