package com.kh.beatbot.effect;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.manager.Managers;

public abstract class Effect implements Comparable<Effect> {
	protected List<Param> params = new ArrayList<Param>();

	protected int effectNum;
	protected int trackNum;
	protected int numParams;
	protected int position;

	public String name;
	public boolean on = true;
	protected boolean paramsLinked = false;

	public Effect(String name, int trackNum, int position) {
		this.name = name;
		this.trackNum = trackNum;
		this.position = position;
		initParams();
		addEffect(trackNum, effectNum, position);
	}

	public void setOn(boolean on) {
		this.on = on;
		setEffectOn(trackNum, position, on);
	}

	public int getPosition() {
		return position;
	}

	public String getName() {
		return name;
	}

	public int getNumParams() {
		return numParams;
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
		Managers.trackManager.getTrack(trackNum).effects.remove(this);
	}

	public native void addEffect(int trackNum, int effectNum, int position);

	public native void removeEffect(int trackNum, int position);

	public static native void setEffectPosition(int trackNum, int oldPosition,
			int newPosition);

	public native void setEffectOn(int trackNum, int effectPosition, boolean on);

	public native void setEffectParam(int trackNum, int effectPosition,
			int paramNum, float paramLevel);

	protected abstract void initParams();

	public abstract int getParamLayoutId();

	public abstract int getOnDrawableId();

	public abstract int getOffDrawableId();

	@Override
	public int compareTo(Effect effect) {
		return this.position - effect.position;
	}
}
