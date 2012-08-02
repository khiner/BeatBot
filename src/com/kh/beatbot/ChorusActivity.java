package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class ChorusActivity extends EffectActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NUM_PARAMS = 5;
		setContentView(R.layout.chorus_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.chorusOn[trackNum]);
	}
			
	public void setEffectOn(boolean on) {
		GlobalVars.chorusOn[trackNum] = on;
		setChorusOn(trackNum, on);
	}
	
	@Override
	public void setParamLevel(int paramNum, float level) {		
		switch (paramNum) {
		case 2: GlobalVars.chorusWet[trackNum] = level;
			break;
		case 3: GlobalVars.chorusModRate[trackNum] = level;
			break;
		case 4: GlobalVars.chorusModAmt[trackNum] = level;
			break;
		default:
			return;
		}
		setChorusParam(trackNum, paramNum, scaleLevel(level));
	}

	@Override
	public float getParamLevel(int paramNum) {
		switch (paramNum) {
		case 2:
			return GlobalVars.chorusWet[trackNum]; 
		case 3:
			return GlobalVars.chorusModRate[trackNum];
		case 4:
			return GlobalVars.chorusModAmt[trackNum];
		default:
			return 0;
		}
	}
	
	public String getParamSuffix(int paramNum) {
		switch (paramNum) {
		case 2:
			return ""; // wet value is naked 0-1
		case 3:
			return "Hz";  // mod rate is in Hz
		case 4:
			return ""; // mod amt is naked 0-1
		default:
			return "";
		}
	}
	
	public native void setChorusOn(int trackNum, boolean on);
	public native void setChorusParam(int trackNum, int paramNum, float param);
}
