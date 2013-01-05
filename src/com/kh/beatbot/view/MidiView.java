package com.kh.beatbot.view;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.RecordManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.view.helper.MidiTrackControlHelper;
import com.kh.beatbot.view.helper.ScrollBarHelper;
import com.kh.beatbot.view.helper.TickWindowHelper;
import com.kh.beatbot.view.helper.WaveformHelper;

public class MidiView extends ClickableSurfaceView {
	/**************** ATTRIBUTES ***************/
	public static final float COLOR_TRANSITION_RATE = .02f;
	public static float X_OFFSET; // dynamically determined
	public static final float Y_OFFSET = 21;

	public static final float LOOP_SELECT_SNAP_DIST = 30;

	// the size of the "dots" at the top of level display
	public static final int LEVEL_POINT_SIZE = 16;
	// the width of the lines for note levels
	public static final int LEVEL_LINE_WIDTH = 7;

	public static float trackHeight, allTracksHeight;

	public static float dragOffsetTick[] = { 0, 0, 0, 0, 0 };

	public static int pinchLeftPointerId = -1, pinchRightPointerId = -1;
	public static float pinchLeftOffset = 0, pinchRightOffset = 0,
			zoomLeftAnchorTick = 0, zoomRightAnchorTick = 0;

	public static int scrollPointerId = -1;
	public static float scrollAnchorTick = 0, scrollAnchorY = 0;

	private static boolean selectRegion = false;
	private static float selectRegionStartTick = -1, selectRegionStartY = -1;

	// true when a note is being "pinched" (two-fingers touching the note)
	public static boolean pinch = false;

	private static int[] loopPointerIds = { -1, -1, -1 };
	public static float loopSelectionOffset = 0;

	// set this to true after an event that can be undone (with undo btn)
	public static boolean stateChanged = false;

	// this option can be set via a menu item.
	// if true, all midi note movements are rounded to the nearest major tick
	public static boolean snapToGrid = true;

	public float getMidiWidth() {
		return width - X_OFFSET;
	}

	public float getMidiHeight() {
		return height - Y_OFFSET;
	}

	public void setLoopPointerId(int num, int id) {
		if ((id != -1 && loopPointerIds[1] != -1 || num == 1
				&& (loopPointerIds[0] != -1 || loopPointerIds[2] != -1)))
			return; // can't select middle and left or right at the same time
		loopPointerIds[num] = id;
	}

	public int getNumLoopMarkersSelected() {
		int numSelected = 0;
		for (int i = 0; i < 3; i++)
			if (loopPointerIds[i] != -1)
				numSelected++;
		return numSelected;
	}

	public boolean toggleSnapToGrid() {
		snapToGrid = !snapToGrid;
		return snapToGrid;
	}

	/**************** END ATTRIBUTES ****************/

	private MidiManager midiManager;

	private static final int[] V_LINE_WIDTHS = new int[] { 5, 3, 2 };
	private static final float[] V_LINE_COLORS = new float[] { 0, .2f, .3f };

