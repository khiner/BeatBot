package com.kh.beatbot.global;

public class ImageIconSource extends IconSource {

	public int listViewIconResource = -1;
	public int listTitleIconResource = -1;

	public ImageIconSource(int defaultIconResourceId) {
		this(defaultIconResourceId, -1);
	}
	
	public ImageIconSource(int defaultIconResourceId, int pressedIconResourceId) {
		this(defaultIconResourceId, pressedIconResourceId, -1);
	}

	public ImageIconSource(int defaultIconResourceId,
			int pressedIconResourceId, int selectedIconResourceId) {
		this(defaultIconResourceId, pressedIconResourceId,
				selectedIconResourceId, -1);
	}

	public ImageIconSource(int defaultIconResourceId,
			int pressedIconResourceId, int selectedIconResourceId,
			int disabledIconResourceId) {
		super(defaultIconResourceId == -1 ? null : new Image(defaultIconResourceId),
				pressedIconResourceId == -1 ? null : new Image(pressedIconResourceId),
				selectedIconResourceId == -1 ? null : new Image(selectedIconResourceId),
				disabledIconResourceId == -1 ? null : new Image(disabledIconResourceId));
	}
	
	public ImageIconSource(int defaultIconResourceId,
			int pressedIconResourceId, int selectedIconResourceId,
			int disabledIconResourceId, int listViewIconResourceId,
			int listTitleIconResourceId) {
		this(defaultIconResourceId, pressedIconResourceId, selectedIconResourceId,
				disabledIconResourceId);
		this.listViewIconResource = listViewIconResourceId;
		this.listTitleIconResource = listTitleIconResourceId;
	}
	
}
