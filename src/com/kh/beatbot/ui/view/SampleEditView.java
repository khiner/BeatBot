package com.kh.beatbot.ui.view;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.Track;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.Shape;
import com.kh.beatbot.ui.mesh.Shape.Type;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.mesh.WaveformShape;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ControlView2dBase;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.group.PageSelectGroup;

public class SampleEditView extends ControlView2dBase {

	private static final String NO_SAMPLE_MESSAGE = "Tap to load a sample.";

	// min distance for pointer to select loop markers
	private static final float SNAP_DIST = 32f, X_OFFSET = SNAP_DIST / 2;
	private static float minLoopWindow;

	private static ShapeGroup shapeGroup = new ShapeGroup();
	private static WaveformShape waveformShape;
	private static ImageButton loopBeginButton, loopEndButton;
	private static FloatBuffer currSampleLineVb = null;

	private boolean pressed = false;

	private int scrollPointerId = -1, zoomLeftPointerId = -1,
			zoomRightPointerId = -1;

	private float scrollAnchorLevel = -1, zoomLeftAnchorLevel = -1,
			zoomRightAnchorLevel = -1;

	// zooming/scrolling will change the view window of the samples
	// keep track of that with offset and width
	private float levelOffset = 0, levelWidth = 0, waveformWidth = 0;

	public synchronized void update() {
		if (params[0] == null && params[1] == null)
			return;
		setLevel(0, 1);
		updateLoopSelectionVbs();
		waveformShape.resample();
	}

	private void updateWaveformVb() {
		waveformShape.update((long) params[0].getLevel(levelOffset),
				(long) params[0].getLevel(levelWidth), X_OFFSET);
	}

	private void updateLoopSelectionVbs() {
		float beginX = levelToX(params[0].viewLevel);
		float endX = levelToX(params[1].viewLevel);
		waveformShape.setLoopPoints(beginX, endX);
		loopBeginButton.setPosition(beginX - X_OFFSET, 0);
		loopEndButton.setPosition(endX - X_OFFSET, 0);
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
		float newLevelOffset = ZRAL - newLevelWidth * (x2 - X_OFFSET)
				/ waveformWidth;

		if (newLevelOffset < 0) {
			newLevelWidth = ZRAL * waveformWidth / (x2 - X_OFFSET);
			newLevelWidth = newLevelWidth <= 1 ? (newLevelWidth >= minLoopWindow ? newLevelWidth
					: minLoopWindow)
					: 1;
			setLevel(0, newLevelWidth);
		} else if (newLevelWidth > 1) {
			setLevel(newLevelOffset, 1 - newLevelOffset);
		} else if (newLevelWidth < minLoopWindow) {
			setLevel(newLevelOffset, minLoopWindow);
		} else if (newLevelOffset + newLevelWidth > 1) {
			newLevelWidth = ((ZLAL - 1) * waveformWidth)
					/ (x1 - X_OFFSET - waveformWidth);
			setLevel(1 - newLevelWidth, newLevelWidth);
		} else {
			setLevel(newLevelOffset, newLevelWidth);
		}
		updateLoopSelectionVbs();
	}

	private void initCurrSampleLineVb() {
		currSampleLineVb = makeFloatBuffer(new float[] { 0, 0, 0, height });
	}

	@Override
	public synchronized void init() {
		setStrokeColor(Colors.BLACK);
		initCurrSampleLineVb();
		// find view level for 32 samples
		minLoopWindow = params[0].getViewLevel(Track.MIN_LOOP_WINDOW);
		update();
	}

