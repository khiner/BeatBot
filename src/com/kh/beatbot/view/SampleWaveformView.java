package com.kh.beatbot.view;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.global.BeatBotButton;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GeneralUtils;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Track;
import com.kh.beatbot.view.helper.WaveformHelper;

public class SampleWaveformView extends SurfaceViewBase {

	private static final float[] ADSR_COLOR = Colors.PITCH_COLOR.clone();
	private static final float[] ADSR_SELECTED_COLOR = { ADSR_COLOR[0],
			ADSR_COLOR[1], ADSR_COLOR[2], .6f };
	private static final float[] LOOP_HIGHLIGHT_COLOR = { 1, .64706f, 0, .4f };
	private static final float[] LOOP_SELECTION_LINE_COLOR = { 1, 1, 1, 1 };
	private static final float[] LOOP_SELECTION_RECT_COLOR = { .9f, .9f, 1, .5f };
	private static final float[] LOOP_SELECTION_RECT_SELECT_COLOR = {
			Colors.VOLUME_COLOR[0], Colors.VOLUME_COLOR[1],
			Colors.VOLUME_COLOR[2], .6f };
	private static final float[] BG_COLOR = { LOOP_HIGHLIGHT_COLOR[0] * .5f,
			LOOP_HIGHLIGHT_COLOR[1] * .5f, LOOP_HIGHLIGHT_COLOR[2] * .5f, 1 };

	private static final float ADSR_POINT_RADIUS = 5;
	// min distance for pointer to select loop markers
	private static final int SNAP_DIST = 32;
	private static final int SNAP_DIST_SQUARED = 1024;
	private static FloatBuffer waveformVb = null;
	private static FloatBuffer backgroundOutlineVb = null;
	private static FloatBuffer previewButtonSquareVb = null;
	private static FloatBuffer loopSelectionLineVb = null;
	private static FloatBuffer loopSelectionRectVbs[] = new FloatBuffer[2];
	private static FloatBuffer adsrPointVb = null;
	private static FloatBuffer[] adsrCurveVb = new FloatBuffer[4];

	private BeatBotButton previewButton;
	
	// keep track of which pointer ids are selecting which ADSR points
	// init to -1 to indicate no pointer is selecting
	private int[] adsrSelected = new int[] { -1, -1, -1, -1, -1 };

	private boolean showAdsr = false; // show ADSR points?

	private final int MIN_LOOP_WINDOW = 32;
	// the left of this view is a preview button
	private float previewButtonWidth, waveformWidth;

	private Track track;

	private long numSamples = -1;

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

	public void setTrack(Track track) {
		this.track = track;
	}

	public void setSampleFile(File sampleFile) {
		WaveformHelper.setSampleFile(sampleFile);
		numSamples = sampleFile.length() / 8 - 44;
		track.sampleLoopBegin = 0;
		track.sampleLoopEnd = numSamples;
		sampleWidth = numSamples;
		if (height != 0 && sampleWidth != 0)
			updateWaveformVb();
		updateLoopMarkers();
	}

	public void setShowAdsr(boolean on) {
		showAdsr = on;
	}

