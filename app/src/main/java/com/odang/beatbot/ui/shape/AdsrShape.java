package com.odang.beatbot.ui.shape;

public class AdsrShape extends Shape {
    private final Circle[] circles = new Circle[4];
    private final Circle[] selectCircles = new Circle[4];

    public AdsrShape(RenderGroup group, float[] fillColor, float[] selectFillColor,
                     float[] strokeColor) {
        super(group, fillColor, strokeColor, 0, 10);
        for (int i = 0; i < circles.length; i++) {
            circles[i] = new Circle(group, fillColor, null);
        }
        for (int i = 0; i < circles.length; i++) {
            selectCircles[i] = new Circle(group, selectFillColor, null);
        }
        for (Circle selectCircle : selectCircles) {
            selectCircle.hide();
        }
    }

    @Override
    protected void updateVertices() {

    }

    public void update(float[] vertices) {
        resetIndices();
        for (int i = 0; i < vertices.length / 2; i++) {
            if (i != 3) {
                int circleIndex = i < 3 ? i : i - 1;
                circles[circleIndex].setPosition(vertices[i * 2] + x, vertices[i * 2 + 1] + y);
                selectCircles[circleIndex].setPosition(vertices[i * 2] + x, vertices[i * 2 + 1] + y);
            }
            if (i < (vertices.length - 1) / 2) {
                strokeVertex(vertices[i * 2] + x, vertices[i * 2 + 1] + y);
                strokeVertex(vertices[(i + 1) * 2] + x, vertices[(i + 1) * 2 + 1] + y);
            }
        }
    }

    public void select(int circleIndex) {
        selectCircles[circleIndex].show();
    }

    public void deselect(int circleIndex) {
        selectCircles[circleIndex].hide();
    }

    @Override
    public void layout(float x, float y, float width, float height) {
        super.layout(x, y, width, height);
        for (Circle circle : circles) {
            circle.setDimensions(width / 30, width / 30);
        }
        for (Circle circle : selectCircles) {
            circle.setDimensions(width / 15, width / 15);
        }
    }

    @Override
    public void show() {
        super.show();
        if (null != circles) {
            for (Circle circle : circles) {
                circle.show();
            }
        }
    }

    @Override
    public void hide() {
        super.hide();
        if (null != circles) {
            for (Circle circle : circles) {
                circle.hide();
            }
        }
        if (null != selectCircles) {
            for (Circle circle : selectCircles) {
                circle.hide();
            }
        }
    }
}
