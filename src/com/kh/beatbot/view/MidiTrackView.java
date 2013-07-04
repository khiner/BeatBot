package com.kh.beatbot.view;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.global.Track;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.view.helper.TickWindowHelper;

public class MidiTrackView extends TouchableBBView {

	private float lastY = 0;

	public void notifyTrackCreated(Track track) {
		addChild(track.getButtonRow());
		if (initialized) {
			layoutChildren();
			track.getButtonRow().loadAllIcons();
		}
	}
	
	public void notifyTrackDeleted(Track track) {
		track.getButtonRow().destroy();
		removeChild(track.getButtonRow());
		layoutChildren();
	}

	public void draw() {
		float newY = -absoluteY - TickWindowHelper.getYOffset();
		if (newY != lastY) {
			layoutChildren();
		}
		push();
		translate(-absoluteX, -absoluteY);
		TrackButtonRow.roundedRectGroup.draw((GL11) BBView.gl, 1);
		pop();
		lastY = newY;
	}

	@Override
	public void init() {
		// nothing
	}

	protected void loadIcons() {
		Managers.directoryManager.loadIcons();
	}
	
	@Override
	protected void createChildren() {
		// all button rows are added dynamically as tracks are added
	}

	@Override
	public void layoutChildren() {
		float yPos = MidiView.Y_OFFSET - TickWindowHelper.getYOffset();
		for (BBView child : children) {
			child.layout(this, 0, yPos, width, MidiView.trackHeight);
			yPos += MidiView.trackHeight;
		}
	}
}
