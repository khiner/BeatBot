package com.kh.beatbot.ui.view;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.Track;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.icon.IconResourceSet.State;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.shape.WaveformShape;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ControlView2dBase;

public class SampleEditView extends ControlView2dBase {

	public SampleEditView(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

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

	public synchronized void update() {
		loopButtonW = height / 3;
		waveformWidth = width - loopButtonW;

		if (hasSample()) {
			if (null == waveformShape) {
				waveformShape = new WaveformShape(shapeGroup, waveformWidth, Color.LABEL_SELECTED,
						Color.BLACK);
			}
			for (int i = 0; i < loopButtons.length; i++) {
				if (null == loopButtons[i]) {
					loopButtons[i] = new Button(shapeGroup);
					loopButtons[i].deselectOnPointerExit = false;
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
				loopButtons[i].setState(loopButtons[i].getState());
			}
			addChildren(loopButtons);

			waveformShape.layout(absoluteX, absoluteY, waveformWidth, height);

			updateLoopSelectionVbs();
			setLevel(0, 1);
			waveformShape.resample();
			setText("");
		} else {
			for (Button button : loopButtons) {
				removeChild(button);
				button = null;
			}
			if (null != waveformShape) {
				waveformShape.destroy();
				waveformShape = null;
			}
			setText(NO_SAMPLE_MESSAGE);
		}
	}

	private void updateWaveformVb() {
		if (null != waveformShape) {
			waveformShape.update((long) params[0].getLevel(levelOffset),
					(long) params[0].getLevel(levelWidth), loopButtonW / 2);
		}
	}

	private void updateLoopSelectionVbs() {
		if (null == waveformShape)
			return;
		float beginX = levelToX(params[0].viewLevel);
		float endX = levelToX(params[1].viewLevel);
		waveformShape.setLoopPoints(beginX, endX);
		loopButtons[0].layout(this, beginX - loopButtonW / 2, 0, loopButtonW, height);
		loopButtons[1].layout(this, endX - loopButtonW / 2, 0, loopButtonW, height);
	}

	private void updateZoom() {
		float x1 = pointersById.get(zoomLeftPointerId).x;
		float x2 = pointersById.get(zoomRightPointerId).x;

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
	}

	private void updateCurrSample() {
		if (null == currSampleRect) {
			currSampleRect = new Rectangle(shapeGroup, Color.TRON_BLUE, null);
		}
		float x = levelToX(params[0].getViewLevel(TrackManager.currTrack.getCurrentFrame()));
		currSampleRect.layout(absoluteX + x, absoluteY, 4, height);
	}

	@Override
	public void tick() {
		if (TrackManager.currTrack.isPlaying() || TrackManager.currTrack.isPreviewing()) {
			updateCurrSample();
		} else if (null != currSampleRect) {
			currSampleRect.destroy();
			currSampleRect = null;
		}
	}

	private boolean moveLoopMarker(int id, Pointer pos) {
		return moveLoopMarker(id, pos.x, loopButtons[0], params[0])
				|| moveLoopMarker(id, pos.x, loopButtons[1], params[1]) || false;
	}

	private boolean moveLoopMarker(int id, float x, Button button, Param param) {
		if (button.isPressed() && button.ownsPointer(id)) {
			// update track loop begin
			param.setLevel(xToLevel(x));
			// update ui to fit the new begin point
			if (param.viewLevel < levelOffset)
				setLevel(param.viewLevel, levelWidth + levelOffset - param.viewLevel);
			else if (param.viewLevel > levelOffset + levelWidth)
				setLevel(levelOffset, param.viewLevel - levelOffset);
			return true;
		} else {
			return false;
		}
	}

	private boolean setZoomAnchor(int id) {
		if (scrollPointerId == -1) {
			return false; // need to be scrolling to start zooming
		}
		if (pointersById.get(scrollPointerId).x > pointersById.get(id).x) {
			zoomLeftPointerId = id;
			zoomRightPointerId = scrollPointerId;
		} else {
			zoomLeftPointerId = scrollPointerId;
			zoomRightPointerId = id;
		}
		scrollPointerId = -1; // not scrolling anymore

		zoomLeftAnchorLevel = xToLevel(pointersById.get(zoomLeftPointerId).x);
		zoomRightAnchorLevel = xToLevel(pointersById.get(zoomRightPointerId).x);
		return true;
	}

	private void setLevel(float levelOffset, float levelWidth) {
		this.levelOffset = levelOffset;
		this.levelWidth = levelWidth;
		updateWaveformVb();
	}

	private void setScrollAnchor(int id, Pointer pos) {
		if (null != pos) {
			scrollPointerId = id;
			scrollAnchorLevel = xToLevel(pos.x);
		} else {
			scrollPointerId = -1;
		}
	}

	private void scroll(Pointer pos) {
		// set levelOffset such that the scroll anchor level stays under scrollX
		float newLevelOffset = GeneralUtils.clipTo(scrollAnchorLevel - xToLevel(pos.x)
				+ levelOffset, 0, 1 - levelWidth);
		setLevel(newLevelOffset, levelWidth);
		updateLoopSelectionVbs();
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		super.handleActionDown(id, pos);
		if (!hasSample())
			return;
		setScrollAnchor(id, pos);
	}

	@Override
	public void handleActionUp(int id, Pointer pos) {
		super.handleActionUp(id, pos);
		if (!hasSample())
			return;
		waveformShape.resample();
		scrollPointerId = zoomLeftPointerId = zoomRightPointerId = -1;
	}

	@Override
	public void handleActionPointerDown(int id, Pointer pos) {
		if (!hasSample())
			return;
		if (!setZoomAnchor(id)) {
			// loop marker not close enough to select, and first pointer down. Start scrolling
			setScrollAnchor(id, pos);
		}
	}

	@Override
	public void handleActionPointerUp(int id, Pointer pos) {
		if (!hasSample())
			return;
		// stop zooming
		int scrollPointerId = id == zoomLeftPointerId ? zoomRightPointerId : zoomLeftPointerId;
		setScrollAnchor(scrollPointerId, pointersById.get(scrollPointerId));
		zoomLeftPointerId = zoomRightPointerId = -1;
	}

	@Override
	public void handleActionMove(int id, Pointer pos) {
		if (!hasSample()) {
			checkPointerExit(id, pos);
			return;
		}
		if (id == zoomLeftPointerId) {
			// no-op: only zoom once both actions have been handled
		} else if (id == zoomRightPointerId) {
			updateZoom();
		} else if (!moveLoopMarker(id, pos)) {
			scroll(pos);
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
	protected void dragRelease() {
		setState(State.DEFAULT);
	}

	@Override
	protected void release() {
		if (!hasSample() && isPressed()) {
			View.mainPage.pageSelectGroup.selectBrowsePage();
		}
		super.release();
	}
}
