package com.kh.beatbot;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLU;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.midi.MidiNote;

public class MidiSurfaceView extends SurfaceViewBase {

	private class TickWindow {
		// leftmost tick to display
		private long tickOffset;
		// number of ticks within the window
		private long numTicks;

		int minLinesDisplayed = 8;
		int maxLinesDisplayed = 32;

		long minTicks = allTicks / 8;
		long maxTicks = allTicks * 10;

		float granularity = 1;

		TickWindow(long tickOffset, long numTicks) {
			this.tickOffset = tickOffset;
			this.numTicks = numTicks - 1;
			updateGranularity();
		}

		public void zoom(float leftX, float rightX) {
			long newTOS = (long) ((zoomRightAnchorTick * leftX - zoomLeftAnchorTick
					* rightX) / (leftX - rightX));
			long newNumTicks = (long) ((zoomLeftAnchorTick - newTOS) * width / leftX);

			if (newTOS < 0 && newTOS + newNumTicks > maxTicks
					|| newNumTicks < minTicks) {
				return;
			}
			if (newTOS >= 0 && newTOS + newNumTicks <= maxTicks) {
				tickOffset = newTOS;
				numTicks = newNumTicks;
			} else if (newTOS < 0) {
				tickOffset = 0;
				numTicks = (long) (zoomRightAnchorTick * width / rightX);
				numTicks = numTicks > maxTicks ? maxTicks : numTicks;
			} else if (newTOS + newNumTicks > maxTicks) {
				numTicks = (long) ((zoomLeftAnchorTick - maxTicks) / (leftX
						/ width - 1));
				tickOffset = maxTicks - numTicks;
			}
			updateGranularity();
		}

		public void scroll() {
			if (!scrolling && scrollVelocity != 0) {
				scrollVelocity *= 0.95;
				setTickOffset((long) (tickOffset + scrollVelocity));
				if (scrollVelocity == 0) {
					scrollViewEndTime = System.currentTimeMillis();
				}
			}
		}

		public long scroll(float x) {
			long newTickOffset = scrollAnchorTick
					- (long) ((numTicks * x) / width);
			long diff = newTickOffset - tickOffset;
			setTickOffset(newTickOffset);
			return diff;
		}

		private void updateGranularity() {
			// x-coord width of one eight note
			float spacing = ((float) allTicks * width) / (numTicks * 8);
			// after algebra, this condition says: if more than maxLines will
			// display, reduce the granularity by one half, else if less than
			// maxLines will display, increase the granularity by one half
			// so that (minLinesDisplayed <= lines-displayed <=
			// maxLinesDisplayed) at all times
			if ((maxLinesDisplayed * spacing) / granularity < width)
				granularity /= 2;
			else if ((minLinesDisplayed * spacing) / granularity > width
					&& granularity < 4)
				granularity *= 2;
		}

		public void setTickOffset(long tickOffset) {
			if (tickOffset + numTicks <= maxTicks && tickOffset >= 0) {
				this.tickOffset = tickOffset;
			} else if (tickOffset < 0)
				this.tickOffset = 0;
			else if (tickOffset + numTicks > maxTicks)
				this.tickOffset = maxTicks - numTicks;
		}

		public boolean setNumTicks(long numTicks) {
			if (numTicks <= maxTicks && numTicks >= minTicks) {
				this.numTicks = numTicks;
				updateGranularity();
				return true;
			}
			return false;
		}

		public long getTickSpacing() {
			return (long) (minTicks / granularity);
		}

		public long getMajorTickToLeftOf(long tick) {
			return tick - tick % getTickSpacing();
		}

		// adds a note starting at the nearest major tick (nearest displayed
		// grid line) to the left and ending one tick before the nearest major
		// tick to the right of the given tick
		// @param tick the tick somewhere in the middle of the desired range
		public MidiNote addMidiNote(long tick, int note) {
			long spacing = (long) (minTicks / granularity);
			long onTick = tick - tick % spacing;
			long offTick = onTick + spacing - 1;
			return midiManager.addNote(onTick, offTick, note);
		}

		public void updateView(long leftTick, long rightTick) {
			// if we are dragging out of view, scroll appropriately
			if (rightTick > tickWindow.tickOffset + tickWindow.numTicks) {
				tickWindow.setTickOffset(rightTick - tickWindow.numTicks);
			} else if (leftTick < tickWindow.tickOffset) {
				tickWindow.setTickOffset(leftTick);
			}
		}
	}

