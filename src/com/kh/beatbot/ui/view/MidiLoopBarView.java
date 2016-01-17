package com.kh.beatbot.ui.view;

import com.kh.beatbot.event.LoopWindowSetEvent;
import com.kh.beatbot.listener.LoopWindowListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.group.MidiViewGroup;

public class MidiLoopBarView extends TouchableView implements LoopWindowListener {
	private LoopWindowSetEvent currLoopWindowEvent;
	private Button loopBarButton;
	private float selectionOffsetTick = 0;

	public MidiLoopBarView(View view) {
		super(view);
		MidiManager.addLoopChangeListener(this);
	}

	@Override
	protected synchronized void createChildren() {
		bgShape = new Rectangle(MidiViewGroup.scaleGroup, Color.TRANSPARENT, Color.TRANSPARENT);
		setIcon(IconResourceSets.MIDI_TICK_BAR);

		loopBarButton = new Button(this, MidiViewGroup.scaleGroup).withRect().withIcon(
				IconResourceSets.MIDI_LOOP_BAR);
		loopBarButton.deselectOnPointerExit = false;
		loopBarButton.setShrinkable(false);
	}

	@Override
	public synchronized void layoutChildren() {
		onLoopWindowChange(MidiManager.getLoopBeginTick(), MidiManager.getLoopEndTick());
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
		if (mainPage.getMidiView().isPinchingLoopWindow()) {
			selectionOffsetTick = tick - MidiManager.getLoopBeginTick(); // prevent loop-snapping to current x position when MidiView pointer is released
			if (pos.x < mainPage.getMidiView().getPointer().x)
				MidiManager.pinchLoopWindow(tick - (long) mainPage.getMidiView().getPinchLeftOffset() - MidiManager.getLoopBeginTick(), 0);
			else
				MidiManager.pinchLoopWindow(0, tick + (long) mainPage.getMidiView().getPinchRightOffset() - MidiManager.getLoopEndTick());
		} else if (loopBarButton.isPressed() && pos.equals(loopBarButton.getPointer())) {
			// middle selected. translate loop window (preserve loop length)
			MidiManager.translateLoopWindowTo(tick - (long) selectionOffsetTick);
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
		float tick = xToTick(x - mainPage.getMidiTrackView().width);
		if (tick >= MidiManager.getLoopBeginTick() && tick <= MidiManager.getLoopEndTick()) {
			selectionOffsetTick = xToTick(x) - MidiManager.getLoopBeginTick();
			return loopBarButton;
		} else {
			return null;
		}
	}

	@Override
	public void onLoopWindowChange(long loopBeginTick, long loopEndTick) {
		MidiView midiView = mainPage.getMidiView();
		float beginX = midiView.tickToUnscaledX(loopBeginTick);
		float endX = midiView.tickToUnscaledX(loopEndTick);
		loopBarButton.layout(this, beginX - absoluteX, 0, endX - beginX, height);
		if (midiView.isPinchingLoopWindow()) // prevent loop-snapping to current x position when MidiView pointer is released
			selectionOffsetTick = xToTick(getPointer().x) - MidiManager.getLoopBeginTick();
	}

	private float xToTick(float x) {
		return mainPage.getMidiView().xToTick(x);
	}
}
