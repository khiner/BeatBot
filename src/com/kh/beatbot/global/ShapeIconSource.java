package com.kh.beatbot.global;

import com.kh.beatbot.view.mesh.RoundedRect;
import com.kh.beatbot.view.mesh.Shape;

public class ShapeIconSource extends IconSource {

	private Shape prevShape;
	
	public ShapeIconSource() {};
	
	public ShapeIconSource(Shape defaultIcon, Shape pressedIcon,
			Shape selectedIcon, Shape disabledIcon) {
		super(defaultIcon, pressedIcon, selectedIcon, disabledIcon);
	}

	@Override
	protected void setIcon(Drawable icon) {
		prevShape = (Shape) currentIcon;
		super.setIcon(icon);
		if (prevShape == null) {
			((RoundedRect) currentIcon).getGroup().addShape((Shape)currentIcon);
		} else {
			((RoundedRect) currentIcon).getGroup().replaceShape(prevShape, (Shape)currentIcon);
		}
	}
}
