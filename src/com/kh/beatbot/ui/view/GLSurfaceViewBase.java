package com.kh.beatbot.ui.view;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.view.SurfaceHolder;

public abstract class GLSurfaceViewBase extends GLSurfaceView implements
		GLSurfaceView.Renderer {

	public GLSurfaceViewBase(Context context) {
		super(context);
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}

	public static GL11 gl;

	protected int width, height;

	// private long frameCount = 0, averageFrameTime = 0;

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		super.surfaceChanged(holder, format, width, height);
		this.width = width;
		this.height = height;
	}

	public void onSurfaceChanged(GL10 _gl, int width, int height) {
		gl = (GL11) _gl;
		gl.glViewport(0, 0, this.width, this.height);
		GLU.gluOrtho2D(gl, 0, this.width, this.height, 0);
		initGl(gl);
	}

	public void onSurfaceCreated(GL10 _gl, EGLConfig config) {
	}

	public final void onDrawFrame(GL10 _gl) {
		/* uncomment for timing logs */
		//long startTime = System.nanoTime();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		draw();
		//long frameTime = System.nanoTime() - startTime;

		//if (frameCount++ < 200)
//			return;
		//averageFrameTime += (frameTime - averageFrameTime) / (frameCount - 200);
		//Log.i("Avg Frame time: ", String.valueOf(averageFrameTime) + ", " + frameCount);
	}

	protected void initGl(GL10 _gl) {
		gl.glEnable(GL10.GL_BLEND);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}

	public static final void loadTexture(Bitmap bitmap, int[] textureHandlers,
			int textureId) {
		// Generate Texture ID
		gl.glGenTextures(1, textureHandlers, textureId);
		// Bind texture id texturing target
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandlers[textureId]);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
		// allow non-power-of-2 images to render with hardware acceleration
		// enabled
//		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
//				GL10.GL_CLAMP_TO_EDGE);
//		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
//				GL10.GL_CLAMP_TO_EDGE);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
	}

	protected abstract void draw();
}