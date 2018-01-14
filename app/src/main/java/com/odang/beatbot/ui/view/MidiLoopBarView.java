package com.odang.beatbot.ui.view;

import com.odang.beatbot.event.LoopWindowSetEvent;
import com.odang.beatbot.listener.LoopWindowListener;
import com.odang.beatbot.manager.MidiManager;
import com.odang.beatbot.ui.color.Color;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.shape.Rectangle;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.view.control.Button;

public class MidiLoopBarView extends TouchableView implements LoopWindowListener {
    private LoopWindowSetEvent currLoopWindowEvent;
    private Button loopBarButton;
    private long translateTickAnchor = 0;
    private float translateXAnchor = 0;

    public MidiLoopBarView(View view, RenderGroup scaleGroup) {
        super(view);

        bgShape = new Rectangle(scaleGroup, Color.TRANSPARENT, Color.TRANSPARENT);
        setIcon(IconResourceSets.MIDI_TICK_BAR);

        loopBarButton = new Button(this, scaleGroup).withRect().withIcon(
                IconResourceSets.MIDI_LOOP_BAR);
        loopBarButton.deselectOnPointerExit = false;
        loopBarButton.setShrinkable(false);

        context.getMidiManager().addLoopChangeListener(this);
    }

    @Override
    public void layoutChildren() {
        onLoopWindowChange(context.getMidiManager().getLoopBeginTick(), context.getMidiManager()
                .getLoopEndTick());
    }

    @Override
    public void handleActionDown(int id, Pointer pos) {
        super.handleActionDown(id, pos);
        currLoopWindowEvent = new LoopWindowSetEvent();
        currLoopWindowEvent.begin();
    }

    @Override
    public void handleActionMove(int id, Pointer pos) {
        final MidiManager midiManager = context.getMidiManager();
        final MidiView midiView = context.getMainPage().getMidiView();
        synchronized (context.getMainPage().getMidiViewGroup()) {
            if (midiView.isPinchingLoopWindow()) {
                translateTickAnchor = midiManager.getLoopBeginTick();
                translateXAnchor = pos.x;
                midiView.pinchLoopWindow(id, pos);
            } else if (loopBarButton.isPressed() && pos.equals(loopBarButton.getPointer())) {
                // middle selected. translate loop window (preserve loop length)
                midiManager.translateLoopWindowTo(translateTickAnchor + (long) midiView.xToNumTicks(pos.x - translateXAnchor));
            }
        }
    }

    @Override
    public void handleActionUp(int id, Pointer pos) {
        if (null != currLoopWindowEvent) {
            currLoopWindowEvent.end();
            currLoopWindowEvent = null;
        }
        super.handleActionUp(id, pos);
    }

    @Override
    protected View findChildAt(float x, float y) {
        float tick = context.getMainPage().getMidiView().xToTick(x - context.getMainPage().getMidiTrackView().width);
        final MidiManager midiManager = context.getMidiManager();
        if (tick >= midiManager.getLoopBeginTick() && tick <= midiManager.getLoopEndTick()) {
            translateTickAnchor = midiManager.getLoopBeginTick();
            translateXAnchor = x;
            return loopBarButton;
        } else {
            return null;
        }
    }

    @Override
    public void onLoopWindowChange(long loopBeginTick, long loopEndTick) {
        MidiView midiView = context.getMainPage().getMidiView();
        float beginX = midiView.tickToUnscaledX(loopBeginTick);
        float endX = midiView.tickToUnscaledX(loopEndTick);
        loopBarButton.layout(this, beginX - absoluteX, 0, endX - beginX, height);
    }
}
