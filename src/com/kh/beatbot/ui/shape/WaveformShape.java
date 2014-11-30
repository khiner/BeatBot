package com.kh.beatbot.ui.shape;

import android.util.SparseArray;

import com.kh.beatbot.manager.TrackManager;

public class WaveformShape extends Shape {
	private final static float MAX_SPP = 0.5f;
	private long offsetInFrames, widthInFrames;
	private float xOffset, numSamples, loopBeginX, loopEndX;
	private SparseArray<Float> sampleBuffer = new SparseArray<Float>();

	public WaveformShape(RenderGroup group, float width, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, Rectangle.FILL_INDICES, getStrokeIndices(width),
				Rectangle.NUM_FILL_VERTICES, (int) (width * MAX_SPP * 3));
		this.width = width;
		// 3 window-lengths
	}

	private static short[] getStrokeIndices(float width) {
		short[] strokeIndices = new short[(int) (width * MAX_SPP * 3 * 2) + 2];
		// degenerate line begin
		strokeIndices[0] = 0;
		strokeIndices[1] = 1;
		for (int i = 1; i < (strokeIndices.length - 1) / 2; i++) {
			strokeIndices[i * 2] = (short) i;
			strokeIndices[i * 2 + 1] = (short) (i + 1);
		}

		// degenerate line end
		strokeIndices[strokeIndices.length - 2] = strokeIndices[strokeIndices.length - 4];
		strokeIndices[strokeIndices.length - 1] = strokeIndices[strokeIndices.length - 3];
		return strokeIndices;
	}

	/*
	 * Read samples from disk at the current granularity
	 */
	public synchronized void resample() {
		sampleBuffer.clear();
		float numFrames = TrackManager.currTrack.getNumFrames();
		for (float s = -numSamples; s < numSamples * 2; s++) {
			int sampleIndex = (int) (offsetInFrames + s * widthInFrames / numSamples);
			if (sampleIndex < 0)
				continue;
			else if (sampleIndex >= numFrames)
				break;
			sampleBuffer.put(sampleIndex, TrackManager.currTrack.getSample(sampleIndex, 0));
		}

		resetIndices();
		updateWaveformVertices();
	}

	protected synchronized void updateVertices() {
		updateLoopSelectionVertices();
		updateWaveformVertices();
	}

	public synchronized void update(float loopBeginX, float loopEndX, long offsetInFrames,
			long widthInFrames, float xOffset) {
		boolean loopSelectionChanged = this.loopBeginX != loopBeginX || this.loopEndX != loopEndX;
		this.loopBeginX = loopBeginX;
		this.loopEndX = loopEndX;

		long newWidthInFrames = (long) Math.min(widthInFrames,
				TrackManager.currTrack.getNumFrames());
		boolean waveformChanged = this.offsetInFrames != offsetInFrames
				|| this.widthInFrames != newWidthInFrames || this.xOffset != xOffset;
		this.offsetInFrames = offsetInFrames;
		this.widthInFrames = newWidthInFrames;
		this.xOffset = xOffset;

		if (waveformChanged) {
			float spp = Math.min(MAX_SPP, widthInFrames / width);
			numSamples = (int) (width * spp);
		}
		if (loopSelectionChanged || waveformChanged)
			resetIndices();
		if (loopSelectionChanged)
			updateLoopSelectionVertices();
		if (waveformChanged)
			updateWaveformVertices();
	}

	private synchronized void updateWaveformVertices() {
		if (null == sampleBuffer)
			return;

		float x = this.x;
		float y = this.y + height / 2;

		strokeVertex(x, y);
		for (int i = 0; i < sampleBuffer.size(); i++) {
			int sampleIndex = sampleBuffer.keyAt(i);
			float sample = sampleBuffer.get(sampleIndex);
			float percent = (float) (sampleIndex - offsetInFrames) / (float) widthInFrames;

			strokeVertex(x, y);
			x = this.x + percent * width + xOffset;
			y = this.y + height * (1 - sample) / 2;
		}

		while (!strokeMesh.isFull()) {
			strokeVertex(this.x + Float.MAX_VALUE, this.y + height / 2);
		}
	}

	private synchronized void updateLoopSelectionVertices() {
		fillVertex(x + loopBeginX, y);
		fillVertex(x + loopBeginX, y + height);
		fillVertex(x + loopEndX, y + height);
		fillVertex(x + loopEndX, y);
	}
}