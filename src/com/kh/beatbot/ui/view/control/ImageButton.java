package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.IconResource.State;
import com.kh.beatbot.ui.ShapeIcon;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public class ImageButton extends Button {

	protected final int BACKGROUND_ICON_INDEX = 0, FOREGROUND_ICON_INDEX = 1;
	protected float iconXOffset = 0, iconYOffset = 0, iconW = 0, iconH = 0;
	// two icon sources - foreground and background
	protected Icon[] icons;

	public ImageButton(ShapeGroup shapeGroup) {
		super(shapeGroup);
		icons = new Icon[2];
	}

	public ImageButton() {
		super();
		icons = new Icon[2];
	}

	public Icon getIcon() {
		return icons[FOREGROUND_ICON_INDEX];
	}

	public Icon getBgIcon() {
		return icons[BACKGROUND_ICON_INDEX];
	}

	public void setIcon(Icon icon) {
		icons[FOREGROUND_ICON_INDEX] = icon;
		layoutIcons();
	}

	public void setIconResource(IconResource iconResource) {
		Icon icon = getIcon();
		if (null != icon) {
			icon.setResource(iconResource);
		}
		updateState();
	}

	public void setBgIcon(Icon bgIcon) {
		if (null == bgIcon) {
			destroyBgIcon();
		}
		icons[BACKGROUND_ICON_INDEX] = bgIcon;
		layoutIcons();
	}

	@Override
	public synchronized void destroy() {
		super.destroy();
		destroyBgIcon();
	}

	protected void destroyBgIcon() {
		if (getBgIcon() instanceof ShapeIcon) {
			((ShapeIcon) getBgIcon()).destroy();
		}
	}

	@Override
	public void press() {
		super.press();
		for (Icon icon : icons) {
			if (icon != null) {
				icon.setState(State.PRESSED);
			}
		}
		updateState();
	}

	@Override
	public void release() {
		super.release();
		for (Icon icon : icons) {
			if (icon != null) {
				icon.setState(State.DEFAULT);
			}
		}
		updateState();
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (Icon iconSource : icons) {
			if (iconSource != null) {
				iconSource.setState(enabled ? State.DEFAULT : State.DISABLED);
			}
		}
		updateState();
	}

	@Override
	public synchronized void layoutChildren() {
		super.layoutChildren();
		layoutIcons();
	}

	protected void layoutIcons() {
		Icon bgIconSource = getBgIcon();
		if (null != bgIconSource) {
			// if there is a bg shape, we shrink the icon a bit to avoid overlap
			iconXOffset = iconYOffset = height / 10;
			iconW = 4 * height / 5;
			iconH = 4 * height / 5;
			bgIconSource.layout(absoluteX, absoluteY, width, height);
		} else {
			iconXOffset = text.isEmpty() ? (width - height) / 2 : 0;
			iconYOffset = 0;
			iconW = height;
			iconH = height;
		}
		if (null != textureMesh) {
			textureMesh.layout(absoluteX, absoluteY, iconW, iconH);
		}
		initText();
	}

	@Override
	// text goes to the right of the icon
	protected float calcTextX() {
		float textXOffset = super.calcTextX();
		if (getIcon() != null) {
			textXOffset += iconXOffset + iconW;
		}
		return textXOffset;
	}

	@Override
	// less room for text when there's an icon
	protected float calcNonIconWidth() {
		float nonIconWidth = super.calcNonIconWidth();
		if (getIcon() != null) {
			nonIconWidth -= (iconXOffset + iconW);
		}
		return nonIconWidth;
	}

	@Override
	public float[] getStrokeColor() {
		Icon bgIconSource = getBgIcon();
		if (bgIconSource != null && bgIconSource instanceof ShapeIcon) {
			float[] color = ((ShapeIcon) bgIconSource).getCurrStrokeColor();
			if (color != null) {
				return color;
			}
		}
		return super.getStrokeColor();
	}

	private synchronized void updateState() {
		setStrokeColor(getStrokeColor());
		Icon icon = getIcon();
		if (null != icon) {
			setResource(icon.getCurrResourceId(), iconXOffset, iconYOffset,
					iconW, iconH, icon.getCurrStrokeColor());
		}
	}
}
