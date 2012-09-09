package com.kh.beatbot.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.manager.PlaybackManager;

public class Filter extends Effect {
	private int mode = 0;
	
	public Filter(int id, String name, int trackNum) {
		super(id, name, trackNum);
	}

	@Override
	public void initParams() {
		numParams = 4;
		effectNum = 3;
		if (params.isEmpty()) {
			params.add(new EffectParam(true, false, "Hz"));
			getParam(0).scale = PlaybackManager.SAMPLE_RATE / 2;
			params.add(new EffectParam(false, false, ""));
			params.add(new EffectParam(true, true, "Hz"));
			getParam(2).hz = true;
			params.add(new EffectParam(false, false, ""));
		}
	}

	public int getMode() {
		return mode;
	}
	
	public void setMode(int mode) {
		this.mode = mode;
		setEffectParam(trackNum, id, 4, mode); 
	}
	
	@Override
	public int getParamLayoutId() {
		return R.layout.filter_param_layout;
	}
	
	@Override
	public int getOnDrawableId() {
		return R.drawable.filter_label_on;
	}
	
	@Override
	public int getOffDrawableId() {
		return R.drawable.filter_label_off;
	}
}
