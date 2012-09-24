package com.kh.beatbot.view;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.view.bean.MidiViewBean;
import com.kh.beatbot.view.helper.LevelsViewHelper;
import com.kh.beatbot.view.helper.MidiTrackControlHelper;
import com.kh.beatbot.view.helper.ScrollBarHelper;
import com.kh.beatbot.view.helper.TickWindowHelper;
import com.kh.beatbot.view.helper.WaveformHelper;

public class MidiView extends ClickableSurfaceView {

	private static MidiManager midiManager;
	private MidiViewBean bean = new MidiViewBean();

	private static final int[] V_LINE_WIDTHS = new int[] { 5, 3, 2 };
	private static final float[] V_LINE_COLORS = new float[] { 0, .2f, .3f };

	// NIO Buffers
	private FloatBuffer[] vLineVb = new FloatBuffer[3];
	private FloatBuffer currTickVb = null;
	private FloatBuffer hLineVb = null;
	private FloatBuffer tickFillVb = null;
	private FloatBuffer selectRegionVb = null;
	private FloatBuffer loopMarkerVb = null;
	private FloatBuffer loopMarkerLineVb = null;
	private FloatBuffer loopRectVb = null;
	private FloatBuffer bgRectVb = null;

	// map of pointerIds to the notes they are selecting
	private Map<Integer, MidiNote> touchedNotes = new HashMap<Integer, MidiNote>();

	// map of pointerIds to the original on-ticks of the notes they are touching
	// (before dragging)
	private Map<Integer, Float> startOnTicks = new HashMap<Integer, Float>();

	private List<Integer> myPointers = new ArrayList<Integer>();

	public enum State {
		LEVELS_VIEW, NORMAL_VIEW, TO_LEVELS_VIEW, TO_NORMAL_VIEW
	};

	private WaveformHelper waveformHelper;

	public MidiView(Context context, AttributeSet attrs) {
		super(context, attrs);
		bean.setHeight(height);
		bean.setWidth(width);
		for (int i = 0; i < 5; i++) {
			bean.setDragOffsetTick(i, 0);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		super.surfaceChanged(holder, format, width, height);
		bean.setWidth(width);
		bean.setHeight(height);
	}

	public void initMeFirst() {
		midiManager = Managers.midiManager;
		TickWindowHelper.viewBean = bean;
		TickWindowHelper.updateGranularity();
	}

	public MidiViewBean getBean() {
		return bean;
	}

	public GL10 getGL10() {
		return gl;
	}

	public State getViewState() {
		return bean.getViewState();
	}

	public void handleUndo() {
		LevelsViewHelper.resetSelected();
	}

	public void setViewState(State viewState) {
		if (viewState == State.TO_LEVELS_VIEW
				|| viewState == State.TO_NORMAL_VIEW)
			return;
		bean.setViewState(viewState);
		if (viewState == State.LEVELS_VIEW)
			bean.setBgColor(0);
		else
			bean.setBgColor(Colors.MIDI_VIEW_DEFAULT_BG_COLOR);
	}

	public void setLevelMode(LevelsViewHelper.LevelMode levelMode) {
		LevelsViewHelper.setLevelMode(levelMode);
	}

	public void reset() {
		TickWindowHelper.setTickOffset(0);
	}

	public void drawWaveform(byte[] bytes) {
		waveformHelper.addBytesToQueue(bytes);
	}

	public void endWaveform() {
		waveformHelper.endWaveform();
	}

	private void selectRegion(float x, float y) {
		float tick = xToTick(x);
		float leftTick = Math.min(tick, bean.getSelectRegionStartTick());
		float rightTick = Math.max(tick, bean.getSelectRegionStartTick());
		float topY = Math.min(y, bean.getSelectRegionStartY());
		float bottomY = Math.max(y, bean.getSelectRegionStartY());
		// make sure select rect doesn't go into the tick view
		topY = Math.max(topY, MidiViewBean.Y_OFFSET);
		// make sure select rect doesn't go past the last track/note
		bottomY = Math.min(bottomY, MidiViewBean.Y_OFFSET
				+ MidiTrackControlHelper.height - .01f);
		if (bean.getViewState() == State.LEVELS_VIEW) {
			LevelsViewHelper.selectRegion(leftTick, rightTick, topY, bottomY);
		} else {
			int topNote = yToNote(topY);
			int bottomNote = yToNote(bottomY);
			midiManager.selectRegion((long) leftTick, (long) rightTick,
					topNote, bottomNote);
			// for normal view, round the drawn rectangle to nearest notes
			topY = noteToY(topNote);
			bottomY = noteToY(bottomNote + 1);
		}
		// make room in the view window if we are dragging out of the view
		TickWindowHelper.updateView(leftTick, rightTick);
		initSelectRegionVb(leftTick, rightTick, topY, bottomY);
	}

	private void selectMidiNote(float x, float y, int pointerId) {
		float tick = xToTick(x);
		int note = yToNote(y);

		for (int i = 0; i < midiManager.getMidiNotes().size(); i++) {
			MidiNote midiNote = midiManager.getMidiNotes().get(i);
			if (midiNote.getNoteValue() == note && midiNote.getOnTick() <= tick
					&& midiNote.getOffTick() >= tick) {
				if (!touchedNotes.containsValue(midiNote)) {
					startOnTicks.put(pointerId, (float) midiNote.getOnTick());
					float leftOffset = tick - midiNote.getOnTick();
					bean.setDragOffsetTick(pointerId, leftOffset);
					// don't need right offset for simple drag (one finger
					// select)

					// If this is the only touched midi note, and it hasn't yet
					// been selected, make it the only selected note.
					// If we are multi-selecting, add it to the selected list
					if (!midiNote.isSelected()) {
						if (touchedNotes.isEmpty())
							midiManager.deselectAllNotes();
						midiManager.selectNote(midiNote);
					}
					touchedNotes.put(pointerId, midiNote);
				}
				return;
			}
		}
	}

	public void selectLoopMarker(int pointerId, float x) {
		float loopBeginX = tickToX(midiManager.getLoopBeginTick());
		float loopEndX = tickToX(midiManager.getLoopEndTick());
		if (Math.abs(x - loopBeginX) <= MidiViewBean.LOOP_SELECT_SNAP_DIST) {
			bean.setLoopPointerId(0, pointerId);
		} else if (Math.abs(x - loopEndX) <= MidiViewBean.LOOP_SELECT_SNAP_DIST) {
			bean.setLoopPointerId(2, pointerId);
		} else if (x > loopBeginX && x < loopEndX) {
			bean.setLoopPointerId(1, pointerId);
			bean.setLoopSelectionOffset(x - loopBeginX);
		}
	}

	private void drawBgRect() {
		drawTriangleStrip(bgRectVb, bean.getBgColorRgb());
	}

	private void drawHorizontalLines() {
		gl.glColor4f(0, 0, 0, 1);
		gl.glLineWidth(2);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, hLineVb);
		gl.glDrawArrays(GL10.GL_LINES, 0, hLineVb.capacity() / 2);
	}

