package com.kh.beatbot.view;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.global.ColorSet;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.global.RoundedRectIcon;
import com.kh.beatbot.global.RoundedRectIconSource;
import com.kh.beatbot.view.mesh.ShapeGroup;

public class TextButton extends ToggleButton {

	private ShapeGroup globalGroup;

	private String text = "";
	private float iconOffset = 0, textWidth = 0, textHeight = 0;
	private float textXOffset = 0, textYOffset = 0;
	private ColorSet bgColorSet, strokeColorSet;
	private ToggleButton iconButton;

	private int defaultIconResource, pressedIconResource, selectedIconResource;

	private RoundedRectIcon prevIcon;

	private boolean shouldDrawShape = false;
	
	public TextButton(ShapeGroup globalGroup, ColorSet bgColorSet,
			ColorSet strokeColorSet) {
		this(globalGroup, bgColorSet, strokeColorSet, -1, -1, -1);
	}

	public TextButton(ShapeGroup globalGroup, ColorSet bgColorSet,
			ColorSet strokeColorSet, int defaultIconResource,
			int pressedIconResource, int selectedIconResource) {
		super();
		if (globalGroup == null) {
			shouldDrawShape = true;
			this.globalGroup = new ShapeGroup();
		} else {
			this.globalGroup = globalGroup;
		}
		this.bgColorSet = bgColorSet;
		this.strokeColorSet = strokeColorSet;
		this.defaultIconResource = defaultIconResource;
		this.pressedIconResource = pressedIconResource;
		this.selectedIconResource = selectedIconResource;
		if (defaultIconResource != -1 || pressedIconResource != -1
				|| selectedIconResource != -1) {
			iconButton = new ToggleButton();
		} else {
			iconButton = null;
		}
	}

	@Override
	public void press() {
		prevIcon = (RoundedRectIcon) currentIcon;
		super.press();
		if (iconButton != null) {
			iconButton.press();
		}
		updateGlobalMeshGroup();
	}

	@Override
	public void release() {
		prevIcon = (RoundedRectIcon) currentIcon;
		super.release();
		if (iconButton != null) {
			iconButton.release();
		}
		updateGlobalMeshGroup();
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		if (iconButton != null) {
			iconButton.setChecked(checked);
		}
	}

	@Override
	protected void loadIcons() {
		setIconSource(new RoundedRectIconSource(absoluteX, absoluteY, width,
				height, bgColorSet, strokeColorSet));
		setText(text);

		if (iconButton != null) {
			iconButton.setIconSource(new ImageIconSource(defaultIconResource,
					pressedIconResource, selectedIconResource));
		}
		addToGlobalMeshGroup();
	}

	@Override
	public void init() {
		if (text.isEmpty()) {
			return;
		}
		GLSurfaceViewBase.storeText(text);
		textHeight = 5 * height / 8;
		textWidth = GLSurfaceViewBase.getTextWidth(text, textHeight);
		textXOffset = (iconButton != null ? iconOffset + iconButton.width
				+ (width - iconButton.width - iconOffset) / 2 : width / 2)
				- textWidth / 2;
		textXOffset += 2; // kludgey magic number correction,
							// but it corrects for something weird in
							// GLSurfaceViewBase.getTextWidth
		textYOffset = 0;
	}

	public void setText(String text) {
		this.text = text;
		if (initialized)
			init();
	}

	@Override
	public void draw() {
		super.draw();
		if (shouldDrawShape) {
			globalGroup.render((GL11)gl, 1);
		}
		if (iconButton != null) { // draw optional icon
			iconButton.draw();
		}
		if (text != null) { // draw optional text
			float[] textColor = pressed ? strokeColorSet.pressedColor
					: checked ? strokeColorSet.selectedColor
							: strokeColorSet.defaultColor;
			drawText(text, textColor, (int) textHeight, textXOffset,
					textYOffset);
		}
	}

	@Override
	public void layoutChildren() {
		if (iconButton != null) { // layout optional icon
			iconOffset = height / 8;
			iconButton.layout(this, iconOffset, iconOffset, 3 * height / 4,
					3 * height / 4);
		}
	}

	private void addToGlobalMeshGroup() {
		globalGroup.addMeshPair(
				((RoundedRectIcon) currentIcon).roundedRectMesh,
				((RoundedRectIcon) currentIcon).roundedRectOutlineMesh);
	}

	private void updateGlobalMeshGroup() {
		globalGroup.replaceMeshPair(prevIcon.roundedRectMesh,
				prevIcon.roundedRectOutlineMesh,
				((RoundedRectIcon) currentIcon).roundedRectMesh,
				((RoundedRectIcon) currentIcon).roundedRectOutlineMesh);
	}
}
