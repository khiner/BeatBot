package com.kh.beatbot.view.window;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BBButton;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.TouchableSurfaceView;

public class PreviewButton extends BBButton {

	public PreviewButton(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public void loadIcons() {
		setIconSource(new BBIconSource(-1 , R.drawable.preview_icon, R.drawable.preview_icon_selected));
	}
	
	public void touch() {
		super.touch();
		TrackManager.currTrack.preview();
	}
	
	public void release() {
		super.release();
		TrackManager.currTrack.stopPreviewing();
	}
}
