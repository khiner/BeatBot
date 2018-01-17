package com.odang.beatbot.listener;

import com.odang.beatbot.effect.Effect;

public interface NoteLevelsChangeListener {
    void onNoteLevelsChange(int noteValue, Effect.LevelType type);
}
