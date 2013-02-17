package com.kh.beatbot.layout.page;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BBButton;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.global.BBToggleButton;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.window.SampleEditViewWindow;

public class SampleEditPage extends Page {

	private SampleEditViewWindow sampleEdit;
	private BBButton previewButton;
	private BBToggleButton loopButton, reverseButton;
	
	public SampleEditPage(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public void init() {
	}
	
	@Override
	public void update() {
		if (sampleEdit != null)
			sampleEdit.update();
		loopButton.setOn(TrackManager.currTrack.isLooping());
		reverseButton.setOn(TrackManager.currTrack.isReverse());
	}

	@Override
	protected void loadIcons() {
		previewButton.setIconSource(new BBIconSource(-1 , R.drawable.preview_icon, R.drawable.preview_icon_selected));
		loopButton.setIconSource(new BBIconSource(-1, R.drawable.loop_icon, R.drawable.loop_selected_icon));
		reverseButton.setIconSource(new BBIconSource(-1, R.drawable.reverse_icon, R.drawable.reverse_selected_icon));
	}

	@Override
	public void draw() {
		// parent view - no drawing to do
	}

	@Override
	protected void createChildren() {
		sampleEdit = new SampleEditViewWindow((TouchableSurfaceView)parent);
		previewButton = new BBButton((TouchableSurfaceView)parent);
		previewButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(BBButton button) {
				if (button.isTouched()) {
					TrackManager.currTrack.preview();
				} else {
					TrackManager.currTrack.stopPreviewing();
				}
			}
		});
		loopButton = new BBToggleButton((TouchableSurfaceView)parent);
		reverseButton = new BBToggleButton((TouchableSurfaceView)parent);
		loopButton.setOnClickListener(new BBOnClickListener() {
			public void onClick(BBButton arg0) {
				TrackManager.currTrack.toggleLooping();
			}
		});
		reverseButton.setOnClickListener(new BBOnClickListener() {
			public void onClick(BBButton arg0) {
				TrackManager.currTrack.setReverse(reverseButton.isOn());
			}
		});
		addChild(sampleEdit);
		addChild(previewButton);
		addChild(loopButton);
		addChild(reverseButton);
	}

	@Override
	protected void layoutChildren() {
		float halfHeight = height / 2;
		previewButton.layout(this, 0, 0, height, height);
		sampleEdit.layout(this, height, 0, width - height - halfHeight, height);
		loopButton.layout(this, width - halfHeight, 0, halfHeight, halfHeight);
		reverseButton.layout(this, width - halfHeight, halfHeight, halfHeight, halfHeight);
	}
}
