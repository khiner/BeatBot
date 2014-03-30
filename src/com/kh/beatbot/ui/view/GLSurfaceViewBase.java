package com.kh.beatbot.ui.view;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.view.SurfaceHolder;

public abstract class GLSurfaceViewBase extends GLSurfaceView implements GLSurfaceView.Renderer {

	public static GL11 gl;

	private static final long DESIRED_FPS = 30, TICK_MILLIS = (long) (1000.0f / DESIRED_FPS);

	// private final long BEGIN_FRAME = 200;
	// private long frameCount = 0, averageFrameTime = 0;

	private long startTimeMillis = System.currentTimeMillis(), endTimeMillis = 0, deltaTimeMillis = 0;

	public GLSurfaceViewBase(Context context) {
		super(context);
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		super.surfaceChanged(holder, format, width, height);
	}

	public void onSurfaceChanged(GL10 _gl, int width, int height) {
		gl = (GL11) _gl;
		gl.glViewport(0, 0, width, height);
		GLU.gluOrtho2D(gl, 0, width, height, 0);
		initGl(gl);
	}

	public void onSurfaceCreated(GL10 _gl, EGLConfig config) {
	}

	public final void onDrawFrame(GL10 _gl) {
		endTimeMillis = System.currentTimeMillis();
		/* uncomment for timing logs */
		// long startTime = System.nanoTime();
		deltaTimeMillis += endTimeMillis - startTimeMillis;

		while (deltaTimeMillis >= TICK_MILLIS) {
			tick();
			deltaTimeMillis -= TICK_MILLIS;
		}

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		draw();

		startTimeMillis = endTimeMillis;
		// long frameTime = System.nanoTime() - startTime;

		// if (frameCount++ < BEGIN_FRAME)
		// return;
		// averageFrameTime += (frameTime - averageFrameTime) / (frameCount - BEGIN_FRAME);
		// Log.i("Avg Frame time: ", String.valueOf(averageFrameTime) + ", " + frameCount);
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