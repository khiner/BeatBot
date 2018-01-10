package com.odang.beatbot.listener;

import com.odang.beatbot.event.Stateful;

public interface StatefulEventListener {
    void onEventCompleted(Stateful event);
}
