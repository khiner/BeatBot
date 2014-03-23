package com.kh.beatbot.ui.view;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.Track;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.shape.WaveformShape;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ControlView2dBase;

public class SampleEditView extends ControlView2dBase {

	private static final String NO_SAMPLE_MESSAGE = "Tap to load a sample.";

	// min distance for pointer to select loop markers
	private static float minLoopWindow;
	private static WaveformShape waveformShape;
	private static Button[] loopButtons = new Button[2];
	private static Rectangle currSampleRect = null;

	private int scrollPointerId = -1, zoomLeftPointerId = -1, zoomRightPointerId = -1;

	private float scrollAnchorLevel = -1, zoomLeftAnchorLevel = -1, zoomRightAnchorLevel = -1;

	// Zooming/scrolling will change the view window of the samples.
	// Keep track of that with offset and width.
	private float levelOffset = 0, levelWidth = 0, waveformWidth = 0, loopButtonW = 0;

	public SampleEditView(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	public synchronized void update() {
		setText(hasSample() ? "" : NO_SAMPLE_MESSAGE);
		if (!hasSample())
			return;
		setLevel(0, 1);
		updateLoopSelectionVbs();
		waveformShape.resample();
	}

	private void updateWaveformVb() {
		waveformShape.update((long) params[0].getLevel(levelOffset),
				(long) params[0].getLevel(levelWidth), loopButtonW / 2);
	}

	private void updateLoopSelectionVbs() {
		float beginX = levelToX(params[0].viewLevel);
		float endX = levelToX(params[1].viewLevel);
		waveformShape.setLoopPoints(beginX, endX);
		loopButtons[0].setPosition(beginX - loopButtonW / 2, 0);
		loopButtons[1].setPosition(endX - loopButtonW / 2, 0);
		updateCurrSample();
	}

	private void updateZoom() {
		float x1 = pointerIdToPos.get(zoomLeftPointerId).x;
		float x2 = pointerIdToPos.get(zoomRightPointerId).x;

		if (x1 >= x2)
			return; // sanity check

		float ZLAL = zoomLeftAnchorLevel, ZRAL = zoomRightAnchorLevel;

		// set levelOffset and levelWidth such that the zoom anchor levels stay under x1 and x2
		float newLevelWidth = waveformWidth * (ZRAL - ZLAL) / (x2 - x1);
		float newLevelOffset = ZRAL - newLevelWidth * (x2 - loopButtonW / 2) / waveformWidth;

		if (newLevelOffset < 0) {
			newLevelWidth = ZRAL * waveformWidth / (x2 - loopButtonW / 2);
			newLevelWidth = newLevelWidth <= 1 ? (newLevelWidth >= minLoopWindow ? newLevelWidth
					: minLoopWindow) : 1;
			setLevel(0, newLevelWidth);
		} else if (newLevelWidth > 1) {
			setLevel(newLevelOffset, 1 - newLevelOffset);
		} else if (newLevelWidth < minLoopWindow) {
			setLevel(newLevelOffset, minLoopWindow);
		} else if (newLevelOffset + newLevelWidth > 1) {
			newLevelWidth = ((ZLAL - 1) * waveformWidth) / (x1 - loopButtonW / 2 - waveformWidth);
			setLevel(1 - newLevelWidth, newLevelWidth);
		} else {
			setLevel(newLevelOffset, newLevelWidth);
		}
		updateLoopSelectionVbs();
	}

	@Override
	public synchronized void init() {
		setClip(true);
		// find view level for 32 samples
		minLoopWindow = params[0].getViewLevel(Track.MIN_LOOP_WINDOW);
		update();
	}

	@Override
	public synchronized void createChildren() {
		setIcon(IconResourceSets.SAMPLE_BG);
		initRect();

		for (int i = 0; i < loopButtons.length; i++) {
			loopButtons[i] = new Button(shapeGroup);
			loopButtons[i].setIcon(IconResourceSets.SAMPLE_LOOP);
			loopButtons[i].setOnPressListener(new OnPressListener() {
				@Override
				public void onPress(Button button) {
					if (button.ownsPointer(scrollPointerId)) {
						scrollPointerId = -1;
					} else if (button.ownsPointer(zoomLeftPointerId)) {
						scrollPointerId = zoomRightPointerId;
						zoomLeftPointerId = zoomRightPointerId = -1;
					} else if (button.ownsPointer(zoomRightPointerId)) {
						scrollPointerId = zoomLeftPointerId;
						zoomLeftPointerId = zoomRightPointerId = -1;
					}
				}
			});
		}
		currSampleRect = new Rectangle(shapeGroup, Color.TRON_BLUE, null);
		addChildren(loopButtons);
	}

	public synchronized void layoutChildren() {
		loopButtonW = height / 3;
		waveformWidth = width - loopButtonW;

		if (null == waveformShape) {
			waveformShape = new WaveformShape(shapeGroup, waveformWidth, Color.LABEL_SELECTED,
					Color.BLACK);
			for (View child : children) {
				child.shape.bringToTop();
			}
			currSampleRect.bringToTop();
		}

		waveformShape.layout(absoluteX, absoluteY, waveformWidth, height);
		currSampleRect.layout(absoluteX, absoluteY, 4, height);
		loopButtons[0].layout(this, 0, 0, loopButtonW, height);
		loopButtons[1].layout(this, width - loopButtonW, 0, loopButtonW, height);
	}

	private void updateCurrSample() {
		currSampleRect
				.setPosition(
						absoluteX
								+ levelToX(params[0].getViewLevel(TrackManager.currTrack
										.getCurrentFrame())), absoluteY);
	}

	@Override
	public void draw() {
		if (TrackManager.currTrack.isPlaying() || TrackManager.currTrack.isPreviewing()) {
			updateCurrSample();
		}
	}

	private boolean moveLoopMarker(int id, float x) {
		if (loopButtons[0].isPressed() && loopButtons[0].ownsPointer(id)) {
			// update track loop begin
			params[0].setLevel(xToLevel(x));
			// update ui to fit the new begin point
			if (params[0].viewLevel < levelOffset) {
				setLevel(params[0].viewLevel, levelWidth + levelOffset - params[0].viewLevel);
			} else if (params[0].viewLevel > levelOffset + levelWidth) {
				setLevel(levelOffset, params[0].viewLevel - levelOffset);
			}
			return true;
		} else if (loopButtons[1].isPressed() && loopButtons[1].ownsPointer(id)) {
			// update track loop end
			params[1].setLevel(xToLevel(x));
			// update ui to fit the new end point
			if (params[1].viewLevel > levelOffset + levelWidth) {
				setLevel(levelOffset, params[1].viewLevel - levelOffset);
			} else if (params[1].viewLevel < levelOffset) {
				setLevel(params[1].viewLevel, levelWidth + levelOffset - params[1].viewLevel);
			}
			return true;
		} else {
			return false;
		}
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
		if (null != pointerIdToPos.get(id)) {
			scrollPointerId = id;
			scrollAnchorLevel = xToLevel(pointerIdToPos.get(id).x);
		} else {
			scrollPointerId = -1;
		}
	}

	private void scroll(float scrollX) {
		if (scrollPointerId == -1)
			return; // not scrolling

		// set levelOffset such that the scroll anchor level stays under scrollX
		float newLevelOffset = GeneralUtils.clipTo(scrollAnchorLevel - xToLevel(scrollX)
				+ levelOffset, 0, 1 - levelWidth);
		setLevel(newLevelOffset, levelWidth);
		updateLoopSelectionVbs();
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		if (!hasSample())
			return;
		setScrollAnchor(id);
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
		if (!hasSample())
			return;
		waveformShape.resample();
		scrollPointerId = zoomLeftPointerId = zoomRightPointerId = -1;
	}

	@Override
	public void handleActionPointerDown(int id, float x, float y) {
		if (!hasSample())
			return;
		if (!setZoomAnchor(id)) {
			// loop marker not close enough to select, and first pointer down. Start scrolling
			setScrollAnchor(id);
		}
	}

	@Override
	public void handleActionPointerUp(int id, float x, float y) {
		if (!hasSample())
			return;
		// stop zooming
		setScrollAnchor(id == zoomLeftPointerId ? zoomRightPointerId : zoomLeftPointerId);
		zoomLeftPointerId = zoomRightPointerId = -1;
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		if (!hasSample())
			return;
		if (id == zoomLeftPointerId) {
			// no-op: only zoom once both actions have been handled
		} else if (id == zoomRightPointerId) {
			updateZoom();
		} else if (!moveLoopMarker(id, x)) {
			scroll(x);
		}
	}

	@Override
	protected float xToLevel(float x) {
		return (x - loopButtonW / 2) * checkedDivide(levelWidth, waveformWidth) + levelOffset;
	}

	@Override
	protected float yToLevel(float y) {
		return 0;
	}

	protected float levelToX(float level) {
		return loopButtonW / 2 + checkedDivide((level - levelOffset) * waveformWidth, levelWidth);
	}

	@Override
	public void onParamChanged(Param param) {
		if (param == null)
			return;
		if (param.equals(TrackManager.currTrack.getGainParam())) {
			waveformShape.resample();
		} else {
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

	@Override
	protected void release() {
		super.release();
		if (!hasSample()) {
			View.mainPage.pageSelectGroup.selectBrowsePage();
		}
	}
}
