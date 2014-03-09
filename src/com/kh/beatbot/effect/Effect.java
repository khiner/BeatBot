package com.kh.beatbot.effect;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.manager.TrackManager;

public abstract class Effect implements Comparable<Effect>, ParamListener {

	public static enum LevelType {
		VOLUME, PAN, PITCH
	};

	// also in jni/Track.h: ugly but necessary
	public final static int MAX_EFFECTS_PER_TRACK = 3;
	protected List<EffectParam> params = new ArrayList<EffectParam>();

	protected BaseTrack track;
	protected int position, xParamIndex = 0, yParamIndex = 1;
	protected boolean on, paramsLinked;

	public Effect(BaseTrack track) {
		this.track = track;
		initParams();
		for (Param param : params) {
			param.addListener(this);
		}
	}

	public Effect(BaseTrack track, int position) {
		this.track = track;
		this.position = position;
		paramsLinked = false;
		initParams();
		for (Param param : params) {
			param.addListener(this);
		}
		addEffect(track.getId(), getNum(), position);
		setDefaultParams();
		setOn(true);
	}

	public abstract int getNum();

	public abstract String getName();

	public int getNumParams() {
		return params.size();
	}

	protected abstract void initParams();

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

	public EffectParam getParam(int paramNum) {
		return params.get(paramNum);
	}

	public String getParamValueString(int paramNum) {
		return getParam(paramNum).getFormattedValue();
	}

	public void removeEffect() {
		removeEffect(track.getId(), position);
		TrackManager.getBaseTrack(track.getId()).removeEffect(this);
	}

	public Param getXParam() {
		return getParam(xParamIndex);
	}

	public Param getYParam() {
		return getParam(yParamIndex);
	}

	public void quantizeParams() {
		for (int i = 0; i < params.size(); i++) {
			EffectParam param = params.get(i);
			if (param.isBeatSync()) {
				param.setLevel(param.viewLevel);
				setEffectParam(track.getId(), position, i, param.level);
			}
		}
	}

	@Override
	public void onParamChanged(Param param) {
		setEffectParam(track.getId(), position, param.id, param.level);
	}

	@Override
	public int compareTo(Effect effect) {
		return this.position - effect.position;
	}

	private void setDefaultParams() {
		for (int i = 0; i < params.size(); i++) {
			EffectParam param = params.get(i);
			setEffectParam(track.getId(), position, i, param.level);
		}
	}

	public native void addEffect(int trackNum, int effectNum, int position);

	public native void removeEffect(int trackNum, int position);

	public static native void setEffectPosition(int trackNum, int oldPosition, int newPosition);

	public native void setEffectOn(int trackNum, int effectPosition, boolean on);

	public native void setEffectParam(int trackNum, int effectPosition, int paramNum,
			float paramLevel);
}
