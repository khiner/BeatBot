package com.kh.beatbot.effect;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.manager.TrackManager;

public abstract class Effect implements Comparable<Effect> {
	
	public static enum LevelType {
		VOLUME, PAN, PITCH
	};

	// also in jni/Track.h: ugly but necessary
	public final static int MAX_EFFECTS_PER_TRACK = 3;
	protected List<Param> params = new ArrayList<Param>();

	protected BaseTrack track;
	protected int position;
	protected boolean on, paramsLinked;

	public Effect(BaseTrack track) {
		this.track = track;
		initParams();
	}

	public Effect(BaseTrack track, int position) {
		this.track = track;
		this.position = position;
		paramsLinked = false;
		initParams();
		addEffect(track.getId(), getNum(), position);
		setDefaultParams();
		setOn(true);
	}

	public abstract int getNum();
	public abstract int numParams();
	public abstract String getName();
	public abstract ParamData[] getParamsData();
	
	public void setOn(boolean on) {
		this.on = on;
		setEffectOn(track.getId(), position, on);
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
		setEffectParam(track.getId(), position, paramNum, param.level);
	}

	public String getParamValueString(int paramNum) {
		return getParam(paramNum).getFormattedValueString();
	}

	public void removeEffect() {
		removeEffect(track.getId(), position);
		TrackManager.getBaseTrack(track.getId()).removeEffect(this);
	}

	public void quantizeParams() {
		for (int i = 0; i < params.size(); i++) {
			Param param = params.get(i);
			if (param.beatSync) {
				param.setLevel(param.viewLevel);
				setEffectParam(track.getId(), position, i, param.level);
			}
		}
	}

	@Override
	public int compareTo(Effect effect) {
		return this.position - effect.position;
	}
	
	private void setDefaultParams() {
		for (int i = 0; i < params.size(); i++) {
			Param param = params.get(i);
			setEffectParam(track.getId(), position, i, param.level);
		}
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
