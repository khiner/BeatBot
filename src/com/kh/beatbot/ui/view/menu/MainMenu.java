package com.kh.beatbot.ui.view.menu;

import java.io.File;

import android.annotation.SuppressLint;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.listener.FileMenuItemListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.MidiFileManager;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.Color;
import com.kh.beatbot.ui.IconResourceSets;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.shape.SlideTab;
import com.kh.beatbot.ui.view.MidiView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.page.MainPage;

public class MainMenu extends Menu implements FileMenuItemListener {

	private class PhysicsState {
		private Menu menu;

		private final float SPRING_CONST = .1f, DAMP = .65f, STOP_THRESH = 0.001f;

		private float velocity = 0, downXOffset = 0, lastX = 0, goalX = 0;
		private boolean snap = true;

		public PhysicsState(Menu menu) {
			this.menu = menu;
		}

		public void update() {
			if (!snap)
				return;
			velocity += SPRING_CONST * (goalX - menu.x - menu.width);
			velocity *= DAMP;
			if (Math.abs(velocity) < STOP_THRESH) {
				snap = false;
			}
		}

		public void snap(float goalX) {
			snap(goalX, velocity);
		}

		public void snap(float goalX, float velocity) {
			this.goalX = velocity >= 0 ? goalX : 0;
			snap = true;
		}
	}

	private MenuItem fileItem, settingsItem, snapToGridItem, midiImportItem, midiExportItem;

	private SlideTab tab;
	private PhysicsState physicsState;

	private float menuOffset = MidiView.Y_OFFSET / 4;
	private boolean menuPressed = false;

	private Rectangle foregroundRect;
	private static float[] fillColor = Color.TRANSPARENT.clone();

	protected synchronized void createMenuItems() {
		physicsState = new PhysicsState(this);
		tab = new SlideTab(shapeGroup, Color.LABEL_SELECTED, null);
		setShape(tab);
		setIcon(IconResourceSets.SLIDE_MENU);

		fileItem = new MenuItem(this, null, new ToggleButton(shapeGroup, false));
		settingsItem = new MenuItem(this, null, new ToggleButton(shapeGroup, false));
		snapToGridItem = new MenuItem(this, settingsItem, new ToggleButton(shapeGroup, true));
		midiImportItem = new FileMenuItem(this, fileItem, new File(
				FileManager.midiDirectory.getPath()));
		midiExportItem = new MenuItem(this, fileItem, new Button(shapeGroup));

		topLevelItems.add(fileItem);
		topLevelItems.add(settingsItem);

		snapToGridItem.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				MidiManager.setSnapToGrid(((ToggleButton) button).isChecked());
			}
		});

		midiExportItem.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				midiExportItem.onRelease(button);
				BeatBotActivity.mainActivity
						.showDialog(BeatBotActivity.MIDI_FILE_NAME_EDIT_DIALOG_ID);
			}
		});

		fileItem.addSubMenuItems(midiImportItem, midiExportItem);
		settingsItem.addSubMenuItems(snapToGridItem);
	}

	protected float getWidthForLevel(int level) {
		return level == 0 ? columnWidth : 2 * columnWidth;
	}

	@Override
	public synchronized void createChildren() {
		foregroundRect = new Rectangle(shapeGroup, fillColor, null);

		super.createChildren();

		fileItem.setIcon(IconResourceSets.FILE);
		settingsItem.setIcon(IconResourceSets.SETTINGS);

		snapToGridItem.setResourceId(IconResourceSets.SNAP_TO_GRID);
		snapToGridItem.setChecked(MidiManager.isSnapToGrid());

		midiImportItem.setResourceId(IconResourceSets.MIDI_IMPORT);
		midiExportItem.setResourceId(IconResourceSets.MIDI_EXPORT);

		snapToGridItem.setText("Snap-to-grid");
		midiImportItem.setText("Import MIDI");
		midiExportItem.setText("Export MIDI");
	}

	// adjust width of this view to fit all children
	public synchronized void adjustWidth() {
		float maxX = 0;
		for (View child : children) {
			if (child.absoluteX + child.width > maxX) {
				maxX = child.absoluteX + child.width;
			}
		}
		width = maxX;
	}

	@Override
	public void onFileMenuItemReleased(FileMenuItem fileItem) {
		MidiFileManager.importMidi(fileItem.getText());
	}

	@Override
	public void onFileMenuItemLongPressed(FileMenuItem menuItem) {
	}

	@Override
	public void onMenuItemReleased(MenuItem menuItem) {
		super.onMenuItemReleased(menuItem);
		adjustWidth();
		expand();
	}

	@SuppressLint("DefaultLocale")
	@Override
	public boolean accept(File file) {
		return file.getName().toLowerCase().endsWith(".midi");
	}

	@Override
	public synchronized void layoutChildren() {
		super.layoutChildren();
		foregroundRect.layout(0, 0, mainPage.width, mainPage.height);
	}

	@Override
	public void draw() {
		if (physicsState.snap) {
			setPosition(x + physicsState.velocity, y);
		}
		physicsState.update();
	}

	@Override
	public synchronized void setPosition(float x, float y) {
		super.setPosition(x, y);
		layoutShape();
	}

	@Override
	protected synchronized void layoutShape() {
		if (null == mainPage)
			return;

		tab.layout(x - mainPage.width + width, menuOffset / 2, columnWidth,
				MainPage.controlButtonHeight + menuOffset * 2);
		textureMesh.layout(x + width + menuOffset * 2, menuOffset / 2, tab.height, tab.height);

		fillColor[3] = GeneralUtils.clipToUnit(textureMesh.x / width) * .75f;
		foregroundRect.setFillColor(fillColor);
	}

	public void expand() {
		physicsState.snap(width, 1);
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		if (!textureMesh.containsPoint(x + absoluteX, y)) {
			return;
		}
		menuPressed = true;
		physicsState.downXOffset = x - this.x + absoluteX;
		physicsState.snap = false;
		physicsState.lastX = this.x;
	}

	@Override
	public void handleActionMove(int pointerId, float x, float y) {
		super.handleActionMove(pointerId, x, y);
		if (!menuPressed)
			return;
		physicsState.velocity = this.x - physicsState.lastX;
		physicsState.lastX = this.x;
		setPosition(x + absoluteX - physicsState.downXOffset, menuOffset / 2);
	}

	@Override
	public void handleActionUp(int pointerId, float x, float y) {
		super.handleActionUp(pointerId, x, y);
		if (!menuPressed)
			return;
		physicsState.snap(width);
		menuPressed = false;
	}

	@Override
	public boolean containsPoint(float x, float y) {
		return super.containsPoint(x, y) || textureMesh.containsPoint(x, y);
	}
}
