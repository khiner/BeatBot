package com.kh.beatbot.ui.view;

import com.kh.beatbot.ui.mesh.GLText;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public class TextView extends View {
	// kludgey magic number
	// but it corrects for something weird in
	// GLSurfaceViewBase.getTextWidth
	private final float X_OFFSET = 2;

	protected String text = "";
	protected float textWidth = 0, textHeight = 0;

	public TextView() {
		super();
	}

	public TextView(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		initText();
	}

	@Override
	public synchronized void init() {
	}

	@Override
	public synchronized void update() {
	}

	@Override
	public void draw() {
	}

	@Override
	protected synchronized void createChildren() {
	}

	@Override
	public synchronized void layoutChildren() {
		initText();
	}

	@Override
	protected synchronized void initIcons() {
		initText();
	}

	protected float calcTextX() {
		return 3 * X_OFFSET + (calcNonIconWidth() - textWidth) / 2;
	}

	protected float calcTextY() {
		return (height - textHeight) / 2;
	}

	protected float calcNonIconWidth() {
		return width - X_OFFSET * 4;
	}

	protected void initText() {
		if (text == null || text.isEmpty())
			return;

		textHeight = height;
		textWidth = GLText.getTextWidth(text, textHeight);
		if (textWidth > calcNonIconWidth()) {
			float scaleRatio = calcNonIconWidth() / textWidth;
			textWidth *= scaleRatio;
			textHeight *= scaleRatio;
		}

		setText(text, getStrokeColor(), calcTextX(), calcTextY(), textHeight);
	}
}