	// options
	boolean snapToGrid = false;

	// NIO Buffers
	FloatBuffer hLineVB = null;
	FloatBuffer vLineVB = null;
	FloatBuffer recordRowVB = null;
	FloatBuffer selectRegionVB = null;
	FloatBuffer loopMarkerVB = null;
	FloatBuffer loopMarkerLineVB = null;

	MidiManager midiManager;
	RecordManager recorder;
	PlaybackManager playbackManager;

	// map of pointerIds to the notes they are selecting
	Map<Integer, MidiNote> touchedNotes = new HashMap<Integer, MidiNote>();
	Map<Integer, Long> startOnTicks = new HashMap<Integer, Long>();
	// all selected notes (to draw in blue, and to drag together)
	List<MidiNote> selectedNotes = new ArrayList<MidiNote>();

	TickWindow tickWindow;

	long dragOffsetTick[] = new long[5];

	long pinchLeftOffsetTick = 0;
	long pinchRightOffsetTick = 0;

	long zoomLeftAnchorTick = 0;
	long zoomRightAnchorTick = 0;

	long scrollAnchorTick = 0;
	long scrollVelocity = 0;

	boolean selectRegion = false;
	long selectRegionStartTick = -1;
	int selectRegionStartNote = -1;

	long currTick = 0;
	long allTicks;

	long lastDownTime = 0;
	long lastTapTime = 0;

	float lastTapX = -10;
	float lastTapY = -10;

	// true when a note is being "pinched" (two-fingers touching the note)
	boolean pinch = false;

	boolean scrolling = false;	
	long scrollViewStartTime = 0;
	long scrollViewEndTime = Long.MAX_VALUE;

