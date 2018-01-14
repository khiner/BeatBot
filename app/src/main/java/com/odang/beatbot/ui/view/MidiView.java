package com.odang.beatbot.ui.view;

import android.util.SparseArray;

import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.effect.Effect.LevelType;
import com.odang.beatbot.listener.LoopWindowListener;
import com.odang.beatbot.listener.MidiNoteListener;
import com.odang.beatbot.listener.TrackListener;
import com.odang.beatbot.manager.MidiManager;
import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.midi.TouchedNotes;
import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.color.Color;
import com.odang.beatbot.ui.shape.Line;
import com.odang.beatbot.ui.shape.Rectangle;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.shape.RoundedRect;
import com.odang.beatbot.ui.transition.ColorTransition;
import com.odang.beatbot.ui.view.helper.ScrollHelper;
import com.odang.beatbot.ui.view.helper.Scrollable;

public class MidiView extends ClickableView implements TrackListener, Scrollable,
        LoopWindowListener, MidiNoteListener {

    public static float trackHeight;

    private float dragOffsetTick[] = {0, 0, 0, 0, 0};
    private float pinchLeftOffset = 0, pinchRightOffset = 0, zoomLeftAnchorTick = 0,
            zoomRightAnchorTick = 0, selectRegionStartTick = -1, selectRegionStartY = -1;

    private boolean pinchingLoopWindow = false;

    private int pinchLeftPointerId = -1, pinchRightPointerId = -1;

    private Line[] loopMarkerLines, tickLines;
    private Line currTickLine;
    private Rectangle leftLoopRect, rightLoopRect, selectRect;
    private RoundedRect horizontalScrollBar;

    // map of pointerIds to the notes they are selecting
    private TouchedNotes touchedNotes = new TouchedNotes();
    // map of pointerIds to the original on-ticks of the notes they are touching (before dragging)
    private SparseArray<Float> startOnTicks = new SparseArray<Float>();

    protected ScrollHelper scrollHelper;

    private RenderGroup translateYGroup, translateScaleGroup;

    public ColorTransition scrollBarColorTrans = new ColorTransition(20, 20, Color.TRANSPARENT,
            new float[]{0, 0, 0, .7f});

    public MidiView(View view, RenderGroup scaleGroup, RenderGroup translateYGroup,
                    RenderGroup translateScaleGroup) {
        super(view);
        shouldDraw = false; // drawing handled by MidiViewGroup (parent)
        this.translateYGroup = translateYGroup;
        this.translateScaleGroup = translateScaleGroup;

        setClip(true);

        leftLoopRect = new Rectangle(scaleGroup, Color.DARK_TRANS, null);
        rightLoopRect = new Rectangle(scaleGroup, Color.DARK_TRANS, null);
        selectRect = new Rectangle(translateScaleGroup, Color.TRON_BLUE_TRANS, Color.TRON_BLUE);
        currTickLine = new Line(translateScaleGroup, null, Color.TRON_BLUE);
        tickLines = new Line[(MidiManager.MAX_TICKS * 8) / MidiManager.MIN_TICKS];
        loopMarkerLines = new Line[2];
        for (int i = 0; i < tickLines.length; i++)
            tickLines[i] = new Line(scaleGroup, null, Color.TRANSPARENT);
        for (int i = 0; i < loopMarkerLines.length; i++)
            loopMarkerLines[i] = new Line(translateScaleGroup, null, Color.TRON_BLUE);

        horizontalScrollBar = new RoundedRect(renderGroup, Color.TRANSPARENT, null);

        addShapes(leftLoopRect, rightLoopRect, selectRect, currTickLine, horizontalScrollBar);
        addShapes(tickLines);
        addShapes(loopMarkerLines);

        scrollHelper = new ScrollHelper(this);
        context.getMidiManager().addLoopChangeListener(this);
    }

    public float getTotalTrackHeight() {
        return trackHeight * context.getTrackManager().getNumTracks();
    }

    public void reset() {
        scrollHelper.setXOffset(0);
    }

    public float tickToUnscaledX(float tick) {
        return tick * width / MidiManager.MAX_TICKS;
    }

    public float tickToX(float tick) {
        return (tick - scrollHelper.xOffset) / getNumTicks() * width;
    }

    public float xToTick(float x) {
        return getNumTicks() * x / width + scrollHelper.xOffset;
    }

    public int yToNote(float y) {
        float f = ((y + scrollHelper.yOffset) / trackHeight);
        return f < 0 ? -1 : (int) f;
    }

    public float noteToY(int note) {
        return note * trackHeight - scrollHelper.yOffset;
    }

    public float noteToUnscaledY(int note) {
        return note * trackHeight;
    }

    public float getXOffset() {
        return scrollHelper.xOffset;
    }

    public float getYOffset() {
        return scrollHelper.yOffset;
    }

    public float getNumTicks() {
        return scrollHelper.numTicks;
    }

    @Override
    public void layoutChildren() {
        horizontalScrollBar.setCornerRadius(getLabelHeight() / 5f);
        for (int i = 0; i < loopMarkerLines.length; i++) {
            loopMarkerLines[i].layout(absoluteX, absoluteY, 3, height);
        }
        int minTickSpacing = MidiManager.MIN_TICKS / 8;

        for (int i = 0, currTick = 0; currTick < MidiManager.MAX_TICKS; i++, currTick += minTickSpacing) {
            tickLines[i].layout(tickToUnscaledX(currTick), absoluteY, 2, 1);
        }
        onScaleX();
        onTrackHeightChange();
        onLoopWindowChange(context.getMidiManager().getLoopBeginTick(), context.getMidiManager()
                .getLoopEndTick());
        updateCurrentTick();
        scrollHelper.setYOffset(0);
        layoutNotes();
    }

    @Override
    public void tick() {
        if (context.getPlaybackManager().isPlaying()) {
            updateCurrentTick();
        }
        if (pointerCount() == 0) {
            scrollHelper.scroll(); // take care of any momentum scrolling
            if (scrollHelper.scrollXVelocity == 0) {
                scrollBarColorTrans.end();
            }
        }
        scrollBarColorTrans.tick();
        horizontalScrollBar.setFillColor(scrollBarColorTrans.getColor());
    }

    @Override
    public void handleActionDown(int id, Pointer pos) {
        super.handleActionDown(id, pos);
        context.getMidiManager().beginEvent();
        selectMidiNote(id, pos);
        if (touchedNotes.get(id) == null) { // no note selected.
            MidiLoopBarView midiLoopBarView = getMidiLoopBarView();
            if (midiLoopBarView.isPressed()) {
                float leftTick = xToTick(Math.min(pos.x, midiLoopBarView.getPointer().x));
                float rightTick = xToTick(Math.max(pos.x, midiLoopBarView.getPointer().x));
                pinchLeftOffset = leftTick - context.getMidiManager().getLoopBeginTick();
                pinchRightOffset = context.getMidiManager().getLoopEndTick() - rightTick;

                if (pos.x < midiLoopBarView.getPointer().x) {
                    pinchLeftPointerId = pos.id;
                    pinchRightPointerId = midiLoopBarView.getPointer().id;
                } else {
                    pinchLeftPointerId = midiLoopBarView.getPointer().id;
                    pinchRightPointerId = pos.id;
                }

                pinchingLoopWindow = true;
            } else { // enable scrolling
                scrollHelper.setScrollAnchor(id, xToTick(pos.x), pos.y + scrollHelper.yOffset);
            }
        }
    }

    @Override
    public void handleActionPointerDown(int id, Pointer pos) {
        super.handleActionPointerDown(id, pos);
        selectMidiNote(id, pos);
        if (pointerCount() > 2 || null != touchedNotes.get(id))
            return;
        Pointer firstPointer = pointerById.get(0);
        Pointer secondPointer = pointerById.get(1);
        if (firstPointer == null || secondPointer == null)
            return; // shouldn't ever happen

        float leftTick = xToTick(Math.min(firstPointer.x, secondPointer.x));
        float rightTick = xToTick(Math.max(firstPointer.x, secondPointer.x));
        if (!touchedNotes.isEmpty()) {
            long[] selectedNoteTickWindow = context.getTrackManager().getSelectedNoteTickWindow();
            // note is selected with one pointer, but this pointer
            // did not select a note. start pinching all selected notes.
            if (firstPointer.x <= secondPointer.x) {
                pinchLeftPointerId = firstPointer.id;
                pinchRightPointerId = secondPointer.id;
            } else {
                pinchLeftPointerId = secondPointer.id;
                pinchRightPointerId = firstPointer.id;
            }
            pinchLeftOffset = leftTick - selectedNoteTickWindow[0];
            pinchRightOffset = selectedNoteTickWindow[1] - rightTick;
        } else if (pointerCount() == 1) {
            // otherwise, enable scrolling
            scrollHelper.setScrollAnchor(id, xToTick(pos.x));
        } else {
            // can never select region with two pointers in midi view
            stopSelectRegion();
            // init zoom anchors (the same ticks should be under the
            // fingers at all times)
            zoomLeftAnchorTick = leftTick;
            zoomRightAnchorTick = rightTick;
        }
    }

    @Override
    public void handleActionMove(int id, Pointer pos) {
        super.handleActionMove(id, pos);
        synchronized (context.getMainPage().getMidiViewGroup()) {
            handleActionMoveInner(id, pos);
        }
    }

    private void handleActionMoveInner(final int id, final Pointer pos) {
        if ((id == pinchLeftPointerId || id == pinchRightPointerId)) {
            if (isPinchingLoopWindow()) {
                pinchLoopWindow(id, pos);
            } else {
                float tick = xToTick(pos.x);
                final long[] selectedNoteTickWindow = context.getTrackManager().getSelectedNoteTickWindow();
                final long beginTickDiff;
                final long endTickDiff;
                if (id == pinchLeftPointerId) {
                    beginTickDiff = (long) (tick - pinchLeftOffset - selectedNoteTickWindow[0]);
                    endTickDiff = 0;
                } else {
                    beginTickDiff = 0;
                    endTickDiff = (long) (tick + pinchRightOffset - selectedNoteTickWindow[1]);
                }
                context.getMidiManager().pinchSelectedNotes(beginTickDiff, endTickDiff);
            }
        } else if (touchedNotes.isEmpty()) {
            // no midi selected. scroll, zoom, or update select region
            noMidiMove();
        } else if (touchedNotes.get(id) != null) { // at least one midi selected
            float tick = xToTick(pos.x);
            int note = yToNote(pos.y);
            if (touchedNotes.size() == 1) {
                // exactly one pointer not dragging loop markers - drag all selected notes together
                dragNotes(true, touchedNotes.keyAt(0), note, tick);
                // make room in the view window if we are dragging out of the view
                scrollHelper.updateView(tick);
            } else if (touchedNotes.size() > 1) { // drag each touched note separately
                dragNotes(false, id, note, tick);
                if (id == 0) {
                    // need to make room for two pointers in this case.
                    float otherTick = xToTick(pointerById.get(1).x);
                    scrollHelper.updateView(Math.min(tick, otherTick), Math.max(tick, otherTick));
                }
            }
        }
    }

    public void pinchLoopWindow(final int id, final Pointer pos) {
        final MidiManager midiManager = context.getMidiManager();

        float tick = xToTick(pos.x);
        final long beginTickDiff;
        final long endTickDiff;
        if (id == pinchLeftPointerId) {
            beginTickDiff = (long) (tick - pinchLeftOffset - midiManager.getLoopBeginTick());
            endTickDiff = 0;
        } else {
            beginTickDiff = 0;
            endTickDiff = (long) (tick + pinchRightOffset - midiManager.getLoopEndTick());
        }
        midiManager.pinchLoopWindow(beginTickDiff, endTickDiff);
    }

    @Override
    public void handleActionPointerUp(int id, Pointer pos) {
        scrollHelper.pointerUp(id);
        if (id == pinchLeftPointerId || id == pinchRightPointerId) {
            pinchLeftPointerId = pinchRightPointerId = -1;
            pinchingLoopWindow = false;
        }
        if (zoomLeftAnchorTick != -1) {
            int otherId = id == 0 ? 1 : 0;
            Pointer otherPointer = pointerById.get(otherId);
            if (otherPointer != null) {
                scrollHelper.setScrollAnchor(otherId, xToTick(otherPointer.x));
            }
        }
        touchedNotes.remove(id);
    }

    @Override
    public void handleActionUp(int id, Pointer pos) {
        super.handleActionUp(id, pos);
        scrollHelper.handleActionUp();
        pinchLeftPointerId = pinchRightPointerId = -1;
        pinchingLoopWindow = false;
        stopSelectRegion();
        startOnTicks.clear();
        touchedNotes.clear();
        context.getMidiManager().endEvent();
    }

    @Override
    protected void longPress(int id, Pointer pos) {
        if (pointerCount() == 1 && touchedNotes.get(id) == null
                && !getMidiLoopBarView().isPressed()) {
            startSelectRegion(pos);
        }
    }

    @Override
    protected void singleTap(int id, Pointer pos) {
        MidiNote touchedNote = touchedNotes.get(id);
        if (context.getMidiManager().isCopying()) {
            context.getMidiManager().paste(
                    context.getMidiManager().getMajorTickBefore((long) xToTick(pos.x)));
        } else if (touchedNote != null) {
            // single tapping a note always makes it the only selected note
            if (touchedNote.isSelected()) {
                context.getTrackManager().deselectAllNotes();
            }
            touchedNote.setSelected(true);
        } else { // if no note is touched, than this tap deselects all notes
            if (context.getTrackManager().anyNoteSelected()) {
                context.getTrackManager().deselectAllNotes();
            } else { // add a note based on the current tick granularity
                addMidiNote(yToNote(pos.y), xToTick(pos.x));
            }
        }
    }

    @Override
    protected void doubleTap(int id, Pointer pos) {
        MidiNote touchedNote = touchedNotes.get(id);
        if (null != touchedNote) {
            context.getMidiManager().deleteNote(touchedNote);
        }
    }

    @Override
    public void onCreate(Track track) {
        Rectangle trackRect = new Rectangle(translateYGroup, Color.TRANSPARENT, Color.BLACK);
        addShapes(trackRect);
        track.setRectangle(trackRect);
        onTrackHeightChange();
        scrollHelper.setYOffset(Float.MAX_VALUE);
        for (MidiNote note : track.getMidiNotes()) {
            onCreate(note);
            onSelectStateChange(note);
        }
    }

    @Override
    public void onDestroy(Track track) {
        for (MidiNote note : track.getMidiNotes()) {
            onDestroy(note);
        }
        removeShape(track.getRectangle());
        onTrackHeightChange();
        layoutNotes();
        scrollHelper.setYOffset(scrollHelper.yOffset);
    }

    @Override
    public void onSelect(BaseTrack track) {
        updateTrackColors();
    }

    @Override
    public void onSampleChange(Track track) {
    }

    @Override
    public void onMuteChange(Track track, boolean mute) {
        updateTrackColors();
    }

    @Override
    public void onSoloChange(Track track, boolean solo) {
        updateTrackColors();
    }

    @Override
    public void onReverseChange(Track track, boolean reverse) {
    }

    @Override
    public void onLoopChange(Track track, boolean loop) {
    }

    @Override
    public float unscaledHeight() {
        return getTotalTrackHeight();
    }

    @Override
    public void onScrollX() {
        float displayOffsetX = width / 50;
        float w = scrollHelper.numTicks * (width - displayOffsetX * 2) / MidiManager.MAX_TICKS;
        updateHorizontalScrollBarPosition();
        horizontalScrollBar.setDimensions(w, 2 * horizontalScrollBar.cornerRadius);
    }

    @Override
    public void onScrollY() {
    }

    @Override
    public void onScaleX() {
        context.getMidiManager().adjustBeatDivision(getNumTicks());
        updateTickLineColors();
    }

    @Override
    public float width() {
        return width;
    }

    @Override
    public float height() {
        return height;
    }

    @Override
    public void onLoopWindowChange(long loopBeginTick, long loopEndTick) {
        float x1 = tickToUnscaledX(loopBeginTick);
        float x2 = tickToUnscaledX(loopEndTick);
        leftLoopRect.layout(0, absoluteY, x1, leftLoopRect.height);
        rightLoopRect.layout(x2, absoluteY, width - x2, rightLoopRect.height);
        loopMarkerLines[0].setPosition(x1, absoluteY);
        loopMarkerLines[1].setPosition(x2, absoluteY);
        scrollHelper.updateView(loopBeginTick, loopEndTick + context.getMidiManager().getMajorTickSpacing());
        scrollBarColorTrans.begin();
    }

    public boolean isPinchingLoopWindow() {
        return pinchingLoopWindow;
    }

    private void noMidiMove() {
        if (pointerCount() == 1) {
            if (selectRegionStartTick >= 0) {
                selectRegion(getPointer());
            } else { // one finger scroll
                scrollHelper.scroll(getPointer());
            }
        } else if (pointerCount() == 2) {
            // two finger zoom
            Pointer leftPointer = pointerById.get(0);
            Pointer rightPointer = pointerById.get(1);
            if (leftPointer != null && rightPointer != null) {
                float leftX = Math.min(pointerById.get(0).x, pointerById.get(1).x);
                float rightX = Math.max(pointerById.get(0).x, pointerById.get(1).x);
                scrollHelper.zoom(leftX, rightX, zoomLeftAnchorTick, zoomRightAnchorTick);
            }
        }
    }

    private static float[] whichColor(MidiNote note) {
        boolean selected = context.getTrackManager().getTrackByNoteValue(note.getNoteValue())
                .isSelected();
        if (selected)
            return note.isSelected() ? Color.NOTE_SELECTED_LIGHT : Color.NOTE_LIGHT;
        else
            return note.isSelected() ? Color.NOTE_SELECTED : Color.NOTE;
    }

    private static float[] whichColor(Track track) {
        BaseTrack soloingTrack = context.getTrackManager().getSoloingTrack();
        if (track.isSelected())
            return Color.LABEL_SELECTED;
        else if (track.isMuted() || (soloingTrack != null && !soloingTrack.equals(track)))
            return Color.MIDI_VIEW_DARK_BG;
        else
            return Color.MIDI_VIEW_BG;
    }

    private void dragNotes(boolean dragAllSelected, int pointerId, int currNote, float currTick) {
        MidiNote touchedNote = touchedNotes.get(pointerId);
        if (touchedNote == null)
            return;
        int noteDiff = currNote - touchedNote.getNoteValue();
        float tickDiff = currTick - dragOffsetTick[pointerId] - touchedNote.getOnTick();
        if (noteDiff == 0 && tickDiff == 0)
            return;
        tickDiff = context.getTrackManager().getAdjustedTickDiff(tickDiff,
                startOnTicks.get(pointerId).longValue(), dragAllSelected ? null : touchedNote);
        noteDiff = context.getTrackManager().getAdjustedNoteDiff(noteDiff,
                dragAllSelected ? null : touchedNote);
        if (dragAllSelected)
            context.getMidiManager().moveSelectedNotes(noteDiff, (long) tickDiff);
        else
            context.getMidiManager().moveNote(touchedNote, noteDiff, (long) tickDiff);
    }

    private void updateTrackColors() {
        for (Track track : context.getTrackManager().getTracks()) {
            track.getRectangle().setFillColor(whichColor(track));
            for (MidiNote note : track.getMidiNotes()) {
                note.getRectangle().setFillColor(whichColor(note));
            }
        }
    }

    private void onTrackHeightChange() {
        float trackHeight = getTotalTrackHeight();
        float lineHeight = Math.min(trackHeight, height);
        currTickLine.setDimensions(currTickLine.width, trackHeight);
        leftLoopRect.setDimensions(leftLoopRect.width, lineHeight);
        rightLoopRect.setDimensions(rightLoopRect.width, lineHeight);
        for (int i = 0; i < loopMarkerLines.length; i++) {
            loopMarkerLines[i].setDimensions(loopMarkerLines[i].width, trackHeight);
        }

        for (Line line : tickLines) {
            line.setDimensions(2, lineHeight);
        }
        layoutTrackRects();
        updateTrackColors();
        updateHorizontalScrollBarPosition();
    }

    private void updateHorizontalScrollBarPosition() {
        float displayOffsetX = width / 50;
        float x = tickToUnscaledX(scrollHelper.xOffset) + displayOffsetX;
        float y = Math.min(unscaledHeight(), height) - 2.5f * horizontalScrollBar.cornerRadius;
        horizontalScrollBar.setPosition(absoluteX + x, absoluteY + y);
    }

    private void selectRegion(Pointer pos) {
        float tick = xToTick(pos.x);
        float leftTick = Math.min(tick, selectRegionStartTick);
        float rightTick = Math.max(tick, selectRegionStartTick);
        float topY = Math.min(pos.y, selectRegionStartY);
        float bottomY = Math.max(pos.y, selectRegionStartY);
        // make sure select rect doesn't go into the tick view
        topY = Math.max(topY + .01f, 0);
        // make sure select rect doesn't go past the last track/note
        bottomY = Math.min(bottomY + .01f, getTotalTrackHeight() - .01f);
        int topNote = yToNote(topY);
        int bottomNote = yToNote(bottomY);
        context.getTrackManager().selectRegion((long) leftTick, (long) rightTick, topNote,
                bottomNote);
        // for normal view, round the drawn rectangle to nearest notes
        topY = noteToY(topNote);
        bottomY = noteToY(bottomNote + 1);
        // make room in the view window if we are dragging out of the view
        scrollHelper.updateView(tick, topY, bottomY);
        selectRect.layout(tickToUnscaledX(leftTick), absoluteY + topY, tickToUnscaledX(rightTick
                - leftTick), bottomY - topY);
    }

    private void startSelectRegion(Pointer pos) {
        selectRegionStartTick = xToTick(pos.x);
        selectRegionStartY = noteToY(yToNote(pos.y));
        selectRect.layout(tickToUnscaledX(selectRegionStartTick), absoluteY + selectRegionStartY,
                1, trackHeight);
        selectRect.show();
        selectRect.bringToTop();
    }

    private void stopSelectRegion() {
        selectRegionStartTick = -1;
        selectRect.hide();
    }

    // adds a note starting at the nearest major tick (nearest displayed
    // grid line) to the left and ending one tick before the nearest major
    // tick to the right of the given tick
    private void addMidiNote(int track, float tick) {
        if (track < 0 || track >= context.getTrackManager().getNumTracks())
            return;
        long onTick = context.getMidiManager().getMajorTickBefore((long) tick);
        long offTick = context.getMidiManager().getMajorTickAfter((long) tick) - 1;
        context.getMidiManager().addNote(onTick, offTick, track);
    }

    private void selectMidiNote(int pointerId, Pointer pos) {
        final float tick = xToTick(pos.x);
        MidiNote selectedNote = context.getMidiManager().findNoteContaining(yToNote(pos.y),
                (long) tick);
        if (selectedNote == null || selectedNote.isTouched())
            return;

        startOnTicks.put(pointerId, (float) selectedNote.getOnTick());
        dragOffsetTick[pointerId] = tick - selectedNote.getOnTick();
        // don't need right offset for simple drag (one finger select)

        // If this is the only touched midi note, and it hasn't yet
        // been selected, make it the only selected note.
        // If we are multi-selecting, add it to the selected list
        if (!selectedNote.isSelected()) {
            if (touchedNotes.isEmpty()) {
                context.getTrackManager().deselectAllNotes();
            }
            selectedNote.setSelected(true);
        }
        touchedNotes.put(pointerId, selectedNote);
    }

    private void updateCurrentTick() {
        currTickLine
                .setPosition(tickToUnscaledX(context.getMidiManager().getCurrTick()), absoluteY);
    }

    private void layoutTrackRects() {
        for (int i = 0; i < context.getTrackManager().getNumTracks(); i++) {
            Track track = context.getTrackManager().getTrackByNoteValue(i);
            Rectangle trackRect = track.getRectangle();
            if (trackRect != null) {
                trackRect.layout(absoluteX, absoluteY + noteToUnscaledY(i), width, trackHeight);
            }
        }
    }

    private void updateTickLineColors() {
        for (int i = 0; i < tickLines.length; i++) {
            float[] lineColor = lineColor(i);
            if (!tickLines[i].getStrokeColor().equals(lineColor)) {
                tickLines[i].setStrokeColor(lineColor);
            }
        }
    }

    private float[] lineColor(int lineIndex) {
        for (int i = 0; i < Color.MIDI_LINES.length; i++) {
            if (i > context.getMidiManager().getBeatDivision() + 3) {
                return Color.TRANSPARENT;
            } else if (lineIndex % (tickLines.length / (1 << i)) == 0) {
                return Color.MIDI_LINES[i];
            }
        }
        return Color.TRANSPARENT;
    }

    @Override
    public void onCreate(MidiNote note) {
        if (note.getRectangle() == null) {
            Rectangle noteRect = new Rectangle(translateScaleGroup, whichColor(note), Color.BLACK);
            note.setRectangle(noteRect);
            addShapes(noteRect);
            layoutNoteRectangle(note);
        }
    }

    @Override
    public void onDestroy(MidiNote note) {
        removeShape(note.getRectangle());
        note.setRectangle(null);
    }

    @Override
    public void onMove(MidiNote note, int beginNoteValue, long beginOnTick, long beginOffTick,
                       int endNoteValue, long endOnTick, long endOffTick) {
        layoutNoteRectangle(note);
        if (beginNoteValue != endNoteValue) { // could be moving into/out of selected tack
            note.getRectangle().setFillColor(whichColor(note));
        }
    }

    private void layoutNotes() {
        for (Track track : context.getTrackManager().getTracks()) {
            for (MidiNote note : track.getMidiNotes()) {
                layoutNoteRectangle(note);
                note.getRectangle().setFillColor(whichColor(note));
            }
        }
    }

    private void layoutNoteRectangle(MidiNote note) {
        Rectangle rectangle = note.getRectangle();
        if (rectangle != null) {
            rectangle.layout(tickToUnscaledX(note.getOnTick()),
                    absoluteY + noteToUnscaledY(note.getNoteValue()),
                    tickToUnscaledX(note.getOffTick() - note.getOnTick()), trackHeight);
        }
    }

    @Override
    public void onSelectStateChange(MidiNote note) {
        Rectangle rectangle = note.getRectangle();
        if (rectangle != null) {
            rectangle.setFillColor(whichColor(note));
            rectangle.bringToTop();
        }
        // loop/tick lines always display ontop of notes
        currTickLine.bringToTop();
        loopMarkerLines[0].bringToTop();
        loopMarkerLines[1].bringToTop();
        selectRect.bringToTop();
    }

    @Override
    public void beforeLevelChange(MidiNote note) {
        // no-op
    }

    @Override
    public void onLevelChange(MidiNote note, LevelType type) {
        // no-op
    }

    @Override
    public void onEffectCreate(BaseTrack track, Effect effect) {
        // no-op
    }

    @Override
    public void onEffectDestroy(BaseTrack track, Effect effect) {
        // no-op
    }

    @Override
    public void onEffectOrderChange(BaseTrack track, int initialEffectPosition,
                                    int endEffectPosition) {
        // no-op
    }

    private MidiLoopBarView getMidiLoopBarView() {
        return context.getMainPage().getMidiViewGroup().midiLoopBarView;
    }
}
