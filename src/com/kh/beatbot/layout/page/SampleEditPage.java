package com.kh.beatbot.layout.page;

import com.kh.beatbot.R;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.Button;
import com.kh.beatbot.view.ImageButton;
import com.kh.beatbot.view.SampleEditBBView;
import com.kh.beatbot.view.ToggleButton;

public class SampleEditPage extends Page {

	private SampleEditBBView sampleEdit;
	private ImageButton previewButton;
	private ToggleButton loopButton, reverseButton;

	public void init() {
	}
	
	@Override
	public void update() {
		if (sampleEdit != null)
			sampleEdit.update();
		loopButton.setChecked(TrackManager.currTrack.isLooping());
		reverseButton.setChecked(TrackManager.currTrack.isReverse());
	}

	@Override
	protected void loadIcons() {
		previewButton.setIconSource(new ImageIconSource(R.drawable.preview_icon, R.drawable.preview_icon_selected));
		loopButton.setIconSource(new ImageIconSource(R.drawable.loop_icon, R.drawable.loop_selected_icon));
		reverseButton.setIconSource(new ImageIconSource(R.drawable.reverse_icon, R.drawable.reverse_selected_icon));
	}

	@Override
	public void draw() {
		// parent view - no drawing to do
	}

	@Override
	protected void createChildren() {
		sampleEdit = new SampleEditBBView();
		previewButton = new ImageButton();
		previewButton.setOnClickListener(new BBOnClickListener() {
			@Override
			public void onClick(Button button) {
				if (button.isPressed()) {
					TrackManager.currTrack.preview();
				} else {
					TrackManager.currTrack.stopPreviewing();
				}
			}
		});
		loopButton = new ToggleButton();
		reverseButton = new ToggleButton();
		loopButton.setOnClickListener(new BBOnClickListener() {
			public void onClick(Button arg0) {
				TrackManager.currTrack.toggleLooping();
			}
		});
		reverseButton.setOnClickListener(new BBOnClickListener() {
			public void onClick(Button arg0) {
				TrackManager.currTrack.setReverse(reverseButton.isChecked());
			}
		});
		addChild(sampleEdit);
		addChild(previewButton);
		addChild(loopButton);
		addChild(reverseButton);
	}

	@Override
	public void layoutChildren() {
		float halfHeight = height / 2;
		previewButton.layout(this, 0, 0, height, height);
		sampleEdit.layout(this, height, 0, width - height - halfHeight, height);
		loopButton.layout(this, width - halfHeight, 0, halfHeight, halfHeight);
		reverseButton.layout(this, width - halfHeight, halfHeight, halfHeight, halfHeight);
	}
}
