package com.kh.beatbot.effect;

import java.util.ArrayList;
import java.util.List;

import android.util.FloatMath;

import com.kh.beatbot.manager.Managers;

public abstract class Effect implements Comparable<Effect> {
	public class EffectParam {
		public float level, viewLevel, scale = 1;
		public int topBeatNum = 1, bottomBeatNum = 1;
		public boolean hz = false;
		public boolean beatSync;
		public boolean logScale;
		public String unitString;

		public EffectParam(boolean logScale, boolean beatSync, String unitString) {
			level = viewLevel = 0.5f;
			this.beatSync = beatSync;
			this.logScale = logScale;
			this.unitString = unitString;
		}
	}

	protected List<EffectParam> params = new ArrayList<EffectParam>();

	protected int id;
	protected int effectNum;
	protected int trackNum;
	protected int numParams;
	protected int position;

	public String name;
	public boolean on = true;
	protected boolean paramsLinked = false;

	public Effect(int id, String name, int trackNum, int position) {
		this.id = id;
		this.name = name;
		this.trackNum = trackNum;
		this.position = position;
		initParams();
		addEffect(trackNum, effectNum, id, position);
	}

	public void setOn(boolean on) {
		this.on = on;
		setEffectOn(trackNum, id, on);
	}

	public int getId() {
		return id;
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
		setEffectPosition(trackNum, id, position);
	}

	public void incPosition() {
		this.position++;
	}

	public void setParamsLinked(boolean linked) {
		this.paramsLinked = linked;
	}

	public EffectParam getParam(int paramNum) {
		return params.get(paramNum);
	}

	public String getParamValueString(int paramNum) {
		EffectParam param = getParam(paramNum);
		if (param.beatSync)
			return param.topBeatNum
					+ (param.bottomBeatNum == 1 ? "" : "/"
							+ param.bottomBeatNum);
		else
			return String.format("%.2f", param.level * param.scale) + " "
					+ param.unitString;
	}

	public final void setParamLevel(int paramNum, float level) {
		EffectParam param = getParam(paramNum);
		param.viewLevel = level;
		setParamLevel(param, level);
		setEffectParam(trackNum, id, paramNum, param.level);
	}

	public void setParamLevel(EffectParam param, float level) {
		if (param.beatSync) {
			quantizeToBeat(param, level);
		} else if (param.logScale) {
			logScaleLevel(param, level);
		} else {
			param.level = level;
		}
	}

	protected static void logScaleLevel(EffectParam param, float level) {
		param.level = (float) (Math.pow(9, level) - 1) / 8;
		if (param.hz)
			param.level *= 32;
	}

	protected static void quantizeToBeat(EffectParam param, float level) {
		param.topBeatNum = getTopBeatNum((int) FloatMath.ceil(level * 14));
		param.bottomBeatNum = getBottomBeatNum((int) FloatMath.ceil(level * 14));
		param.level = (60f / (Managers.midiManager.getBPM()) * ((float) param.topBeatNum / (float) param.bottomBeatNum));
		if (param.hz)
			param.level = 1 / param.level;
	}

	private static int getTopBeatNum(int which) {
		switch (which) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
			return 1;
		case 5:
			return 3;
		case 6:
			return 1;
		case 7:
			return 5;
		case 8:
			return 1;
		case 9:
			return 3;
		case 10:
			return 1;
		case 11:
			return 3;
		case 12:
			return 1;
		case 13:
			return 3;
		case 14:
			return 2;
		default:
			return 1;
		}
	}

	private static int getBottomBeatNum(int which) {
		switch (which) {
		case 0:
		case 1:
			return 16;
		case 2:
			return 12;
		case 3:
			return 8;
		case 4:
			return 6;
		case 5:
			return 16;
		case 6:
			return 4;
		case 7:
			return 16;
		case 8:
			return 3;
		case 9:
			return 8;
		case 10:
			return 2;
		case 11:
			return 4;
		case 12:
			return 1;
		case 13:
			return 2;
		case 14:
			return 1;
		default:
			return 1;
		}
	}

	public void removeEffect() {
		removeEffect(trackNum, id);
		Managers.trackManager.getTrack(trackNum).effects.remove(this);
	}

	public native void addEffect(int trackNum, int effectNum, int effectId,
			int position);

	public native void removeEffect(int trackNum, int id);

	public native void setEffectPosition(int trackNum, int effectId,
			int position);

	public native void setEffectOn(int trackNum, int effectId, boolean on);

	public native void setEffectParam(int trackNum, int effectId, int paramNum,
			float paramLevel);

	protected abstract void initParams();

	public abstract int getParamLayoutId();

	public abstract int getOnDrawableId();

	public abstract int getOffDrawableId();

	@Override
	public int compareTo(Effect effect) {
		return this.position - effect.position;
	}
}
