package com.odang.beatbot.ui.view.control;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.ui.color.Color;
import com.odang.beatbot.ui.shape.Circle;
import com.odang.beatbot.ui.shape.IntersectingLines;
import com.odang.beatbot.ui.view.View;

public class Seekbar2d extends ControlView2dBase {
    private IntersectingLines intersectingLines;
    private Circle circle, selectedCircle;

    public Seekbar2d(View view) {
        super(view);
    }

    public void createChildren() {
        selectColor = Color.LABEL_SELECTED;
        initRoundedRect();
        intersectingLines = new IntersectingLines(renderGroup, Color.TRON_BLUE, null);
        circle = new Circle(renderGroup, Color.TRON_BLUE, null);
        selectedCircle = new Circle(renderGroup, selectColorTrans, null);
        addShapes(intersectingLines, circle, selectedCircle);
    }

    public void setDimensions(float width, float height) {
        withCornerRadius(height / 10);
        super.setDimensions(width, height);
    }

    public void layoutChildren() {
        intersectingLines.layout(absoluteX, absoluteY, width, height);

        circle.setDimensions(getCornerRadius(), getCornerRadius());
        selectedCircle.setDimensions(getCornerRadius() * 2, getCornerRadius() * 2);
        selectedCircle.hide();
    }

    public void onParamChange(Param param) {
        if (params[0] == null || params[1] == null)
            return;
        float viewX = viewX(params[0].viewLevel);
        float viewY = viewY(params[1].viewLevel);
        circle.setPosition(absoluteX + BG_OFFSET + viewX, absoluteY + BG_OFFSET + viewY);
        selectedCircle.setPosition(absoluteX + BG_OFFSET + viewX, absoluteY + BG_OFFSET + viewY);
        intersectingLines.setIntersect(viewX, viewY);
    }

    protected float xToLevel(float x) {
        return unitX(clipX(x));
    }

    protected float yToLevel(float y) {
        return unitY(clipY(y));
    }

    @Override
    public void handleActionDown(int id, Pointer pos) {
        super.handleActionDown(id, pos);
        params[0].setLevel(xToLevel(pos.x));
        params[1].setLevel(yToLevel(pos.y));
        intersectingLines.setFillColor(selectColor);
        circle.setFillColor(selectColor);
        selectedCircle.show();
    }

    @Override
    public void handleActionUp(int id, Pointer pos) {
        super.handleActionUp(id, pos);
        intersectingLines.setFillColor(levelColor);
        circle.setFillColor(levelColor);
        selectedCircle.hide();
    }
}
