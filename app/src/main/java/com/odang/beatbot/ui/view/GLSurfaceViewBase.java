package com.odang.beatbot.ui.view;

import android.content.Context;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class GLSurfaceViewBase extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static final int DESIRED_FPS = 45;
    private static final int FRAME_PERIOD_MILLIS = 1000 / DESIRED_FPS;
    private static final int MAX_FRAME_SKIPS = 5;

    private long beginTimeMillis = System.currentTimeMillis(), deltaTimeMillis = 0;
    private int framesSkipped = 0, sleepTimeMillis = 0;

    public GLSurfaceViewBase(Context context) {
        super(context);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        //setPreserveEGLContextOnPause(true);
    }

    public void onSurfaceChanged(GL10 _gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        GLU.gluOrtho2D(_gl, 0, width, height, 0);
    }

    public void onSurfaceCreated(GL10 _gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        GLES10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public final void onDrawFrame(GL10 _gl) {
        beginTimeMillis = System.currentTimeMillis();
        framesSkipped = 0;
        tick();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        draw();
        deltaTimeMillis = System.currentTimeMillis() - beginTimeMillis;
        sleepTimeMillis = (int) (FRAME_PERIOD_MILLIS - deltaTimeMillis);

        if (sleepTimeMillis > 0) {
            try {
                Thread.sleep(sleepTimeMillis);
            } catch (InterruptedException e) {
                Log.e("", e.getMessage());
            }
        }

        while (sleepTimeMillis < 0 && framesSkipped < MAX_FRAME_SKIPS) {
            // we need to catch up. update without rendering
            tick();
            sleepTimeMillis += FRAME_PERIOD_MILLIS;
            framesSkipped++;
        }
    }

    protected abstract void draw();

    protected abstract void tick();
}