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
    private String lastDirectoryPath;

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

    public void update(File oldFile, File newFile) {
        // recursively select file menu items based on current sample
        final BaseTrack currTrack = context.getTrackManager().getCurrTrack();
        if (!(currTrack instanceof Track))
            return;

        File currSampleFile = ((Track) currTrack).getCurrSampleFile();

        final String navigatePath;
        if (currSampleFile == null) {
            if (lastDirectoryPath == null) {
                return; // no history or current file to navigate to
            } else {
                navigatePath = lastDirectoryPath;
            }
        } else {
            navigatePath = currSampleFile.getAbsolutePath();
        }

        FileMenuItem match = null;
        while ((match = findMatchingChild(match, navigatePath, oldFile != null ? oldFile.getAbsolutePath() : null)) != null) {
            if (oldFile != null && match.getFile().equals(oldFile) && !match.getFile().exists()) {
                match.setFile(newFile);
            }
            if (!match.isChecked()) {
                match.trigger();
                match.scrollTo();
            }
        }
    }

    private FileMenuItem findMatchingChild(FileMenuItem parent, String path, String otherPath) {
        List<MenuItem> children = parent == null ? topLevelItems : parent.getSubMenuItems();
        for (MenuItem child : children) {
            FileMenuItem fileChild = (FileMenuItem) child;
            final String prefix = fileChild.getFile().getAbsolutePath();
            if (path.startsWith(prefix) || (otherPath != null && otherPath.startsWith(prefix))) {
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

    @Override
    public void onDirectoryMenuItemReleased(FileMenuItem directoryMenuItem) {
        lastDirectoryPath = directoryMenuItem.getFile().getAbsolutePath();

        final BaseTrack currTrack = context.getTrackManager().getCurrTrack();
        if (!(currTrack instanceof Track))
            return;

        File currSampleFile = ((Track) currTrack).getCurrSampleFile();
        if (null == currSampleFile)
            return;

        String currSamplePath = currSampleFile.getAbsolutePath();
        if (currSamplePath.startsWith(directoryMenuItem.getFile().getAbsolutePath())) {
            for (MenuItem child : directoryMenuItem.getSubMenuItems()) {
                FileMenuItem fileChild = (FileMenuItem) child;
                if (fileChild.getFile().getAbsolutePath().equals(currSamplePath)) {
                    fileChild.setChecked(true);
                    fileChild.scrollTo();
                }
            }
        }
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            return children != null && children.length > 0 && !isSystemDirectory(file);
        } else {
            return FileManager.isAudioFile(file);
        }
    }

    @Override
    public void onSelect(BaseTrack track) {
        update(null, null);
    }

    @Override
    public void onCreate(Track track) {
    }

    @Override
    public void onDestroy(Track track) {
    }

    @Override
    public void onSampleChange(Track track) {
        update(null, null);
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
