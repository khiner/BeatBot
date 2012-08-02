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
		public boolean beatSyncable;
		public char    axis;
		public String  unitString;
		
		public EffectParam() {
			level = 0.5f;
			beatSyncable = false;
			axis = ' ';
			unitString = "";
		}
		
		public EffectParam(boolean beatSyncable, char axis, String unitString) {
			this.beatSyncable = beatSyncable;
			this.axis = axis;
			this.unitString = unitString;
		}
	}
	
	protected int NUM_PARAMS;
	protected int trackNum;
	protected List<EffectControlLayout> paramControls = new ArrayList<EffectControlLayout>();
	protected TronSeekbar2d level2d = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		trackNum = getIntent().getExtras().getInt("trackNum");
	}
	
	protected void initParams() {
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
			ecl.getKnob().addLevelListener(this);
		}
		level2d = (TronSeekbar2d)findViewById(R.id.xyParamBar);
		level2d.addLevelListener(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	public void updateParamValueLabel(int paramNum) {
		paramControls.get(paramNum).setValueLabel(getParamValueString(paramNum));
	}
	
	public abstract void setEffectOn(boolean on);
	public abstract void setParamNative(int paramNum, float level);

	public void toggleOn(View view) {
		setEffectOn(((ToggleButton) view).isChecked());
	}

	public String getParamValueString(int paramNum) {
		return ((Float)GlobalVars.params[trackNum].get(paramNum).level).toString() + " " +
				GlobalVars.params[trackNum].get(paramNum).unitString;
	}
	
	@Override
	public final void setLevel(LevelListenable levelListenable, float level) {
		int paramNum = levelListenable.getId();
		setParamLevel(paramNum, level);
		updateParamValueLabel(paramNum);
	}
	
	private final void setParamLevel(int paramNum, float level) {
		GlobalVars.params[trackNum].get(paramNum).level = level;
		setParamNative(paramNum, level);
		if (paramNum == 0)
			level2d.setViewLevelX(level);
		else if (paramNum == 1)
			level2d.setViewLevelY(level);
	}

	@Override
	public void setLevel(LevelListenable level2d, float levelX, float levelY) {
		setParamLevel(0, levelX);
		setParamLevel(1, levelY);
		paramControls.get(0).setLevel(levelX);
		paramControls.get(1).setLevel(levelY);
		updateParamValueLabel(0);
		updateParamValueLabel(1);
	}

	@Override
	public void notifyChecked(LevelListenable levelBar, boolean checked) {
		// do nothing
	}

	@Override
	public final void notifyInit(LevelListenable listenable) {
		if (listenable instanceof TronSeekbar2d)
			return;
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
