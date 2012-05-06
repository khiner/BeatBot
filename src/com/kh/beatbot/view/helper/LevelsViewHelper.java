package com.kh.beatbot.view.helper;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import android.view.MotionEvent;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.bean.MidiViewBean;

public class LevelsViewHelper {
	private class DragLine {
		private float m;
		private int b;
		private long leftTick, rightTick;
		private int leftLevel, rightLevel;
		
		public DragLine() {
			this.m = 0;
			this.b = 0;
			this.leftTick = Long.MIN_VALUE;
			this.rightTick = Long.MAX_VALUE;
			this.leftLevel = 0;
			this.leftTick = 0;
		}

		public int getLevel(long tick) {
			if (tick < leftTick)
				return leftLevel;
			if (tick > rightTick)
				return rightLevel;
			
			return (int)(m*tick + b);
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
	// notes to display level info for in LEVEL_VIEW (volume, etc.)
	// (for multiple notes with same start tick, only one displays level info)
	private List<MidiNote> selectedLevelNotes = new ArrayList<MidiNote>();

	// note with their levels currently selected, and being manipulated.
	private List<MidiNote> selectedLevels = new ArrayList<MidiNote>();

	// map of pointerIds to the notes they are selecting
	private Map<Integer, MidiNote> touchedLevels = new HashMap<Integer, MidiNote>();

	// map Midi Note to the offset of their level relative to the touched
	// level(s)
	private Map<MidiNote, Integer> levelOffsets = new HashMap<MidiNote, Integer>();

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
		selectedLevels.clear();
	}

	public List<MidiNote> getSelectedLevelNotes() {
		return selectedLevelNotes;
	}

	public void clearTouchedNotes() {
		touchedLevels.clear();
	}

	private void initLevelBarsVB() {
		float[] selectedLevelBars = new float[selectedLevels.size() * 4];

		for (int i = 0; i < selectedLevels.size(); i++) {
			MidiNote levelNote = selectedLevels.get(i);
			float x = midiView.tickToX(levelNote.getOnTick());
			selectedLevelBars[i * 4] = x;
			selectedLevelBars[i * 4 + 1] = levelToY(levelNote
					.getLevel(levelMode));
			selectedLevelBars[i * 4 + 2] = x;
			selectedLevelBars[i * 4 + 3] = bean.getHeight();
		}

		float[] levelBars = new float[(selectedLevelNotes.size() - selectedLevels
				.size()) * 4];
		
		int j = 0;
		for (int i = 0; i < selectedLevelNotes.size(); i++) {
			MidiNote levelNote = selectedLevelNotes.get(i);
			float x = midiView.tickToX(levelNote.getOnTick());
			if (!selectedLevels.contains(levelNote)) {
				levelBars[j * 4] = x;
				levelBars[j * 4 + 1] = levelToY(levelNote.getLevel(levelMode));
				levelBars[j * 4 + 2] = x;
				levelBars[j * 4 + 3] = bean.getHeight();
				j++;
			}
		}

		levelBarsVB = MidiView.makeFloatBuffer(levelBars);
		selectedLevelBarsVB = MidiView.makeFloatBuffer(selectedLevelBars);
	}

	private void drawLevels() {
		drawNonSelectedLevels();
		drawSelectedLevels();
	}

	private void drawNonSelectedLevels() {
		if (levelMode == LevelMode.VOLUME)
			gl.glColor4f(MidiViewBean.VOLUME_R, MidiViewBean.VOLUME_G,
					MidiViewBean.VOLUME_B, 1);
		else if (levelMode == LevelMode.PAN)
			gl.glColor4f(MidiViewBean.PAN_R, MidiViewBean.PAN_G,
					MidiViewBean.PAN_B, 1);
		else if (levelMode == LevelMode.PITCH)
			gl.glColor4f(MidiViewBean.PITCH_R, MidiViewBean.PITCH_G,
					MidiViewBean.PITCH_B, 1);
		gl.glLineWidth(MidiViewBean.LEVEL_LINE_WIDTH);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, levelBarsVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, levelBarsVB.capacity() / 2);
		for (int i = 0; i < levelBarsVB.capacity() / 2; i += 2) {
			gl.glDrawArrays(GL10.GL_POINTS, i, 1);
		}

	}

