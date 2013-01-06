package com.kh.beatbot.view;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.GlobalVars.LevelType;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.midi.MidiNote;

public class LevelsView extends SurfaceViewBase {

	public LevelsView(Context c, AttributeSet as) {
		super(c, as);
	}

	private static class DragLine {
		private static float m = 0;
		private static float b = 0;
		private static float leftTick = 0;
		private static float rightTick = Float.MAX_VALUE;
		private static float leftLevel = 0;
		private static float rightLevel = 0;

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

	private FloatBuffer levelBarVb = null, selectRegionVb = null;

	// map of pointerIds to the notes they are selecting
	private Map<Integer, MidiNote> touchedLevels = new HashMap<Integer, MidiNote>();

	// map Midi Note to the offset of their level relative to the touched
	// level(s)
	private Map<MidiNote, Float> levelOffsets = new HashMap<MidiNote, Float>();

	private LevelType currLevelType = LevelType.VOLUME;

	private boolean selectRegion = false;
	private float selectRegionStartTick = -1, selectRegionStartY = -1;

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

	private void initLevelBarVb() {
		float[] vertices = new float[800];
		vertices[0] = -LEVEL_BAR_WIDTH / 2;
		vertices[1] = height;
		vertices[2] = LEVEL_BAR_WIDTH / 2;
		vertices[3] = height;
		for (int i = 1; i < vertices.length / 4; i++) {
			vertices[i * 4] = -LEVEL_BAR_WIDTH / 2;
			vertices[i * 4 + 1] = levelToY((float) i / (vertices.length / 4));
			vertices[i * 4 + 2] = LEVEL_BAR_WIDTH / 2;
			vertices[i * 4 + 3] = vertices[i * 4 + 1];
		}
		levelBarVb = SurfaceViewBase.makeFloatBuffer(vertices);
	}

	private void drawSelectRegion() {
		if (!selectRegion || selectRegionVb == null)
			return;
		drawTriangleStrip(selectRegionVb, Colors.SELECT_REGION_COLOR);
	}

	private void initSelectRegionVb(float leftTick, float rightTick,
			float topY, float bottomY) {
		selectRegionVb = makeRectFloatBuffer(tickToX(leftTick), topY,
				tickToX(rightTick), bottomY);
	}

	private int calcVertex(float level) {
		int vertex = (int) (level * (levelBarVb.capacity() / 2 - 16));
		vertex += 16;
		vertex += vertex % 2;
		vertex = vertex > 2 ? vertex : 2;
		return vertex;
	}

	protected void drawLevel(float x, float level, float[] levelColor) {
		int vertex = calcVertex(level);
		gl.glPushMatrix();
		SurfaceViewBase.translate(x, 0);
		SurfaceViewBase.drawTriangleStrip(levelBarVb, levelColor, vertex);

		SurfaceViewBase.translate(LEVEL_BAR_WIDTH / 2, 0);
		// draw level-colored circle at beginning and end of level
		SurfaceViewBase.drawPoint(LEVEL_BAR_WIDTH, levelColor, vertex - 2);

		drawLevelSelectionCircle(vertex - 2, levelColor);
		gl.glPopMatrix();
	}

	protected void drawLevelSelectionCircle(int vertex, float[] levelColor) {
		// draw bigger, translucent 'selection' circle at end of level
		levelColor[3] = .5f;
		SurfaceViewBase.drawPoint(LEVEL_BAR_WIDTH * 2.5f, levelColor, vertex);
		levelColor[3] = 1;
	}

	private float[] calcLevelColor(boolean selected) {
		if (selected) {
			return Colors.LEVEL_SELECTED_COLOR;
		} else {
			switch (currLevelType) {
			case VOLUME:
				return Colors.VOLUME_COLOR;
			case PAN:
				return Colors.PAN_COLOR;
			case PITCH:
				return Colors.PITCH_COLOR;
			default:
				return Colors.LEVEL_SELECTED_COLOR;
			}
		}
	}

	private void drawLevels() {
		for (MidiNote midiNote : Managers.midiManager.getMidiNotes()) {
			drawLevel(tickToX(midiNote.getOnTick()),
					midiNote.getLevel(currLevelType),
					calcLevelColor(midiNote.isSelected()));
		}
	}

	private boolean selectLevel(float x, float y, int pointerId) {
		for (MidiNote midiNote : Managers.midiManager.getMidiNotes()) {
			float velocityY = levelToY(midiNote.getLevel(currLevelType));
			if (Math.abs(tickToX(midiNote.getOnTick()) - x) < 35
					&& Math.abs(velocityY - y) < 35) {
				// If this is the only touched level, and it hasn't yet
				// been selected, make it the only selected level.
				// If we are multi-selecting, add it to the selected list
				if (!midiNote.isSelected()) {
					if (touchedLevels.isEmpty())
						Managers.midiManager.deselectAllNotes();
					midiNote.setSelected(true);
				}
				touchedLevels.put(pointerId, midiNote);
				updateLevelOffsets();
				return true;
			}
		}
		return false;
	}

