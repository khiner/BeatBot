package com.kh.beatbot.view;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BBButton;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.helper.WaveformHelper;

public class SampleWaveformView extends TouchableSurfaceView {

	// min distance for pointer to select loop markers
	private static final int SNAP_DIST = 32;
	private static FloatBuffer waveformVb = null;
	private static FloatBuffer backgroundOutlineVb = null;
	private static FloatBuffer previewButtonSquareVb = null;
	private static FloatBuffer loopSelectionLineVb = null;
	private static FloatBuffer loopSelectionRectVbs[] = new FloatBuffer[2];

	private static BBButton previewButton;

	private final int MIN_LOOP_WINDOW = 32;
	// the left of this view is a preview button
	private float previewButtonWidth, waveformWidth;

	// which pointer id is touching which marker (-1 means no pointer)
	private int beginLoopMarkerTouched = -1;
	private int endLoopMarkerTouched = -1;

	// keep track of which finger is touching the preview button,
	// so we can handle pointer-up/ move events outside the button
	private int previewPointerId = -1;

	private float zoomLeftAnchorSample = -1;
	private float zoomRightAnchorSample = -1;

	private float scrollAnchorSample = -1;

	// zooming/scrolling will change the view window of the samples
	// keep track of that with offset and width
	private float sampleOffset = 0;
	private float sampleWidth = 0;

	public SampleWaveformView(Context c, AttributeSet as) {
		super(c, as);
	}

	public void update() {
		File sampleFile = TrackManager.currTrack.getSampleFile();
		WaveformHelper.setSampleFile(sampleFile);
		sampleWidth = TrackManager.currTrack.getNumSamples();
		updateVbs();
	}

	private void initBackgroundOutlineVb() {
		backgroundOutlineVb = makeRectOutlineFloatBuffer(previewButtonWidth, 0,
				width - 2, height);
	}

	private void initPreviewButtonSquareVb() {
		previewButtonSquareVb = makeRectFloatBuffer(0, 0, previewButtonWidth,
				previewButtonWidth);
	}

	private void initLoopMarkerVb() {
		float xLoopBegin = sampleToX(TrackManager.currTrack
				.getLoopBeginSample());
		float xLoopEnd = sampleToX(TrackManager.currTrack.getLoopEndSample());
		float[] loopSelectionVertices = { xLoopBegin, 0, xLoopBegin, height,
				xLoopEnd, height, xLoopEnd, 0 };

		loopSelectionLineVb = makeFloatBuffer(loopSelectionVertices);
		loopSelectionRectVbs[0] = makeRectFloatBuffer(xLoopBegin - SNAP_DIST
				/ 2, height, xLoopBegin + SNAP_DIST / 2, 0);
		loopSelectionRectVbs[1] = makeRectFloatBuffer(xLoopEnd - SNAP_DIST / 2,
				height, xLoopEnd + SNAP_DIST / 2, 0);
	}

