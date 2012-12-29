package com.kh.beatbot.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.Managers;

public abstract class SurfaceViewBase extends SurfaceView implements
		SurfaceHolder.Callback, Runnable {
	public static final float ¹ = (float) Math.PI;

	protected EGLContext glContext;
	protected SurfaceHolder sHolder;
	protected Thread t;
	protected boolean running;
	protected int width;
	protected int height;
	protected float[] backgroundColor = Colors.BG_COLOR;
	protected float[] clearColor = Colors.BG_COLOR;

	private static Resources resources;
	protected static GL10 gl;

	public static void setResources(Resources resources) {
		SurfaceViewBase.resources = resources;
	}

	/**
	 * Make a direct NIO FloatBuffer from an array of floats
	 * 
	 * @param arr
	 *            The array
	 * @return The newly created FloatBuffer
	 */
	public static FloatBuffer makeFloatBuffer(float[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}

	public static void translate(float x, float y) {
		gl.glTranslatef(x, y, 0);
	}

	public static void scale(float x, float y) {
		gl.glScalef(x, y, 1);
	}

	public static void push() {
		gl.glPushMatrix();
	}

	public static void pop() {
		gl.glPopMatrix();
	}

	public static FloatBuffer makeRectFloatBuffer(float x1, float y1, float x2,
			float y2) {
		return makeFloatBuffer(new float[] { x1, y1, x2, y1, x1, y2, x2, y2 });
	}

	public static FloatBuffer makeRectOutlineFloatBuffer(float x1, float y1,
			float x2, float y2) {
		return makeFloatBuffer(new float[] { x1, y1, x1, y2, x2, y2, x2, y1 });
	}

	public static FloatBuffer makeRoundedCornerRectBuffer(float width,
			float height, float cornerRadius, int resolution) {
		float[] roundedRect = new float[resolution * 8];
		float theta = 0, addX, addY;
		for (int i = 0; i < roundedRect.length / 2; i++) {
			theta += 4 * ¹ / roundedRect.length;
			if (theta < ¹ / 2) { // lower right
				addX = width / 2 - cornerRadius;
				addY = height / 2 - cornerRadius;
			} else if (theta < ¹) { // lower left
				addX = -width / 2 + cornerRadius;
				addY = height / 2 - cornerRadius;
			} else if (theta < 3 * ¹ / 2) { // upper left
				addX = -width / 2 + cornerRadius;
				addY = -height / 2 + cornerRadius;
			} else { // upper right
				addX = width / 2 - cornerRadius;
				addY = -height / 2 + cornerRadius;
			}
			roundedRect[i * 2] = FloatMath.cos(theta) * cornerRadius + addX;
			roundedRect[i * 2 + 1] = FloatMath.sin(theta) * cornerRadius + addY;
		}
		return makeFloatBuffer(roundedRect);
	}

	public static void drawRectangle(float x1, float y1, float x2, float y2,
			float[] color) {
		drawTriangleStrip(makeRectFloatBuffer(x1, y1, x2, y2), color);
	}

	public static void drawRectangleOutline(float x1, float y1, float x2,
			float y2, float[] color, float width) {
		drawLines(makeRectOutlineFloatBuffer(x1, y1, x2, y2), color, width,
				GL10.GL_LINE_LOOP);
	}

	public static void drawTriangleStrip(FloatBuffer vb, float[] color,
			int numVertices) {
		drawTriangleStrip(vb, color, 0, numVertices);
	}

	public static void drawTriangleStrip(FloatBuffer vb, float[] color) {
		if (vb == null)
			return;
		drawTriangleStrip(vb, color, vb.capacity() / 2);
	}

	public static void drawTriangleStrip(FloatBuffer vb, float[] color,
			int beginVertex, int endVertex) {
		if (vb == null)
			return;
		setColor(color);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vb);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, beginVertex, endVertex
				- beginVertex);
	}

	public static void drawTriangleFan(FloatBuffer vb, float[] color) {
		setColor(color);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vb);
		gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, vb.capacity() / 2);
	}

	public static void drawLines(FloatBuffer vb, float[] color, float width,
			int type, int stride) {
		setColor(color);
		gl.glLineWidth(width);
		gl.glVertexPointer(2, GL10.GL_FLOAT, stride, vb);
		gl.glDrawArrays(type, 0, vb.capacity() / (2 + stride / 8));
	}

	public static void drawLines(FloatBuffer vb, float[] color, float width,
			int type) {
		drawLines(vb, color, width, type, 0);
	}

	public static void drawPoint(float pointSize, float[] color, int vertex) {
		setColor(color);
		gl.glPointSize(pointSize);
		gl.glDrawArrays(GL10.GL_POINTS, vertex, 1);
	}

	protected void setBackgroundColor(float[] color) {
		backgroundColor = color;
	}

	protected static void setColor(float[] color) {
		gl.glColor4f(color[0], color[1], color[2], color[3]);
	}

	public static void loadTexture(int resourceId, int textureId,
			int[] textureHandlers, int[] crop) {
		// Generate Texture ID
		gl.glGenTextures(1, textureHandlers, textureId);
		// Bind texture id texturing target
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandlers[textureId]);

		Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId);
		// Build our crop texture to be the size of the bitmap (ie full texture)
		crop[0] = 0;
		crop[1] = bitmap.getHeight();
		crop[2] = bitmap.getWidth();
		crop[3] = -bitmap.getHeight();

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_REPEAT);

		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
	}

	protected float distanceFromCenterSquared(float x, float y) {
		return (x - width / 2) * (x - width / 2) + (y - height / 2)
				* (y - height / 2);
	}

	public static void drawTexture(int textureId, int[] textureHandlers,
			int[] crop, float x, float y, float width, float height) {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandlers[textureId]);
		((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D,
				GL11Ext.GL_TEXTURE_CROP_RECT_OES, crop, 0);
		gl.glColor4f(1, 1, 1, 1);
		((GL11Ext) gl).glDrawTexfOES(x, y, 0, width, height);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

	/**
	 * Constructor
	 * 
	 * @param c
	 *            The View's context.
	 * @param as
	 *            The View's Attribute Set
	 */
	public SurfaceViewBase(Context c, AttributeSet as) {
		this(c, as, -1);
	}

	/**
	 * Constructor for animated views
	 * 
	 * @param c
	 *            The View's context
	 * @param fps
	 *            The frames per second for the animation.
	 */
	public SurfaceViewBase(Context c, AttributeSet as, int fps) {
		super(c, as);
		sHolder = getHolder();
		sHolder.addCallback(this);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		this.width = width;
		this.height = height;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		t = new Thread(this);
		t.start();
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		running = false;
		try {
			t.join();
		} catch (InterruptedException ex) {
		}
		t = null;
	}

	public void run() {
		// Much of this code is from GLSurfaceView in the Google API Demos.
		int[] version = new int[2];
		int[] num_config = new int[1];
		int[] configSpec = { EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4,
				EGL10.EGL_BLUE_SIZE, 4, EGL10.EGL_NONE };

		EGLConfig[] configs = new EGLConfig[1];
		EGL10 egl = (EGL10) EGLContext.getEGL();

		EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		egl.eglInitialize(dpy, version);
		egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
		EGLConfig config = configs[0];
		EGLContext context = egl.eglCreateContext(dpy, config,
				EGL10.EGL_NO_CONTEXT, null);

		EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, sHolder,
				null);
		egl.eglMakeCurrent(dpy, surface, surface, context);

		gl = (GL10) context.getGL();
		GlobalVars.initIcons();
		Managers.directoryManager.initIcons();
		gl.glViewport(0, 0, width, height);
		GLU.gluOrtho2D(gl, 0, width, height, 0);

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnable(GL10.GL_POINT_SMOOTH);
		init();

		running = true;
		while (running) {
			drawFrame(gl, width, height);
			egl.eglSwapBuffers(dpy, surface);

			if (egl.eglGetError() == EGL11.EGL_CONTEXT_LOST) {
				Context c = getContext();
				if (c instanceof Activity) {
					((Activity) c).finish();
				}
			}
		}
		egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
				EGL10.EGL_NO_CONTEXT);
		egl.eglDestroySurface(dpy, surface);
		egl.eglDestroyContext(dpy, context);
		egl.eglTerminate(dpy);
	}

	/**
	 * Create a texture and send it to the graphics system
	 * 
	 * @param gl
	 *            The GL object
	 * @param bmp
	 *            The bitmap of the texture
	 * @param reverseRGB
	 *            Should the RGB values be reversed? (necessary workaround for
	 *            loading .pngs...)
	 * @return The newly created identifier for the texture.
	 */
	protected static void loadTexture(Bitmap bitmap, int[] textures) {
		// generate one texture pointer
		gl.glGenTextures(1, textures, 0);
		// ...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		// create nearest filtered texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);

		// Use Android GLUtils to specify a two-dimensional texture image from
		// our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		// Clean up
		bitmap.recycle();
	}

	protected void fillBackground() {
		gl.glClearColor(backgroundColor[0], backgroundColor[1],
				backgroundColor[2], backgroundColor[3]);
	}

	protected void drawFrame(GL10 gl, int w, int h) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		fillBackground();
		drawFrame();
	}

	protected abstract void init();

	protected abstract void drawFrame();

	protected abstract void handleActionDown(int id, float x, float y);

	protected abstract void handleActionPointerDown(MotionEvent e, int id,
			float x, float y);

	protected abstract void handleActionMove(MotionEvent e);

	protected abstract void handleActionPointerUp(MotionEvent e, int id,
			float x, float y);

	protected abstract void handleActionUp(int id, float x, float y);

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_CANCEL:
			return false;
		case MotionEvent.ACTION_DOWN:
			handleActionDown(e.getPointerId(0), e.getX(0), e.getY(0));
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			int index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			handleActionPointerDown(e, e.getPointerId(index), e.getX(index),
					e.getY(index));
			break;
		case MotionEvent.ACTION_MOVE:
			index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			handleActionMove(e);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			handleActionPointerUp(e, e.getPointerId(index), e.getX(index),
					e.getY(index));
			break;
		case MotionEvent.ACTION_UP:
			handleActionUp(e.getPointerId(0), e.getX(0), e.getY(0));
			break;
		}
		return true;
	}
}