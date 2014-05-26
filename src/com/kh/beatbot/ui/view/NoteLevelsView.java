package com.kh.beatbot.ui.view;

import java.util.HashMap;
import java.util.Map;

import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.Circle;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.shape.RenderGroup;

public class NoteLevelsView extends TouchableView {

	private static class DragLine {
		private static float m = 0, b = 0, leftTick = 0, rightTick = Float.MAX_VALUE,
				leftLevel = 0, rightLevel = 0;

		public static float getLevel(float tick) {
			if (tick <= leftTick)
				return leftLevel;
			if (tick >= rightTick)
				return rightLevel;

			return m * tick + b;
		}
	}

	// the size of the "dots" at the top of level display
	public static final int LEVEL_POINT_SIZE = 16;
	// the width of the lines for note levels
	public static final int LEVEL_LINE_WIDTH = 7;

	private static final int LEVEL_BAR_WIDTH = LEVEL_POINT_SIZE / 2;

	// map of pointerIds to the notes they are selecting
	private Map<Integer, MidiNote> touchedLevels = new HashMap<Integer, MidiNote>();

	// map Midi Note to the offset of their level relative to the touched level(s)
	private Map<MidiNote, Float> levelOffsets = new HashMap<MidiNote, Float>();

	private LevelType currLevelType = LevelType.VOLUME;

	private float selectRegionStartX = -1, selectRegionStartY = -1;

	private Rectangle selectRegionRect, levelBarRect;
	private Circle levelBarCircle, levelBarSelectCircle;

	private static RenderGroup levelBarGroup = new RenderGroup();

	public NoteLevelsView(View view) {
		super(view);
		setClip(true);
	}

	public LevelType getLevelType() {
		return currLevelType;
	}

	public void setLevelType(LevelType levelType) {
		currLevelType = levelType;
	}

	public void clearTouchedLevels() {
		touchedLevels.clear();
	}

	public MidiNote getTouchedLevel(int id) {
		return touchedLevels.get(id);
	}

	protected void drawLevel(MidiNote midiNote, float[] levelColor, float[] levelColorTrans) {
		float y = levelToY(midiNote.getLevel(currLevelType));
		levelBarRect.layout(-LEVEL_BAR_WIDTH / 2, y, LEVEL_BAR_WIDTH, height - y - 1);
		levelBarCircle.setPosition(0, y);
		levelBarSelectCircle.setPosition(0, y);
		levelBarRect.setFillColor(levelColor);
		levelBarCircle.setFillColor(levelColor);
		levelBarSelectCircle.setFillColor(levelColorTrans);
		push();
		translate(tickToX(midiNote.getOnTick()) + absoluteX, absoluteY);
		levelBarGroup.draw();
		pop();
	}

	private float[] calcLevelColor(boolean selected) {
		if (selected) {
			return Color.LEVEL_SELECTED;
		} else {
			switch (currLevelType) {
			case VOLUME:
				return Color.TRON_BLUE;
			case PAN:
				return Color.PAN;
			case PITCH:
				return Color.PITCH;
			default:
				return Color.LEVEL_SELECTED;
			}
		}
	}

	private float[] calcLevelColorTrans(boolean selected) {
		if (selected) {
			return Color.LEVEL_SELECTED_TRANS;
		} else {
			switch (currLevelType) {
			case VOLUME:
				return Color.TRON_BLUE_TRANS;
			case PAN:
				return Color.PAN_TRANS;
			case PITCH:
				return Color.PITCH_TRANS;
			default:
				return Color.LEVEL_SELECTED_TRANS;
			}
		}
	}

	private boolean selectLevel(int pointerId, Pointer pos) {
		for (MidiNote midiNote : TrackManager.currTrack.getMidiNotes()) {
			float velocityY = levelToY(midiNote.getLevel(currLevelType));
			if (Math.abs(tickToX(midiNote.getOnTick()) - pos.x) < 35
					&& Math.abs(velocityY - pos.y) < 35) {
				// If this is the only touched level, and it hasn't yet
				// been selected, make it the only selected level.
				// If we are multi-selecting, add it to the selected list
				if (!midiNote.isSelected()) {
					if (touchedLevels.isEmpty()) {
						TrackManager.deselectAllNotes();
					}
					midiNote.setSelected(true);
				}
				touchedLevels.put(pointerId, midiNote);
				updateLevelOffsets();
				return true;
			}
		}
		if (touchedLevels.isEmpty()) {
			TrackManager.deselectAllNotes();
		}
		return false;
	}

	public void selectRegion(float x, float y) {
		float leftX = Math.min(x, selectRegionStartX);
		float rightX = Math.max(x, selectRegionStartX);
		float topY = Math.max(BG_OFFSET, Math.min(y, selectRegionStartY));
		float bottomY = Math.max(y, selectRegionStartY);

		float leftTick = xToTick(leftX), rightTick = xToTick(rightX);

		for (MidiNote selectedNote : TrackManager.currTrack.getMidiNotes()) {
			float levelY = levelToY(selectedNote.getLevel(currLevelType));
			boolean selected = leftTick < selectedNote.getOnTick()
					&& rightTick > selectedNote.getOnTick() && topY < levelY && bottomY > levelY;
			selectedNote.setSelected(selected);
		}

		selectRegionRect
				.layout(absoluteX + leftX, absoluteY + topY, rightX - leftX, bottomY - topY);
		selectRegionRect.setFillColor(Color.TRON_BLUE_TRANS);
	}