	public MidiSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		for (int i = 0; i < 5; i++) {
			dragOffsetTick[i] = 0;
		}
	}

	public void setMidiManager(MidiManager midiManager) {
		this.midiManager = midiManager;
		allTicks = midiManager.RESOLUTION * 4;
	}

	public void setRecorderService(RecordManager recorder) {
		this.recorder = recorder;
	}

	public void setPlaybackManager(PlaybackManager playbackManager) {
		this.playbackManager = playbackManager;
	}

	public float currentBeatDivision() {
		// currently, granularity is relative to an eight note; X2 for quarter
		// note
		return tickWindow.granularity * 2;
	}

	public void reset() {
		tickWindow.setTickOffset(0);
		currTick = 0;
	}

	private void selectWithRegion(float x, float y) {
		long tick = xToTick(x);
		int note = yToNote(y);

		long leftTick = Math.min(tick, selectRegionStartTick);
		long rightTick = Math.max(tick, selectRegionStartTick);
		int topNote = Math.min(note, selectRegionStartNote);
		int bottomNote = Math.max(note, selectRegionStartNote);

		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			// conditions for region selection
			boolean a = leftTick < midiNote.getOffTick();
			boolean b = rightTick > midiNote.getOffTick();
			boolean c = leftTick < midiNote.getOnTick();
			boolean d = rightTick > midiNote.getOnTick();
			boolean noteCondition = topNote <= midiNote.getNote()
					&& bottomNote >= midiNote.getNote();

			if (noteCondition && (a && b || c && d || !b && !c)) {
				if (!selectedNotes.contains(midiNote)) {
					selectedNotes.add(midiNote);
				}
			} else if (selectedNotes.contains(midiNote)) {
				selectedNotes.remove(midiNote);
			}
		}
		// make room in the view window if we are dragging out of the view
		tickWindow.updateView(leftTick, rightTick);
		updateSelectRegionVB(leftTick, rightTick, topNote, bottomNote);
	}

	private void selectMidiNote(float x, float y, int pointerId) {
		long tick = xToTick(x);
		long note = yToNote(y);

		for (int i = 0; i < midiManager.getMidiNotes().size(); i++) {
			MidiNote midiNote = midiManager.getMidiNotes().get(i);
			if (midiNote.getNote() == note && midiNote.getOnTick() <= tick
					&& midiNote.getOffTick() >= tick) {
				if (touchedNotes.containsValue(midiNote)) {
					long leftOffsetTick = tick - midiNote.getOnTick();
					long rightOffsetTick = midiNote.getOffTick() - tick;
					pinchLeftOffsetTick = Math.min(leftOffsetTick,
							pinchLeftOffsetTick);
					pinchRightOffsetTick = Math.min(rightOffsetTick,
							pinchRightOffsetTick);
					pinch = true;
				} else {
					startOnTicks.put(pointerId, midiNote.getOnTick());
					dragOffsetTick[pointerId] = tick - midiNote.getOnTick();
					pinchLeftOffsetTick = dragOffsetTick[pointerId];
					pinchRightOffsetTick = midiNote.getOffTick() - tick;
					// don't need right offset for simple drag (one finger
					// select)

					// If this is the only touched midi note, and it hasn't yet
					// been selected, make it the only selected note.
					// If we are multi-selecting, add it to the selected list
					if (!selectedNotes.contains(midiNote)) {
						if (touchedNotes.isEmpty())
							selectedNotes.clear();
						selectedNotes.add(midiNote);
					}
				}
				touchedNotes.put(pointerId, midiNote);
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
		gl.glColor4f(0, 0, 0, 1);
		gl.glLineWidth(2);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vLineVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, vLineVB.capacity() / 2);
	}

	private void drawCurrentTick() {
		float xLoc = tickToX(currTick);
		float[] vertLine = new float[] { xLoc, width, xLoc, 0 };
		FloatBuffer lineBuff = makeFloatBuffer(vertLine);
		gl.glColor4f(1, 1, 1, 0.5f);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, lineBuff);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);
	}

	private void drawLoopMarker() {
		gl.glPushMatrix();
		gl.glTranslatef(tickToX(midiManager.getLoopTick()), 0, 0);
		gl.glColor4f(0, 0, 0, 1);
		gl.glLineWidth(6);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, loopMarkerVB);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, loopMarkerLineVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);
		gl.glPopMatrix();
	}
	
	private void drawRecordRowFill(boolean recording) {
		gl.glColor4f(.7f, .7f, .7f, 1f);
		if (recording)
			gl.glColor4f(1f, .6f, .6f, .7f);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, recordRowVB);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	}

	private void updateSelectRegionVB(long leftTick, long rightTick,
			int topNote, int bottomNote) {
		float x1 = tickToX(leftTick);
		float x2 = tickToX(rightTick);
		float y1 = (height * topNote) / midiManager.getNumSamples();
		float y2 = (height * (bottomNote + 1)) / midiManager.getNumSamples();
		selectRegionVB = makeFloatBuffer(new float[] { x1, y1, x2, y1, x1, y2,
				x2, y2 });
	}

	private void drawSelectRegion() {
		if (!selectRegion || selectRegionVB == null)
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
			if (midiNote != null)
				drawMidiNote(midiNote.getNote(), midiNote.getOnTick(),
						midiNote.getOffTick(), selectedNotes.contains(midiNote));
		}
		if (midiManager.isRecording()) {
			drawMidiNote(0, midiManager.getLastTick(), currTick, true);
		}
	}

	private void drawMidiNote(int note, long onTick, long offTick,
			boolean selected) {
		// midi note rectangle coordinates
		float x1 = tickToX(onTick);
		float x2 = tickToX(offTick);
		float y1 = (height * note) / midiManager.getNumSamples();
		float y2 = (height * (note + 1)) / midiManager.getNumSamples();
		// the float buffer for the midi note coordinates
		FloatBuffer midiBuf = makeFloatBuffer(new float[] { x1, y1, x2, y1, x1,
				y2, x2, y2 });
		if (selected) // draw blue note if it is currently selected (also will
						// be true if recording)
			gl.glColor4f(0, 0, 1, 1);
		else
			// non-selected, non-recording notes are filled red
			gl.glColor4f(1, 0, 0, 1);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, midiBuf);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

		// draw outline - same coordinates, but different ordering for a
		// line_loop instead of a triangle_strip
		FloatBuffer outlineBuf = makeFloatBuffer(new float[] { x1, y1, x2, y1,
				x2, y2, x1, y2, x1, y2 });
		gl.glColor4f(0, 0, 0, 1);
		gl.glLineWidth(4);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, outlineBuf);
		gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, outlineBuf.capacity() / 2);
	}

	private void initRecordRowVB() {
		float x1 = 0;
		float x2 = width;
		float y1 = 0;
		float y2 = height / midiManager.getNumSamples();
		recordRowVB = makeFloatBuffer(new float[] { x1, y1, x2, y1, x1, y2, x2,
				y2 });
	}

	private void initHLineVB() {
		float[] hLines = new float[(midiManager.getNumSamples() + 1) * 4];
		for (int i = 0; i <= midiManager.getNumSamples(); i++) {
			float yLoc = (height * i) / midiManager.getNumSamples();
			hLines[i * 4] = 0;
			hLines[i * 4 + 1] = yLoc;
			hLines[i * 4 + 2] = width;
			hLines[i * 4 + 3] = yLoc;
		}
		hLineVB = makeFloatBuffer(hLines);
	}

	private void initVLineVB() {
		// 4 vertices per line
		float[] vLines = new float[(tickWindow.maxLinesDisplayed + 1) * 4];
		long tickStart = tickWindow.getMajorTickToLeftOf(tickWindow.tickOffset);
		long tickSpacing = tickWindow.getTickSpacing();
		float y1 = height / midiManager.getNumSamples();
		for (int t = (int) tickStart, i = 0; t < tickWindow.tickOffset
				+ tickWindow.numTicks; t += tickSpacing, i++) {
			float x = tickToX(t);
			vLines[i * 4] = x;
			// the top of the tick line is higher for major ticks
			if (t % (int) (midiManager.RESOLUTION * 2 / tickWindow.granularity) == 0)
				vLines[i * 4 + 1] = y1 - y1 / 2;
			else if (t
					% (int) (midiManager.RESOLUTION / tickWindow.granularity) == 0)
				vLines[i * 4 + 1] = y1 - y1 / 3;
			else
				vLines[i * 4 + 1] = y1 - y1 / 4;
			vLines[i * 4 + 2] = x;
			vLines[i * 4 + 3] = height;
		}
		vLineVB = makeFloatBuffer(vLines);
	}

	private void initLoopMarkerVBs() {
		float h = height/midiManager.getNumSamples();
		float[] loopMarkerLine = new float[] {0, h/2 + 1, 0, height};
		float[] loopMarkerTriangle = new float[] {0, h/2, 0, h, -h/2, 3*h/4};
		
		loopMarkerLineVB = makeFloatBuffer(loopMarkerLine);
		loopMarkerVB = makeFloatBuffer(loopMarkerTriangle);
	}
	
	private float tickToX(long tick) {
		return (float) (tick - tickWindow.tickOffset) / tickWindow.numTicks
				* width;
	}

	private long xToTick(float x) {
		return (long) (tickWindow.numTicks * x / width + tickWindow.tickOffset);
	}

	private int yToNote(float y) {
		return (int) (midiManager.getNumSamples() * y / height);
	}

	private boolean legalSelectedNoteMove(int noteDiff) {
		for (MidiNote midiNote : selectedNotes) {
			if (midiNote.getNote() + noteDiff < 1
					|| midiNote.getNote() + noteDiff >= midiManager
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
		gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
		tickWindow = new TickWindow(0, allTicks);
		initHLineVB();
		initVLineVB();
		initLoopMarkerVBs();
		initRecordRowVB();
	}

	public boolean toggleSnapToGrid() {
		snapToGrid = !snapToGrid;
		return snapToGrid;
	}

	@Override
	protected void drawFrame() {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glViewport(0, 0, width, height);
		gl.glLoadIdentity();
		GLU.gluOrtho2D(gl, 0, width, height, 0);

		boolean recording = recorder.getState() == RecordManager.State.LISTENING
				|| recorder.getState() == RecordManager.State.RECORDING;
		drawRecordRowFill(recording);
		if (recording
				|| playbackManager.getState() == PlaybackManager.State.PLAYING) {
			currTick = midiManager.getCurrTick();
			// if we're recording, keep the current recording tick in view.
			if (recording && currTick > tickWindow.tickOffset + tickWindow.numTicks)
				tickWindow.setNumTicks(currTick - tickWindow.tickOffset);
			drawCurrentTick();
		}
		tickWindow.scroll();
		initVLineVB();
		drawHorizontalLines();
		drawVerticalLines();
		drawLoopMarker();
		drawAllMidiNotes();
		drawSelectRegion();
		if (scrolling || scrollVelocity != 0 || Math.abs(System.currentTimeMillis() - scrollViewEndTime) <= 600) {
			drawScrollView();
		}
	}

	private void drawScrollView() {
		// if scrolling is still in progress, elapsed time is relative to the
		// time of scroll start,
		// otherwise, elapsed time is relative to scroll end time
		boolean scrollingEnded = scrollViewStartTime < scrollViewEndTime;
		long elapsedTime = scrollingEnded ? System.currentTimeMillis()
				- scrollViewEndTime : System.currentTimeMillis()
				- scrollViewStartTime;

		float alpha = .8f;
		if (!scrollingEnded && elapsedTime <= 300)
			alpha *= elapsedTime / 300f;
		else if (scrollingEnded && elapsedTime > 300)
			alpha *= (600 - elapsedTime) / 300f;

		gl.glColor4f(1, 1, 1, alpha);

		float x1 = tickWindow.tickOffset * width / tickWindow.maxTicks;
		float x2 = (tickWindow.tickOffset + tickWindow.numTicks) * width
				/ tickWindow.maxTicks;
		// the float buffer for the midi note coordinates
		FloatBuffer scrollBuf = makeFloatBuffer(new float[] { x1, height - 20,
				x2, height - 20, x1, height, x2, height });
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
		// drag only if the note is moved a certain distance from its start tick
		// this prevents minute finger movements from moving/quantizing notes
		// and also indicates a "sticking point" of the initial note position
		if (startOnTicks.containsKey(pointerId)
				&& Math.abs(startOnTicks.get(pointerId) - midiNote.getOnTick())
						+ Math.abs(tickDiff) > 15) {
			midiNote.setOnTick(midiNote.getOnTick() + tickDiff);
			midiNote.setOffTick(midiNote.getOffTick() + tickDiff);
			if (snapToGrid) // quantize if snapToGrid mode is on
				midiManager.quantize(midiNote, currentBeatDivision());
		}
		return midiNote.getOnTick() - beforeTick;
	}

	private void pinchNote(MidiNote midiNote, long onTickDiff, long offTickDiff) {
		if (snapToGrid) {
			long minDiff = tickWindow.getTickSpacing() / 2;
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
			if (midiNote.getOffTick() + offTickDiff <= tickWindow.maxTicks)
				midiNote.setOffTick(midiNote.getOffTick() + offTickDiff);
		}
	}

	private void handleActionUp(MotionEvent e) {
		long time = System.currentTimeMillis();
		if (time - lastDownTime < 200) {
			// if the second tap is not in the same location as the first tap,
			// no double tap :(
			if (time - lastTapTime < 300 && Math.abs(e.getX() - lastTapX) <= 25 && yToNote(e.getY()) == yToNote(lastTapY)) {
				doubleTap(e, touchedNotes.get(e.getPointerId(0)));
			} else {
				singleTap(e, touchedNotes.get(e.getPointerId(0)));
			}
		}
	}

	private void singleTap(MotionEvent e, MidiNote touchedNote) {
		lastTapX = e.getX();
		lastTapY = e.getY();
		lastTapTime = System.currentTimeMillis();
		selectedNotes.clear();
		if (touchedNote != null) {
			selectedNotes.add(touchedNote);
		}
	}

	private void doubleTap(MotionEvent e, MidiNote touchedNote) {
		long tick = xToTick(e.getX());
		int note = yToNote(e.getY());
		if (touchedNote != null) {
			if (selectedNotes.contains(touchedNote)) {
				selectedNotes.remove(touchedNote);
			}
			midiManager.removeNote(touchedNote);
		} else {
			// add a note based on the current tick granularity
			if (note > 0) {// can't add note on record track
				MidiNote noteToAdd = tickWindow.addMidiNote(tick, note);
				selectedNotes.add(noteToAdd);
				handleMidiCollisions();
				selectedNotes.remove(noteToAdd);
			}
		}
		// reset tap time so that a third tap doesn't register as
		// another double tap
		lastTapTime = 0;
	}

	private void handleMidiCollisions() {
		midiManager.getTempNotes().clear();
		for (MidiNote selected : selectedNotes) {
			for (int i = 0; i < midiManager.getMidiNotes().size(); i++) {
				MidiNote note = midiManager.getMidiNotes().get(i);
				if (selected.equals(note))
					continue;
				if (selected.getNote() == note.getNote()) {
					// if a selected note begins in the middle of another note,
					// clip the covered note
					if (selected.getOnTick() > note.getOnTick()
							&& selected.getOnTick() <= note.getOffTick()) {
						MidiNote copy = note.getCopy();
						copy.setOffTick(selected.getOnTick() - 1);
						midiManager.getTempNotes().put(i, copy);
						// if the selected note ends after the beginning
						// of the other note, or if the selected note completely covers
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

	private void startScrollView() {
		long now = System.currentTimeMillis();
		if (now - scrollViewEndTime > 600)
			scrollViewStartTime = now;
		else
			scrollViewEndTime = Long.MAX_VALUE;
		scrolling = true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			startScrollView();
			int id = e.getPointerId(0);
			lastDownTime = System.currentTimeMillis();
			selectMidiNote(e.getX(0), e.getY(0), id);
			if (touchedNotes.get(id) == null) {
				long tick = xToTick(e.getX(0));
				int note = yToNote(e.getY(0));
				// no note selected. if this is a double-tap-hold (if there was
				// a recent single tap),
				// start a selection region. otherwise, enable scrolling
				if (System.currentTimeMillis() - lastTapTime < 300
						&& Math.abs(lastTapX - e.getX(0)) < 20
						&& note == yToNote(lastTapY)) {
					selectRegionStartTick = tick;
					selectRegionStartNote = note;
					selectRegionVB = null;
					selectRegion = true;
				} else {
					scrollAnchorTick = tick;
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			// lastDownTime = System.currentTimeMillis();
			int index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			selectMidiNote(e.getX(index), e.getY(index), e.getPointerId(index));
			if (touchedNotes.isEmpty() && e.getPointerCount() == 2) {
				// init zoom anchors (the same ticks should be under the fingers
				// at all times)
				float leftAnchorX = Math.min(e.getX(0), e.getX(1));
				float rightAnchorX = Math.max(e.getX(0), e.getX(1));
				zoomLeftAnchorTick = xToTick(leftAnchorX);
				zoomRightAnchorTick = xToTick(rightAnchorX);
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			touchedNotes.remove(e.getPointerId(index));
			index = index == 0 ? 1 : 0;
			if (e.getPointerCount() == 2) {
				long tick = xToTick(e.getX(index));
				if (pinch) {
					id = e.getPointerId(index);
					MidiNote touched = touchedNotes.get(id);
					dragOffsetTick[id] = tick - touched.getOnTick();
					pinch = false;
				} else {
					scrollAnchorTick = tick;
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (pinch) { // two touching one note
				id = e.getPointerId(0);
				MidiNote selectedNote = touchedNotes.get(id);
				float leftX = Math.min(e.getX(0), e.getX(1));
				float rightX = Math.max(e.getX(0), e.getX(1));
				long onTickDiff = xToTick(leftX) - pinchLeftOffsetTick
						- selectedNote.getOnTick();
				long offTickDiff = xToTick(rightX) + pinchRightOffsetTick
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
					long newOnTick = xToTick(e.getX(i)) - dragOffsetTick[id];
					int noteDiff = newNote - touchedNote.getNote();
					long tickDiff = newOnTick - touchedNote.getOnTick();
					if (e.getPointerCount() == 1) {
						// dragging one note - drag all
						// selected notes together
						boolean moveNote = legalSelectedNoteMove(noteDiff);

						if (leftMost.getOnTick() + tickDiff < 0)
							tickDiff = -leftMost.getOnTick();
						else if (rightMost.getOffTick() + tickDiff > tickWindow.maxTicks)
							tickDiff = tickWindow.maxTicks
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
								midiNote.setNote(midiNote.getNote() + noteDiff);
						}
					} else {
						// dragging more than one note - drag each note
						// individually
						dragNote(id, touchedNote, tickDiff);
						if (newNote > 0) // can't drag to record row (note 0)
							touchedNote.setNote(newNote);
					}

					// make room in the view window if we are dragging out of
					// the view
					tickWindow.updateView(leftMost.getOnTick(),
							rightMost.getOffTick());
				}
				// handle any overlapping notes (clip or delete notes as
				// appropriate)
				handleMidiCollisions();
			} else { // no midi selected. scroll, zoom, or update select region
				if (e.getPointerCount() == 1) {
					if (selectRegion) { // update select region
						selectWithRegion(e.getX(0), e.getY(0));
					} else { // one finger scroll
						scrollVelocity = tickWindow.scroll(e.getX());
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
			scrolling = false;
			if (scrollVelocity == 0)
				scrollViewEndTime = System.currentTimeMillis();
			selectRegion = false;
			handleActionUp(e);
			midiManager.mergeTempNotes();
			touchedNotes.clear();
			startOnTicks.clear();
			break;
		}
		return true;
	}
}