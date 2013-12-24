package com.kh.beatbot.ui.mesh;

import android.util.SparseArray;

import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.View;

public class WaveformShape extends Shape {
	private final static float MAX_SPP = 0.5f;
	private long offsetInSamples, widthInSamples;
	private float xOffset, numSamples, loopBeginX, loopEndX;
	private SparseArray<Float> sampleBuffer = new SparseArray<Float>();
	
	public WaveformShape(ShapeGroup group, float width) {
		super(group);
		this.width = width;
	}

	protected int getNumFillVertices() {
		return 6; // two triangles
	}

	protected int getNumStrokeVertices() {
		return (int) (width * MAX_SPP * 3 * 2); // 3 window-lengths, 2 vertices for each sample
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
		float lastX = 0;
		float lastY = height / 2;
		for (int i = 0; i < sampleBuffer.size(); i++) {
			int sampleIndex = sampleBuffer.keyAt(i);
			float sample = sampleBuffer.get(sampleIndex);

			float percent = (float) (sampleIndex - offsetInSamples)
					/ (float) widthInSamples;
			float x = percent * width + xOffset;
			float y = height * (1 - sample) / 2;

			strokeVertex(x, y);
			strokeVertex(lastX, lastY);
			lastX = x;
			lastY = y;
		}
		while (strokeMesh.index < strokeMesh.numVertices) {
			strokeVertex(Float.MAX_VALUE, height / 2);
		}
	}

	private void updateLoopSelectionVertices() {
		// fill triangle 1
		fillVertex(loopBeginX, View.BG_OFFSET);
		fillVertex(loopEndX, View.BG_OFFSET);
		fillVertex(loopBeginX, height - View.BG_OFFSET);
		// fill triangle 2
		fillVertex(loopBeginX, height - View.BG_OFFSET);
		fillVertex(loopEndX, View.BG_OFFSET);
		fillVertex(loopEndX, height - View.BG_OFFSET);
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