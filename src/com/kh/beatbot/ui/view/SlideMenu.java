package com.kh.beatbot.ui.view;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.mesh.SlideTab;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.page.Page;

public class SlideMenu extends TouchableView {
	private SlideTab tab;
	private ShapeGroup shapeGroup;

	private Position downPosition = new Position(0, 0);

	private final float SPRING_CONST = .1f, DAMP = .65f, STOP_THRESH = 0.001f;

	private float menuWidth = 0, velocity = 0, lastX = 0, goalX = 0;
	private boolean snap = false;

	private float[] fadeColor = new float[] {0, 0, 0, 0};

	private ImageButton menuButton;

	public void init() {
		shouldClip = false;
		menuWidth = parent.width / 5;
	}

	public void createChildren() {
		shapeGroup = new ShapeGroup();
		tab = new SlideTab(shapeGroup, Colors.LABEL_SELECTED);
		menuButton = new ImageButton();

		addChild(menuButton);
	}

	public void loadIcons() {
		menuButton.setIcon(new Icon(IconResources.MENU));
	}

	public void draw() {
		if (snap) {
			float force = SPRING_CONST * (goalX - x);
			velocity += force;
			velocity *= DAMP;
			setPosition(x + velocity, y);
			if (Math.abs(velocity) < STOP_THRESH) {
				snap = false;
			}
		}
		shapeGroup.draw(this, 1);
	}

	public void layoutChildren() {
		menuButton.layout(this, height * .25f, 0, height, height);
		tab.layout(absoluteX - parent.width, absoluteY, width, height);
	}

	public void handleActionDown(int pointerId, float x, float y) {
		downPosition.set(x, y);
		snap = false;
		lastX = this.x;
	}

	public void handleActionMove(int pointerId, float x, float y) {
		setPosition(this.x + x - downPosition.x, this.y);
		velocity = this.x - lastX;
		lastX = this.x;
	}

	public void handleActionUp(int pointerId, float x, float y) {
		goalX = velocity >= 0 ? menuWidth : 0;
		snap = true;
	}
	
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		fadeColor[3] = GeneralUtils.clipToUnit(x / menuWidth) * .8f;
		Page.mainPage.setForegroundColor(fadeColor);
	}
}
