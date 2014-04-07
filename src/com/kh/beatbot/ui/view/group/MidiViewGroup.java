package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.view.MidiLoopBarView;
import com.kh.beatbot.ui.view.MidiTrackView;
import com.kh.beatbot.ui.view.MidiView;
import com.kh.beatbot.ui.view.TouchableView;

public class MidiViewGroup extends TouchableView {
	public MidiTrackView midiTrackView;
	public MidiView midiView;
	public MidiLoopBarView midiLoopBarView;

	public static ShapeGroup scaleGroup, translateYGroup, translateScaleGroup;

	public MidiViewGroup(ShapeGroup shapeGroup) {
		super(shapeGroup);
		setClip(true);
	}

	public float getTrackControlWidth() {
		return midiTrackView.width;
	}

	@Override
	protected synchronized void createChildren() {
		scaleGroup = new ShapeGroup();
		translateYGroup = new ShapeGroup();
		translateScaleGroup = new ShapeGroup();

		midiTrackView = new MidiTrackView(translateYGroup);
		midiView = new MidiView(shapeGroup);
		midiLoopBarView = new MidiLoopBarView(shapeGroup);

		TrackManager.addTrackListener(midiView);
		TrackManager.addTrackListener(midiTrackView);

		addChildren(midiView, midiLoopBarView, midiTrackView);
	}

	@Override
	protected synchronized void layoutChildren() {
		float loopBarHeight = 21;
		MidiView.trackHeight = (height - loopBarHeight) / 5f;
		float trackControlWidth = MidiView.trackHeight * 2.5f;

		midiTrackView.layout(this, 0, loopBarHeight, trackControlWidth, height - loopBarHeight);
		midiView.layout(this, trackControlWidth, loopBarHeight, width - trackControlWidth, height
				- loopBarHeight);
		midiLoopBarView
				.layout(this, trackControlWidth, 0, width - trackControlWidth, loopBarHeight);
	}

	@Override
	public void draw() {
		push();
		translate(0, -midiView.getYOffset());
		translateYGroup.draw();
		pop();

		push();
		translate(
				midiView.absoluteX - midiView.width * midiView.getXOffset()
						/ midiView.getNumTicks(), 0);
		scale(MidiManager.MAX_TICKS / midiView.getNumTicks(), 1);
		scaleGroup.draw();
		translate(0, -midiView.getYOffset());
		translateScaleGroup.draw();
		pop();
	}
}
