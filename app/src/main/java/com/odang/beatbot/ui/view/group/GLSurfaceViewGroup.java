package com.odang.beatbot.ui.view.group;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.odang.beatbot.listener.GLSurfaceViewGroupListener;
import com.odang.beatbot.ui.view.TouchableSurfaceView;
import com.odang.beatbot.ui.view.TouchableView;

import java.util.HashSet;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceViewGroup extends TouchableSurfaceView {
    private TouchableView renderer;
    private Set<GLSurfaceViewGroupListener> listeners = new HashSet<GLSurfaceViewGroupListener>();

    public GLSurfaceViewGroup(Context context) {
        super(context);
    }

    public void addListener(GLSurfaceViewGroupListener listener) {
        listeners.add(listener);
    }

    public void setBBRenderer(TouchableView renderer) {
        this.renderer = renderer;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        renderer.layout(null, 0, 0, w, h);
    }

    @Override
    public void onSurfaceCreated(GL10 _gl, EGLConfig config) {
        super.onSurfaceCreated(_gl, config);
        for (GLSurfaceViewGroupListener listener : listeners) {
            listener.onGlReady(this);
        }
    }

    @Override
    protected void draw() {
        renderer.drawAll();
    }

    @Override
    protected void tick() {
        renderer.tickAll();
    }

    /*
     * Touch events are delegated to children, using coordinates relative to child.
     */
    protected void handleActionDown(MotionEvent e, int id, float x, float y) {
        renderer.propagateActionDown(e, id, x, y);
    }

    protected void handleActionUp(MotionEvent e, int id, float x, float y) {
        renderer.propagateActionUp(e, id, x, y);
    }

    protected void handleActionPointerDown(MotionEvent e, int id, float x, float y) {
        renderer.propagateActionPointerDown(e, id, x, y);
    }

    protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
        renderer.propagateActionPointerUp(e, id, x, y);
    }

    protected void handleActionMove(MotionEvent e, int id, float x, float y) {
        renderer.propagateActionMove(e, id, x, y);
    }
}