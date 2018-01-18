package com.odang.beatbot.ui.view.page.track;

import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.event.SampleSetEvent;
import com.odang.beatbot.listener.TrackListener;
import com.odang.beatbot.manager.FileManager;
import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.menu.FileMenuItem;
import com.odang.beatbot.ui.view.menu.Menu;
import com.odang.beatbot.ui.view.menu.MenuItem;

import java.io.File;
import java.util.List;

public class BrowsePage extends Menu implements TrackListener {
    public BrowsePage(View view, RenderGroup renderGroup) {
        super(view, renderGroup);
        setScrollable(true, false);
    }

    protected void createMenuItems() {
        setIcon(IconResourceSets.BROWSE_PAGE);
        initRoundedRect();

        File[] topLevelDirs = new File[]{context.getFileManager().getDrumsDirectory(),
                context.getFileManager().getRecordDirectory(),
                context.getFileManager().getRootDirectory()};

        for (File topLevelDir : topLevelDirs) {
            FileMenuItem fileMenuItem = new FileMenuItem(this, null, topLevelDir);
            fileMenuItem.setIcon(IconResourceSets.FILE_MENU_ITEM);
            topLevelItems.add(fileMenuItem);
        }
    }

    public void update() {
        // recursively select file menu items based on current sample
        final BaseTrack currTrack = context.getTrackManager().getCurrTrack();
        if (!(currTrack instanceof Track))
            return;

        File currSampleFile = ((Track) currTrack).getCurrSampleFile();
        if (null == currSampleFile)
            return;
        String currSamplePath = currSampleFile.getAbsolutePath();

        FileMenuItem match = null;
        while ((match = findMatchingChild(match, currSamplePath)) != null) {
            if (!match.isChecked()) {
                match.trigger();
                match.scrollTo();
            }
        }
    }

    private FileMenuItem findMatchingChild(FileMenuItem parent, String path) {
        List<MenuItem> children = parent == null ? topLevelItems : parent.getSubMenuItems();
        for (MenuItem child : children) {
            FileMenuItem fileChild = (FileMenuItem) child;
            if (path.startsWith(fileChild.getFile().getAbsolutePath())) {
                return fileChild;
            }
        }
        return null;
    }

    // exclude known system directories
    private boolean isSystemDirectory(File file) {
        for (String systemDirectoryName : FileManager.SYSTEM_DIRECTORY_NAMES) {
            if (file.getName().toLowerCase().equals(systemDirectoryName)) {
                return true;
            }
        }
        return false;
    }

    protected float getWidthForLevel(int level) {
        return width / 4;
    }

    @Override
    public void onFileMenuItemReleased(FileMenuItem fileItem) {
        new SampleSetEvent(context.getTrackManager().getCurrTrack().getId(), fileItem.getFile())
                .execute();
    }

    @Override
    public void onFileMenuItemLongPressed(FileMenuItem fileItem) {
        context.editFileName(fileItem.getFile());
    }

    private boolean isAudioFile(File file) {
        for (String extension : FileManager.SUPPORTED_EXTENSIONS) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            return children != null && children.length > 0 && !isSystemDirectory(file);
        } else {
            return isAudioFile(file);
        }
    }

    @Override
    public void onSelect(BaseTrack track) {
        update();
    }

    @Override
    public void onCreate(Track track) {
    }

    @Override
    public void onDestroy(Track track) {
    }

    @Override
    public void onSampleChange(Track track) {
        update();
    }

    @Override
    public void onMuteChange(Track track, boolean mute) {
    }

    @Override
    public void onSoloChange(Track track, boolean solo) {
    }

    @Override
    public void onReverseChange(Track track, boolean reverse) {
    }

    @Override
    public void onLoopChange(Track track, boolean loop) {
    }

    @Override
    public void onEffectCreate(BaseTrack track, Effect effect) {
    }

    @Override
    public void onEffectDestroy(BaseTrack track, Effect effect) {
    }

    @Override
    public void onEffectOrderChange(BaseTrack track, int initialEffectPosition,
                                    int endEffectPosition) {
    }
}
