package com.kh.beatbot;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.TronSeekbar;

public abstract class EffectActivity extends Activity implements LevelListener {
	
	protected int trackNum;
	protected TronSeekbar xLevelBar = null;
	protected TronSeekbar yLevelBar = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
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
	
	public abstract void setEffectDynamic(boolean dynamic);
	
	public void toggleEffect(View view) {
		setEffectOn(((ToggleButton)view).isChecked());
	}

	protected void initLevelBars() {
		xLevelBar = (TronSeekbar)findViewById(R.id.xParamBar);
		yLevelBar = (TronSeekbar)findViewById(R.id.yParamBar);
		xLevelBar.setLevelListener(this);
		yLevelBar.setLevelListener(this);
	}
	
	@Override
	public void setLevel(TronSeekbar levelBar, float level) {
		if (levelBar.equals(xLevelBar))
			setXValue(level);
		else if (levelBar.equals(yLevelBar))
			setYValue(level);
	}

	@Override
	public void setLevelChecked(TronSeekbar levelBar, boolean checked) {
		// do nothing
	}
	
}
