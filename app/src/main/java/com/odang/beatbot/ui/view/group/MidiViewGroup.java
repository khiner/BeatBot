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
        final float translateY = -midiView.getYOffset();
        translateYGroup.translateY(translateY);
        translateScaleGroup.translateY(translateY);
    }

    @Override
    public void draw() {
        final float translateX = midiView.absoluteX - midiView.width * midiView.getXOffset()
                / midiView.getNumTicks();
        final float scale = MidiManager.MAX_TICKS / midiView.getNumTicks();

        midiView.startClip(false, true);
        translateYGroup.draw();
        midiView.endClip();

        midiView.startClip(true, false);
        scaleGroup.scaleX(scale);
        scaleGroup.translateX(translateX);
        scaleGroup.draw();
        scaleGroup.translateX(-translateX);
        scaleGroup.scaleX(1f / scale);

        midiView.startClip(true, true);
        translateScaleGroup.scaleX(scale);
        translateScaleGroup.translateX(translateX);
        translateScaleGroup.draw();
        translateScaleGroup.translateX(-translateX);
        translateScaleGroup.scaleX(1f / scale);
        midiView.endClip();
    }

    @Override
    public void handleActionDown(int id, Pointer pos) {
        super.handleActionDown(id, pos);
        midiView.scrollBarColorTrans.begin();
    }
}
