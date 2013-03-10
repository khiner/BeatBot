package com.kh.beatbot.view;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.view.helper.ScrollBarHelper;
import com.kh.beatbot.view.helper.TickWindowHelper;
import com.kh.beatbot.view.window.ClickableViewWindow;

public class MidiView extends ClickableViewWindow {

	/**************** ATTRIBUTES ***************/
	public static final float Y_OFFSET = 21;

	public static final float LOOP_SELECT_SNAP_DIST = 30;

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

	public MidiView(TouchableSurfaceView parent) {
		super(parent);
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

	private MidiManager midiManager;

	private FloatBuffer bgVb = null, currTickVb = null, hLineVb = null,
			tickHLineVb = null, tickFillVb = null, selectRegionVb = null,
			loopRectVb = null, loopBarVb;

	private FloatBuffer[] loopMarkerVb = new FloatBuffer[2]; // loop triangle
																// markers
	private FloatBuffer[] loopMarkerLineVb = new FloatBuffer[2]; // vertical
																	// line loop
																	// markers

	// map of pointerIds to the notes they are selecting
	private Map<Integer, MidiNote> touchedNotes = new HashMap<Integer, MidiNote>();

	// map of pointerIds to the original on-ticks of the notes they are touching
	// (before dragging)
	private Map<Integer, Float> startOnTicks = new HashMap<Integer, Float>();

	private List<Integer> myPointers = new ArrayList<Integer>();

	public enum State {
		LEVELS_VIEW, NORMAL_VIEW, TO_LEVELS_VIEW, TO_NORMAL_VIEW
	};

	public void reset() {
		TickWindowHelper.setTickOffset(0);
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
		TickWindowHelper.updateView(tick, topY, bottomY);
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
		if (loopPointerIds[1] != -1)
			return; // middle loop marker already being dragged
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

	private void drawCurrentTick() {
		float xLoc = tickToX(midiManager.getCurrTick());
		translate(xLoc, 0);
		drawLines(currTickVb, Colors.VOLUME, 5, GL10.GL_LINES);
		translate(-xLoc, 0);
	}

	private void drawLoopMarker() {
		for (int i = 0; i < 2; i++) {
			float[] color = loopPointerIds[i * 2] == -1 ? Colors.TICK_MARKER
					: Colors.TICK_SELECTED;
			// drawTriangleStrip(loopMarkerVb[i], color);
			drawLines(loopMarkerLineVb[i], color, 6, GL10.GL_LINES);
		}
	}

	private void drawTickFill() {
		drawTriangleFan(tickFillVb, Colors.TICK_FILL);
		drawLoopBar();
		drawLines(tickHLineVb, Colors.BLACK, 2, GL10.GL_LINES);
	}

	private void drawLoopBar() {
		float[] color = loopPointerIds[1] == -1 ? Colors.TICKBAR
				: Colors.TICK_SELECTED;
		// entire loop bar is selected. draw darker square
		drawTriangleFan(loopBarVb, color);
	}

	private void drawLoopRect() {
		drawTriangleFan(loopRectVb, Colors.MIDI_VIEW_LIGHT_BG);
	}

	private void initBgVb() {
		bgVb = makeRectFloatBuffer(0, 0, width, height);
	}

	private void initSelectRegionVb(float leftTick, float rightTick,
			float topY, float bottomY) {
		selectRegionVb = makeRectFloatBuffer(tickToUnscaledX(leftTick), topY,
				tickToUnscaledX(rightTick), bottomY);
	}

	private void drawSelectRegion() {
		if (!selectRegion || selectRegionVb == null)
			return;
		drawTriangleFan(selectRegionVb, Colors.SELECT_REGION);
	}

	private void drawAllMidiNotes() {
		// not using for-each to avoid concurrent modification
		for (int i = 0; i < midiManager.getMidiNotes().size(); i++) {
			if (midiManager.getMidiNotes().size() <= i)
				break;
			MidiNote midiNote = midiManager.getMidiNote(i);
			if (midiNote != null) {
				drawMidiNote(midiNote,
						midiNote.isSelected() ? Colors.NOTE_SELECTED
								: Colors.NOTE);
			}
		}
	}

	public void drawMidiNote(MidiNote midiNote, float[] color) {
		// fill
		drawTriangleFan(midiNote.getVb(), color);
		// outline
		drawLines(midiNote.getVb(), Colors.BLACK, 4, GL10.GL_LINE_LOOP);
	}

	private void initTickFillVb() {
		tickFillVb = makeRectFloatBuffer(0, 0, width, Y_OFFSET);
	}

	private void initLoopBarVb() {
		loopBarVb = makeRectFloatBuffer(
				tickToUnscaledX(midiManager.getLoopBeginTick()), 0,
				tickToUnscaledX(midiManager.getLoopEndTick()), Y_OFFSET);
	}

	private void initLoopRectVb() {
		loopRectVb = makeRectFloatBuffer(
				tickToUnscaledX(midiManager.getLoopBeginTick()), Y_OFFSET,
				tickToUnscaledX(midiManager.getLoopEndTick()), height);
	}

	private void initCurrTickVb() {
		float[] vertLine = new float[] { 0, Y_OFFSET, 0, height };
		currTickVb = makeFloatBuffer(vertLine);
	}

	private void initHLineVb() {
		float[] hLines = new float[(Managers.trackManager.getNumTracks() + 1) * 4];
		float[] tickHLines = new float[8];
		tickHLines[0] = 0;
		tickHLines[1] = 0;
		tickHLines[2] = width;
		tickHLines[3] = 0;
		tickHLines[4] = 0;
		tickHLines[5] = Y_OFFSET;
		tickHLines[6] = width;
		tickHLines[7] = Y_OFFSET;

		float y = Y_OFFSET;
		for (int i = 1; i < Managers.trackManager.getNumTracks() + 1; i++) {
			y += trackHeight;
			hLines[i * 4] = 0;
			hLines[i * 4 + 1] = y;
			hLines[i * 4 + 2] = width;
			hLines[i * 4 + 3] = y;
		}
		tickHLineVb = makeFloatBuffer(tickHLines);
		hLineVb = makeFloatBuffer(hLines);
	}

	private void initLoopMarkerVbs() {
		float x1 = tickToUnscaledX(midiManager.getLoopBeginTick());
		float x2 = tickToUnscaledX(midiManager.getLoopEndTick());
		float[][] loopMarkerLines = new float[][] { { x1, 0, x1, height },
				{ x2, 0, x2, height } };
		// loop begin triangle, pointing right, and
		// loop end triangle, pointing left
		float[][] loopMarkerTriangles = new float[][] {
				{ x1, 0, x1, Y_OFFSET, x1 + Y_OFFSET, Y_OFFSET / 2 },
				{ x2, 0, x2, Y_OFFSET, x2 - Y_OFFSET, Y_OFFSET / 2 } };
		for (int i = 0; i < 2; i++) {
			loopMarkerLineVb[i] = makeFloatBuffer(loopMarkerLines[i]);
			loopMarkerVb[i] = makeFloatBuffer(loopMarkerTriangles[i]);
		}
	}

	public float tickToUnscaledX(float tick) {
		return tick / TickWindowHelper.MAX_TICKS * width;
	}

	public float tickToX(float tick) {
		return (tick - TickWindowHelper.getTickOffset())
				/ TickWindowHelper.getNumTicks() * width;
	}

	public float xToTick(float x) {
		return TickWindowHelper.getNumTicks() * x / width
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

	public void trackAdded(int trackNum) {
		if (parent.initialized)
			initAllVbs();
	}

	public void initAllVbs() {
		initBgVb();
		initCurrTickVb();
		initHLineVb();
		initLoopMarkerVbs();
		initLoopRectVb();
		initLoopBarVb();
		initTickFillVb();
	}

	protected void loadIcons() {
		// no icons
	}

	public void init() {
		midiManager = Managers.midiManager;
		TickWindowHelper.init(this);
		initAllVbs();
	}

	@Override
	public void draw() {
		TickWindowHelper.scroll(); // take care of any momentum scrolling

		drawTriangleFan(bgVb, Colors.MIDI_VIEW_BG);
		push();
		translate(
				-TickWindowHelper.getTickOffset()
						/ TickWindowHelper.getNumTicks() * width, 0);
		scale((float) TickWindowHelper.MAX_TICKS
				/ (float) TickWindowHelper.getNumTicks(), 1);
		drawLoopRect();
		drawHorizontalLines();
		drawTickFill();
		TickWindowHelper.drawVerticalLines();
		drawAllMidiNotes();
		drawSelectRegion();
		drawLoopMarker();
		pop();
		drawLines(bgVb, Colors.BLACK, 3, GL10.GL_LINE_LOOP);
		ScrollBarHelper.drawScrollView(this);
		if (Managers.playbackManager.getState() == PlaybackManager.State.PLAYING) {
			// if playing, draw curr tick
			drawCurrentTick();
		}
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

	public void updateNoteVb(MidiNote note) {
		note.setVb(makeNoteVb(note));
	}

	private FloatBuffer makeNoteVb(MidiNote note) {
		// midi note rectangle coordinates
		float x1 = tickToUnscaledX(note.getOnTick());
		float y1 = noteToY(note.getNoteValue());
		float x2 = tickToUnscaledX(note.getOffTick());
		float y2 = y1 + trackHeight;
		return makeRectFloatBuffer(x1, y1, x2, y2);
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

	public void updateLoopMarkers() {
		if (loopPointerIds[0] != -1 && loopPointerIds[2] != -1) {
			float leftX = pointerIdToPos.get(loopPointerIds[0]).x;
			float rightX = pointerIdToPos.get(loopPointerIds[2]).x;
			float leftTick = xToTick(leftX);
			float rightTick = xToTick(rightX);
			float leftMajorTick = TickWindowHelper
					.getMajorTickNearestTo(leftTick);
			float rightMajorTick = TickWindowHelper
					.getMajorTickNearestTo(rightTick);
			midiManager.setLoopTicks((long) leftMajorTick,
					(long) rightMajorTick);
			TickWindowHelper.updateView(leftTick, rightTick);
		} else if (loopPointerIds[0] != -1) {
			float leftX = pointerIdToPos.get(loopPointerIds[0]).x;
			float leftTick = xToTick(leftX);
			float leftMajorTick = TickWindowHelper
					.getMajorTickNearestTo(leftTick);
			midiManager.setLoopBeginTick((long) leftMajorTick);
			TickWindowHelper.updateView(leftTick);
		} else if (loopPointerIds[2] != -1) {
			float rightX = pointerIdToPos.get(loopPointerIds[2]).x;
			float rightTick = xToTick(rightX);
			float rightMajorTick = TickWindowHelper
					.getMajorTickNearestTo(rightTick);
			midiManager.setLoopEndTick((long) rightMajorTick);
			TickWindowHelper.updateView(rightTick);
		} else if (loopPointerIds[1] != -1) {
			float x = pointerIdToPos.get(loopPointerIds[1]).x;
			// middle selected. move begin and end
			// preserve current loop length
			float loopLength = midiManager.getLoopEndTick()
					- midiManager.getLoopBeginTick();
			float newBeginTick = TickWindowHelper
					.getMajorTickToLeftOf(xToTick(x - loopSelectionOffset));
			newBeginTick = newBeginTick >= 0 ? (newBeginTick <= TickWindowHelper.MAX_TICKS
					- loopLength ? newBeginTick : TickWindowHelper.MAX_TICKS
					- loopLength)
					: 0;
			midiManager.setLoopTicks((long) newBeginTick,
					(long) (newBeginTick + loopLength));
			TickWindowHelper.updateView(xToTick(x));
		} else {
			return;
		}
		Managers.trackManager.updateAllTrackNextNotes();
		initLoopRectVb();
		initLoopMarkerVbs();
		initLoopBarVb();
	}

	public void noMidiMove() {
		if (myPointers.size() - getNumLoopMarkersSelected() == 1) {
			if (selectRegion) { // update select region
				selectRegion(pointerIdToPos.get(myPointers.get(0)).x,
						pointerIdToPos.get(myPointers.get(0)).y);
			} else { // one finger scroll
				TickWindowHelper.scroll(pointerIdToPos.get(scrollPointerId).x,
						pointerIdToPos.get(scrollPointerId).y);
			}
		} else if (myPointers.size() - getNumLoopMarkersSelected() == 2) {
			// two finger zoom
			float leftX = Math.min(pointerIdToPos.get(myPointers.get(0)).x,
					pointerIdToPos.get(myPointers.get(1)).x);
			float rightX = Math.max(pointerIdToPos.get(myPointers.get(0)).x,
					pointerIdToPos.get(myPointers.get(1)).x);
			TickWindowHelper.zoom(leftX, rightX, zoomLeftAnchorTick,
					zoomRightAnchorTick);
		}
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
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
	protected void handleActionPointerDown(int id, float x, float y) {
		super.handleActionPointerDown(id, x, y);
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
				// TODO might cause problems
				float leftTick = xToTick(Math.min(pointerIdToPos.get(0).x,
						pointerIdToPos.get(1).x));
				float rightTick = xToTick(Math.max(pointerIdToPos.get(0).x,
						pointerIdToPos.get(1).x));
				if (noteAlreadySelected) {
					// note is selected with one pointer, but this pointer
					// did not select a note. start pinching all selected notes.
					MidiNote touchedNote = touchedNotes.values().iterator()
							.next();
					int leftId = pointerIdToPos.get(0).x <= pointerIdToPos
							.get(1).x ? 0 : 1;
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
	protected void handleActionMove(int id, float x, float y) {
		super.handleActionMove(id, x, y);
		if (pinch) {
			float leftTick = xToTick(pointerIdToPos.get(pinchLeftPointerId).x);
			float rightTick = xToTick(pointerIdToPos.get(pinchRightPointerId).x);
			pinchSelectedNotes(leftTick, rightTick);
		} else if (touchedNotes.isEmpty()) {
			// no midi selected. scroll, zoom, or update select region
			noMidiMove();
		} else { // at least one midi selected
			float tick = xToTick(x);
			int note = yToNote(y);
			if (myPointers.size() - getNumLoopMarkersSelected() == 1 && id == 0) {
				// exactly one pointer not dragging loop markers - drag all
				// selected notes together
				dragNotes(true, myPointers.get(0), tick, note);
			} else { // drag each touched note separately
				dragNotes(false, id, tick, note);
			}
			// make room in the view window if we are dragging out of the view
			TickWindowHelper.updateView(tick);
		}
		updateLoopMarkers();
	}

	@Override
	protected void handleActionPointerUp(int id, float x, float y) {
		if (scrollPointerId == id)
			scrollPointerId = -1;
		for (int i = 0; i < 3; i++)
			if (loopPointerIds[i] == id)
				loopPointerIds[i] = -1;
		if (zoomLeftAnchorTick != -1) {
			int otherId = id == 0 ? 1 : 0;
			pinch = false;
			scrollAnchorTick = xToTick(pointerIdToPos.get(otherId).x);
			scrollPointerId = otherId;
		}
		myPointers.remove((Object) id);
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
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
		if (myPointers.size() == 1)
			startSelectRegion(x, y);
	}

	@Override
	protected void singleTap(int id, float x, float y) {
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

	@Override
	protected void createChildren() {
		// leaf child - no children
	}

	@Override
	protected void layoutChildren() {
		// leaf child - no children
	}
}
