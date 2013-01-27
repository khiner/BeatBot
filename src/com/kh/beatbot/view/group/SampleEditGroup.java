package com.kh.beatbot.view.group;

import android.content.Context;
import android.util.AttributeSet;

import com.kh.beatbot.view.window.PreviewButton;
import com.kh.beatbot.view.window.SampleEditViewWindow;

public class SampleEditGroup extends GLSurfaceViewGroup {
	
	private SampleEditViewWindow sampleEdit;
	private PreviewButton previewButton;
	
	public SampleEditGroup(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@Override
	protected void createChildren() {
		sampleEdit = new SampleEditViewWindow(this);
		previewButton = new PreviewButton(this);
		addChild(sampleEdit);
		addChild(previewButton);
	}
	
	@Override
	protected void layoutChildren() {
		previewButton.layout(gl, 0, 0, height, height);
		sampleEdit.layout(gl, height, 0, width - height, height);
	}
	
	@Override
	public void update() {
		if (sampleEdit != null)
			sampleEdit.update();
	}
}