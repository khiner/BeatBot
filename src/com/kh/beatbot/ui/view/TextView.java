package com.kh.beatbot.ui.view;

public class TextView extends View {
	// kludgey magic number
	// but it corrects for something weird in
	// GLSurfaceViewBase.getTextWidth
	private final float X_OFFSET = 2;

	protected String text = "";
	protected float textWidth = 0, textHeight = 0, textXOffset = 0,
			textYOffset = 0;

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
	public synchronized void destroy() {
	}

	@Override
	public void draw() {
		drawText();
	}

	@Override
	protected synchronized void createChildren() {
	}

	@Override
	public synchronized void layoutChildren() {
		initText();
	}

	@Override
	protected synchronized void loadIcons() {
		initText();
	}

	protected float calcTextXOffset() {
		return (width - textWidth) / 2;
	}

	protected float calcTextYOffset() {
		return (height - textHeight) / 2;
	}

	private void drawText() {
		if (text == null || text.isEmpty())
			return;
		drawText(text, getStrokeColor(), (int) textHeight, textXOffset,
				textYOffset);
	}

	private void initText() {
		if (text == null || text.isEmpty()) {
			return;
		}

		textHeight = height;
		textWidth = GLSurfaceViewBase.getTextWidth(text, textHeight);
		if (textWidth > width - X_OFFSET * 3) {
			float scaleRatio = (width - X_OFFSET * 3) / textWidth;
			textWidth *= scaleRatio;
			textHeight *= scaleRatio;
		}
		textXOffset = calcTextXOffset() + X_OFFSET;
		textYOffset = calcTextYOffset();
	}
}
