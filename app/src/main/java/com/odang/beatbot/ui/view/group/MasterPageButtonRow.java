package com.odang.beatbot.ui.view.group;

import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.ToggleButton;

public class MasterPageButtonRow extends PageButtonRow {
    private static final int LEVELS_PAGE_ID = 0, EFFECTS_PAGE_ID = 1, TEMPO_PAGE_ID = 2;

    public MasterPageButtonRow(View view) {
        super(view);
    }

    public ToggleButton getLevelsButton() {
        return pageButtons[LEVELS_PAGE_ID];
    }

    public ToggleButton getEffectsButton() {
        return pageButtons[EFFECTS_PAGE_ID];
    }

    public ToggleButton getTempoButton() {
        return pageButtons[TEMPO_PAGE_ID];
    }

    @Override
    protected void createChildren() {
        super.createChildren();

        getLevelsButton().setResourceId(IconResourceSets.LEVELS);
        getEffectsButton().setText("FX");
        getTempoButton().setResourceId(IconResourceSets.CLOCK);
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();

        float x = addTrackButton.width;
        for (int i = 0; i < pageButtons.length; i++) {
            pageButtons[i].layout(this, x, 0, height, height);
            x += pageButtons[i].width;
        }
    }

    @Override
    protected int getNumPages() {
        return 3;
    }
}
