package com.odang.beatbot.ui.mesh;

import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeshGroup {
    private final static int SHORT_BYTES = Short.SIZE / 8, FLOAT_BYTES = Float.SIZE / 8;

    private final static int COLOR_OFFSET = 2, TEX_OFFSET = 6;
    private final static int COLOR_OFFSET_BYTES = COLOR_OFFSET * FLOAT_BYTES,
            TEX_OFFSET_BYTES = TEX_OFFSET * FLOAT_BYTES;

    private int strokeWeight = 1, indicesPerVertex, vertexBytes, primitiveType,
            vertexHandle = -1, indexHandle = -1;

    private short[] indices = new short[0];
    protected float[] vertices = new float[0];
    private int[] textureId;
    protected boolean dirty = false;

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;

    private List<Mesh> children = new ArrayList<Mesh>();

    public MeshGroup(int primitiveType) {
        this(primitiveType, 6);
    }

    protected MeshGroup(int primitiveType, int indicesPerVertex) {
        this(primitiveType, indicesPerVertex, null);
    }

    protected MeshGroup(int primitiveType, int indicesPerVertex, int[] textureId) {
        this.primitiveType = primitiveType;
        this.indicesPerVertex = indicesPerVertex;
        this.vertexBytes = this.indicesPerVertex * FLOAT_BYTES;
        this.textureId = textureId;
    }

    public void setStrokeWeight(int weight) {
        this.strokeWeight = weight;
    }

    public void draw() {
        if (children.isEmpty())
            return;

        if (dirty) {
            updateBuffers();
            dirty = false;
        }

        //final GL10 gl = View.context.get_Gl();
        GLES20.glLineWidth(strokeWeight);
        if (hasTexture()) {
            GLES20.glEnable(GLES20.GL_TEXTURE_2D);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexHandle);
        //gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        final int shaderProgram = 0;
        GLES20.glUseProgram(shaderProgram);

        final int vertexPositionIndex = GLES20.glGetAttribLocation(shaderProgram, "vertexPosition");
        GLES20.glEnableVertexAttribArray(vertexPositionIndex);
        GLES20.glVertexAttribPointer(vertexPositionIndex, 2, GLES20.GL_FLOAT, false, 0, 0);

        final int vertexColorIndex = GLES20.glGetAttribLocation(shaderProgram, "vertexColor");
        GLES20.glEnableVertexAttribArray(vertexColorIndex);
        GLES20.glVertexAttribPointer(vertexColorIndex, 4, GLES20.GL_FLOAT, false, 0, COLOR_OFFSET_BYTES);
        //((GL11) gl).glVertexPointer(2, GLES20.GL_FLOAT, vertexBytes, 0);
        //((GL11) gl).glColorPointer(4, GLES20.GL_FLOAT, vertexBytes, COLOR_OFFSET_BYTES);

        if (hasTexture()) {
            //((GL11) gl).glTexCoordPointer(2, GLES20.GL_FLOAT, vertexBytes, TEX_OFFSET_BYTES);
            final int texCoordIndex = GLES20.glGetAttribLocation(shaderProgram, "vertexTexCoord");
            GLES20.glEnableVertexAttribArray(texCoordIndex);
            GLES20.glVertexAttribPointer(texCoordIndex, 2, GLES20.GL_FLOAT, false, 0, TEX_OFFSET_BYTES);
        }

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexHandle);
        GLES20.glDrawElements(primitiveType, indexBuffer.limit(), GLES20.GL_UNSIGNED_SHORT, 0);

        //gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        if (hasTexture()) {
            GLES20.glDisable(GLES20.GL_TEXTURE_2D);
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public boolean contains(Mesh mesh) {
        return children.contains(mesh);
    }

    public void add(Mesh mesh) {
        if (null == mesh || contains(mesh)) {
            return;
        }

        adjustVertexLength(mesh.getNumVertices(), mesh.getNumIndices());

        children.add(mesh);
        resetIndices();
        resetIndices(mesh); // TODO only needed when numChildren == 1 ?
    }

    public void remove(Mesh mesh) {
        if (null == mesh || !contains(mesh)) {
            return;
        }

        int src = getVertexIndex(mesh, mesh.getNumVertices());
        int dst = getVertexIndex(mesh, 0);

        System.arraycopy(vertices, src, vertices, dst, vertices.length - src);

        children.remove(mesh);

        adjustVertexLength(-mesh.getNumVertices(), -mesh.getNumIndices());

        resetIndices();
    }

    public void push(Mesh2D mesh) {
        if (null == mesh || !children.contains(mesh))
            return;

        int src = getVertexIndex(mesh, 0);
        int dst = getVertexIndex(mesh, mesh.getNumVertices());

        // move vertices to the end of the array
        float[] tmp = Arrays.copyOfRange(vertices, src, dst);
        System.arraycopy(vertices, dst, vertices, src, vertexBuffer.limit() - dst);
        System.arraycopy(tmp, 0, vertices, vertexBuffer.limit() - mesh.numVertices
                * indicesPerVertex, mesh.numVertices * indicesPerVertex);

        children.remove(mesh);
        children.add(mesh);

        resetIndices();
    }

    public float getVertexX(int index) {
        return vertices[index * indicesPerVertex];
    }

    public float getVertexY(int index) {
        return vertices[index * indicesPerVertex + 1];
    }

    protected void vertex(Mesh mesh, int index, float x, float y) {
        vertex(mesh, index, x, y, null);
    }

    protected void vertex(Mesh mesh, int index, float x, float y, float[] color) {
        int offset = getVertexIndex(mesh, index);

        vertices[offset] = x;
        vertices[offset + 1] = y;
        if (null != color) {
            vertices[offset + 2] = color[0];
            vertices[offset + 3] = color[1];
            vertices[offset + 4] = color[2];
            vertices[offset + 5] = color[3];
        }
        dirty = true;
    }

    protected void setColor(Mesh mesh, float[] color) {
        for (int vertexIndex = 0; vertexIndex < mesh.getNumVertices(); vertexIndex++) {
            setColor(mesh, vertexIndex, color);
        }
        dirty = true;
    }

    protected void setColor(Mesh mesh, int index, float[] color) {
        int offset = getVertexIndex(mesh, index) + COLOR_OFFSET;
        vertices[offset] = color[0];
        vertices[offset + 1] = color[1];
        vertices[offset + 2] = color[2];
        vertices[offset + 3] = color[3];

        dirty = true;
    }

    public void translate(float x, float y) {
        for (int i = 0; i < vertices.length; i += indicesPerVertex) {
            vertices[i] += x;
            vertices[i + 1] += y;
        }
        dirty = true;
    }

    protected void translate(Mesh mesh, float x, float y) {
        for (int i = getVertexIndex(mesh, 0); i < getVertexIndex(mesh, mesh.getNumVertices()); i += indicesPerVertex) {
            vertices[i] += x;
            vertices[i + 1] += y;
        }
        dirty = true;
    }

    protected void changeSize(Mesh mesh, int oldSize, int newSize, int oldNumIndices,
                              int newNumIndices) {
        if (oldSize == newSize)
            return;

        int src = getVertexIndex(mesh, oldSize);
        int dst = getVertexIndex(mesh, newSize);

        if (newSize > oldSize) {
            adjustVertexLength(newSize - oldSize, newNumIndices - oldNumIndices);
            System.arraycopy(vertices, src, vertices, dst, vertices.length - dst);
        } else {
            System.arraycopy(vertices, src, vertices, dst, vertices.length - src);
            adjustVertexLength(-(oldSize - newSize), -(oldNumIndices - newNumIndices));
        }

        resetIndices(mesh);
        resetIndices();
    }

    protected int getVertexIndex(Mesh mesh, int index) {
        return (mesh.getGroupVertexOffset() + index) * indicesPerVertex;
    }

    private void resetIndices() {
        int numVertices = 0;
        int numIndices = 0;
        for (Mesh child : children) {
            child.setGroupVertexOffset(numVertices);
            if (child.getGroupIndexOffset() != numIndices) {
                child.setGroupIndexOffset(numIndices);
                resetIndices(child);
            }
            numVertices += child.getNumVertices();
            numIndices += child.getNumIndices();
        }
    }

    private void resetIndices(Mesh mesh) {
        for (int i = 0; i < mesh.getNumIndices(); i++) {
            indices[mesh.getGroupIndexOffset() + i] = (short) (mesh.getIndex(i) + mesh
                    .getGroupVertexOffset());
        }
        dirty = true;
    }

    private void updateBuffers() {
        initHandles();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexHandle);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.limit() * FLOAT_BYTES, vertexBuffer,
                GLES20.GL_DYNAMIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexHandle);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.limit() * SHORT_BYTES,
                indexBuffer, GLES20.GL_DYNAMIC_DRAW);
    }

    private void initHandles() {
        if (vertexHandle != -1) {
            return; // already initialized
        }
        final int[] buffer = new int[1];

        GLES20.glGenBuffers(1, buffer, 0);
        vertexHandle = buffer[0];

        GLES20.glGenBuffers(1, buffer, 0);
        indexHandle = buffer[0];
    }

    private boolean hasTexture() {
        return null != textureId;
    }

    private void adjustVertexLength(int vertexDiff, int indexDiff) {
        vertices = Arrays.copyOf(vertices, vertices.length + vertexDiff * indicesPerVertex);
        indices = Arrays.copyOf(indices, indices.length + indexDiff);

        vertexBuffer = FloatBuffer.wrap(vertices);
        indexBuffer = ShortBuffer.wrap(indices);
    }
}
