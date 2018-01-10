package com.odang.beatbot.ui.shape;

import com.odang.beatbot.ui.mesh.Mesh2D;

public abstract class Shape {
    public static final float π = (float) Math.PI;
    protected Mesh2D fillMesh, strokeMesh;
    protected float[] fillColor, strokeColor;

    public float x, y, width, height;

    public Shape(RenderGroup group, float[] fillColor, float[] strokeColor, int numFillVertices,
                 int numStrokeVertices) {
        this(group, fillColor, strokeColor, null, null, numFillVertices, numStrokeVertices);
    }

    public Shape(RenderGroup group, float[] fillColor, float[] strokeColor, short[] fillIndices,
                 short[] strokeIndices, int numFillVertices, int numStrokeVertices) {
        RenderGroup myGroup = null == group ? new RenderGroup() : group;

        fillMesh = new Mesh2D(myGroup.getFillGroup(), numFillVertices, fillIndices);
        setFillColor(fillColor);
        strokeMesh = new Mesh2D(myGroup.getStrokeGroup(), numStrokeVertices, strokeIndices);
        setStrokeColor(strokeColor);
        show();
    }

    protected abstract void updateVertices();

    protected float getFillVertexX(int i) {
        return fillMesh.getGroup().getVertexX(i);
    }

    protected float getFillVertexY(int i) {
        return fillMesh.getGroup().getVertexY(i);
    }

    protected void fillVertex(float x, float y) {
        fillMesh.vertex(x, y, fillColor);
    }

    protected void strokeVertex(float x, float y) {
        strokeMesh.vertex(x, y, strokeColor);
    }

    protected void resetIndices() {
        if (!isVisible())
            return;
        fillMesh.reset();
        strokeMesh.reset();
    }

    public void update() {
        if (isVisible()) {
            resetIndices();
            updateVertices();
        }
    }

    protected void setFillColor(int vertexIndex, float[] fillColor) {
        fillMesh.setColor(vertexIndex, fillColor);
    }

    public void setFillColor(float[] fillColor) {
        this.fillColor = fillColor;
        fillMesh.setColor(fillColor);
    }

    public void setStrokeColor(float[] strokeColor) {
        this.strokeColor = strokeColor;
        strokeMesh.setColor(strokeColor);
    }

    public void setColors(float[] fillColor, float[] strokeColor) {
        setFillColor(fillColor);
        setStrokeColor(strokeColor);
    }

    public Mesh2D getFillMesh() {
        return fillMesh;
    }

    public Mesh2D getStrokeMesh() {
        return strokeMesh;
    }

    public float[] getFillColor() {
        return fillColor;
    }

    public float[] getStrokeColor() {
        return strokeColor;
    }

    // set "z-index" of this shape to the top of the stack
    public void bringToTop() {
        fillMesh.push();
        strokeMesh.push();
    }

    public void setPosition(float x, float y) {
        if (this.x == x && this.y == y)
            return;
        this.x = x;
        this.y = y;
        fillMesh.setPosition(x, y);
        strokeMesh.setPosition(x, y);
    }

    public void setDimensions(float width, float height) {
        if (this.width == width && this.height == height)
            return;
        this.width = width;
        this.height = height;
        fillMesh.setDimensions(width, height);
        strokeMesh.setDimensions(width, height);
        update();
    }

    public void layout(float x, float y, float width, float height) {
        setPosition(x, y);
        setDimensions(width, height);
    }

    public void hide() {
        fillMesh.hide();
        strokeMesh.hide();
    }

    public void show() {
        fillMesh.show();
        strokeMesh.show();
        update();
    }

    public boolean isVisible() {
        return (null != fillMesh && fillMesh.isVisible())
                || (null != strokeMesh && strokeMesh.isVisible());
    }
}
