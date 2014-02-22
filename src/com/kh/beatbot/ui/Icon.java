package com.kh.beatbot.ui;

import com.kh.beatbot.ui.IconResource.State;
import com.kh.beatbot.ui.color.ColorSet;

public class Icon {
	protected int currentResourceId;
	protected IconResource resource;
	protected IconResource.State state = State.DEFAULT;
	protected IconResource.State lockedState = null;
	protected ColorSet fillColorSet, strokeColorSet;

	protected float x, y, width, height;

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
		if (resource == null) {
			currentResourceId = -1;
		} else {
			setResourceId(resource.whichResource(this.state));
		}
	}

	public void lockState(State state) {
		lockedState = state;
		setState(lockedState);
	}

	public int getCurrResourceId() {
		return currentResourceId;
	}

	public float[] getCurrFillColor() {
		return getFillColor(state);
	}

	public float[] getCurrStrokeColor() {
		return getStrokeColor(state);
	}

	protected float[] getFillColor(State state) {
		return fillColorSet == null ? null : fillColorSet.getColor(state);
	}

	protected float[] getStrokeColor(State state) {
		return strokeColorSet == null ? null : strokeColorSet.getColor(state);
	}
	
	public void layout(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	private void setResourceId(int resourceId) {
		currentResourceId = resourceId != -1 ? resourceId
				: (state == State.PRESSED && resource.selectedResource != -1) ? resource.selectedResource
						: resource.defaultResource;
	}
}
