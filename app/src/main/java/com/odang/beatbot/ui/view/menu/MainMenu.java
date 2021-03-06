package com.odang.beatbot.ui.view.menu;

import com.odang.beatbot.activity.BeatBotActivity;
import com.odang.beatbot.listener.FileMenuItemListener;
import com.odang.beatbot.listener.OnReleaseListener;
import com.odang.beatbot.listener.SnapToGridListener;
import com.odang.beatbot.manager.MidiFileManager;
import com.odang.beatbot.manager.ProjectFileManager;
import com.odang.beatbot.midi.util.GeneralUtils;
import com.odang.beatbot.ui.color.Color;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.shape.Rectangle;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.shape.SlideTab;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.Button;
import com.odang.beatbot.ui.view.control.ToggleButton;
import com.odang.beatbot.ui.view.page.main.MainPage;

import java.io.File;

public class MainMenu extends Menu implements FileMenuItemListener {

    private MenuItem fileItem, settingsItem, snapToGridItem, newProjectItem, saveProjectItem, loadProjectItem,
            midiImportItem, midiExportItem;

    private SlideTab tab;
    private PhysicsState physicsState;

    private float menuOffset = 4;
    private boolean menuPressed = false;

    private Rectangle foregroundRect;
    private static float[] fillColor = Color.TRANSPARENT.clone();

    public MainMenu(View view, RenderGroup renderGroup) {
        super(view, renderGroup);
    }

