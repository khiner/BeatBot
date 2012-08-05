package com.kh.beatbot;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.layout.EffectControlLayout;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.TronSeekbar2d;

public abstract class EffectActivity extends Activity implements LevelListener {
	public class EffectParam {
		public float level;
		public boolean beatSync;
		public boolean logScale;
		public char    axis;
		public String  unitString;
		
		public EffectParam() {
			level = 0.5f;
			beatSync = false;
			logScale = false;
			axis = ' ';
			unitString = "";
		}
		
		public EffectParam(boolean logScale, char axis, String unitString) {
			level = 0.5f;
			this.beatSync = false;
			this.logScale = logScale;
			this.axis = axis;
			this.unitString = unitString;
		}
	}
	
	protected int EFFECT_NUM;
	protected int NUM_PARAMS;
	protected int trackNum;
	protected List<EffectControlLayout> paramControls  = new ArrayList<EffectControlLayout>();
	protected TronSeekbar2d level2d = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		trackNum = getIntent().getExtras().getInt("trackNum");
		setContentView(getEffectLayoutId());
		initParams();
		((ToggleButton) findViewById(R.id.effectToggleOn))
				.setChecked(GlobalVars.effectOn[trackNum][EFFECT_NUM]);
	}
	
	protected void initParams() {
		paramControls = new ArrayList<EffectControlLayout>();
		paramControls.add((EffectControlLayout)findViewById(R.id.param1));
		paramControls.add((EffectControlLayout)findViewById(R.id.param2));
		if (NUM_PARAMS > 2) {
			paramControls.add((EffectControlLayout)findViewById(R.id.param3));
		}
		if (NUM_PARAMS > 3) {
			paramControls.add((EffectControlLayout)findViewById(R.id.param4));
		}		
		if (NUM_PARAMS > 4) {
			paramControls.add((EffectControlLayout)findViewById(R.id.param5));
		}
		if (NUM_PARAMS > 5) {
			paramControls.add((EffectControlLayout)findViewById(R.id.param6));
		}
		for (int i = 0; i < paramControls.size(); i++) {
			EffectControlLayout ecl = paramControls.get(i);
			ecl.getKnob().setId(i);
			ecl.getKnob().removeAllListeners();
			ecl.getKnob().addLevelListener(this);
		}
		level2d = (TronSeekbar2d)findViewById(R.id.xyParamBar);
		level2d.removeAllListeners();
		level2d.addLevelListener(this);
	}
	
	public void updateParamValueLabel(int paramNum) {
		paramControls.get(paramNum).setValueLabel(getParamValueString(paramNum));
	}
	
	public abstract int getEffectLayoutId();
	public abstract void setEffectOnNative(boolean on);
	public abstract void setParamNative(int paramNum, float level);

	public void toggleOn(View view) {
		boolean on = ((ToggleButton) view).isChecked();
		GlobalVars.effectOn[trackNum][EFFECT_NUM] = on;
		setEffectOnNative(on);
	}

	public String getParamValueString(int paramNum) {
		EffectParam param = GlobalVars.params[trackNum][EFFECT_NUM].get(paramNum); 
		return ((Float)param.level).toString() + " " + param.unitString;
	}
	
	@Override
	public final void setLevel(LevelListenable levelListenable, float level) {
		int paramNum = levelListenable.getId();
		setParamLevel(paramNum, level);
		if (paramNum == 0)
			level2d.setViewLevelX(level);
		else if (paramNum == 1)
			level2d.setViewLevelY(level);
		updateParamValueLabel(paramNum);
	}
	
	private final void setParamLevel(int paramNum, float level) {
		EffectParam param = GlobalVars.params[trackNum][EFFECT_NUM].get(paramNum);
		if (param.logScale) {
			level = scaleLevel(level);
		}
		if (param.beatSync) {
			level = quantizeToBeat(level);
		}
		setParamNative(paramNum, level);
		param.level = level;
	}

	@Override
	public void setLevel(LevelListenable level2d, float levelX, float levelY) {
		setParamLevel(0, levelX);
		setParamLevel(1, levelY);
		paramControls.get(0).getKnob().setViewLevel(levelX);
		paramControls.get(1).getKnob().setViewLevel(levelY);
		updateParamValueLabel(0);
		updateParamValueLabel(1);
	}

	@Override
	public void notifyPressed(LevelListenable listenable, boolean pressed) {
		// do nothing for
	}

	@Override
	public void notifyClicked(LevelListenable listenable) {
		if (!(listenable instanceof TronSeekbar2d)) {
			EffectParam param = GlobalVars.params[trackNum][EFFECT_NUM].get(listenable.getId()); 
			param.beatSync = !param.beatSync;
		}
	}
	
	@Override
	public final void notifyInit(LevelListenable listenable) {
		if (!(listenable instanceof TronSeekbar2d))
			setParamLevel(listenable.getId(), .5f);
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