	private void initAdsrVb() {
		float[] pointVertices = new float[10];
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 2; j++) {
				pointVertices[i * 2 + j] = j == 1 ? adsrToY(track.adsrPoints[i][j])
						: adsrToX(track.adsrPoints[i][j]);
			}
		}
		adsrPointVb = makeFloatBuffer(pointVertices);
		for (int i = 0; i < 4; i++) {
			ArrayList<Float> curveVertices = new ArrayList<Float>();
			curveVertices
					.addAll(makeExponentialCurveVertices(pointVertices[i * 2],
							pointVertices[i * 2 + 1],
							pointVertices[(i + 1) * 2],
							pointVertices[(i + 1) * 2 + 1]));
			float[] converted = new float[curveVertices.size()];
			for (int j = 0; j < curveVertices.size(); j++) {
				converted[j] = curveVertices.get(j);
			}
			adsrCurveVb[i] = makeFloatBuffer(converted);
		}
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
		float xLoopBegin = sampleToX(track.sampleLoopBegin);
		float xLoopEnd = sampleToX(track.sampleLoopEnd);
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
		waveformVb = WaveformHelper.floatFileToBuffer(width - previewButtonWidth, height, (long)sampleOffset, (long)sampleWidth, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<Float> makeExponentialCurveVertices(float x1, float y1,
			float x2, float y2) {
		ArrayList<Float> vertices = new ArrayList<Float>();
		// fake it w/ Bezier curve
		for (float t = 0; t <= 1; t += 0.05) {
			float bezierX = x1;
			float bezierY = y2;
			vertices.add((1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * bezierX + t
					* t * x2);
			vertices.add((1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * bezierY + t
					* t * y2);
		}
		vertices.add(x2);
		vertices.add(y2);
		return vertices;
	}

	private void drawWaveform() {
		if (waveformVb == null)
			return;
		gl.glEnable(GL10.GL_LINE_SMOOTH);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, waveformVb);
		gl.glPushMatrix();
		gl.glTranslatef(previewButtonWidth + SNAP_DIST / 2, 0, 0);
		drawLines(waveformVb, Colors.VOLUME_COLOR, 10, GL10.GL_LINE_STRIP);
		gl.glPopMatrix();
		gl.glDisable(GL10.GL_LINE_SMOOTH);
	}

	private void drawLoopSelectionMarkers() {
		if (loopSelectionLineVb == null)
			return;
		drawLines(loopSelectionLineVb, LOOP_SELECTION_LINE_COLOR, 2,
				GL10.GL_LINES);
		drawTriangleFan(loopSelectionLineVb, LOOP_HIGHLIGHT_COLOR);
		drawTriangleStrip(loopSelectionRectVbs[0],
				beginLoopMarkerTouched == -1 ? LOOP_SELECTION_RECT_COLOR
						: LOOP_SELECTION_RECT_SELECT_COLOR);
		drawTriangleStrip(loopSelectionRectVbs[1],
				endLoopMarkerTouched == -1 ? LOOP_SELECTION_RECT_COLOR
						: LOOP_SELECTION_RECT_SELECT_COLOR);
	}

	private void drawAdsr() {
		if (!showAdsr)
			return;
		setColor(ADSR_COLOR);
		gl.glPointSize(ADSR_POINT_RADIUS * 2);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, adsrPointVb);
		gl.glDrawArrays(GL10.GL_POINTS, 0, 5);
		for (int i = 0; i < 5; i++) {
			if (adsrSelected[i] != -1) {
				gl.glPointSize(ADSR_POINT_RADIUS * 4);
				setColor(ADSR_SELECTED_COLOR);
				gl.glDrawArrays(GL10.GL_POINTS, i, 1);
			}
		}
		for (int i = 0; i < adsrCurveVb.length; i++) {
			drawLines(adsrCurveVb[i], ADSR_COLOR, 3, GL10.GL_LINE_STRIP);
		}
	}

	@Override
	protected void init() {
		// preview button is 80dpX80dp, but that is not the same as a height of
		// '80'. 80dp will be the height
		previewButton = new BeatBotButton(GlobalVars.previewIcon);
		setBackgroundColor(BG_COLOR);
		previewButtonWidth = height;
		waveformWidth = width - previewButtonWidth - SNAP_DIST;
		setSampleFile(track.getInstrument().getCurrSampleFile());
		initBackgroundOutlineVb();
		initPreviewButtonSquareVb();
		initLoopMarkerVb();
		initAdsrVb();
	}

	private void stopPreviewing() {
		previewPointerId = -1;
		previewButton.release();
		track.stop();
	}
	
	private float sampleToX(float sample) {
		return (sample - sampleOffset) * waveformWidth / sampleWidth
				+ previewButtonWidth + SNAP_DIST / 2;
	}

	private float xToSample(float x) {
		return (x - previewButtonWidth - SNAP_DIST / 2) * sampleWidth
				/ waveformWidth + sampleOffset;
	}

	private float adsrToX(float adsr) {
		return sampleToX(track.sampleLoopBegin)
				* (1 - adsr) + adsr
				* sampleToX(track.sampleLoopEnd);
	}

	private float adsrToY(float adsr) {
		return -(adsr - 1) * (height - 2 * ADSR_POINT_RADIUS)
				+ ADSR_POINT_RADIUS;
	}

	private float xToAdsr(float x) {
		return (x - sampleToX(track.sampleLoopBegin))
				/ (sampleToX(track.sampleLoopEnd) - sampleToX(track.sampleLoopBegin));
	}

	private float yToAdsr(float y) {
		// clip y to half an adsr circle above 0 and half a circle below height
		y = y > ADSR_POINT_RADIUS ? (y < height - ADSR_POINT_RADIUS ? y
				: height - ADSR_POINT_RADIUS) : ADSR_POINT_RADIUS;
		return 1 - (y - ADSR_POINT_RADIUS) / (height - 2 * ADSR_POINT_RADIUS);
	}

	private void updateSampleOffset(float scrollX) {
		// set sampleOffset such that the scroll anchor sample stays under
		// scrollX
		float newSampleOffset = scrollAnchorSample - xToSample(scrollX)
				+ sampleOffset;
		sampleOffset = newSampleOffset < 0 ? 0
				: (newSampleOffset + sampleWidth > numSamples ? numSamples
						- sampleWidth : newSampleOffset);
	}

	private void updateZoom(float x1, float x2) {
		// set sampleOffset and sampleWidth such that the zoom
		// anchor samples stay under x1 and x2
		float newSampleWidth = waveformWidth
				* (zoomRightAnchorSample - zoomLeftAnchorSample) / (x2 - x1);
		float newSampleOffset = zoomRightAnchorSample - newSampleWidth
				* (x2 - previewButtonWidth - SNAP_DIST / 2) / waveformWidth;
		if (newSampleOffset < 0
				&& newSampleOffset + newSampleWidth > numSamples
				|| newSampleWidth < MIN_LOOP_WINDOW) {
			return;
		}
		if (newSampleOffset >= 0
				&& newSampleOffset + newSampleWidth <= numSamples) {
			sampleOffset = newSampleOffset;
			sampleWidth = newSampleWidth;
		} else if (newSampleOffset < 0) {
			sampleOffset = 0;
			sampleWidth = zoomRightAnchorSample * waveformWidth
					/ (x2 - previewButtonWidth);
		} else if (newSampleOffset + newSampleWidth > numSamples) {
			sampleWidth = waveformWidth * (zoomLeftAnchorSample - numSamples)
					/ (x1 - previewButtonWidth - waveformWidth);
			sampleOffset = numSamples - sampleWidth;
		}
	}

	private void updateLoopMarkers() {
		float diff = 0;
		if (track.sampleLoopBegin < sampleOffset
				&& track.sampleLoopEnd > sampleOffset
						+ sampleWidth) {
			clipLoopToWindow();
		} else if (track.sampleLoopBegin < sampleOffset)
			diff = sampleOffset
					- track.sampleLoopBegin;
		else if (track.sampleLoopEnd >= sampleOffset
				+ sampleWidth)
			diff = sampleOffset + sampleWidth
					- track.sampleLoopEnd;
		if (diff != 0) {
			track.sampleLoopBegin += diff;
			track.sampleLoopEnd += diff;
			clipLoopToWindow();
		}
		track.setLoopWindow((long)track.sampleLoopBegin, (long)track.sampleLoopEnd);
		// update the display location of the loop markers
		initLoopMarkerVb();
		initAdsrVb();
		updateWaveformVb();
	}

	@Override
	protected void drawFrame() {
		drawTriangleStrip(previewButtonSquareVb, Colors.BG_COLOR);
		previewButton.draw(0, 0, height, height);
		drawWaveform();
		drawLines(backgroundOutlineVb, Colors.WHITE, 4, GL10.GL_LINE_LOOP);
		drawLoopSelectionMarkers();
		if (showAdsr)
			drawAdsr();
	}

	private boolean selectAdsrPoint(int id, float x, float y) {
		if (!showAdsr)
			return false;
		for (int i = 0; i < 5; i++) {
			if (GeneralUtils.distanceFromPointSquared(
					adsrToX(track.adsrPoints[i][0]),
					adsrToY(track.adsrPoints[i][1]),
					x, y) < SNAP_DIST_SQUARED) {
				adsrSelected[i] = id;
				return true;
			}
		}
		return false;
	}

	private boolean selectLoopMarker(int id, float x) {
		if (Math.abs(x
				- sampleToX(track.sampleLoopBegin)) < SNAP_DIST) {
			// begin loop marker touched
			beginLoopMarkerTouched = id;
			return true;
		} else if (Math.abs(x
				- sampleToX(track.sampleLoopEnd)) < SNAP_DIST) {
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

	private void deselectAdsrPoint(int id) {
		for (int i = 0; i < adsrSelected.length; i++) {
			if (adsrSelected[i] == id)
				adsrSelected[i] = -1;
		}
	}

	private void clearAdsrSelected() {
		for (int i = 0; i < adsrSelected.length; i++) {
			adsrSelected[i] = -1;
		}
	}

	private boolean moveAdsrPoint(int id, float x, float y) {
		for (int i = 0; i < adsrSelected.length; i++) {
			if (adsrSelected[i] == id) {
				float adsrX = xToAdsr(x);
				float adsrY = yToAdsr(y);
				float prevX = i >= 2 ? track.adsrPoints[i - 1][0]
						: 0;
				float nextX = i <= 3 ? track.adsrPoints[i + 1][0]
						: 1;
				if (i == 0)
					adsrX = 0;
				// ADSR samples cannot go past the next ADSR sample or before
				// the previous sample
				track.adsrPoints[i][0] = adsrX > prevX ? (adsrX < nextX ? adsrX
						: nextX)
						: prevX;
				if (i != 3) // can only change the x coord of the cutoff point
					track.adsrPoints[i][1] = adsrY > 0 ? (adsrY < 1 ? adsrY
							: 1)
							: 0;
				// points 2 and 3 must have the same y value, since these are
				// the two ends
				// of the sustain level, which must be linear.
				// ie. adjusting either 2 or 3 will adjust both points' y values
				if (i == 2)
					track.adsrPoints[3][1] = track.adsrPoints[2][1];
				initAdsrVb();
				track.setAdsrPoint(i,
						track.adsrPoints[i][0],
						track.adsrPoints[i][1]);
				return true;
			}
		}
		return false;
	}

	private boolean moveLoopMarker(int id, float x) {
		if (beginLoopMarkerTouched == id) {
			float newLoopBegin = xToSample(x);
			track.sampleLoopBegin = newLoopBegin < 0 ? 0
					: (newLoopBegin >= track.sampleLoopEnd
							- MIN_LOOP_WINDOW ? track.sampleLoopEnd
							- MIN_LOOP_WINDOW
							: newLoopBegin);
		} else if (endLoopMarkerTouched == id) {
			float newLoopEnd = xToSample(x);
			track.sampleLoopEnd = newLoopEnd >= numSamples ? numSamples - 1
					: (newLoopEnd <= track.sampleLoopBegin
							+ MIN_LOOP_WINDOW ? track.sampleLoopBegin
							+ MIN_LOOP_WINDOW
							: newLoopEnd);
		} else {
			return false;
		}
		if (track.sampleLoopBegin < sampleOffset) {
			sampleOffset = track.sampleLoopBegin;
			if (track.sampleLoopEnd > sampleOffset
					+ sampleWidth)
				sampleWidth = track.sampleLoopEnd
						- track.sampleLoopBegin;
		} else if (track.sampleLoopEnd > sampleOffset
				+ sampleWidth) {
			sampleWidth = track.sampleLoopEnd
					- sampleOffset;
			if (sampleWidth + sampleOffset > numSamples)
				sampleWidth = numSamples - sampleOffset;
		}
		// update UI
		initLoopMarkerVb();
		initAdsrVb();
		// update sample playback.
		updateLoopMarkers();
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

	private void clipLoopToWindow() {
		if (track.sampleLoopBegin < sampleOffset)
			track.sampleLoopBegin = sampleOffset;
		if (track.sampleLoopEnd > sampleOffset
				+ sampleWidth)
			track.sampleLoopEnd = sampleOffset
					+ sampleWidth;
	}

	private void handleWaveActionDown(int id, float x, float y) {
		if (!selectAdsrPoint(id, x, y) && !selectLoopMarker(id, x)) {
			// no ADSR points are close enough, and
			// loop marker not close enough to select. start scroll
			// (we know it's the first pointer down, so we're not zooming)
			setScrollAnchor(x);
		}
	}

	private void handleWaveActionPointerDown(int id, float x, float y) {
		if (!selectAdsrPoint(id, x, y) && !selectLoopMarker(id, x)
				&& !setZoomAnchor(x)) {
			// loop marker not close enough to select, and first pointer down.
			// start scrolling
			setScrollAnchor(x);
		}
	}

	private void handleWaveActionPointerUp(MotionEvent e, int id) {
		deselectLoopMarker(id);
		deselectAdsrPoint(id);
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
		previewButton.touch();
		track.play();
		previewPointerId = id;
	}

	public void handlePreviewActionPointerDown(int id) {
		handlePreviewActionDown(id);
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
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

	@Override
	protected void handleActionMove(MotionEvent e) {
		if (zoomLeftAnchorSample != -1 && zoomRightAnchorSample != -1
				&& previewPointerId == -1)
			zoom(e.getX(0), e.getX(1));
		for (int i = 0; i < e.getPointerCount(); i++) {
			int id = e.getPointerId(i);
			if (id == previewPointerId) {
				if (e.getX(i) > previewButtonWidth) {
					// if a click goes down on the preview button,
					// then moves out of it into wave, stop previewing
					stopPreviewing();
				}
			} else if (!moveAdsrPoint(id, e.getX(i), e.getY(i))
					&& !moveLoopMarker(id, e.getX(i)))
				scroll(e.getX(i));
		}
		updateLoopMarkers();
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		if (x > previewButtonWidth)
			handleWaveActionPointerUp(e, id);
		else {
			previewButton.release();
			track.stop();
		}
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		previewButton.release();
		beginLoopMarkerTouched = endLoopMarkerTouched = -1;
		track.stop();
		scrollAnchorSample = zoomLeftAnchorSample = zoomRightAnchorSample = -1;
		clearAdsrSelected();
	}
}