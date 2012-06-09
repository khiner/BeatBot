package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.view.helper.WaveformHelper;

public class SampleWaveformView extends SurfaceViewBase {

	private FloatBuffer waveformVB = null;
	private FloatBuffer loopMarkerVB = null;
	private PlaybackManager playbackManager = null;
	
	// min distance for pointer to select loop markers
	private final int SNAP_DIST = 35;
	
	private final int MIN_LOOP_WINDOW = 32;
	// the left of this view is a preview button
	private float previewButtonWidth, waveformWidth;

	private int trackNum;
	
	private int numSamples = 0;

	// which pointer id is touching which marker (-1 means no pointer)
	private int beginLoopMarkerTouched = -1;
	private int endLoopMarkerTouched = -1;
	
	// keep track of which finger is touching the preview button,
	// so we can handle pointer-up/ move events outside the button
	private int previewPointerId = -1;

	private float sampleLoopBegin = 0;
	private float sampleLoopEnd = 0; // to be set with the sample byte-length
	
	private float zoomLeftAnchorSample = -1;
	private float zoomRightAnchorSample = -1;

	private float scrollAnchorSample = -1;
	private int scrollVelocity = 0;

	private byte[] sampleBytes = null;
	// zooming/scrolling will change the view window of the samples
	// keep track of that with offset and width
	private float sampleOffset = 0;
	private float sampleWidth;
	
	public SampleWaveformView(Context c, AttributeSet as) {
		super(c, as);
	}

	public void setPlaybackManager(PlaybackManager playbackManager) {
		this.playbackManager = playbackManager;
	}

	public void setTrackNum(int trackNum) {
		this.trackNum = trackNum;
	}

	public void setSampleBytes(byte[] sampleBytes) {
		this.sampleBytes = sampleBytes;
		numSamples = sampleBytes.length/2;
		sampleLoopEnd = numSamples;
		sampleWidth = numSamples;		
	}
	
