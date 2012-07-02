package com.kh.beatbot;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.view.TronSeekbar2d;

public class FilterActivity extends EffectActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_layout);
		((ListView) findViewById(R.id.paramListView)).setAdapter(adapter);
		((ToggleButton)findViewById(R.id.effect_toggleOn)).setChecked(GlobalVars.filterOn[trackNum]);
		((TronSeekbar2d)findViewById(R.id.xyParamBar)).addLevelListener(this);
	}
	
	public float getXValue() {
		return GlobalVars.filterX[trackNum];
	}
	
	public float getYValue() {
		return GlobalVars.filterY[trackNum];
	}
	
	public void setXValue(float xValue) {
		Log.d("yo", "yo");
		GlobalVars.filterX[trackNum] = xValue;
		setFilterCutoff(trackNum, xValue);
	}
	
	public void setYValue(float yValue) {
		GlobalVars.filterY[trackNum] = yValue;
		setFilterResonance(trackNum, yValue);
	}
	
	public void setEffectOn(boolean on) {
		GlobalVars.filterOn[trackNum] = on;
		setFilterOn(trackNum, on);
	}
	
	public void setEffectDynamic(boolean dynamic) {
		//setFilterDynamic(trackNum, dynamic);
	}
	
	public void toggleLpHpFilter(View view) {
		setFilterMode(trackNum, ((ToggleButton)findViewById(R.id.lp_hp_toggle)).isChecked());
	}
	
	public native void setFilterOn(int trackNum, boolean on);
	public native void setFilterDynamic(int trackNum, boolean dynamic);
	public native void setFilterMode(int trackNum, boolean lp);	
	public native void setFilterCutoff(int trackNum, float cutoff);
	public native void setFilterResonance(int trackNum, float q);

	@Override
	public int getNumParams() {
		return 2;
	}
	
	@Override
	public String getLabelX() {
		return getResources().getString(R.string.cutoff);
	}

	@Override
	public String getLabelY() {
		return getResources().getString(R.string.resonance);
	}

	@Override
	public String getLabelParam3() {
		return "";
	}
}
