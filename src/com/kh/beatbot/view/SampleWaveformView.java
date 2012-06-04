package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.view.helper.WaveformHelper;

public class SampleWaveformView extends SurfaceViewBase {

	WaveformHelper waveformHelper = null;
	FloatBuffer waveformVB = null;
	FloatBuffer loopMarkerVB = null;
	PlaybackManager playbackManager = null;

	byte[] sampleBytes = null;

	int sampleNum;

	// which pointer id is touching which marker (-1 means no pointer)
	int beginLoopMarkerTouched = -1;
	int endLoopMarkerTouched = -1;

	int sampleLoopBegin = 0;
	int sampleLoopEnd = 0; // to be set with the sample byte-length

	public SampleWaveformView(Context c, AttributeSet as) {
		super(c, as);
	}

	public void setPlaybackManager(PlaybackManager playbackManager) {
		this.playbackManager = playbackManager;
	}

	public void setSampleNum(int sampleNum) {
		this.sampleNum = sampleNum;
	}

	public void setSampleBytes(byte[] sampleBytes) {
		this.sampleBytes = sampleBytes;
		sampleLoopEnd = sampleBytes.length;
	}

	private void drawWaveform() {
		if (waveformVB == null)
			return;
		gl.glColor4f(0, 0, 1, 1);
		gl.glLineWidth(1);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, waveformVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, waveformVB.capacity() / 2);
	}

	private void drawLoopMarkers() {
		if (loopMarkerVB == null)
			return;
		gl.glColor4f(1, 1, 1, 1); // white for now
		gl.glLineWidth(10); // width of 10 for now
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, loopMarkerVB);
		gl.glDrawArrays(GL10.GL_LINES, 0, loopMarkerVB.capacity() / 2);
	}

	@Override
	protected void init() {
		waveformHelper = new WaveformHelper(width, height);
		while (sampleBytes == null)
			; // wait until we're sure sampleBytes has been passed in
		waveformVB = waveformHelper.bytesToFloatBuffer(sampleBytes);
		initLoopMarkerVB();
	}

	private void initLoopMarkerVB() {
		float xLoopBegin = sampleToX(sampleLoopBegin);
		float xLoopEnd = sampleToX(sampleLoopEnd);
		float[] loopMarkerVertices = { xLoopBegin, 0, xLoopBegin, height,
				xLoopEnd, 0, xLoopEnd, height };

		loopMarkerVB = makeFloatBuffer(loopMarkerVertices);
	}

	float sampleToX(int sample) {
		return width * (float) sample / sampleBytes.length;
	}

	int xToSample(float x) {
		return (int) (x * sampleBytes.length / width);
	}

	@Override
	protected void drawFrame() {
		gl.glClearColor(.2f, .2f, .2f, 1);
		drawWaveform();
		drawLoopMarkers();
	}

	private void selectLoopMarker(int id, float x) {
		if (Math.abs(x - sampleToX(sampleLoopBegin)) < 35) {
			// begin loop marker touched
			beginLoopMarkerTouched = id;
		} else if (Math.abs(x - sampleToX(sampleLoopEnd)) < 35) {
			// end loop marker touched
			endLoopMarkerTouched = id;
		}
	}

	private void deselectLoopMarker(int id) {
		if (beginLoopMarkerTouched == id) {
			beginLoopMarkerTouched = -1;
		} else if (endLoopMarkerTouched == id) {
			endLoopMarkerTouched = -1;
		}
	}

	private void moveLoopMarker(int id, float x) {
		if (beginLoopMarkerTouched == id) {
			int newLoopBegin = xToSample(x);
			sampleLoopBegin = newLoopBegin < 0 ? 0
					: (newLoopBegin >= sampleLoopEnd - 512 ? sampleLoopEnd - 512
							: newLoopBegin);
		} else if (endLoopMarkerTouched == id) {
			int newLoopEnd = xToSample(x);
			sampleLoopEnd = newLoopEnd >= sampleBytes.length ? sampleBytes.length - 1
					: (newLoopEnd <= sampleLoopBegin + 512 ? sampleLoopBegin + 512
							: newLoopEnd);
		} else {
			return;
		}
		// update UI
		initLoopMarkerVB();
		// update sample playback. need to divide by two since we have bytes and
		// the
		// native audio samples are in shorts
		playbackManager.setLoopWindow(sampleNum, sampleLoopBegin / 2,
				sampleLoopEnd / 2);
	}

	public void handleActionDown(int id, float x) {
		selectLoopMarker(id, x);
	}

	public void handleActionUp(int id, float x) {
		beginLoopMarkerTouched = endLoopMarkerTouched = -1;
	}

	public void handleActionPointerDown(int id, float x) {
		selectLoopMarker(id, x);
	}

	public void handleActionPointerUp(int id, float x) {
		deselectLoopMarker(id);
	}

	public void handleActionMove(MotionEvent e) {
		for (int i = 0; i < e.getPointerCount(); i++) {
			int id = e.getPointerId(i);
			moveLoopMarker(id, e.getX(i));
		}
	}
}
