package com.kh.beatbot.ui.view;

import com.kh.beatbot.event.LoopWindowSetEvent;
import com.kh.beatbot.listener.LoopWindowListener;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.control.Button;

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
	public synchronized void layoutChildren() {
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
		if (context.getMainPage().getMidiView().isPinchingLoopWindow()) {
			selectionOffsetTick = tick - context.getMidiManager().getLoopBeginTick(); // prevent
																						// loop-snapping
																						// to
																						// current x
																						// position
																						// when
																						// MidiView
																						// pointer
																						// is
																						// released
			if (pos.x < context.getMainPage().getMidiView().getPointer().x)
				context.getMidiManager().pinchLoopWindow(
						tick - (long) context.getMainPage().getMidiView().getPinchLeftOffset()
								- context.getMidiManager().getLoopBeginTick(), 0);
			else
				context.getMidiManager().pinchLoopWindow(
						0,
						tick + (long) context.getMainPage().getMidiView().getPinchRightOffset()
								- context.getMidiManager().getLoopEndTick());
		} else if (loopBarButton.isPressed() && pos.equals(loopBarButton.getPointer())) {
			// middle selected. translate loop window (preserve loop length)
			context.getMidiManager().translateLoopWindowTo(tick - (long) selectionOffsetTick);
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
	protected synchronized View findChildAt(float x, float y) {
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
		if (midiView.isPinchingLoopWindow()) // prevent loop-snapping to current x position when
												// MidiView pointer is released
			selectionOffsetTick = xToTick(getPointer().x)
					- context.getMidiManager().getLoopBeginTick();
	}

	private float xToTick(float x) {
		return context.getMainPage().getMidiView().xToTick(x);
	}
}
