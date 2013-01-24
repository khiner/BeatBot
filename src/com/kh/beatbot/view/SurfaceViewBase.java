package com.kh.beatbot.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.view.text.GLText;

public abstract class SurfaceViewBase extends SurfaceView implements
		SurfaceHolder.Callback, Runnable {
	public static final float ¹ = (float) Math.PI;

	private static int[] version = new int[2];
	private static int[] numConfig = new int[1];
	private static int[] configSpec = { EGL10.EGL_RED_SIZE, 4,
			EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4, EGL10.EGL_NONE };
	private static EGLConfig[] configs = new EGLConfig[1];
	private static Resources resources;
	private static EGL10 egl = (EGL10) EGLContext.getEGL();
	private static EGLDisplay dpy = egl
			.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
	private EGLContext context;
	private EGLSurface surface;

	protected EGLContext glContext;
	protected SurfaceHolder sHolder;
	protected Thread t;
	protected boolean initialized = false;
	protected boolean running;
	protected int width;
	protected int height;
	protected float[] backgroundColor = Colors.BG_COLOR;
	protected float[] clearColor = Colors.BG_COLOR;

	protected static GL10 gl = null;
	protected static GLText glText; // A GLText Instance

	private static FloatBuffer circleVb = null;
	private static final float CIRCLE_RADIUS = 100;
	
	static { // init circle
		float theta = 0;
		float coords[] = new float[128];
		for (int i = 0; i < 128; i += 2) {
			  coords[i] = FloatMath.cos(theta) * CIRCLE_RADIUS;
			  coords[i + 1] = FloatMath.sin(theta) * CIRCLE_RADIUS;
			  theta += 2 * Math.PI / 64;
			}
			circleVb = makeFloatBuffer(coords);		
	}
	
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
		return makeFloatBuffer(arr, 0);
	}
	
	public static FloatBuffer makeFloatBuffer(float[] arr, int position) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(position);
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
	
	public static void drawPoint(float pointSize, float[] color, float x, float y) {
		push();
		translate(x, y);
		float scale = pointSize / CIRCLE_RADIUS;
		scale(scale, scale);
		drawTriangleFan(circleVb, color);
		pop();
	}

	protected float distanceFromCenterSquared(float x, float y) {
		return (x - width / 2) * (x - width / 2) + (y - height / 2)
				* (y - height / 2);
	}
	
	protected void setBackgroundColor(float[] color) {
		backgroundColor = color;
	}

	protected static void setColor(float[] color) {
		gl.glColor4f(color[0], color[1], color[2], color[3]);
	}

	public static void loadTexture(Bitmap bitmap, int[] textureHandlers, int textureId) {
		// Generate Texture ID
		gl.glGenTextures(1, textureHandlers, textureId);
		// Bind texture id texturing target
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandlers[textureId]);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
		// allow non-power-of-2 images to render with hardware accelleration enabled
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		//bitmap.recycle();
	}
	
	public static void loadTexture(int resourceId, int[] textureHandlers, int textureId, int[] crop) {
		Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId);
		
		// Build our crop texture to be the size of the bitmap (ie full texture)
		crop[0] = 0;
		crop[1] = bitmap.getHeight();
		crop[2] = bitmap.getWidth();
		crop[3] = -bitmap.getHeight();
		
		loadTexture(bitmap, textureHandlers, textureId);
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

	public SurfaceViewBase(Context c) {
		super(c);
		sHolder = getHolder();
		sHolder.addCallback(this);
	}
	
	public SurfaceViewBase(Context c, AttributeSet as) {
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
		egl.eglInitialize(dpy, version);
		egl.eglChooseConfig(dpy, configSpec, configs, 1, numConfig);
		context = egl.eglCreateContext(dpy, configs[0], EGL10.EGL_NO_CONTEXT,
				null);

		surface = egl.eglCreateWindowSurface(dpy, configs[0], sHolder, null);

		egl.eglMakeCurrent(dpy, surface, surface, context);
		initGl(context);
		
		if (!initialized) {
			init();
			initialized = true;
		}

		running = true;
		initBackgroundColor();
		while (running) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			drawFrame();
			egl.eglSwapBuffers(dpy, surface);
		}
		egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
				EGL10.EGL_NO_CONTEXT);
		egl.eglDestroySurface(dpy, surface);
		egl.eglDestroyContext(dpy, context);
		egl.eglTerminate(dpy);
	}

	private void initGl(EGLContext context) {
		gl = (GL10) context.getGL();
		gl.glViewport(0, 0, width, height);
		GLU.gluOrtho2D(gl, 0, width, height, 0);
		initGlText();
		loadIcons();
		gl.glEnable(GL10.GL_POINT_SMOOTH);
		gl.glEnable(GL10.GL_BLEND);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public static GL10 getGl() {
		return gl;
	}
	
	private static void initGlText() {
		// load font file once, with static height
		// to change height, simply use gl.scale()
		glText = GLText.getInstance("REDRING-1969-v03.ttf", 50);
		// since the GL10 instance potentially has changed,
		// we need to reload the bitmap texture for the font
		glText.loadTexture();
	}

	protected void initBackgroundColor() {
		gl.glClearColor(backgroundColor[0], backgroundColor[1],
				backgroundColor[2], backgroundColor[3]);
	}

	protected abstract void init();

	protected abstract void loadIcons();

	protected abstract void drawFrame();
	
	protected class ViewRect {
		public int drawOffset;
		public float parentWidth, parentHeight, minX, maxX, minY, maxY, width, height, borderRadius;
		
		private FloatBuffer borderVb = null;
		
		// radiusScale determines the size of the radius of the rounded border.
		// radius will be the given percentage of the shortest side of the view rect.
		ViewRect(float parentWidth, float parentHeight, float radiusScale, int drawOffset) {
			this.parentWidth = parentWidth;
			this.parentHeight = parentHeight;
			this.drawOffset = drawOffset;
			borderRadius = Math.min(parentWidth, parentHeight) * radiusScale;
			minX = borderRadius;
			minY = borderRadius;
			maxX = parentWidth - borderRadius;
			maxY = parentHeight - borderRadius;
			width = parentWidth - 2 * minX;
			height = parentHeight - 2 * minY;
			borderVb = makeRoundedCornerRectBuffer(parentWidth - drawOffset * 2, parentHeight
					- drawOffset * 2, borderRadius, 25);
		}
		
		public float viewX(float x) {
			 return x * width + minX;
		}
		
		public float viewY(float y) {
			return (1 - y) * height + minY;
		}
		
		public float unitX(float viewX) {
			return (viewX - minX) / width;
		}
		
		public float unitY(float viewY) {
			// bottom == height in pixels == 0 in value
			// top == 0 in pixels == 1 in value
			return (parentHeight - viewY - minY) / height;
		}
		
		public float clipX(float x) {
			return x < minX ? minX : (x > maxX ? maxX : x);
		}
		
		public float clipY(float y) {
			return y < minY ? minY : (y > maxY ? maxY : y);
		}
		
		public void drawRoundedBg() {
			gl.glTranslatef(parentWidth / 2, parentHeight / 2, 0);
			drawTriangleFan(borderVb, Colors.VIEW_BG);
			gl.glTranslatef(-parentWidth / 2, -parentHeight / 2, 0);
		}
		
		public void drawRoundedBgOutline() {
			gl.glTranslatef(parentWidth / 2, parentHeight / 2, 0);
			drawLines(borderVb, Colors.VOLUME, drawOffset / 2, GL10.GL_LINE_LOOP);
			gl.glTranslatef(-parentWidth / 2, -parentHeight / 2, 0);
		}
	}
}