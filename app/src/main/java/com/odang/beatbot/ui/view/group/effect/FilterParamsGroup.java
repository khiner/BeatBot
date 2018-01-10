package com.odang.beatbot.ui.view.group.effect;

import com.odang.beatbot.effect.Filter;
import com.odang.beatbot.listener.OnReleaseListener;
import com.odang.beatbot.ui.icon.IconResourceSet;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.Button;
import com.odang.beatbot.ui.view.control.ToggleButton;

public class FilterParamsGroup extends EffectParamsGroup {
    private ToggleButton[] filterToggles;
    private FilterToggleListener filterToggleListener;

    private final IconResourceSet[] FILTER_ICONS = {
            IconResourceSets.LOWPASS_FILTER,
            IconResourceSets.BANDPASS_FILTER,
            IconResourceSets.HIGHPASS_FILTER,
    };

    public FilterParamsGroup(View view) {
        super(view);

        filterToggles = new ToggleButton[3];
        filterToggleListener = new FilterToggleListener();
        for (int i = 0; i < filterToggles.length; i++) {
            filterToggles[i] = new ToggleButton(this).withRoundedRect().withIcon(FILTER_ICONS[i]);
            filterToggles[i].setOnReleaseListener(filterToggleListener);
        }

        filterToggles[0].setChecked(true);
    }

    public void layoutChildren() {
        float toggleY = 5;
        float toggleW = width / 6;
        float paramH = (height - toggleY - toggleW) / 2 - 10;
        float paramW = 2 * paramH / 3;
        float x = width / 2 - 3 * toggleW / 2;
        for (Button toggle : filterToggles) {
            toggle.layout(this, x, toggleY, toggleW, toggleW);
            x += toggleW;
        }

        float paramY = toggleY + toggleW;
        paramControls[0].layout(this, width / 2 - paramW - 10, paramY, paramW, paramH);
        paramControls[1].layout(this, width / 2 + 10, paramY, paramW, paramH);
        paramControls[2].layout(this, width / 2 - paramW - 10, paramY + paramH, paramW, paramH);
        paramControls[3].layout(this, width / 2 + 10, paramY + paramH, paramW, paramH);
    }

    private class FilterToggleListener implements OnReleaseListener {
        @Override
        public void onRelease(Button button) {
            for (int i = 0; i < filterToggles.length; i++) {
                if (button.equals(filterToggles[i])) {
                    ((Filter) effect).setMode(i);
                } else {
                    filterToggles[i].setChecked(false);
                }
            }
        }
    }
}
