package com.kh.beatbot.view.window;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.helper.WaveformHelper;

public class SampleEditViewWindow extends TouchableViewWindow {

	public SampleEditViewWindow(TouchableSurfaceView parent) {
		super(parent);
	}

	private float waveformWidth = 0;
	
	// min distance for pointer to select loop markers
	private static final int SNAP_DIST = 32;
	private static FloatBuffer waveformVb = null;
	private static FloatBuffer backgroundOutlineVb = null;
	private static FloatBuffer loopSelectionLineVb = null;
	private static FloatBuffer loopSelectionRectVbs[] = new FloatBuffer[2];

	private final int MIN_LOOP_WINDOW = 32;

	// which pointer id is touching which marker (-1 means no pointer)
	private int beginLoopPointerId = -1;
	private int endLoopPointerId = -1;

	private int zoomLeftPointerId = -1;
	private int zoomRightPointerId = -1;
	
	private float zoomLeftAnchorSample = -1;
	private float zoomRightAnchorSample = -1;

	private float zoomLeftPointerX = -1;
	private float zoomRightPointerX = -1;
	
	private int scrollPointerId = -1;
	private float scrollAnchorSample = -1;
	

	// zooming/scrolling will change the view window of the samples
	// keep track of that with offset and width
	private float sampleOffset = 0;
	private float sampleWidth = 0;

	public void update() {
		File sampleFile = TrackManager.currTrack.getSampleFile();
		WaveformHelper.setSampleFile(sampleFile);
		sampleWidth = TrackManager.currTrack.getNumSamples();
		updateVbs();
	}

