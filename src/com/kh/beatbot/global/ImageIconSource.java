package com.kh.beatbot.global;

public class ImageIconSource extends IconSource {
	public int defaultIconResource = -1;
	public int pressedIconResource = -1;
	public int selectedIconResource = -1;
	public int disabledIconResource = -1;

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

	public ImageIconSource(int defultIconResource, int pressedIconResource,
			int selectedIconResource, int disabledIconResource) {
		super(defultIconResource == -1 ? null : new Image(defultIconResource),
				pressedIconResource == -1 ? null : new Image(
						pressedIconResource), selectedIconResource == -1 ? null
						: new Image(selectedIconResource),
				disabledIconResource == -1 ? null : new Image(
						disabledIconResource));
		this.defaultIconResource = defultIconResource;
		this.pressedIconResource = pressedIconResource;
		this.selectedIconResource = selectedIconResource;
		this.disabledIconResource = disabledIconResource;
	}

	public ImageIconSource(int defaultIconResourceId,
			int pressedIconResourceId, int selectedIconResourceId,
			int disabledIconResourceId, int listViewIconResourceId,
			int listTitleIconResourceId) {
		this(defaultIconResourceId, pressedIconResourceId,
				selectedIconResourceId, disabledIconResourceId);
		this.listViewIconResource = listViewIconResourceId;
		this.listTitleIconResource = listTitleIconResourceId;
	}
}
