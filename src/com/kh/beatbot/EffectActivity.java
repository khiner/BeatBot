package com.kh.beatbot;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ToggleButton;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.layout.EffectControlLayout;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.view.TronKnob;
import com.kh.beatbot.view.TronSeekbar2d;

public abstract class EffectActivity extends Activity implements LevelListener {
	public class EffectParam {
		public float level, viewLevel;
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

	protected static int EFFECT_NUM;
	protected static int NUM_PARAMS;
	protected static int trackNum;
	protected List<EffectControlLayout> paramControls = new ArrayList<EffectControlLayout>();
	protected TronKnob xParamKnob = null, yParamKnob = null;
	protected TronSeekbar2d level2d = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		trackNum = getIntent().getExtras().getInt("trackNum");
		setContentView(R.layout.effect_layout);
		ViewGroup parent = (ViewGroup) findViewById(R.id.effect_layout);
		View view = LayoutInflater.from(getBaseContext()).inflate(
				getParamLayoutId(), null);
		parent.addView(view, 0);
		initParams();
		initParamControls();

		((ToggleButton) findViewById(R.id.effectToggleOn))
				.setChecked(GlobalVars.effectOn[trackNum][EFFECT_NUM]);
		xParamKnob = paramControls.get(0).getKnob();
		yParamKnob = paramControls.get(1).getKnob();
		for (EffectControlLayout paramControl : paramControls) {
			int paramNum = paramControl.getKnob().getId();
			updateParamValueLabel(paramNum);
		}
	}

	protected abstract void initParams();

	protected void initParamControls() {
		paramControls = new ArrayList<EffectControlLayout>();
		paramControls.add((EffectControlLayout) findViewById(R.id.param1));
		paramControls.add((EffectControlLayout) findViewById(R.id.param2));
		if (NUM_PARAMS > 2) {
			paramControls.add((EffectControlLayout) findViewById(R.id.param3));
		}
		if (NUM_PARAMS > 3) {
			paramControls.add((EffectControlLayout) findViewById(R.id.param4));
		}
		if (NUM_PARAMS > 4) {
			paramControls.add((EffectControlLayout) findViewById(R.id.param5));
		}
		if (NUM_PARAMS > 5) {
			paramControls.add((EffectControlLayout) findViewById(R.id.param6));
		}
		for (int i = 0; i < paramControls.size(); i++) {
			EffectControlLayout ecl = paramControls.get(i);
			ecl.getKnob().setId(i);
			ecl.getKnob().removeAllListeners();
			ecl.getKnob().addLevelListener(this);
		}
		level2d = (TronSeekbar2d) findViewById(R.id.xyParamBar);
		level2d.removeAllListeners();
		level2d.addLevelListener(this);
	}

	public void updateParamValueLabel(int paramNum) {
		paramControls.get(paramNum)
				.setValueLabel(getParamValueString(paramNum));
	}

	public void updateXYViewLevel() {
		level2d.setViewLevelX(xParamKnob.getLevel());
		level2d.setViewLevelY(yParamKnob.getLevel());
	}

	public static EffectParam getParam(int paramNum) {
		return GlobalVars.params[trackNum][EFFECT_NUM].get(paramNum);
	}

	public String getParamValueString(int paramNum) {
		EffectParam param = getParam(paramNum);
		return String.format("%.3f", param.level) + " " + param.unitString;
	}

	public abstract int getParamLayoutId();

	public static void toggleOn(View view) {
		boolean on = ((ToggleButton) view).isChecked();
		GlobalVars.effectOn[trackNum][EFFECT_NUM] = on;
		setEffectOnNative(on);
	}

	protected static final void setParamLevel(int paramNum, float level) {
		EffectParam param = getParam(paramNum);
		param.viewLevel = level;
		setParamLevel(param, level);
		setParamNative(paramNum, param.level);
	}

	@Override
	public void setLevel(LevelListenable levelListenable, float level) {
		int paramNum = levelListenable.getId();
		setParamLevel(paramNum, level);
		updateXYViewLevel();
		updateParamValueLabel(paramNum);
	}

	@Override
	public void setLevel(LevelListenable level2d, float levelX, float levelY) {
		xParamKnob.setLevel(levelX);
		yParamKnob.setLevel(levelY);
		updateParamValueLabel(xParamKnob.getId());
		updateParamValueLabel(yParamKnob.getId());
	}

