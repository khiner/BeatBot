package com.kh.beatbot.ui.view;

import java.io.IOException;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.control.ControlView2dBase;

public class SampleEditView extends ControlView2dBase {

	// min distance for pointer to select loop markers
	private static final float SNAP_DIST = 32f,
			MIN_LOOP_WINDOW = 32f / PlaybackManager.SAMPLE_RATE;

	private static FloatBuffer waveformVb = null, backgroundOutlineVb = null,
			loopSelectionLineVb = null,
			loopSelectionRectVbs[] = new FloatBuffer[2];

	// which pointer id is touching which marker (-1 means no pointer)
	private int beginLoopPointerId = -1, endLoopPointerId = -1;

	private int scrollPointerId = -1, zoomLeftPointerId = -1,
			zoomRightPointerId = -1;

	private float scrollAnchorLevel = -1, zoomLeftAnchorLevel = -1,
			zoomRightAnchorLevel = -1;

	// zooming/scrolling will change the view window of the samples
	// keep track of that with offset and width
	private float levelOffset = 0, levelWidth = 0, waveformWidth = 0;

	public void update() {
		levelOffset = 0;
		levelWidth = 1;
		updateVbs();
	}

	private void initBackgroundOutlineVb() {
		backgroundOutlineVb = makeRectFloatBuffer(0, 0, width - 2, height);
	}

