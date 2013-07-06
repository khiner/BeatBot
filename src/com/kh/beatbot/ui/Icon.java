package com.kh.beatbot.ui;

public class Icon extends Drawable {
	protected Drawable currentDrawable;
	protected IconResource resource;
	protected IconResource.State state;
	
	public Icon() {};

	public Icon(IconResource resource) {
		this.resource = resource;
		setState(IconResource.State.DISABLED);
	}

	public void setResource(IconResource resource) {
		this.resource = resource;
		setState(state);
	}
	
	public void setState(IconResource.State state) {
		this.state = state;
		setDrawable(resource.whichIcon(state));
	}

	public void draw() {
		if (currentDrawable != null) {
			currentDrawable.draw(x, y, width, height);
		}
	}

	public void draw(float x, float y, float width, float height) {
		if (currentDrawable != null) {
			currentDrawable.draw(x, y, width, height);
		}
	}

	protected void setDrawable(Drawable drawable) {
		currentDrawable = drawable != null ? drawable : resource.defaultDrawable;
	}
}
