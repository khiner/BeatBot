package com.kh.beatbot.view.bean;

import com.kh.beatbot.view.MidiView;

public class MidiViewBean {

	// the size of the "dots" at the top of level display
	public static final int LEVEL_POINT_SIZE = 25;
	// the width of the lines for note levels
	public static final int LEVEL_LINE_WIDTH = 7;
	// time (in millis) between taps before handling as a double-tap
	public final static long DOUBLE_TAP_TIME = 300;

	public final static float MIDI_VIEW_DEFAULT_BG_COLOR = .5f;
	
	// RGB color to draw main midi grid lines
	public final static float[] GRID_LINE_COLOR = {0, 0, 0, 1};
		
	// RGB color to draw waveform lines in midi view
	public final static float[] WAVEFORM_COLOR = {0, 0, 0, 1};
	
	// RGB color for non-selected notes
	public final static float[] NOTE_COLOR = {1, 0, 0, 1};	
	// RGB color for selected notes
	public final static float[] NOTE_SELECTED_COLOR = {0, 0, 1, 1};
	
	// RGB color for volume bars
	public final static float[] VOLUME_COLOR = {.412f, .788f, 1, 1};
	// RGB color for pan bars	
	public final static float[] PAN_COLOR = {1, .788f, .392f, 1};
	// RGB color for pitch bars
	public final static float[] PITCH_COLOR = {.443f, 1, .533f, 1};

	// RGB color for selected bars
	public final static float[] LEVEL_SELECTED_COLOR = {.9f, 0, .1f, 1};	
	public final static float[] TICK_FILL_COLOR = {.3f, .3f, .3f, 1};
	public final static float[] TICK_MARKER_COLOR = {.8f, .8f, .8f, 1};
	public final static float[] TICKBAR_COLOR = {.6f, .6f, .6f, 1};
	public final static float[] TICK_SELECTED_COLOR = VOLUME_COLOR;
		
	private MidiView.State viewState = MidiView.State.NORMAL_VIEW;
	
	private float width, height;
	private float yOffset;
	private float noteHeight;
	private float midiHeight;
	private float levelsHeight;
	// the main background color for the view.
	// this color can change when transitioning to/from LEVEL_VIEW
	private float bgColor = MIDI_VIEW_DEFAULT_BG_COLOR;

	private long dragOffsetTick[] = new long[5];

	private int pinchLeftPointerId = -1;
	private int pinchRightPointerId = -1;
	private long pinchLeftOffset = 0;
	private long pinchRightOffset = 0;
	private long zoomLeftAnchorTick = 0;
	private long zoomRightAnchorTick = 0;

	private int scrollPointerId = -1;
	private long scrollAnchorTick = 0;

	private boolean selectRegion = false;
	private long selectRegionStartTick = -1;
	private float selectRegionStartY = -1;

	private long allTicks;

	private long lastDownTime = 0;
	private long lastTapTime = 0;

	private float lastTapX = -10;
	private float lastTapY = -10;

	// true when a note is being "pinched" (two-fingers touching the note)
	private boolean pinch = false;

	private int[] loopPointerIds = {-1, -1, -1};
	private float loopSelectionOffset = 0;

	// set this to true after an event that can be undone (with undo btn)
	private boolean stateChanged = false;

	// this option can be set via a menu item.
	// if true, all midi note movements are rounded to the nearest major tick
	private boolean snapToGrid = true;

	private int animateCount = 0;

	public MidiView.State getViewState() {
		return viewState;
	}

	public void setViewState(MidiView.State viewState) {
		this.viewState = viewState;
	}
	
	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float getMidiHeight() {
		return midiHeight;
	}

	public void setMidiHeight(float midiHeight) {
		this.midiHeight = midiHeight;
	}

	public float getYOffset() {
		return yOffset;
	}

	public void setYOffset(float yOffset) {
		this.yOffset = yOffset;
	}

	public float getNoteHeight() {
		return noteHeight;
	}

	public void setNoteHeight(float noteHeight) {
		this.noteHeight = noteHeight;
	}

	public float getLevelsHeight() {
		return levelsHeight;
	}

	public void setLevelsHeight(float levelsHeight) {
		this.levelsHeight = levelsHeight;
	}

	public float getBgColor() {
		return bgColor;
	}

	public void setBgColor(float bgColor) {
		this.bgColor = bgColor;
	}

	public long getDragOffsetTick(int i) {
		return dragOffsetTick[i];
	}

