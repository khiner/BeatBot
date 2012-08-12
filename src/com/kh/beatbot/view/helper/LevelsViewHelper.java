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

	// Level Bar Vertex Buffers
	private static FloatBuffer levelBarsVB = null;
	private static FloatBuffer selectedLevelBarsVB = null;

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

	private static void initLevelBarsVB() {
		ArrayList<Float> selectedLevelBars = new ArrayList<Float>();
		for (MidiNote levelSelected : midiManager.getLevelSelectedNotes()) {
			float x = midiView.tickToX(levelSelected.getOnTick());
			selectedLevelBars.add(x);
			selectedLevelBars.add(levelToY(levelSelected
					.getLevel(currLevelMode)));
			selectedLevelBars.add(x);
			selectedLevelBars.add(bean.getHeight());
		}
		ArrayList<Float> levelBars = new ArrayList<Float>();
		for (MidiNote levelViewSelected : midiManager
				.getLevelViewSelectedNotes()) {
			float x = midiView.tickToX(levelViewSelected.getOnTick());
			if (!levelViewSelected.isLevelSelected()) {
				levelBars.add(x);
				levelBars.add(levelToY(levelViewSelected
						.getLevel(currLevelMode)));
				levelBars.add(x);
				levelBars.add(bean.getHeight());
			}
		}
		float[] levelBarsAry = new float[levelBars.size()];
		for (int i = 0; i < levelBarsAry.length; i++)
			levelBarsAry[i] = levelBars.get(i);
		float[] selectedLevelBarsAry = new float[selectedLevelBars.size()];
		for (int i = 0; i < selectedLevelBarsAry.length; i++)
			selectedLevelBarsAry[i] = selectedLevelBars.get(i);
		levelBarsVB = MidiView.makeFloatBuffer(levelBarsAry);
		selectedLevelBarsVB = MidiView.makeFloatBuffer(selectedLevelBarsAry);
	}

	private static void drawLevels() {
		drawLevels(true);
		drawLevels(false);
	}

	private static void drawLevels(boolean selected) {
		FloatBuffer vertexBuffer = selected ? selectedLevelBarsVB : levelBarsVB;
		float[] color = null;
		if (selected) {
			color = MidiViewBean.LEVEL_SELECTED_COLOR;
		} else {
			switch (currLevelMode) {
			case VOLUME:
				color = MidiViewBean.VOLUME_COLOR;
				break;
			case PAN:
				color = MidiViewBean.PAN_COLOR;
				break;
			case PITCH:
				color = MidiViewBean.PITCH_COLOR;
				break;
			}
		}

		gl.glLineWidth(2f); // 2 pixels wide
		// draw each line (2*blurWidth) times, translating and changing the
		// alpha channel
		// for each line, to achieve a DIY "blur" effect
		// this blur is animated to get wider and narrower for a "pulse" effect
		float blurWidth = ((bean.getAnimateCount() / 150)) % 2 == 0 ? (bean
				.getAnimateCount() / 30f) % 5
				: 5 - (bean.getAnimateCount() / 30f) % 5;
		blurWidth += 15;
		for (float i = -blurWidth; i < blurWidth; i++) {
			float alpha = 1 - Math.abs(i) / (float) blurWidth;
			// calculate color. selected bars are always red,
			// non-selected bars depend on the LevelMode type
			gl.glColor4f(color[0], color[1], color[2], alpha);
			gl.glTranslatef(i, 0, 0);
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
			gl.glDrawArrays(GL10.GL_LINES, 0, vertexBuffer.capacity() / 2);
			gl.glTranslatef(-i, 0, 0);
			// draw circles (big points) at top of level bars
			if (i < 1)
				continue;
			gl.glPointSize(i * 3.5f);
			for (int j = 0; j < vertexBuffer.capacity() / 2; j += 2) {
				gl.glDrawArrays(GL10.GL_POINTS, j, 1);
			}
		}
		bean.incrementAnimateCount();
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
		initLevelBarsVB();
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
