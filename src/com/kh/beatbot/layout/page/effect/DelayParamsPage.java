package com.kh.beatbot.layout.page.effect;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Delay;
import com.kh.beatbot.effect.ParamData;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.view.Button;
import com.kh.beatbot.view.ToggleButton;
import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.control.ParamControl;
import com.kh.beatbot.view.control.ToggleKnob;

public class DelayParamsPage extends EffectParamsPage {

	private ToggleButton linkToggle;
	
	public DelayParamsPage(TouchableSurfaceView parent) {
		super(parent);
	}

	@Override
	protected String getName() {
		return Delay.NAME;
	}
	
	@Override
	protected int getNumParams() {
		return Delay.NUM_PARAMS;
	}

	@Override
	public void init() {
		// since left/right delay times are linked by default,
		// xy view is set to x = left channel, y = feedback
		xParamIndex = 0;
		yParamIndex = 2;
	}

	@Override
	protected void loadIcons() {
		linkToggle.setIconSource(new ImageIconSource(R.drawable.link_broken, R.drawable.link));
	}

	@Override
	public void createChildren() {
		super.createChildren();
		linkToggle = new ToggleButton((TouchableSurfaceView) root);
		linkToggle.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				ParamControl leftChannelControl = paramControls[0];
				ParamControl rightChannelControl = paramControls[1];
				ToggleKnob rightKnob = (ToggleKnob)rightChannelControl.knob;
				ToggleKnob leftKnob = (ToggleKnob)leftChannelControl.knob;
				
				float newRightChannelLevel = rightKnob.getLevel();
				boolean newRightChannelSynced = rightKnob.isBeatSync();

				effect.setParamsLinked(linkToggle.isChecked());

				if (effect.paramsLinked()) {
					// y = feedback when linked
					yParamIndex = 2;
					((Delay) effect).rightChannelLevelMemory = rightKnob.getLevel();
					((Delay) effect).rightChannelBeatSyncMemory = rightKnob.isBeatSync();
					newRightChannelLevel = leftChannelControl.knob.getLevel();
					newRightChannelSynced = leftKnob.isBeatSync();
				} else {
					// y = right delay time when not linked
					yParamIndex = 1;
					newRightChannelSynced = ((Delay) effect).rightChannelBeatSyncMemory;
					if (((Delay) effect).rightChannelLevelMemory > 0)
						newRightChannelLevel = ((Delay) effect).rightChannelLevelMemory;
				}
				effect.getParam(1).beatSync = newRightChannelSynced;
				rightKnob.setBeatSync(newRightChannelSynced);
				rightKnob.setLevel(newRightChannelLevel);
			}
		});
	}
	
	@Override
	protected ParamData[] getParamsData() {
		return Delay.PARAMS_DATA;
	}
}