	private void drawWaveform() {
		if (waveformVB == null)
			return;
		float scale = (waveformWidth*WaveformHelper.DEFAULT_SPP)/((float)sampleWidth);
		float translate = -sampleOffset/WaveformHelper.DEFAULT_SPP;
		gl.glLineWidth(1);
		gl.glColor4f(0, 1, 1, 1);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, waveformVB);		
		gl.glPushMatrix();
		// scale drawing so the entire waveform exactly fits in the view
		gl.glTranslatef(previewButtonWidth, 0, 0);
		gl.glScalef(scale, 1, 1);
		gl.glTranslatef(translate, 0, 0);		
		gl.glDrawArrays(GL10.GL_LINE_STRIP, (int)(sampleOffset/WaveformHelper.DEFAULT_SPP),
				(int)(sampleWidth/WaveformHelper.DEFAULT_SPP));
		gl.glPopMatrix();
	}

	private void drawLoopMarkers() {
		if (loopMarkerVB == null)
			return;
		gl.glColor4f(1, 1, 1, 1); // white for now
		gl.glLineWidth(10); // width of 10 for now
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, loopMarkerVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, loopMarkerVB.capacity() / 2);
		gl.glColor4f(1, 1, 1, .3f); // white for now		
		gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, loopMarkerVB.capacity()/2);
	}

	@Override
	protected void init() {
		previewButtonWidth = width/7;
		waveformWidth = 6*width/7;
		while (sampleBytes == null)
			; // wait until we're sure the sample bytes have been set
		waveformVB = WaveformHelper.bytesToFloatBuffer(sampleBytes, height);		
		initLoopMarkerVB();
	}

	private void initLoopMarkerVB() {
		float xLoopBegin = sampleToX(sampleLoopBegin);
		float xLoopEnd = sampleToX(sampleLoopEnd);
		float[] loopMarkerVertices = { xLoopBegin, 0, xLoopBegin, height,
				xLoopEnd, height, xLoopEnd, 0 };

		loopMarkerVB = makeFloatBuffer(loopMarkerVertices);
	}

	float sampleToX(float sample) {
		return (sample - sampleOffset)*waveformWidth/sampleWidth + previewButtonWidth;
	}

	float xToSample(float x) {
		return (x - previewButtonWidth)*sampleWidth / waveformWidth + sampleOffset;
	}

	void updateSampleOffset(float scrollX) {
		// set sampleOffset such that the scroll anchor sample stays under scrollX
		float newSampleOffset = scrollAnchorSample - (scrollX - previewButtonWidth)*sampleWidth/waveformWidth;
		sampleOffset = newSampleOffset < 0 ?
				0 : (newSampleOffset + sampleWidth > numSamples ?
						numSamples - sampleWidth : newSampleOffset);
	}
	
	void updateZoom(float x1, float x2) {
		// set sampleOffset and sampleWidth such that the zoom
		// anchor samples stay under x1 and x2
		float newSampleWidth = waveformWidth*(zoomRightAnchorSample - zoomLeftAnchorSample)/(x2 - x1);
		float newSampleOffset = zoomRightAnchorSample - newSampleWidth*(x2 - previewButtonWidth)/waveformWidth;
		if (newSampleOffset < 0 && newSampleOffset + newSampleWidth > numSamples ||
				newSampleWidth < MIN_LOOP_WINDOW) {
			return;
		}
		if (newSampleOffset >= 0 && newSampleOffset + newSampleWidth <= numSamples) {
			sampleOffset = newSampleOffset;
			sampleWidth = newSampleWidth;
		} else if (newSampleOffset < 0) {
			sampleOffset = 0;
			sampleWidth = zoomRightAnchorSample*waveformWidth/(x2 - previewButtonWidth);
		} else if (newSampleOffset + newSampleWidth > numSamples) {
			sampleWidth = waveformWidth*(zoomLeftAnchorSample - numSamples)/(x1 - previewButtonWidth - waveformWidth);
			sampleOffset = numSamples - sampleWidth;
		}
	}
	
	void updateLoopMarkers() {
		float diff = 0;
		if (sampleLoopBegin < sampleOffset && sampleLoopEnd > sampleOffset + sampleWidth) {
			clipLoopToWindow();
		} else if (sampleLoopBegin < sampleOffset)
			diff = sampleOffset - sampleLoopBegin;
		else if (sampleLoopEnd >= sampleOffset + sampleWidth)
			diff = sampleOffset + sampleWidth - sampleLoopEnd;
		if (diff != 0) {			
			sampleLoopBegin += diff;
			sampleLoopEnd += diff;
			clipLoopToWindow();
		}
		playbackManager.setLoopWindow(trackNum, (int)sampleLoopBegin, (int)sampleLoopEnd);
		// update the display location of the loop markers
		initLoopMarkerVB();
	}
	
	@Override
	protected void drawFrame() {
		gl.glClearColor(.2f, .2f, .2f, 1);
		drawWaveform();
		drawLoopMarkers();
	}

	private boolean selectLoopMarker(int id, float x) {
		if (Math.abs(x - sampleToX(sampleLoopBegin)) < SNAP_DIST) {
			// begin loop marker touched
			beginLoopMarkerTouched = id;
			return true;
		} else if (Math.abs(x - sampleToX(sampleLoopEnd)) < SNAP_DIST) {
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
			float newLoopBegin = xToSample(x);
			sampleLoopBegin = newLoopBegin < 0 ? 0
					: (newLoopBegin >= sampleLoopEnd - MIN_LOOP_WINDOW ? sampleLoopEnd - MIN_LOOP_WINDOW
							: newLoopBegin);
		} else if (endLoopMarkerTouched == id) {
			float newLoopEnd = xToSample(x);
			sampleLoopEnd = newLoopEnd >= numSamples ? numSamples - 1
					: (newLoopEnd <= sampleLoopBegin + MIN_LOOP_WINDOW ? sampleLoopBegin + MIN_LOOP_WINDOW
							: newLoopEnd);
		} else {
			return false;
		}
		if (sampleLoopBegin < sampleOffset) {
			sampleOffset = sampleLoopBegin;
			if (sampleLoopEnd > sampleOffset + sampleWidth)
				sampleWidth = sampleLoopEnd - sampleLoopBegin;
		} else if (sampleLoopEnd > sampleOffset + sampleWidth) {
			sampleWidth = sampleLoopEnd - sampleOffset;
			if (sampleWidth + sampleOffset > numSamples)
				sampleWidth = numSamples - sampleOffset;
		}
		// update UI
		initLoopMarkerVB();
		// update sample playback.
		playbackManager.setLoopWindow(trackNum, (int)sampleLoopBegin, (int)sampleLoopEnd);
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
		if (sampleLoopBegin < sampleOffset)
			sampleLoopBegin = sampleOffset;
		if (sampleLoopEnd > sampleOffset + sampleWidth)
			sampleLoopEnd = sampleOffset + sampleWidth;
	}
	
	private void handleWaveActionDown(int id, float x) {
		if (!selectLoopMarker(id, x)) {
			// loop marker not close enough to select.  start scroll
			// (we know it's the first pointer down, so we're not zooming)
			setScrollAnchor(x);
		}			
	}

	private void handleWaveActionPointerDown(int id, float x) {
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
			int otherId = (id + 1)%2;
			if (otherId != previewPointerId)
				setScrollAnchor(e.getX(otherId));
		}
		zoomLeftAnchorSample = zoomRightAnchorSample = -1;
	}

	private void handleActionMove(MotionEvent e) {
		if (zoomLeftAnchorSample != -1 && zoomRightAnchorSample != -1
				&& previewPointerId == -1)
			zoom(e.getX(0), e.getX(1));
		for (int i = 0; i < e.getPointerCount(); i++) {
			int id = e.getPointerId(i);
			if (id == previewPointerId) {
				if (e.getX(i) > previewButtonWidth) {					
					// if a click goes down on the preview button, 
					// then moves out of it into wave, stop previewing
					previewPointerId = -1;
					playbackManager.stopTrack(trackNum);
				}
			} else if (!moveLoopMarker(id, e.getX(i)))
				scroll(e.getX(i));
		}
		updateLoopMarkers();
	}
	
	public void handlePreviewActionDown(int id) {
		playbackManager.playTrack(trackNum, .8f, .5f, .5f);
		previewPointerId = id;		
	}
	
	public void handlePreviewActionPointerDown(int id) {
		playbackManager.playTrack(trackNum, .8f, .5f, .5f);
		previewPointerId = id;
	}
	
	public boolean onTouchEvent(MotionEvent e) {		
		// delegating the wave edit touch events through the parent activity to allow
		// touching of multiple views at the same time
		// TODO: still doesn't work!
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_CANCEL:
			return false;
		case MotionEvent.ACTION_DOWN:
			if (e.getX(0) > previewButtonWidth)
				handleWaveActionDown(e.getPointerId(0), e.getX(0));
			else
				handlePreviewActionDown(e.getPointerId(0));
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			int index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if (e.getX(index) > previewButtonWidth)
				handleWaveActionPointerDown(e.getPointerId(index), e.getX(index));
			else
				handlePreviewActionPointerDown(e.getPointerId(index));
			break;
		case MotionEvent.ACTION_MOVE:
			handleActionMove(e);			
			break;
		case MotionEvent.ACTION_POINTER_UP:			
			index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if (e.getX(index) > previewButtonWidth)
				handleWaveActionPointerUp(e, e.getPointerId(index));
			else
				playbackManager.stopTrack(trackNum);				
			break;
		case MotionEvent.ACTION_UP:
			beginLoopMarkerTouched = endLoopMarkerTouched = -1;			
			playbackManager.stopTrack(trackNum);			
			scrollAnchorSample = zoomLeftAnchorSample = zoomRightAnchorSample = -1;			
			break;
		}
		return true;
	}
}
