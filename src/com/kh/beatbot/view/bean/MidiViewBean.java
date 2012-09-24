package com.kh.beatbot.view.bean;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.helper.MidiTrackControlHelper;

public class MidiViewBean {
	public static final float COLOR_TRANSITION_RATE = .02f;
	public static float X_OFFSET;
	public static final float Y_OFFSET = 21;

	public static final float LOOP_SELECT_SNAP_DIST = 30;
	
	// the size of the "dots" at the top of level display
	public static final int LEVEL_POINT_SIZE = 16;
	// the width of the lines for note levels
	public static final int LEVEL_LINE_WIDTH = 7;

	private MidiView.State viewState = MidiView.State.NORMAL_VIEW;

	private float width, height;
	private float levelsHeight;
	// the main background color for the view.
	// this color can change when transitioning to/from LEVEL_VIEW
	private float bgColor = Colors.MIDI_VIEW_DEFAULT_BG_COLOR;
	private float[] bgColorRgb = {bgColor, bgColor, bgColor, 1};
	
	private float dragOffsetTick[] = new float[5];

	private int pinchLeftPointerId = -1;
	private int pinchRightPointerId = -1;
	private float pinchLeftOffset = 0;
	private float pinchRightOffset = 0;
	private float zoomLeftAnchorTick = 0;
	private float zoomRightAnchorTick = 0;

	private int scrollPointerId = -1;
	private float scrollAnchorTick = 0;

	private boolean selectRegion = false;
	private float selectRegionStartTick = -1;
	private float selectRegionStartY = -1;

	// true when a note is being "pinched" (two-fingers touching the note)
	private boolean pinch = false;

	private int[] loopPointerIds = { -1, -1, -1 };
	private float loopSelectionOffset = 0;

	// set this to true after an event that can be undone (with undo btn)
	private boolean stateChanged = false;

	// this option can be set via a menu item.
	// if true, all midi note movements are rounded to the nearest major tick
	private boolean snapToGrid = true;

	public MidiView.State getViewState() {
		return viewState;
	}

	public void setViewState(MidiView.State viewState) {
		this.viewState = viewState;
	}
	
	public float getWidth() {
		return width - MidiTrackControlHelper.width;
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

	public float getLevelsHeight() {
		return levelsHeight;
	}

	public void setLevelsHeight(float levelsHeight) {
		this.levelsHeight = levelsHeight;
	}

	public float getBgColor() {
		return bgColor;
	}

	public float[] getBgColorRgb() {
		return bgColorRgb;
	}
	
	public void setBgColor(float bgColor) {
		this.bgColor = bgColor;
		bgColorRgb[0] = bgColorRgb[1] = bgColorRgb[2] = bgColor;
	}

	public float getDragOffsetTick(int i) {
		return dragOffsetTick[i];
	}

	public void setDragOffsetTick(int i, float dragOffset) {
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

	public float getPinchLeftOffset() {
		return pinchLeftOffset;
	}

	public void setPinchLeftOffset(float pinchLeftOffset) {
		this.pinchLeftOffset = pinchLeftOffset;
	}

	public float getPinchRightOffset() {
		return pinchRightOffset;
	}

	public void setPinchRightOffset(float pinchRightOffset) {
		this.pinchRightOffset = pinchRightOffset;
	}

	public float getZoomLeftAnchorTick() {
		return zoomLeftAnchorTick;
	}

	public void setZoomLeftAnchorTick(float zoomLeftAnchorTick) {
		this.zoomLeftAnchorTick = zoomLeftAnchorTick;
	}

	public float getZoomRightAnchorTick() {
		return zoomRightAnchorTick;
	}

	public void setZoomRightAnchorTick(float zoomRightAnchorTick) {
		this.zoomRightAnchorTick = zoomRightAnchorTick;
	}

	public float getScrollAnchorTick() {
		return scrollAnchorTick;
	}

	public void setScrollPointerId(int scrollPointerId) {
		this.scrollPointerId = scrollPointerId;
	}

	public int getScrollPointerId() {
		return scrollPointerId;
	}

	public void setScrollAnchorTick(float scrollAnchorTick) {
		this.scrollAnchorTick = scrollAnchorTick;
	}

	public boolean isSelectRegion() {
		return selectRegion;
	}

	public void setSelectRegion(boolean selectRegion) {
		this.selectRegion = selectRegion;
	}

	public float getSelectRegionStartTick() {
		return selectRegionStartTick;
	}

	public void setSelectRegionStartTick(float selectRegionStartTick) {
		this.selectRegionStartTick = selectRegionStartTick;
	}

	public float getSelectRegionStartY() {
		return selectRegionStartY;
	}

	public void setSelectRegionStartY(float selectRegionStartY) {
		this.selectRegionStartY = selectRegionStartY;
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
}
