package com.kh.beatbot.view.helper;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import android.view.MotionEvent;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.SurfaceViewBase;
import com.kh.beatbot.view.bean.MidiViewBean;

public class LevelsViewHelper {
	private static class DragLine {
		private static float m = 0;
		private static float b = 0;
		private static long leftTick = Long.MIN_VALUE;
		private static long rightTick = Long.MAX_VALUE;
		private static float leftLevel = 0;
		private static float rightLevel = 0;

		public static float getLevel(long tick) {
			if (tick < leftTick)
				return leftLevel;
			if (tick > rightTick)
				return rightLevel;

			return m * tick + b;
		}
	}

	public enum LevelMode {
		VOLUME, PAN, PITCH
	};

	private static FloatBuffer levelBarVb = null;
	private static final int LEVEL_BAR_WIDTH = MidiViewBean.LEVEL_POINT_SIZE / 2;
	private static MidiView midiView;
	private static MidiViewBean bean;
	private static MidiManager midiManager;
	private static GL10 gl;

	// map of pointerIds to the notes they are selecting
	private static Map<Integer, MidiNote> touchedLevels = new HashMap<Integer, MidiNote>();

	// map Midi Note to the offset of their level relative to the touched
	// level(s)
	private static Map<MidiNote, Float> levelOffsets = new HashMap<MidiNote, Float>();

	// last single-tapped level-note
	private static MidiNote tappedLevelNote = null;

	private static LevelMode currLevelMode = LevelMode.VOLUME;

	public static void initHelper(MidiView _midiView) {
		midiView = _midiView;
		bean = midiView.getBean();
		midiManager = midiView.getMidiManager();
		gl = midiView.getGL10();
		initLevelBarVb();
	}

	public static void setLevelMode(LevelMode levelMode) {
		currLevelMode = levelMode;
	}

	public static void clearTouchedLevels() {
		touchedLevels.clear();
	}

	public static MidiNote getTouchedLevel(int id) {
		return touchedLevels.get(id);
	}

	private static void initLevelBarVb() {
		float[] vertices = new float[800];
		for (int i = 0; i < vertices.length / 4; i++) {
			vertices[i * 4] = -LEVEL_BAR_WIDTH / 2;
			vertices[i * 4 + 1] = bean.getHeight()
					- ((float) i / (vertices.length / 4))
					* bean.getLevelsHeight();
			vertices[i * 4 + 2] = LEVEL_BAR_WIDTH / 2;
			vertices[i * 4 + 3] = vertices[i * 4 + 1];
		}
		levelBarVb = SurfaceViewBase.makeFloatBuffer(vertices);
	}

	private static int calcVertex(float level) {
		int vertex = (int) (level * (levelBarVb.capacity() / 2 - 16));
		vertex += 16;
		vertex += vertex % 2;
		vertex = vertex > 2 ? vertex : 2;
		return vertex;
	}

	protected static void drawLevel(float x, float level, float[] levelColor) {
		int vertex = calcVertex(level);
		gl.glPushMatrix();
		SurfaceViewBase.translate(x, 0);
		SurfaceViewBase.drawTriangleStrip(levelBarVb, levelColor, vertex);

		SurfaceViewBase.translate(LEVEL_BAR_WIDTH / 2 - .15f, 0);
		// draw level-colored circle at beginning and end of level
		SurfaceViewBase.drawPoint(LEVEL_BAR_WIDTH, levelColor, vertex - 2);

		drawLevelSelectionCircle(vertex - 2, levelColor);
		gl.glPopMatrix();
	}

	protected static void drawLevelSelectionCircle(int vertex, float[] levelColor) {
		// draw bigger, translucent 'selection' circle at end of level
		levelColor[3] = .5f;
		SurfaceViewBase.drawPoint(LEVEL_BAR_WIDTH * 2.5f, levelColor, vertex);
		levelColor[3] = 1;
	}

	private static float[] calcLevelColor(boolean selected) {
		if (selected) {
			return MidiViewBean.LEVEL_SELECTED_COLOR;
		} else {
			switch (currLevelMode) {
			case VOLUME:
				return MidiViewBean.VOLUME_COLOR;
			case PAN:
				return MidiViewBean.PAN_COLOR;
			case PITCH:
				return MidiViewBean.PITCH_COLOR;
			default:
				return MidiViewBean.LEVEL_SELECTED_COLOR;
			}
		}
	}