	public void setDragOffsetTick(int i, long dragOffset) {
		dragOffsetTick[i] = dragOffset;
	}

	public int getPinchLeftPointerId() {
		return pinchLeftPointerId;
	}

	public void setPinchLeftPointerId(int pinchLeftPointerId) {
		this.pinchLeftPointerId = pinchLeftPointerId;
	}

	public int getPinchRightPointerId() {
		return pinchRightPointerId;
	}

	public void setPinchRightPointerId(int pinchRightPointerId) {
		this.pinchRightPointerId = pinchRightPointerId;
	}
	
	public long getPinchLeftOffset() {
		return pinchLeftOffset;
	}

	public void setPinchLeftOffset(long pinchLeftOffset) {
		this.pinchLeftOffset = pinchLeftOffset;
	}

	public long getPinchRightOffset() {
		return pinchRightOffset;
	}

	public void setPinchRightOffset(long pinchRightOffset) {
		this.pinchRightOffset = pinchRightOffset;
	}

	public long getZoomLeftAnchorTick() {
		return zoomLeftAnchorTick;
	}

	public void setZoomLeftAnchorTick(long zoomLeftAnchorTick) {
		this.zoomLeftAnchorTick = zoomLeftAnchorTick;
	}

	public long getZoomRightAnchorTick() {
		return zoomRightAnchorTick;
	}

	public void setZoomRightAnchorTick(long zoomRightAnchorTick) {
		this.zoomRightAnchorTick = zoomRightAnchorTick;
	}

	public long getScrollAnchorTick() {
		return scrollAnchorTick;
	}

	public void setScrollPointerId(int scrollPointerId) {
		this.scrollPointerId = scrollPointerId;
	}
	
	public int getScrollPointerId() {
		return scrollPointerId;
	}
	
	public void setScrollAnchorTick(long scrollAnchorTick) {
		this.scrollAnchorTick = scrollAnchorTick;
	}

	public boolean isSelectRegion() {
		return selectRegion;
	}

	public void setSelectRegion(boolean selectRegion) {
		this.selectRegion = selectRegion;
	}

	public long getSelectRegionStartTick() {
		return selectRegionStartTick;
	}

	public void setSelectRegionStartTick(long selectRegionStartTick) {
		this.selectRegionStartTick = selectRegionStartTick;
	}

	public float getSelectRegionStartY() {
		return selectRegionStartY;
	}

	public void setSelectRegionStartY(float selectRegionStartY) {
		this.selectRegionStartY = selectRegionStartY;
	}

	public long getAllTicks() {
		return allTicks;
	}

	public void setAllTicks(long allTicks) {
		this.allTicks = allTicks;
	}

	public long getLastDownTime() {
		return lastDownTime;
	}

	public void setLastDownTime(long lastDownTime) {
		this.lastDownTime = lastDownTime;
	}

	public long getLastTapTime() {
		return lastTapTime;
	}

	public void setLastTapTime(long lastTapTime) {
		this.lastTapTime = lastTapTime;
	}

	public float getLastTapX() {
		return lastTapX;
	}

	public void setLastTapX(float lastTapX) {
		this.lastTapX = lastTapX;
	}

	public float getLastTapY() {
		return lastTapY;
	}

	public void setLastTapY(float lastTapY) {
		this.lastTapY = lastTapY;
	}

	public boolean isPinch() {
		return pinch;
	}

	public void setPinch(boolean pinch) {
		this.pinch = pinch;
	}

	public int[] getLoopPointerIds() {
		return loopPointerIds;
	}
	
	public void setLoopPointerId(int num, int id) {
		if ((id != -1 && loopPointerIds[1] != -1 || num == 1 && (loopPointerIds[0] != -1 || loopPointerIds[2] != -1)))
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
	
	public float getLoopSelectionOffset() {
		return loopSelectionOffset;
	}
	
	public void setLoopSelectionOffset(float loopSelectionOffset) {
		this.loopSelectionOffset = loopSelectionOffset;
	}
	
	public boolean isStateChanged() {
		return stateChanged;
	}

	public void setStateChanged(boolean stateChanged) {
		this.stateChanged = stateChanged;
	}

	public boolean isSnapToGrid() {
		return snapToGrid;
	}
	
	public boolean toggleSnapToGrid() {
		snapToGrid = !snapToGrid;
		return snapToGrid;
	}
	
	public int getAnimateCount() {
		return animateCount;
	}

	public void incrementAnimateCount() {
		animateCount++;
	}
}
