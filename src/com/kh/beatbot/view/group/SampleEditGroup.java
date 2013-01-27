package com.kh.beatbot.view.group;

import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.window.PreviewButton;
import com.kh.beatbot.view.window.SampleEditViewWindow;
import com.kh.beatbot.view.window.TouchableViewWindow;

public class SampleEditGroup extends TouchableViewWindow {
	
	public SampleEditGroup(TouchableSurfaceView parent) {
		super(parent);
	}

	private SampleEditViewWindow sampleEdit;
	private PreviewButton previewButton;

	@Override
	protected void createChildren() {
		sampleEdit = new SampleEditViewWindow((TouchableSurfaceView)parent);
		previewButton = new PreviewButton((TouchableSurfaceView)parent);
		addChild(sampleEdit);
		addChild(previewButton);
	}
	
	@Override
	protected void layoutChildren() {
		previewButton.layout(gl, 0, 0, height, height);
		sampleEdit.layout(gl, height, 0, width - height, height);
	}
	
	public void update() {
		if (sampleEdit != null)
			sampleEdit.update();
	}

	@Override
	protected void loadIcons() {
		// no icons
	}

	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public void draw() {
		// nothing to draw
	}
}