package com.kh.beatbot.effect;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.manager.Managers;

public abstract class Effect implements Comparable<Effect> {
	protected List<Param> params = new ArrayList<Param>();

	protected int trackNum, position;
	protected boolean on, paramsLinked;

	public Effect(int trackNum) {
		this.trackNum = trackNum;
		initParams();
	}

	public Effect(int trackNum, int position) {
		this.trackNum = trackNum;
		this.position = position;
		paramsLinked = false;
		initParams();
		addEffect(trackNum, getNum(), position);
		setOn(true);
	}

	public abstract int getNum();
	public abstract int numParams();
	public abstract String getName();
	public abstract ParamData[] getParamsData();
	
	public void setOn(boolean on) {
		this.on = on;
		setEffectOn(trackNum, position, on);
	}

	public boolean isOn() {
		return on;
	}

	public int getPosition() {
		return position;
	}

	public boolean paramsLinked() {
		return paramsLinked;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setParamsLinked(boolean linked) {
		this.paramsLinked = linked;
	}

	public Param getParam(int paramNum) {
		return params.get(paramNum);
	}

	public final void setParamLevel(int paramNum, float level) {
		Param param = getParam(paramNum);
		param.viewLevel = level;
		param.setLevel(level);
		setEffectParam(trackNum, position, paramNum, param.level);
	}

	public String getParamValueString(int paramNum) {
		return getParam(paramNum).getFormattedValueString();
	}

	public void removeEffect() {
		removeEffect(trackNum, position);
		Managers.trackManager.getBaseTrack(trackNum).removeEffect(this);
	}

	public void quantizeParams() {
		for (int i = 0; i < params.size(); i++) {
			Param param = params.get(i);
			if (param.beatSync) {
				param.setLevel(param.viewLevel);
				setEffectParam(trackNum, position, i, param.level);
			}
		}
	}

	@Override
	public int compareTo(Effect effect) {
		return this.position - effect.position;
	}
	
	protected void initParams() {
		if (params.isEmpty()) {
			for (ParamData paramData : getParamsData()) {
				params.add(new Param(paramData));
			}
		}
	}
	
	public native void addEffect(int trackNum, int effectNum, int position);

	public native void removeEffect(int trackNum, int position);

	public static native void setEffectPosition(int trackNum, int oldPosition,
			int newPosition);

	public native void setEffectOn(int trackNum, int effectPosition, boolean on);

	public native void setEffectParam(int trackNum, int effectPosition,
			int paramNum, float paramLevel);
}
