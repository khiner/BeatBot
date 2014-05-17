package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.MidiLoopBarView;
import com.kh.beatbot.ui.view.MidiTrackView;
import com.kh.beatbot.ui.view.MidiView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;

public class MidiViewGroup extends TouchableView {
	public MidiTrackView midiTrackView;
	public MidiView midiView;
	public MidiLoopBarView midiLoopBarView;

	public static RenderGroup scaleGroup, translateYGroup, translateScaleGroup;

	public MidiViewGroup(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
		setClip(true);
	}

	public float getTrackControlWidth() {
		return midiTrackView.width;
	}

	@Override
	protected synchronized void createChildren() {
		scaleGroup = new RenderGroup();
		translateYGroup = new RenderGroup();
		translateScaleGroup = new RenderGroup();

		midiTrackView = new MidiTrackView(this, translateYGroup);
		midiView = new MidiView(this, renderGroup);
		midiLoopBarView = new MidiLoopBarView(this, renderGroup);

		TrackManager.addTrackListener(midiView);
		TrackManager.addTrackListener(midiTrackView);
	}

	@Override
	public synchronized void layoutChildren() {
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
		midiView.startClip(false, true);
		push();
		translate(0, -midiView.getYOffset());
		translateYGroup.draw();
		pop();
		midiView.endClip();

		push();
		translate(
				midiView.absoluteX - midiView.width * midiView.getXOffset()
						/ midiView.getNumTicks(), 0);
		scale(MidiManager.MAX_TICKS / midiView.getNumTicks(), 1);
		midiView.startClip(true, false);
		scaleGroup.draw();
		translate(0, -midiView.getYOffset());
		midiView.startClip(true, true);
		translateScaleGroup.draw();
		midiView.endClip();
		pop();
	}
}
