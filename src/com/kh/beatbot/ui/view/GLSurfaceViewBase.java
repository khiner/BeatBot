package com.kh.beatbot.ui.view;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.view.SurfaceHolder;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.ui.view.text.GLText;

public abstract class GLSurfaceViewBase extends GLSurfaceView implements
		GLSurfaceView.Renderer {

	public GLSurfaceViewBase(Context context) {
		super(context);
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}

	protected int width, height;

	protected static GL11 gl = null;
	protected static GLText glText;

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
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		draw();
	}

	protected void initGl(GL10 _gl) {
		gl.glEnable(GL10.GL_BLEND);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		// load font file once, with static height
		// to change height, simply use gl.scale()
		glText = GLText.getInstance("REDRING-1969-v03.ttf", 30);
		// since the GL10 instance potentially has changed,
		// we need to reload the bitmap texture for the font
		glText.loadTexture();
	}

	public static final void drawText(String text, float height, float x,
			float y) {
		if (glText != null) {
			glText.draw(text, height, x, y);
		}
	}

	public static final float getTextWidth(String text, float height) {
		return glText != null ? glText.getTextWidth(text, height) : 0;
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
		// allow non-power-of-2 images to render with hardware accelleration
		// enabled
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_CLAMP_TO_EDGE);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
	}

	public static final void loadTexture(int resourceId, int[] textureHandlers,
			int textureId, int[] crop) {
		Bitmap bitmap = BitmapFactory.decodeResource(
				BeatBotActivity.mainActivity.getResources(), resourceId);

		// Build our crop texture to be the size of the bitmap (ie full texture)
		crop[0] = 0;
		crop[1] = bitmap.getHeight();
		crop[2] = bitmap.getWidth();
		crop[3] = -bitmap.getHeight();
		loadTexture(bitmap, textureHandlers, textureId);
	}

	public static final void drawTexture(int textureId, int[] textureHandlers,
			int[] crop, float x, float y, float width, float height) {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandlers[textureId]);
		gl.glTexParameteriv(GL10.GL_TEXTURE_2D,
				GL11Ext.GL_TEXTURE_CROP_RECT_OES, crop, 0);
		gl.glColor4f(1, 1, 1, 1);
		((GL11Ext) gl).glDrawTexfOES(x, y, 0, width, height);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

	public final static GL10 getGL10() {
		return gl;
	}

	protected abstract void draw();
}