	// NIO Buffers
	private FloatBuffer[] vLineVb = new FloatBuffer[3];
	private FloatBuffer currTickVb = null, hLineVb = null, tickHLineVb = null,
			tickFillVb = null, selectRegionVb = null, loopMarkerVb = null,
			loopMarkerLineVb = null, loopRectVb = null;

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
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		super.surfaceChanged(holder, format, width, height);
		this.width = width;
		this.height = height;
	}

	public static GL10 getGL10() {
		return gl;
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
		float leftTick = Math.min(tick, selectRegionStartTick);
		float rightTick = Math.max(tick, selectRegionStartTick);
		float topY = Math.min(y, selectRegionStartY);
		float bottomY = Math.max(y, selectRegionStartY);
		// make sure select rect doesn't go into the tick view
		topY = Math.max(topY, Y_OFFSET);
		// make sure select rect doesn't go past the last track/note
		bottomY = Math.min(bottomY, Y_OFFSET + allTracksHeight - .01f);
		int topNote = yToNote(topY);
		int bottomNote = yToNote(bottomY);
		midiManager.selectRegion((long) leftTick, (long) rightTick, topNote,
				bottomNote);
		// for normal view, round the drawn rectangle to nearest notes
		topY = noteToY(topNote);
		bottomY = noteToY(bottomNote + 1);
		// make room in the view window if we are dragging out of the view
		TickWindowHelper.updateView(leftTick, rightTick, topY, bottomY);
		initSelectRegionVb(leftTick, rightTick, topY, bottomY);
	}

	public MidiNote getMidiNote(int track, float tick) {
		if (track < 0 || track >= Managers.trackManager.getNumTracks()) {
			return null;
		}
		for (int i = 0; i < midiManager.getMidiNotes().size(); i++) {
			MidiNote midiNote = midiManager.getMidiNotes().get(i);
			if (midiNote.getNoteValue() == track
					&& midiNote.getOnTick() <= tick
					&& midiNote.getOffTick() >= tick) {
				return midiNote;
			}
		}
		return null;
	}

	private void selectMidiNote(float x, float y, int pointerId) {
		int track = yToNote(y);
		float tick = xToTick(x);

		MidiNote selectedNote = getMidiNote(track, tick);
		if (selectedNote == null || touchedNotes.containsValue(selectedNote)) {
			return;
		}

		startOnTicks.put(pointerId, (float) selectedNote.getOnTick());
		float leftOffset = tick - selectedNote.getOnTick();
		dragOffsetTick[pointerId] = leftOffset;
		// don't need right offset for simple drag (one finger
		// select)

		// If this is the only touched midi note, and it hasn't yet
		// been selected, make it the only selected note.
		// If we are multi-selecting, add it to the selected list
		if (!selectedNote.isSelected()) {
			if (touchedNotes.isEmpty())
				midiManager.deselectAllNotes();
			midiManager.selectNote(selectedNote);
		}
		touchedNotes.put(pointerId, selectedNote);
	}

	public void selectLoopMarker(int pointerId, float x) {
		float loopBeginX = tickToX(Managers.midiManager.getLoopBeginTick());
		float loopEndX = tickToX(Managers.midiManager.getLoopEndTick());
		if (Math.abs(x - loopBeginX) <= LOOP_SELECT_SNAP_DIST) {
			loopPointerIds[0] = pointerId;
		} else if (Math.abs(x - loopEndX) <= LOOP_SELECT_SNAP_DIST) {
			loopPointerIds[2] = pointerId;
		} else if (x > loopBeginX && x < loopEndX) {
			loopPointerIds[1] = pointerId;
			loopSelectionOffset = x - loopBeginX;
		}
	}

	private void drawHorizontalLines() {
		translate(0, -TickWindowHelper.getYOffset());
		drawLines(hLineVb, Colors.BLACK, 2, GL10.GL_LINES);
		translate(0, TickWindowHelper.getYOffset());
	}

	private void drawVerticalLines() {
		// distance between one primary tick to the next
		float translateDist = TickWindowHelper.getMajorTickSpacing() * 4f
				* getMidiWidth() / TickWindowHelper.getNumTicks();
		// start at the first primary tick before display start
		float startX = tickToX(TickWindowHelper
				.getPrimaryTickToLeftOf(TickWindowHelper.getTickOffset()));
		// end at the first primary tick after display end
		float endX = tickToX(TickWindowHelper
				.getPrimaryTickToLeftOf(TickWindowHelper.getTickOffset()
						+ TickWindowHelper.getNumTicks()))
				+ translateDist;

		push();
		translate(startX, 0);
		for (int i = 0; i < 3; i++) {
			float color = V_LINE_COLORS[i];
			gl.glColor4f(color, color, color, 1); // appropriate line color
			gl.glLineWidth(V_LINE_WIDTHS[i]); // appropriate line width
			push();
			for (float x = startX; x < endX; x += translateDist) {
				gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vLineVb[i]);
				gl.glDrawArrays(GL10.GL_LINES, 0, 2);
				translate(translateDist, 0);
			}
			pop();
			if (i == 0) {
				translate(translateDist / 2, 0);
			} else if (i == 1) {
				translateDist /= 2;
				translate(-translateDist / 2, 0);
			}
		}
		pop();
	}

	private void drawCurrentTick() {
		float xLoc = tickToX(midiManager.getCurrTick());
		translate(xLoc, 0);
		drawLines(currTickVb, Colors.VOLUME_COLOR, 5, GL10.GL_LINES);
		translate(-xLoc, 0);
	}

	private void drawLoopMarker() {
		float[][] color = new float[2][3];
		color[0] = loopPointerIds[0] != -1 ? Colors.TICK_SELECTED_COLOR
				: Colors.TICK_MARKER_COLOR;
		color[1] = loopPointerIds[2] != -1 ? Colors.TICK_SELECTED_COLOR
				: Colors.TICK_MARKER_COLOR;
		gl.glLineWidth(6);
		float[] loopMarkerLocs = { tickToX(midiManager.getLoopBeginTick()),
				tickToX(midiManager.getLoopEndTick()) };
		for (int i = 0; i < 2; i++) {
			float loopMarkerLoc = loopMarkerLocs[i];
			setColor(color[i]);
			push();
			translate(loopMarkerLoc, 0);
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, loopMarkerVb);
			gl.glDrawArrays(GL10.GL_TRIANGLES, i * 3, 3);
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, loopMarkerLineVb);
			gl.glDrawArrays(GL10.GL_LINES, 0, 2);
			pop();
		}
	}

	private void drawTickFill() {
		drawTriangleStrip(tickFillVb, Colors.TICK_FILL_COLOR);
		drawLoopBar();
		drawLines(tickHLineVb, Colors.BLACK, 2, GL10.GL_LINES);
	}

	private void drawLoopBar() {
		float[] color = loopPointerIds[1] == -1 ? Colors.TICKBAR_COLOR
				: Colors.TICK_SELECTED_COLOR;
		// entire loop bar is selected. draw darker square
		drawRectangle(tickToX(midiManager.getLoopBeginTick()), 0,
				tickToX(midiManager.getLoopEndTick()), Y_OFFSET, color);
	}

	private void drawLoopRect() {
		float gray = Colors.MIDI_VIEW_DEFAULT_BG_COLOR + .2f;
		float[] color = new float[] { gray, gray, gray, 1 };
		drawTriangleStrip(loopRectVb, color);
	}

	private void drawRecordingWaveforms() {
		ArrayList<FloatBuffer> waveformVbs = waveformHelper
				.getCurrentWaveformVbs();
		if (Managers.recordManager.isRecording() && !waveformVbs.isEmpty()) {
			FloatBuffer last = waveformVbs.get(waveformVbs.size() - 1);
			float waveWidth = last.get(last.capacity() - 2);
			float noteWidth = tickToX(Managers.recordManager
					.getRecordCurrTick()
					- Managers.recordManager.getRecordStartTick()
					+ RecordManager.RECORD_LATENCY_TICKS)
					- X_OFFSET;
			push();
			translate(tickToX(Managers.recordManager.getRecordStartTick()), 0);
			// scale drawing so entire waveform exactly fits in the note width
			scale(noteWidth / waveWidth, 1);
			for (int i = 0; i < waveformVbs.size(); i++) {
				drawLines(waveformVbs.get(i), Colors.WAVEFORM_COLOR, 1,
						GL10.GL_LINE_STRIP);
			}
			pop();
		}
	}

	public void initSelectRegionVb(float leftTick, float rightTick, float topY,
			float bottomY) {
		selectRegionVb = makeRectFloatBuffer(tickToX(leftTick), topY,
				tickToX(rightTick), bottomY);
	}

	private void drawSelectRegion() {
		if (!selectRegion || selectRegionVb == null)
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
		float y2 = y1 + trackHeight;
		drawRectangle(x1, y1, x2, y2, color);
		drawRectangleOutline(x1, y1, x2, y2, Colors.BLACK, 4);
	}

	private void initTickFillVb() {
		tickFillVb = makeRectFloatBuffer(X_OFFSET, 0, width, Y_OFFSET);
	}

	private void initLoopRectVb() {
		loopRectVb = makeRectFloatBuffer(
				tickToX(midiManager.getLoopBeginTick()), Y_OFFSET,
				tickToX(midiManager.getLoopEndTick()), height);
	}

	private void initCurrTickVb() {
		float[] vertLine = new float[] { 0, Y_OFFSET, 0, height };
		currTickVb = makeFloatBuffer(vertLine);
	}

	private void initHLineVb() {
		float[] hLines = new float[(Managers.trackManager.getNumTracks() + 1) * 4];
		float[] tickHLines = new float[8];
		tickHLines[0] = X_OFFSET;
		tickHLines[1] = 0;
		tickHLines[2] = width;
		tickHLines[3] = 0;
		tickHLines[4] = X_OFFSET;
		tickHLines[5] = Y_OFFSET;
		tickHLines[6] = width;
		tickHLines[7] = Y_OFFSET;

		float y = Y_OFFSET;
		for (int i = 1; i < Managers.trackManager.getNumTracks() + 1; i++) {
			y += trackHeight;
			hLines[i * 4] = X_OFFSET;
			hLines[i * 4 + 1] = y;
			hLines[i * 4 + 2] = width;
			hLines[i * 4 + 3] = y;
		}
		tickHLineVb = makeFloatBuffer(tickHLines);
		hLineVb = makeFloatBuffer(hLines);
	}

	private void initVLineVbs() {
		// height of the bottom of the record row
		float y1 = Y_OFFSET;

		for (int i = 0; i < 3; i++) {
			// 4 vertices per line
			float[] line = new float[4];
			line[0] = 0;
			line[1] = y1 - y1 / (i + 1.5f);
			line[2] = 0;
			line[3] = height;
			vLineVb[i] = makeFloatBuffer(line);
		}
	}

	private void initLoopMarkerVbs() {
		float h = Y_OFFSET;
		float[] loopMarkerLine = new float[] { 0, 0, 0, height };
		// loop begin triangle, pointing right, and
		// loop end triangle, pointing left
		float[] loopMarkerTriangles = new float[] { 0, 0, 0, h, h, h / 2, 0, 0,
				0, h, -h, h / 2 };
		loopMarkerLineVb = makeFloatBuffer(loopMarkerLine);
		loopMarkerVb = makeFloatBuffer(loopMarkerTriangles);
	}

	public float tickToX(float tick) {
		return (tick - TickWindowHelper.getTickOffset())
				/ TickWindowHelper.getNumTicks() * getMidiWidth() + X_OFFSET;
	}

	public float xToTick(float x) {
		return TickWindowHelper.getNumTicks() * (x - X_OFFSET) / getMidiWidth()
				+ TickWindowHelper.getTickOffset();
	}

	public static int yToNote(float y) {
		if (y >= 0 && y < Y_OFFSET)
			return -1;
		return (int) ((y + TickWindowHelper.getYOffset() - Y_OFFSET) / trackHeight);
	}

	public static float noteToY(int note) {
		return note * trackHeight + Y_OFFSET - TickWindowHelper.getYOffset();
	}

	public void signalRecording() {
		waveformHelper = new WaveformHelper();
		waveformHelper.start();
	}

	public void signalEndRecording() {
		waveformHelper.endRecording();
	}

	public void initAllVbs() {
		initCurrTickVb();
		initHLineVb();
		initVLineVbs();
		initLoopMarkerVbs();
		initLoopRectVb();
		initTickFillVb();
	}

	protected void init() {
		setBackgroundColor(new float[] { Colors.MIDI_VIEW_DEFAULT_BG_COLOR,
				Colors.MIDI_VIEW_DEFAULT_BG_COLOR,
				Colors.MIDI_VIEW_DEFAULT_BG_COLOR, 1 });
		midiManager = Managers.midiManager;
		TickWindowHelper.init(this);
		MidiTrackControlHelper.init(this);
		waveformHelper = new WaveformHelper();
		initAllVbs();
	}

	public void updateTracks() {
		int newTrackIndex = Managers.trackManager.getNumTracks() - 1;
		MidiTrackControlHelper.addTrack(newTrackIndex, Managers.trackManager
				.getTrack(newTrackIndex).getInstrument().getBBIconSource());
		initAllVbs();
	}

	@Override
	protected void drawFrame() {
		boolean recording = Managers.recordManager.getState() != RecordManager.State.INITIALIZING;
		boolean playing = Managers.playbackManager.getState() == PlaybackManager.State.PLAYING;
		TickWindowHelper.scroll();
		// we need to do this in every frame, because even if loop ticks aren't
		// changing the tick window can change
		initLoopRectVb();
		drawLoopRect();
		// if we're recording, keep the current recording tick in view.
		if (recording
				&& midiManager.getCurrTick() > TickWindowHelper.getTickOffset()
						+ TickWindowHelper.getNumTicks())
			TickWindowHelper.setNumTicks(midiManager.getCurrTick()
					- TickWindowHelper.getTickOffset());
		drawHorizontalLines();
		drawVerticalLines();
		drawAllMidiNotes();
		drawTickFill();
		drawSelectRegion();
		drawLoopMarker();
		ScrollBarHelper.drawScrollView(getMidiWidth(), height, X_OFFSET);
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
			if (selectedNote.getOnTick() < -adjustedTickDiff) {
				adjustedTickDiff = -selectedNote.getOnTick();
			} else if (TickWindowHelper.MAX_TICKS - selectedNote.getOffTick() < adjustedTickDiff) {
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
			if (selectedNote.getNoteValue() < -adjustedNoteDiff) {
				adjustedNoteDiff = -selectedNote.getNoteValue();
			} else if (Managers.trackManager.getNumTracks() - 1
					- selectedNote.getNoteValue() < adjustedNoteDiff) {
				adjustedNoteDiff = Managers.trackManager.getNumTracks() - 1
						- selectedNote.getNoteValue();
			}
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
				snapToGrid, false);
		stateChanged = true;
	}

	private void startSelectRegion(float x, float y) {
		selectRegionStartTick = xToTick(x);
		selectRegionStartY = noteToY(yToNote(y));
		selectRegionVb = null;
		selectRegion = true;
	}

	private void cancelSelectRegion() {
		selectRegion = false;
		selectRegionVb = null;
	}

	// adds a note starting at the nearest major tick (nearest displayed
	// grid line) to the left and ending one tick before the nearest major
	// tick to the right of the given tick
	public MidiNote addMidiNote(float tick, int track) {
		float spacing = TickWindowHelper.getMajorTickSpacing();
		float onTick = tick - tick % spacing;
		float offTick = onTick + spacing - 1;
		return addMidiNote(onTick, offTick, track);
	}

	public MidiNote addMidiNote(float onTick, float offTick, int track) {
		MidiNote noteToAdd = midiManager.addNote((long) onTick, (long) offTick,
				track, .75f, .5f, .5f);
		midiManager.selectNote(noteToAdd);
		midiManager.handleMidiCollisions();
		midiManager.mergeTempNotes();
		midiManager.deselectNote(noteToAdd);
		stateChanged = true;
		return noteToAdd;
	}

	private void dragNotes(boolean dragAllSelected, int pointerId,
			float currTick, int currNote) {
		MidiNote touchedNote = touchedNotes.get(pointerId);
		if (touchedNote == null)
			return;
		int noteDiff = currNote - touchedNote.getNoteValue();
		float tickDiff = currTick - dragOffsetTick[pointerId]
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
			midiManager
					.setNoteTicks(midiNote,
							(long) (midiNote.getOnTick() + tickDiff),
							(long) (midiNote.getOffTick() + tickDiff),
							snapToGrid, true);
			midiManager.setNoteValue(midiNote, midiNote.getNoteValue()
					+ noteDiff);
		}
		stateChanged = true;
		midiManager.handleMidiCollisions();
	}

	private void pinchSelectedNotes(float currLeftTick, float currRightTick) {
		MidiNote touchedNote = touchedNotes.values().iterator().next();
		float onTickDiff = currLeftTick - touchedNote.getOnTick()
				- pinchLeftOffset;
		float offTickDiff = currRightTick - touchedNote.getOffTick()
				+ pinchRightOffset;
		if (onTickDiff == 0 && offTickDiff == 0)
			return;
		for (MidiNote midiNote : midiManager.getSelectedNotes()) {
			pinchNote(midiNote, onTickDiff, offTickDiff);
		}
		midiManager.handleMidiCollisions();
	}

	public void updateLoopMarkers(MotionEvent e) {
		for (int i = 0; i < 3; i++) {
			if (loopPointerIds[i] != -1) {
				float x = e.getX(e.findPointerIndex(loopPointerIds[i]));
				float majorTick = TickWindowHelper
						.getMajorTickToLeftOf(xToTick(x));
				if (i == 0) { // begin loop marker selected
					midiManager.setLoopBeginTick(Math.max(0, (long) majorTick));
				} else if (i == 1) { // middle selected. move begin and end
					// preserve current loop length
					float loopLength = midiManager.getLoopEndTick()
							- midiManager.getLoopBeginTick();
					float newBeginTick = TickWindowHelper
							.getMajorTickToLeftOf(xToTick(x
									- loopSelectionOffset));
					float newEndTick = newBeginTick + loopLength;
					if (newBeginTick <= 0) {
						newBeginTick = 0;
						newEndTick = newBeginTick + loopLength;
					} else if (newEndTick >= TickWindowHelper.MAX_TICKS) {
						newEndTick = TickWindowHelper.MAX_TICKS;
						newBeginTick = newEndTick - loopLength;
					}
					midiManager.setLoopTicks((long) newBeginTick,
							(long) newEndTick);
				} else { // end loop marker selected
					midiManager.setLoopEndTick((long) Math.min(
							TickWindowHelper.MAX_TICKS, majorTick));
				}
				Managers.trackManager.updateAllTrackNextNotes();
				TickWindowHelper.updateView(midiManager.getLoopBeginTick(),
						midiManager.getLoopEndTick());
			}
		}
	}

	public void noMidiMove(MotionEvent e) {
		if (myPointers.size() - getNumLoopMarkersSelected() == 1) {
			if (selectRegion) { // update select region
				int index = e.findPointerIndex(myPointers.get(0));
				selectRegion(e.getX(index), e.getY(index));
			} else { // one finger scroll
				int index = e.findPointerIndex(scrollPointerId);
				if (index < e.getPointerCount()) {
					TickWindowHelper.scroll(e.getX(index) - X_OFFSET,
							e.getY(index));
				}
			}
		} else if (myPointers.size() - getNumLoopMarkersSelected() == 2) {
			// two finger zoom
			float leftX = Math.min(e.getX(0), e.getX(1));
			float rightX = Math.max(e.getX(0), e.getX(1));
			TickWindowHelper.zoom(leftX - X_OFFSET, rightX - X_OFFSET);
		}
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		if (x < X_OFFSET) {
			MidiTrackControlHelper.handlePress(id, x, yToNote(y));
			return;
		}
		myPointers.add(id);
		ScrollBarHelper.startScrollView();
		selectMidiNote(x, y, id);
		if (touchedNotes.get(id) == null) {
			// no note selected.
			// check if loop marker selected
			if (yToNote(y) == -1) {
				selectLoopMarker(id, x);
			} else {
				// otherwise, enable scrolling
				scrollAnchorTick = xToTick(x);
				scrollAnchorY = y + TickWindowHelper.getYOffset();
				scrollPointerId = id;
			}
		}
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		super.handleActionPointerDown(e, id, x, y);
		if (x < X_OFFSET) {
			MidiTrackControlHelper.handlePress(id, x, yToNote(y));
			return;
		}
		myPointers.add(id);
		boolean noteAlreadySelected = false;
		noteAlreadySelected = !touchedNotes.isEmpty();
		selectMidiNote(x, y, id);
		if (myPointers.size() > 2)
			return;
		if (touchedNotes.get(id) == null) {
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
					pinchLeftPointerId = leftId;
					pinchRightPointerId = rightId;
					pinchLeftOffset = leftTick - touchedNote.getOnTick();
					pinchRightOffset = touchedNote.getOffTick() - rightTick;
					pinch = true;
				} else if (myPointers.size() - getNumLoopMarkersSelected() == 1) {
					// otherwise, enable scrolling
					scrollAnchorTick = xToTick(x);
					scrollPointerId = id;
				} else {
					// can never select region with two pointers in midi view
					cancelSelectRegion();
					// init zoom anchors (the same ticks should be under the
					// fingers at all times)
					zoomLeftAnchorTick = leftTick;
					zoomRightAnchorTick = rightTick;
				}
			}
		}
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		super.handleActionMove(e);
		MidiTrackControlHelper.handleMove(e);
		if (pinch) {
			int leftIndex = e.findPointerIndex(pinchLeftPointerId);
			int rightIndex = e.findPointerIndex(pinchRightPointerId);
			float leftTick = xToTick(e.getX(leftIndex));
			float rightTick = xToTick(e.getX(rightIndex));
			pinchSelectedNotes(leftTick, rightTick);
		} else if (touchedNotes.isEmpty()) { // no midi selected. scroll, zoom,
												// or update select region
			noMidiMove(e);
		} else { // at least one midi selected
			if (myPointers.size() - getNumLoopMarkersSelected() == 1) {
				// exactly one pointer not dragging loop markers - drag all
				// selected notes together
				dragNotes(true, myPointers.get(0),
						xToTick(e.getX(e.findPointerIndex(myPointers.get(0)))),
						yToNote(e.getY(e.findPointerIndex(myPointers.get(0)))));
			} else { // drag each touched note separately
				for (int pointerId : myPointers) {
					dragNotes(false, pointerId, xToTick(e.getX(pointerId)),
							yToNote(e.getY(pointerId)));
				}
			}
			// make room in the view window if we are dragging out of the view
			TickWindowHelper.updateView(midiManager.getLeftMostSelectedTick(),
					midiManager.getRightMostSelectedTick());
		}
		updateLoopMarkers(e);
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		if (MidiTrackControlHelper.ownsPointer(id)) {
			MidiTrackControlHelper.handleRelease(id, x, yToNote(y));
			return;
		}
		if (scrollPointerId == id)
			scrollPointerId = -1;
		for (int i = 0; i < 3; i++)
			if (loopPointerIds[i] == id)
				loopPointerIds[i] = -1;
		int index = e.getActionIndex() == 0 ? 1 : 0;
		if (zoomLeftAnchorTick != -1) {
			pinch = false;
			scrollAnchorTick = xToTick(e.getX(index));
			scrollPointerId = e.getPointerId(index);
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
			loopPointerIds[i] = -1;
		selectRegion = false;
		midiManager.mergeTempNotes();
		if (stateChanged)
			midiManager.saveState();
		stateChanged = false;
		startOnTicks.clear();
		touchedNotes.clear();
		myPointers.clear();
	}

	@Override
	protected void longPress(int id, float x, float y) {
		if (x < X_OFFSET) {
			MidiTrackControlHelper.handleLongPress(id, x, yToNote(y));
			return;
		}
		if (myPointers.size() == 1)
			startSelectRegion(x, y);
	}

	@Override
	protected void singleTap(int id, float x, float y) {
		if (x < X_OFFSET) {
			// MidiTrackControlHelper.handleClick(x, yToNote(y));
			return;
		}
		MidiNote touchedNote = touchedNotes.get(id);
		if (midiManager.isCopying()) {
			midiManager.paste((long) TickWindowHelper
					.getMajorTickToLeftOf(xToTick(x)));
		} else if (touchedNote != null) {
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
				if (note >= 0 && note < Managers.trackManager.getNumTracks()) {
					addMidiNote(tick, note);
				}
			}
		}
	}

	@Override
	protected void doubleTap(int id, float x, float y) {
		MidiNote touchedNote = touchedNotes.get(id);
		if (touchedNote != null) {
			midiManager.deleteNote(touchedNote);
			stateChanged = true;
		}
	}
}
