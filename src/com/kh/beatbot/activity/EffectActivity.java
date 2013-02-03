package com.kh.beatbot.activity;

import android.app.Activity;
import android.os.Bundle;

import com.kh.beatbot.R;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.global.GeneralUtils;
import com.kh.beatbot.layout.EffectLayout;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.view.group.GLSurfaceViewGroup;

public class EffectActivity extends Activity {
	protected Effect effect;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GeneralUtils.initAndroidSettings(this);
		int effectPosition = getIntent().getExtras().getInt("effectPosition");
		int trackId = getIntent().getExtras().getInt("trackId");
		boolean setOn = getIntent().getExtras().getBoolean("setOn");
		this.setContentView(R.layout.empty_layout);
		// track could be master, so we need to be general and use BaseTrack
		effect = Managers.trackManager.getBaseTrack(trackId)
				.findEffectByPosition(effectPosition);
		GLSurfaceViewGroup group = (GLSurfaceViewGroup)findViewById(R.id.empty_group);
		EffectLayout renderer = new EffectLayout(group, effect);
		group.setBBRenderer(renderer);
		//if (setOn)
//			effect.setOn(true);
	}
}