	@Override
	public void notifyPressed(LevelListenable listenable, boolean pressed) {
		// do nothing
	}

	@Override
	public void notifyClicked(LevelListenable listenable) {
		if (!(listenable instanceof TronSeekbar2d)) {
			EffectParam param = getParam(listenable.getId());
			param.beatSync = ((TronKnob) listenable).isBeatSync();
			listenable.setLevel(param.viewLevel);
		}
	}

	@Override
	public void notifyInit(final LevelListenable listenable) {
		// need to use the thread that created the text label view to update label
		// (which is done when listenable notifies after 'setLevel')
		Handler refresh = new Handler(Looper.getMainLooper());
		refresh.post(new Runnable() {
			public void run() {
				if (!(listenable instanceof TronSeekbar2d)) {
					EffectParam param = getParam(listenable.getId());
					listenable.setLevel(param.viewLevel);
				}
			}
		});
	}

	protected static void setParamLevel(EffectParam param, float level) {
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
			param.level *= 16;
	}

	protected static void quantizeToBeat(EffectParam param, float level) {
		int numSixteenthNotes = param.hz ? 16 - (int) FloatMath
				.ceil(level * 16) : (int) FloatMath.ceil(level * 16);
		numSixteenthNotes = numSixteenthNotes > 0 ? numSixteenthNotes : 1;
		param.level = (60f * numSixteenthNotes) / (MidiManager.getBPM() * 16f);
		if (param.hz)
			param.level = 1 / param.level;
	}

	public static void setEffectOnNative(boolean on) {
		switch (EFFECT_NUM) {
		case GlobalVars.CHORUS_EFFECT_NUM:
			setChorusOn(trackNum, on);
			return;
		case GlobalVars.DECIMATE_EFFECT_NUM:
			setDecimateOn(trackNum, on);
			return;
		case GlobalVars.DELAY_EFFECT_NUM:
			setDelayOn(trackNum, on);
			return;
		case GlobalVars.FILTER_EFFECT_NUM:
			setFilterOn(trackNum, on);
			return;
		case GlobalVars.FLANGER_EFFECT_NUM:
			setFlangerOn(trackNum, on);
			return;
		case GlobalVars.REVERB_EFFECT_NUM:
			setReverbOn(trackNum, on);
			return;
		case GlobalVars.TREMELO_EFFECT_NUM:
			setTremeloOn(trackNum, on);
			return;
		}
	}

	public static void setParamNative(int paramNum, float paramLevel) {
		switch (EFFECT_NUM) {
		case GlobalVars.CHORUS_EFFECT_NUM:
			setChorusParam(trackNum, paramNum, paramLevel);
			return;
		case GlobalVars.DECIMATE_EFFECT_NUM:
			setDecimateParam(trackNum, paramNum, paramLevel);
			return;
		case GlobalVars.DELAY_EFFECT_NUM:
			setDelayParam(trackNum, paramNum, paramLevel);
			return;
		case GlobalVars.FILTER_EFFECT_NUM:
			setFilterParam(trackNum, paramNum, paramLevel);
			return;
		case GlobalVars.FLANGER_EFFECT_NUM:
			setFlangerParam(trackNum, paramNum, paramLevel);
			return;
		case GlobalVars.REVERB_EFFECT_NUM:
			setReverbParam(trackNum, paramNum, paramLevel);
			return;
		case GlobalVars.TREMELO_EFFECT_NUM:
			setTremeloParam(trackNum, paramNum, paramLevel);
			return;
		}
	}

	public static native void setChorusOn(int trackNum, boolean on);

	public static native void setChorusParam(int trackNum, int paramNum,
			float param);

	public static native void setDecimateOn(int trackNum, boolean on);

	public static native void setDecimateParam(int trackNum, int paramNum,
			float param);

	public static native void setDelayOn(int trackNum, boolean on);

	public static native void setDelayParam(int trackNum, int paramNum,
			float param);

	public static native void setFilterOn(int trackNum, boolean on);

	public static native void setFilterParam(int trackNum, int paramNum,
			float param);

	public static native void setFlangerOn(int trackNum, boolean on);

	public static native void setFlangerParam(int trackNum, int paramNum,
			float param);

	public static native void setReverbOn(int trackNum, boolean on);

	public static native void setReverbParam(int trackNum, int paramNum,
			float param);

	public static native void setTremeloOn(int trackNum, boolean on);

	public static native void setTremeloParam(int trackNum, int paramNum,
			float param);
}
