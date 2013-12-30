package com.kh.beatbot.ui.view.helper;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.MidiView;
import com.kh.beatbot.ui.view.View;

public class TickWindowHelper {

	private static MidiView midiView = null;

	public static final int NUM_VERTICAL_LINE_SETS = 8;
	public static final int MIN_LINES_DISPLAYED = 8;
	public static final int MAX_LINES_DISPLAYED = 32;

	public static float scrollAnchorTick = 0, scrollAnchorY = 0;
			
	// leftmost tick to display
	private static float currTickOffset = 0;
	private static float currYOffset = 0;

	// current number of ticks within the window
	private static float currNumTicks = MidiManager.TICKS_IN_ONE_MEASURE;

	// number of quarter notes per beat division - i.e. the "current grid granularity"
	// can be < 1
	private static float currNumQuarterNotes = 1;

	private static FloatBuffer[] vLineVbs = new FloatBuffer[NUM_VERTICAL_LINE_SETS];

	public static void drawVerticalLines() {
		for (int i = 0; i < NUM_VERTICAL_LINE_SETS; i++) {
			if (1 << i > currNumQuarterNotes * 4)
				break; // break when lines go below current granulariy
			View.drawLines(vLineVbs[i], Colors.MIDI_LINES[i], 2, GL10.GL_LINES);
		}
	}
	
	public static float getNumTicks() {
		return currNumTicks;
	}

	public static float getTickOffset() {
		return currTickOffset;
	}

	public static float getYOffset() {
		return currYOffset;
	}

	public static void init(MidiView _midiView) {
		midiView = _midiView;
		initVLineVbs();
		updateGranularity();
	}

	public static void zoom(float leftX, float rightX, float ZLAT, float ZRAT) {
		leftX = leftX == 0 ? 1 : leftX; // avoid divide by zero
		float newTickOffset = (ZRAT * leftX - ZLAT * rightX) / (leftX - rightX);
		float newNumTicks = (ZLAT - newTickOffset) * midiView.width / leftX;

		if (newTickOffset < 0) {
			currTickOffset = 0;
			currNumTicks = ZRAT * midiView.width / rightX;
			currNumTicks = currNumTicks <= MidiManager.MAX_TICKS ? (currNumTicks >= MidiManager.MIN_TICKS ? currNumTicks : MidiManager.MIN_TICKS) : MidiManager.MAX_TICKS;
		} else if (newNumTicks > MidiManager.MAX_TICKS) {
			currTickOffset = newTickOffset;
			currNumTicks = MidiManager.MAX_TICKS - currTickOffset;
		} else if (newNumTicks < MidiManager.MIN_TICKS) {
			currTickOffset = newTickOffset;
			currNumTicks = MidiManager.MIN_TICKS;
		} else if (newTickOffset + newNumTicks > MidiManager.MAX_TICKS) {
			currNumTicks = ((ZLAT - MidiManager.MAX_TICKS) * midiView.width)
					/ (leftX - midiView.width);
			currTickOffset = MidiManager.MAX_TICKS - currNumTicks;
		} else {
			currTickOffset = newTickOffset;
			currNumTicks = newNumTicks;
		}
		updateGranularity();
	}

	public static void scroll() {
		if (ScrollBarHelper.scrolling) {
			return;
		}
		if (ScrollBarHelper.scrollXVelocity != 0) {
			ScrollBarHelper.tickScrollVelocity();
			setTickOffset(currTickOffset + ScrollBarHelper.scrollXVelocity);
		}
		if (ScrollBarHelper.scrollYVelocity != 0) {
			setYOffset(currYOffset + ScrollBarHelper.scrollYVelocity);
		}
	}

	public static void scroll(float x, float y) {
		float newTickOffset = scrollAnchorTick - currNumTicks * x
				/ midiView.width;
		float newYOffset = scrollAnchorY - y;
		ScrollBarHelper.scrollXVelocity = newTickOffset - currTickOffset;
		ScrollBarHelper.scrollYVelocity = newYOffset - currYOffset;
		setTickOffset(newTickOffset);
		setYOffset(newYOffset);
	}

	public static void updateGranularity() {
		// x-coord width of one quarter note
		float spacing = (MidiManager.TICKS_IN_ONE_MEASURE * midiView.width)
				/ (currNumTicks * 8);
		// after algebra, this condition says: if more than maxLines will
		// display, reduce the granularity by one half, else if less than
		// maxLines will display, increase the granularity by one half
		// so that (minLinesDisplayed <= lines-displayed <=
		// maxLinesDisplayed) at all times
		if ((MAX_LINES_DISPLAYED * spacing * 2) / currNumQuarterNotes < midiView.width)
			currNumQuarterNotes /= 2;
		else if ((MIN_LINES_DISPLAYED * spacing * 2) / currNumQuarterNotes > midiView.width)
			currNumQuarterNotes *= 2;
		MidiManager.currBeatDivision = currNumQuarterNotes;
	}

