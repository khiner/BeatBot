package com.odang.beatbot.listener;

import com.odang.beatbot.ui.view.TouchableView;

public interface TouchableViewListener {
    void onPress(TouchableView view);

    void onRelease(TouchableView view);
}