	public void selectRegion(float x, float y) {
		float tick = xToTick(x);
		float leftTick = Math.min(tick, selectRegionStartTick);
		float rightTick = Math.max(tick, selectRegionStartTick);
		float topY = Math.min(y, selectRegionStartY);
		float bottomY = Math.max(y, selectRegionStartY);
		for (MidiNote selectedNote : Managers.midiManager.getMidiNotes()) {
			float levelY = levelToY(selectedNote.getLevel(currLevelType));
			if (leftTick < selectedNote.getOnTick()
					&& rightTick > selectedNote.getOnTick() && topY < levelY
					&& bottomY > levelY)
				selectedNote.setSelected(true);
			else
				selectedNote.setSelected(false);
		}
		initSelectRegionVb(leftTick, rightTick, topY, bottomY);
	}

	private void updateDragLine() {
		int touchedSize = touchedLevels.values().size();
		if (touchedSize == 1) {
			DragLine.m = 0;
			MidiNote touched = (MidiNote) touchedLevels.values().toArray()[0];
			DragLine.b = touched.getLevel(currLevelType);
			DragLine.leftTick = 0;
			DragLine.rightTick = Float.MAX_VALUE;
			DragLine.leftLevel = DragLine.rightLevel = touched
					.getLevel(currLevelType);
		} else if (touchedSize == 2) {
			MidiNote leftLevel = touchedLevels.get(0).getOnTick() < touchedLevels
					.get(1).getOnTick() ? touchedLevels.get(0) : touchedLevels
					.get(1);
			MidiNote rightLevel = touchedLevels.get(0).getOnTick() < touchedLevels
					.get(1).getOnTick() ? touchedLevels.get(1) : touchedLevels
					.get(0);
			DragLine.m = (rightLevel.getLevel(currLevelType) - leftLevel
					.getLevel(currLevelType))
					/ (rightLevel.getOnTick() - leftLevel.getOnTick());
			DragLine.b = (leftLevel.getLevel(currLevelType) - DragLine.m
					* leftLevel.getOnTick());
			DragLine.leftTick = leftLevel.getOnTick();
			DragLine.rightTick = rightLevel.getOnTick();
			DragLine.leftLevel = leftLevel.getLevel(currLevelType);
			DragLine.rightLevel = rightLevel.getLevel(currLevelType);
		}
	}

	private void updateLevelOffsets() {
		levelOffsets.clear();
		updateDragLine();
		for (MidiNote selectedNote : Managers.midiManager.getSelectedNotes()) {
			levelOffsets.put(selectedNote, selectedNote.getLevel(currLevelType)
					- DragLine.getLevel(selectedNote.getOnTick()));
		}
	}

	private void setLevelsToDragLine() {
		for (MidiNote selectedNote : Managers.midiManager.getSelectedNotes()) {
			if (levelOffsets.get(selectedNote) != null) {
				selectedNote.setLevel(currLevelType,
						DragLine.getLevel(selectedNote.getOnTick())
								+ levelOffsets.get(selectedNote));
			}
		}
	}

	private void startSelectRegion(float x, float y) {
		selectRegionStartTick = xToTick(x);
		selectRegionStartY = y;
		selectRegionVb = null;
		selectRegion = true;
	}

	protected void drawFrame() {
		drawLevels();
		drawSelectRegion();
	}
	
	private float tickToX(float tick) {
		return GlobalVars.midiView.tickToX(tick) - MidiView.X_OFFSET;
	}

	private float xToTick(float x) {
		return GlobalVars.midiView.xToTick(x + MidiView.X_OFFSET);
	}
	
	private float levelToY(float level) {
		return height - level * (height - LEVEL_POINT_SIZE) - LEVEL_POINT_SIZE
				/ 2;
	}

	/*
	 * map y value of level bar to a value in [0,1]
	 */
	private float yToLevel(float y) {
		return (height - y - LEVEL_POINT_SIZE / 2)
				/ (height - LEVEL_POINT_SIZE);
	}

	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		touchedLevels.remove(id);
		updateLevelOffsets();
	}

	public void handleActionMove(MotionEvent e) {
		if (!touchedLevels.isEmpty()) {
			for (int i = 0; i < e.getPointerCount(); i++) {
				MidiNote touched = touchedLevels.get(e.getPointerId(i));
				if (touched != null) {
					touched.setLevel(currLevelType, yToLevel(e.getY(i)));
				}
			}
			updateDragLine();
			setLevelsToDragLine();
			// velocity changes are valid undo events
			MidiView.stateChanged = true;
		} else { // no midi selected. midiView can handle it.
			selectRegion(e.getX(0), e.getY(0));
		}
		GlobalVars.midiView.updateLoopMarkers(e);
	}
	
	protected void loadIcons() {
		// no icons to load
	}
	
	@Override
	protected void init() {
		final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)getLayoutParams();
		params.leftMargin = (int)MidiView.X_OFFSET;
		Handler refresh = new Handler(Looper.getMainLooper());
		refresh.post(new Runnable() {
		    public void run()
		    {
		        setLayoutParams(params);
		    }
		});
		
		setBackgroundColor(Colors.MIDI_VIEW_DEFAULT_BG_COLOR);
		gl = MidiView.getGL10();
		initLevelBarVb();
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		if (!selectLevel(x, y, id)) {
			startSelectRegion(x, y);
		}
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		selectLevel(x, y, id);
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		clearTouchedLevels();
		selectRegion = false;
	}
}
