package com.kh.beatbot.ui.view;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.Rectangle;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.helper.ScrollBarHelper;
import com.kh.beatbot.ui.view.helper.TickWindowHelper;

public class MidiView extends ClickableView {

	public static final float Y_OFFSET = 21, LOOP_SELECT_SNAP_DIST = 30;
	public static float trackHeight, allTracksHeight;

	private static float dragOffsetTick[] = { 0, 0, 0, 0, 0 };

	private static int pinchLeftPointerId = -1, pinchRightPointerId = -1,
			scrollPointerId = -1;

	private static float pinchLeftOffset = 0, pinchRightOffset = 0,
			zoomLeftAnchorTick = 0, zoomRightAnchorTick = 0,
			loopSelectionOffset = 0, selectRegionStartTick = -1,
			selectRegionStartY = -1;

	private static boolean selectRegion = false;

	private static int[] loopPointerIds = { -1, -1, -1 };

	private static ShapeGroup unselectedRectangles = new ShapeGroup(),
			selectedRectangles = new ShapeGroup(),
			bgShapeGroup = new ShapeGroup(),
			tickBarShapeGroup = new ShapeGroup();

	public float getMidiHeight() {
		return Math.min(height - Y_OFFSET, allTracksHeight);
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

	private FloatBuffer currTickVb = null, hLineVb = null,
			selectRegionVb = null;

	private FloatBuffer[] loopMarkerVb = new FloatBuffer[2]; // loop triangle
																// markers
	private FloatBuffer[] loopMarkerLineVb = new FloatBuffer[2]; // vertical
																	// line loop
																	// markers

	private Rectangle selectedNoteRect = null, bgRect = null, loopRect = null,
			tickBarRect, loopBarRect;

	// map of pointerIds to the notes they are selecting
	private Map<Integer, MidiNote> touchedNotes = new HashMap<Integer, MidiNote>();

	// map of pointerIds to the original on-ticks of the notes they are touching
	// (before dragging)
	private Map<Integer, Float> startOnTicks = new HashMap<Integer, Float>();

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
		topY = Math.max(topY + .01f, Y_OFFSET);
		// make sure select rect doesn't go past the last track/note
		bottomY = Math.min(bottomY + .01f, Y_OFFSET + allTracksHeight - .01f);
		int topNote = yToNote(topY);
		int bottomNote = yToNote(bottomY);
		MidiManager.selectRegion((long) leftTick, (long) rightTick, topNote,
				bottomNote);
		// for normal view, round the drawn rectangle to nearest notes
		topY = noteToY(topNote);
		bottomY = noteToY(bottomNote + 1);
		// make room in the view window if we are dragging out of the view
		TickWindowHelper.updateView(tick, topY, bottomY);
		initSelectRegionVb(leftTick, rightTick, topY, bottomY);
	}

	public MidiNote getMidiNote(int track, float tick) {
		if (track < 0 || track >= TrackManager.getNumTracks()) {
			return null;
		}
		for (int i = 0; i < MidiManager.getMidiNotes().size(); i++) {
			MidiNote midiNote = MidiManager.getMidiNotes().get(i);
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
			if (touchedNotes.isEmpty()) {
				MidiManager.deselectAllNotes();
			}
			MidiManager.selectNote(selectedNote);
		}
		touchedNotes.put(pointerId, selectedNote);
	}

	public void selectLoopMarker(int pointerId, float x) {
		if (loopPointerIds[1] != -1)
			return; // middle loop marker already being dragged
		float loopBeginX = tickToX(MidiManager.getLoopBeginTick());
		float loopEndX = tickToX(MidiManager.getLoopEndTick());
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
		drawLines(hLineVb, Colors.BLACK, 2, GL10.GL_LINES);
	}

