package com.odang.beatbot.ui.view;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.listener.OnPressListener;
import com.odang.beatbot.midi.util.GeneralUtils;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.color.Color;
import com.odang.beatbot.ui.icon.IconResourceSet.State;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.shape.Rectangle;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.shape.WaveformShape;
import com.odang.beatbot.ui.view.control.Button;
import com.odang.beatbot.ui.view.control.ControlView2dBase;

public class SampleEditView extends ControlView2dBase {
    private static final String NO_SAMPLE_MESSAGE = "Tap to load a sample.";

    // min distance for pointer to select loop markers
    private WaveformShape waveformShape;
    private Button[] loopButtons;
    private Rectangle currSampleRect;

    private int scrollPointerId = -1, zoomLeftPointerId = -1, zoomRightPointerId = -1;
    private float scrollAnchorLevel = -1, zoomLeftAnchorLevel = -1, zoomRightAnchorLevel = -1;

    // Zooming/scrolling will change the view window of the samples.
    // Keep track of that with offset and width.
    private float levelOffset = 0, levelWidth = 0, waveformWidth = 0, loopButtonW = 0;

    private boolean needsResample = false;

    public SampleEditView(View view, RenderGroup renderGroup) {
        super(view, renderGroup);
    }

    public synchronized void update() {
        if (height <= 0) // uninitialized
            return;
        loopButtonW = height / 3;
        waveformWidth = width - loopButtonW;

        if (hasSample()) {
            if (null == waveformShape) {
                waveformShape = new WaveformShape(renderGroup, waveformWidth, Color.LABEL_SELECTED,
                        Color.BLACK);
                addShapes(waveformShape);
            }
            waveformShape.layout(absoluteX, absoluteY, waveformWidth, height);
            for (final Button loopButton : loopButtons) {
                loopButton.show();
                loopButton.bgShape.bringToTop();
            }
            setLevel(0, 1);
            waveformShape.resample();
            setText("");
            currSampleRect.hide();
        } else {
            if (null != waveformShape) {
                removeShape(waveformShape);
                waveformShape = null;
            }
            for (Button button : loopButtons) {
                button.hide();
            }
            setText(NO_SAMPLE_MESSAGE);
        }
    }

    private void updateWaveformVb() {
        if (null == waveformShape)
            return;
        float beginX = levelToX(params[0].viewLevel);
        float endX = levelToX(params[1].viewLevel);
        loopButtons[0].layout(this, beginX - loopButtonW / 2, 0, loopButtonW, height);
        loopButtons[1].layout(this, endX - loopButtonW / 2, 0, loopButtonW, height);
        waveformShape.update(beginX, endX, (long) params[0].getLevel(levelOffset),
                (long) params[0].getLevel(levelWidth), loopButtonW / 2);

    }

    private void updateZoom() {
        float x1 = pointerById.get(zoomLeftPointerId).x;
        float x2 = pointerById.get(zoomRightPointerId).x;

        if (x1 >= x2)
            return; // sanity check

        float ZLAL = zoomLeftAnchorLevel, ZRAL = zoomRightAnchorLevel;

        // set levelOffset and levelWidth such that the zoom anchor levels stay under x1 and x2
        float newLevelWidth = waveformWidth * (ZRAL - ZLAL) / (x2 - x1);
        float newLevelOffset = ZRAL - newLevelWidth * (x2 - loopButtonW / 2) / waveformWidth;

        float minLoopWindow = params[0].getViewLevel(Track.MIN_LOOP_WINDOW);

        if (newLevelOffset < 0) {
            newLevelWidth = ZRAL * waveformWidth / (x2 - loopButtonW / 2);
            newLevelWidth = newLevelWidth <= 1 ? (newLevelWidth >= minLoopWindow ? newLevelWidth
                    : minLoopWindow) : 1;
            setLevel(0, newLevelWidth);
        } else if (newLevelWidth > 1) {
            setLevel(newLevelOffset, 1 - newLevelOffset);
        } else if (newLevelWidth < minLoopWindow) {
            setLevel(newLevelOffset, minLoopWindow);
        } else if (newLevelOffset + newLevelWidth > 1) {
            newLevelWidth = ((ZLAL - 1) * waveformWidth) / (x1 - loopButtonW / 2 - waveformWidth);
            setLevel(1 - newLevelWidth, newLevelWidth);
        } else {
            setLevel(newLevelOffset, newLevelWidth);
        }
    }

