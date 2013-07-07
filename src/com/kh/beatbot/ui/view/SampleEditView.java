package com.kh.beatbot.ui.view;

import java.io.IOException;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Colors;

public class SampleEditView extends TouchableView {

	// min distance for pointer to select loop markers
	private static final int SNAP_DIST = 32, MIN_LOOP_WINDOW = 32;

	private static FloatBuffer waveformVb = null, backgroundOutlineVb = null,
			loopSelectionLineVb = null,
			loopSelectionRectVbs[] = new FloatBuffer[2];

	// which pointer id is touching which marker (-1 means no pointer)
	private int beginLoopPointerId = -1, endLoopPointerId = -1;

	private int scrollPointerId = -1, zoomLeftPointerId = -1,
			zoomRightPointerId = -1;

	private float scrollAnchorSample = -1, zoomLeftAnchorSample = -1,
			zoomRightAnchorSample = -1;

	// zooming/scrolling will change the view window of the samples
	// keep track of that with offset and width
	private float sampleOffset = 0, numSamples = 0, waveformWidth = 0;

	public void update() {
		numSamples = TrackManager.currTrack.getNumSamples();
		updateVbs();
	}

	private void initBackgroundOutlineVb() {
		backgroundOutlineVb = makeRectFloatBuffer(0, 0, width - 2, height);
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
		try {
			waveformVb = TrackManager.currTrack.getCurrSampleFile()
					.floatFileToBuffer(this, (long) sampleOffset,
							(long) numSamples, SNAP_DIST / 2);
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

	private float sampleToX(float sample) {
		return (sample - sampleOffset) * waveformWidth / numSamples + SNAP_DIST
				/ 2;
	}

	private float xToSample(float x) {
		return (x - SNAP_DIST / 2) * numSamples / waveformWidth + sampleOffset;
	}

	private void updateSampleOffset(float scrollX) {
		// set sampleOffset such that the scroll anchor sample stays under
		// scrollX
		float newSampleOffset = scrollAnchorSample - xToSample(scrollX)
				+ sampleOffset;
		sampleOffset = newSampleOffset < 0 ? 0
				: (newSampleOffset + numSamples > TrackManager.currTrack
						.getNumSamples() ? TrackManager.currTrack
						.getNumSamples() - numSamples : newSampleOffset);
	}

	private void updateZoom() {
		float x1 = pointerIdToPos.get(zoomLeftPointerId).x;
		float x2 = pointerIdToPos.get(zoomRightPointerId).x;

		if (x1 >= x2)
			return; // sanity check

		float ZLAS = zoomLeftAnchorSample, ZRAS = zoomRightAnchorSample;

		float MAX_LOOP_WINDOW = TrackManager.currTrack.getNumSamples();
		// set sampleOffset and sampleWidth such that the zoom
		// anchor samples stay under x1 and x2
		float newNumSamples = waveformWidth * (ZRAS - ZLAS) / (x2 - x1);
		float newSampleOffset = ZRAS - newNumSamples * (x2 - SNAP_DIST / 2)
				/ waveformWidth;

		if (newSampleOffset < 0) {
			sampleOffset = 0;
			numSamples = ZRAS * waveformWidth / (x2 - SNAP_DIST / 2);
			numSamples = numSamples <= MAX_LOOP_WINDOW ? (numSamples >= MIN_LOOP_WINDOW ? numSamples
					: MIN_LOOP_WINDOW)
					: MAX_LOOP_WINDOW;
		} else if (newNumSamples > MAX_LOOP_WINDOW) {
			sampleOffset = newSampleOffset;
			numSamples = MAX_LOOP_WINDOW - sampleOffset;
		} else if (newNumSamples < MIN_LOOP_WINDOW) {
			sampleOffset = newSampleOffset;
			numSamples = MIN_LOOP_WINDOW;
		} else if (newSampleOffset + newNumSamples > MAX_LOOP_WINDOW) {
			numSamples = ((ZLAS - MAX_LOOP_WINDOW) * waveformWidth)
					/ (x1 - SNAP_DIST / 2 - waveformWidth);
			sampleOffset = MAX_LOOP_WINDOW - numSamples;
		} else {
			sampleOffset = newSampleOffset;
			numSamples = newNumSamples;
		}
		updateVbs();
	}

	private void updateVbs() {
		// update the display location of the loop markers
		initLoopMarkerVb();
		updateWaveformVb();
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
		float LBS = TrackManager.currTrack.getLoopBeginSample();
		float LES = TrackManager.currTrack.getLoopEndSample();

		if (id == beginLoopPointerId) {
			// update track loop begin
			float newLoopBegin = xToSample(x);
			TrackManager.currTrack
					.setLoopBeginSample(newLoopBegin < 0 ? 0
							: (newLoopBegin >= TrackManager.currTrack
									.getLoopEndSample() - MIN_LOOP_WINDOW ? TrackManager.currTrack
									.getLoopEndSample() - MIN_LOOP_WINDOW
									: newLoopBegin));
			// update ui to fit the new begin point
			if (LBS < sampleOffset) {
				numSamples += sampleOffset - LBS;
				sampleOffset = LBS;
			} else if (LBS > sampleOffset + numSamples) {
				numSamples = LBS - sampleOffset;
			}
		} else if (id == endLoopPointerId) {
			// update track loop end
			float newLoopEnd = xToSample(x);
			TrackManager.currTrack
					.setLoopEndSample(newLoopEnd >= TrackManager.currTrack
							.getNumSamples() ? TrackManager.currTrack
							.getNumSamples() - 1 : (newLoopEnd <= LBS
							+ MIN_LOOP_WINDOW ? LBS + MIN_LOOP_WINDOW
							: newLoopEnd));
			// update ui to fit the new end point
			if (LES > sampleOffset + numSamples) {
				numSamples = LES - sampleOffset;
			} else if (LES < sampleOffset) {
				numSamples += sampleOffset - LES;
				sampleOffset = LES;
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

		zoomLeftAnchorSample = xToSample(pointerIdToPos.get(zoomLeftPointerId).x);
		zoomRightAnchorSample = xToSample(pointerIdToPos
				.get(zoomRightPointerId).x);
		return true;
	}

	private void setScrollAnchor(int id) {
		scrollPointerId = id;
		scrollAnchorSample = xToSample(pointerIdToPos.get(id).x);
	}

	private void scroll(float scrollX) {
		if (scrollPointerId == -1)
			return; // not scrolling
		updateSampleOffset(scrollX);
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
		scrollAnchorSample = zoomLeftAnchorSample = zoomRightAnchorSample = -1;
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
		zoomLeftAnchorSample = zoomRightAnchorSample = -1;
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
	protected void loadIcons() {
		// no icons
	}

	@Override
	protected void createChildren() {
		// no children
	}

	@Override
	public void layoutChildren() {
		// no children to layout
	}
}
