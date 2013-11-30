package com.kh.beatbot.ui;

public class IconResource {
	public enum State {
		DEFAULT, PRESSED, SELECTED, DISABLED
	}
	
	public Drawable defaultDrawable, pressedDrawable, selectedDrawable,
			disabledDrawable;

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
		this(defaultResource == -1 ? null : new Image(defaultResource),
			 pressedResource == -1 ? null : new Image(pressedResource),
			 selectedResource == -1 ? null : new Image(selectedResource),
			 disabledResource == -1 ? null : new Image(disabledResource));
	}

	public IconResource(Drawable defaultDrawable, Drawable pressedDrawable,
			Drawable selectedDrawable, Drawable disabledDrawable) {
		this.defaultDrawable = defaultDrawable;
		this.pressedDrawable = pressedDrawable;
		this.selectedDrawable = selectedDrawable;
		this.disabledDrawable = disabledDrawable;
	}
	
	public Drawable whichIcon(State state) {
		switch (state) {
		case DEFAULT:
			return defaultDrawable;
		case PRESSED:
			return pressedDrawable;
		case SELECTED:
			return selectedDrawable;
		case DISABLED:
			return disabledDrawable;
		default:
			return defaultDrawable;
		}
	}
}
