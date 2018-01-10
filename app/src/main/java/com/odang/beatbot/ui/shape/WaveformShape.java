package com.odang.beatbot.ui.shape;

import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.view.View;

public class WaveformShape extends Shape {
    private final static float MAX_SPP = 1, BUFFER_RATIO = 4;
    private float offsetInFrames, widthInFrames;
    private float xOffset, numSamples, loopBeginX, loopEndX;
    private float[] sampleBuffer;

    public WaveformShape(RenderGroup group, float width, float[] fillColor, float[] strokeColor) {
        super(group, fillColor, strokeColor, Rectangle.FILL_INDICES, getStrokeIndices(width),
                Rectangle.NUM_FILL_VERTICES, (int) (width * MAX_SPP * BUFFER_RATIO));
        this.width = width;
        this.sampleBuffer = new float[2 * (int) (width * MAX_SPP * BUFFER_RATIO)];
    }

    private static short[] getStrokeIndices(float width) {
        short[] strokeIndices = new short[(int) (width * MAX_SPP * BUFFER_RATIO) * 2 + 2];
        // degenerate line begin
        strokeIndices[0] = 0;
        strokeIndices[1] = 0;
        for (int i = 1; i < (strokeIndices.length - 2) / 2; i++) {
            strokeIndices[i * 2] = (short) (i - 1);
            strokeIndices[i * 2 + 1] = (short) i;
        }

        // degenerate line end
        strokeIndices[strokeIndices.length - 2] = strokeIndices[strokeIndices.length - 3];
        strokeIndices[strokeIndices.length - 1] = strokeIndices[strokeIndices.length - 3];
        return strokeIndices;
    }

    /*
     * Read samples from disk at the current granularity
     */
    public void resample() {
        float numBufferFrames = widthInFrames * BUFFER_RATIO / 2;
        int startFrame = (int) Math.floor(offsetInFrames - numBufferFrames);
        int endFrame = (int) Math.ceil(offsetInFrames + numBufferFrames);
        int stepFrames = (int) Math.ceil(widthInFrames / numSamples);

        Track track = (Track) View.context.getTrackManager().getCurrTrack();
        track.fillSampleBuffer(sampleBuffer, startFrame, endFrame, stepFrames);
        resetIndices();
        updateWaveformVertices();
    }

    protected void updateVertices() {
        updateLoopSelectionVertices();
        updateWaveformVertices();
    }

    public void update(float loopBeginX, float loopEndX, float offsetInFrames, float widthInFrames,
                       float xOffset) {
        boolean loopSelectionChanged = this.loopBeginX != loopBeginX || this.loopEndX != loopEndX;
        this.loopBeginX = loopBeginX;
        this.loopEndX = loopEndX;

        final Track track = (Track) View.context.getTrackManager().getCurrTrack();
        float newWidthInFrames = Math.min(widthInFrames, track.getNumFrames());
        boolean waveformChanged = this.offsetInFrames != offsetInFrames
                || this.widthInFrames != newWidthInFrames || this.xOffset != xOffset;
        this.offsetInFrames = offsetInFrames;
        this.widthInFrames = newWidthInFrames;
        this.xOffset = xOffset;

        if (waveformChanged) {
            float spp = Math.min(MAX_SPP, widthInFrames / width);
            numSamples = (int) (width * spp);
        }
        if (loopSelectionChanged || waveformChanged)
            resetIndices();
        if (loopSelectionChanged)
            updateLoopSelectionVertices();
        if (waveformChanged)
            updateWaveformVertices();
    }

    private void updateWaveformVertices() {
        if (null == sampleBuffer)
            return;

        float x = 0, y = 0;

        for (int i = 0; i < sampleBuffer.length / 2; i++) {
            int sampleIndex = (int) sampleBuffer[i * 2];
            if (sampleIndex == -1)
                break;
            float sample = sampleBuffer[i * 2 + 1];
            float percent = (sampleIndex - offsetInFrames) / widthInFrames;
            x = this.x + percent * width + xOffset;
            y = this.y + height * (1 - sample) / 2;
            if (i == 0) {
                strokeVertex(x, y); // starting degenerate line
            }
            strokeVertex(x, y);
        }
        strokeVertex(x, y); // terminating degenerate line

        while (!strokeMesh.isFull())
            strokeVertex(x, y);
    }

    private void updateLoopSelectionVertices() {
        fillVertex(x + loopBeginX, y);
        fillVertex(x + loopBeginX, y + height);
        fillVertex(x + loopEndX, y + height);
        fillVertex(x + loopEndX, y);
    }
}