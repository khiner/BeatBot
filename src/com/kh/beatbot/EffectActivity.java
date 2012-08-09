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
import com.kh.beatbot.view.TronKnob;
import com.kh.beatbot.view.TronSeekbar2d;

public abstract class EffectActivity extends Activity implements LevelListener {
	public class EffectParam {
		public float level;
		public boolean beatSync;
		public boolean logScale;
		public String  unitString;
		
		public EffectParam() {
			level = 0.5f;
			beatSync = false;
			logScale = false;
			unitString = "";
		}
		
		public EffectParam(boolean logScale, String unitString) {
			level = 0.5f;
			this.beatSync = false;
			this.logScale = logScale;
			this.unitString = unitString;
		}
	}
	
	protected int EFFECT_NUM;
	protected int NUM_PARAMS;
	protected int trackNum;
	protected List<EffectControlLayout> paramControls  = new ArrayList<EffectControlLayout>();
	protected TronKnob xParamKnob = null, yParamKnob = null; 
	protected TronSeekbar2d level2d = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		trackNum = getIntent().getExtras().getInt("trackNum");
		setContentView(getEffectLayoutId());
		initParams();
		initParamControls();
			
		((ToggleButton) findViewById(R.id.effectToggleOn))
				.setChecked(GlobalVars.effectOn[trackNum][EFFECT_NUM]);
		xParamKnob = paramControls.get(0).getKnob();
		yParamKnob = paramControls.get(1).getKnob();
		for (EffectControlLayout paramControl : paramControls)
			setLevel(paramControl.getKnob(), 0.5f);
	}
	
	protected abstract void initParams();
	
	protected void initParamControls() {
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
	
	public void updateXYViewLevel() {
		level2d.setViewLevelX(xParamKnob.getLevel());
		level2d.setViewLevelY(yParamKnob.getLevel());
	}
	public String getParamValueString(int paramNum) {
		EffectParam param = GlobalVars.params[trackNum][EFFECT_NUM].get(paramNum); 
		return String.format("%.3f", param.level) + " " + param.unitString;
	}
	
	public abstract int getEffectLayoutId();
	public abstract void setEffectOnNative(boolean on);
	public abstract float setParamNative(int paramNum, float level);

	public void toggleOn(View view) {
		boolean on = ((ToggleButton) view).isChecked();
		GlobalVars.effectOn[trackNum][EFFECT_NUM] = on;
		setEffectOnNative(on);
	}
	
	private final void setParamLevel(int paramNum, float level) {
		EffectParam param = GlobalVars.params[trackNum][EFFECT_NUM].get(paramNum);
		if (param.logScale) {
			level = scaleLevel(level);
		}
		if (param.beatSync) {
			level = quantizeToBeat(level);
		}
		level = setParamNative(paramNum, level);
		param.level = level;
	}

	@Override
	public void setLevel(LevelListenable levelListenable, float level) {
		int paramNum = levelListenable.getId();
		setParamLevel(paramNum, level);
		updateXYViewLevel();
		updateParamValueLabel(paramNum);
	}
	
	@Override
	public void setLevel(LevelListenable level2d, float levelX, float levelY) {
		xParamKnob.setLevel(levelX);
		yParamKnob.setLevel(levelY);
		updateParamValueLabel(xParamKnob.getId());
		updateParamValueLabel(yParamKnob.getId());
	}

	@Override
	public void notifyPressed(LevelListenable listenable, boolean pressed) {
		// do nothing
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
		// nothing
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