    protected synchronized void createMenuItems() {
        fileItem = new MenuItem(this, null, true);
        settingsItem = new MenuItem(this, null, true);
        snapToGridItem = new SnapToGridMenuItem(this, settingsItem, true);
        ((ToggleButton) snapToGridItem.button).oscillating();


        newProjectItem = new MenuItem(this, fileItem, false);
        saveProjectItem = new MenuItem(this, fileItem, false);
        loadProjectItem = new FileMenuItem(this, fileItem, new File(context.getFileManager()
                .getProjectDirectory().getPath()));

        midiExportItem = new MenuItem(this, fileItem, false);
        midiImportItem = new FileMenuItem(this, fileItem, new File(context.getFileManager()
                .getMidiDirectory().getPath()));

        topLevelItems.add(fileItem);
        topLevelItems.add(settingsItem);

        snapToGridItem.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                context.getMidiManager().setSnapToGrid(((ToggleButton) button).isChecked());
            }
        });

        newProjectItem.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                newProjectItem.onRelease(button);
                context.showDialog(BeatBotActivity.NEW_PROJECT_DIALOG_ID);
            }
        });

        saveProjectItem.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                saveProjectItem.onRelease(button);
                context.showDialog(BeatBotActivity.PROJECT_FILE_NAME_EDIT_DIALOG_ID);
            }
        });

        midiExportItem.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                midiExportItem.onRelease(button);
                context.showDialog(BeatBotActivity.MIDI_FILE_NAME_EDIT_DIALOG_ID);
            }
        });

        fileItem.addSubMenuItems(newProjectItem, saveProjectItem, loadProjectItem, midiExportItem, midiImportItem);
        settingsItem.addSubMenuItems(snapToGridItem);
    }

    protected float getWidthForLevel(int level) {
        return level == 0 ? columnWidth : 2 * columnWidth;
    }

    @Override
    public synchronized void createChildren() {
        foregroundRect = new Rectangle(renderGroup, fillColor, null);
        addShapes(foregroundRect);

        physicsState = new PhysicsState(this);
        tab = new SlideTab(renderGroup, Color.LABEL_SELECTED, null);
        setShape(tab);
        setIcon(IconResourceSets.SLIDE_MENU);

        super.createChildren();

        fileItem.setIcon(IconResourceSets.FILE);
        settingsItem.setIcon(IconResourceSets.SETTINGS);

        snapToGridItem.setResourceId(IconResourceSets.SNAP_TO_GRID);
        snapToGridItem.setChecked(context.getMidiManager().isSnapToGrid()); // XXX use listener

        // TODO
        // saveProjectItem.setResourceId(IconResourceSets.SAVE_PROJECT);
        // loadProjectItem.setResourceId(IconResourceSets.LOAD_PROJECT);
        midiImportItem.setResourceId(IconResourceSets.MIDI_IMPORT);
        midiExportItem.setResourceId(IconResourceSets.MIDI_EXPORT);

        snapToGridItem.setText("Snap-to-grid");
        newProjectItem.setText("New project");
        saveProjectItem.setText("Save project");
        loadProjectItem.setText("Load project");
        midiImportItem.setText("Import MIDI");
        midiExportItem.setText("Export MIDI");
    }

    // adjust width of this view to fit all children
    public synchronized void adjustWidth() {
        width = 0;
        for (final View child : children) {
            if (child.absoluteX + child.width > width) {
                width = child.absoluteX + child.width;
            }
        }
    }

    @Override
    public void onFileMenuItemReleased(final FileMenuItem fileItem) {
        if (MidiFileManager.isMidiFileName(fileItem.getText())) {
            context.importMidi(fileItem.getText());
        } else {
            context.importProject(fileItem.getText());
        }
    }

    @Override
    public void onDirectoryMenuItemReleased(FileMenuItem directoryMenuItem) {
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

    @Override
    public boolean accept(File file) {
        return MidiFileManager.isMidiFileName(file.getName())
                || ProjectFileManager.isProjectFileName(file.getName());
    }

    @Override
    public synchronized void layoutChildren() {
        super.layoutChildren();
        final MainPage mainPage = context.getMainPage();
        foregroundRect.layout(0, 0, mainPage.width, mainPage.height);
    }

    @Override
    public synchronized void tick() {
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
        final MainPage mainPage = context.getMainPage();
        if (mainPage == null)
            return;

        tab.layout(x - mainPage.width + width, menuOffset / 2, columnWidth,
                mainPage.controlButtonGroup.height + menuOffset * 2);
        textureMesh.layout(x + width + menuOffset * 8, menuOffset / 2, tab.height, tab.height);

        fillColor[3] = GeneralUtils.clipToUnit(textureMesh.x / width) * .75f;
        foregroundRect.setFillColor(fillColor);
    }

    public void expand() {
        physicsState.snap(width, 1);
    }

    @Override
    public void handleActionDown(int id, Pointer pos) {
        super.handleActionDown(id, pos);
        if (!textureMesh.containsPoint(pos.x + absoluteX, pos.y)) {
            return;
        }
        menuPressed = true;
        physicsState.downXOffset = pos.x - this.x + absoluteX;
        physicsState.snap = false;
        physicsState.lastX = this.x;
    }

    @Override
    public void handleActionMove(int pointerId, Pointer pos) {
        super.handleActionMove(pointerId, pos);
        if (!menuPressed)
            return;
        physicsState.velocity = this.x - physicsState.lastX;
        physicsState.lastX = this.x;
        setPosition(pos.x + absoluteX - physicsState.downXOffset, menuOffset / 2);
    }

    @Override
    public void handleActionUp(int pointerId, Pointer pos) {
        super.handleActionUp(pointerId, pos);
        if (!menuPressed)
            return;
        physicsState.snap(width);
        menuPressed = false;
    }

    @Override
    public boolean containsPoint(float x, float y) {
        return super.containsPoint(x, y) || textureMesh.containsPoint(x, y);
    }

    private class PhysicsState {
        private Menu menu;

        private final float SPRING_CONST = .6f, DAMP = .57f, STOP_THRESH = 0.001f;

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

    private class SnapToGridMenuItem extends MenuItem implements SnapToGridListener {
        public SnapToGridMenuItem(Menu menu, MenuItem parent, boolean toggle) {
            super(menu, parent, toggle);
            context.getMidiManager().setSnapToGridListener(this);
        }

        @Override
        public void onSnapToGridChanged(boolean snapToGrid) {
            ((ToggleButton) button).setChecked(snapToGrid);
        }
    }
}
