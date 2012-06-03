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
			selectedLevelBars.add(levelToY(midiNote
					.getLevel(levelMode)));
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
		drawNonSelectedLevels();
		drawSelectedLevels();
	}

	private void drawNonSelectedLevels() {
		gl.glLineWidth(2f);
		for (int i = -10; i < 10; i++) {		
			float alpha = 1 - Math.abs(i)/10f;
		if (levelMode == LevelMode.VOLUME)
			gl.glColor4f(MidiViewBean.VOLUME_R, MidiViewBean.VOLUME_G,
					MidiViewBean.VOLUME_B, alpha);
		else if (levelMode == LevelMode.PAN)
			gl.glColor4f(MidiViewBean.PAN_R, MidiViewBean.PAN_G,
					MidiViewBean.PAN_B, alpha);
		else if (levelMode == LevelMode.PITCH)
			gl.glColor4f(MidiViewBean.PITCH_R, MidiViewBean.PITCH_G,
					MidiViewBean.PITCH_B, alpha);
			gl.glTranslatef(i, 0, 0);
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, levelBarsVB);
			gl.glDrawArrays(GL10.GL_LINES, 0, levelBarsVB.capacity() / 2);
			gl.glTranslatef(-i, 0, 0);
			// draw circles (big points) at top of level bars
			if (i < 1)
				continue;
			gl.glPointSize((i*4));			
			for (int j = 0; j < levelBarsVB.capacity() / 2; j += 2) {
				gl.glDrawArrays(GL10.GL_POINTS, j, 1);
			}			
		}
	}

	private void drawSelectedLevels() {
		gl.glLineWidth(2f);
		for (int i = -10; i < 10; i++) {		
			float alpha = 1 - Math.abs(i)/10f;
		if (levelMode == LevelMode.VOLUME)
			gl.glColor4f(MidiViewBean.VOLUME_SELECTED_R, MidiViewBean.VOLUME_SELECTED_G,
					MidiViewBean.VOLUME_SELECTED_B, alpha);
		else if (levelMode == LevelMode.PAN)
			gl.glColor4f(MidiViewBean.PAN_SELECTED_R, MidiViewBean.PAN_SELECTED_G,
					MidiViewBean.PAN_SELECTED_B, alpha);
		else if (levelMode == LevelMode.PITCH)
			gl.glColor4f(MidiViewBean.PITCH_SELECTED_R, MidiViewBean.PITCH_SELECTED_G,
					MidiViewBean.PITCH_SELECTED_B, alpha);
			gl.glTranslatef(i, 0, 0);
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, selectedLevelBarsVB);
			gl.glDrawArrays(GL10.GL_LINES, 0, selectedLevelBarsVB.capacity() / 2);
			gl.glTranslatef(-i, 0, 0);
			// draw circles (big points) at top of level bars
			if (i < 1)
				continue;
			gl.glPointSize((i*4));			
			for (int j = 0; j < selectedLevelBarsVB.capacity() / 2; j += 2) {
				gl.glDrawArrays(GL10.GL_POINTS, j, 1);
			}			
		}
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
		for (MidiNote overlapping : getOverlapping(midiNote))
			overlapping.setLevelViewSelected(false);
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

	private void selectWithRegion(float x, float y) {
		long tick = midiView.xToTick(x);

		long leftTick = Math.min(tick, bean.getSelectRegionStartTick());
		long rightTick = Math.max(tick, bean.getSelectRegionStartTick());
		float topY = Math.min(y, bean.getSelectRegionStartY());
		float bottomY = Math.max(y, bean.getSelectRegionStartY());
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			if (!midiNote.isLevelViewSelected())
				return;
			float levelY = levelToY(midiNote.getLevel(levelMode));
			if (leftTick < midiNote.getOnTick()
					&& rightTick > midiNote.getOnTick() && topY < levelY
					&& bottomY > levelY) {
				if (!midiNote.isLevelSelected()) {
					midiNote.setLevelSelected(true);
				}
			} else
				midiNote.setLevelSelected(false);
		}
		// make room in the view window if we are dragging out of the view
		midiView.getTickWindow().updateView(leftTick, rightTick);
		midiView.updateSelectRegionVB(leftTick, rightTick, topY, bottomY);
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

	private void calculateColor(MidiNote midiNote) {
		boolean selected = midiNote.isSelected();
		boolean levelViewSelected = midiNote.isLevelViewSelected();

		float blackToWhite = (1 - bean.getBgColor() * 2);
		float whiteToBlack = bean.getBgColor() * 2;
		if (!selected && levelViewSelected) {
			// fade from red to white
			gl.glColor4f(1, blackToWhite, blackToWhite, 1);
		} else if (selected && levelViewSelected) {
			// fade from blue to white
			gl.glColor4f(blackToWhite, blackToWhite, 1, 1);
		} else if (!selected && !levelViewSelected) {
			// fade from red to black
			gl.glColor4f(whiteToBlack, 0, 0, 1);
		} else if (selected && !levelViewSelected) {
			// fade from blue to black
			gl.glColor4f(0, 0, whiteToBlack, 1);
		}
	}

	private void drawAllMidiNotes() {
		// not using for-each to avoid concurrent modification
		for (int i = 0; i < midiManager.getMidiNotes().size(); i++) {
			if (midiManager.getMidiNotes().size() <= i)
				break;
			MidiNote midiNote = midiManager.getMidiNote(i);
			if (midiNote != null) {
				calculateColor(midiNote);
				midiView.drawMidiNote(midiNote.getNoteValue(),
						midiNote.getOnTick(), midiNote.getOffTick());
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

	public boolean handleActionPointerDown(MotionEvent e, int index) {
		selectLevel(e.getX(index), e.getY(index), e.getPointerId(index));
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

	public boolean handleActionPointerUp(MotionEvent e, int index) {
		touchedLevels.remove(e.getPointerId(index));
		updateLevelOffsets();
		index = index == 0 ? 1 : 0;
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
				if (selected.isLevelSelected() && levelOffsets.get(selected) != null) {
					selected.setLevel(levelMode,
						dragLine.getLevel(selected.getOnTick())
								+ levelOffsets.get(selected));
				}
			}

			// velocity changes are valid undo events
			bean.setStateChanged(true);

		} else { // no midi selected. scroll, zoom, or update select
					// region
			if (e.getPointerCount() == 1) {
				if (bean.isSelectRegion()) { // update select region
					selectWithRegion(e.getX(0), e.getY(0));
				} else if (bean.isLoopMarkerSelected()) {
					midiManager.setLoopTick(midiView.getTickWindow()
							.getMajorTickToLeftOf(midiView.xToTick(e.getX(0))));
				} else { // one finger scroll
					bean.setScrollVelocity(midiView.getTickWindow().scroll(
							e.getX(0)));
				}
			} else if (e.getPointerCount() == 2) {
				// two finger zoom
				float leftX = Math.min(e.getX(0), e.getX(1));
				float rightX = Math.max(e.getX(0), e.getX(1));
				midiView.getTickWindow().zoom(leftX, rightX);
			}
		}
		return true;
	}
}
