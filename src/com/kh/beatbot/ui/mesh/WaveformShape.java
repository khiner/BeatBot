package com.kh.beatbot.ui.mesh;

import javax.microedition.khronos.opengles.GL10;

import android.util.SparseArray;

import com.kh.beatbot.manager.TrackManager;

public class WaveformShape extends Shape {
	private final static float MAX_SPP = 0.5f;
	private long offsetInSamples, widthInSamples;
	private float xOffset, numSamples, loopBeginX, loopEndX;
	private SparseArray<Float> sampleBuffer = new SparseArray<Float>();

	public WaveformShape(ShapeGroup group, float width) {
		super(group);
		this.group.setStrokePrimitiveType(GL10.GL_LINE_STRIP);
		this.width = width;
	}

	protected int getNumFillVertices() {
		return 6; // two triangles
	}

	protected int getNumStrokeVertices() {
		return (int) (width * 3 * MAX_SPP);
	}

	/*
	 * Read samples from disk at the current granularity
	 */
	public synchronized void resample() {
		sampleBuffer.clear();
		float numFrames = TrackManager.currTrack.getNumFrames();
		for (float s = -numSamples; s < numSamples * 2; s++) {
			int sampleIndex = (int) (offsetInSamples + s * widthInSamples
					/ numSamples);
			if (sampleIndex < 0)
				continue;
			else if (sampleIndex >= numFrames)
				break;
			sampleBuffer.put(sampleIndex,
					TrackManager.currTrack.getSample(sampleIndex, 0));
		}

		updateWaveformVertices();
	}

	protected void updateVertices() {
		updateLoopSelectionVertices();
		updateWaveformVertices();
	}

	private synchronized void updateWaveformVertices() {
		for (int i = 0; i < sampleBuffer.size(); i++) {
			int sampleIndex = sampleBuffer.keyAt(i);
			float sample = sampleBuffer.get(sampleIndex);

			float percent = (float) (sampleIndex - offsetInSamples)
					/ (float) widthInSamples;
			float x = percent * width + xOffset;
			float y = height * (1 - sample) / 2;

			strokeVertex(x, y);
		}
		while (strokeMesh.index < strokeMesh.numVertices) {
			strokeVertex(Float.MAX_VALUE, height / 2);
		}
	}

	private void updateLoopSelectionVertices() {
		// fill triangle 1
		fillVertex(loopBeginX, 0);
		fillVertex(loopEndX, 0);
		fillVertex(loopBeginX, height);
		// fill triangle 2
		fillVertex(loopBeginX, height);
		fillVertex(loopEndX, 0);
		fillVertex(loopEndX, height);
	}

	public void update(long offsetInSamples, long widthInSamples, float xOffset) {
		this.offsetInSamples = offsetInSamples;
		this.widthInSamples = widthInSamples;
		this.xOffset = xOffset;
		float spp = Math.min(MAX_SPP, widthInSamples / width);
		numSamples = (int) (width * spp);
		resetIndices();
		updateWaveformVertices();
	}

	public void setLoopPoints(float beginX, float endX) {
		loopBeginX = beginX;
		loopEndX = endX;
		resetIndices();
		updateLoopSelectionVertices();
	}
}