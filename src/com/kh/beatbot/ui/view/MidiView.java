package com.kh.beatbot.ui.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.Track;
import com.kh.beatbot.listener.LoopChangeListener;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.Line;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.group.MidiViewGroup;
import com.kh.beatbot.ui.view.helper.ScrollHelper;
import com.kh.beatbot.ui.view.helper.Scrollable;

public class MidiView extends ClickableView implements TrackListener, Scrollable,
		LoopChangeListener {

	public static final float LOOP_SELECT_SNAP_DIST = 30;
	public static final int NUM_VERTICAL_LINE_SETS = 8;
	public static float trackHeight;

	private float dragOffsetTick[] = { 0, 0, 0, 0, 0 };
	private float pinchLeftOffset = 0, pinchRightOffset = 0, zoomLeftAnchorTick = 0,
			zoomRightAnchorTick = 0, selectRegionStartTick = -1, selectRegionStartY = -1;

	private int pinchLeftPointerId = -1, pinchRightPointerId = -1;

	private Line[] loopMarkerLines;
	private List<Line>[] vLines;
	private Line currTickLine;
	private Rectangle leftLoopRect, rightLoopRect, selectRegionRect;

	// map of pointerIds to the notes they are selecting
	private Map<Integer, MidiNote> touchedNotes = new HashMap<Integer, MidiNote>();
	// map of pointerIds to the original on-ticks of the notes they are touching
	// (before dragging)
	private Map<Integer, Float> startOnTicks = new HashMap<Integer, Float>();

	protected ScrollHelper scrollHelper;

	public MidiView(RenderGroup renderGroup) {
		super(renderGroup);
		scrollHelper = new ScrollHelper(this);
		MidiManager.addLoopChangeListener(this);
	}

	public synchronized void updateVerticalLineColors() {
		for (int i = 0; i < NUM_VERTICAL_LINE_SETS; i++) {
			float[] lineColor = i > MidiManager.getBeatDivision() + 2 ? Color.TRANSPARENT
					: Color.MIDI_LINES[i];
			for (Line line : vLines[i]) {
				if (!line.getStrokeColor().equals(lineColor)) {
					line.setStrokeColor(lineColor);
				}
			}
		}
	}

	public float getTotalTrackHeight() {
		return trackHeight * TrackManager.getNumTracks();
	}

	public void reset() {
		scrollHelper.setXOffset(0);
	}

	private void selectRegion(Pointer pos) {
		float tick = xToTick(pos.x);
		float leftTick = Math.min(tick, selectRegionStartTick);
		float rightTick = Math.max(tick, selectRegionStartTick);
		float topY = Math.min(pos.y, selectRegionStartY);
		float bottomY = Math.max(pos.y, selectRegionStartY);
		// make sure select rect doesn't go into the tick view
		topY = Math.max(topY + .01f, 0);
		// make sure select rect doesn't go past the last track/note
		bottomY = Math.min(bottomY + .01f, getTotalTrackHeight() - .01f);
		int topNote = yToNote(topY);
		int bottomNote = yToNote(bottomY);
		MidiManager.selectRegion((long) leftTick, (long) rightTick, topNote, bottomNote);
		// for normal view, round the drawn rectangle to nearest notes
		topY = noteToY(topNote);
		bottomY = noteToY(bottomNote + 1);
		// make room in the view window if we are dragging out of the view
		scrollHelper.updateView(tick, topY, bottomY);
		selectRegionRect.layout(tickToUnscaledX(leftTick), absoluteY + topY,
				tickToUnscaledX(rightTick - leftTick), bottomY - topY);
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

	private void selectMidiNote(int pointerId, Pointer pos) {
		final float tick = xToTick(pos.x);
		MidiNote selectedNote = getMidiNote(yToNote(pos.y), tick);

		if (selectedNote == null || selectedNote.isTouched())
			return;

		startOnTicks.put(pointerId, (float) selectedNote.getOnTick());
		dragOffsetTick[pointerId] = tick - selectedNote.getOnTick();
		// don't need right offset for simple drag (one finger select)

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

	private void updateCurrentTick() {
		currTickLine.setPosition(tickToUnscaledX(MidiManager.getCurrTick()), absoluteY);
	}

	public float tickToUnscaledX(float tick) {
		return tick * width / MidiManager.MAX_TICKS;
	}

	public float unscaledXToTick(float x) {
		return x * MidiManager.MAX_TICKS / width;
	}

	public float tickToX(float tick) {
		return (tick - scrollHelper.xOffset) / getNumTicks() * width;
	}

	public float xToTick(float x) {
		return getNumTicks() * x / width + scrollHelper.xOffset;
	}

	public int yToNote(float y) {
		float f = ((y + scrollHelper.yOffset) / trackHeight);
		return f < 0 ? -1 : (int) f;
	}

	public float noteToY(int note) {
		return note * trackHeight - scrollHelper.yOffset;
	}

	public float noteToUnscaledY(int note) {
		return note * trackHeight;
	}

	public float getXOffset() {
		return scrollHelper.xOffset;
	}

	public float getYOffset() {
		return scrollHelper.yOffset;
	}

	public float getNumTicks() {
		return scrollHelper.numTicks;
	}

	@Override
	public synchronized void createChildren() {
		setClip(true);
		leftLoopRect = new Rectangle(MidiViewGroup.scaleGroup, Color.DARK_TRANS, null);
		rightLoopRect = new Rectangle(MidiViewGroup.scaleGroup, Color.DARK_TRANS, null);
		selectRegionRect = new Rectangle(MidiViewGroup.translateScaleGroup, Color.TRANSPARENT,
				Color.TRON_BLUE);
		currTickLine = new Line(MidiViewGroup.translateScaleGroup, null, Color.TRON_BLUE);
		loopMarkerLines = new Line[2];
		vLines = new ArrayList[NUM_VERTICAL_LINE_SETS];
		for (int i = 0; i < NUM_VERTICAL_LINE_SETS; i++) {
			vLines[i] = new ArrayList<Line>();
		}
		for (int i = 0; i < loopMarkerLines.length; i++) {
			loopMarkerLines[i] = new Line(MidiViewGroup.translateScaleGroup, null, Color.TRON_BLUE);
		}
	}

	@Override
	public synchronized void layoutChildren() {
		currTickLine.layout(absoluteX, absoluteY, 3, height);
		for (int i = 0; i < loopMarkerLines.length; i++) {
			loopMarkerLines[i].layout(absoluteX, absoluteY, 3, height);
		}
		int minTickSpacing = MidiManager.MIN_TICKS / 8;
		for (List<Line> lines : vLines) {
			lines.clear();
		}
		for (long currTick = 0; currTick < MidiManager.MAX_TICKS; currTick += minTickSpacing) {
			for (int i = 0; i < NUM_VERTICAL_LINE_SETS; i++) {
				if (currTick % (2 << (NUM_VERTICAL_LINE_SETS - i)) == 0) {
					Line line = new Line(MidiViewGroup.scaleGroup, null, Color.TRANSPARENT);
					line.layout(tickToUnscaledX(currTick), absoluteY, 2, 1);
					vLines[i].add(line);
					break; // each line goes in only ONE line set
				}
			}
		}
		onScaleX();
	}

	@Override
	public void tick() {
		if (PlaybackManager.getState() == PlaybackManager.State.PLAYING) {
			updateCurrentTick();
		}
		if (pointerCount() == 0) {
			scrollHelper.scroll(); // take care of any momentum scrolling
		}
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

	private void startSelectRegion(Pointer pos) {
		selectRegionStartTick = xToTick(pos.x);
		selectRegionStartY = noteToY(yToNote(pos.y));
		selectRegionRect.layout(tickToUnscaledX(selectRegionStartTick), absoluteY
				+ selectRegionStartY, 1, trackHeight);
		selectRegionRect.bringToTop();
		selectRegionRect.setColors(Color.TRON_BLUE_TRANS, Color.TRON_BLUE);
	}

	private void stopSelectRegion() {
		selectRegionStartTick = -1;
		selectRegionRect.setColors(Color.TRANSPARENT, Color.TRANSPARENT);
	}

	// adds a note starting at the nearest major tick (nearest displayed
	// grid line) to the left and ending one tick before the nearest major
	// tick to the right of the given tick
	private void addMidiNote(int track, float tick) {
		if (track < 0 || track >= TrackManager.getNumTracks())
			return;
		long spacing = MidiManager.getMajorTickSpacing();
		long onTick = (long) (tick - tick % spacing);
		long offTick = onTick + spacing - 1;
		MidiManager.addNote(onTick, offTick, track, .75f, .5f, .5f);
	}

	public void createNoteView(MidiNote note) {
		note.setRectangle(new Rectangle(MidiViewGroup.translateScaleGroup, whichColor(note),
				Color.BLACK));
		layoutNote(note);
	}

	public void layoutNote(MidiNote note) {
		if (note.getRectangle() != null) {
			note.getRectangle().layout(tickToUnscaledX(note.getOnTick()),
					absoluteY + noteToUnscaledY(note.getNoteValue()),
					tickToUnscaledX(note.getOffTick() - note.getOnTick()), trackHeight);
		}
	}

	public void updateNoteViewSelected(MidiNote note) {
		if (note.getRectangle() != null) {
			note.getRectangle().setFillColor(whichColor(note));
			note.getRectangle().bringToTop();
		}
		//loop/tick lines always display ontop of notes
		currTickLine.bringToTop();
		loopMarkerLines[0].bringToTop();
		loopMarkerLines[1].bringToTop();
		selectRegionRect.bringToTop();
	}

	public void layoutNotes() {
		for (Track track : TrackManager.getTracks()) {
			for (MidiNote note : track.getMidiNotes()) {
				layoutNote(note);
			}
		}
	}

	private static float[] whichColor(MidiNote note) {
		return note.isSelected() ? Color.NOTE_SELECTED : Color.NOTE;
	}

	private static float[] whichColor(Track track) {
		Track soloingTrack = TrackManager.getSoloingTrack();
		if (track.isSelected()) {
			return Color.LABEL_SELECTED;
		} else if (track.isMuted() || (soloingTrack != null && !soloingTrack.equals(track))) {
			return Color.MIDI_VIEW_DARK_BG;
		} else {
			return Color.MIDI_VIEW_BG;
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

	public void updateView(float tick) {
		scrollHelper.updateView(tick);
	}

	public void updateView(float leftTick, float rightTick) {
		scrollHelper.updateView(leftTick, rightTick);
	}

	private synchronized void noMidiMove() {
		if (pointerCount() == 1) {
			if (selectRegionStartTick >= 0) {
				selectRegion(pointersById.get(0));
			} else { // one finger scroll
				scrollHelper.scroll(pointersById.get(scrollHelper.scrollPointerId));
			}
		} else if (pointerCount() == 2) {
			// two finger zoom
			float leftX = Math.min(pointersById.get(0).x, pointersById.get(1).x);
			float rightX = Math.max(pointersById.get(0).x, pointersById.get(1).x);
			scrollHelper.zoom(leftX, rightX, zoomLeftAnchorTick, zoomRightAnchorTick);
		}
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		super.handleActionDown(id, pos);
		MidiManager.beginMidiEvent(null);
		selectMidiNote(id, pos);
		if (touchedNotes.get(id) == null) { // no note selected. enable scrolling
			scrollHelper.setScrollAnchor(id, xToTick(pos.x), pos.y + scrollHelper.yOffset);
		}
	}

	@Override
	public void handleActionPointerDown(int id, Pointer pos) {
		super.handleActionPointerDown(id, pos);
		selectMidiNote(id, pos);
		if (pointerCount() > 2 || null != touchedNotes.get(id))
			return;
		// TODO might cause problems
		float leftTick = xToTick(Math.min(pointersById.get(0).x, pointersById.get(1).x));
		float rightTick = xToTick(Math.max(pointersById.get(0).x, pointersById.get(1).x));
		if (!touchedNotes.isEmpty()) {
			// note is selected with one pointer, but this pointer
			// did not select a note. start pinching all selected notes.
			pinchLeftPointerId = pointersById.get(0).x <= pointersById.get(1).x ? 0 : 1;
			pinchRightPointerId = (pinchLeftPointerId + 1) % 2;
			pinchLeftOffset = leftTick - MidiManager.getLeftmostSelectedTick();
			pinchRightOffset = MidiManager.getRightmostSelectedTick() - rightTick;
		} else if (pointerCount() == 1) {
			// otherwise, enable scrolling
			scrollHelper.setScrollAnchor(id, xToTick(pos.x));
		} else {
			// can never select region with two pointers in midi view
			stopSelectRegion();
			// init zoom anchors (the same ticks should be under the
			// fingers at all times)
			zoomLeftAnchorTick = leftTick;
			zoomRightAnchorTick = rightTick;
		}

	}

	@Override
	public void handleActionMove(int id, Pointer pos) {
		super.handleActionMove(id, pos);
		if (id == pinchLeftPointerId) {
			float leftTick = xToTick(pos.x);
			pinchSelectedNotes(leftTick - pinchLeftOffset - MidiManager.getLeftmostSelectedTick(),
					0);
		} else if (id == pinchRightPointerId) {
			float rightTick = xToTick(pos.x);
			pinchSelectedNotes(0,
					rightTick + pinchRightOffset - MidiManager.getRightmostSelectedTick());
		} else if (touchedNotes.isEmpty()) {
			// no midi selected. scroll, zoom, or update select region
			noMidiMove();
		} else if (touchedNotes.containsKey(id)) { // at least one midi selected
			float tick = xToTick(pos.x);
			int note = yToNote(pos.y);
			if (touchedNotes.size() == 1) {
				// exactly one pointer not dragging loop markers - drag all selected notes together
				dragNotes(true, touchedNotes.keySet().iterator().next(), tick, note);
				// make room in the view window if we are dragging out of the view
				scrollHelper.updateView(tick);
			} else if (touchedNotes.size() > 1) { // drag each touched note separately
				dragNotes(false, id, tick, note);
				if (id == 0) {
					// need to make room for two pointers in this case.
					float otherTick = xToTick(pointersById.get(1).x);
					scrollHelper.updateView(Math.min(tick, otherTick), Math.max(tick, otherTick));
				}
			}
		}
	}

	@Override
	public void handleActionPointerUp(int id, Pointer pos) {
		scrollHelper.pointerUp(id);
		if (id == pinchLeftPointerId || id == pinchRightPointerId) {
			pinchLeftPointerId = pinchRightPointerId = -1;
		}
		if (zoomLeftAnchorTick != -1) {
			int otherId = id == 0 ? 1 : 0;
			scrollHelper.setScrollAnchor(otherId, xToTick(pointersById.get(otherId).x));
		}
		touchedNotes.remove(id);
	}

	@Override
	public void handleActionUp(int id, Pointer pos) {
		super.handleActionUp(id, pos);
		scrollHelper.handleActionUp();
		pinchLeftPointerId = pinchRightPointerId = -1;
		stopSelectRegion();
		startOnTicks.clear();
		touchedNotes.clear();
		MidiManager.endMidiEvent();
	}

	@Override
	protected void longPress(int id, Pointer pos) {
		if (pointerCount() == 1) {
			startSelectRegion(pos);
		}
	}

	@Override
	protected void singleTap(int id, Pointer pos) {
		MidiNote touchedNote = touchedNotes.get(id);
		if (MidiManager.isCopying()) {
			MidiManager.paste((long) MidiManager.getMajorTickToLeftOf(xToTick(pos.x)));
		} else if (touchedNote != null) {
			// single tapping a note always makes it the only selected note
			if (touchedNote.isSelected()) {
				MidiManager.deselectAllNotes();
			}
			touchedNote.setSelected(true);
		} else { // if no note is touched, than this tap deselects all notes
			if (MidiManager.anyNoteSelected()) {
				MidiManager.deselectAllNotes();
			} else { // add a note based on the current tick granularity
				addMidiNote(yToNote(pos.y), xToTick(pos.x));
			}
		}
	}

	@Override
	protected void doubleTap(int id, Pointer pos) {
		MidiNote touchedNote = touchedNotes.get(id);
		if (touchedNote != null) {
			MidiManager.deleteNote(touchedNote);
		}
	}

	@Override
	public void onCreate(Track track) {
		track.setRectangle(new Rectangle(MidiViewGroup.translateYGroup, Color.TRANSPARENT,
				Color.BLACK));
		onTrackHeightChange();
		scrollHelper.setYOffset(Float.MAX_VALUE);
		for (MidiNote note : track.getMidiNotes()) {
			createNoteView(note);
		}
	}

	@Override
	public void onDestroy(Track track) {
		track.getRectangle().hide();
		for (MidiNote note : track.getMidiNotes()) {
			note.getRectangle().hide();
			note.setRectangle(null);
		}
		onTrackHeightChange();
		layoutNotes();
		scrollHelper.setYOffset(scrollHelper.yOffset);
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

	private synchronized void onTrackHeightChange() {
		float trackHeight = getTotalTrackHeight();
		currTickLine.setDimensions(currTickLine.width, trackHeight);
		leftLoopRect.setDimensions(leftLoopRect.width, trackHeight);
		rightLoopRect.setDimensions(rightLoopRect.width, trackHeight);
		for (int i = 0; i < loopMarkerLines.length; i++) {
			loopMarkerLines[i].setDimensions(loopMarkerLines[i].width, trackHeight);
		}
		float lineHeight = Math.min(trackHeight, height);
		for (List<Line> lines : vLines) {
			for (Line line : lines) {
				line.setDimensions(2, lineHeight);
			}
		}
		layoutTrackRects();
	}

	private void layoutTrackRects() {
		for (Track track : TrackManager.getTracks()) {
			Rectangle trackRect = track.getRectangle();
			if (trackRect != null) {
				trackRect.layout(absoluteX, absoluteY + noteToUnscaledY(track.getId()), width,
						trackHeight);
			}
		}
	}

	@Override
	public float unscaledHeight() {
		return getTotalTrackHeight();
	}

	@Override
	public void onScrollX() {
	}

	@Override
	public void onScrollY() {
	}

	@Override
	public void onScaleX() {
		updateVerticalLineColors();
		MidiManager.adjustBeatDivision(getNumTicks());
	}

	@Override
	public float width() {
		return width;
	}

	@Override
	public float height() {
		return height;
	}

	@Override
	public void onLoopChange(long loopBeginTick, long loopEndTick) {
		float x1 = tickToUnscaledX(loopBeginTick);
		float x2 = tickToUnscaledX(loopEndTick);
		float height = getTotalTrackHeight();
		leftLoopRect.layout(0, absoluteY, x1, height);
		rightLoopRect.layout(x2, absoluteY, width - x2, height);
		float lineY = absoluteY;
		loopMarkerLines[0].setPosition(x1, lineY);
		loopMarkerLines[1].setPosition(x2, lineY);

		// scrollHelper.updateView(MidiManager.getLoopBeginTick(), MidiManager.getLoopEndTick());
	}
}
