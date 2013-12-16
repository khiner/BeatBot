package com.kh.beatbot.ui.mesh;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Colors;

public class WaveformShape extends Shape {

	private long offset, numFloats;
	private float xOffset, numSamples, loopBeginX, loopEndX;

	protected int getNumFillVertices() {
		return 6; // two triangles
	}

	protected int getNumStrokeVertices() {
		return (int) (width * 2);
	}

	public WaveformShape(ShapeGroup group, float width) {
		super(group);
		this.group.setOutlinePrimitiveType(GL10.GL_LINE_STRIP);
		this.width = width;
	}

	protected void updateVertices() {
		// fill triangle 1
		fillVertex(loopBeginX, 0);
		fillVertex(loopEndX, 0);
		fillVertex(loopBeginX, height);
		// fill triangle 2
		fillVertex(loopBeginX, height);
		fillVertex(loopEndX, 0);
		fillVertex(loopEndX, height);

		for (int x = 0; x < numSamples; x++) {
			float percent = (float) x / numSamples;
			int sampleIndex = (int) (offset + percent * numFloats);
			float sample = TrackManager.currTrack.getSample(sampleIndex, 0);

			strokeVertex(percent * width + xOffset, height * (1 - sample) / 2);
		}

		for (int i = (int) numSamples - 1; i >= 0 && i < getNumStrokeVertices(); i++) {
			getStrokeMesh().setColor(i, Colors.TRANSPARANT);
		}
	}

	public void update(long offset, long numFloats, float xOffset) {
		this.offset = offset;
		this.numFloats = numFloats;
		this.xOffset = xOffset;
		float spp = Math.min(.5f, numFloats / width);
		numSamples = (int) (width * spp);
		update();
	}

	public void updateLoopSelection(float beginX, float endX) {
		loopBeginX = beginX;
		loopEndX = endX;
		update();
	}
}