	private void drawSelectedLevels() {
		if (levelMode == LevelMode.VOLUME)
			gl.glColor4f(MidiViewBean.VOLUME_SELECTED_R,
					MidiViewBean.VOLUME_SELECTED_G,
					MidiViewBean.VOLUME_SELECTED_B, 1);
		else if (levelMode == LevelMode.PAN)
			gl.glColor4f(MidiViewBean.PAN_SELECTED_R,
					MidiViewBean.PAN_SELECTED_G, MidiViewBean.PAN_SELECTED_B, 1);
		else if (levelMode == LevelMode.PITCH)
			gl.glColor4f(MidiViewBean.PITCH_SELECTED_R,
					MidiViewBean.PITCH_SELECTED_G,
					MidiViewBean.PITCH_SELECTED_B, 1);
		gl.glLineWidth(MidiViewBean.LEVEL_LINE_WIDTH);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, selectedLevelBarsVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, selectedLevelBarsVB.capacity() / 2);
		for (int i = 0; i < selectedLevelBarsVB.capacity() / 2; i += 2) {
			gl.glDrawArrays(GL10.GL_POINTS, i, 1);
		}
	}

	public void selectLevel(float x, float y, int pointerId) {
		for (MidiNote midiNote : selectedLevelNotes) {
			float velocityY = levelToY(midiNote.getLevel(levelMode));
			if (Math.abs(midiView.tickToX(midiNote.getOnTick()) - x) < 35
					&& Math.abs(velocityY - y) < 35) {
				// If this is the only touched level, and it hasn't yet
				// been selected, make it the only selected level.
				// If we are multi-selecting, add it to the selected list
				if (!selectedLevels.contains(midiNote)) {
					if (touchedLevels.isEmpty())
						selectedLevels.clear();
					selectedLevels.add(midiNote);
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
				addToSelectedLevelNotes(midiNote);
				tappedLevelNote = midiNote;
				return;
			}
		}
	}

	private void selectWithRegion(float x, float y) {
		long tick = midiView.xToTick(x);

		long leftTick = Math.min(tick, bean.getSelectRegionStartTick());
		long rightTick = Math.max(tick, bean.getSelectRegionStartTick());
		float topY = Math.min(y, bean.getSelectRegionStartY());
		float bottomY = Math.max(y, bean.getSelectRegionStartY());
		for (MidiNote midiNote : selectedLevelNotes) {
			float levelY = levelToY(midiNote.getLevel(levelMode));
			if (leftTick < midiNote.getOnTick()
					&& rightTick > midiNote.getOnTick() && topY < levelY
					&& bottomY > levelY) {
				if (!selectedLevels.contains(midiNote)) {
					selectedLevels.add(midiNote);
				}
			} else
				selectedLevels.remove(midiNote);
		}
		// make room in the view window if we are dragging out of the view
		midiView.getTickWindow().updateView(leftTick, rightTick);
		midiView.updateSelectRegionVB(leftTick, rightTick, topY, bottomY);
	}

	// add midiNote to selectedLevelNotes.
	// if another note in the list has the same onTick,
	// it is replaced by midiNote
	private void addToSelectedLevelNotes(MidiNote midiNote) {
		long tick = midiNote.getOnTick();
		for (int i = 0; i < selectedLevelNotes.size(); i++) {
			MidiNote selected = selectedLevelNotes.get(i);
			if (tick == selected.getOnTick()) {
				selectedLevelNotes.remove(i);
				break;
			}
		}
		selectedLevelNotes.add(midiNote);
	}

	private void updateDragLine() {
		int touchedSize = touchedLevels.values().size();
		if (touchedSize == 1) {
			dragLine.m = 0;
			MidiNote touched = (MidiNote)touchedLevels.values().toArray()[0];
			dragLine.b = touched.getLevel(levelMode);
			dragLine.leftTick = Long.MIN_VALUE;
			dragLine.rightTick = Long.MAX_VALUE;
		} else if (touchedSize == 2) {
			MidiNote leftLevel = touchedLevels.get(0).getOnTick() < touchedLevels.get(1).getOnTick() ? touchedLevels.get(0) : touchedLevels.get(1);
			MidiNote rightLevel = touchedLevels.get(0).getOnTick() < touchedLevels.get(1).getOnTick() ? touchedLevels.get(1) : touchedLevels.get(0);
			dragLine.m = (float)(rightLevel.getLevel(levelMode) - leftLevel.getLevel(levelMode))/(rightLevel.getOnTick() - leftLevel.getOnTick());
			dragLine.b = (int)(leftLevel.getLevel(levelMode) - dragLine.m*leftLevel.getOnTick());
			dragLine.leftTick = leftLevel.getOnTick();
			dragLine.rightTick = rightLevel.getOnTick();
			dragLine.leftLevel = leftLevel.getLevel(levelMode);
			dragLine.rightLevel = rightLevel.getLevel(levelMode);
		}
	}
	
	private void updateLevelOffsets() {
		levelOffsets.clear();
		updateDragLine();
		for (MidiNote selected : selectedLevels) {
			levelOffsets.put(selected, selected.getLevel(levelMode)
					- dragLine.getLevel(selected.getOnTick()));
		}
	}

	// add all non-overlapping notes to selectedLevelNotes
	public void updateSelectedLevelNotes() {
		selectedLevelNotes.clear();
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			addToSelectedLevelNotes(midiNote);
		}
	}

	private void calculateColor(MidiNote midiNote) {
		boolean selected = midiView.isNoteSelected(midiNote);
		boolean levelSelected = selectedLevelNotes.contains(midiNote);

		float blackToWhite = (1 - bean.getBgColor() * 2);
		float whiteToBlack = bean.getBgColor() * 2;
		if (!selected && levelSelected) {
			// fade from red to white
			gl.glColor4f(1, blackToWhite, blackToWhite, 1);
		} else if (selected && levelSelected) {
			// fade from blue to white
			gl.glColor4f(blackToWhite, blackToWhite, 1, 1);
		} else if (!selected && !levelSelected) {
			// fade from red to black
			gl.glColor4f(whiteToBlack, 0, 0, 1);
		} else if (selected && !levelSelected) {
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

	private float levelToY(int level) {
		return bean.getHeight() - MidiViewBean.LEVEL_POINT_SIZE / 2 - level
				* bean.getLevelsHeight() / GlobalVars.LEVEL_MAX;
	}

	private int yToLevel(float y) {
		return (int) (GlobalVars.LEVEL_MAX
				* (bean.getHeight() - MidiViewBean.LEVEL_POINT_SIZE / 2 - y) / bean
					.getLevelsHeight());
	}

	public void doubleTap() {
		if (tappedLevelNote == null)
			return;
		if (selectedLevelNotes.contains(tappedLevelNote))
			selectedLevelNotes.remove(tappedLevelNote);
		midiView.removeNote((tappedLevelNote));
		midiManager.removeNote(tappedLevelNote);
		updateSelectedLevelNotes();
		bean.setStateChanged(true);
	}
	
	public void handleUndo() {
		selectedLevels.clear();
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
				if (touched != null)
					touched.setLevel(levelMode, yToLevel(e.getY(i)));
			}
			updateDragLine();
			for (MidiNote selected : selectedLevels) {
				selected.setLevel(levelMode, dragLine.getLevel(selected.getOnTick()) + levelOffsets.get(selected));
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
