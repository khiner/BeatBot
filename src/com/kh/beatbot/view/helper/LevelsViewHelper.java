package com.kh.beatbot.view.helper;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import android.view.MotionEvent;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.bean.MidiViewBean;

public class LevelsViewHelper {
	public enum LevelMode {
		VOLUME, PAN, PITCH
	};

	// Level Bar Vertex Buffers
	private FloatBuffer velocityBarsVB = null;
	private FloatBuffer panBarsVB = null;
	private FloatBuffer pitchBarsVB = null;

	private MidiView midiView;
	private MidiViewBean bean;
	private MidiManager midiManager;
	private GL10 gl;

	// notes to display level info for in LEVEL_VIEW (volume, etc.)
	// (for multiple notes with same start tick, only one displays level info)
	private List<MidiNote> selectedLevelNotes = new ArrayList<MidiNote>();
	// map of pointerIds to the notes they are selecting
	private Map<Integer, MidiNote> touchedLevelNotes = new HashMap<Integer, MidiNote>();
	
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

	public List<MidiNote> getSelectedLevelNotes() {
		return selectedLevelNotes;
	}
	
	public void clearTouchedNotes() {
		touchedLevelNotes.clear();
	}
	private void initLevelBarsVB() {
		float[] volumeBars = new float[selectedLevelNotes.size() * 4];
		float[] panBars = new float[selectedLevelNotes.size() * 4];
		float[] pitchBars = new float[selectedLevelNotes.size() * 4];
		for (int i = 0; i < selectedLevelNotes.size(); i++) {
			MidiNote levelNote = selectedLevelNotes.get(i);
			float x = midiView.tickToX(levelNote.getOnTick());
			volumeBars[i * 4] = x;
			panBars[i * 4] = x;
			pitchBars[i * 4] = x;
			volumeBars[i * 4 + 1] = levelToY(levelNote.getVelocity());
			panBars[i * 4 + 1] = levelToY(levelNote.getPan());
			pitchBars[i * 4 + 1] = levelToY(levelNote.getPitch());
			volumeBars[i * 4 + 2] = x;
			panBars[i * 4 + 2] = x;
			pitchBars[i * 4 + 2] = x;
			volumeBars[i * 4 + 3] = bean.getHeight();
			panBars[i * 4 + 3] = bean.getHeight();
			pitchBars[i * 4 + 3] = bean.getHeight();
		}

		velocityBarsVB = MidiView.makeFloatBuffer(volumeBars);
		panBarsVB = MidiView.makeFloatBuffer(panBars);
		pitchBarsVB = MidiView.makeFloatBuffer(pitchBars);
	}

	private void drawVelocityBars() {
		gl.glColor4f(MidiViewBean.VOLUME_R, MidiViewBean.VOLUME_G,
				MidiViewBean.VOLUME_B, 1);
		gl.glLineWidth(MidiViewBean.LEVEL_LINE_WIDTH);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, velocityBarsVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, velocityBarsVB.capacity() / 2);
		for (int i = 0; i < velocityBarsVB.capacity() / 2; i += 2) {
			gl.glDrawArrays(GL10.GL_POINTS, i, 1);
		}
	}

	private void drawPanBars() {
		gl.glColor4f(MidiViewBean.PAN_R, MidiViewBean.PAN_G,
				MidiViewBean.PAN_B, 1);
		gl.glLineWidth(MidiViewBean.LEVEL_LINE_WIDTH);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, panBarsVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, panBarsVB.capacity() / 2);
		for (int i = 0; i < panBarsVB.capacity() / 2; i += 2) {
			gl.glDrawArrays(GL10.GL_POINTS, i, 1);
		}
	}

	private void drawPitchBars() {
		gl.glColor4f(MidiViewBean.PITCH_R, MidiViewBean.PITCH_G,
				MidiViewBean.PITCH_B, 1);
		gl.glLineWidth(MidiViewBean.LEVEL_LINE_WIDTH);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, pitchBarsVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, pitchBarsVB.capacity() / 2);
		for (int i = 0; i < pitchBarsVB.capacity() / 2; i += 2) {
			gl.glDrawArrays(GL10.GL_POINTS, i, 1);
		}
	}

	public void selectLevel(float x, float y, int pointerId) {
		for (MidiNote midiNote : selectedLevelNotes) {
			float velocityY = levelToY(midiNote.getLevel(levelMode));
			if (Math.abs(midiView.tickToX(midiNote.getOnTick()) - x) < 35
					&& Math.abs(velocityY - y) < 35) {
				touchedLevelNotes.put(pointerId, midiNote);
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
				midiView.drawMidiNote(midiNote.getNoteValue(), midiNote.getOnTick(),
						midiNote.getOffTick());
			}
		}
	}

	public void drawFrame() {
		drawAllMidiNotes();
		initLevelBarsVB();
		if (levelMode == LevelMode.VOLUME)
			drawVelocityBars();
		else if (levelMode == LevelMode.PAN)
			drawPanBars();
		else if (levelMode == LevelMode.PITCH)
			drawPitchBars();

	}

	private float levelToY(int level) {
		return bean.getHeight() - level * bean.getMidiHeight() / 127;
	}

	private int yToLevel(float y) {
		return (int) (127 * (bean.getHeight() - y) / bean.getMidiHeight());
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

	public boolean moveAction(MotionEvent e) {
		if (!touchedLevelNotes.isEmpty()) {
			for (int i = 0; i < e.getPointerCount(); i++) {
				int id = e.getPointerId(i);
				MidiNote touchedNote = touchedLevelNotes.get(id);
				if (touchedNote == null)
					continue;
				touchedNote.setLevel(levelMode, yToLevel(e.getY(i)));
				// velocity changes are valid undo events
				bean.setStateChanged(true);
			}
		} else { // no midi selected. scroll, zoom, or update select
				// region
			if (e.getPointerCount() == 1) {
				bean.setScrollVelocity(midiView.getTickWindow().scroll(e.getX(0)));
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
