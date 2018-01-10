package com.odang.beatbot.ui.view.group;

import com.odang.beatbot.manager.MidiManager;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.view.MidiLoopBarView;
import com.odang.beatbot.ui.view.MidiTrackView;
import com.odang.beatbot.ui.view.MidiView;
import com.odang.beatbot.ui.view.TouchableView;
import com.odang.beatbot.ui.view.View;

public class MidiViewGroup extends TouchableView {
	public MidiTrackView midiTrackView;
	public MidiView midiView;
	public MidiLoopBarView midiLoopBarView;

	private RenderGroup scaleGroup, translateYGroup, translateScaleGroup;

	public MidiViewGroup(View view) {
		super(view);
	}

	public float getTrackControlWidth() {
		return midiTrackView.width;
	}

	@Override
	protected void createChildren() {
		scaleGroup = new RenderGroup();
		translateYGroup = new RenderGroup();
		translateScaleGroup = new RenderGroup();

		midiTrackView = new MidiTrackView(this, translateYGroup);
		midiView = new MidiView(this, scaleGroup, translateYGroup, translateScaleGroup);
		midiLoopBarView = new MidiLoopBarView(this, scaleGroup);

		context.getTrackManager().addTrackListener(midiView);
		context.getTrackManager().addTrackListener(midiTrackView);
	}

	@Override
	public void layoutChildren() {
		float loopBarHeight = height / 12f;
		MidiView.trackHeight = (height - loopBarHeight) / 5f;
		float trackControlWidth = MidiView.trackHeight * 2.5f;

		midiTrackView.layout(this, 0, loopBarHeight, trackControlWidth, height - loopBarHeight);
		midiView.layout(this, trackControlWidth, loopBarHeight, width - trackControlWidth, height
				- loopBarHeight);
		midiLoopBarView.layout(this, 0, 0, width - trackControlWidth, loopBarHeight);
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
		midiView.getRenderGroup().draw();
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		super.handleActionDown(id, pos);
		midiView.scrollBarColorTrans.begin();
	}
}