	private void drawVerticalLines() {
		// distance between one primary tick to the next
		float translateDist = TickWindowHelper.getMajorTickSpacing() * 4f
				* bean.getWidth() / TickWindowHelper.getNumTicks();
		// start at the first primary tick before display start
		float startX = tickToX(TickWindowHelper
				.getPrimaryTickToLeftOf(TickWindowHelper.getTickOffset()));
		// end at the first primary tick after display end
		float endX = tickToX(TickWindowHelper
				.getPrimaryTickToLeftOf(TickWindowHelper.getTickOffset()
						+ TickWindowHelper.getNumTicks()))
				+ translateDist;

		gl.glPushMatrix();
		gl.glTranslatef(startX, 0, 0);
		for (int i = 0; i < 3; i++) {
			float color = V_LINE_COLORS[i];
			gl.glColor4f(color, color, color, 1); // appropriate line color
			gl.glLineWidth(V_LINE_WIDTHS[i]); // appropriate line width
			gl.glPushMatrix();
			for (float x = startX; x < endX; x += translateDist) {
				gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vLineVb[i]);
				gl.glDrawArrays(GL10.GL_LINES, 0, 2);
				gl.glTranslatef(translateDist, 0, 0);
			}
			gl.glPopMatrix();
			if (i == 0) {
				gl.glTranslatef(translateDist / 2, 0, 0);
			} else if (i == 1) {
				translateDist /= 2;
				gl.glTranslatef(-translateDist / 2, 0, 0);
			}
		}
		gl.glPopMatrix();
	}

	private void drawCurrentTick() {
		float xLoc = tickToX(midiManager.getCurrTick());
		gl.glTranslatef(xLoc, 0, 0);
		drawLines(currTickVb, Colors.VOLUME_COLOR, 5, GL10.GL_LINES);
		gl.glTranslatef(-xLoc, 0, 0);
	}

	private void drawLoopMarker() {
		float[][] color = new float[2][3];
		color[0] = bean.getLoopPointerIds()[0] != -1 ? Colors.TICK_SELECTED_COLOR
				: Colors.TICK_MARKER_COLOR;
		color[1] = bean.getLoopPointerIds()[2] != -1 ? Colors.TICK_SELECTED_COLOR
				: Colors.TICK_MARKER_COLOR;
		gl.glLineWidth(6);
		float[] loopMarkerLocs = { tickToX(midiManager.getLoopBeginTick()),
				tickToX(midiManager.getLoopEndTick()) };
		for (int i = 0; i < 2; i++) {
			float loopMarkerLoc = loopMarkerLocs[i];
			setColor(color[i]);
			gl.glPushMatrix();
			gl.glTranslatef(loopMarkerLoc, 0, 0);
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, loopMarkerVb);
			gl.glDrawArrays(GL10.GL_TRIANGLES, i * 3, 3);
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, loopMarkerLineVb);
			gl.glDrawArrays(GL10.GL_LINES, 0, 2);
			gl.glPopMatrix();
		}
	}

	private void drawTickFill() {
		drawTriangleStrip(tickFillVb, Colors.TICK_FILL_COLOR);
		drawLoopBar();
	}

	private void drawLoopBar() {
		float[] color = bean.getLoopPointerIds()[1] == -1 ? Colors.TICKBAR_COLOR
				: Colors.TICK_SELECTED_COLOR;
		// entire loop bar is selected. draw darker square
		drawRectangle(tickToX(midiManager.getLoopBeginTick()), 0,
				tickToX(midiManager.getLoopEndTick()), MidiViewBean.Y_OFFSET,
				color);
	}

	private void drawLoopRect() {
		float gray = bean.getBgColor() + .2f;
		float[] color = new float[] { gray, gray, gray, 1 };
		drawTriangleStrip(loopRectVb, color);
	}

	private void drawRecordingWaveforms() {
		ArrayList<FloatBuffer> waveformVbs = waveformHelper
				.getCurrentWaveformVbs();
		if (RecordManager.isRecording() && !waveformVbs.isEmpty()) {
			FloatBuffer last = waveformVbs.get(waveformVbs.size() - 1);
			float waveWidth = last.get(last.capacity() - 2);
			float noteWidth = tickToX(midiManager.getCurrTick()
					- RecordManager.getRecordStartTick()) - MidiTrackControlHelper.width;
			gl.glPushMatrix();
			gl.glTranslatef(tickToX(RecordManager.getRecordStartTick()), 0, 0);
			// scale drawing so the entire waveform exactly fits in the note
			// width
			gl.glScalef(noteWidth / waveWidth, 1, 1);
			for (int i = 0; i < waveformVbs.size(); i++) {
				drawLines(waveformVbs.get(i), Colors.WAVEFORM_COLOR, 1,
						GL10.GL_LINE_STRIP);
			}
			gl.glPopMatrix();
		}
	}

	public void initSelectRegionVb(float leftTick, float rightTick, float topY,
			float bottomY) {
		selectRegionVb = makeRectFloatBuffer(tickToX(leftTick), topY,
				tickToX(rightTick), bottomY);
	}

	private void drawSelectRegion() {
		if (!bean.isSelectRegion() || selectRegionVb == null)
			return;
		drawTriangleStrip(selectRegionVb, new float[] { .6f, .6f, 1, .7f });
	}

	private void drawAllMidiNotes() {
		// not using for-each to avoid concurrent modification
		for (int i = 0; i < midiManager.getMidiNotes().size(); i++) {
			if (midiManager.getMidiNotes().size() <= i)
				break;
			MidiNote midiNote = midiManager.getMidiNote(i);
			if (midiNote != null) {
				drawMidiNote(midiNote,
						midiNote.isSelected() ? Colors.NOTE_SELECTED_COLOR
								: Colors.NOTE_COLOR);
			}
		}
	}

	public void drawMidiNote(MidiNote midiNote, float[] color) {
		// midi note rectangle coordinates
		float x1 = tickToX(midiNote.getOnTick());
		float y1 = noteToY(midiNote.getNoteValue());
		float x2 = tickToX(midiNote.getOffTick());
		float y2 = y1 + MidiTrackControlHelper.trackHeight;
		// fade outline from black to white
		float baseColor = (1 - bean.getBgColor() * 2);
		drawRectangle(x1, y1, x2, y2, color);
		drawRectangleOutline(x1, y1, x2, y2, new float[] { baseColor,
				baseColor, baseColor, 1 }, 4);
	}

	private void initTickFillVb() {
		tickFillVb = makeRectFloatBuffer(MidiViewBean.X_OFFSET, 0, width,
				MidiViewBean.Y_OFFSET);
	}

	private void initBgRectVb() {
		bgRectVb = makeRectFloatBuffer(MidiViewBean.X_OFFSET,
				MidiViewBean.Y_OFFSET, width, MidiTrackControlHelper.height
						+ MidiViewBean.Y_OFFSET);
	}

	private void initLoopRectVb() {
		loopRectVb = makeRectFloatBuffer(
				tickToX(midiManager.getLoopBeginTick()), MidiViewBean.Y_OFFSET,
				tickToX(midiManager.getLoopEndTick()),
				MidiTrackControlHelper.height + MidiViewBean.Y_OFFSET);
	}

	private void initCurrTickVb() {
		float[] vertLine = new float[] { 0, MidiViewBean.Y_OFFSET, 0,
				MidiTrackControlHelper.height + MidiViewBean.Y_OFFSET };
		currTickVb = makeFloatBuffer(vertLine);
	}

	private void initHLineVb() {
		float[] hLines = new float[(GlobalVars.tracks.size() + 2) * 4];
		hLines[0] = MidiViewBean.X_OFFSET;
		hLines[1] = 0;
		hLines[2] = width;
		hLines[3] = 0;
		float y = MidiViewBean.Y_OFFSET;
		for (int i = 1; i < GlobalVars.tracks.size() + 2; i++) {
			hLines[i * 4] = MidiViewBean.X_OFFSET;
			;
			hLines[i * 4 + 1] = y;
			hLines[i * 4 + 2] = width;
			hLines[i * 4 + 3] = y;
			y += MidiTrackControlHelper.trackHeight;
		}
		hLineVb = makeFloatBuffer(hLines);
	}

	private void initVLineVbs() {
		// height of the bottom of the record row
		float y1 = MidiViewBean.Y_OFFSET;

		for (int i = 0; i < 3; i++) {
			// 4 vertices per line
			float[] line = new float[4];
			line[0] = 0;
			line[1] = y1 - y1 / (i + 1.5f);
			line[2] = 0;
			line[3] = MidiTrackControlHelper.height + MidiViewBean.Y_OFFSET;
			vLineVb[i] = makeFloatBuffer(line);
		}
	}

	private void initLoopMarkerVbs() {
		float h = MidiViewBean.Y_OFFSET;
		float[] loopMarkerLine = new float[] { 0, 0, 0,
				MidiTrackControlHelper.height + MidiViewBean.Y_OFFSET };
		// loop begin triangle, pointing right, and
		// loop end triangle, pointing left
		float[] loopMarkerTriangles = new float[] { 0, 0, 0, h, h, h / 2, 0, 0,
				0, h, -h, h / 2 };
		loopMarkerLineVb = makeFloatBuffer(loopMarkerLine);
		loopMarkerVb = makeFloatBuffer(loopMarkerTriangles);
	}

	public float tickToX(float tick) {
		return (tick - TickWindowHelper.getTickOffset())
				/ TickWindowHelper.getNumTicks() * bean.getWidth()
				+ MidiViewBean.X_OFFSET;
	}

	public float xToTick(float x) {
		return TickWindowHelper.getNumTicks() * (x - MidiViewBean.X_OFFSET)
				/ bean.getWidth() + TickWindowHelper.getTickOffset();
	}

	public static int yToNote(float y) {
		if (y >= 0 && y < MidiViewBean.Y_OFFSET)
			return -1;
		return (int) ((y - MidiViewBean.Y_OFFSET) / MidiTrackControlHelper.trackHeight);
	}

	public static float noteToY(int note) {
		return note * MidiTrackControlHelper.trackHeight
				+ MidiViewBean.Y_OFFSET;
	}

	public void signalRecording() {
		waveformHelper = new WaveformHelper();
		waveformHelper.start();
	}

	public void signalEndRecording() {
		waveformHelper.endRecording();
	}

	public void initAllVbs() {
		initBgRectVb();
		initCurrTickVb();
		initHLineVb();
		initVLineVbs();
		initLoopMarkerVbs();
		initLoopRectVb();
		initTickFillVb();
	}

	protected void init() {
		MidiTrackControlHelper.init();
		LevelsViewHelper.init(this);
		waveformHelper = new WaveformHelper();
		TickWindowHelper.updateGranularity();
		initAllVbs();
	}

	public void updateTracks() {
		int newTrackIndex = GlobalVars.tracks.size() - 1;
		MidiTrackControlHelper.addTrack(newTrackIndex,
				GlobalVars.tracks.get(newTrackIndex).getInstrument()
						.getIconSource());
		initAllVbs();
	}

	protected void calcBgColor() {
		if (bean.getViewState() == State.TO_LEVELS_VIEW
				|| bean.getViewState() == State.TO_NORMAL_VIEW) { // transitioning
			bean.setBgColor(bean.getViewState() == State.TO_LEVELS_VIEW ? bean
					.getBgColor() - MidiViewBean.COLOR_TRANSITION_RATE : bean
					.getBgColor() + MidiViewBean.COLOR_TRANSITION_RATE);
			if (bean.getBgColor() >= .5f) {
				bean.setViewState(State.NORMAL_VIEW);
			} else if (bean.getBgColor() <= 0) {
				bean.setViewState(State.LEVELS_VIEW);
			}
		}
	}

	@Override
	protected void drawFrame() {
		calcBgColor();
		drawBgRect();
		boolean recording = RecordManager.getState() != RecordManager.State.INITIALIZING;
		boolean playing = Managers.playbackManager.getState() == PlaybackManager.State.PLAYING;
		TickWindowHelper.scroll();
		// we need to do this in every frame, because even if loop ticks aren't
		// changing
		// the tick window can change
		initLoopRectVb();
		drawTickFill();
		drawLoopRect();
		// if we're recording, keep the current recording tick in view.
		if (recording
				&& midiManager.getCurrTick() > TickWindowHelper.getTickOffset()
						+ TickWindowHelper.getNumTicks())
			TickWindowHelper.setNumTicks(midiManager.getCurrTick()
					- TickWindowHelper.getTickOffset());
		if (bean.getViewState() != State.LEVELS_VIEW) {
			// normal or transitioning view. draw lines
			drawHorizontalLines();
			drawVerticalLines();
		}
		if (bean.getViewState() != State.NORMAL_VIEW) {
			LevelsViewHelper.drawFrame();
		} else {
			drawAllMidiNotes();
		}
		drawLoopMarker();
		drawSelectRegion();
		ScrollBarHelper.drawScrollView(bean.getWidth(),
				MidiTrackControlHelper.height + MidiViewBean.Y_OFFSET,
				MidiViewBean.X_OFFSET);
		drawRecordingWaveforms();
		if (playing || recording) {
			drawCurrentTick();
		}
		MidiTrackControlHelper.draw();
	}

	private float getAdjustedTickDiff(float tickDiff, int pointerId,
			MidiNote singleNote) {
		if (tickDiff == 0)
			return 0;
		float adjustedTickDiff = tickDiff;
		for (MidiNote selectedNote : midiManager.getSelectedNotes()) {
			if (singleNote != null && !selectedNote.equals(singleNote))
				continue;
			if (Math.abs(startOnTicks.get(pointerId) - selectedNote.getOnTick())
					+ Math.abs(tickDiff) <= 10) {
				// inside threshold distance - set to original position
				return startOnTicks.get(pointerId) - selectedNote.getOnTick();
			}
			if (selectedNote.getOnTick() + tickDiff < 0) {
				if (selectedNote.getOnTick() > adjustedTickDiff)
					adjustedTickDiff = -selectedNote.getOnTick();
			} else if (selectedNote.getOffTick() + tickDiff > TickWindowHelper.MAX_TICKS) {
				if (TickWindowHelper.MAX_TICKS - selectedNote.getOffTick() < adjustedTickDiff)
					adjustedTickDiff = TickWindowHelper.MAX_TICKS
							- selectedNote.getOffTick();
			}
		}
		return adjustedTickDiff;
	}

	private int getAdjustedNoteDiff(int noteDiff, MidiNote singleNote) {
		int adjustedNoteDiff = noteDiff;
		for (MidiNote selectedNote : midiManager.getSelectedNotes()) {
			if (singleNote != null && !selectedNote.equals(singleNote))
				continue;
			if (selectedNote.getNoteValue() + noteDiff < 0
					&& selectedNote.getNoteValue() > adjustedNoteDiff)
				adjustedNoteDiff = -selectedNote.getNoteValue();
			else if (selectedNote.getNoteValue() + noteDiff > GlobalVars.tracks
					.size() - 1
					&& GlobalVars.tracks.size() - 1
							- selectedNote.getNoteValue() < adjustedNoteDiff)
				adjustedNoteDiff = GlobalVars.tracks.size() - 1
						- selectedNote.getNoteValue();
		}
		return adjustedNoteDiff;
	}

	private void pinchNote(MidiNote midiNote, float onTickDiff,
			float offTickDiff) {
		float newOnTick = midiNote.getOnTick();
		float newOffTick = midiNote.getOffTick();
		if (midiNote.getOnTick() + onTickDiff >= 0)
			newOnTick += onTickDiff;
		if (midiNote.getOffTick() + offTickDiff <= TickWindowHelper.MAX_TICKS)
			newOffTick += offTickDiff;
		midiManager.setNoteTicks(midiNote, (long) newOnTick, (long) newOffTick,
				bean.isSnapToGrid(), false);
		bean.setStateChanged(true);
	}

	private void startSelectRegion(float x, float y) {
		bean.setSelectRegionStartTick(xToTick(x));
		if (bean.getViewState() == State.LEVELS_VIEW)
			bean.setSelectRegionStartY(y);
		else
			bean.setSelectRegionStartY(noteToY(yToNote(y)));
		selectRegionVb = null;
		bean.setSelectRegion(true);
	}

	private void cancelSelectRegion() {
		bean.setSelectRegion(false);
		selectRegionVb = null;
	}

	// adds a note starting at the nearest major tick (nearest displayed
	// grid line) to the left and ending one tick before the nearest major
	// tick to the right of the given tick
	private void addMidiNote(float tick, int note) {
		float spacing = TickWindowHelper.getMajorTickSpacing();
		float onTick = tick - tick % spacing;
		float offTick = onTick + spacing - 1;
		addMidiNote(onTick, offTick, note);
	}

	public void addMidiNote(float onTick, float offTick, int note) {
		MidiNote noteToAdd = midiManager.addNote((long) onTick, (long) offTick,
				note, .75f, .5f, .5f);
		midiManager.selectNote(noteToAdd);
		handleMidiCollisions();
		midiManager.mergeTempNotes();
		midiManager.deselectNote(noteToAdd);
	}

	public void handleMidiCollisions() {
		midiManager.clearTempNotes();
		for (MidiNote selected : midiManager.getSelectedNotes()) {
			for (int i = 0; i < midiManager.getMidiNotes().size(); i++) {
				MidiNote note = midiManager.getMidiNote(i);
				if (note == null || selected.equals(note)
						|| selected.getNoteValue() != note.getNoteValue()) {
					continue;
				}
				// if a selected note begins in the middle of another note,
				// clip the covered note
				if (selected.getOnTick() > note.getOnTick()
						&& selected.getOnTick() <= note.getOffTick()) {
					MidiNote copy = note.getCopy();
					copy.setOffTick(selected.getOnTick() - 1);
					// update the native midi events
					midiManager.moveMidiNoteTicks(note.getNoteValue(),
							note.getOnTick(), copy.getOnTick(),
							copy.getOffTick());
					midiManager.putTempNote(i, copy);
					// if the selected note ends after the beginning
					// of the other note, or if the selected note completely
					// covers the other note, delete the covered note
				} else if (!note.isSelected()
						&& selected.getOffTick() >= note.getOnTick()
						&& selected.getOffTick() <= note.getOffTick()
						|| selected.getOnTick() <= note.getOnTick()
						&& selected.getOffTick() >= note.getOffTick()) {
					midiManager.setNoteMute(note.getNoteValue(),
							note.getOnTick(), true);
					midiManager.putTempNote(i, null);
				}
			}
		}
	}

	public boolean toggleSnapToGrid() {
		return bean.toggleSnapToGrid();
	}

	public void toggleLevelsView() {
		if (bean.getViewState() == State.NORMAL_VIEW
				|| bean.getViewState() == State.TO_NORMAL_VIEW) {
			LevelsViewHelper.resetSelected();
			bean.setViewState(State.TO_LEVELS_VIEW);
		} else {
			bean.setViewState(State.TO_NORMAL_VIEW);
		}
	}

	private void dragNotes(boolean dragAllSelected, int pointerId,
			float currTick, int currNote) {
		MidiNote touchedNote = touchedNotes.get(pointerId);
		if (touchedNote == null)
			return;
		int noteDiff = currNote - touchedNote.getNoteValue();
		float tickDiff = currTick - bean.getDragOffsetTick(pointerId)
				- touchedNote.getOnTick();
		if (noteDiff == 0 && tickDiff == 0)
			return;
		tickDiff = getAdjustedTickDiff(tickDiff, pointerId,
				dragAllSelected ? null : touchedNote);
		noteDiff = getAdjustedNoteDiff(noteDiff, dragAllSelected ? null
				: touchedNote);
		List<MidiNote> notesToDrag = dragAllSelected ? midiManager
				.getSelectedNotes() : Arrays.asList(touchedNote);
		// dragging one note - drag all selected notes together
		for (MidiNote midiNote : notesToDrag) {
			midiManager.setNoteTicks(midiNote,
					(long) (midiNote.getOnTick() + tickDiff),
					(long) (midiNote.getOffTick() + tickDiff),
					bean.isSnapToGrid(), true);
			midiManager.setNoteValue(midiNote, midiNote.getNoteValue()
					+ noteDiff);
		}
		bean.setStateChanged(true);
		handleMidiCollisions();
	}

	private void pinchSelectedNotes(float currLeftTick, float currRightTick) {
		MidiNote touchedNote = touchedNotes.values().iterator().next();
		float onTickDiff = currLeftTick - touchedNote.getOnTick()
				- bean.getPinchLeftOffset();
		float offTickDiff = currRightTick - touchedNote.getOffTick()
				+ bean.getPinchRightOffset();
		if (onTickDiff == 0 && offTickDiff == 0)
			return;
		for (MidiNote midiNote : midiManager.getSelectedNotes()) {
			pinchNote(midiNote, onTickDiff, offTickDiff);
		}
		handleMidiCollisions();
	}

	public void updateLoopMarkers(MotionEvent e) {
		for (int i = 0; i < 3; i++) {
			if (bean.getLoopPointerIds()[i] != -1) {
				float x = e
						.getX(e.findPointerIndex(bean.getLoopPointerIds()[i]));
				float majorTick = TickWindowHelper
						.getMajorTickToLeftOf(xToTick(x));
				if (i == 0) { // begin loop marker selected
					midiManager.setLoopBeginTick((long) majorTick);
				} else if (i == 1) { // middle selected. move begin and end
					// preserve current loop length
					float loopLength = midiManager.getLoopEndTick() - midiManager.getLoopBeginTick(); 
					float newBeginTick = TickWindowHelper
							.getMajorTickToLeftOf(xToTick(x
									- bean.getLoopSelectionOffset()));
					float newEndTick = newBeginTick + loopLength;
					if (newBeginTick >= 0
							&& newEndTick <= TickWindowHelper.MAX_TICKS) {
						midiManager.setLoopTicks((long) newBeginTick, (long) newEndTick);
					}
				} else { // end loop marker selected
					midiManager.setLoopEndTick((long) majorTick);
				}
				TickWindowHelper.updateView(midiManager.getLoopBeginTick(),
						midiManager.getLoopEndTick());
			}
		}
	}

	public void noMidiMove(MotionEvent e) {
		if (myPointers.size() - bean.getNumLoopMarkersSelected() == 1) {
			if (bean.isSelectRegion()) { // update select region
				int index = e.findPointerIndex(myPointers.get(0));
				selectRegion(e.getX(index), e.getY(index));
			} else { // one finger scroll
				int index = e.findPointerIndex(bean.getScrollPointerId());
				if (index < e.getPointerCount()) {
					TickWindowHelper.scroll(e.getX(index)
							- MidiViewBean.X_OFFSET);
				}
			}
		} else if (myPointers.size() - bean.getNumLoopMarkersSelected() == 2) {
			// two finger zoom
			float leftX = Math.min(e.getX(0), e.getX(1));
			float rightX = Math.max(e.getX(0), e.getX(1));
			TickWindowHelper.zoom(leftX - MidiViewBean.X_OFFSET, rightX
					- MidiViewBean.X_OFFSET);
		}
	}

	public void writeToBundle(Bundle out) {
		out.putInt("viewState", bean.getViewState().ordinal());
	}

	// use constructor first, and set the deets with this method
	public void readFromBundle(Bundle in) {
		setViewState(State.values()[in.getInt("viewState")]);
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		if (x < MidiTrackControlHelper.width) {
			MidiTrackControlHelper.handlePress(id, x, yToNote(y));
			return;
		}
		myPointers.add(id);
		ScrollBarHelper.startScrollView();
		if (bean.getViewState() == State.LEVELS_VIEW) {
			LevelsViewHelper.selectLevel(x, y, id);
		} else {
			selectMidiNote(x, y, id);
		}
		if (touchedNotes.get(id) == null) {
			// no note selected.
			// check if loop marker selected
			if (yToNote(y) == -1) {
				selectLoopMarker(id, x);
			} else {
				// otherwise, enable scrolling
				bean.setScrollAnchorTick(xToTick(x));
				bean.setScrollPointerId(id);
			}
		}
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		super.handleActionPointerDown(e, id, x, y);
		if (x < MidiTrackControlHelper.width) {
			MidiTrackControlHelper.handlePress(id, x, yToNote(y));
			return;
		}
		myPointers.add(id);
		boolean noteAlreadySelected = false;
		if (bean.getViewState() == State.LEVELS_VIEW) {
			LevelsViewHelper.selectLevel(x, y, id);
		} else {
			noteAlreadySelected = !touchedNotes.isEmpty();
			selectMidiNote(x, y, id);
		}
		if (myPointers.size() > 2)
			return;
		if (touchedNotes.get(id) == null
				|| bean.getViewState() == State.LEVELS_VIEW
				&& LevelsViewHelper.getTouchedLevel(id) == null) {
			if (yToNote(y) == -1) {
				selectLoopMarker(id, x);
			} else {
				float leftTick = xToTick(Math.min(e.getX(0), e.getX(1)));
				float rightTick = xToTick(Math.max(e.getX(0), e.getX(1)));
				if (noteAlreadySelected) {
					// note is selected with one pointer, but this pointer
					// did not select a note. start pinching all selected notes.
					MidiNote touchedNote = touchedNotes.values().iterator()
							.next();
					int leftId = e.getX(e.findPointerIndex(0)) <= e.getX(e
							.findPointerIndex(1)) ? 0 : 1;
					int rightId = (leftId + 1) % 2;
					bean.setPinchLeftPointerId(leftId);
					bean.setPinchRightPointerId(rightId);
					bean.setPinchLeftOffset(leftTick - touchedNote.getOnTick());
					bean.setPinchRightOffset(touchedNote.getOffTick()
							- rightTick);
					bean.setPinch(true);
				} else if (myPointers.size() - bean.getNumLoopMarkersSelected() == 1) {
					// otherwise, enable scrolling
					bean.setScrollAnchorTick(xToTick(x));
					bean.setScrollPointerId(id);
				} else {
					// can never select region with two pointers in midi view
					cancelSelectRegion();
					// init zoom anchors (the same ticks should be under the
					// fingers at all times)
					bean.setZoomLeftAnchorTick(leftTick);
					bean.setZoomRightAnchorTick(rightTick);
				}
			}
		}
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		super.handleActionMove(e);
		MidiTrackControlHelper.handleMove(e);
		if (bean.getViewState() == State.LEVELS_VIEW) {
			LevelsViewHelper.handleActionMove(e);
			return;
		}
		if (bean.isPinch()) {
			float leftTick = xToTick(e.getX(e.findPointerIndex(bean
					.getPinchLeftPointerId())));
			float rightTick = xToTick(e.getX(e.findPointerIndex(bean
					.getPinchRightPointerId())));
			pinchSelectedNotes(leftTick, rightTick);
		} else if (!touchedNotes.isEmpty()) { // at least one midi selected
			if (myPointers.size() - bean.getNumLoopMarkersSelected() == 1) {
				// drag all selected notes together
				dragNotes(true, myPointers.get(0),
						xToTick(e.getX(e.findPointerIndex(myPointers.get(0)))),
						yToNote(e.getY(e.findPointerIndex(myPointers.get(0)))));
			} else {
				// drag each touched note separately
				for (int pointerId : myPointers) {
					dragNotes(false, pointerId, xToTick(e.getX(pointerId)),
							yToNote(e.getY(pointerId)));
				}
			}
			// make room in the view window if we are dragging out of the view
			TickWindowHelper.updateView(midiManager.getLeftMostSelectedTick(),
					midiManager.getRightMostSelectedTick());
		} else { // no midi selected. scroll, zoom, or update select region
			noMidiMove(e);
		}
		updateLoopMarkers(e);
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		if (MidiTrackControlHelper.ownsPointer(id)) {
			MidiTrackControlHelper.handleRelease(id, x, yToNote(y));
			return;
		}
		if (bean.getViewState() == State.LEVELS_VIEW) {
			LevelsViewHelper.handleActionPointerUp(e, id);
		} else {
			touchedNotes.remove(id);
		}
		if (bean.getScrollPointerId() == id)
			bean.setScrollPointerId(-1);
		for (int i = 0; i < 3; i++)
			if (bean.getLoopPointerIds()[i] == id)
				bean.setLoopPointerId(i, -1);
		int index = e.getActionIndex() == 0 ? 1 : 0;
		if (bean.getZoomLeftAnchorTick() != -1) {
			bean.setPinch(false);
			bean.setScrollAnchorTick(xToTick(e.getX(index)));
			bean.setScrollPointerId(e.getPointerId(index));
		}
		myPointers.remove((Object) id);
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
		if (MidiTrackControlHelper.ownsPointer(id)) {
			MidiTrackControlHelper.handleRelease(id, x, yToNote(y));
			MidiTrackControlHelper.clearPointers();
			return;
		}
		ScrollBarHelper.handleActionUp();
		for (int i = 0; i < 3; i++)
			bean.setLoopPointerId(i, -1);
		bean.setSelectRegion(false);
		midiManager.mergeTempNotes();
		if (bean.isStateChanged())
			midiManager.saveState();
		bean.setStateChanged(false);
		if (bean.getViewState() == State.LEVELS_VIEW)
			LevelsViewHelper.clearTouchedLevels();
		else {
			startOnTicks.clear();
			touchedNotes.clear();
		}
		myPointers.clear();
	}

	@Override
	protected void longPress(int id, float x, float y) {
		if (x < MidiTrackControlHelper.width) {
			MidiTrackControlHelper.handleLongPress(id, x, yToNote(y));
			return;
		}
		if (myPointers.size() == 1)
			startSelectRegion(x, y);
	}

	@Override
	protected void singleTap(int id, float x, float y) {
		if (x < MidiTrackControlHelper.width) {
			// MidiTrackControlHelper.handleClick(x, yToNote(y));
			return;
		}
		MidiNote touchedNote = touchedNotes.get(id);
		if (bean.getViewState() == State.LEVELS_VIEW) {
			LevelsViewHelper.selectLevelNote(x, y);
		} else {
			if (touchedNote != null) {
				// single tapping a note always makes it the only selected note
				if (touchedNote.isSelected())
					midiManager.deselectAllNotes();
				midiManager.selectNote(touchedNote);
			} else {
				int note = yToNote(y);
				float tick = xToTick(x);
				// if no note is touched, than this tap deselects all notes
				if (midiManager.anyNoteSelected()) {
					midiManager.deselectAllNotes();
				} else { // add a note based on the current tick granularity
					if (note >= 0 && note < GlobalVars.tracks.size()) {
						addMidiNote(tick, note);
						bean.setStateChanged(true);
					}
				}
			}
		}
	}

	@Override
	protected void doubleTap(int id, float x, float y) {
		MidiNote touchedNote = touchedNotes.get(id);
		if (bean.getViewState() == State.LEVELS_VIEW) {
			LevelsViewHelper.doubleTap();
			return;
		}
		if (touchedNote != null) {
			midiManager.deleteNote(touchedNote);
			bean.setStateChanged(true);
		}
	}
}
