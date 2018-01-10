package com.odang.beatbot.listener;

import com.odang.beatbot.event.Stateful;

public interface StatefulEventListener {
    public void onEventCompleted(Stateful event);
}
