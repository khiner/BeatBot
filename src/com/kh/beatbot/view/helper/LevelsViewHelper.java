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
	private class DragLine {
		private float m;
		private float b;
		private long leftTick, rightTick;
		private float leftLevel, rightLevel;

		public DragLine() {
			this.m = 0;
			this.b = 0;
			this.leftTick = Long.MIN_VALUE;
			this.rightTick = Long.MAX_VALUE;
			this.leftLevel = 0;
			this.leftTick = 0;
		}

		public float getLevel(long tick) {
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
	private FloatBuffer levelBarsVB = null;
	private FloatBuffer selectedLevelBarsVB = null;

	private MidiView midiView;
	private MidiViewBean bean;
	private MidiManager midiManager;
	private GL10 gl;

	DragLine dragLine = new DragLine();

	// map of pointerIds to the notes they are selecting
	private Map<Integer, MidiNote> touchedLevels = new HashMap<Integer, MidiNote>();

	// map Midi Note to the offset of their level relative to the touched
	// level(s)
	private Map<MidiNote, Float> levelOffsets = new HashMap<MidiNote, Float>();

	// last single-tapped level-note
	private MidiNote tappedLevelNote = null;

	private LevelMode levelMode = LevelMode.VOLUME;

	public LevelsViewHelper(MidiView midiView) {
		this.midiView = midiView;
		this.bean = midiView.getBean();
		this.midiManager = midiView.getMidiManager();
		this.gl = midiView.getGL10();
	}

	public void setLevelMode(LevelMode levelMode) {
		this.levelMode = levelMode;
	}

	public void clearTouchedNotes() {
		touchedLevels.clear();
	}

	private void initLevelBarsVB() {
		ArrayList<Float> selectedLevelBars = new ArrayList<Float>();
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			if (!midiNote.isLevelSelected())
				continue;
			float x = midiView.tickToX(midiNote.getOnTick());
			selectedLevelBars.add(x);
			selectedLevelBars.add(levelToY(midiNote.getLevel(levelMode)));
			selectedLevelBars.add(x);
			selectedLevelBars.add(bean.getHeight());
		}
		ArrayList<Float> levelBars = new ArrayList<Float>();
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			if (!midiNote.isLevelViewSelected())
				continue;
			float x = midiView.tickToX(midiNote.getOnTick());
			if (!midiNote.isLevelSelected()) {
				levelBars.add(x);
				levelBars.add(levelToY(midiNote.getLevel(levelMode)));
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

	private void drawLevels() {
		drawLevels(true);
		drawLevels(false);
	}

	private void drawLevels(boolean selected) {
		FloatBuffer vertexBuffer = selected ? selectedLevelBarsVB : levelBarsVB;
		float[] color = null;
		if (selected) {
			color = MidiViewBean.LEVEL_SELECTED_COLOR;
		} else {
			switch (levelMode) {
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

	public void selectLevel(float x, float y, int pointerId) {
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			if (!midiNote.isLevelViewSelected())
				continue;
			float velocityY = levelToY(midiNote.getLevel(levelMode));
			if (Math.abs(midiView.tickToX(midiNote.getOnTick()) - x) < 35
					&& Math.abs(velocityY - y) < 35) {
				// If this is the only touched level, and it hasn't yet
				// been selected, make it the only selected level.
				// If we are multi-selecting, add it to the selected list
				if (!midiNote.isLevelSelected()) {
					if (touchedLevels.isEmpty())
						deselectAllLevels();
					midiNote.setLevelSelected(true);
				}
				touchedLevels.put(pointerId, midiNote);
				updateLevelOffsets();
				return;
			}
		}
	}

	public void selectLevelNote(float x, float y) {
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

	private void addToLevelViewSelected(MidiNote midiNote) {
		for (MidiNote overlapping : getOverlapping(midiNote)) {
			overlapping.setLevelViewSelected(false);
			overlapping.setLevelSelected(false);
		}
		if (!midiNote.isLevelViewSelected())
			midiNote.setLevelViewSelected(true);
	}

	private ArrayList<MidiNote> getOverlapping(MidiNote midiNote) {
		ArrayList<MidiNote> overlapping = new ArrayList<MidiNote>();
		for (MidiNote otherNote : midiManager.getMidiNotes()) {
			if (!otherNote.equals(midiNote)
					&& midiNote.getOnTick() == otherNote.getOnTick())
				overlapping.add(otherNote);
		}
		return overlapping;
	}

	public void selectRegion(long leftTick, long rightTick, float topY,
			float bottomY) {
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			if (!midiNote.isLevelViewSelected())
				return;
			float levelY = levelToY(midiNote.getLevel(levelMode));
			if (leftTick < midiNote.getOnTick()
					&& rightTick > midiNote.getOnTick() && topY < levelY
					&& bottomY > levelY) {
				midiNote.setLevelSelected(true);
			} else
				midiNote.setLevelSelected(false);
		}
	}

	private void updateDragLine() {
		int touchedSize = touchedLevels.values().size();
		if (touchedSize == 1) {
			dragLine.m = 0;
			MidiNote touched = (MidiNote) touchedLevels.values().toArray()[0];
			dragLine.b = touched.getLevel(levelMode);
			dragLine.leftTick = Long.MIN_VALUE;
			dragLine.rightTick = Long.MAX_VALUE;
		} else if (touchedSize == 2) {
			MidiNote leftLevel = touchedLevels.get(0).getOnTick() < touchedLevels
					.get(1).getOnTick() ? touchedLevels.get(0) : touchedLevels
					.get(1);
			MidiNote rightLevel = touchedLevels.get(0).getOnTick() < touchedLevels
					.get(1).getOnTick() ? touchedLevels.get(1) : touchedLevels
					.get(0);
			dragLine.m = (rightLevel.getLevel(levelMode) - leftLevel
					.getLevel(levelMode))
					/ (rightLevel.getOnTick() - leftLevel.getOnTick());
			dragLine.b = (leftLevel.getLevel(levelMode) - dragLine.m
					* leftLevel.getOnTick());
			dragLine.leftTick = leftLevel.getOnTick();
			dragLine.rightTick = rightLevel.getOnTick();
			dragLine.leftLevel = leftLevel.getLevel(levelMode);
			dragLine.rightLevel = rightLevel.getLevel(levelMode);
		}
	}

	private void updateLevelOffsets() {
		levelOffsets.clear();
		updateDragLine();
		for (MidiNote selected : midiManager.getMidiNotes()) {
			if (!selected.isLevelSelected())
				return;
			levelOffsets.put(
					selected,
					selected.getLevel(levelMode)
							- dragLine.getLevel(selected.getOnTick()));
		}
	}

	private void deselectAllLevelViews() {
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			midiNote.setLevelViewSelected(false);
		}
	}

	private void deselectAllLevels() {
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			midiNote.setLevelSelected(false);
		}
	}

	// add all non-overlapping notes to selectedLevelNotes
	public void updateSelectedLevelNotes() {
		deselectAllLevelViews();
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			addToLevelViewSelected(midiNote);
			midiNote.setLevelViewSelected(true);
		}
	}

	private float[] calculateColor(MidiNote midiNote) {
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

	private void drawAllMidiNotes() {
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

	public void drawFrame() {
		drawAllMidiNotes();
		initLevelBarsVB();
		drawLevels();
	}

	private float levelToY(float level) {
		return bean.getHeight() - MidiViewBean.LEVEL_POINT_SIZE / 2 - level
				* bean.getLevelsHeight();
	}

	private float yToLevel(float y) {
		return (bean.getHeight() - MidiViewBean.LEVEL_POINT_SIZE / 2 - y)
				/ bean.getLevelsHeight();
	}

	public void doubleTap() {
		if (tappedLevelNote == null)
			return;
		tappedLevelNote.setLevelSelected(false);
		tappedLevelNote.setLevelViewSelected(false);
		tappedLevelNote.setSelected(false);
		midiManager.removeNote(tappedLevelNote);
		updateSelectedLevelNotes();
		bean.setStateChanged(true);
	}

	public void resetSelected() {
		deselectAllLevels();
		updateSelectedLevelNotes();
	}

	public boolean handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		selectLevel(x, y, id);
		if (touchedLevels.isEmpty() && e.getPointerCount() == 2) {
			// init zoom anchors (the same ticks should be under the fingers
			// at all times)
			float leftAnchorX = Math.min(e.getX(0), e.getX(1));
			float rightAnchorX = Math.max(e.getX(0), e.getX(1));
			bean.setZoomLeftAnchorTick(midiView.xToTick(leftAnchorX));
			bean.setZoomRightAnchorTick(midiView.xToTick(rightAnchorX));
		}
		return true;
	}

	public boolean handleActionPointerUp(MotionEvent e, int id) {
		touchedLevels.remove(id);
		updateLevelOffsets();
		// TODO : using getActionIndex could introduce bugs.
		int index = e.getActionIndex() == 0 ? 1 : 0;
		if (e.getPointerCount() == 2) {
			long tick = midiView.xToTick(e.getX(index));
			bean.setScrollAnchorTick(tick);
		}
		return true;
	}

	public boolean handleActionMove(MotionEvent e) {
		if (!touchedLevels.isEmpty()) {
			for (int i = 0; i < e.getPointerCount(); i++) {
				MidiNote touched = touchedLevels.get(e.getPointerId(i));
				if (touched != null) {
					touched.setLevel(levelMode, yToLevel(e.getY(i)));
				}
			}
			updateDragLine();
			for (MidiNote selected : midiManager.getMidiNotes()) {
				if (selected.isLevelSelected()
						&& levelOffsets.get(selected) != null) {
					selected.setLevel(levelMode,
							dragLine.getLevel(selected.getOnTick())
									+ levelOffsets.get(selected));
				}
			}
			// velocity changes are valid undo events
			bean.setStateChanged(true);
		} else { // no midi selected. midiView can handle it.
			midiView.noMidiMove(e);
		}
		return true;
	}
}
