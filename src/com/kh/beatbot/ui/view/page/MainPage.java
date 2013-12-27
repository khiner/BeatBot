package com.kh.beatbot.ui.view.page;

import java.nio.FloatBuffer;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.Track;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.Shape;
import com.kh.beatbot.ui.mesh.SlideTab;
import com.kh.beatbot.ui.view.MidiTrackView;
import com.kh.beatbot.ui.view.MidiView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.group.ControlButtonGroup;
import com.kh.beatbot.ui.view.group.PageSelectGroup;
import com.kh.beatbot.ui.view.menu.MainMenu;

public class MainPage extends TouchableView {

	public MidiView midiView;
	public ControlButtonGroup controlButtonGroup;
	public MidiTrackView midiTrackView;
	public PageSelectGroup pageSelectGroup;
	public MainMenu slideMenu;

	private SlideTab tab;
	private MenuButton menuButton;
	private FloatBuffer foregroundRectBuffer;

	private float[] foregroundColor = Colors.TRANSPARANT.clone();

	private float trackControlWidth = 0, controlButtonHeight = 0,
			menuOffset = 0;

	public void notifyTrackCreated(Track track) {
		midiTrackView.notifyTrackCreated(track);
		midiView.notifyTrackCreated(track);
		pageSelectGroup.updateAll();
	}

	public void notifyTrackChanged(Track track) {
		midiView.notifyTrackChanged(track.getId());
		pageSelectGroup.updateAll();
	}

	public void notifyTrackUpdated(Track track) {
		midiView.notifyTrackChanged(track.getId());
		midiTrackView.notifyTrackUpdated(track);
		pageSelectGroup.updateAll();
	}

	public void notifyTrackDeleted(Track track) {
		midiTrackView.notifyTrackDeleted(track);
		midiView.notifyTrackDeleted(track);
	}

	public void notifyMenuExpanded() {
		menuButton.snap(1);
	}

	@Override
	public synchronized void init() {
		foregroundRectBuffer = makeRectFloatBuffer(0, 0, width, height);
	}

	@Override
	public void initAll() {
		super.initAll();
		BeatBotActivity.mainActivity.setupProject();
	}

	@Override
	public synchronized void initIcons() {
		menuButton.setIcon(new Icon(IconResources.MENU));
	}

	@Override
	protected synchronized void createChildren() {
		tab = (SlideTab) Shape.get(Shape.Type.SLIDE_TAB, null,
				Colors.LABEL_SELECTED, null);

		midiView = new MidiView();
		controlButtonGroup = new ControlButtonGroup();
		midiTrackView = new MidiTrackView();
		pageSelectGroup = new PageSelectGroup();
		slideMenu = new MainMenu();
		menuButton = new MenuButton();

		slideMenu.setClip(false);
		menuButton.setClip(false);

		addChildren(controlButtonGroup, midiTrackView, midiView,
				pageSelectGroup, slideMenu, menuButton);
	}

	@Override
	public synchronized void layoutChildren() {
		controlButtonHeight = height / 10;
		float midiHeight = 3 * (height - controlButtonHeight) / 5;
		float pageSelectGroupHeight = height - midiHeight - controlButtonHeight;
		MidiView.allTracksHeight = midiHeight - MidiView.Y_OFFSET;
		MidiView.trackHeight = MidiView.allTracksHeight
				/ TrackManager.getNumTracks();
		View.LABEL_HEIGHT = pageSelectGroupHeight / 5;

		trackControlWidth = MidiView.trackHeight * 2.5f;
		menuOffset = MidiView.Y_OFFSET / 4;

		midiTrackView.layout(this, 0, controlButtonHeight, trackControlWidth,
				midiHeight);
		midiView.layout(this, trackControlWidth, controlButtonHeight, width
				- trackControlWidth - 15, midiHeight);

		controlButtonGroup.layout(this, trackControlWidth, 0, width
				- trackControlWidth, controlButtonHeight);
		pageSelectGroup.layout(this, 0, controlButtonHeight + midiHeight,
				width, pageSelectGroupHeight);

		slideMenu
				.layout(this, 0, 0, trackControlWidth + menuOffset * 2, height);
		tab.layout(0, 0, slideMenu.width, controlButtonHeight + menuOffset * 2);
		menuButton.layout(this, 0, 0, tab.height * 1.75f, tab.height);

		setMenuPosition(0, 0);
	}

	@Override
	public void draw() {
		menuButton.updateState();
		if (menuButton.snap) {
			setMenuPosition(menuButton.x, y);
		}
	}

	@Override
	public synchronized void drawChildren() {
		for (View child : children) {
			if (!child.equals(slideMenu) && !child.equals(menuButton)) {
				drawChild(child);
			}
		}
		drawTriangleFan(foregroundRectBuffer, foregroundColor);
		tab.draw();
		drawChild(slideMenu);
		drawChild(menuButton);
	}

	private synchronized void setMenuPosition(float x, float y) {
		slideMenu.setPosition(Math.min(0, x - slideMenu.width), y + menuOffset
				/ 2);
		menuButton.setPosition(x + menuButton.velocity, y + menuOffset / 2);
		tab.layout(x - width, y + menuOffset / 2, tab.width, tab.height);

		foregroundColor[3] = GeneralUtils.clipToUnit(menuButton.x
				/ slideMenu.width) * .8f;
	}

	private class MenuButton extends ImageButton {
		private final float SPRING_CONST = .1f, DAMP = .65f,
				STOP_THRESH = 0.001f;

		private float velocity = 0, downX = 0, lastX = 0, goalX = 0;
		private boolean snap = true;

		public void updateState() {
			if (!snap)
				return;

			velocity += SPRING_CONST * (goalX - this.x);
			velocity *= DAMP;
			if (Math.abs(velocity) < STOP_THRESH) {
				snap = false;
			}
		}

		@Override
		public void handleActionDown(int id, float x, float y) {
			downX = x;
			snap = false;
			lastX = this.x;
		}

		@Override
		public void handleActionMove(int pointerId, float x, float y) {
			velocity = this.x - lastX;
			lastX = this.x;
			setMenuPosition(this.x + x - downX - velocity, 0);
		}

		@Override
		public void handleActionUp(int pointerId, float x, float y) {
			snap(velocity);
		}

		public void snap(float velocity) {
			goalX = velocity >= 0 ? slideMenu.width : 0;
			snap = true;
		}
	}
}
