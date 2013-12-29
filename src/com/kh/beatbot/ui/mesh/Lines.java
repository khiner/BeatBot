package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.MidiView;

public class Lines extends Shape {

	public Lines(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor);
	}

	@Override
	protected int getNumFillVertices() {
		return 0;
	}

	@Override
	protected int getNumStrokeVertices() {
		return 0;
	}

	@Override
	protected void updateVertices() {
		float y = MidiView.Y_OFFSET;
		for (int i = 1; i < TrackManager.getNumTracks(); i++) {
			y += MidiView.trackHeight;
			strokeVertex(0, y);
			strokeVertex(width, y);
		}
	}
}
