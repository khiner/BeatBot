package com.kh.beatbot.layout.page;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars.LevelType;
import com.kh.beatbot.view.LevelsView;
import com.kh.beatbot.view.MidiView;

public class NoteLevelsPage extends Page {
	private LevelsView levelsView;
	private ToggleButton volumeToggle, panToggle, pitchToggle;
	
	public NoteLevelsPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void init() {
		levelsView = (LevelsView)findViewById(R.id.levelsView);
		levelsView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		volumeToggle = (ToggleButton) findViewById(R.id.trackVolumeToggle);
		panToggle = (ToggleButton) findViewById(R.id.trackPanToggle);
		pitchToggle = (ToggleButton) findViewById(R.id.trackPitchToggle);
		volumeToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				levelsView.setLevelType(LevelType.VOLUME);
				update();
			}
		});
		panToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				levelsView.setLevelType(LevelType.PAN);
				update();
			}
		});
		pitchToggle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				levelsView.setLevelType(LevelType.PITCH);
				update();
			}
		});
	}

	@Override
	public void update() {
		deselectAll();
		selectActiveLevel();
	}

	private void deselectAll() {
		volumeToggle.setChecked(false);
		panToggle.setChecked(false);
		pitchToggle.setChecked(false);
	}

	private void selectActiveLevel() {
		switch (levelsView.getLevelType()) {
		case VOLUME:
			volumeToggle.setChecked(true);
			return;
		case PAN:
			panToggle.setChecked(true);
			return;
		case PITCH:
			pitchToggle.setChecked(true);
			return;
		}
	}
	
	@Override
	public void setVisibilityCode(int code) {
		levelsView.setVisibility(code);
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		int w = r - l;
		int h = b - t;
		levelsView.layout((int)MidiView.X_OFFSET, 0, w, h);
	}
	
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		int width = MeasureSpec.makeMeasureSpec(w - (int)MidiView.X_OFFSET, MeasureSpec.EXACTLY);
		int height = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
		levelsView.measure(width, height);
	}
}
