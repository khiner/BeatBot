package com.kh.beatbot.view.mesh;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class ShapeGroup {

	private MeshGroup fillGroup, outlineGroup;

	public ShapeGroup() {
		fillGroup = new MeshGroup();
		outlineGroup = new MeshGroup();
	}
	
	public void render(GL11 gl, int borderWidth) {
		fillGroup.render(GL10.GL_TRIANGLES);
		gl.glLineWidth(borderWidth);
		outlineGroup.render(GL10.GL_LINES);
	}
	
	public void addMeshPair(Mesh2D fillMesh, Mesh2D outlineMesh) {
		fillGroup.addMesh(fillMesh);
		outlineGroup.addMesh(outlineMesh);
	}
	
	public void removeMeshPair(Mesh2D fillMesh, Mesh2D outlineMesh) {
		fillGroup.removeMesh(fillMesh);
		outlineGroup.removeMesh(outlineMesh);
	}
	
	public void replaceMeshPair(Mesh2D oldFillMesh, Mesh2D oldOutlineMesh,
			Mesh2D newFillMesh, Mesh2D newOutlineMesh) {
		fillGroup.replaceMesh(oldFillMesh, newFillMesh);
		outlineGroup.replaceMesh(oldOutlineMesh, newOutlineMesh);
	}
}
