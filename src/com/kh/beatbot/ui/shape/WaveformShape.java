package com.kh.beatbot.ui.shape;

import android.util.Log;
import android.util.SparseArray;

import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.view.View;

public class WaveformShape extends Shape {
	private final static float MAX_SPP = 1, BUFFER_RATIO = 4;
	private long offsetInFrames, widthInFrames;
	private float xOffset, numSamples, loopBeginX, loopEndX;
	private SparseArray<Float> sampleBuffer = new SparseArray<Float>();

	public WaveformShape(RenderGroup group, float width, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, Rectangle.FILL_INDICES, getStrokeIndices(width),
				Rectangle.NUM_FILL_VERTICES, (int) (width * MAX_SPP * BUFFER_RATIO));
		this.width = width;
	}

	private static short[] getStrokeIndices(float width) {
		short[] strokeIndices = new short[(int) (width * MAX_SPP * BUFFER_RATIO) * 2 + 2];
		// degenerate line begin
		strokeIndices[0] = 0;
		strokeIndices[1] = 0;
		for (int i = 1; i < (strokeIndices.length - 2) / 2; i++) {
			strokeIndices[i * 2] = (short) (i - 1);
			strokeIndices[i * 2 + 1] = (short) i;
		}

		// degenerate line end
		strokeIndices[strokeIndices.length - 2] = strokeIndices[strokeIndices.length - 3];
		strokeIndices[strokeIndices.length - 1] = strokeIndices[strokeIndices.length - 3];
		return strokeIndices;
	}

	/*
	 * Read samples from disk at the current granularity
	 */
	public void resample() {
		Track track = (Track) View.context.getTrackManager().getCurrTrack();
		sampleBuffer.clear();
		float numFrames = track.getNumFrames();

		for (int frameIndex = (int) (offsetInFrames - widthInFrames * BUFFER_RATIO / 2); frameIndex < offsetInFrames
				+ widthInFrames * BUFFER_RATIO / 2; frameIndex += widthInFrames / numSamples) {
			if (frameIndex < 0)
				continue;
			else if (frameIndex >= numFrames)
				break;
			float[] maxSample = track.getMaxSample(frameIndex, (long) (frameIndex + widthInFrames / numSamples), 0);
			sampleBuffer.put((int) maxSample[0], maxSample[1]);
		}

		resetIndices();
		updateWaveformVertices();
	}

	protected void updateVertices() {
		updateLoopSelectionVertices();
		updateWaveformVertices();
	}

	public void update(float loopBeginX, float loopEndX, long offsetInFrames, long widthInFrames,
			float xOffset) {
		boolean loopSelectionChanged = this.loopBeginX != loopBeginX || this.loopEndX != loopEndX;
		this.loopBeginX = loopBeginX;
		this.loopEndX = loopEndX;

		final Track track = (Track) View.context.getTrackManager().getCurrTrack();
		long newWidthInFrames = (long) Math.min(widthInFrames, track.getNumFrames());
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

	private void updateWaveformVertices() {
		if (null == sampleBuffer)
			return;

		float x = 0;
		float y = 0;

		for (int i = 0; i < sampleBuffer.size(); i++) {
			int sampleIndex = sampleBuffer.keyAt(i);
			float sample = sampleBuffer.get(sampleIndex);
			float percent = (float) (sampleIndex - offsetInFrames) / (float) widthInFrames;
			x = this.x + percent * width + xOffset;
			y = this.y + height * (1 - sample) / 2;
			if (i == 0) {
				strokeVertex(x, y); // starting degenerate line
			}
			strokeVertex(x, y);
		}
		strokeVertex(x, y); // terminating degenerate line

		while (!strokeMesh.isFull())
			strokeVertex(x, y);
	}

	private void updateLoopSelectionVertices() {
		fillVertex(x + loopBeginX, y);
		fillVertex(x + loopBeginX, y + height);
		fillVertex(x + loopEndX, y + height);
		fillVertex(x + loopEndX, y);
	}
}