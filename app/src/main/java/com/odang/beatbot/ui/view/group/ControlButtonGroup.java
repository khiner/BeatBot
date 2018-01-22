package com.odang.beatbot.ui.view.group;

import android.widget.Toast;

import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.effect.Effect.LevelType;
import com.odang.beatbot.event.Stateful;
import com.odang.beatbot.event.effect.EffectCreateEvent;
import com.odang.beatbot.event.effect.EffectToggleEvent;
import com.odang.beatbot.listener.MidiNoteListener;
import com.odang.beatbot.listener.OnReleaseListener;
import com.odang.beatbot.listener.StatefulEventListener;
import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.TouchableView;
import com.odang.beatbot.ui.view.View;
import com.odang.beatbot.ui.view.control.Button;
import com.odang.beatbot.ui.view.control.ToggleButton;

public class ControlButtonGroup extends TouchableView implements MidiNoteListener,
        StatefulEventListener {

    private ToggleButton playButton, copyButton, effectToggle;
    private Button stopButton, undoButton, redoButton, deleteButton, quantizeButton;

    public ControlButtonGroup(View view) {
        super(view);
        context.getEventManager().addListener(this);
    }

    @Override
    protected void createChildren() {
        playButton = new ToggleButton(this).withIcon(IconResourceSets.PLAY);
        stopButton = new Button(this).withIcon(IconResourceSets.STOP);
        copyButton = new ToggleButton(this).withIcon(IconResourceSets.COPY);
        deleteButton = new Button(this).withIcon(IconResourceSets.DELETE_NOTE);
        quantizeButton = new Button(this).withIcon(IconResourceSets.QUANTIZE);
        undoButton = new Button(this).withIcon(IconResourceSets.UNDO);
        redoButton = new Button(this).withIcon(IconResourceSets.REDO);

        playButton.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                context.getPlaybackManager().play();
            }
        });

        stopButton.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                playButton.setChecked(false);
                if (context.getPlaybackManager().isPlaying()) {
                    context.getPlaybackManager().stop();
                } else {
                    context.getPlaybackManager().resetTicker();
                }
            }
        });

        undoButton.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                context.getEventManager().undo();
            }
        });

        redoButton.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                context.getEventManager().redo();
            }
        });

        copyButton.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                String msg = null;
                if (((ToggleButton) button).isChecked()) {
                    context.getMidiManager().copy();
                    msg = "Tap To Paste";
                } else {
                    context.getMidiManager().cancelCopy();
                    msg = "Copy Cancelled";
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                context.getMidiManager().deleteSelectedNotes();
            }
        });

        quantizeButton.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                context.getMidiManager().quantize(); // TODO bring back quantize list (1/4,
                // 1/8, 1/16, 1/32)
            }
        });

        effectToggle = new ToggleButton(this).oscillating().withRoundedRect()
                .withIcon(IconResourceSets.TOGGLE);
        effectToggle.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onRelease(Button button) {
                int trackId = context.getTrackManager().getCurrTrack().getId();
                int effectPosition = context.getMainPage().getCurrEffect().getPosition();
                new EffectToggleEvent(trackId, effectPosition).execute();
            }
        });

        setEditIconsEnabled(false);
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        quantizeButton.setEnabled(false);
    }

    @Override
    public void layoutChildren() {
        // left-aligned buttons
        playButton.layout(this, 0, 0, height, height);
        stopButton.layout(this, height, 0, height, height);

        float rightMargin = BG_OFFSET * 2;
        // right-aligned buttons
        effectToggle.layout(this, width - 9 * height - rightMargin, 0, height * 4, height);
        quantizeButton.layout(this, width - 5 * height - rightMargin, 0, height, height);
        copyButton.layout(this, width - 4 * height - rightMargin, 0, height, height);
        undoButton.layout(this, width - 3 * height - rightMargin, 0, height, height);
        redoButton.layout(this, width - 2 * height - rightMargin, 0, height, height);
        deleteButton.layout(this, width - height - rightMargin, 0, height, height);
    }

    @Override
    public void onCreate(MidiNote note) {
        quantizeButton.setEnabled(true);
    }

    @Override
    public void onDestroy(MidiNote note) {
        quantizeButton.setEnabled(context.getTrackManager().anyNotes());
        onSelectStateChange(note);
    }

    @Override
    public void onMove(MidiNote note, int beginNoteValue, long beginOnTick, long beginOffTick,
                       int endNoteValue, long endOnTick, long endOffTick) {
        // no-op
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
    public void onSelectStateChange(MidiNote note) {
        setEditIconsEnabled(context.getTrackManager().anyNoteSelected());
    }

    public void updateEffectToggle(Effect effect) {
        effectToggle.show();
        effectToggle.enable();
        effectToggle.setChecked(effect.isOn());
        effectToggle.setText(effect.getName());
    }

    public void hideEffectToggle() {
        effectToggle.disable();
        effectToggle.hide();
    }

    private void setEditIconsEnabled(final boolean enabled) {
        deleteButton.setEnabled(enabled);
        copyButton.setEnabled(enabled);
    }

    private void updateStateStackIcons(boolean hasUndo, boolean hasRedo) {
        undoButton.setEnabled(hasUndo);
        redoButton.setEnabled(hasRedo);
    }

    @Override
    public void onEventCompleted(Stateful event) {
        updateStateStackIcons(context.getEventManager().canUndo(), context.getEventManager()
                .canRedo());
        if (event instanceof EffectCreateEvent || event instanceof EffectToggleEvent) {
            final Effect effect = context.getMainPage().getCurrEffect();
            effectToggle.setChecked(effect.isOn());
        }
    }
}
