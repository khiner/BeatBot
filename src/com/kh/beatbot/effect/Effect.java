package com.kh.beatbot.effect;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

	protected int trackId, position;
	protected int xParamIndex = 0, yParamIndex = 1;
	protected boolean on;

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
		initParams();
		for (Param param : params) {
			param.addListener(this);
		}
		addEffect(trackId, getId(), position);
		setDefaultParams();
		setOn(true);
	}

	public JsonObject serialize(Gson gson) {
		JsonObject object = new JsonObject();
		object.addProperty("name", getName());
		object.addProperty("trackId", getTrackId());
		object.addProperty("position", getPosition());
		object.addProperty("on", isOn());
		object.add("levels", gson.toJsonTree(getLevels()).getAsJsonArray());
		object.addProperty("class", getClass().getName());
		return object;
	}

	public void deserialize(Gson gson, JsonObject jsonObject) {
		setOn(jsonObject.get("on").getAsBoolean());
		setLevels(gson.fromJson(jsonObject.get("levels"), float[].class));
	}

	public abstract int getId();

	public abstract String getName();

	protected abstract void initParams();

	public int getNumParams() {
		return params.size();
	}

	public int getTrackId() {
		return trackId;
	}

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

	public void setPosition(int position) {
		this.position = position;
	}

	public Param getParam(int paramNum) {
		return params.get(paramNum);
	}

	public String getParamValueString(int paramNum) {
		return getParam(paramNum).getFormattedValue();
	}

	public void destroy() {
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
