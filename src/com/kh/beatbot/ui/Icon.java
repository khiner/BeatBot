package com.kh.beatbot.ui;

import com.kh.beatbot.ui.IconResource.State;

public class Icon extends Drawable {
	protected Drawable currentDrawable;
	protected IconResource resource;
	protected IconResource.State state = State.DEFAULT;
	protected IconResource.State lockedState = null;

	public Icon() {
	};

	public Icon(IconResource resource) {
		setResource(resource);
	}

	public void setResource(IconResource resource) {
		this.resource = resource;
		setState(state);
	}

	public void setState(IconResource.State state) {
		this.state = lockedState != null ? lockedState : state;
		setDrawable(resource.whichIcon(this.state));
	}

	public void draw() {
		draw(x, y, width, height);
	}

	public void draw(float x, float y, float width, float height) {
		if (currentDrawable != null) {
			currentDrawable.draw(x, y, width, height);
		}
	}

	protected void setDrawable(Drawable drawable) {
		currentDrawable = drawable != null ? drawable
				: (state == State.PRESSED && resource.selectedDrawable != null) ? resource.selectedDrawable
						: resource.defaultDrawable;
	}
	
	public void lockState(State state) {
		lockedState = state;
		setState(lockedState);
	}
}
