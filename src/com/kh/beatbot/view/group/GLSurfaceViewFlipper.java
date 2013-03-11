package com.kh.beatbot.view.group;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.kh.beatbot.view.BBView;

public abstract class GLSurfaceViewFlipper extends GLSurfaceViewGroup {

	protected List<BBView> pages = new ArrayList<BBView>();
	
	public GLSurfaceViewFlipper(Context context, AttributeSet attr) {
		super(context, attr);
		createPages();
	}

	protected abstract void createPages();
	
	public void addPage(BBView page) {
		pages.add(page);
	}
	
	public void setPage(int num) {
		setBBRenderer(pages.get(num));
		requestRender();
	}

	@Override
	protected void init() {
		for (BBView page : pages) {
			page.initAll();
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		super.surfaceChanged(holder, format, w, h);
		for (BBView page : pages)
			page.layout(null, 0, 0, w, h);
	}
	
	public void initGl(GL10 gl) {
		super.initGl(gl);
		for (BBView page : pages)
			page.initGl(gl);
	}
}