	private void initBackgroundOutlineVb() {
		backgroundOutlineVb = makeRectOutlineFloatBuffer(0, 0,
				width - 2, height);
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
			waveformVb = WaveformHelper.floatFileToBuffer(width - SNAP_DIST, height,
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
		drawLines(waveformVb, Colors.VOLUME, 10, GL10.GL_LINE_STRIP);
		gl.glDisable(GL10.GL_LINE_SMOOTH);
	}

	private void drawLoopSelectionHighlightRect() {
		drawTriangleFan(loopSelectionLineVb, Colors.SAMPLE_LOOP_HIGHLIGHT);
	}
	
	private void drawLoopSelectionMarkers() {
		drawLines(loopSelectionLineVb, Colors.SAMPLE_LOOP_SELECT_OUTLINE, 2,
				GL10.GL_LINES);
		drawTriangleStrip(loopSelectionRectVbs[0],
				beginLoopPointerId == -1 ? Colors.SAMPLE_LOOP_SELECT
						: Colors.SAMPLE_LOOP_SELECT_SELECTED);
		drawTriangleStrip(loopSelectionRectVbs[1],
				endLoopPointerId == -1 ? Colors.SAMPLE_LOOP_SELECT
						: Colors.SAMPLE_LOOP_SELECT_SELECTED);
	}
	
	@Override
	public void init() {
		setBackgroundColor(Colors.VIEW_BG);
		waveformWidth = width - SNAP_DIST;
		initBackgroundOutlineVb();
		update();
	}
	
	private float sampleToX(float sample) {
		return (sample - sampleOffset) * waveformWidth / sampleWidth + SNAP_DIST / 2;
	}

	private float xToSample(float x) {
		return (x - SNAP_DIST / 2) * sampleWidth / waveformWidth + sampleOffset;
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

	private void updateZoom() {
		float x1 = zoomLeftPointerX;
		float x2 = zoomRightPointerX;
		// set sampleOffset and sampleWidth such that the zoom
		// anchor samples stay under x1 and x2
		float newSampleWidth = waveformWidth
				* (zoomRightAnchorSample - zoomLeftAnchorSample) / (x2 - x1);
		float newSampleOffset = zoomRightAnchorSample - newSampleWidth
				* (x2 - SNAP_DIST / 2) / waveformWidth;
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
			sampleWidth = zoomRightAnchorSample * waveformWidth / x2;
		} else if (newSampleOffset + newSampleWidth > TrackManager.currTrack
				.getNumSamples()) {
			sampleWidth = waveformWidth
					* (zoomLeftAnchorSample - TrackManager.currTrack
							.getNumSamples())
					/ (x1 - waveformWidth);
			sampleOffset = TrackManager.currTrack.getNumSamples() - sampleWidth;
		}
		updateVbs();
	}

	private void updateVbs() {
		// update the display location of the loop markers
		initLoopMarkerVb();
		updateWaveformVb();
		requestRender();
	}

	@Override
	public void draw() {
		drawLines(backgroundOutlineVb, Colors.WHITE, 4, GL10.GL_LINE_LOOP);
		drawLoopSelectionHighlightRect();
		drawWaveform();
		drawLoopSelectionMarkers();
	}

	private boolean selectLoopMarker(int id, float x) {
		if (Math.abs(x - sampleToX(TrackManager.currTrack.getLoopBeginSample())) < SNAP_DIST) {
			// begin loop marker touched
			beginLoopPointerId = id;
			return true;
		} else if (Math.abs(x
				- sampleToX(TrackManager.currTrack.getLoopEndSample())) < SNAP_DIST) {
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
		if (beginLoopPointerId == id) {
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
		} else if (endLoopPointerId == id) {
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
		updateVbs();
		return true;
	}

	private boolean setZoomAnchor(int id, float x) {
		if (scrollPointerId != -1) {
			// one pointer is already scrolling.
			
			zoomLeftPointerX = Math.min(pointerIdToPos.get(scrollPointerId).x, x);
			zoomRightPointerX = Math.max(pointerIdToPos.get(scrollPointerId).x, x);
			zoomLeftAnchorSample = xToSample(zoomLeftPointerX);
			zoomRightAnchorSample = xToSample(zoomRightPointerX);
			zoomLeftPointerId = zoomRightAnchorSample == scrollAnchorSample ? id : scrollPointerId;
			zoomRightPointerId = zoomLeftPointerId == scrollPointerId ? id : scrollPointerId;
			scrollPointerId = -1; // not scrolling anymore
			return true;
		}
		return false;
	}

	private void setScrollAnchor(int id, float x) {
		scrollPointerId = id;
		scrollAnchorSample = xToSample(x);
	}

	private void scroll(float scrollX) {
		if (scrollPointerId == -1)
			return; // not scrolling
		updateSampleOffset(scrollX);
		updateVbs();
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		if (!selectLoopMarker(id, x)) {
			// loop marker not close enough to select. start scroll
			// (we know it's the first pointer down, so we're not zooming)
			setScrollAnchor(id, x);
		}
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		scrollPointerId = beginLoopPointerId = endLoopPointerId = zoomLeftPointerId = zoomRightPointerId = -1;
		scrollAnchorSample = zoomLeftAnchorSample = zoomRightAnchorSample = -1;
		requestRender();
	}

	@Override
	protected void handleActionPointerDown(int id, float x, float y) {
		if (!selectLoopMarker(id, x) && !setZoomAnchor(id, x)) {
			// loop marker not close enough to select, and first pointer down.
			// start scrolling
			setScrollAnchor(id, x);
		}
	}

	@Override
	protected void handleActionPointerUp(int id, float x, float y) {
		deselectLoopMarker(id);
		// stop zooming
		if (id == zoomLeftPointerId) {
			setScrollAnchor(zoomRightPointerId, zoomRightPointerX);
		} else if (id == zoomRightPointerId) {
			setScrollAnchor(zoomLeftPointerId, zoomLeftPointerX);
		}
		zoomLeftPointerId = zoomRightPointerId = -1;
		zoomLeftAnchorSample = zoomRightAnchorSample = -1;
	}

	@Override
	protected void handleActionMove(int id, float x, float y) {
		if (id == zoomLeftPointerId) {
			zoomLeftPointerX = x;
		} else if (id == zoomRightPointerId) {
			zoomRightPointerX = x;
			updateZoom(); // only zoom once both actions have been handled
		} else if (!moveLoopMarker(id, x)) {
			scroll(x);
		}
	}

	@Override
	public void loadIcons() {
		//no icons
	}
}
