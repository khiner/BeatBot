package com.kh.beatbot;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.KarlHiner.BeatBot.R;
import com.kh.beatbot.listener.Level2dListener;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.XYView;

public abstract class EffectActivity extends Activity implements LevelListener, Level2dListener {
	
	protected int trackNum;
	protected TronSeekbar xLevelBar = null;
	protected TronSeekbar yLevelBar = null;
	protected XYView level2d = null;

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
		level2d = (XYView)findViewById(R.id.xy_view);
		xLevelBar.addLevelListener(this);
		yLevelBar.addLevelListener(this);
		level2d.addLevelListener(this);
	}
	
	@Override
	public void setLevel(TronSeekbar levelBar, float level) {
		if (levelBar.equals(xLevelBar)) {
			level2d.setViewLevelX(level);
			setXValue(level);
		}
		else if (levelBar.equals(yLevelBar)) {
			level2d.setViewLevelY(level);
			setYValue(level);
		}
	}

	@Override
	public void setLevel(XYView level2d, float levelX, float levelY) {
		xLevelBar.setViewLevel(levelX);
		yLevelBar.setViewLevel(levelY);
		setXValue(levelX);
		setYValue(levelY);
	}
	
	@Override
	public void notifyChecked(TronSeekbar levelBar, boolean checked) {
		// do nothing
	}
	
	@Override
	public void notifyChecked(XYView level2d, boolean checked) {
		setEffectDynamic(checked);
	}
	
	@Override
	public void notifyInit(TronSeekbar levelBar) {
		if (levelBar.equals(xLevelBar))
			xLevelBar.setViewLevel(getXValue());
		else if (levelBar.equals(yLevelBar))
			yLevelBar.setViewLevel(getYValue());
	}
	
	@Override
	public void notifyInit(XYView level2d) {
		level2d.setViewLevelX(getXValue());
		level2d.setViewLevelY(getYValue());
	}	
}