	private void updateWaveformVb() {
		try {
			waveformVb = TrackManager.currTrack.getCurrSampleFile()
					.floatFileToBuffer(this,
							(long) params[0].getLevel(levelOffset),
							(long) params[0].getLevel(levelWidth),
							(int) (SNAP_DIST / 2));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void drawWaveform() {
		if (waveformVb == null)
			return;
		gl.glEnable(GL10.GL_LINE_SMOOTH);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, waveformVb);
		drawLines(waveformVb, Colors.VOLUME, 10, GL10.GL_LINE_STRIP);
		gl.glDisable(GL10.GL_LINE_SMOOTH);
	}

	private void drawLoopSelectionHighlightRect() {
		drawTriangleFan(loopSelectionLineVb, Colors.SAMPLE_LOOP_HIGHLIGHT);
	}

	private void drawLoopSelectionMarkers() {
		drawLines(loopSelectionLineVb, Colors.SAMPLE_LOOP_SELECT_OUTLINE, 2,
				GL10.GL_LINES);
		drawTriangleFan(loopSelectionRectVbs[0],
				beginLoopPointerId == -1 ? Colors.SAMPLE_LOOP_SELECT
						: Colors.SAMPLE_LOOP_SELECT_SELECTED);
		drawTriangleFan(loopSelectionRectVbs[1],
				endLoopPointerId == -1 ? Colors.SAMPLE_LOOP_SELECT
						: Colors.SAMPLE_LOOP_SELECT_SELECTED);
	}

	@Override
	public void init() {
		// setBackgroundColor(Colors.VIEW_BG);
		waveformWidth = width - SNAP_DIST;
		initBackgroundOutlineVb();
		update();
	}

	private void updateLevelOffset(float scrollX) {
		// set levelOffset such that the scroll anchor level stays under
		// scrollX
		float newLevelOffset = scrollAnchorLevel - xToLevel(scrollX)
				+ levelOffset;
		levelOffset = newLevelOffset < 0 ? 0
				: (newLevelOffset + levelWidth > 1 ? 1 - levelWidth
						: newLevelOffset);
	}

	private void updateZoom() {
		float x1 = pointerIdToPos.get(zoomLeftPointerId).x;
		float x2 = pointerIdToPos.get(zoomRightPointerId).x;

		if (x1 >= x2)
			return; // sanity check

		float ZLAL = zoomLeftAnchorLevel, ZRAL = zoomRightAnchorLevel;

		// set levelOffset and levelWidth such that the zoom
		// anchor levels stay under x1 and x2
		float newLevelWidth = waveformWidth * (ZRAL - ZLAL) / (x2 - x1);
		float newLevelOffset = ZRAL - newLevelWidth * (x2 - SNAP_DIST / 2)
				/ waveformWidth;

		if (newLevelOffset < 0) {
			levelOffset = 0;
			levelWidth = ZRAL * waveformWidth / (x2 - SNAP_DIST / 2);
			levelWidth = levelWidth <= 1 ? (levelWidth >= MIN_LOOP_WINDOW ? levelWidth
					: MIN_LOOP_WINDOW)
					: 1;
		} else if (newLevelWidth > 1) {
			levelOffset = newLevelOffset;
			levelWidth = 1 - levelOffset;
		} else if (newLevelWidth < MIN_LOOP_WINDOW) {
			levelOffset = newLevelOffset;
			levelWidth = MIN_LOOP_WINDOW;
		} else if (newLevelOffset + newLevelWidth > 1) {
			levelWidth = ((ZLAL - 1) * waveformWidth)
					/ (x1 - SNAP_DIST / 2 - waveformWidth);
			levelOffset = 1 - levelWidth;
		} else {
			levelOffset = newLevelOffset;
			levelWidth = newLevelWidth;
		}
		updateVbs();
	}

	private void updateVbs() {
		updateWaveformVb();
		setViewLevel(params[0].viewLevel, params[1].viewLevel);
	}

	@Override
	public void draw() {
		drawLines(backgroundOutlineVb, Colors.WHITE, 4, GL10.GL_LINE_LOOP);
		drawLoopSelectionHighlightRect();
		drawWaveform();
		drawLoopSelectionMarkers();
	}

	private boolean selectLoopMarker(int id, float x) {
		if (Math.abs(x - levelToX(params[0].viewLevel)) < SNAP_DIST) {
			// begin loop marker touched
			beginLoopPointerId = id;
			return true;
		} else if (Math.abs(x - levelToX(params[1].viewLevel)) < SNAP_DIST) {
			// end loop marker touched
			endLoopPointerId = id;
			return true;
		}
		return false;
	}

	private void deselectLoopMarker(int id) {
		if (beginLoopPointerId == id) {
			beginLoopPointerId = -1;
		} else if (endLoopPointerId == id) {
			endLoopPointerId = -1;
		}
	}

	private boolean moveLoopMarker(int id, float x) {
		float beginLevel = params[0].viewLevel;
		float endLevel = params[1].viewLevel;

		if (id == beginLoopPointerId) {
			// update track loop begin
			float newLoopBegin = xToLevel(x);
			params[0].setLevel(newLoopBegin < 0 ? 0
					: (newLoopBegin >= beginLevel - MIN_LOOP_WINDOW ? endLevel
							- MIN_LOOP_WINDOW : newLoopBegin));
			// update ui to fit the new begin point
			if (beginLevel < levelOffset) {
				levelWidth += levelOffset - beginLevel;
				levelOffset = beginLevel;
			} else if (beginLevel > levelOffset + levelWidth) {
				levelWidth = beginLevel - levelOffset;
			}
		} else if (id == endLoopPointerId) {
			// update track loop end
			float newLoopEnd = xToLevel(x);
			params[1].setLevel(newLoopEnd >= 1 ? 1 : (newLoopEnd <= beginLevel
					+ MIN_LOOP_WINDOW ? beginLevel + MIN_LOOP_WINDOW
					: newLoopEnd));
			// update ui to fit the new end point
			if (endLevel > levelOffset + levelWidth) {
				levelWidth = endLevel - levelOffset;
			} else if (endLevel < levelOffset) {
				levelWidth += levelOffset - endLevel;
				levelOffset = endLevel;
			}
		} else {
			return false;
		}
		updateVbs();
		return true;
	}

	private boolean setZoomAnchor(int id) {
		if (scrollPointerId == -1) {
			return false; // need to be scrolling to start zooming
		}
		if (pointerIdToPos.get(scrollPointerId).x > pointerIdToPos.get(id).x) {
			zoomLeftPointerId = id;
			zoomRightPointerId = scrollPointerId;
		} else {
			zoomLeftPointerId = scrollPointerId;
			zoomRightPointerId = id;
		}
		scrollPointerId = -1; // not scrolling anymore

		zoomLeftAnchorLevel = xToLevel(pointerIdToPos.get(zoomLeftPointerId).x);
		zoomRightAnchorLevel = xToLevel(pointerIdToPos.get(zoomRightPointerId).x);
		return true;
	}

	private void setScrollAnchor(int id) {
		scrollPointerId = id;
		scrollAnchorLevel = xToLevel(pointerIdToPos.get(id).x);
	}

	private void scroll(float scrollX) {
		if (scrollPointerId == -1)
			return; // not scrolling
		updateLevelOffset(scrollX);
		updateVbs();
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		if (!selectLoopMarker(id, x)) {
			// loop marker not close enough to select. start scroll
			// (we know it's the first pointer down, so we're not zooming)
			setScrollAnchor(id);
		}
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		scrollPointerId = beginLoopPointerId = endLoopPointerId = zoomLeftPointerId = zoomRightPointerId = -1;
		scrollAnchorLevel = zoomLeftAnchorLevel = zoomRightAnchorLevel = -1;
	}

	@Override
	public void handleActionPointerDown(int id, float x, float y) {
		if (!selectLoopMarker(id, x) && !setZoomAnchor(id)) {
			// loop marker not close enough to select, and first pointer down.
			// start scrolling
			setScrollAnchor(id);
		}
	}

	@Override
	public void handleActionPointerUp(int id, float x, float y) {
		deselectLoopMarker(id);
		// stop zooming
		if (id == zoomLeftPointerId) {
			setScrollAnchor(zoomRightPointerId);
		} else if (id == zoomRightPointerId) {
			setScrollAnchor(zoomLeftPointerId);
		}
		zoomLeftPointerId = zoomRightPointerId = -1;
		zoomLeftAnchorLevel = zoomRightAnchorLevel = -1;
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		if (id == zoomLeftPointerId) {
			// no-op: only zoom once both actions have been handled
		} else if (id == zoomRightPointerId) {
			updateZoom();
		} else if (!moveLoopMarker(id, x)) {
			scroll(x);
		}
	}

	@Override
	protected void setViewLevel(float beginLevel, float endLevel) {
		float beginX = levelToX(beginLevel);
		float endX = levelToX(endLevel);
		float[] loopSelectionVertices = { beginX, 0, beginX, height, endX,
				height, endX, 0 };

		loopSelectionLineVb = makeFloatBuffer(loopSelectionVertices);
		loopSelectionRectVbs[0] = makeRectFloatBuffer(beginX - SNAP_DIST / 2,
				height, beginX + SNAP_DIST / 2, 0);
		loopSelectionRectVbs[1] = makeRectFloatBuffer(endX - SNAP_DIST / 2,
				height, endX + SNAP_DIST / 2, 0);
	}

	@Override
	protected float xToLevel(float x) {
		return (x - SNAP_DIST / 2) / waveformWidth + levelOffset;
	}

	protected float levelToX(float level) {
		return SNAP_DIST / 2 + (level - levelOffset) * waveformWidth;
	}

	@Override
	protected float yToLevel(float y) {
		return 0;
	}
}
