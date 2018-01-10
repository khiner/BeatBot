package com.odang.beatbot.ui.view;

import com.odang.beatbot.effect.Effect.LevelType;
import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.midi.TouchedNotes;
import com.odang.beatbot.midi.util.GeneralUtils;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.color.Color;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.shape.Circle;
import com.odang.beatbot.ui.shape.Rectangle;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.view.control.ValueLabel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteLevelsView extends TouchableView {
    private class DragLine {
        private float m = 0, b = 0, leftTick = 0, rightTick = Float.MAX_VALUE, leftLevel = 0,
                rightLevel = 0;

        public float getLevel(float tick) {
            if (tick <= leftTick)
                return leftLevel;
            if (tick >= rightTick)
                return rightLevel;

            return m * tick + b;
        }
    }

    public static final int LEVEL_POINT_SIZE = 16, LEVEL_LINE_WIDTH = 7,
            LEVEL_BAR_WIDTH = LEVEL_POINT_SIZE / 2;

    // map of pointerIds to the notes they are selecting
    private TouchedNotes touchedNotes = new TouchedNotes();

    // map Midi Note to the offset of their level relative to the touched level(s)
    private Map<MidiNote, Float> levelOffsets = new HashMap<MidiNote, Float>();

    private LevelType currLevelType = LevelType.VOLUME;

    private float selectRegionStartX = -1, selectRegionStartY = -1;

    private RenderGroup levelBarGroup;
    private Rectangle selectRegionRect, levelBarRect;
    private Circle levelBarCircle, levelBarSelectCircle;
    private ValueLabel valueLabel;
    private DragLine dragLine;

    public NoteLevelsView(View view) {
        super(view);
        setClip(true);
        dragLine = new DragLine();
    }

    public LevelType getLevelType() {
        return currLevelType;
    }

    public void setLevelType(LevelType levelType) {
        currLevelType = levelType;
    }

    public void clearTouchedLevels() {
        touchedNotes.clear();
    }

    public MidiNote getTouchedLevel(int id) {
        return touchedNotes.get(id);
    }

    protected void drawLevel(MidiNote midiNote, float[] levelColor, float[] levelColorTrans) {
        float y = levelToY(midiNote.getLinearLevel(currLevelType));
        levelBarRect.layout(-LEVEL_BAR_WIDTH / 2, y, LEVEL_BAR_WIDTH, height - y - 1);
        levelBarCircle.setPosition(0, y);
        levelBarSelectCircle.setPosition(0, y);
        levelBarRect.setFillColor(levelColor);
        levelBarCircle.setFillColor(levelColor);
        levelBarSelectCircle.setFillColor(levelColorTrans);
        push();
        translate(tickToX(midiNote.getOnTick()) + absoluteX, absoluteY);
        levelBarGroup.draw();
        pop();
    }

    private float[] calcLevelColor(boolean selected) {
        if (selected) {
            return Color.LEVEL_SELECTED;
        } else {
            switch (currLevelType) {
                case VOLUME:
                    return Color.TRON_BLUE;
                case PAN:
                    return Color.PAN;
                case PITCH:
                    return Color.PITCH;
                default:
                    return Color.LEVEL_SELECTED;
            }
        }
    }

    private float[] calcLevelColorTrans(boolean selected) {
        if (selected) {
            return Color.LEVEL_SELECTED_TRANS;
        } else {
            switch (currLevelType) {
                case VOLUME:
                    return Color.TRON_BLUE_TRANS;
                case PAN:
                    return Color.PAN_TRANS;
                case PITCH:
                    return Color.PITCH_TRANS;
                default:
                    return Color.LEVEL_SELECTED_TRANS;
            }
        }
    }

    private boolean selectLevel(int pointerId, Pointer pos) {
        Track track = (Track) context.getTrackManager().getCurrTrack();
        for (MidiNote midiNote : track.getMidiNotes()) {
            float velocityY = levelToY(midiNote.getLinearLevel(currLevelType));
            if (Math.abs(tickToX(midiNote.getOnTick()) - pos.x) < 35
                    && Math.abs(velocityY - pos.y) < 35) {
                // If this is the only touched level, and it hasn't yet
                // been selected, make it the only selected level.
                // If we are multi-selecting, add it to the selected list
                if (!midiNote.isSelected()) {
                    if (touchedNotes.isEmpty()) {
                        context.getTrackManager().deselectAllNotes();
                    }
                    midiNote.setSelected(true);
                }
                valueLabel.show();
                updateValueLabel(midiNote);
                touchedNotes.put(pointerId, midiNote);
                updateLevelOffsets();
                return true;
            }
        }
        if (touchedNotes.isEmpty()) {
            context.getTrackManager().deselectAllNotes();
        }
        return false;
    }

    public void selectRegion(float x, float y) {
        float leftX = Math.min(Math.max(x, BG_OFFSET), selectRegionStartX);
        float rightX = Math.max(Math.min(x, width - BG_OFFSET), selectRegionStartX);
        float topY = Math.max(BG_OFFSET, Math.min(y, selectRegionStartY));
        float bottomY = Math.max(y, selectRegionStartY);

        float leftTick = xToTick(leftX), rightTick = xToTick(rightX);

        Track track = (Track) context.getTrackManager().getCurrTrack();
        for (MidiNote selectedNote : track.getMidiNotes()) {
            float levelY = levelToY(selectedNote.getLinearLevel(currLevelType));
            boolean selected = leftTick < selectedNote.getOnTick()
                    && rightTick > selectedNote.getOnTick() && topY < levelY && bottomY > levelY;
            selectedNote.setSelected(selected);
        }

        selectRegionRect
                .layout(absoluteX + leftX, absoluteY + topY, rightX - leftX, bottomY - topY);
        selectRegionRect.setFillColor(Color.TRON_BLUE_TRANS);
    }

    private void updateDragLine() {
        int touchedSize = touchedNotes.size();
        if (touchedSize == 1) {
            dragLine.m = 0;
            MidiNote touched = (MidiNote) touchedNotes.valueAt(0);
            dragLine.b = touched.getLinearLevel(currLevelType);
            dragLine.leftTick = 0;
            dragLine.rightTick = Float.MAX_VALUE;
            dragLine.leftLevel = dragLine.rightLevel = touched.getLinearLevel(currLevelType);
        } else if (touchedSize == 2) {
            MidiNote first = touchedNotes.valueAt(0);
            MidiNote second = touchedNotes.valueAt(1);
            MidiNote leftLevel = first.getOnTick() < second.getOnTick() ? first : second;
            MidiNote rightLevel = first.getOnTick() < second.getOnTick() ? second : first;
            dragLine.m = (rightLevel.getLinearLevel(currLevelType) - leftLevel
                    .getLinearLevel(currLevelType))
                    / (rightLevel.getOnTick() - leftLevel.getOnTick());
            dragLine.b = (leftLevel.getLinearLevel(currLevelType) - dragLine.m
                    * leftLevel.getOnTick());
            dragLine.leftTick = leftLevel.getOnTick();
            dragLine.rightTick = rightLevel.getOnTick();
            dragLine.leftLevel = leftLevel.getLinearLevel(currLevelType);
            dragLine.rightLevel = rightLevel.getLinearLevel(currLevelType);
        }
    }

    private void updateLevelOffsets() {
        levelOffsets.clear();
        updateDragLine();
        for (MidiNote selectedNote : context.getTrackManager().getSelectedNotes()) {
            levelOffsets.put(
                    selectedNote,
                    selectedNote.getLinearLevel(currLevelType)
                            - dragLine.getLevel(selectedNote.getOnTick()));
        }
    }

    private void setLevelsToDragLine() {
        for (MidiNote selectedNote : context.getTrackManager().getSelectedNotes()) {
            if (levelOffsets.get(selectedNote) != null) {
                float linear = dragLine.getLevel(selectedNote.getOnTick())
                        + levelOffsets.get(selectedNote);
                selectedNote.setLevel(currLevelType, GeneralUtils.linearToByte(linear));
            }
        }
    }

    private void startSelectRegion(Pointer pos) {
        selectRegionStartX = pos.x;
        selectRegionStartY = pos.y;
    }

    public void draw() {
        List<MidiNote> notes = ((Track) context.getTrackManager().getCurrTrack()).getMidiNotes();
        for (MidiNote note : notes) {
            drawLevel(note, calcLevelColor(note.isSelected()),
                    calcLevelColorTrans(note.isSelected()));
        }
    }

    private float levelToY(float linearLevel) {
        return height - linearLevel * getLevelHeight() - LEVEL_POINT_SIZE / 2;
    }

    private float levelToLabelY(float linearLevel) {
        return height - linearLevel * (getLevelHeight() - valueLabel.height) - LEVEL_POINT_SIZE / 2
                - valueLabel.height;
    }

    /*
     * map y value of level bar to a value in [0,1]
     */
    private float yToLevel(float y) {
        return (height - y - LEVEL_POINT_SIZE / 2) / getLevelHeight();
    }

    private float tickToX(float tick) {
        final MidiView midiView = context.getMainPage().getMidiView();
        return midiView.tickToX(tick) + midiView.absoluteX - absoluteX;
    }

    private float xToTick(float x) {
        final MidiView midiView = context.getMainPage().getMidiView();
        return midiView.xToTick(x + absoluteX - midiView.absoluteX);
    }

    private float getLevelHeight() {
        return height - LEVEL_POINT_SIZE;
    }

    private void updateValueLabel(MidiNote touchedNote) {
        valueLabel.setPosition(absoluteX + tickToX(touchedNote.getOnTick()) + width / 40, absoluteY
                + levelToLabelY(touchedNote.getLinearLevel(currLevelType)));
        valueLabel.setText(touchedNote.getLevelDisplay(currLevelType));
    }

    @Override
    public void handleActionPointerUp(int id, Pointer pos) {
        touchedNotes.remove(id);
        updateLevelOffsets();
    }

    @Override
    public void handleActionMove(int id, Pointer pos) {
        if (!touchedNotes.isEmpty()) {
            MidiNote touched = touchedNotes.get(id);
            if (touched != null) {
                touched.setLevel(currLevelType, GeneralUtils.linearToByte(yToLevel(pos.y)));
                updateValueLabel(touched);
            }
            if (id == pointerById.size() - 1) {
                updateDragLine();
                setLevelsToDragLine();
            }
        } else if (id == 0) {
            selectRegion(pos.x, pos.y);
        }
    }

    @Override
    public void handleActionDown(int id, Pointer pos) {
        super.handleActionDown(id, pos);
        context.getMidiManager().beginEvent();
        if (!selectLevel(id, pos)) {
            startSelectRegion(pos);
        }
    }

    @Override
    public void handleActionPointerDown(int id, Pointer pos) {
        selectLevel(id, pos);
    }

    @Override
    public void handleActionUp(int id, Pointer pos) {
        super.handleActionUp(id, pos);
        clearTouchedLevels();
        selectRegionRect.setFillColor(Color.TRANSPARENT);
        valueLabel.hide();
        context.getMidiManager().endEvent();
    }

    @Override
    protected void createChildren() {
        initRoundedRect();

        levelBarGroup = new RenderGroup();
        selectRegionRect = new Rectangle(renderGroup, Color.TRANSPARENT, null);
        levelBarRect = new Rectangle(levelBarGroup, Color.TRON_BLUE, null);
        levelBarCircle = new Circle(levelBarGroup, Color.TRON_BLUE, null);
        levelBarSelectCircle = new Circle(levelBarGroup, Color.TRON_BLUE, null);
        valueLabel = new ValueLabel(this, null);
        valueLabel.setIcon(IconResourceSets.VALUE_LABEL_VIEW_ONLY);
        valueLabel.disable();
        addShapes(selectRegionRect);
    }

    @Override
    public void layoutChildren() {
        levelBarRect.layout(-LEVEL_BAR_WIDTH / 2, 0, LEVEL_BAR_WIDTH, height);
        levelBarCircle.layout(0, 0, LEVEL_BAR_WIDTH, LEVEL_BAR_WIDTH);
        levelBarSelectCircle.layout(0, 0, LEVEL_BAR_WIDTH * 2, LEVEL_BAR_WIDTH * 2);
        valueLabel.setDimensions(width / 12, width / 26);
        valueLabel.hide();
    }
}