	@Override
	public synchronized void initIcons() {
		loopBeginButton.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.loopSelectionColorSet));
		loopEndButton.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.loopSelectionColorSet));
	}

	@Override
	public synchronized void createChildren() {
		loopBeginButton = new ImageButton();
		loopEndButton = new ImageButton();
		loopBeginButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {

			}
		});

		loopEndButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {

			}
		});

		addChildren(loopBeginButton, loopEndButton);
	}

	public void layout(View parent, float x, float y, float width, float height) {
		initBgRect(Type.RECTANGLE, shapeGroup, Colors.LABEL_VERY_LIGHT, null);
		waveformShape = Shape.createWaveform(shapeGroup, width,
				Colors.LABEL_SELECTED, Colors.BLACK);
		waveformShape.setStrokeWeight(2);
		waveformWidth = width - SNAP_DIST;
		super.layout(parent, x, y, width, height);
	}

	public synchronized void layoutChildren() {
		waveformShape.layout(0, 0, width, height);
		loopBeginButton.layout(null, 0, 0, X_OFFSET * 2, height);
		loopEndButton.layout(null, width - X_OFFSET, 0, X_OFFSET * 2, height);
	}

	private void drawCurrSampleLine() {
		push();
		translate(levelToX(params[0].getViewLevel(TrackManager.currTrack
				.getCurrentFrame())), 0);
		drawLines(currSampleLineVb, Colors.VOLUME, 4, GL10.GL_LINES);
		pop();
	}

	@Override
	public void draw() {
		if (!hasSample()) {
			setText(NO_SAMPLE_MESSAGE);
			super.draw();
			return;
		}
		shapeGroup.draw();
		if (TrackManager.currTrack.isPlaying()
				|| TrackManager.currTrack.isPreviewing()) {
			drawCurrSampleLine();
		}
	}

	private boolean isLoopMarkerSelected(int id) {
		return loopBeginButton.ownsPointer(id) || loopEndButton.ownsPointer(id);
	}

	private boolean moveLoopMarker(int id, float x) {
		float beginLevel = params[0].viewLevel;
		float endLevel = params[1].viewLevel;

		if (loopBeginButton.ownsPointer(id)) {
			// update track loop begin
			params[0].setLevel(xToLevel(x));
			// update ui to fit the new begin point
			if (beginLevel < levelOffset) {
				setLevel(beginLevel, levelWidth + levelOffset - beginLevel);
			} else if (beginLevel > levelOffset + levelWidth) {
				setLevel(levelOffset, beginLevel - levelOffset);
			}
		} else if (loopEndButton.ownsPointer(id)) {
			// update track loop end
			params[1].setLevel(xToLevel(x));
			// update ui to fit the new end point
			if (endLevel > levelOffset + levelWidth) {
				setLevel(levelOffset, endLevel - levelOffset);
			} else if (endLevel < levelOffset) {
				setLevel(endLevel, levelWidth + levelOffset - endLevel);
			}
		} else {
			return false;
		}
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

	private void setLevel(float levelOffset, float levelWidth) {
		this.levelOffset = levelOffset;
		this.levelWidth = levelWidth;
		updateWaveformVb();
	}

	private void setScrollAnchor(int id) {
		scrollPointerId = id;
		scrollAnchorLevel = xToLevel(pointerIdToPos.get(id).x);
	}

	private void scroll(float scrollX) {
		if (scrollPointerId == -1)
			return; // not scrolling

		// set levelOffset such that the scroll anchor level stays under
		// scrollX
		float newLevelOffset = GeneralUtils.clipTo(scrollAnchorLevel
				- xToLevel(scrollX) + levelOffset, 0, 1 - levelWidth);
		setLevel(newLevelOffset, levelWidth);
		updateLoopSelectionVbs();
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		if (!hasSample()) {
			press();
			return;
		}
		if (!isLoopMarkerSelected(id)) {
			// loop marker not close enough to select. start scroll
			// (we know it's the first pointer down, so we're not zooming)
			setScrollAnchor(id);
		}
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		if (!hasSample()) {
			if (pressed) {
				View.mainPage.pageSelectGroup
						.selectPage(PageSelectGroup.BROWSE_PAGE_ID);
			}
			release();
			return;
		}
		waveformShape.resample();
		scrollPointerId = zoomLeftPointerId = zoomRightPointerId = -1;
		scrollAnchorLevel = zoomLeftAnchorLevel = zoomRightAnchorLevel = -1;
	}

	@Override
	public void handleActionPointerDown(int id, float x, float y) {
		if (!hasSample())
			return;
		if (!isLoopMarkerSelected(id) && !setZoomAnchor(id)) {
			// loop marker not close enough to select, and first pointer down.
			// start scrolling
			setScrollAnchor(id);
		}
	}

	@Override
	public void handleActionPointerUp(int id, float x, float y) {
		if (!hasSample())
			return;
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
		if (!hasSample()) {
			if (!containsPoint(this.x + x, this.y + y)) {
				if (pressed) {
					release();
				}
			} else { // pointer inside button
				if (!pressed) {
					press();
				}
			}
			return;
		}
		if (id == zoomLeftPointerId) {
			// no-op: only zoom once both actions have been handled
		} else if (id == zoomRightPointerId) {
			updateZoom();
		} else if (!moveLoopMarker(id, x)) {
			scroll(x);
		}
	}

	private void press() {
		pressed = true;
		bgRect.setFillColor(Colors.LABEL_SELECTED);
	}

	private void release() {
		bgRect.setFillColor(Colors.LABEL_VERY_LIGHT);
		pressed = false;
	}

	@Override
	protected void setViewLevel(float beginLevel, float endLevel) {
		updateLoopSelectionVbs();
	}

	@Override
	protected float xToLevel(float x) {
		return (x - X_OFFSET) * checkedDivide(levelWidth, waveformWidth)
				+ levelOffset;
	}

	@Override
	protected float yToLevel(float y) {
		return 0;
	}

	protected float levelToX(float level) {
		return X_OFFSET
				+ checkedDivide((level - levelOffset) * waveformWidth,
						levelWidth);
	}

	@Override
	public void onParamChanged(Param param) {
		super.onParamChanged(param);
		if (param != null) {
			updateLoopSelectionVbs();
		}
	}

	private float checkedDivide(float num, float den) {
		return den == 0 ? 0 : num / den;
	}

	// if the params are null, then there is no sample file for this track.
	private boolean hasSample() {
		return params[0] != null && params[1] != null;
	}
}