    @Override
    public void createChildren() {
        setIcon(IconResourceSets.SAMPLE_BG);
        initRoundedRect();
        currSampleRect = new Rectangle(renderGroup, Color.TRON_BLUE, null);
        addShapes(currSampleRect);
        loopButtons = new Button[2];
        for (int i = 0; i < loopButtons.length; i++) {
            loopButtons[i] = new Button(this).withRoundedRect()
                    .withIcon(IconResourceSets.SAMPLE_LOOP);
            loopButtons[i].setShrinkable(false);
            loopButtons[i].deselectOnPointerExit = false;
            loopButtons[i].setOnPressListener(new OnPressListener() {
                @Override
                public void onPress(Button button) {
                    if (button.ownsPointer(scrollPointerId)) {
                        scrollPointerId = -1;
                    } else if (button.ownsPointer(zoomLeftPointerId)) {
                        scrollPointerId = zoomRightPointerId;
                        zoomLeftPointerId = zoomRightPointerId = -1;
                    } else if (button.ownsPointer(zoomRightPointerId)) {
                        scrollPointerId = zoomLeftPointerId;
                        zoomLeftPointerId = zoomRightPointerId = -1;
                    }
                }
            });
        }
    }

    @Override
    public void layoutChildren() {
        currSampleRect.layout(absoluteX, absoluteY, 4, height);
        for (final Button loopButton : loopButtons) {
            loopButton.withCornerRadius(this.getCornerRadius());
        }
    }

    @Override
    public void tick() {
        if (!(context.getTrackManager().getCurrTrack() instanceof Track))
            return;
        final Track track = (Track) context.getTrackManager().getCurrTrack();
        if (hasSample() && track.isSounding()) {
            currSampleRect.show();
            final float currentFrameViewLevel = params[0].getViewLevel(track.getCurrentFrame());
            if (currentFrameViewLevel >= track.getLoopEndParam().viewLevel) {
                track.stopPreviewing();
                currSampleRect.hide();
            } else {
                currSampleRect.setPosition(absoluteX + levelToX(currentFrameViewLevel), absoluteY);
            }
        } else {
            currSampleRect.hide();
        }
    }

    private boolean moveLoopMarker(int id, Pointer pos) {
        return moveLoopMarker(id, pos.x, loopButtons[0], params[0])
                || moveLoopMarker(id, pos.x, loopButtons[1], params[1]);
    }

    private boolean moveLoopMarker(int id, float x, Button button, Param param) {
        if (button.isPressed() && button.ownsPointer(id)) {
            // update track loop begin
            param.setLevel(xToLevel(x));
            // update ui to fit the new begin point
            if (param.viewLevel < levelOffset)
                setLevel(param.viewLevel, levelWidth + levelOffset - param.viewLevel);
            else if (param.viewLevel > levelOffset + levelWidth)
                setLevel(levelOffset, param.viewLevel - levelOffset);
            return true;
        } else {
            return false;
        }
    }

    private boolean setZoomAnchor(int id) {
        if (scrollPointerId == -1) {
            return false; // need to be scrolling to start zooming
        }
        if (pointerById.get(scrollPointerId).x > pointerById.get(id).x) {
            zoomLeftPointerId = id;
            zoomRightPointerId = scrollPointerId;
        } else {
            zoomLeftPointerId = scrollPointerId;
            zoomRightPointerId = id;
        }
        scrollPointerId = -1; // not scrolling anymore

        zoomLeftAnchorLevel = xToLevel(pointerById.get(zoomLeftPointerId).x);
        zoomRightAnchorLevel = xToLevel(pointerById.get(zoomRightPointerId).x);
        return true;
    }

