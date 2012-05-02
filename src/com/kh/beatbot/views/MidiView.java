package com.kh.beatbot.views;

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

import com.kh.beatbot.MidiManager;
import com.kh.beatbot.PlaybackManager;
import com.kh.beatbot.RecordManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiView extends SurfaceViewBase {

	private class TickWindow {
		// leftmost tick to display
		private long tickOffset;
		// number of ticks within the window
		private long numTicks;

		private int minLinesDisplayed = 8;
		private int maxLinesDisplayed = 32;

		private long minTicks = allTicks / 8;
		private long maxTicks = allTicks * 10;

		private float granularity = 1;

		public TickWindow(long tickOffset, long numTicks) {
			this.tickOffset = tickOffset;
			this.numTicks = numTicks;
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

		public long getMajorTickSpacing() {
			return (long) (minTicks / granularity);
		}

		public long getMajorTickToLeftOf(long tick) {
			return tick - tick % getMajorTickSpacing();
		}

		public long getPrimaryTickToLeftOf(long tick) {
			return tick - tick % (getMajorTickSpacing() * 4);
		}

		// adds a note starting at the nearest major tick (nearest displayed
		// grid line) to the left and ending one tick before the nearest major
		// tick to the right of the given tick
		// @param tick the tick somewhere in the middle of the desired range
		public MidiNote addMidiNote(long tick, int note) {
			long spacing = (long) (minTicks / granularity);
			long onTick = tick - tick % spacing;
			long offTick = onTick + spacing - 1;
			return midiManager.addNote(onTick, offTick, note, 80);
		}

		// translates the tickOffset to ensure that leftTick and rightTick are
		// in view
		// returns the amount translated
		// TODO: to fix select region prob, also update numTicks if need be,
		// i.e. fit everything!
		public void updateView(long leftTick, long rightTick) {
			// if we are dragging out of view, scroll appropriately
			// if the right is out but the left is in, just scroll
			if (leftTick < tickOffset && rightTick < tickOffset + numTicks) {
				setTickOffset(leftTick);

				// if the left is out but the right is in, just scroll
			} else if (rightTick > tickOffset + numTicks
					&& leftTick > tickOffset) {
				setTickOffset(rightTick - numTicks);

				// if both left and right are out of view, us
			} else if (leftTick <= tickOffset
					&& rightTick >= tickOffset + numTicks) {
				// setTickOffset(leftTick);
				// setNumTicks(rightTick - leftTick);
			}
		}
	}

	private static final int[] V_LINE_WIDTHS = new int[] { 5, 3, 2 };
	private static final float[] V_LINE_COLORS = new float[] { 0, .2f, .3f };

	// NIO Buffers
	private FloatBuffer[] vLineVB = new FloatBuffer[3];	
	private FloatBuffer hLineVB = null;
	private FloatBuffer recordRowVB = null;
	private FloatBuffer selectRegionVB = null;
	private FloatBuffer loopMarkerVB = null;
	private FloatBuffer loopMarkerLineVB = null;
	private FloatBuffer volumeBarsVB = null;

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

	// notes to display level info for in LEVEL_VIEW (volume, etc.)
	// (for multiple notes with same start tick, only one displays level info)
	private List<MidiNote> selectedLevelNotes = new ArrayList<MidiNote>();

	public enum State {
		LEVELS_VIEW, NORMAL_VIEW, TO_LEVELS_VIEW, TO_NORMAL_VIEW
	};

	private MidiNote tappedLevelNote = null;

	private TickWindow tickWindow;

	// the size of the "dots" at the top of level display
	private final int LEVEL_POINT_SIZE = 25;
	// the width of the lines for note levels
	private final int LEVEL_LINE_WIDTH = 7;
	// time (in millis) between taps before handling as a double-tap
	private final long DOUBLE_TAP_TIME = 300;

	// the main background color for the view.
	// this color can change when transitioning to/from LEVEL_VIEW
	private float bgColor = .5f;

	private long dragOffsetTick[] = new long[5];

	private long pinchLeftOffsetTick = 0;
	private long pinchRightOffsetTick = 0;

	private long zoomLeftAnchorTick = 0;
	private long zoomRightAnchorTick = 0;

	private long scrollAnchorTick = 0;
	private long scrollVelocity = 0;

	private boolean selectRegion = false;
	private long selectRegionStartTick = -1;
	private int selectRegionStartNote = -1;

	private long allTicks;

	private long lastDownTime = 0;
	private long lastTapTime = 0;

	private float lastTapX = -10;
	private float lastTapY = -10;

	// true when a note is being "pinched" (two-fingers touching the note)
	private boolean pinch = false;

	private boolean scrolling = false;
	private long scrollViewStartTime = 0;
	private long scrollViewEndTime = Long.MAX_VALUE;

	private boolean loopMarkerSelected = false;

	// set this to true after an event that can be undone (with undo btn)
	private boolean stateChanged = false;

	// this option can be set via a menu item.
	// if true, all midi note movements are rounded to the nearest major tick
	private boolean snapToGrid = false;

	private State viewState = State.NORMAL_VIEW;

	public MidiView(Context context, AttributeSet attrs) {
		super(context, attrs);
		for (int i = 0; i < 5; i++) {
			dragOffsetTick[i] = 0;
		}
	}

	public void setMidiManager(MidiManager midiManager) {
		this.midiManager = midiManager;
		allTicks = midiManager.RESOLUTION * 4;
		tickWindow = new TickWindow(0, allTicks - 1);
	}

	public void setRecordManager(RecordManager recorder) {
		this.recorder = recorder;
	}

	public void setPlaybackManager(PlaybackManager playbackManager) {
		this.playbackManager = playbackManager;
	}

	public State getViewState() {
		return viewState;
	}

	public void setViewState(State viewState) {
		if (viewState == State.TO_LEVELS_VIEW || viewState == State.TO_NORMAL_VIEW)
			return;
		this.viewState = viewState;
		if (viewState == State.LEVELS_VIEW)
			bgColor = 0;
		else
			bgColor = .5f;
	}
	
	public float currentBeatDivision() {
		// currently, granularity is relative to an eight note; x2 for quarter
		// note
		return tickWindow.granularity * 2;
	}

	public void reset() {
		tickWindow.setTickOffset(0);
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
			boolean noteCondition = topNote <= midiNote.getNoteValue()
					&& bottomNote >= midiNote.getNoteValue();

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
			if (midiNote.getNoteValue() == note && midiNote.getOnTick() <= tick
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

	private MidiNote selectLevelNote(float x, float y) {
		long tick = xToTick(x);
		long note = yToNote(y);

		for (MidiNote midiNote : midiManager.getMidiNotes()) {
			if (midiNote.getNoteValue() == note && midiNote.getOnTick() <= tick
					&& midiNote.getOffTick() >= tick) {
				addToSelectedLevelNotes(midiNote);
				return midiNote;
			}
		}
		return null;
	}

	private void selectVelocity(float x, float y, int pointerId) {
		for (MidiNote midiNote : selectedLevelNotes) {
			float velocityY = velocityToY(midiNote);
			if (Math.abs(tickToX(midiNote.getOnTick()) - x) < 35
					&& Math.abs(velocityY - y) < 35) {
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
		// distance between one primary (LONG) tick to the next
		float translateDist = tickWindow.getMajorTickSpacing() * 4f * width
				/ tickWindow.numTicks;
		// start at the first primary tick before display start
		float startX = tickToX(tickWindow
				.getPrimaryTickToLeftOf(tickWindow.tickOffset));
		// end at the first primary tick after display end
		float endX = tickToX(tickWindow
				.getPrimaryTickToLeftOf(tickWindow.tickOffset
						+ tickWindow.numTicks))
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
		gl.glColor4f(0, 0, 0, 1);
		gl.glLineWidth(6);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, loopMarkerVB);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, loopMarkerLineVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);
		gl.glPopMatrix();
	}

	private void drawRecordRowFill(boolean recording) {
		float recordRowColor = bgColor * 1.2f;
		gl.glColor4f(recordRowColor, recordRowColor, recordRowColor, 1f);
		if (recording)
			gl.glColor4f(1f, .6f, .6f, .7f);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, recordRowVB);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	}

	private void drawVolumeBars() {
		gl.glColor4f(.505f, .776f, 1, 1);
		gl.glLineWidth(LEVEL_LINE_WIDTH);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, volumeBarsVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, volumeBarsVB.capacity() / 2);
		// draw circles (big points) at top of bars
		gl.glPointSize(LEVEL_POINT_SIZE);
		gl.glEnable(GL10.GL_POINT_SMOOTH);
		for (int i = 0; i < volumeBarsVB.capacity() / 2; i += 2) {
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, volumeBarsVB);
			gl.glDrawArrays(GL10.GL_POINTS, i, 1);
		}
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
		boolean levelSelected = viewState != State.NORMAL_VIEW
				&& selectedLevelNotes.contains(midiNote);
		if (viewState == State.NORMAL_VIEW) {
			if (selected)
				// selected notes are fileed blue
				gl.glColor4f(0, 0, 1, 1);
			else
				// non-selected, non-recording notes are filled red
				gl.glColor4f(1, 0, 0, 1);
		} else {
			float blackToWhite = (1 - bgColor * 2);
			float whiteToBlack = bgColor * 2;
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

	}

	private void drawMidiNote(int note, long onTick, long offTick) {
		// midi note rectangle coordinates
		float x1 = tickToX(onTick);
		float x2 = tickToX(offTick);
		float y1 = (height * note) / midiManager.getNumSamples();
		float y2 = (height * (note + 1)) / midiManager.getNumSamples();
		// the float buffer for the midi note coordinates
		FloatBuffer midiBuf = makeFloatBuffer(new float[] { x1, y1, x2, y1, x1,
				y2, x2, y2 });
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, midiBuf);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

		// draw outline - same coordinates, but different ordering for a
		// line_loop instead of a triangle_strip
		FloatBuffer outlineBuf = makeFloatBuffer(new float[] { x1, y1, x2, y1,
				x2, y2, x1, y2, x1, y2 });
		float baseColor = (1 - bgColor * 2); // fade outline from black to white
		gl.glColor4f(baseColor, baseColor, baseColor, 1);
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

	private void initVLineVBs() {
		// height of the bottom of the record row
		float y1 = height / midiManager.getNumSamples();
		
		for (int i = 0; i < 3; i++) {
			// 4 vertices per line					
			float[] line = new float[4];
			line[0] = 0;
			line[1] = y1 - y1/(i + 2);
			line[2] = 0;
			line[3] = height;
			vLineVB[i] = makeFloatBuffer(line);
		}
	}

	private void initLoopMarkerVBs() {
		float h = height / midiManager.getNumSamples();
		float[] loopMarkerLine = new float[] { 0, h / 2 + 1, 0, height };
		float[] loopMarkerTriangle = new float[] { 0, h / 2, 0, h, -h / 2,
				3 * h / 4 };

		loopMarkerLineVB = makeFloatBuffer(loopMarkerLine);
		loopMarkerVB = makeFloatBuffer(loopMarkerTriangle);
	}

	private void initVolumeBarsVB() {
		float[] volumeBars = new float[selectedLevelNotes.size() * 4];
		for (int i = 0; i < selectedLevelNotes.size(); i++) {
			float x = tickToX(selectedLevelNotes.get(i).getOnTick());
			volumeBars[i * 4] = x;
			volumeBars[i * 4 + 1] = velocityToY(selectedLevelNotes.get(i));
			volumeBars[i * 4 + 2] = x;
			volumeBars[i * 4 + 3] = height;
		}

		volumeBarsVB = makeFloatBuffer(volumeBars);
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

	private float velocityToY(MidiNote midiNote) {
		return height - midiNote.getVelocity() * height / 100;
	}

	private int yToVelocity(float y) {
		return (int) (100 * (height - y) / height);
	}

	private boolean legalSelectedNoteMove(int noteDiff) {
		for (MidiNote midiNote : selectedNotes) {
			if (midiNote.getNoteValue() + noteDiff < 1
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
		tickWindow.updateGranularity();
		gl.glClearColor(bgColor, bgColor, bgColor, 1.0f);
		initHLineVB();
		initVLineVBs();
		initLoopMarkerVBs();
		initRecordRowVB();
	}

	public boolean toggleSnapToGrid() {
		snapToGrid = !snapToGrid;
		return snapToGrid;
	}

	@Override
	protected void drawFrame() {
		if (viewState == State.TO_LEVELS_VIEW
				|| viewState == State.TO_NORMAL_VIEW) {
			float amt = .5f / 30;
			bgColor = viewState == State.TO_LEVELS_VIEW ? bgColor - amt
					: bgColor + amt;
			gl.glClearColor(bgColor, bgColor, bgColor, 1.0f);
			if (bgColor >= .5f || bgColor <= 0f) {
				viewState = bgColor >= .5f ? State.NORMAL_VIEW
						: State.LEVELS_VIEW;
			}
		}

		boolean recording = recorder.getState() == RecordManager.State.LISTENING
				|| recorder.getState() == RecordManager.State.RECORDING;
		drawRecordRowFill(recording);
		if (recording
				|| playbackManager.getState() == PlaybackManager.State.PLAYING) {
			// if we're recording, keep the current recording tick in view.
			if (recording
					&& midiManager.getCurrTick() > tickWindow.tickOffset
							+ tickWindow.numTicks)
				tickWindow.setNumTicks(midiManager.getCurrTick()
						- tickWindow.tickOffset);
			drawCurrentTick();
		}
		tickWindow.scroll();
		if (viewState == State.LEVELS_VIEW) {
			drawAllMidiNotes();
			initVolumeBarsVB();
			drawVolumeBars();
		} else {
			drawHorizontalLines();
			drawVerticalLines();
			drawLoopMarker();
			drawAllMidiNotes();
		}
		drawSelectRegion();
		if (scrolling
				|| scrollVelocity != 0
				|| Math.abs(System.currentTimeMillis() - scrollViewEndTime) <= DOUBLE_TAP_TIME * 2) {
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
		if (!scrollingEnded && elapsedTime <= DOUBLE_TAP_TIME)
			alpha *= elapsedTime / (float) DOUBLE_TAP_TIME;
		else if (scrollingEnded && elapsedTime > DOUBLE_TAP_TIME)
			alpha *= (DOUBLE_TAP_TIME * 2 - elapsedTime)
					/ (float) DOUBLE_TAP_TIME;

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
		if (startOnTicks.containsKey(pointerId)) {
			// drag only if the note is moved a certain distance
			// from its start tick this prevents minute finger
			// movements from moving/quantizing notes and also
			// indicates a "sticking point" of the initial note position
			if (Math.abs(startOnTicks.get(pointerId) - midiNote.getOnTick())
					+ Math.abs(tickDiff) > 10) {
				midiNote.setOnTick(midiNote.getOnTick() + tickDiff);
				midiNote.setOffTick(midiNote.getOffTick() + tickDiff);
				if (snapToGrid) // quantize if snapToGrid mode is on
					midiManager.quantize(midiNote, currentBeatDivision());
			} else { // inside threshold distance - set to original position
				midiNote.setOffTick(startOnTicks.get(pointerId)
						+ midiNote.getOffTick() - midiNote.getOnTick());
				midiNote.setOnTick(startOnTicks.get(pointerId));
			}
		}
		stateChanged = true;
		return midiNote.getOnTick() - beforeTick;
	}

	private void pinchNote(MidiNote midiNote, long onTickDiff, long offTickDiff) {
		if (snapToGrid) {
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
			if (midiNote.getOffTick() + offTickDiff <= tickWindow.maxTicks)
				midiNote.setOffTick(midiNote.getOffTick() + offTickDiff);
		}
		stateChanged = true;
	}

	private void handleActionUp(MotionEvent e) {
		long time = System.currentTimeMillis();
		if (time - lastDownTime < 200) {
			// if the second tap is not in the same location as the first tap,
			// no double tap :(
			if (time - lastTapTime < DOUBLE_TAP_TIME
					&& Math.abs(e.getX() - lastTapX) <= 25
					&& yToNote(e.getY()) == yToNote(lastTapY)) {
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
		if (viewState == State.LEVELS_VIEW) {
			tappedLevelNote = selectLevelNote(lastTapX, lastTapY);
		} else {
			selectedNotes.clear();
			if (touchedNote != null) {
				selectedNotes.add(touchedNote);
			}
		}
	}

	private void doubleTap(MotionEvent e, MidiNote touchedNote) {
		if (viewState == State.LEVELS_VIEW) {
			if (tappedLevelNote == null)
				return;
			if (selectedLevelNotes.contains(tappedLevelNote))
				selectedLevelNotes.remove(tappedLevelNote);
			if (selectedNotes.contains(tappedLevelNote))
				selectedNotes.remove(tappedLevelNote);
			midiManager.removeNote(tappedLevelNote);
			updateSelectedLevelNotes();
			stateChanged = true;
			return;
		}
		long tick = xToTick(e.getX());
		int note = yToNote(e.getY());
		if (touchedNote != null) {
			if (selectedNotes.contains(touchedNote)) {
				selectedNotes.remove(touchedNote);
			}
			midiManager.removeNote(touchedNote);
			stateChanged = true;
		} else {
			// add a note based on the current tick granularity
			if (note > 0) {// can't add note on record track
				MidiNote noteToAdd = tickWindow.addMidiNote(tick, note);
				selectedNotes.add(noteToAdd);
				handleMidiCollisions();
				selectedNotes.remove(noteToAdd);
				stateChanged = true;
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

	private void startScrollView() {
		long now = System.currentTimeMillis();
		if (now - scrollViewEndTime > DOUBLE_TAP_TIME * 2)
			scrollViewStartTime = now;
		else
			scrollViewEndTime = Long.MAX_VALUE;
		scrolling = true;
	}

	public void toggleLevelsView() {
		updateSelectedLevelNotes();
		if (viewState == State.NORMAL_VIEW || viewState == State.TO_NORMAL_VIEW) {
			viewState = State.TO_LEVELS_VIEW;
		} else {
			viewState = State.TO_NORMAL_VIEW;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			lastDownTime = System.currentTimeMillis();
			startScrollView();
			int id = e.getPointerId(0);
			if (viewState == State.LEVELS_VIEW) {
				selectVelocity(e.getX(), e.getY(), id);
			} else {
				selectMidiNote(e.getX(0), e.getY(0), id);
			}
			if (touchedNotes.get(id) == null) {
				long tick = xToTick(e.getX(0));
				int note = yToNote(e.getY(0));
				// no note selected. if this is a double-tap-hold (if there was
				// a recent single tap),
				// start a selection region.
				if (System.currentTimeMillis() - lastTapTime < DOUBLE_TAP_TIME
						&& Math.abs(lastTapX - e.getX(0)) < 20
						&& note == yToNote(lastTapY)) {
					selectRegionStartTick = tick;
					selectRegionStartNote = note;
					selectRegionVB = null;
					selectRegion = true;
				} else {
					// check if loop marker selected
					float loopX = tickToX(midiManager.getLoopTick());
					if (note == 0 && e.getX(0) >= loopX - 20
							&& e.getX(0) <= loopX + 20) {
						loopMarkerSelected = true;
					} else
						// otherwise, enable scrolling
						scrollAnchorTick = tick;
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			// lastDownTime = System.currentTimeMillis();
			int index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if (viewState == State.LEVELS_VIEW) {
				selectVelocity(e.getX(index), e.getY(index),
						e.getPointerId(index));
			} else {
				selectMidiNote(e.getX(index), e.getY(index),
						e.getPointerId(index));
			}
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
				if (viewState != State.LEVELS_VIEW && pinch) {
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
			if (viewState == State.LEVELS_VIEW) {
				if (!touchedNotes.isEmpty())
					for (int i = 0; i < e.getPointerCount(); i++) {
						id = e.getPointerId(i);
						MidiNote touchedNote = touchedNotes.get(id);
						if (touchedNote == null)
							continue;
						touchedNote.setVelocity(yToVelocity(e.getY(i)));
						// velocity changes are valid undo events
						stateChanged = true;
					}
				else { // no midi selected. scroll, zoom, or update select
						// region
					if (e.getPointerCount() == 1) {
						scrollVelocity = tickWindow.scroll(e.getX(0));
					} else if (e.getPointerCount() == 2) {
						// two finger zoom
						float leftX = Math.min(e.getX(0), e.getX(1));
						float rightX = Math.max(e.getX(0), e.getX(1));
						tickWindow.zoom(leftX, rightX);
					}
				}
				return true;
			}

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
					int noteDiff = newNote - touchedNote.getNoteValue();
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
								midiNote.setNote(midiNote.getNoteValue()
										+ noteDiff);
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
					// dragOffsetTick[id] += translate;
				}
				// handle any overlapping notes (clip or delete notes as
				// appropriate)
				handleMidiCollisions();
			} else { // no midi selected. scroll, zoom, or update select region
				if (e.getPointerCount() == 1) {
					if (selectRegion) { // update select region
						selectWithRegion(e.getX(0), e.getY(0));
					} else if (loopMarkerSelected) {
						midiManager.setLoopTick(tickWindow
								.getMajorTickToLeftOf(xToTick(e.getX(0))));
					} else { // one finger scroll
						scrollVelocity = tickWindow.scroll(e.getX(0));
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
			loopMarkerSelected = false;
			if (scrollVelocity == 0)
				scrollViewEndTime = System.currentTimeMillis();
			selectRegion = false;
			handleActionUp(e);
			if (viewState != State.LEVELS_VIEW) {
				midiManager.mergeTempNotes();
				startOnTicks.clear();
			}
			touchedNotes.clear();
			if (stateChanged)
				midiManager.saveState();
			stateChanged = false;
			break;
		}
		return true;
	}

	public void writeToBundle(Bundle out) {
		ArrayList<Integer> selectedIndices = new ArrayList<Integer>();
		for (int i = 0; i < selectedNotes.size(); i++) {
			int index = midiManager.getMidiNotes().indexOf(selectedNotes.get(i)); 
			if (index != -1)
				selectedIndices.add(index);
		}
		out.putIntegerArrayList("selectedIndices", selectedIndices);
		ArrayList<Integer> selectedLevelIndices = new ArrayList<Integer>();
		for (int i = 0; i < selectedLevelNotes.size(); i++) {
			int index = midiManager.getMidiNotes().indexOf(selectedLevelNotes.get(i)); 
			if (index != -1)
				selectedLevelIndices.add(index);
		}		
		out.putIntegerArrayList("selectedLevelIndices", selectedLevelIndices);
		out.putInt("viewState", viewState.ordinal());
	}

	// use constructor first, and set the deets with this method
	public void readFromBundle(Bundle in) {
		ArrayList<Integer> selectedIndices = in.getIntegerArrayList("selectedIndices");
		for (int index : selectedIndices) {
			selectedNotes.add(midiManager.getMidiNotes().get(index));
		}		
		ArrayList<Integer> selectedLevelIndices = in.getIntegerArrayList("selectedLevelIndices");
		for (int index : selectedLevelIndices) {
			selectedLevelNotes.add(midiManager.getMidiNotes().get(index));
		}
		setViewState(State.values()[in.getInt("viewState")]);			
	}
}