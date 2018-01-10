package com.odang.beatbot.event;

public interface Stateful {
    void undo();

    void apply();
}
