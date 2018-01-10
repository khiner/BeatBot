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
        final float translateX = midiView.absoluteX - midiView.width * midiView.getXOffset()
                / midiView.getNumTicks();
        final float translateY = -midiView.getYOffset();
        final float scale = MidiManager.MAX_TICKS / midiView.getNumTicks();

        midiView.startClip(false, true);
        translateYGroup.translate(0, translateY);
        translateYGroup.draw();
        translateYGroup.translate(0, -translateY);
        midiView.endClip();

        midiView.startClip(true, false);
        scaleGroup.scale(scale, 1);
        scaleGroup.translate(translateX, 0);

        scaleGroup.draw();

        scaleGroup.translate(-translateX, 0);
        scaleGroup.scale(1f / scale, 1);
        midiView.startClip(true, true);
        translateScaleGroup.scale(scale, 1);
        translateScaleGroup.translate(translateX, translateY);
        translateScaleGroup.draw();
        translateScaleGroup.translate(-translateX, -translateY);
        translateScaleGroup.scale(1f / scale, 1);
        midiView.endClip();

        midiView.getRenderGroup().draw();
    }

    @Override
    public void handleActionDown(int id, Pointer pos) {
        super.handleActionDown(id, pos);
        midiView.scrollBarColorTrans.begin();
    }
}
