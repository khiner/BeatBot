package com.kh.beatbot;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;

public class FlangerActivity extends EffectActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NUM_PARAMS = 6;
		setContentView(R.layout.flanger_layout);
		initParams();
		((ToggleButton)findViewById(R.id.effectToggleOn)).setChecked(GlobalVars.flangerOn[trackNum]);
	}
			
	public void setEffectOn(boolean on) {
		GlobalVars.flangerOn[trackNum] = on;
		setFlangerOn(trackNum, on);
	}
	
	@Override
	public void setParamLevel(int paramNum, float level) {		
		super.setParamLevel(paramNum, level);
		switch (paramNum) {
		case 0: GlobalVars.flangerX[trackNum] = level;
			break;
		case 1: GlobalVars.flangerY[trackNum] = level;
			break;
		case 2: GlobalVars.flangerWet[trackNum] = level;
			break;
		case 3: GlobalVars.flangerModRate[trackNum] = level;
			// exponential scale for mod rate
			level = scaleLevel(level);
			break;
		case 4: GlobalVars.flangerModAmt[trackNum] = level;
			break;
		case 5: GlobalVars.flangerPhase[trackNum] = level;
			break;
		default:
			return;
		}
		setFlangerParam(trackNum, paramNum, level);
	}
	
	public native void setFlangerOn(int trackNum, boolean on);
	public native void setFlangerParam(int trackNum, int paramNum, float param);

	@Override
	public float getParamLevel(int paramNum) {
		switch (paramNum) {
		case 0:  return GlobalVars.flangerX[trackNum];
		case 1:  return GlobalVars.flangerY[trackNum];
		case 2:  return GlobalVars.flangerWet[trackNum]; 
		case 3:  return GlobalVars.flangerModRate[trackNum];
		case 4:  return GlobalVars.flangerModAmt[trackNum];
		default: return 0;
		}		
	}

	@Override
	public String getParamSuffix(int paramNum) {
		switch (paramNum) {
		case 0:
			return "ms"; // time in mn;
		case 1:
			return "fb";
		case 2:
			return ""; // wet/dry has naked units 0-1
		case 3:
			return "Hz"; // rate in Hz
		case 4:
			return ""; // naked units for mod amt
		default:
			return "";
		}
	}
}