	private void updateWaveformVb() {
		if (height == 0) // this view hasn't even been init()'d yet.
			return;
		try {
			waveformVb = WaveformHelper.floatFileToBuffer(width
					- previewButtonWidth - SNAP_DIST, height,
					(long) sampleOffset, (long) sampleWidth, SNAP_DIST / 2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void drawWaveform() {
		if (waveformVb == null)
			return;
		gl.glEnable(GL10.GL_LINE_SMOOTH);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, waveformVb);
		gl.glPushMatrix();
		gl.glTranslatef(previewButtonWidth, 0, 0);
		drawLines(waveformVb, Colors.VOLUME, 10, GL10.GL_LINE_STRIP);
		gl.glPopMatrix();
		gl.glDisable(GL10.GL_LINE_SMOOTH);
	}

	private void drawLoopSelectionHighlightRect() {
		drawTriangleFan(loopSelectionLineVb, Colors.SAMPLE_LOOP_HIGHLIGHT);
	}
	
	private void drawLoopSelectionMarkers() {
		drawLines(loopSelectionLineVb, Colors.SAMPLE_LOOP_SELECT_OUTLINE, 2,
				GL10.GL_LINES);
		drawTriangleStrip(loopSelectionRectVbs[0],
				beginLoopMarkerTouched == -1 ? Colors.SAMPLE_LOOP_SELECT
						: Colors.SAMPLE_LOOP_SELECT_SELECTED);
		drawTriangleStrip(loopSelectionRectVbs[1],
				endLoopMarkerTouched == -1 ? Colors.SAMPLE_LOOP_SELECT
						: Colors.SAMPLE_LOOP_SELECT_SELECTED);
	}

	protected void loadIcons() {
		previewButton = new BBButton(new BBIconSource(-1, R.drawable.preview_icon,
				R.drawable.preview_icon_selected));
	}
	
	@Override
	protected void init() {
		setBackgroundColor(Colors.VIEW_BG);
		// make preview button square
		previewButtonWidth = height;
		waveformWidth = width - previewButtonWidth - SNAP_DIST;
		initBackgroundOutlineVb();
		initPreviewButtonSquareVb();
		update();
	}

	private void stopPreviewing() {
		previewPointerId = -1;
		previewButton.release();
		TrackManager.currTrack.stopPreviewing();
	}

	private float sampleToX(float sample) {
		return (sample - sampleOffset) * waveformWidth / sampleWidth
				+ previewButtonWidth + SNAP_DIST / 2;
	}

	private float xToSample(float x) {
		return (x - previewButtonWidth - SNAP_DIST / 2) * sampleWidth
				/ waveformWidth + sampleOffset;
	}

	private void updateSampleOffset(float scrollX) {
		// set sampleOffset such that the scroll anchor sample stays under
		// scrollX
		float newSampleOffset = scrollAnchorSample - xToSample(scrollX)
				+ sampleOffset;
		sampleOffset = newSampleOffset < 0 ? 0
				: (newSampleOffset + sampleWidth > TrackManager.currTrack
						.getNumSamples() ? TrackManager.currTrack
						.getNumSamples() - sampleWidth : newSampleOffset);
	}

	private void updateZoom(float x1, float x2) {
		// set sampleOffset and sampleWidth such that the zoom
		// anchor samples stay under x1 and x2
		float newSampleWidth = waveformWidth
				* (zoomRightAnchorSample - zoomLeftAnchorSample) / (x2 - x1);
		float newSampleOffset = zoomRightAnchorSample - newSampleWidth
				* (x2 - previewButtonWidth - SNAP_DIST / 2) / waveformWidth;
		if (newSampleOffset < 0
				&& newSampleOffset + newSampleWidth > TrackManager.currTrack
						.getNumSamples() || newSampleWidth < MIN_LOOP_WINDOW) {
			return;
		}
		if (newSampleOffset >= 0
				&& newSampleOffset + newSampleWidth <= TrackManager.currTrack
						.getNumSamples()) {
			sampleOffset = newSampleOffset;
			sampleWidth = newSampleWidth;
		} else if (newSampleOffset < 0) {
			sampleOffset = 0;
			sampleWidth = zoomRightAnchorSample * waveformWidth
					/ (x2 - previewButtonWidth);
		} else if (newSampleOffset + newSampleWidth > TrackManager.currTrack
				.getNumSamples()) {
			sampleWidth = waveformWidth
					* (zoomLeftAnchorSample - TrackManager.currTrack
							.getNumSamples())
					/ (x1 - previewButtonWidth - waveformWidth);
			sampleOffset = TrackManager.currTrack.getNumSamples() - sampleWidth;
		}
	}

	private void updateVbs() {
		// update the display location of the loop markers
		initLoopMarkerVb();
		updateWaveformVb();
		requestRender();
	}

	@Override
	protected void drawFrame() {
		drawLines(backgroundOutlineVb, Colors.WHITE, 4, GL10.GL_LINE_LOOP);
		drawLoopSelectionHighlightRect();
		drawWaveform();
		drawLoopSelectionMarkers();
		drawTriangleStrip(previewButtonSquareVb, Colors.BG_COLOR);
		previewButton.draw(0, 0, height, height);
	}

	private boolean selectLoopMarker(int id, float x) {
		if (Math.abs(x - sampleToX(TrackManager.currTrack.getLoopBeginSample())) < SNAP_DIST) {
			// begin loop marker touched
			beginLoopMarkerTouched = id;
			return true;
		} else if (Math.abs(x
				- sampleToX(TrackManager.currTrack.getLoopEndSample())) < SNAP_DIST) {
			// end loop marker touched
			endLoopMarkerTouched = id;
			return true;
		}
		return false;
	}

	private void deselectLoopMarker(int id) {
		if (beginLoopMarkerTouched == id) {
			beginLoopMarkerTouched = -1;
		} else if (endLoopMarkerTouched == id) {
			endLoopMarkerTouched = -1;
		}
	}

	private boolean moveLoopMarker(int id, float x) {
		if (beginLoopMarkerTouched == id) {
			// update track loop begin
			float newLoopBegin = xToSample(x);
			TrackManager.currTrack
					.setLoopBeginSample(newLoopBegin < 0 ? 0
							: (newLoopBegin >= TrackManager.currTrack
									.getLoopEndSample() - MIN_LOOP_WINDOW ? TrackManager.currTrack
									.getLoopEndSample() - MIN_LOOP_WINDOW
									: newLoopBegin));
			// update ui to fit the new begin point
			if (TrackManager.currTrack.getLoopBeginSample() < sampleOffset) {
				sampleWidth += sampleOffset
						- TrackManager.currTrack.getLoopBeginSample();
				sampleOffset = TrackManager.currTrack.getLoopBeginSample();
			} else if (TrackManager.currTrack.getLoopBeginSample() > sampleOffset
					+ sampleWidth) {
				sampleWidth = TrackManager.currTrack.getLoopBeginSample()
						- sampleOffset;
			}
		} else if (endLoopMarkerTouched == id) {
			// update track loop end
			float newLoopEnd = xToSample(x);
			TrackManager.currTrack
					.setLoopEndSample(newLoopEnd >= TrackManager.currTrack
							.getNumSamples() ? TrackManager.currTrack
							.getNumSamples() - 1
							: (newLoopEnd <= TrackManager.currTrack
									.getLoopBeginSample() + MIN_LOOP_WINDOW ? TrackManager.currTrack
									.getLoopBeginSample() + MIN_LOOP_WINDOW
									: newLoopEnd));
			// update ui to fit the new end point
			if (TrackManager.currTrack.getLoopEndSample() > sampleOffset
					+ sampleWidth) {
				sampleWidth = TrackManager.currTrack.getLoopEndSample()
						- sampleOffset;
			} else if (TrackManager.currTrack.getLoopEndSample() < sampleOffset) {
				sampleWidth += sampleOffset
						- TrackManager.currTrack.getLoopEndSample();
				sampleOffset = TrackManager.currTrack.getLoopEndSample();
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean setZoomAnchor(float x) {
		if (scrollAnchorSample != -1) {
			// one pointer is already scrolling.
			zoomLeftAnchorSample = Math.min(scrollAnchorSample, xToSample(x));
			zoomRightAnchorSample = Math.max(scrollAnchorSample, xToSample(x));
			scrollAnchorSample = -1; // not scrolling anymore
			return true;
		}
		return false;
	}

	private void setScrollAnchor(float x) {
		scrollAnchorSample = xToSample(x);
	}

	private boolean scroll(float scrollX) {
		if (scrollAnchorSample == -1)
			return false; // not scrolling
		updateSampleOffset(scrollX);
		return true;
	}

	private boolean zoom(float x1, float x2) {
		if (zoomLeftAnchorSample == -1 || zoomRightAnchorSample == -1)
			return false; // not zooming
		float leftX = Math.min(x1, x2);
		float rightX = Math.max(x1, x2);
		updateZoom(leftX, rightX);
		return true;
	}

	private void handleWaveActionDown(int id, float x, float y) {
		if (!selectLoopMarker(id, x)) {
			// loop marker not close enough to select. start scroll
			// (we know it's the first pointer down, so we're not zooming)
			setScrollAnchor(x);
		}
	}

	private void handleWaveActionPointerDown(int id, float x, float y) {
		if (!selectLoopMarker(id, x) && !setZoomAnchor(x)) {
			// loop marker not close enough to select, and first pointer down.
			// start scrolling
			setScrollAnchor(x);
		}
	}

	private void handleWaveActionPointerUp(MotionEvent e, int id) {
		deselectLoopMarker(id);
		scrollAnchorSample = -1;
		// stop zooming
		if (zoomLeftAnchorSample != -1 && zoomRightAnchorSample != -1) {
			int otherId = (id + 1) % 2;
			if (otherId != previewPointerId)
				setScrollAnchor(e.getX(otherId));
		}
		zoomLeftAnchorSample = zoomRightAnchorSample = -1;
	}

	public void handlePreviewActionDown(int id) {
		TrackManager.currTrack.preview();
		previewButton.touch();
		previewPointerId = id;
		requestRender();
	}

	public void handlePreviewActionPointerDown(int id) {
		handlePreviewActionDown(id);
	}

	@Override
	protected void handleActionDown(MotionEvent e, int id, float x, float y) {
		if (x > previewButtonWidth)
			handleWaveActionDown(id, x, y);
		else
			handlePreviewActionDown(id);
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		if (x > previewButtonWidth)
			handleWaveActionPointerDown(id, x, y);
		else
			handlePreviewActionPointerDown(id);
	}

	int count = 0;
	
	@Override
	protected void handleActionMove(MotionEvent e, int id, float x, float y) {
		if (zoomLeftAnchorSample != -1 && zoomRightAnchorSample != -1
				&& previewPointerId == -1)
			zoom(e.getX(0), e.getX(1)); // zoom always with pointers 1 & 2
		for (int i = 0; i < e.getPointerCount(); i++) {
			id = e.getPointerId(i);
			if (id == previewPointerId) {
				if (e.getX(i) > previewButtonWidth) {
					// if a click goes down on the preview button,
					// then moves out of it into wave, stop previewing
					stopPreviewing();
				}
			} else if (!moveLoopMarker(id, e.getX(i))) {
				scroll(e.getX(i));
			}
		}
		updateVbs();
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		if (x > previewButtonWidth) {
			handleWaveActionPointerUp(e, id);
		} else {
			previewButton.release();
			TrackManager.currTrack.stopPreviewing();
		}
	}

	@Override
	protected void handleActionUp(MotionEvent e, int id, float x, float y) {
		previewButton.release();
		beginLoopMarkerTouched = endLoopMarkerTouched = -1;
		TrackManager.currTrack.stopPreviewing();
		scrollAnchorSample = zoomLeftAnchorSample = zoomRightAnchorSample = -1;
		requestRender();
	}
}