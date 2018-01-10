package com.odang.beatbot.ui.view.control;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.midi.util.GeneralUtils;
import com.odang.beatbot.ui.color.Color;
import com.odang.beatbot.ui.shape.DialShape;
import com.odang.beatbot.ui.view.View;

public class Dial extends ControlView1dBase {
    private DialShape knobShape;

    public Dial(View view) {
        super(view);
    }

    @Override
    public void onParamChange(Param param) {
        knobShape.setLevel(param.viewLevel);
    }

    @Override
    protected float posToLevel(Pointer pos) {
        float unitX = pos.x / width - .5f;
        float unitY = pos.y / height - .5f;
        float theta = (float) Math.atan(unitY / unitX) + π / 2;
        // atan ranges from 0 to π, and produces symmetric results around the y axis.
        // we need 0 to 2π, so add π if right of x axis.
        if (unitX > 0)
            theta += π;
        // convert to level - remember, min theta is π/4, max is 7π/8
        float level = (4 * theta / π - 1) / 6;
        return GeneralUtils.clipToUnit(level);
    }

    @Override
    public void createChildren() {
        knobShape = new DialShape(renderGroup, Color.TRON_BLUE, null);
        addShapes(knobShape);
    }

    @Override
    public void layoutChildren() {
        knobShape.layout(absoluteX, absoluteY, width, height);
    }

    @Override
    public void press() {
        super.press();
        knobShape.setFillColor(selectColor);
    }

    @Override
    public void release() {
        super.release();
        knobShape.setFillColor(levelColor);
    }
}
