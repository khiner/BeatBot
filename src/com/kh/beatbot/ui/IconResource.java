package com.kh.beatbot.ui;

public class IconResource {
	public enum State {
		DEFAULT, PRESSED, SELECTED, DISABLED
	}
	
	public int defaultResource, pressedResource, selectedResource,
			disabledResource;

	public IconResource(int defaultIcon) {
		this(defaultIcon, -1);
	}

	public IconResource(int defaultResource, int pressedResource) {
		this(defaultResource, pressedResource, -1);
	}

	public IconResource(int defaultResource, int pressedResource,
			int selectedResource) {
		this(defaultResource, pressedResource, selectedResource, -1);
	}

	public IconResource(int defaultResource, int pressedResource,
			int selectedResource, int disabledResource) {
		this.defaultResource = defaultResource;
		this.pressedResource = pressedResource;
		this.selectedResource = selectedResource;
		this.disabledResource = disabledResource;
	}

	public int whichResource(State state) {
		switch (state) {
		case DEFAULT:
			return defaultResource;
		case PRESSED:
			return pressedResource;
		case SELECTED:
			return selectedResource;
		case DISABLED:
			return disabledResource;
		default:
			return defaultResource;
		}
	}
}
