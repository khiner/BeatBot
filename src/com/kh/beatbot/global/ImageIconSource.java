package com.kh.beatbot.global;

public class ImageIconSource extends IconSource {

	public int listViewIconResource = -1;
	public int listTitleIconResource = -1;

	public ImageIconSource(int defaultIconResourceId, int selectedIconResourceId) {
		this(defaultIconResourceId, selectedIconResourceId, -1);
	}

	public ImageIconSource(int defaultIconResourceId,
			int selectedIconResourceId, int listViewIconResourceId) {
		this(defaultIconResourceId, selectedIconResourceId,
				listViewIconResourceId, -1);
	}

	public ImageIconSource(int defaultIconResourceId,
			int selectedIconResourceId, int listViewIconResourceId,
			int listTitleIconResourceId) {
		super(defaultIconResourceId == -1 ? null : new ImageIcon(
				defaultIconResourceId), selectedIconResourceId == -1 ? null
				: new ImageIcon(selectedIconResourceId));
		this.listViewIconResource = listViewIconResourceId;
		this.listTitleIconResource = listTitleIconResourceId;
	}

	public void setDisabledIcon(int disabledResourceId) {
		super.setDisabledIcon(new ImageIcon(disabledResourceId));
	}

	public void setPressedIcon(int pressedResourceId) {
		super.setPressedIcon(new ImageIcon(pressedResourceId));
	}
}
