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
    private float selectionOffsetTick = 0;

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
        long tick = (long) xToTick(pos.x);
        final MidiManager midiManager = context.getMidiManager();
        final MidiView midiView = context.getMainPage().getMidiView();
        if (midiView.isPinchingLoopWindow()) {
            selectionOffsetTick = tick - midiManager.getLoopBeginTick();
            final long beginTickDiff;
            final long endTickDiff;
            if (id == midiView.getPinchLeftPointerId()) {
                beginTickDiff = tick - (long) midiView.getPinchLeftOffset() - midiManager.getLoopBeginTick();
                endTickDiff = 0;
            } else {
                beginTickDiff = 0;
                endTickDiff = tick + (long) midiView.getPinchRightOffset() - midiManager.getLoopEndTick();
            }
            midiManager.pinchLoopWindow(beginTickDiff, endTickDiff);
        } else if (loopBarButton.isPressed() && pos.equals(loopBarButton.getPointer())) {
            // middle selected. translate loop window (preserve loop length)
            midiManager.translateLoopWindowTo(tick - (long) selectionOffsetTick);
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
        float tick = xToTick(x - context.getMainPage().getMidiTrackView().width);
        if (tick >= context.getMidiManager().getLoopBeginTick()
                && tick <= context.getMidiManager().getLoopEndTick()) {
            selectionOffsetTick = xToTick(x) - context.getMidiManager().getLoopBeginTick();
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

    private float xToTick(float x) {
        return context.getMainPage().getMidiView().xToTick(x);
    }
}