    private void setLevel(float levelOffset, float levelWidth) {
        if (this.levelOffset != levelOffset || this.levelWidth != levelWidth) {
            this.levelOffset = levelOffset;
            this.levelWidth = levelWidth;
            needsResample = true;
        }
        updateWaveformVb();
    }

    private void setScrollAnchor(int id, Pointer pos) {
        if (null != pos) {
            scrollPointerId = id;
            scrollAnchorLevel = xToLevel(pos.x);
        } else {
            scrollPointerId = -1;
        }
    }

    private void scroll(Pointer pos) {
        // set levelOffset such that the scroll anchor level stays under scrollX
        float newLevelOffset = GeneralUtils.clipTo(scrollAnchorLevel - xToLevel(pos.x)
                + levelOffset, 0, 1 - levelWidth);
        setLevel(newLevelOffset, levelWidth);
    }

    @Override
    public synchronized void handleActionDown(int id, Pointer pos) {
        super.handleActionDown(id, pos);
        if (!hasSample())
            return;
        setScrollAnchor(id, pos);
    }

    @Override
    public synchronized void handleActionUp(int id, Pointer pos) {
        super.handleActionUp(id, pos);
        if (!hasSample())
            return;
        if (needsResample) {
            waveformShape.resample();
        }
        needsResample = false;
        scrollPointerId = zoomLeftPointerId = zoomRightPointerId = -1;
    }

    @Override
    public synchronized void handleActionPointerDown(int id, Pointer pos) {
        if (!hasSample())
            return;
        if (!setZoomAnchor(id)) {
            // loop marker not close enough to select, and first pointer down. Start scrolling
            setScrollAnchor(id, pos);
        }
    }

    @Override
    public synchronized void handleActionPointerUp(int id, Pointer pos) {
        if (!hasSample())
            return;
        // stop zooming
        int scrollPointerId = id == zoomLeftPointerId ? zoomRightPointerId : zoomLeftPointerId;
        setScrollAnchor(scrollPointerId, pointerById.get(scrollPointerId));
        zoomLeftPointerId = zoomRightPointerId = -1;
    }

    @Override
    public synchronized void handleActionMove(int id, Pointer pos) {
        if (!hasSample()) {
            checkPointerExit(id, pos);
            return;
        }
        if (id == zoomLeftPointerId) {
            // no-op: only zoom once both actions have been handled
        } else if (id == zoomRightPointerId) {
            updateZoom();
        } else if (!moveLoopMarker(id, pos)) {
            scroll(pos);
        }
    }

    @Override
    protected float xToLevel(float x) {
        return (x - loopButtonW / 2) * checkedDivide(levelWidth, waveformWidth) + levelOffset;
    }

    @Override
    protected float yToLevel(float y) {
        return 0;
    }

    protected float levelToX(float level) {
        return loopButtonW / 2 + checkedDivide((level - levelOffset) * waveformWidth, levelWidth);
    }

    @Override
    public synchronized void onParamChange(Param param) {
        final Track track = (Track) context.getTrackManager().getCurrTrack();
        if (param == null)
            return;
        if (param.equals(track.getGainParam())) {
            waveformShape.resetIndices();
            waveformShape.updateWaveformVertices();
        } else if (param.equals(track.getLoopBeginParam()) || param.equals(track.getLoopEndParam())) {
            updateWaveformVb();
        }
    }

    private float checkedDivide(float num, float den) {
        return den == 0 ? 0 : num / den;
    }

    // if the params are null, then there is no sample file for this track.
    private boolean hasSample() {
        return params[0] != null && params[1] != null;
    }

    @Override
    protected void dragRelease() {
        setState(State.DEFAULT);
    }

    @Override
    public void release() {
        if (!hasSample() && isPressed()) {
            context.getPageSelectGroup().selectBrowsePage();
        }
        super.release();
    }
}
