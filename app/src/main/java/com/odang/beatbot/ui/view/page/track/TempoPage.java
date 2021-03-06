package com.odang.beatbot.ui.view.page.track;

import com.odang.beatbot.event.Stateful;
import com.odang.beatbot.event.TempoChangeEvent;
import com.odang.beatbot.listener.OnReleaseListener;
import com.odang.beatbot.manager.MidiManager;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.BpmView;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.Button;

public class TempoPage extends TrackPage {
    private Button tapTempoButton;
    private BpmView bpmView;
    private long lastTapTime = 0;
    private long lastTempoSetEventMillis = 0;
    private final int ONE_MINUTE_MILLIS = 60000;
    // group any tempo-tap set events in this time period together
    private final long EVENT_CONSOLIDATION_MILLIS = 1500;

    public TempoPage(View view) {
        super(view);
    }

    @Override
    protected void createChildren() {
        tapTempoButton = new Button(this).withRoundedRect().withIcon(
                IconResourceSets.TAP_TEMPO_BUTTON);
        bpmView = new BpmView(this);

        tapTempoButton.setText("TAP");

        tapTempoButton.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                final long tapTime = System.currentTimeMillis();
                final float bpm = ONE_MINUTE_MILLIS / (tapTime - lastTapTime);
                // if we are far outside of the range, don't change the tempo.
                // otherwise, midiManager will take care of clipping the result
                if (bpm >= MidiManager.MIN_BPM - 20 && bpm <= MidiManager.MAX_BPM + 20) {
                    final Stateful latestEvent = context.getEventManager().getLastEvent();
                    if (latestEvent instanceof TempoChangeEvent
                            && tapTime - lastTempoSetEventMillis < EVENT_CONSOLIDATION_MILLIS) {
                        // group close tempo-tap events together on the undo-stack
                        final TempoChangeEvent tempoChangeEvent = (TempoChangeEvent) latestEvent;
                        tempoChangeEvent.setEndBpm(bpm);
                        tempoChangeEvent.doExecute();
                    } else {
                        new TempoChangeEvent(bpm).execute();
                    }
                    lastTempoSetEventMillis = tapTime;
                }
                lastTapTime = tapTime;
            }
        });
    }

    @Override
    public void layoutChildren() {
        float bpmHeight = 3 * height / 4;
        float bpmWidth = bpmHeight * 2;
        float tapDim = 11 * height / 16;
        float allChildrenWidth = tapDim + bpmWidth + BG_OFFSET * 2;
        tapTempoButton.layout(this, (width - allChildrenWidth) / 2, (height - tapDim) / 2, tapDim,
                tapDim);
        bpmView.layout(this, (width - allChildrenWidth) / 2 + tapDim + BG_OFFSET, height / 8,
                bpmWidth, bpmHeight);
    }
}
