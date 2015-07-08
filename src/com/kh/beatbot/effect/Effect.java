package com.kh.beatbot.effect;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.manager.TrackManager;

public abstract class Effect implements Comparable<Effect>, ParamListener {

	public static enum LevelType {
		VOLUME, PAN, PITCH
	};

	// also in jni/Track.h: ugly but necessary
	public final static int MAX_EFFECTS_PER_TRACK = 3;
	public final static String NEW_EFFECT_LABEL = "Effect";

	protected List<Param> params = new ArrayList<Param>();

	protected int trackId;
	protected int position, xParamIndex = 0, yParamIndex = 1;
	protected boolean on, paramsLinked;

	public Effect() {
		this(-2);
	}

	public Effect(int trackId) {
		this.trackId = trackId;
		initParams();
		for (Param param : params) {
			param.addListener(this);
		}
	}

	public Effect(int trackId, int position) {
		this.trackId = trackId;
		this.position = position;
		paramsLinked = false;
		initParams();
		for (Param param : params) {
			param.addListener(this);
		}
		addEffect(trackId, getId(), position);
		setDefaultParams();
		setOn(true);
	}

	public abstract int getId();

	public abstract String getName();

	public int getNumParams() {
		return params.size();
	}

	protected abstract void initParams();

	public void setTrackId(int trackId) {
		this.trackId = trackId;
	}

	public void setOn(boolean on) {
		this.on = on;
		setEffectOn(trackId, position, on);
	}

	public boolean isOn() {
		return on;
	}

	public int getPosition() {
		return position;
	}

	public float[] getLevels() {
		float[] levels = new float[params.size()];
		for (int i = 0; i < params.size(); i++) {
			levels[i] = params.get(i).viewLevel;
		}
		return levels;
	}

	public void setLevels(float[] levels) {
		for (int i = 0; i < levels.length; i++) {
			if (i < params.size()) {
				Param param = params.get(i);
				param.setLevel(levels[i]);
			}
		}
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

	public String getParamValueString(int paramNum) {
		return getParam(paramNum).getFormattedValue();
	}

	public void removeEffect() {
		removeEffect(trackId, position);
		TrackManager.getBaseTrackById(trackId).removeEffect(this);
	}

	public Param getXParam() {
		return getParam(xParamIndex);
	}

	public Param getYParam() {
		return getParam(yParamIndex);
	}

	public void quantizeParams() {
		for (int i = 0; i < params.size(); i++) {
			Param param = params.get(i);
			if (param.isBeatSync()) {
				param.setLevel(param.viewLevel);
				setEffectParam(trackId, position, i, param.level);
			}
		}
	}

	@Override
	public void onParamChanged(Param param) {
		setEffectParam(trackId, position, param.id, param.level);
	}

	@Override
	public int compareTo(Effect effect) {
		return this.position - effect.position;
	}

	private void setDefaultParams() {
		for (int i = 0; i < params.size(); i++) {
			Param param = params.get(i);
			setEffectParam(trackId, position, i, param.level);
		}
	}

	public native void addEffect(int trackId, int effectId, int position);

	public native void removeEffect(int trackId, int position);

	public static native void setEffectPosition(int trackId, int oldPosition, int newPosition);

	public native void setEffectOn(int trackId, int effectPosition, boolean on);

	public native void setEffectParam(int trackId, int effectPosition, int paramNum,
			float paramLevel);
}