	private static void drawLevels() {
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			if (midiNote.isLevelViewSelected()) {
				drawLevel(midiView.tickToX(midiNote.getOnTick()),
						midiNote.getLevel(currLevelMode),
						calcLevelColor(midiNote.isLevelSelected()));
			}
		}
	}

	public static void selectLevel(float x, float y, int pointerId) {
		for (MidiNote levelViewSelected : midiManager
				.getLevelViewSelectedNotes()) {
			float velocityY = levelToY(levelViewSelected
					.getLevel(currLevelMode));
			if (Math.abs(midiView.tickToX(levelViewSelected.getOnTick()) - x) < 35
					&& Math.abs(velocityY - y) < 35) {
				// If this is the only touched level, and it hasn't yet
				// been selected, make it the only selected level.
				// If we are multi-selecting, add it to the selected list
				if (!levelViewSelected.isLevelSelected()) {
					if (touchedLevels.isEmpty())
						deselectAllLevels();
					levelViewSelected.setLevelSelected(true);
				}
				touchedLevels.put(pointerId, levelViewSelected);
				updateLevelOffsets();
				return;
			}
		}
	}

	public static void selectLevelNote(float x, float y) {
		long tick = midiView.xToTick(x);
		long note = midiView.yToNote(y);

		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			if (midiNote.getNoteValue() == note && midiNote.getOnTick() <= tick
					&& midiNote.getOffTick() >= tick) {
				addToLevelViewSelected(midiNote);
				tappedLevelNote = midiNote;
				return;
			}
		}
	}

	private static void addToLevelViewSelected(MidiNote midiNote) {
		for (MidiNote overlapping : getOverlapping(midiNote)) {
			overlapping.setLevelViewSelected(false);
			overlapping.setLevelSelected(false);
		}
		midiNote.setLevelViewSelected(true);
	}

	private static ArrayList<MidiNote> getOverlapping(MidiNote midiNote) {
		ArrayList<MidiNote> overlapping = new ArrayList<MidiNote>();
		for (MidiNote otherNote : midiManager.getMidiNotes()) {
			if (!otherNote.equals(midiNote)
					&& midiNote.getOnTick() == otherNote.getOnTick())
				overlapping.add(otherNote);
		}
		return overlapping;
	}

	public static void selectRegion(long leftTick, long rightTick, float topY,
			float bottomY) {
		for (MidiNote levelViewSelected : midiManager
				.getLevelViewSelectedNotes()) {
			float levelY = levelToY(levelViewSelected.getLevel(currLevelMode));
			if (leftTick < levelViewSelected.getOnTick()
					&& rightTick > levelViewSelected.getOnTick()
					&& topY < levelY && bottomY > levelY)
				levelViewSelected.setLevelSelected(true);
			else
				levelViewSelected.setLevelSelected(false);
		}
	}

	private static void updateDragLine() {
		int touchedSize = touchedLevels.values().size();
		if (touchedSize == 1) {
			DragLine.m = 0;
			MidiNote touched = (MidiNote) touchedLevels.values().toArray()[0];
			DragLine.b = touched.getLevel(currLevelMode);
			DragLine.leftTick = Long.MIN_VALUE;
			DragLine.rightTick = Long.MAX_VALUE;
		} else if (touchedSize == 2) {
			MidiNote leftLevel = touchedLevels.get(0).getOnTick() < touchedLevels
					.get(1).getOnTick() ? touchedLevels.get(0) : touchedLevels
					.get(1);
			MidiNote rightLevel = touchedLevels.get(0).getOnTick() < touchedLevels
					.get(1).getOnTick() ? touchedLevels.get(1) : touchedLevels
					.get(0);
			DragLine.m = (rightLevel.getLevel(currLevelMode) - leftLevel
					.getLevel(currLevelMode))
					/ (rightLevel.getOnTick() - leftLevel.getOnTick());
			DragLine.b = (leftLevel.getLevel(currLevelMode) - DragLine.m
					* leftLevel.getOnTick());
			DragLine.leftTick = leftLevel.getOnTick();
			DragLine.rightTick = rightLevel.getOnTick();
			DragLine.leftLevel = leftLevel.getLevel(currLevelMode);
			DragLine.rightLevel = rightLevel.getLevel(currLevelMode);
		}
	}

	private static void updateLevelOffsets() {
		levelOffsets.clear();
		updateDragLine();
		for (MidiNote levelSelected : midiManager.getLevelSelectedNotes()) {
			levelOffsets.put(
					levelSelected,
					levelSelected.getLevel(currLevelMode)
							- DragLine.getLevel(levelSelected.getOnTick()));
		}
	}

	private static void setLevelsToDragLine() {
		for (MidiNote levelSelected : midiManager.getLevelSelectedNotes()) {
			if (levelOffsets.get(levelSelected) != null) {
				levelSelected.setLevel(currLevelMode,
						DragLine.getLevel(levelSelected.getOnTick())
								+ levelOffsets.get(levelSelected));
			}
		}
	}

	private static void deselectAllLevelViews() {
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			midiNote.setLevelViewSelected(false);
		}
	}

	private static void deselectAllLevels() {
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			midiNote.setLevelSelected(false);
		}
	}

	// add all non-overlapping notes to selectedLevelNotes
	public static void updateSelectedLevelNotes() {
		deselectAllLevelViews();
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			addToLevelViewSelected(midiNote);
		}
	}

	private static float[] calculateColor(MidiNote midiNote) {
		float[] color = new float[4];
		boolean selected = midiNote.isSelected();
		boolean levelViewSelected = midiNote.isLevelViewSelected();
		float blackToWhite = (1 - bean.getBgColor() * 2);
		float whiteToBlack = bean.getBgColor() * 2;
		if (!selected && levelViewSelected) {
			// fade from red to white
			color[0] = 1;
			color[1] = color[2] = blackToWhite;
		} else if (selected && levelViewSelected) {
			// fade from blue to white
			color[0] = color[1] = blackToWhite;
			color[2] = 1;
		} else if (!selected && !levelViewSelected) {
			// fade from red to black
			color[0] = whiteToBlack;
			color[1] = color[2] = 0;
		} else if (selected && !levelViewSelected) {
			// fade from blue to black
			color[0] = color[1] = 0;
			color[2] = whiteToBlack;
		}
		color[3] = 1; // alpha always 1
		return color;
	}

	private static void drawAllMidiNotes() {
		// not using for-each to avoid concurrent modification
		for (int i = 0; i < midiManager.getMidiNotes().size(); i++) {
			if (midiManager.getMidiNotes().size() <= i)
				break;
			MidiNote midiNote = midiManager.getMidiNote(i);
			if (midiNote != null) {
				midiView.drawMidiNote(midiNote, calculateColor(midiNote));
			}
		}
	}

	public static void drawFrame() {
		drawAllMidiNotes();
		drawLevels();
	}

	private static float levelToY(float level) {
		return bean.getHeight() - MidiViewBean.LEVEL_POINT_SIZE / 2 - level
				* bean.getLevelsHeight();
	}

	private static float yToLevel(float y) {
		return (bean.getHeight() - MidiViewBean.LEVEL_POINT_SIZE / 2 - y)
				/ bean.getLevelsHeight();
	}

	public static void doubleTap() {
		if (tappedLevelNote == null)
			return;
		tappedLevelNote.setLevelSelected(false);
		tappedLevelNote.setLevelViewSelected(false);
		tappedLevelNote.setSelected(false);
		midiManager.deleteNote(tappedLevelNote);
		updateSelectedLevelNotes();
		bean.setStateChanged(true);
	}

	public static void resetSelected() {
		deselectAllLevels();
		updateSelectedLevelNotes();
	}

	public static void handleActionPointerUp(MotionEvent e, int id) {
		touchedLevels.remove(id);
		updateLevelOffsets();
	}

	public static void handleActionMove(MotionEvent e) {
		if (!touchedLevels.isEmpty()) {
			for (int i = 0; i < e.getPointerCount(); i++) {
				MidiNote touched = touchedLevels.get(e.getPointerId(i));
				if (touched != null) {
					touched.setLevel(currLevelMode, yToLevel(e.getY(i)));
				}
			}
			updateDragLine();
			setLevelsToDragLine();
			// velocity changes are valid undo events
			bean.setStateChanged(true);
		} else { // no midi selected. midiView can handle it.
			midiView.noMidiMove(e);
		}
		midiView.updateLoopMarkers(e);
	}
}