	private void updateDragLine() {
		int touchedSize = touchedLevels.values().size();
		if (touchedSize == 1) {
			DragLine.m = 0;
			MidiNote touched = (MidiNote) touchedLevels.values().toArray()[0];
			DragLine.b = touched.getLevel(currLevelType);
			DragLine.leftTick = 0;
			DragLine.rightTick = Float.MAX_VALUE;
			DragLine.leftLevel = DragLine.rightLevel = touched.getLevel(currLevelType);
		} else if (touchedSize == 2) {
			MidiNote leftLevel = touchedLevels.get(0).getOnTick() < touchedLevels.get(1)
					.getOnTick() ? touchedLevels.get(0) : touchedLevels.get(1);
			MidiNote rightLevel = touchedLevels.get(0).getOnTick() < touchedLevels.get(1)
					.getOnTick() ? touchedLevels.get(1) : touchedLevels.get(0);
			DragLine.m = (rightLevel.getLevel(currLevelType) - leftLevel.getLevel(currLevelType))
					/ (rightLevel.getOnTick() - leftLevel.getOnTick());
			DragLine.b = (leftLevel.getLevel(currLevelType) - DragLine.m * leftLevel.getOnTick());
			DragLine.leftTick = leftLevel.getOnTick();
			DragLine.rightTick = rightLevel.getOnTick();
			DragLine.leftLevel = leftLevel.getLevel(currLevelType);
			DragLine.rightLevel = rightLevel.getLevel(currLevelType);
		}
	}

	private void updateLevelOffsets() {
		levelOffsets.clear();
		updateDragLine();
		for (MidiNote selectedNote : TrackManager.getSelectedNotes()) {
			levelOffsets.put(
					selectedNote,
					selectedNote.getLevel(currLevelType)
							- DragLine.getLevel(selectedNote.getOnTick()));
		}
	}

	private void setLevelsToDragLine() {
		for (MidiNote selectedNote : TrackManager.getSelectedNotes()) {
			if (levelOffsets.get(selectedNote) != null) {
				selectedNote.setLevel(currLevelType, DragLine.getLevel(selectedNote.getOnTick())
						+ levelOffsets.get(selectedNote));
			}
		}
	}

	private void startSelectRegion(Pointer pos) {
		selectRegionStartX = pos.x;
		selectRegionStartY = pos.y;
	}

	public void draw() {
		for (int i = 0; i < TrackManager.currTrack.getMidiNotes().size(); i++) {
			MidiNote midiNote = TrackManager.currTrack.getMidiNotes().get(i);
			drawLevel(midiNote, calcLevelColor(midiNote.isSelected()),
					calcLevelColorTrans(midiNote.isSelected()));
		}
	}

	private float levelToY(float level) {
		return height - level * (height - LEVEL_POINT_SIZE) - LEVEL_POINT_SIZE / 2;
	}

	/*
	 * map y value of level bar to a value in [0,1]
	 */
	private float yToLevel(float y) {
		return (height - y - LEVEL_POINT_SIZE / 2) / (height - LEVEL_POINT_SIZE);
	}

	private float tickToX(float tick) {
		return mainPage.midiViewGroup.midiView.tickToX(tick)
				+ mainPage.midiViewGroup.midiView.absoluteX - absoluteX;
	}

	private float xToTick(float x) {
		return mainPage.midiViewGroup.midiView.xToTick(x + absoluteX
				- mainPage.midiViewGroup.midiView.absoluteX);
	}

	@Override
	public void handleActionPointerUp(int id, Pointer pos) {
		touchedLevels.remove(id);
		updateLevelOffsets();
	}

	@Override
	public void handleActionMove(int id, Pointer pos) {
		if (!touchedLevels.isEmpty()) {
			MidiNote touched = touchedLevels.get(id);
			if (touched != null) {
				touched.setLevel(currLevelType, yToLevel(pos.y));
			}
			if (id == pointersById.size() - 1) {
				updateDragLine();
				setLevelsToDragLine();
			}
		} else if (id == 0) {
			selectRegion(pos.x, pos.y);
		}
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		super.handleActionDown(id, pos);
		MidiManager.beginMidiEvent(TrackManager.currTrack);
		if (!selectLevel(id, pos)) {
			startSelectRegion(pos);
		}
	}

	@Override
	public void handleActionPointerDown(int id, Pointer pos) {
		selectLevel(id, pos);
	}

	@Override
	public void handleActionUp(int id, Pointer pos) {
		super.handleActionUp(id, pos);
		clearTouchedLevels();
		selectRegionRect.setFillColor(Color.TRANSPARENT);
		MidiManager.endMidiEvent();
	}

	@Override
	protected synchronized void createChildren() {
		initRoundedRect();
		selectRegionRect = new Rectangle(renderGroup, Color.TRANSPARENT, null);
		levelBarRect = new Rectangle(levelBarGroup, Color.TRON_BLUE, null);
		levelBarCircle = new Circle(levelBarGroup, Color.TRON_BLUE, null);
		levelBarSelectCircle = new Circle(levelBarGroup, Color.TRON_BLUE, null);
		addShapes(selectRegionRect);
	}

	@Override
	public synchronized void layoutChildren() {
		levelBarRect.layout(-LEVEL_BAR_WIDTH / 2, 0, LEVEL_BAR_WIDTH, height);
		levelBarCircle.layout(0, 0, LEVEL_BAR_WIDTH, LEVEL_BAR_WIDTH);
		levelBarSelectCircle.layout(0, 0, LEVEL_BAR_WIDTH * 2, LEVEL_BAR_WIDTH * 2);
	}
}
