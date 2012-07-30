package com.kh.beatbot;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.layout.EffectControlLayout;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.TronKnob;
import com.kh.beatbot.view.TronSeekbar;
import com.kh.beatbot.view.TronSeekbar2d;

public abstract class EffectActivity extends Activity implements LevelListener {

	protected int trackNum;
	protected ArrayList<TronKnob> paramKnobs = new ArrayList<TronKnob>();
	protected TronSeekbar2d level2d = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		trackNum = getIntent().getExtras().getInt("trackNum");
	}
	
	protected void initParams(int numParams) {
		EffectControlLayout param = (EffectControlLayout)findViewById(R.id.param1);
		paramKnobs.add((TronKnob)(param.findViewById(R.id.param_knob)));
		param = (EffectControlLayout)findViewById(R.id.param2);
		paramKnobs.add((TronKnob)(param.findViewById(R.id.param_knob)));
		if (numParams > 2) {
			param = (EffectControlLayout)findViewById(R.id.param3);
			paramKnobs.add((TronKnob)(param.findViewById(R.id.param_knob)));
		}
		if (numParams > 3) {
			param = (EffectControlLayout)findViewById(R.id.param4);
			paramKnobs.add((TronKnob)(param.findViewById(R.id.param_knob)));
		}		
		if (numParams > 4) {
			param = (EffectControlLayout)findViewById(R.id.param5);
			paramKnobs.add((TronKnob)(param.findViewById(R.id.param_knob)));
		}
		if (numParams > 5) {
			param = (EffectControlLayout)findViewById(R.id.param6);
			paramKnobs.add((TronKnob)(param.findViewById(R.id.param_knob)));
		}
		for (int i = 0; i < paramKnobs.size(); i++) {
			TronKnob knob = paramKnobs.get(i);
			knob.setId(i);
			knob.addLevelListener(this);
		}
		level2d = (TronSeekbar2d)findViewById(R.id.xyParamBar);
		level2d.addLevelListener(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public abstract float getXValue();

	public abstract float getYValue();

	public abstract int getNumParams();

	public abstract void setXValue(float x);

	public abstract void setYValue(float y);

	public abstract void setEffectOn(boolean on);

	public void toggleOn(View view) {
		setEffectOn(((ToggleButton) view).isChecked());
	}

	@Override
	public void setLevel(LevelListenable levelListenable, float level) {
		switch (levelListenable.getId()) {
		case 0:
			level2d.setViewLevelX(level);
			setXValue(level);
			break;
		case 1:
			level2d.setViewLevelY(level);
			setYValue(level);
			break;
		}
	}

	@Override
	public void setLevel(LevelListenable level2d, float levelX, float levelY) {
		paramKnobs.get(0).setViewLevel(levelX);
		paramKnobs.get(1).setViewLevel(levelY);
		setXValue(levelX);
		setYValue(levelY);
	}

	@Override
	public void notifyChecked(LevelListenable levelBar, boolean checked) {
		// do nothing
	}

	@Override
	public void notifyInit(LevelListenable listenable) {
		if (listenable instanceof TronSeekbar) {
			if (listenable.getTag().equals(0))
				listenable.setViewLevel(getXValue());
			else if (listenable.getTag().equals(1))
				listenable.setViewLevel(getYValue());
		} else if (listenable instanceof TronSeekbar2d) {
			level2d.setViewLevelX(getXValue());
			level2d.setViewLevelY(getYValue());
		}
	}

	protected float scaleLevel(float level) {
		return (float) (Math.pow(9, level) - 1) / 8;
	}

	protected float quantizeToBeat(float level) {
		// minimum beat == 1/16 note = 1(1 << 4)
		for (int pow = 4; pow >= 0; pow--) {
			float quantized = 1f / (1 << pow);
			if (level <= quantized) {
				return quantized;
			}
		}
		return 1;
	}
}
