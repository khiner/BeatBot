package com.kh.beatbot.ui.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.Track;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.Line;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.view.helper.ScrollBarHelper;
import com.kh.beatbot.ui.view.helper.TickWindowHelper;

public class MidiView extends ClickableView implements TrackListener {

	public static final float Y_OFFSET = 21, LOOP_SELECT_SNAP_DIST = 30;
	public static float trackHeight;

	private static float dragOffsetTick[] = { 0, 0, 0, 0, 0 };

	private static int pinchLeftPointerId = -1, pinchRightPointerId = -1, scrollPointerId = -1;

	private static float pinchLeftOffset = 0, pinchRightOffset = 0, zoomLeftAnchorTick = 0,
			zoomRightAnchorTick = 0, loopSelectionOffset = 0, selectRegionStartTick = -1,
			selectRegionStartY = -1;

	private static int[] loopPointerIds = { -1, -1, -1 };

	private static ShapeGroup noteRectangles = new ShapeGroup();

	// vertical line loop markers
	private Line[] loopMarkerLines;
	private Line currTickLine;
	private Rectangle backgroundRect, loopRect, tickBarRect, loopBarRect, selectRegionRect;

	// map of pointerIds to the notes they are selecting
	private Map<Integer, MidiNote> touchedNotes = new HashMap<Integer, MidiNote>();

	// map of pointerIds to the original on-ticks of the notes they are touching
	// (before dragging)
	private Map<Integer, Float> startOnTicks = new HashMap<Integer, Float>();

	public float getMidiHeight() {
		return Math.min(height - Y_OFFSET, getTotalTrackHeight());
	}

	public float getTotalTrackHeight() {
		return trackHeight * TrackManager.getNumTracks();
	}

