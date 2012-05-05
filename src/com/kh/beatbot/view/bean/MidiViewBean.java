package com.kh.beatbot.view.bean;

public class MidiViewBean {

	// the size of the "dots" at the top of level display
	public static final int LEVEL_POINT_SIZE = 25;
	// the width of the lines for note levels
	public static final int LEVEL_LINE_WIDTH = 7;
	// time (in millis) between taps before handling as a double-tap
	public final static long DOUBLE_TAP_TIME = 300;

	// RGB color for volume bars
	public final static float VOLUME_R = .412f;
	public final static float VOLUME_G = .788f;
	public final static float VOLUME_B = 1;

	// RGB color for pan bars	
	public final static float PAN_R = 1;
	public final static float PAN_G = .788f;
	public final static float PAN_B = .392f;

	// RGB color for pitch bars		
	public final static float PITCH_R = .443f;
	public final static float PITCH_G = 1;
	public final static float PITCH_B = .533f;
	
	private float width, height;
	private float yOffset;
	private float noteHeight;
	private float midiHeight;
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

	public long getPinchLeftOffsetTick() {
		return pinchLeftOffsetTick;
	}

	public void setPinchLeftOffsetTick(long pinchLeftOffsetTick) {
		this.pinchLeftOffsetTick = pinchLeftOffsetTick;
	}

	public long getPinchRightOffsetTick() {
		return pinchRightOffsetTick;
	}

	public void setPinchRightOffsetTick(long pinchRightOffsetTick) {
		this.pinchRightOffsetTick = pinchRightOffsetTick;
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

	public void setScrollAnchorTick(long scrollAnchorTick) {
		this.scrollAnchorTick = scrollAnchorTick;
	}

	public long getScrollVelocity() {
		return scrollVelocity;
	}

	public void setScrollVelocity(long scrollVelocity) {
		this.scrollVelocity = scrollVelocity;
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

	public int getSelectRegionStartNote() {
		return selectRegionStartNote;
	}

	public void setSelectRegionStartNote(int selectRegionStartNote) {
		this.selectRegionStartNote = selectRegionStartNote;
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

	public boolean isScrolling() {
		return scrolling;
	}

	public void setScrolling(boolean scrolling) {
		this.scrolling = scrolling;
	}

	public long getScrollViewStartTime() {
		return scrollViewStartTime;
	}

	public void setScrollViewStartTime(long scrollViewStartTime) {
		this.scrollViewStartTime = scrollViewStartTime;
	}

	public long getScrollViewEndTime() {
		return scrollViewEndTime;
	}

	public void setScrollViewEndTime(long scrollViewEndTime) {
		this.scrollViewEndTime = scrollViewEndTime;
	}

	public boolean isLoopMarkerSelected() {
		return loopMarkerSelected;
	}

	public void setLoopMarkerSelected(boolean loopMarkerSelected) {
		this.loopMarkerSelected = loopMarkerSelected;
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
}
