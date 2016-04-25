package com.kh.beatbot.ui.view;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

public abstract class GLSurfaceViewBase extends GLSurfaceView implements GLSurfaceView.Renderer {
	private static final int DESIRED_FPS = 45;
	private static final int FRAME_PERIOD_MILLIS = 1000 / DESIRED_FPS;
	private static final int MAX_FRAME_SKIPS = 5;

	private GL11 gl;
	private long beginTimeMillis = System.currentTimeMillis(), deltaTimeMillis = 0;
	private int framesSkipped = 0, sleepTimeMillis = 0;

	public GLSurfaceViewBase(Context context) {
		super(context);
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		//setPreserveEGLContextOnPause(true);
	}

	public void onSurfaceChanged(GL10 _gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		GLU.gluOrtho2D(gl, 0, width, height, 0);
	}

	public void onSurfaceCreated(GL10 _gl, EGLConfig config) {
		gl = (GL11) _gl;
		initGl(gl);
	}

	public GL11 getGl() {
		return gl;
	}

	public final void onDrawFrame(GL10 _gl) {
		beginTimeMillis = System.currentTimeMillis();
		framesSkipped = 0;
		tick();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
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

	protected void initGl(GL10 _gl) {
		gl.glEnable(GL10.GL_BLEND);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}

	protected abstract void draw();

	protected abstract void tick();
}