	public int getNumLoopMarkersSelected() {
		int numSelected = 0;
		for (int i = 0; i < 3; i++)
			if (loopPointerIds[i] != -1)
				numSelected++;
		return numSelected;
	}

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
		bottomY = Math.min(bottomY + .01f, Y_OFFSET + getTotalTrackHeight() - .01f);
		int topNote = yToNote(topY);
		int bottomNote = yToNote(bottomY);
		MidiManager.selectRegion((long) leftTick, (long) rightTick, topNote, bottomNote);
		// for normal view, round the drawn rectangle to nearest notes
		topY = noteToY(topNote);
		bottomY = noteToY(bottomNote + 1);
		// make room in the view window if we are dragging out of the view
		TickWindowHelper.updateView(tick, topY, bottomY);
		selectRegionRect.layout(tickToUnscaledX(leftTick), topY, tickToUnscaledX(rightTick
				- leftTick), bottomY - topY);
		selectRegionRect.setFillColor(Color.TRON_BLUE_TRANS);
	}

	public MidiNote getMidiNote(int track, float tick) {
		if (track < 0 || track >= TrackManager.getNumTracks()) {
			return null;
		}
		for (int i = 0; i < MidiManager.getMidiNotes().size(); i++) {
			MidiNote midiNote = MidiManager.getMidiNotes().get(i);
			if (midiNote.getNoteValue() == track && midiNote.getOnTick() <= tick
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
		dragOffsetTick[pointerId] = tick - selectedNote.getOnTick();
		// don't need right offset for simple drag (one finger
		// select)

		// If this is the only touched midi note, and it hasn't yet
		// been selected, make it the only selected note.
		// If we are multi-selecting, add it to the selected list
		if (!selectedNote.isSelected()) {
			if (touchedNotes.isEmpty()) {
				MidiManager.deselectAllNotes();
			}
			selectedNote.setSelected(true);
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
			loopMarkerLines[0].setStrokeColor(Color.TRON_BLUE);
		} else if (Math.abs(x - loopEndX) <= LOOP_SELECT_SNAP_DIST) {
			loopPointerIds[2] = pointerId;
			loopMarkerLines[1].setStrokeColor(Color.TRON_BLUE);
		} else if (x > loopBeginX && x < loopEndX) {
			loopPointerIds[1] = pointerId;
			loopSelectionOffset = x - loopBeginX;
			loopBarRect.setFillColor(Color.TRON_BLUE);
		}
	}

	private void updateCurrentTick() {
		currTickLine.setPosition(tickToUnscaledX(MidiManager.getCurrTick()), 0);
	}

	public float tickToUnscaledX(float tick) {
		return tick / MidiManager.MAX_TICKS * width;
	}

	public float tickToX(float tick) {
		return (tick - TickWindowHelper.getTickOffset()) / TickWindowHelper.getNumTicks() * width;
	}

	public float xToTick(float x) {
		return TickWindowHelper.getNumTicks() * x / width + TickWindowHelper.getTickOffset();
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

	public void initAllVbs() {
		updateLoopUi();
	}

	public synchronized void init() {
		setClip(true);
		shouldDraw = false;
		initAllVbs();
	}

	@Override
	public synchronized void createChildren() {
		backgroundRect = new Rectangle(shapeGroup, Color.MIDI_VIEW_BG, null);
		loopRect = new Rectangle(shapeGroup, Color.MIDI_VIEW_LIGHT_BG, null);

		tickBarRect = new Rectangle(shapeGroup, Color.TICK_FILL, Color.BLACK);
		loopBarRect = new Rectangle(shapeGroup, Color.TICKBAR, null);
		selectRegionRect = new Rectangle(shapeGroup, Color.TRANSPARENT, null);
		currTickLine = new Line(shapeGroup, null, Color.TRON_BLUE);
		loopMarkerLines = new Line[2];
		for (int i = 0; i < loopMarkerLines.length; i++) {
			loopMarkerLines[i] = new Line(shapeGroup, null, Color.TRON_BLUE);
		}
	}

	@Override
	public synchronized void layoutChildren() {
		tickBarRect.layout(0, 0, width, Y_OFFSET);
		backgroundRect.layout(0, Y_OFFSET, tickToUnscaledX(MidiManager.MAX_TICKS - 1), 1);
		currTickLine.layout(0, 0, 3, height);
		for (int i = 0; i < loopMarkerLines.length; i++) {
			loopMarkerLines[i].layout(0, 0, 3, height);
		}
		TickWindowHelper.init(this, shapeGroup);
	}

	@Override
	public void draw() {
		if (PlaybackManager.getState() == PlaybackManager.State.PLAYING) {
			// if playing, draw curr tick
			updateCurrentTick();
		}
		TickWindowHelper.scroll(); // take care of any momentum scrolling
		push();
		translate(-TickWindowHelper.getTickOffset() / TickWindowHelper.getNumTicks() * width
				+ absoluteX, absoluteY);
		scale((float) MidiManager.MAX_TICKS / (float) TickWindowHelper.getNumTicks(), 1);

		// draws all rect children in one call
		shapeGroup.draw();

		push();
		translate(0, -TickWindowHelper.getYOffset());
		noteRectangles.draw();
		pop();

		pop();
	}

	private float getAdjustedTickDiff(float tickDiff, int pointerId, MidiNote singleNote) {
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
				adjustedTickDiff = MidiManager.MAX_TICKS - selectedNote.getOffTick();
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
			} else if (TrackManager.getNumTracks() - 1 - selectedNote.getNoteValue() < adjustedNoteDiff) {
				adjustedNoteDiff = TrackManager.getNumTracks() - 1 - selectedNote.getNoteValue();
			}
		}
		return adjustedNoteDiff;
	}

	private void startSelectRegion(float x, float y) {
		selectRegionStartTick = xToTick(x);
		selectRegionStartY = noteToY(yToNote(y));
	}

	// adds a note starting at the nearest major tick (nearest displayed
	// grid line) to the left and ending one tick before the nearest major
	// tick to the right of the given tick
	private MidiNote addMidiNote(float tick, int track) {
		float spacing = MidiManager.getMajorTickSpacing();
		float onTick = tick - tick % spacing;
		float offTick = onTick + spacing - 1;
		return MidiManager.addNote((long) onTick, (long) offTick, track, .75f, .5f, .5f);
	}

	public void createNoteView(MidiNote note) {
		Rectangle noteRect = new Rectangle(noteRectangles, whichColor(note), Color.BLACK);
		note.setRectangle(noteRect);
		updateNoteView(note);
	}

	public void updateNoteView(MidiNote note) {
		float x1 = tickToUnscaledX(note.getOnTick());
		float y1 = noteToUnscaledY(note.getNoteValue());
		float width = tickToUnscaledX(note.getOffTick()) - x1;

		if (note.getRectangle() != null) {
			note.getRectangle().layout(x1, y1, width, trackHeight);
		}
	}

	public void updateNoteViewSelected(MidiNote note) {
		if (note.getRectangle() != null) {
			note.getRectangle().setFillColor(whichColor(note));
			note.getRectangle().bringToTop();
		}
	}

	public void updateNoteRects() {
		for (Track track : TrackManager.getTracks()) {
			for (MidiNote note : track.getMidiNotes()) {
				updateNoteView(note);
			}
		}
	}

	private static float[] whichColor(MidiNote note) {
		return note.isSelected() ? Color.NOTE_SELECTED : Color.NOTE;
	}

	private static float[] whichColor(Track track) {
		Track soloingTrack = TrackManager.getSoloingTrack();
		if (track.isSelected()) {
			return Color.LABEL_SELECTED_TRANS;
		} else if (track.isMuted() || (soloingTrack != null && !soloingTrack.equals(track))) {
			return Color.DARK_TRANS;
		} else {
			return Color.TRANSPARENT;
		}
	}

	private void dragNotes(boolean dragAllSelected, int pointerId, float currTick, int currNote) {
		MidiNote touchedNote = touchedNotes.get(pointerId);
		if (touchedNote == null)
			return;
		int noteDiff = currNote - touchedNote.getNoteValue();
		float tickDiff = currTick - dragOffsetTick[pointerId] - touchedNote.getOnTick();
		if (noteDiff == 0 && tickDiff == 0)
			return;
		tickDiff = getAdjustedTickDiff(tickDiff, pointerId, dragAllSelected ? null : touchedNote);
		noteDiff = getAdjustedNoteDiff(noteDiff, dragAllSelected ? null : touchedNote);
		List<MidiNote> notesToDrag = dragAllSelected ? MidiManager.getSelectedNotes() : Arrays
				.asList(touchedNote);

		MidiManager.moveNotes(notesToDrag, (long) tickDiff, noteDiff);
	}

	private void pinchSelectedNotes(float onTickDiff, float offTickDiff) {
		if (onTickDiff == 0 && offTickDiff == 0)
			return;
		MidiManager.pinchNotes(MidiManager.getSelectedNotes(), (long) onTickDiff,
				(long) offTickDiff);
	}

	private void updateTrackColors() {
		for (Track track : TrackManager.getTracks()) {
			track.getRectangle().setFillColor(whichColor(track));
		}
	}

	private void updateLoopMarkers() {
		if (loopPointerIds[0] != -1 && loopPointerIds[2] != -1) {
			float leftX = pointerIdToPos.get(loopPointerIds[0]).x;
			float rightX = pointerIdToPos.get(loopPointerIds[2]).x;
			float leftTick = xToTick(leftX);
			float rightTick = xToTick(rightX);
			float leftMajorTick = MidiManager.getMajorTickNearestTo(leftTick);
			float rightMajorTick = MidiManager.getMajorTickNearestTo(rightTick);
			MidiManager.setLoopTicks((long) leftMajorTick, (long) rightMajorTick);
			TickWindowHelper.updateView(leftTick, rightTick);
		} else if (loopPointerIds[0] != -1) {
			float leftX = pointerIdToPos.get(loopPointerIds[0]).x;
			float leftTick = xToTick(leftX);
			float leftMajorTick = MidiManager.getMajorTickNearestTo(leftTick);
			MidiManager.setLoopBeginTick((long) leftMajorTick);
			TickWindowHelper.updateView(leftTick);
		} else if (loopPointerIds[2] != -1) {
			float rightX = pointerIdToPos.get(loopPointerIds[2]).x;
			float rightTick = xToTick(rightX);
			float rightMajorTick = MidiManager.getMajorTickNearestTo(rightTick);
			MidiManager.setLoopEndTick((long) rightMajorTick);
			TickWindowHelper.updateView(rightTick);
		} else if (loopPointerIds[1] != -1) {
			float x = pointerIdToPos.get(loopPointerIds[1]).x;
			// middle selected. move begin and end
			// preserve current loop length
			float loopLength = MidiManager.getLoopEndTick() - MidiManager.getLoopBeginTick();
			float newBeginTick = MidiManager.getMajorTickToLeftOf(xToTick(x - loopSelectionOffset));
			newBeginTick = newBeginTick >= 0 ? (newBeginTick <= MidiManager.MAX_TICKS - loopLength ? newBeginTick
					: MidiManager.MAX_TICKS - loopLength)
					: 0;
			MidiManager.setLoopTicks((long) newBeginTick, (long) (newBeginTick + loopLength));
			TickWindowHelper.updateView(xToTick(x));
		} else {
			return;
		}
		updateLoopUi();
	}

	public void updateLoopUi() {
		float x1 = tickToUnscaledX(MidiManager.getLoopBeginTick());
		float x2 = tickToUnscaledX(MidiManager.getLoopEndTick());

		loopBarRect.layout(x1, 0, x2 - x1, Y_OFFSET);
		loopRect.layout(x1, Y_OFFSET, x2 - x1, getTotalTrackHeight());
		loopMarkerLines[0].setPosition(x1, 0);
		loopMarkerLines[1].setPosition(x2, 0);
	}

	private synchronized void noMidiMove() {
		if (pointerCount() - getNumLoopMarkersSelected() == 1) {
			if (selectRegionStartTick >= 0) {
				selectRegion(pointerIdToPos.get(0).x, pointerIdToPos.get(0).y);
			} else { // one finger scroll
				TickWindowHelper.scroll(pointerIdToPos.get(scrollPointerId).x,
						pointerIdToPos.get(scrollPointerId).y);
			}
		} else if (pointerCount() - getNumLoopMarkersSelected() == 2) {
			// two finger zoom
			float leftX = Math.min(pointerIdToPos.get(0).x, pointerIdToPos.get(1).x);
			float rightX = Math.max(pointerIdToPos.get(0).x, pointerIdToPos.get(1).x);
			TickWindowHelper.zoom(leftX, rightX, zoomLeftAnchorTick, zoomRightAnchorTick);
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
				TickWindowHelper.scrollAnchorY = y + TickWindowHelper.getYOffset();
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
				float leftTick = xToTick(Math.min(pointerIdToPos.get(0).x, pointerIdToPos.get(1).x));
				float rightTick = xToTick(Math
						.max(pointerIdToPos.get(0).x, pointerIdToPos.get(1).x));
				if (noteAlreadySelected) {
					// note is selected with one pointer, but this pointer
					// did not select a note. start pinching all selected notes.
					pinchLeftPointerId = pointerIdToPos.get(0).x <= pointerIdToPos.get(1).x ? 0 : 1;
					pinchRightPointerId = (pinchLeftPointerId + 1) % 2;
					pinchLeftOffset = leftTick - MidiManager.getLeftmostSelectedTick();
					pinchRightOffset = MidiManager.getRightmostSelectedTick() - rightTick;
				} else if (pointerCount() - getNumLoopMarkersSelected() == 1) {
					// otherwise, enable scrolling
					TickWindowHelper.scrollAnchorTick = xToTick(x);
					scrollPointerId = id;
				} else {
					// can never select region with two pointers in midi view
					selectRegionRect.setFillColor(Color.TRANSPARENT);
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
			pinchSelectedNotes(leftTick - pinchLeftOffset - MidiManager.getLeftmostSelectedTick(),
					0);
		} else if (id == pinchRightPointerId) {
			float rightTick = xToTick(x);
			pinchSelectedNotes(0,
					rightTick + pinchRightOffset - MidiManager.getRightmostSelectedTick());
		} else if (touchedNotes.isEmpty()) {
			// no midi selected. scroll, zoom, or update select region
			noMidiMove();
		} else if (touchedNotes.containsKey(id)) { // at least one midi selected
			float tick = xToTick(x);
			int note = yToNote(y);
			if (touchedNotes.size() == 1) {
				// exactly one pointer not dragging loop markers - drag all
				// selected notes together
				dragNotes(true, touchedNotes.keySet().iterator().next(), tick, note);
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
			if (loopPointerIds[i] == id) {
				loopPointerIds[i] = -1;
				if (i == 0) {
					loopMarkerLines[0].setStrokeColor(Color.TICKBAR);
				} else if (i == 1) {
					loopMarkerLines[1].setStrokeColor(Color.TICKBAR);
				} else if (i == 1) {
					loopBarRect.setFillColor(Color.TICKBAR);
				}
			}
		if (zoomLeftAnchorTick != -1) {
			int otherId = id == 0 ? 1 : 0;
			TickWindowHelper.scrollAnchorTick = xToTick(pointerIdToPos.get(otherId).x);
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
		loopBarRect.setFillColor(Color.TICKBAR);
		pinchLeftPointerId = pinchRightPointerId = -1;
		selectRegionStartTick = -1;
		selectRegionRect.setFillColor(Color.TRANSPARENT);
		startOnTicks.clear();
		touchedNotes.clear();
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
			MidiManager.paste((long) MidiManager.getMajorTickToLeftOf(xToTick(x)));
		} else if (touchedNote != null) {
			// single tapping a note always makes it the only selected note
			if (touchedNote.isSelected()) {
				MidiManager.deselectAllNotes();
			}
			touchedNote.setSelected(true);
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

	@Override
	public void onCreate(Track track) {
		onTrackHeightChange();
		TickWindowHelper.setYOffset(Float.MAX_VALUE);
		Rectangle trackRect = new Rectangle(shapeGroup, Color.TRANSPARENT, Color.BLACK);
		track.setRectangle(trackRect);
		layoutTrackRects();
		for (MidiNote note : track.getMidiNotes()) {
			createNoteView(note);
		}
	}

	@Override
	public void onDestroy(Track track) {
		onTrackHeightChange();
		TickWindowHelper.setYOffset(TickWindowHelper.getYOffset());
		track.getRectangle().destroy();
		for (MidiNote note : track.getMidiNotes()) {
			note.getRectangle().destroy();
			note.setRectangle(null);
		}
		updateNoteRects();
		layoutTrackRects();
	}

	@Override
	public void onSelect(BaseTrack track) {
		updateTrackColors();
	}

	@Override
	public void onSampleChange(Track track) {
	}

	@Override
	public void onMuteChange(Track track, boolean mute) {
		updateTrackColors();
	}

	@Override
	public void onSoloChange(Track track, boolean solo) {
		updateTrackColors();
	}

	private void onTrackHeightChange() {
		initAllVbs();
		float trackHeight = getTotalTrackHeight();
		backgroundRect.setDimensions(backgroundRect.width, trackHeight);
		currTickLine.setDimensions(currTickLine.width, trackHeight);
		loopRect.setDimensions(loopRect.width, trackHeight);
		for (int i = 0; i < loopMarkerLines.length; i++) {
			loopMarkerLines[i].setDimensions(loopMarkerLines[i].width, trackHeight);
		}
		TickWindowHelper.setHeight(trackHeight);
	}

	private void layoutTrackRects() {
		for (Track track : TrackManager.getTracks()) {
			float x1 = 0;
			float y1 = noteToY(track.getId());
			float width = tickToUnscaledX(MidiManager.MAX_TICKS - 1);
			Rectangle trackRect = track.getRectangle();
			if (trackRect != null) {
				trackRect.layout(x1, y1, width, trackHeight);
			}
		}
	}

	@Override
	public void notifyScrollY() {
		super.notifyScrollY();
		layoutTrackRects();
	}
}
