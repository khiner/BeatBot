package com.kh.beatbot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;

public abstract class EffectActivity extends Activity {
	
	protected int trackNum;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.effect_layout);
		trackNum = getIntent().getExtras().getInt("trackNum");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public abstract float getXValue();
	public abstract float getYValue();
	public abstract void setXValue(float x);
	public abstract void setYValue(float y);
	
	public abstract void setEffectOn(boolean on);
	
	public void toggleEffect(View view) {
		setEffectOn(((ToggleButton)view).isChecked());
	}
}
