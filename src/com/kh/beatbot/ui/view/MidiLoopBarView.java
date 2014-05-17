package com.kh.beatbot.ui.view;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.listener.LoopChangeListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.group.MidiViewGroup;

public class MidiLoopBarView extends TouchableView implements LoopChangeListener {

	private Button loopBarButton;
	private Button loopBeginButton, loopEndButton;

	private float selectionOffsetTick = 0;

	public MidiLoopBarView(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
		MidiManager.addLoopChangeListener(this);
	}

	@Override
	protected synchronized void createChildren() {
		initRect();
		setIcon(IconResourceSets.MIDI_TICK_BAR);

		loopBarButton = new Button(this, MidiViewGroup.scaleGroup).withRect().withIcon(IconResourceSets.MIDI_LOOP_BAR);
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
		onLoopChange(0, 1);
	}

	@Override
	public void handleActionMove(int id, Pointer pos) {
		MidiView midiView = mainPage.midiViewGroup.midiView;
		if (loopBeginButton.isPressed() && pos.equals(loopBeginButton.getPointer())) {
			float leftTick = midiView.xToTick(pos.x);
			float leftMajorTick = MidiManager.getMajorTickNearestTo(leftTick);
			MidiManager.setLoopBeginTick((long) leftMajorTick);
			midiView.updateView(leftTick);
		}
		if (loopEndButton.isPressed() && pos.equals(loopEndButton.getPointer())) {
			float rightTick = midiView.xToTick(pos.x);
			float rightMajorTick = MidiManager.getMajorTickNearestTo(rightTick);
			MidiManager.setLoopEndTick((long) rightMajorTick);
			midiView.updateView(rightTick);
		}
		if (loopBarButton.isPressed() && pos.equals(loopBarButton.getPointer())) {
			// middle selected. move begin and end preserve current loop length
			float loopLength = MidiManager.getLoopEndTick() - MidiManager.getLoopBeginTick();
			float newBeginTick = MidiManager.getMajorTickToLeftOf(midiView.xToTick(pos.x)
					- selectionOffsetTick);
			newBeginTick = GeneralUtils.clipTo(newBeginTick, 0, MidiManager.MAX_TICKS - loopLength);
			MidiManager.setLoopTicks((long) newBeginTick, (long) (newBeginTick + loopLength));
			midiView.updateView(newBeginTick, newBeginTick + loopLength);
		}
	}

	@Override
	protected synchronized View findChildAt(float x, float y) {
		MidiView midiView = mainPage.midiViewGroup.midiView;
		float tick = midiView.xToTick(x);
		float tickWidth = midiView.unscaledXToTick(height / 2);
		if (tick >= MidiManager.getLoopBeginTick() - tickWidth
				&& tick <= MidiManager.getLoopBeginTick() + tickWidth) {
			return loopBeginButton;
		} else if (tick >= MidiManager.getLoopEndTick() - tickWidth
				&& tick <= MidiManager.getLoopEndTick() + tickWidth) {
			return loopEndButton;
		} else if (tick >= MidiManager.getLoopBeginTick() && tick <= MidiManager.getLoopEndTick()) {
			selectionOffsetTick = midiView.xToTick(x) - MidiManager.getLoopBeginTick();
			return loopBarButton;
		} else {
			return null;
		}
	}

	@Override
	public void onLoopChange(long loopBeginTick, long loopEndTick) {
		MidiView midiView = mainPage.midiViewGroup.midiView;
		float beginX = midiView.tickToUnscaledX(loopBeginTick);
		float endX = midiView.tickToUnscaledX(loopEndTick);
		loopBarButton.layout(this, beginX - absoluteX, 0, endX - beginX, height);
		// loopBeginButton.layout(this, beginX - absoluteX, 0, height, height);
		// loopEndButton.layout(this, endX - absoluteX, 0, height, height);
	}
}
