package com.kh.beatbot.ui.view;

import com.kh.beatbot.event.LoopWindowSetEvent;
import com.kh.beatbot.listener.LoopWindowListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.util.GeneralUtils;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.group.MidiViewGroup;

public class MidiLoopBarView extends TouchableView implements LoopWindowListener {
	private LoopWindowSetEvent currLoopWindowEvent;

	private Button loopBarButton, loopBeginButton, loopEndButton;

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
		loopBeginButton = new Button(this, MidiViewGroup.scaleGroup);
		loopEndButton = new Button(this, MidiViewGroup.scaleGroup);

		loopBeginButton.hide();
		loopEndButton.hide();
		loopBarButton.deselectOnPointerExit = false;
		loopBeginButton.deselectOnPointerExit = false;
		loopEndButton.deselectOnPointerExit = false;
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
		float tick = xToTick(pos.x);
		if (loopBeginButton.isPressed() && pos.equals(loopBeginButton.getPointer())) {
			MidiManager.setLoopBeginTick((long) MidiManager.getMajorTickNearestTo(tick));
		}
		if (loopEndButton.isPressed() && pos.equals(loopEndButton.getPointer())) {
			MidiManager.setLoopEndTick((long) MidiManager.getMajorTickNearestTo(tick));
		}
		if (loopBarButton.isPressed() && pos.equals(loopBarButton.getPointer())) {
			// middle selected. move begin and end preserve current loop length
			float loopLength = MidiManager.getLoopEndTick() - MidiManager.getLoopBeginTick();
			float newBeginTick = MidiManager.getMajorTickToLeftOf(tick - selectionOffsetTick);
			newBeginTick = GeneralUtils.clipTo(newBeginTick, 0, MidiManager.MAX_TICKS - loopLength);
			MidiManager.setLoopTicks((long) newBeginTick, (long) (newBeginTick + loopLength));
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
		MidiView midiView = mainPage.midiViewGroup.midiView;
		float tick = xToTick(x);
		float tickWidth = midiView.unscaledXToTick(height / 2);
		if (tick >= MidiManager.getLoopBeginTick() - tickWidth
				&& tick <= MidiManager.getLoopBeginTick() + tickWidth) {
			return loopBeginButton;
		} else if (tick >= MidiManager.getLoopEndTick() - tickWidth
				&& tick <= MidiManager.getLoopEndTick() + tickWidth) {
			return loopEndButton;
		} else if (tick >= MidiManager.getLoopBeginTick() && tick <= MidiManager.getLoopEndTick()) {
			selectionOffsetTick = tick - MidiManager.getLoopBeginTick();
			return loopBarButton;
		} else {
			return null;
		}
	}

	@Override
	public void onLoopWindowChange(long loopBeginTick, long loopEndTick) {
		MidiView midiView = mainPage.midiViewGroup.midiView;
		float beginX = midiView.tickToUnscaledX(loopBeginTick);
		float endX = midiView.tickToUnscaledX(loopEndTick);
		loopBarButton.layout(this, beginX - absoluteX, 0, endX - beginX, height);
		// loopBeginButton.layout(this, beginX - absoluteX, 0, height, height);
		// loopEndButton.layout(this, endX - absoluteX, 0, height, height);
	}

	// need to account for midiTrackView width offset (hack to group this draw into the parent)
	private float xToTick(float x) {
		return mainPage.midiViewGroup.midiView.xToTick(x
				- mainPage.midiViewGroup.midiTrackView.width);
	}
}
