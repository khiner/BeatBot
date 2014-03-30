package com.kh.beatbot.ui.view.helper;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.Line;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.view.MidiView;
import com.kh.beatbot.ui.view.TouchableView.Position;

public class TickWindowHelper {

	private static MidiView midiView = null;

	public static final int NUM_VERTICAL_LINE_SETS = 8, MIN_LINES_DISPLAYED = 8,
			MAX_LINES_DISPLAYED = 32;

	public static int scrollPointerId = -1;
	private static float scrollAnchorTick = 0, scrollAnchorY = 0, currTickOffset = 0,
			currYOffset = 0, currNumTicks = MidiManager.TICKS_PER_MEASURE;

	private static List<Line>[] vLines = (List<Line>[]) new ArrayList[NUM_VERTICAL_LINE_SETS];

	public static float getNumTicks() {
		return currNumTicks;
	}

	public static float getTickOffset() {
		return currTickOffset;
	}

	public static float getYOffset() {
		return currYOffset;
	}

	public static void init(MidiView _midiView, ShapeGroup shapeGroup) {
		midiView = _midiView;
		createVLines(shapeGroup);
		updateGranularity();
	}

	public static void pointerUp(int id) {
		if (id == scrollPointerId) {
			scrollPointerId = -1;
		}
	}

	public static void setScrollAnchor(int id, float tick) {
		setScrollAnchor(id, tick, scrollAnchorY);
	}

	public static void setScrollAnchor(int id, float tick, float y) {
		scrollPointerId = id;
		scrollAnchorTick = tick;
		scrollAnchorY = y;
	}

	public static void zoom(float leftX, float rightX, float ZLAT, float ZRAT) {
		leftX = leftX == 0 ? 1 : leftX; // avoid divide by zero
		float newTickOffset = (ZRAT * leftX - ZLAT * rightX) / (leftX - rightX);
		float newNumTicks = (ZLAT - newTickOffset) * midiView.width / leftX;

		if (newTickOffset < 0) {
			currTickOffset = 0;
			currNumTicks = ZRAT * midiView.width / rightX;
			currNumTicks = currNumTicks <= MidiManager.MAX_TICKS ? (currNumTicks >= MidiManager.MIN_TICKS ? currNumTicks
					: MidiManager.MIN_TICKS)
					: MidiManager.MAX_TICKS;
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

	public static void scroll(Position pos) {
		float newTickOffset = scrollAnchorTick - currNumTicks * pos.x / midiView.width;
		float newYOffset = scrollAnchorY - pos.y;
		ScrollBarHelper.scrollXVelocity = newTickOffset - currTickOffset;
		ScrollBarHelper.scrollYVelocity = newYOffset - currYOffset;
		setTickOffset(newTickOffset);
		setYOffset(newYOffset);
	}

	public static void scroll() {
		if (!ScrollBarHelper.scrolling) {
			if (ScrollBarHelper.scrollXVelocity != 0) {
				ScrollBarHelper.tickScrollVelocity();
				setTickOffset(currTickOffset + ScrollBarHelper.scrollXVelocity);
			}
			if (ScrollBarHelper.scrollYVelocity != 0) {
				setYOffset(currYOffset + ScrollBarHelper.scrollYVelocity);
			}
		}
	}

	public static void setTickOffset(float tickOffset) {
		currTickOffset = GeneralUtils.clipTo(tickOffset, 0, MidiManager.MAX_TICKS - currNumTicks);
		midiView.notifyScrollX();
	}

	public synchronized static void setHeight(float height) {
		for (List<Line> lines : vLines) {
			for (Line line : lines) {
				line.setDimensions(2, height);
			}
		}
	}

	public static void setYOffset(float yOffset) {
		currYOffset = GeneralUtils.clipTo(yOffset, 0,
				midiView.getTotalTrackHeight() - midiView.getMidiHeight());
		midiView.notifyScrollY();
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
	public static void updateView(float tick, float topY, float bottomY) {
		updateView(tick);
		if (topY < currYOffset && bottomY < currYOffset + midiView.getMidiHeight()) {
			setYOffset(topY);
		} else if (bottomY > currYOffset + midiView.getMidiHeight() && topY > currYOffset) {
			setYOffset(bottomY - midiView.getMidiHeight());
		}
	}

	private static void setNumTicks(float numTicks) {
		if (numTicks > MidiManager.MAX_TICKS || numTicks < MidiManager.MIN_TICKS) {
			return;
		}
		currNumTicks = numTicks;
		updateGranularity();
	}

	private static void updateGranularity() {
		// x-coord width of one quarter note
		float spacing = (MidiManager.TICKS_PER_NOTE * midiView.width) / currNumTicks;
		// after algebra, this condition says: if more than maxLines will display, reduce the
		// granularity by one half, else if less than maxLines will display, increase the
		// granularity by one half so that (minLinesDisplayed <= lines-displayed <=
		// maxLinesDisplayed) at all times
		if (MAX_LINES_DISPLAYED * spacing / MidiManager.getBeatDivision() < midiView.width) {
			MidiManager.decreaseBeatDivision();
		} else if (MIN_LINES_DISPLAYED * spacing / MidiManager.getBeatDivision() > midiView.width) {
			MidiManager.increaseBeatDivision();
		}
		updateVerticalLineColors();
	}

	private synchronized static void createVLines(ShapeGroup shapeGroup) {
		long minTickSpacing = (long) (MidiManager.MIN_TICKS / 8);
		long[] tickSpacings = new long[NUM_VERTICAL_LINE_SETS];
		for (int i = 0; i < NUM_VERTICAL_LINE_SETS; i++) {
			tickSpacings[i] = minTickSpacing * (1 << (NUM_VERTICAL_LINE_SETS - i - 1));
			vLines[i] = new ArrayList<Line>();
		}
		for (long currTick = 0; currTick < MidiManager.MAX_TICKS; currTick += minTickSpacing) {
			for (int i = 0; i < NUM_VERTICAL_LINE_SETS; i++) {
				if (currTick % tickSpacings[i] == 0) {
					float x = midiView.tickToUnscaledX(currTick);
					Line line = new Line(shapeGroup, null, Color.TRANSPARENT);
					line.layout(x, MidiView.Y_OFFSET, 2, 1);
					vLines[i].add(line);
					break; // each line goes in only ONE line set
				}
			}
		}
	}

	private synchronized static void updateVerticalLineColors() {
		for (int i = 0; i < NUM_VERTICAL_LINE_SETS; i++) {
			if (1 << i > MidiManager.getBeatDivision() * MidiManager.NOTES_PER_MEASURE) {
				// lines are invisible below current granulariy
				for (Line line : vLines[i]) {
					if (!line.getStrokeColor().equals(Color.TRANSPARENT))
						line.setStrokeColor(Color.TRANSPARENT);
				}
			} else {
				for (Line line : vLines[i]) {
					if (!line.getStrokeColor().equals(Color.MIDI_LINES[i]))
						line.setStrokeColor(Color.MIDI_LINES[i]);
				}
			}
		}
	}
}
