package com.kh.beatbot.view;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.view.bean.MidiViewBean;
import com.kh.beatbot.view.helper.LevelsViewHelper;
import com.kh.beatbot.view.helper.TickWindowHelper;
import com.kh.beatbot.view.helper.WaveformHelper;

public class MidiView extends SurfaceViewBase {

	private MidiViewBean bean = new MidiViewBean();

	private static final int[] V_LINE_WIDTHS = new int[] { 5, 3, 2 };
	private static final float[] V_LINE_COLORS = new float[] { 0, .2f, .3f };

	// NIO Buffers
	private FloatBuffer[] vLineVB = new FloatBuffer[3];
	private FloatBuffer hLineVB = null;
	private FloatBuffer tipTickFillVB = null;
	private FloatBuffer selectRegionVB = null;
	private FloatBuffer loopMarkerVB = null;
	private FloatBuffer loopMarkerLineVB = null;

	private MidiManager midiManager;
	private RecordManager recorder;
	private PlaybackManager playbackManager;

	// map of pointerIds to the notes they are selecting
	private Map<Integer, MidiNote> touchedNotes = new HashMap<Integer, MidiNote>();

	// map of pointerIds to the original on-ticks of the notes they are touching
	// (before dragging)
	private Map<Integer, Long> startOnTicks = new HashMap<Integer, Long>();
	// all selected notes (to draw in blue, and to drag together)
	private List<MidiNote> selectedNotes = new ArrayList<MidiNote>();

	public enum State {
		LEVELS_VIEW, NORMAL_VIEW, TO_LEVELS_VIEW, TO_NORMAL_VIEW
	};

	private TickWindowHelper tickWindow;
	private LevelsViewHelper levelsHelper;
	private WaveformHelper waveformHelper;

	public MidiView(Context context, AttributeSet attrs) {
		super(context, attrs);
		bean.setHeight(height);
		bean.setWidth(width);
		for (int i = 0; i < 5; i++) {
			bean.setDragOffsetTick(i, 0);
		}
	}

	public void setMidiManager(MidiManager midiManager) {
		this.midiManager = midiManager;		
		bean.setAllTicks(midiManager.RESOLUTION * 4);
		bean.setYOffset(21);		
		tickWindow = new TickWindowHelper(bean, 0, bean.getAllTicks() - 1);
	}

	public MidiManager getMidiManager() {
		return midiManager;
	}

	public MidiViewBean getBean() {
		return bean;
	}

	public GL10 getGL10() {
		return gl;
	}

	public TickWindowHelper getTickWindow() {
		return tickWindow;
	}

	public void setRecordManager(RecordManager recorder) {
		this.recorder = recorder;
	}

	public void setPlaybackManager(PlaybackManager playbackManager) {
		this.playbackManager = playbackManager;
	}

	public State getViewState() {
		return bean.getViewState();
	}

	public void handleUndo() {
		levelsHelper.resetSelected();
	}

	public void setViewState(State viewState) {
		if (viewState == State.TO_LEVELS_VIEW
				|| viewState == State.TO_NORMAL_VIEW)
			return;
		bean.setViewState(viewState);
		if (viewState == State.LEVELS_VIEW)
			bean.setBgColor(0);
		else
			bean.setBgColor(.5f);
	}

	public void setLevelMode(LevelsViewHelper.LevelMode levelMode) {
		levelsHelper.setLevelMode(levelMode);
	}

	public boolean isNoteSelected(MidiNote midiNote) {
		return selectedNotes.contains(midiNote);
	}

	public void removeSelected(MidiNote midiNote) {
		selectedNotes.remove(midiNote);
	}

	public void reset() {
		tickWindow.setTickOffset(0);
	}

	public void drawWaveform(byte[] bytes) {
		waveformHelper.addBytesToQueue(bytes);
	}
	
