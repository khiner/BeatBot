package com.kh.beatbot.view.mesh;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.util.Log;

import com.kh.beatbot.view.BBView;

public class MeshGroup {
	private List<Mesh2D> children = new ArrayList<Mesh2D>();
	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer;
	private float[] vertices;
	private float[] colors;
	private int vertexHandle, colorHandle;
	
	private int numVertices = -1;
	
	private boolean dirty = false;
	
	public void draw(int primitiveType) {
		GL11 gl = (GL11)BBView.gl;
		
		if (children.isEmpty()) {
			return;
		}
		
		if (dirty) {
			updateBuffers();
			dirty = false;
		}
		
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexHandle);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, 0);

		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorHandle);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, 0);

		gl.glDrawArrays(primitiveType, 0, numVertices * 2);
		
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
	}
	
	public void addMesh(Mesh2D mesh) {
		children.add(mesh);
		update();
	}
	
	public void removeMesh(Mesh2D mesh) {
		children.remove(mesh);
		update();
	}
	
	public void replaceMesh(Mesh2D oldMesh, Mesh2D newMesh) {
		if (oldMesh.getNumVertices() != newMesh.getNumVertices()) {
			Log.e("MeshGroup", "Attempting to replace a mesh with a new one with different num vertices");
			return;
		}
		if (!children.contains(oldMesh)) {
			Log.e("MeshGroup", "Attempting to update a mesh that is not a child.");
			return;
		}
		
		newMesh.parentVertexIndex = oldMesh.parentVertexIndex;
		children.set(children.indexOf(oldMesh), newMesh);
		update(newMesh);
	}
	
	private void update() {
		initBuffers();
		
		int currVertexIndex = 0;
		for (Mesh2D child : children) {
			child.parentVertexIndex = currVertexIndex;
			update(child);
			currVertexIndex += child.getNumVertices();
		}
	}
	
	private void update(Mesh2D child) {
		if (!children.contains(child)) {
			Log.e("MeshGroup", "Attempting to update a mesh that is not a child.");
			return;
		}
		dirty = true;
		int vertexIndex = child.parentVertexIndex * 2;
		for (float vertex : child.vertices) {
			vertices[vertexIndex++] = vertex;
		}
		
		int colorIndex = child.parentVertexIndex * 4;
		for (float colorValue : child.color) {
			colors[colorIndex++] = colorValue;
		}
	}
	
	private void updateBuffers() {
		GL11 gl = (GL11)BBView.gl;
		
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexHandle);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, vertices.length * 4,
				vertexBuffer, GL11.GL_DYNAMIC_DRAW);
		
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorHandle);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, colors.length * 4,
				colorBuffer, GL11.GL_DYNAMIC_DRAW);
		
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
	}
	
	private void initBuffers() {
		GL11 gl = (GL11)BBView.gl;
		
		int[] buffer = new int[1];
		
		numVertices = calcNumVertices();
		
		vertices = new float[numVertices * 2];
		gl.glGenBuffers(1, buffer, 0);
		vertexHandle = buffer[0];
		vertexBuffer = FloatBuffer.wrap(vertices);
		
		colors = new float[numVertices * 4];
		gl.glGenBuffers(1, buffer, 0);
		colorHandle = buffer[0];
		colorBuffer = FloatBuffer.wrap(colors);
	}
	
	private int calcNumVertices() {
		int totalVertices = 0;
		for (Mesh2D child : children) {
			totalVertices += child.getNumVertices();
		}
		
		return totalVertices;
	}
}
