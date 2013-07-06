package com.kh.beatbot.ui;

public class IconResource {
	public enum State {
		DEFAULT, PRESSED, SELECTED, DISABLED
	}
	
	public Drawable defaultDrawable, pressedDrawable, selectedDrawable,
			disabledDrawable, listViewDrawable, listTitleDrawable;

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
		this(defaultResource, pressedResource, selectedResource,
				disabledResource, -1, -1);
	}

	public IconResource(int defaultResource, int pressedResource,
			int selectedResource, int disabledResource, int listViewResource,
			int listTitleResource) {
		this(defaultResource == -1 ? null : new Image(defaultResource),
			 pressedResource == -1 ? null : new Image(pressedResource),
			 selectedResource == -1 ? null : new Image(selectedResource),
			 disabledResource == -1 ? null : new Image(disabledResource),
			 listViewResource == -1 ? null : new Image(listViewResource),
			 listTitleResource == -1 ? null : new Image(listViewResource));
	}

	public IconResource(Drawable defaultDrawable, Drawable pressedDrawable,
			Drawable selectedDrawable, Drawable disabledDrawable, Drawable listViewDrawable,
			Drawable listTitleDrawable) {
		this.defaultDrawable = defaultDrawable;
		this.pressedDrawable = pressedDrawable;
		this.selectedDrawable = selectedDrawable;
		this.disabledDrawable = disabledDrawable;
		this.listViewDrawable = listViewDrawable;
		this.listTitleDrawable = listTitleDrawable;
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