	private void drawCurrentTick() {
		float xLoc = tickToX(MidiManager.getCurrTick());
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

	private void updateBgRect() {
		float width = tickToUnscaledX(MidiManager.MAX_TICKS - 1);
		if (bgRect == null) {
			bgRect = makeRectangle(bgShapeGroup, 0, Y_OFFSET, width,
					allTracksHeight, Colors.MIDI_VIEW_BG);
		} else {
			bgRect.update(0, Y_OFFSET, width, allTracksHeight,
					Colors.MIDI_VIEW_BG);
		}
		updateLoopRect();
	}

	private void initSelectedNoteVb(int selectedNote) {
		float x1 = 0;
		float y1 = noteToUnscaledY(selectedNote);
		float width = tickToUnscaledX(MidiManager.MAX_TICKS - 1);
		if (selectedNoteRect == null) {
			selectedNoteRect = makeRectangle(null, x1, y1, width, trackHeight,
					Colors.MIDI_SELECTED_TRACK, Colors.BLACK);
		} else {
			selectedNoteRect.update(x1, y1, width, trackHeight,
					Colors.MIDI_SELECTED_TRACK, Colors.BLACK);
		}
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
		unselectedRectangles.draw(MidiNote.BORDER_WIDTH);
		selectedRectangles.draw(MidiNote.BORDER_WIDTH);
	}

	private void updateTickFillRect() {
		if (tickBarRect == null) {
			tickBarRect = makeRectangle(tickBarShapeGroup, 0, 0, width,
					Y_OFFSET, Colors.TICK_FILL, Colors.BLACK);
		} else {
			tickBarRect.update(0, 0, width, Y_OFFSET, Colors.TICK_FILL,
					Colors.BLACK);
		}
	}

	private void initLoopBarVb() {
		float x = tickToUnscaledX(MidiManager.getLoopBeginTick());
		float width = tickToUnscaledX(MidiManager.getLoopEndTick()) - x;
		float height = Y_OFFSET;
		float[] fillColor = loopPointerIds[1] == -1 ? Colors.TICKBAR
				: Colors.TICK_SELECTED;
		if (loopBarRect == null) {
			loopBarRect = makeRectangle(tickBarShapeGroup, x, 0, width, height,
					fillColor);
		} else {
			loopBarRect.update(x, 0, width, height, fillColor);
		}
	}

	private void updateLoopRect() {
		float x = tickToUnscaledX(MidiManager.getLoopBeginTick());
		float y = Y_OFFSET;
		float width = tickToUnscaledX(MidiManager.getLoopEndTick()) - x;
		if (loopRect == null) {
			loopRect = makeRectangle(bgShapeGroup, x, y, width,
					allTracksHeight, Colors.MIDI_VIEW_LIGHT_BG);
		} else {
			loopRect.update(x, y, width, allTracksHeight,
					Colors.MIDI_VIEW_LIGHT_BG);
		}
	}

	private void initCurrTickVb() {
		float[] vertLine = new float[] { 0, Y_OFFSET, 0,
				Y_OFFSET + allTracksHeight };
		currTickVb = makeFloatBuffer(vertLine);
	}

	private void initHLineVb() {
		float[] hLines = new float[(TrackManager.getNumTracks()) * 4];

		float y = Y_OFFSET;
		for (int i = 1; i < TrackManager.getNumTracks(); i++) {
			y += trackHeight;
			hLines[i * 4] = 0;
			hLines[i * 4 + 1] = y;
			hLines[i * 4 + 2] = width;
			hLines[i * 4 + 3] = y;
		}
		hLineVb = makeFloatBuffer(hLines);
	}

	private void initLoopMarkerVbs() {
		float x1 = tickToUnscaledX(MidiManager.getLoopBeginTick());
		float x2 = tickToUnscaledX(MidiManager.getLoopEndTick());
		float[][] loopMarkerLines = new float[][] {
				{ x1, 0, x1, Y_OFFSET + allTracksHeight },
				{ x2, 0, x2, Y_OFFSET + allTracksHeight } };
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
		return tick / MidiManager.MAX_TICKS * width;
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
		float f = ((y + TickWindowHelper.getYOffset() - Y_OFFSET) / trackHeight);
		return f < 0 ? -1 : (int) f;
	}

	public static float noteToY(int note) {
		return note * trackHeight + Y_OFFSET - TickWindowHelper.getYOffset();
	}

	public static float noteToUnscaledY(int note) {
		return note * trackHeight + Y_OFFSET;
	}

	public void notifyTrackCreated(Track track) {
		allTracksHeight += trackHeight;
		if (initialized) {
			initAllVbs();
			TickWindowHelper.setYOffset(Float.MAX_VALUE);
			for (MidiNote note : track.getMidiNotes()) {
				createNoteView(note);
			}
		}
	}

	public void notifyTrackDeleted(Track track) {
		allTracksHeight -= trackHeight;
		initAllVbs();
		TickWindowHelper.setYOffset(TickWindowHelper.getYOffset());
		for (MidiNote note : track.getMidiNotes()) {
			note.getRectangle().getGroup().remove(note.getRectangle());
		}
		updateNoteRects();
	}

	public void notifyTrackChanged(int newTrackNum) {
		initSelectedNoteVb(newTrackNum);
	}

	public void initAllVbs() {
		updateBgRect();
		initCurrTickVb();
		initHLineVb();
		initLoopMarkerVbs();
		updateTickFillRect();
		initLoopBarVb();
		TickWindowHelper.initVLineVbs();
	}

	protected void loadIcons() {
		// no icons
	}

	public void init() {
		TickWindowHelper.init(this);
		initAllVbs();
	}

	@Override
	public void draw() {
		TickWindowHelper.scroll(); // take care of any momentum scrolling

		push();
		translate(
				-TickWindowHelper.getTickOffset()
						/ TickWindowHelper.getNumTicks() * width, 0);
		scale((float) MidiManager.MAX_TICKS
				/ (float) TickWindowHelper.getNumTicks(), 1);

		// draws background and loop-background in one call
		bgShapeGroup.draw(-1);

		push();

		push();
		translate(0, -TickWindowHelper.getYOffset());
		selectedNoteRect.draw();
		pop();

		TickWindowHelper.drawVerticalLines();
		translate(0, -TickWindowHelper.getYOffset());
		drawHorizontalLines();
		drawAllMidiNotes();
		pop();

		// draws tick rect and loop rect in one call
		tickBarShapeGroup.draw(2);

		drawSelectRegion();
		drawLoopMarker();
		pop();
		// ScrollBarHelper.drawScrollView(this);
		if (PlaybackManager.getState() == PlaybackManager.State.PLAYING) {
			// if playing, draw curr tick
			drawCurrentTick();
		}
	}

	private float getAdjustedTickDiff(float tickDiff, int pointerId,
			MidiNote singleNote) {
		if (tickDiff == 0)
			return 0;
		float adjustedTickDiff = tickDiff;
		for (MidiNote selectedNote : MidiManager.getSelectedNotes()) {
			if (singleNote != null && !selectedNote.equals(singleNote))
				continue;
			if (Math.abs(startOnTicks.get(pointerId) - selectedNote.getOnTick())
					+ Math.abs(tickDiff) <= 10) {
				// inside threshold distance - set to original position
				return startOnTicks.get(pointerId) - selectedNote.getOnTick();
			}
			if (selectedNote.getOnTick() < -adjustedTickDiff) {
				adjustedTickDiff = -selectedNote.getOnTick();
			} else if (MidiManager.MAX_TICKS - selectedNote.getOffTick() < adjustedTickDiff) {
				adjustedTickDiff = MidiManager.MAX_TICKS
						- selectedNote.getOffTick();
			}
		}
		return adjustedTickDiff;
	}

	private int getAdjustedNoteDiff(int noteDiff, MidiNote singleNote) {
		int adjustedNoteDiff = noteDiff;
		for (MidiNote selectedNote : MidiManager.getSelectedNotes()) {
			if (singleNote != null && !selectedNote.equals(singleNote))
				continue;
			if (selectedNote.getNoteValue() < -adjustedNoteDiff) {
				adjustedNoteDiff = -selectedNote.getNoteValue();
			} else if (TrackManager.getNumTracks() - 1
					- selectedNote.getNoteValue() < adjustedNoteDiff) {
				adjustedNoteDiff = TrackManager.getNumTracks() - 1
						- selectedNote.getNoteValue();
			}
		}
		return adjustedNoteDiff;
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
	private MidiNote addMidiNote(float tick, int track) {
		float spacing = TickWindowHelper.getMajorTickSpacing();
		float onTick = tick - tick % spacing;
		float offTick = onTick + spacing - 1;
		return MidiManager.addNote((long) onTick, (long) offTick, track, .75f,
				.5f, .5f);
	}

	public void createNoteView(MidiNote note) {
		Rectangle noteRect = makeNoteRectangle(note);
		note.setRectangle(noteRect);
	}

	private Rectangle makeNoteRectangle(MidiNote note) {
		float x1 = tickToUnscaledX(note.getOnTick());
		float y1 = noteToUnscaledY(note.getNoteValue());
		float width = tickToUnscaledX(note.getOffTick()) - x1;

		return makeRectangle(note.isSelected() ? selectedRectangles
				: unselectedRectangles, x1, y1, width, trackHeight,
				whichColor(note), Colors.BLACK);
	}

	private Rectangle makeRectangle(ShapeGroup group, float x1, float y1,
			float width, float height, float[] fillColor) {
		return makeRectangle(group, x1, y1, width, height, fillColor, null);
	}

	private Rectangle makeRectangle(ShapeGroup group, float x1, float y1,
			float width, float height, float[] fillColor, float[] outlineColor) {
		Rectangle newRect = outlineColor == null ? new Rectangle(group,
				fillColor) : new Rectangle(group, fillColor, outlineColor);
		newRect.getGroup().add(newRect);
		newRect.layout(x1, y1, width, height);
		return newRect;
	}

	public void updateNoteView(MidiNote note) {
		// note coords
		float x1 = tickToUnscaledX(note.getOnTick());
		float y1 = noteToUnscaledY(note.getNoteValue());
		float width = tickToUnscaledX(note.getOffTick()) - x1;

		float[] color = note.isSelected() ? Colors.NOTE_SELECTED : Colors.NOTE;

		if (note.getRectangle() != null) {
			note.getRectangle().update(x1, y1, width, trackHeight, color,
					Colors.BLACK);
		}
	}

	public void updateNoteRects() {
		// unselectedRectangles.clear();
		// selectedRectangles.clear();
		for (int i = 0; i < TrackManager.getNumTracks(); i++) {
			Track track = TrackManager.getTrack(i);
			for (MidiNote note : track.getMidiNotes()) {
				updateNoteView(note);
			}
		}
	}

	public void updateNoteFillColor(MidiNote note) {
		note.getRectangle().setColors(whichColor(note), Colors.BLACK);
		note.getRectangle().setGroup(whichRectangleGroup(note));
	}

	private static float[] whichColor(MidiNote note) {
		return note.isSelected() ? Colors.NOTE_SELECTED : Colors.NOTE;
	}

	private static ShapeGroup whichRectangleGroup(MidiNote note) {
		return note.isSelected() ? selectedRectangles : unselectedRectangles;
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
		List<MidiNote> notesToDrag = dragAllSelected ? MidiManager
				.getSelectedNotes() : Arrays.asList(touchedNote);

		MidiManager.moveNotes(notesToDrag, (long) tickDiff, noteDiff);
	}

	private void pinchSelectedNotes(float onTickDiff, float offTickDiff) {
		if (onTickDiff == 0 && offTickDiff == 0)
			return;
		MidiManager.pinchNotes(MidiManager.getSelectedNotes(),
				(long) onTickDiff, (long) offTickDiff);
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
			MidiManager.setLoopTicks((long) leftMajorTick,
					(long) rightMajorTick);
			TickWindowHelper.updateView(leftTick, rightTick);
		} else if (loopPointerIds[0] != -1) {
			float leftX = pointerIdToPos.get(loopPointerIds[0]).x;
			float leftTick = xToTick(leftX);
			float leftMajorTick = TickWindowHelper
					.getMajorTickNearestTo(leftTick);
			MidiManager.setLoopBeginTick((long) leftMajorTick);
			TickWindowHelper.updateView(leftTick);
		} else if (loopPointerIds[2] != -1) {
			float rightX = pointerIdToPos.get(loopPointerIds[2]).x;
			float rightTick = xToTick(rightX);
			float rightMajorTick = TickWindowHelper
					.getMajorTickNearestTo(rightTick);
			MidiManager.setLoopEndTick((long) rightMajorTick);
			TickWindowHelper.updateView(rightTick);
		} else if (loopPointerIds[1] != -1) {
			float x = pointerIdToPos.get(loopPointerIds[1]).x;
			// middle selected. move begin and end
			// preserve current loop length
			float loopLength = MidiManager.getLoopEndTick()
					- MidiManager.getLoopBeginTick();
			float newBeginTick = TickWindowHelper
					.getMajorTickToLeftOf(xToTick(x - loopSelectionOffset));
			newBeginTick = newBeginTick >= 0 ? (newBeginTick <= MidiManager.MAX_TICKS
					- loopLength ? newBeginTick : MidiManager.MAX_TICKS
					- loopLength)
					: 0;
			MidiManager.setLoopTicks((long) newBeginTick,
					(long) (newBeginTick + loopLength));
			TickWindowHelper.updateView(xToTick(x));
		} else {
			return;
		}
		updateLoopUi();
	}

	public void updateLoopUi() {
		updateLoopRect();
		initLoopMarkerVbs();
		initLoopBarVb();
	}

	public void noMidiMove() {
		if (pointerCount() - getNumLoopMarkersSelected() == 1) {
			if (selectRegion) { // update select region
				selectRegion(pointerIdToPos.get(0).x, pointerIdToPos.get(0).y);
			} else { // one finger scroll
				TickWindowHelper.scroll(pointerIdToPos.get(scrollPointerId).x,
						pointerIdToPos.get(scrollPointerId).y);
			}
		} else if (pointerCount() - getNumLoopMarkersSelected() == 2) {
			// two finger zoom
			float leftX = Math.min(pointerIdToPos.get(0).x,
					pointerIdToPos.get(1).x);
			float rightX = Math.max(pointerIdToPos.get(0).x,
					pointerIdToPos.get(1).x);
			TickWindowHelper.zoom(leftX, rightX, zoomLeftAnchorTick,
					zoomRightAnchorTick);
		}
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		MidiManager.beginMidiEvent(null);
		ScrollBarHelper.startScrollView();
		selectMidiNote(x, y, id);
		if (touchedNotes.get(id) == null) {
			// no note selected.
			// check if loop marker selected
			if (yToNote(y) == -1) {
				selectLoopMarker(id, x);
			} else {
				// otherwise, enable scrolling
				TickWindowHelper.scrollAnchorTick = xToTick(x);
				TickWindowHelper.scrollAnchorY = y
						+ TickWindowHelper.getYOffset();
				scrollPointerId = id;
			}
		}
	}

	@Override
	public void handleActionPointerDown(int id, float x, float y) {
		super.handleActionPointerDown(id, x, y);
		boolean noteAlreadySelected = false;
		noteAlreadySelected = !touchedNotes.isEmpty();
		selectMidiNote(x, y, id);
		if (pointerCount() > 2)
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
					pinchLeftPointerId = pointerIdToPos.get(0).x <= pointerIdToPos
							.get(1).x ? 0 : 1;
					pinchRightPointerId = (pinchLeftPointerId + 1) % 2;
					pinchLeftOffset = leftTick
							- MidiManager.getLeftmostSelectedTick();
					pinchRightOffset = MidiManager.getRightmostSelectedTick()
							- rightTick;
				} else if (pointerCount() - getNumLoopMarkersSelected() == 1) {
					// otherwise, enable scrolling
					TickWindowHelper.scrollAnchorTick = xToTick(x);
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
	public void handleActionMove(int id, float x, float y) {
		super.handleActionMove(id, x, y);
		if (id == pinchLeftPointerId) {
			float leftTick = xToTick(x);
			pinchSelectedNotes(
					leftTick - pinchLeftOffset
							- MidiManager.getLeftmostSelectedTick(), 0);
		} else if (id == pinchRightPointerId) {
			float rightTick = xToTick(x);
			pinchSelectedNotes(
					0,
					rightTick + pinchRightOffset
							- MidiManager.getRightmostSelectedTick());
		} else if (touchedNotes.isEmpty()) {
			// no midi selected. scroll, zoom, or update select region
			noMidiMove();
		} else if (touchedNotes.containsKey(id)) { // at least one midi selected
			float tick = xToTick(x);
			int note = yToNote(y);
			if (touchedNotes.size() == 1) {
				// exactly one pointer not dragging loop markers - drag all
				// selected notes together
				dragNotes(true, touchedNotes.keySet().iterator().next(), tick,
						note);
				// make room in the view window if we are dragging out of the
				// view
				TickWindowHelper.updateView(tick);
			} else if (touchedNotes.size() > 1) { // drag each touched note
													// separately
				dragNotes(false, id, tick, note);
				if (id == 0) {
					// need to make room for two pointers in this case.
					float otherTick = xToTick(pointerIdToPos.get(1).x);
					TickWindowHelper.updateView(Math.min(tick, otherTick),
							Math.max(tick, otherTick));
				}
			}
		}
		updateLoopMarkers();
	}

	@Override
	public void handleActionPointerUp(int id, float x, float y) {
		if (id == scrollPointerId)
			scrollPointerId = -1;
		if (id == pinchLeftPointerId || id == pinchRightPointerId)
			pinchLeftPointerId = pinchRightPointerId = -1;
		for (int i = 0; i < 3; i++)
			if (loopPointerIds[i] == id)
				loopPointerIds[i] = -1;
		if (zoomLeftAnchorTick != -1) {
			int otherId = id == 0 ? 1 : 0;
			TickWindowHelper.scrollAnchorTick = xToTick(pointerIdToPos
					.get(otherId).x);
			scrollPointerId = otherId;
		}
		touchedNotes.remove(id);
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
		ScrollBarHelper.handleActionUp();
		for (int i = 0; i < 3; i++) {
			loopPointerIds[i] = -1;
		}
		pinchLeftPointerId = pinchRightPointerId = -1;
		selectRegion = false;
		startOnTicks.clear();
		touchedNotes.clear();
		initLoopBarVb();
		MidiManager.endMidiEvent();
	}

	@Override
	protected void longPress(int id, float x, float y) {
		if (pointerCount() == 1) {
			startSelectRegion(x, y);
		}
	}

	@Override
	protected void singleTap(int id, float x, float y) {
		MidiNote touchedNote = touchedNotes.get(id);
		if (MidiManager.isCopying()) {
			MidiManager.paste((long) TickWindowHelper
					.getMajorTickToLeftOf(xToTick(x)));
		} else if (touchedNote != null) {
			// single tapping a note always makes it the only selected note
			if (touchedNote.isSelected()) {
				MidiManager.deselectAllNotes();
			}
			MidiManager.selectNote(touchedNote);
		} else {
			int note = yToNote(y);
			float tick = xToTick(x);
			// if no note is touched, than this tap deselects all notes
			if (MidiManager.anyNoteSelected()) {
				MidiManager.deselectAllNotes();
			} else { // add a note based on the current tick granularity
				if (note >= 0 && note < TrackManager.getNumTracks()) {
					addMidiNote(tick, note);
				}
			}
		}
	}

	@Override
	protected void doubleTap(int id, float x, float y) {
		MidiNote touchedNote = touchedNotes.get(id);
		if (touchedNote != null) {
			MidiManager.deleteNote(touchedNote);
		}
	}
}
