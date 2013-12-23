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
		this.group.setStrokePrimitiveType(GL10.GL_LINE_STRIP);
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

		for (int s = 0; s < numSamples; s++) {
			float percent = (float) s / numSamples;
			int sampleIndex = (int) (offset + percent * numFloats);
			float sample = TrackManager.currTrack.getSample(sampleIndex, 0);

			float x = percent * width + xOffset;
			float y = height * (1 - sample) / 2;
			
			if (s > numSamples - 2) {
				strokeVertex(x, y, Colors.TRANSPARANT);
			} else {
				strokeVertex(x, y);
			}
		}
	}

	public void update(long offset, long numFloats, float xOffset) {
		this.offset = offset;
		this.numFloats = numFloats;
		this.xOffset = xOffset;
		float spp = Math.min(1, numFloats / width);
		numSamples = (int) (width * spp);
		update();
	}

	public void setLoopPoints(float beginX, float endX) {
		loopBeginX = beginX;
		loopEndX = endX;
		update();
	}
}