	private void selectWithRegion(float x, float y) {
		long tick = xToTick(x);

		long leftTick = Math.min(tick, bean.getSelectRegionStartTick());
		long rightTick = Math.max(tick, bean.getSelectRegionStartTick());
		float topY = Math.min(y, bean.getSelectRegionStartY());
		float bottomY = Math.max(y, bean.getSelectRegionStartY());
		int topNote = yToNote(topY);
		int bottomNote = yToNote(bottomY);
		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			// conditions for region selection
			boolean a = leftTick < midiNote.getOffTick();
			boolean b = rightTick > midiNote.getOffTick();
			boolean c = leftTick < midiNote.getOnTick();
			boolean d = rightTick > midiNote.getOnTick();
			boolean noteCondition = topNote <= midiNote.getNoteValue()
					&& bottomNote >= midiNote.getNoteValue();

			if (noteCondition && (a && b || c && d || !b && !c)) {
				if (!isNoteSelected(midiNote)) {
					selectedNotes.add(midiNote);
				}
			} else
				selectedNotes.remove(midiNote);
		}
		// make room in the view window if we are dragging out of the view
		tickWindow.updateView(leftTick, rightTick);
		updateSelectRegionVB(leftTick, rightTick, noteToY(topNote),
				noteToY(bottomNote + 1));
	}

	private void selectMidiNote(float x, float y, int pointerId) {
		long tick = xToTick(x);
		long note = yToNote(y);

		for (int i = 0; i < midiManager.getMidiNotes().size(); i++) {
			MidiNote midiNote = midiManager.getMidiNotes().get(i);
			if (midiNote.getNoteValue() == note && midiNote.getOnTick() <= tick
					&& midiNote.getOffTick() >= tick) {
				if (touchedNotes.containsValue(midiNote)) {
					long leftOffsetTick = tick - midiNote.getOnTick();
					long rightOffsetTick = midiNote.getOffTick() - tick;
					bean.setPinchLeftOffsetTick(Math.min(leftOffsetTick,
							bean.getPinchLeftOffsetTick()));
					bean.setPinchRightOffsetTick(Math.min(rightOffsetTick,
							bean.getPinchRightOffsetTick()));
					bean.setPinch(true);
				} else {
					startOnTicks.put(pointerId, midiNote.getOnTick());
					long leftOffset = tick - midiNote.getOnTick();
					long rightOffset = midiNote.getOffTick() - tick;
					bean.setDragOffsetTick(pointerId, leftOffset);
					bean.setPinchLeftOffsetTick(leftOffset);
					bean.setPinchRightOffsetTick(rightOffset);
					// don't need right offset for simple drag (one finger
					// select)

					// If this is the only touched midi note, and it hasn't yet
					// been selected, make it the only selected note.
					// If we are multi-selecting, add it to the selected list
					if (!isNoteSelected(midiNote)) {
						if (touchedNotes.isEmpty())
							selectedNotes.clear();
						selectedNotes.add(midiNote);
					}
				}
				touchedNotes.put(pointerId, midiNote);
				return;
			}
		}
	}

	private void drawHorizontalLines() {
		gl.glColor4f(0, 0, 0, 1);
		gl.glLineWidth(2);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, hLineVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, hLineVB.capacity() / 2);
	}

	private void drawVerticalLines() {
		// distance between one primary (LONG) tick to the next
		float translateDist = tickWindow.getMajorTickSpacing() * 4f * width
				/ tickWindow.getNumTicks();
		// start at the first primary tick before display start
		float startX = tickToX(tickWindow.getPrimaryTickToLeftOf(tickWindow
				.getTickOffset()));
		// end at the first primary tick after display end
		float endX = tickToX(tickWindow.getPrimaryTickToLeftOf(tickWindow
				.getTickOffset() + tickWindow.getNumTicks()))
				+ translateDist;

		gl.glPushMatrix();
		gl.glTranslatef(startX, 0, 0);
		for (int i = 0; i < 3; i++) {
			float color = V_LINE_COLORS[i];
			gl.glColor4f(color, color, color, 1); // appropriate line color
			gl.glLineWidth(V_LINE_WIDTHS[i]); // appropriate line width
			gl.glPushMatrix();
			for (float x = startX; x < endX; x += translateDist) {
				gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vLineVB[i]);
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
		float[] vertLine = new float[] { xLoc, width, xLoc, 0 };
		FloatBuffer lineBuff = makeFloatBuffer(vertLine);
		gl.glColor4f(1, 1, 1, 0.5f);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, lineBuff);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);
	}

	private void drawLoopMarker() {
		gl.glPushMatrix();
		gl.glTranslatef(tickToX(midiManager.getLoopTick()), 0, 0);
		gl.glColor4f(.8f, .8f, .8f, 1);
		gl.glLineWidth(6);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, loopMarkerVB);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, loopMarkerLineVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);
		gl.glPopMatrix();
	}

	private void drawTopTickFill() {
		float color = .4f * bean.getBgColor() * 2;
		gl.glColor4f(color, color, color, 1);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, tipTickFillVB);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	}

	private void drawRecordingWaveforms() {
		gl.glLineWidth(1);
		ArrayList<FloatBuffer> waveformVBs = waveformHelper.getWaveformFloatBuffers();
		for (int i = 0; i < waveformVBs.size(); i++) {
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, waveformVBs.get(i));
			gl.glDrawArrays(GL10.GL_LINES, 0, waveformVBs.get(i).capacity() / 2);
		}
	}
	
	public void updateSelectRegionVB(long leftTick, long rightTick, float topY,
			float bottomY) {
		float x1 = tickToX(leftTick);
		float x2 = tickToX(rightTick);
		selectRegionVB = makeFloatBuffer(new float[] { x1, topY, x2, topY, x1,
				bottomY, x2, bottomY });
	}

	private void drawSelectRegion() {
		if (!bean.isSelectRegion() || selectRegionVB == null)
			return;
		gl.glColor4f(.6f, .6f, 1, .7f);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, selectRegionVB);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	}

	private void drawAllMidiNotes() {
		// not using for-each to avoid concurrent modification
		for (int i = 0; i < midiManager.getMidiNotes().size(); i++) {
			if (midiManager.getMidiNotes().size() <= i)
				break;
			MidiNote midiNote = midiManager.getMidiNote(i);
			if (midiNote != null) {
				calculateColor(midiNote);
				drawMidiNote(midiNote.getNoteValue(), midiNote.getOnTick(),
						midiNote.getOffTick());
			}
		}
		if (midiManager.isRecording()) {
			gl.glColor4f(0, 0, 1, 1);
			drawMidiNote(0, midiManager.getLastTick(),
					midiManager.getCurrTick());
		}
	}

	private void calculateColor(MidiNote midiNote) {
		boolean selected = selectedNotes.contains(midiNote);
		if (selected)
			// selected notes are fileed blue
			gl.glColor4f(0, 0, 1, 1);
		else
			// non-selected, non-recording notes are filled red
			gl.glColor4f(1, 0, 0, 1);
	}

	public void drawMidiNote(int note, long onTick, long offTick) {
		// midi note rectangle coordinates
		float x1 = tickToX(onTick);
		float x2 = tickToX(offTick);
		float y1 = noteToY(note);
		float y2 = y1 + bean.getNoteHeight();
		// the float buffer for the midi note coordinates
		FloatBuffer midiBuf = makeFloatBuffer(new float[] { x1, y1, x2, y1, x1,
				y2, x2, y2 });
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, midiBuf);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

		// draw outline - same coordinates, but different ordering for a
		// line_loop instead of a triangle_strip
		FloatBuffer outlineBuf = makeFloatBuffer(new float[] { x1, y1, x2, y1,
				x2, y2, x1, y2, x1, y2 });
		float baseColor = (1 - bean.getBgColor() * 2); // fade outline from
														// black to white
		gl.glColor4f(baseColor, baseColor, baseColor, 1);
		gl.glLineWidth(4);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, outlineBuf);
		gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, outlineBuf.capacity() / 2);
	}

	private void initRecordRowVB() {
		float x1 = 0;
		float x2 = width;
		float y1 = 0;
		float y2 = bean.getYOffset();
		tipTickFillVB = makeFloatBuffer(new float[] { x1, y1, x2, y1, x1, y2,
				x2, y2 });
	}

	private void initHLineVB() {
		float[] hLines = new float[(midiManager.getNumSamples() + 2) * 4];
		hLines[0] = 0;
		hLines[1] = 0;
		hLines[2] = width;
		hLines[3] = 0;
		float y = bean.getYOffset();
		for (int i = 1; i < midiManager.getNumSamples() + 2; i++) {
			hLines[i * 4] = 0;
			hLines[i * 4 + 1] = y;
			hLines[i * 4 + 2] = width;
			hLines[i * 4 + 3] = y;
			y += bean.getNoteHeight();
		}
		hLineVB = makeFloatBuffer(hLines);
	}

	private void initVLineVBs() {
		// height of the bottom of the record row
		float y1 = bean.getYOffset();

		for (int i = 0; i < 3; i++) {
			// 4 vertices per line
			float[] line = new float[4];
			line[0] = 0;
			line[1] = y1 - y1 / (i + 1.5f);
			line[2] = 0;
			line[3] = bean.getHeight();
			vLineVB[i] = makeFloatBuffer(line);
		}
	}

	private void initLoopMarkerVBs() {
		float h = bean.getYOffset();
		float[] loopMarkerLine = new float[] { 0, 0, 0, bean.getHeight() };
		float[] loopMarkerTriangle = new float[] { 0, 0, 0, h, -h, h / 2 };

		loopMarkerLineVB = makeFloatBuffer(loopMarkerLine);
		loopMarkerVB = makeFloatBuffer(loopMarkerTriangle);
	}

	public float tickToX(long tick) {
		return (float) (tick - tickWindow.getTickOffset())
				/ tickWindow.getNumTicks() * bean.getWidth();
	}

	public long xToTick(float x) {
		return (long) (tickWindow.getNumTicks() * x / bean.getWidth() + tickWindow
				.getTickOffset());
	}

	public int yToNote(float y) {
		if (y >= 0 && y < bean.getYOffset())
			return -1;
		return (int) (midiManager.getNumSamples() * (y - bean.getYOffset()) / (bean
				.getHeight() - bean.getYOffset()));
	}

	private float noteToY(int note) {
		return note * bean.getNoteHeight() + bean.getYOffset();
	}

	private boolean legalSelectedNoteMove(int noteDiff) {
		for (MidiNote midiNote : selectedNotes) {
			if (midiNote.getNoteValue() + noteDiff < 0
					|| midiNote.getNoteValue() + noteDiff >= midiManager
							.getNumSamples()) {
				return false;
			}
		}
		return true;
	}

	private MidiNote leftMostSelectedNote() {
		MidiNote leftMostNote = selectedNotes.get(0);
		for (MidiNote midiNote : selectedNotes) {
			if (midiNote.getOnTick() < leftMostNote.getOnTick())
				leftMostNote = midiNote;
		}
		return leftMostNote;
	}

	private MidiNote rightMostSelectedNote() {
		MidiNote rightMostNote = selectedNotes.get(0);
		for (MidiNote midiNote : selectedNotes) {
			if (midiNote.getOffTick() > rightMostNote.getOffTick())
				rightMostNote = midiNote;
		}
		return rightMostNote;
	}

	protected void init() {
		levelsHelper = new LevelsViewHelper(this);
		// waveformHelper constructor: yPos, height
		waveformHelper = new WaveformHelper(bean.getHeight()/3);
		tickWindow.updateGranularity();
		float color = bean.getBgColor();
		gl.glClearColor(color, color, color, 1.0f);
		// draw circles (big points) at top of level bars
		gl.glPointSize(MidiViewBean.LEVEL_POINT_SIZE);
		gl.glEnable(GL10.GL_POINT_SMOOTH);
		initHLineVB();
		initVLineVBs();
		initLoopMarkerVBs();
		initRecordRowVB();
	}

	@Override
	protected void drawFrame() {
		boolean recording = recorder.getState() == RecordManager.State.LISTENING
				|| recorder.getState() == RecordManager.State.RECORDING;
		if (bean.getViewState() == State.TO_LEVELS_VIEW
				|| bean.getViewState() == State.TO_NORMAL_VIEW) { // transitioning
			float amt = .5f / 30;
			bean.setBgColor(bean.getViewState() == State.TO_LEVELS_VIEW ? bean
					.getBgColor() - amt : bean.getBgColor() + amt);
			gl.glClearColor(bean.getBgColor(), bean.getBgColor(),
					bean.getBgColor(), 1);
			if (bean.getBgColor() >= .5f || bean.getBgColor() <= 0) {
				bean.setViewState(bean.getBgColor() >= .5f ? State.NORMAL_VIEW
						: State.LEVELS_VIEW);
			}
		}
		drawTopTickFill();
		if (recording
				|| playbackManager.getState() == PlaybackManager.State.PLAYING) {
			// if we're recording, keep the current recording tick in view.
			if (recording
					&& midiManager.getCurrTick() > tickWindow.getTickOffset()
							+ tickWindow.getNumTicks())
				tickWindow.setNumTicks(midiManager.getCurrTick()
						- tickWindow.getTickOffset());
			drawCurrentTick();
		}
		tickWindow.scroll();
		if (bean.getViewState() != State.LEVELS_VIEW) {
			// normal or transitioning view. draw lines
			drawHorizontalLines();
			drawVerticalLines();
		}
		if (bean.getViewState() != State.NORMAL_VIEW) {
			levelsHelper.drawFrame();
		} else {
			drawLoopMarker();
			drawAllMidiNotes();
		}
		drawSelectRegion();
		if (bean.isScrolling()
				|| bean.getScrollVelocity() != 0
				|| Math.abs(System.currentTimeMillis()
						- bean.getScrollViewEndTime()) <= MidiViewBean.DOUBLE_TAP_TIME * 2) {
			drawScrollView();
		}
		drawRecordingWaveforms();
	}

	private void drawScrollView() {
		// if scrolling is still in progress, elapsed time is relative to the
		// time of scroll start,
		// otherwise, elapsed time is relative to scroll end time
		boolean scrollingEnded = bean.getScrollViewStartTime() < bean
				.getScrollViewEndTime();
		long elapsedTime = scrollingEnded ? System.currentTimeMillis()
				- bean.getScrollViewEndTime() : System.currentTimeMillis()
				- bean.getScrollViewStartTime();

		float alpha = .8f;
		if (!scrollingEnded && elapsedTime <= MidiViewBean.DOUBLE_TAP_TIME)
			alpha *= elapsedTime / (float) MidiViewBean.DOUBLE_TAP_TIME;
		else if (scrollingEnded && elapsedTime > MidiViewBean.DOUBLE_TAP_TIME)
			alpha *= (MidiViewBean.DOUBLE_TAP_TIME * 2 - elapsedTime)
					/ (float) MidiViewBean.DOUBLE_TAP_TIME;

		gl.glColor4f(1, 1, 1, alpha);

		float x1 = tickWindow.getTickOffset() * width
				/ tickWindow.getMaxTicks();
		float x2 = (tickWindow.getTickOffset() + tickWindow.getNumTicks())
				* width / tickWindow.getMaxTicks();
		// the float buffer for the midi note coordinates
		FloatBuffer scrollBuf = makeFloatBuffer(new float[] { x1,
				bean.getHeight() - 20, x2, bean.getHeight() - 20, x1,
				bean.getHeight(), x2, bean.getHeight() });
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, scrollBuf);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	}

	/**
	 * only leftmost-notes are dragged. If one note is being dragged, and there
	 * are other selected notes, all other selected notes should move the
	 * difference in ticks returned by this method
	 */
	private long dragNote(int pointerId, MidiNote midiNote, long tickDiff) {
		long beforeTick = midiNote.getOnTick();
		if (startOnTicks.containsKey(pointerId)) {
			// drag only if the note is moved a certain distance
			// from its start tick this prevents minute finger
			// movements from moving/quantizing notes and also
			// indicates a "sticking point" of the initial note position
			if (Math.abs(startOnTicks.get(pointerId) - midiNote.getOnTick())
					+ Math.abs(tickDiff) > 10) {
				midiNote.setOnTick(midiNote.getOnTick() + tickDiff);
				midiNote.setOffTick(midiNote.getOffTick() + tickDiff);
				if (bean.isSnapToGrid()) // quantize if snapToGrid mode is
											// on
					midiManager.quantize(midiNote,
							tickWindow.getCurrentBeatDivision());
			} else { // inside threshold distance - set to original position
				midiNote.setOffTick(startOnTicks.get(pointerId)
						+ midiNote.getOffTick() - midiNote.getOnTick());
				midiNote.setOnTick(startOnTicks.get(pointerId));
			}
		}
		bean.setStateChanged(true);
		return midiNote.getOnTick() - beforeTick;
	}

	private void pinchNote(MidiNote midiNote, long onTickDiff, long offTickDiff) {
		if (bean.isSnapToGrid()) {
			long minDiff = tickWindow.getMajorTickSpacing() / 2;
			if (Math.abs(onTickDiff) >= minDiff) {
				midiNote.setOnTick(tickWindow.getMajorTickToLeftOf(midiNote
						.getOnTick() + onTickDiff));
			}
			if (Math.abs(offTickDiff) >= minDiff) {
				midiNote.setOffTick(tickWindow.getMajorTickToLeftOf(midiNote
						.getOffTick() + offTickDiff));
			}
		} else {
			midiNote.setOnTick(midiNote.getOnTick() + onTickDiff);
			if (midiNote.getOffTick() + offTickDiff <= tickWindow.getMaxTicks())
				midiNote.setOffTick(midiNote.getOffTick() + offTickDiff);
		}
		bean.setStateChanged(true);
	}

	private void handleActionUp(MotionEvent e) {
		long time = System.currentTimeMillis();
		if (time - bean.getLastDownTime() < 200) {
			// if the second tap is not in the same location as the first tap,
			// no double tap :(
			if (time - bean.getLastTapTime() < MidiViewBean.DOUBLE_TAP_TIME
					&& Math.abs(e.getX() - bean.getLastTapX()) <= 25
					&& yToNote(e.getY()) == yToNote(bean.getLastTapY())) {
				doubleTap(e, touchedNotes.get(e.getPointerId(0)));
			} else {
				singleTap(e, touchedNotes.get(e.getPointerId(0)));
			}
		}
	}

	private void singleTap(MotionEvent e, MidiNote touchedNote) {
		bean.setLastTapX(e.getX());
		bean.setLastTapY(e.getY());
		bean.setLastTapTime(System.currentTimeMillis());
		if (bean.getViewState() == State.LEVELS_VIEW) {
			levelsHelper.selectLevelNote(e.getX(), e.getY());
		} else {
			selectedNotes.clear();
			if (touchedNote != null) {
				selectedNotes.add(touchedNote);
			}
		}
	}

	private void doubleTap(MotionEvent e, MidiNote touchedNote) {
		if (bean.getViewState() == State.LEVELS_VIEW) {
			levelsHelper.doubleTap();
			return;
		}
		long tick = xToTick(e.getX());
		int note = yToNote(e.getY());
		if (touchedNote != null) {
			selectedNotes.remove(touchedNote);
			midiManager.removeNote(touchedNote);
			bean.setStateChanged(true);
		} else {
			// add a note based on the current tick granularity
			if (note >= 0) {
				addMidiNote(tick, note);
				bean.setStateChanged(true);
			}
		}
		// reset tap time so that a third tap doesn't register as
		// another double tap
		bean.setLastTapTime(0);
	}

	// adds a note starting at the nearest major tick (nearest displayed
	// grid line) to the left and ending one tick before the nearest major
	// tick to the right of the given tick
	private void addMidiNote(long tick, int note) {
		long spacing = tickWindow.getMajorTickSpacing();
		long onTick = tick - tick % spacing;
		long offTick = onTick + spacing - 1;
		MidiNote noteToAdd = midiManager.addNote(onTick, offTick, note,
				3 * GlobalVars.LEVEL_MAX / 4, GlobalVars.LEVEL_MAX / 2,
				GlobalVars.LEVEL_MAX / 2);
		selectedNotes.add(noteToAdd);
		handleMidiCollisions();
		selectedNotes.remove(noteToAdd);
	}

	private void handleMidiCollisions() {
		midiManager.getTempNotes().clear();
		for (MidiNote selected : selectedNotes) {
			for (int i = 0; i < midiManager.getMidiNotes().size(); i++) {
				MidiNote note = midiManager.getMidiNotes().get(i);
				if (selected.equals(note))
					continue;
				if (selected.getNoteValue() == note.getNoteValue()) {
					// if a selected note begins in the middle of another note,
					// clip the covered note
					if (selected.getOnTick() > note.getOnTick()
							&& selected.getOnTick() <= note.getOffTick()) {
						MidiNote copy = note.getCopy();
						copy.setOffTick(selected.getOnTick() - 1);
						midiManager.getTempNotes().put(i, copy);
						// if the selected note ends after the beginning
						// of the other note, or if the selected note completely
						// covers
						// the other note, delete the covered note
					} else if (selected.getOffTick() >= note.getOnTick()
							&& selected.getOffTick() <= note.getOffTick()
							|| selected.getOnTick() <= note.getOnTick()
							&& selected.getOffTick() >= note.getOffTick()) {
						midiManager.getTempNotes().put(i, null);
					}
				}
			}
		}
	}

	public float getCurrentBeatDivision() {
		return tickWindow.getCurrentBeatDivision();
	}

	public boolean toggleSnapToGrid() {
		return bean.toggleSnapToGrid();
	}

	private void startScrollView() {
		long now = System.currentTimeMillis();
		if (now - bean.getScrollViewEndTime() > MidiViewBean.DOUBLE_TAP_TIME * 2)
			bean.setScrollViewStartTime(now);
		else
			bean.setScrollViewEndTime(Long.MAX_VALUE);
		bean.setScrolling(true);
	}

	public void toggleLevelsView() {
		if (bean.getViewState() == State.NORMAL_VIEW
				|| bean.getViewState() == State.TO_NORMAL_VIEW) {
			levelsHelper.resetSelected();
			bean.setViewState(State.TO_LEVELS_VIEW);
		} else {
			bean.setViewState(State.TO_NORMAL_VIEW);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		super.surfaceChanged(holder, format, width, height);
		bean.setWidth(width);
		bean.setHeight(height);
		bean.setMidiHeight(bean.getHeight() - bean.getYOffset());
		bean.setNoteHeight(bean.getMidiHeight() / midiManager.getNumSamples());
		bean.setLevelsHeight(bean.getMidiHeight()
				- MidiViewBean.LEVEL_POINT_SIZE);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			bean.setLastDownTime(System.currentTimeMillis());
			startScrollView();
			int id = e.getPointerId(0);
			if (bean.getViewState() == State.LEVELS_VIEW) {
				levelsHelper.selectLevel(e.getX(), e.getY(), id);
			} else {
				selectMidiNote(e.getX(0), e.getY(0), id);
			}
			if (touchedNotes.get(id) == null) {
				long tick = xToTick(e.getX(0));
				int note = yToNote(e.getY(0));
				// no note selected. if this is a double-tap-hold (if there was
				// a recent single tap) start a selection region.
				if (System.currentTimeMillis() - bean.getLastTapTime() < MidiViewBean.DOUBLE_TAP_TIME
						&& Math.abs(bean.getLastTapX() - e.getX(0)) < 20
						&& note == yToNote(bean.getLastTapY())) {
					bean.setSelectRegionStartTick(tick);
					if (bean.getViewState() == State.LEVELS_VIEW)
						bean.setSelectRegionStartY(e.getY(0));
					else
						bean.setSelectRegionStartY(noteToY(note));
					selectRegionVB = null;
					bean.setSelectRegion(true);
				} else {
					// check if loop marker selected
					float loopX = tickToX(midiManager.getLoopTick());
					if (note == -1 && Math.abs(e.getX(0) - loopX) <= 20) {
						bean.setLoopMarkerSelected(true);
					} else
						// otherwise, enable scrolling
						bean.setScrollAnchorTick(tick);
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			// lastDownTime = System.currentTimeMillis();
			int index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if (bean.getViewState() == State.LEVELS_VIEW) {
				return levelsHelper.handleActionPointerDown(e, index);
			}
			selectMidiNote(e.getX(index), e.getY(index), e.getPointerId(index));
			if (touchedNotes.isEmpty() && e.getPointerCount() == 2) {
				// init zoom anchors (the same ticks should be under the fingers
				// at all times)
				float leftAnchorX = Math.min(e.getX(0), e.getX(1));
				float rightAnchorX = Math.max(e.getX(0), e.getX(1));
				bean.setZoomLeftAnchorTick(xToTick(leftAnchorX));
				bean.setZoomRightAnchorTick(xToTick(rightAnchorX));
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if (bean.getViewState() == State.LEVELS_VIEW)
				return levelsHelper.handleActionPointerUp(e, index);
			touchedNotes.remove(e.getPointerId(index));
			index = index == 0 ? 1 : 0;
			if (e.getPointerCount() == 2) {
				long tick = xToTick(e.getX(index));
				if (bean.isPinch()) {
					id = e.getPointerId(index);
					MidiNote touched = touchedNotes.get(id);
					bean.setDragOffsetTick(id, tick - touched.getOnTick());
					bean.setPinch(false);
				} else {
					bean.setScrollAnchorTick(tick);
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (bean.getViewState() == State.LEVELS_VIEW) {
				return levelsHelper.handleActionMove(e);
			}

			if (bean.isPinch()) { // two touching one note
				id = e.getPointerId(0);
				MidiNote selectedNote = touchedNotes.get(id);
				float leftX = Math.min(e.getX(0), e.getX(1));
				float rightX = Math.max(e.getX(0), e.getX(1));
				long onTickDiff = xToTick(leftX)
						- bean.getPinchLeftOffsetTick()
						- selectedNote.getOnTick();
				long offTickDiff = xToTick(rightX)
						+ bean.getPinchRightOffsetTick()
						- selectedNote.getOffTick();
				for (MidiNote midiNote : selectedNotes) {
					pinchNote(midiNote, onTickDiff, offTickDiff);
				}
			} else if (!touchedNotes.isEmpty()) { // at least one midi selected
				MidiNote leftMost = leftMostSelectedNote();
				MidiNote rightMost = rightMostSelectedNote();
				for (int i = 0; i < e.getPointerCount(); i++) {
					id = e.getPointerId(i);
					MidiNote touchedNote = touchedNotes.get(id);
					if (touchedNote == null)
						continue;
					int newNote = yToNote(e.getY(i));
					long newOnTick = xToTick(e.getX(i))
							- bean.getDragOffsetTick(id);
					int noteDiff = newNote - touchedNote.getNoteValue();
					long tickDiff = newOnTick - touchedNote.getOnTick();
					if (e.getPointerCount() == 1) {
						// dragging one note - drag all
						// selected notes together
						boolean moveNote = legalSelectedNoteMove(noteDiff);

						if (leftMost.getOnTick() + tickDiff < 0)
							tickDiff = -leftMost.getOnTick();
						else if (rightMost.getOffTick() + tickDiff > tickWindow
								.getMaxTicks())
							tickDiff = tickWindow.getMaxTicks()
									- rightMost.getOffTick();

						tickDiff = dragNote(id, leftMost, tickDiff);
						for (MidiNote midiNote : selectedNotes) {
							if (!midiNote.equals(leftMost)) {
								midiNote.setOnTick(midiNote.getOnTick()
										+ tickDiff);
								midiNote.setOffTick(midiNote.getOffTick()
										+ tickDiff);
							}
							if (moveNote)
								midiNote.setNote(midiNote.getNoteValue()
										+ noteDiff);
						}
					} else {
						// dragging more than one note - drag each note
						// individually
						dragNote(id, touchedNote, tickDiff);
						touchedNote.setNote(newNote);
					}
					// make room in the view window if we are dragging out of
					// the view
					tickWindow.updateView(leftMost.getOnTick(),
							rightMost.getOffTick());
					// dragOffsetTick[id] += translate;
				}
				// handle any overlapping notes (clip or delete notes as
				// appropriate)
				handleMidiCollisions();
			} else { // no midi selected. scroll, zoom, or update select region
				if (e.getPointerCount() == 1) {
					if (bean.isSelectRegion()) { // update select region
						selectWithRegion(e.getX(0), e.getY(0));
					} else if (bean.isLoopMarkerSelected()) {
						midiManager.setLoopTick(tickWindow
								.getMajorTickToLeftOf(xToTick(e.getX(0))));
					} else { // one finger scroll
						bean.setScrollVelocity(tickWindow.scroll(e.getX(0)));
					}
				} else if (e.getPointerCount() == 2) {
					// two finger zoom
					float leftX = Math.min(e.getX(0), e.getX(1));
					float rightX = Math.max(e.getX(0), e.getX(1));
					tickWindow.zoom(leftX, rightX);
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			bean.setScrolling(false);
			bean.setLoopMarkerSelected(false);
			if (bean.getScrollVelocity() == 0)
				bean.setScrollViewEndTime(System.currentTimeMillis());
			bean.setSelectRegion(false);
			midiManager.mergeTempNotes();
			handleActionUp(e);			
			if (bean.isStateChanged())
				midiManager.saveState();
			bean.setStateChanged(false);
			if (bean.getViewState() == State.LEVELS_VIEW)
				levelsHelper.clearTouchedNotes();
			else {
				startOnTicks.clear();
				touchedNotes.clear();
			}
			break;
		}
		return true;
	}

	public void writeToBundle(Bundle out) {
		ArrayList<Integer> selectedIndices = new ArrayList<Integer>();
		for (int i = 0; i < selectedNotes.size(); i++) {
			int index = midiManager.getMidiNotes()
					.indexOf(selectedNotes.get(i));
			if (index != -1)
				selectedIndices.add(index);
		}
		out.putIntegerArrayList("selectedIndices", selectedIndices);
		ArrayList<Integer> selectedLevelIndices = new ArrayList<Integer>();
		for (int i = 0; i < levelsHelper.getSelectedLevelNotes().size(); i++) {
			int index = midiManager.getMidiNotes().indexOf(
					levelsHelper.getSelectedLevelNotes().get(i));
			if (index != -1)
				selectedLevelIndices.add(index);
		}
		out.putIntegerArrayList("selectedLevelIndices", selectedLevelIndices);
		out.putInt("viewState", bean.getViewState().ordinal());
	}

	// use constructor first, and set the deets with this method
	public void readFromBundle(Bundle in) {
		ArrayList<Integer> selectedIndices = in
				.getIntegerArrayList("selectedIndices");
		for (int index : selectedIndices) {
			selectedNotes.add(midiManager.getMidiNotes().get(index));
		}
		ArrayList<Integer> selectedLevelIndices = in
				.getIntegerArrayList("selectedLevelIndices");
		for (int index : selectedLevelIndices) {
			levelsHelper.getSelectedLevelNotes().add(
					midiManager.getMidiNotes().get(index));
		}
		setViewState(State.values()[in.getInt("viewState")]);
	}
}