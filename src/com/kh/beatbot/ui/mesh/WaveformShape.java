package com.kh.beatbot.ui.mesh;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Colors;


public class WaveformShape extends Shape {
	
	private long offset, numFloats;
	private float xOffset, numSamples;

	private float loopBeginX, loopEndX;

	public WaveformShape(ShapeGroup group, float[] fillColor, float[] outlineColor, float width) {
		// 6 vertices for rect fill (two triangles)
		super(group, new Mesh2D(6, fillColor), new Mesh2D((int)(width * 2), outlineColor));
		this.group.setOutlinePrimitiveType(GL10.GL_LINE_STRIP);
	}
	
	/********
	 * ^--^ *
	 * |1/| *
	 * |/2| *
	 * ^--^ *
	 ********/
	protected void createVertices(float[] fillColor) {
		// fill triangle 1
		fillMesh.vertex(loopBeginX, 0);
		fillMesh.vertex(loopEndX, 0);
		fillMesh.vertex(loopBeginX, height);
		// fill triangle 2
		fillMesh.vertex(loopBeginX, height);
		fillMesh.vertex(loopEndX, 0);
		fillMesh.vertex(loopEndX, height);
		
		fillMesh.setColor(fillColor);
	}
	
	public void update(long offset, long numFloats, float xOffset) {
		this.offset = offset;
		this.numFloats = numFloats;
		this.xOffset = xOffset;
		float spp = Math.min(2.0f, numFloats / width);
		numSamples = (int) (width * spp);
		update();
	}
	
	public void updateLoopSelection(float beginX, float endX) {
		loopBeginX = beginX;
		loopEndX = endX;
		update();
	}

	protected void createVertices(float[] fillColor, float[] outlineColor) {
		for (int x = 0; x < numSamples; x++) {
			float percent = (float) x / numSamples;
			int sampleIndex = (int) (offset + percent * numFloats);
			float sample = TrackManager.currTrack.getSample(sampleIndex, 0);
			
			strokeMesh.vertex(percent * width + xOffset, height * (1 - sample) / 2);
		}
		strokeMesh.setColor(outlineColor);
		for (int i = (int)numSamples - 1; i >= 0 && i < strokeMesh.numVertices; i++) {
			strokeMesh.setColor(i, Colors.TRANSPARANT);
		}
		createVertices(fillColor);
	}
}