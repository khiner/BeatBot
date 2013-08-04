package com.kh.beatbot.ui.view;

public class TextView extends View {
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
	public void init() {
	}
	
	@Override
	public void draw() {
		drawText();
	}

	@Override
	protected void createChildren() {

	}

	@Override
	public void layoutChildren() {
		
	}

	@Override
	protected void loadIcons() {
		initText();
	}
	
	protected float calcTextOffset() {
		return width / 2 - textWidth / 2;
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
		GLSurfaceViewBase.storeText(text);
		textHeight = 5 * height / 8;
		textWidth = GLSurfaceViewBase.getTextWidth(text, textHeight);
		textXOffset = calcTextOffset();
		textXOffset += 2; // kludgey magic number
							// but it corrects for something weird in
							// GLSurfaceViewBase.getTextWidth
		textYOffset = 0;
	}
}
