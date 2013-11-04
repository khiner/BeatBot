package com.kh.beatbot.ui.view;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.DirectoryManager;
import com.kh.beatbot.ui.view.helper.TickWindowHelper;

public class MidiTrackView extends TouchableView {

	private float lastY = 0;

	public void notifyTrackCreated(Track track) {
		addChild(track.getButtonRow());
		if (initialized) {
			track.getButtonRow().loadAllIcons();
			layoutChildren();
		}
		track.select();
	}
	
	public void notifyTrackDeleted(Track track) {
		track.getButtonRow().destroy();
		removeChild(track.getButtonRow());
		layoutChildren();
	}

	public void notifyTrackUpdated(Track track) {
		track.updateIcon();
	}
	
	public void draw() {
		float newY = -absoluteY - TickWindowHelper.getYOffset();
		if (newY != lastY) {
			layoutChildren();
		}
		TrackButtonRow.roundedRectGroup.draw(this, 1);
		lastY = newY;
	}

	protected synchronized void loadIcons() {
		DirectoryManager.loadIcons();
	}

	@Override
	public synchronized void layoutChildren() {
		float yPos = MidiView.Y_OFFSET - TickWindowHelper.getYOffset();
		for (View child : children) {
			child.layout(this, 0, yPos, width, MidiView.trackHeight);
			yPos += MidiView.trackHeight;
		}
	}
}
