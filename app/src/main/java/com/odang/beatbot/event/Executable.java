package com.odang.beatbot.event;

import com.odang.beatbot.ui.view.View;

public abstract class Executable implements Stateful {
    public void execute() {
        if (doExecute()) {
            View.context.getEventManager().eventCompleted(this);
        }
    }

    @Override
    public void apply() {
        doExecute();
    }

    public abstract boolean doExecute();
}