	public static void setTickOffset(float tickOffset) {
		if (tickOffset < 0) {
			currTickOffset = 0;
		} else if (tickOffset + currNumTicks > MidiManager.MAX_TICKS) {
			currTickOffset = MidiManager.MAX_TICKS - currNumTicks;
		} else {
			currTickOffset = tickOffset;
		}
	}

	public static void setYOffset(float yOffset) {
		if (yOffset < 0) {
			currYOffset = 0;
		} else if (yOffset + midiView.getMidiHeight() > midiView.getTotalTrackHeight()) {
			currYOffset = midiView.getTotalTrackHeight() - midiView.getMidiHeight();
		} else {
			currYOffset = yOffset;
		}
	}

	public static void setNumTicks(float numTicks) {
		if (numTicks > MidiManager.MAX_TICKS || numTicks < MidiManager.MIN_TICKS) {
			return;
		}
		currNumTicks = numTicks;
		updateGranularity();
	}

	public static float getCurrentBeatDivision() {
		// granularity is relative to one quarter note
		return currNumQuarterNotes;
	}

	public static float getMajorTickSpacing() {
		return MidiManager.TICKS_IN_ONE_MEASURE / (currNumQuarterNotes * 2);
	}

	public static float getMajorTickNearestTo(float tick) {
		float spacing = getMajorTickSpacing(); 
		if (tick % spacing > spacing / 2) {
			return tick + spacing - tick % spacing;
		} else {
			return tick - tick % spacing;
		}
	}
	
	public static float getMajorTickToLeftOf(float tick) {
		return tick - tick % getMajorTickSpacing();
	}

	public static void updateView(float tick) {
		// if we are dragging out of view, scroll appropriately
		if (tick < currTickOffset) {
			setTickOffset(tick);
		} else if (tick > currTickOffset + currNumTicks) {
			setTickOffset(tick - currNumTicks);
		}
	}
	
	public static void updateView(float leftTick, float rightTick) {
		// if we are dragging out of view, scroll appropriately
		if (leftTick <= currTickOffset && rightTick >= currTickOffset + currNumTicks) {
			setTickOffset(leftTick);
			setNumTicks(rightTick - leftTick);
		} else if (leftTick < currTickOffset) {
			setTickOffset(leftTick);
		} else if (rightTick > currTickOffset + currNumTicks) {
			setTickOffset(rightTick - currNumTicks);
		}
	}

	// translates the tickOffset to ensure that leftTick, rightTick, topY and
	// bottomY are all in view
	public static void updateView(float tick, float topY,
			float bottomY) {
		updateView(tick);
		if (topY < currYOffset
				&& bottomY < currYOffset + midiView.getMidiHeight()) {
			setYOffset(topY);
		} else if (bottomY > currYOffset + midiView.getMidiHeight()
				&& topY > currYOffset) {
			setYOffset(bottomY - midiView.getMidiHeight());
		}
	}

	public static void initVLineVbs() {
		List<List<Float>> allVertices = new ArrayList<List<Float>>();
		
		long minTickSpacing = (long)(MidiManager.MIN_TICKS / 8);
		long[] tickSpacings = new long[NUM_VERTICAL_LINE_SETS];
		for (int i = 0; i < NUM_VERTICAL_LINE_SETS; i++) {
			allVertices.add(new ArrayList<Float>());
			tickSpacings[i] = minTickSpacing * (1 << (NUM_VERTICAL_LINE_SETS - i - 1));
		}
		for (long currTick = 0; currTick < MidiManager.MAX_TICKS; currTick += minTickSpacing) {
			for (int i = 0; i < NUM_VERTICAL_LINE_SETS; i++) {
				if (currTick % tickSpacings[i] == 0) {
					float x = midiView.tickToUnscaledX(currTick);
					allVertices.get(i).add(x);
					allVertices.get(i).add(MidiView.Y_OFFSET);
					allVertices.get(i).add(x);
					allVertices.get(i).add(MidiView.Y_OFFSET + midiView.getTotalTrackHeight());
					break; // each line goes in only ONE line set
				}
			}
		}
		// convert the list of float lists into a list of FloatBuffers
		for (int i = 0; i < allVertices.size(); i++) {
			vLineVbs[i] = MidiView.makeFloatBuffer(allVertices.get(i));
		}
	}
}
