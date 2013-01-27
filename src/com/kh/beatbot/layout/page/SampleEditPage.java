package com.kh.beatbot.layout.page;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ToggleButton;

import com.kh.beatbot.R;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.group.GLSurfaceViewGroup;
import com.kh.beatbot.view.group.SampleEditGroup;

public class SampleEditPage extends Page {
	
	private GLSurfaceViewGroup sampleEditView;
	private SampleEditGroup sampleEditGroup;
	private ToggleButton loopButton;
	private ToggleButton reverseButton;
	
	public SampleEditPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init() {
		sampleEditView = (GLSurfaceViewGroup) findViewById(R.id.sample_waveform_view);
		sampleEditGroup = new SampleEditGroup(sampleEditView);
		sampleEditView.setBBRenderer(sampleEditGroup);
		loopButton = (ToggleButton) findViewById(R.id.loop);
		reverseButton = (ToggleButton) findViewById(R.id.reverse);
		loopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				TrackManager.currTrack.toggleLooping();
			}
		});
		reverseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				TrackManager.currTrack.setReverse(reverseButton.isChecked());
			}
		});
	}
	
	@Override
	public void update() {
		sampleEditGroup.update();
		loopButton.setChecked(TrackManager.currTrack.isLooping());
		reverseButton.setChecked(TrackManager.currTrack.isReverse());
	}

	@Override
	public void setVisibilityCode(int code) {
		//sampleWaveformView.setVisibility(code);
	}
}
