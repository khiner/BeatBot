package com.odang.beatbot.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.odang.beatbot.file.MidiFile;
import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.midi.MidiTrack;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MidiFileManager {
    private static final String MIDI_FILE_EXTENSION = ".midi";

    private String inFileName, outFileName;
    private AlertDialog confirmImportAlert, fileExistsAlert;

    public MidiFileManager(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("The file exists. Would you like to overwrite it?").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        completeExport();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        fileExistsAlert = builder.create();

        builder = new AlertDialog.Builder(context);
        builder.setMessage(
                "Are you sure you want to import this MIDI file? "
                        + "Your current project will be lost.").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        completeImport(context);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        confirmImportAlert = builder.create();
    }

    public void exportMidi(String fileName) {
        outFileName = fileName;
        if (!new File(getFullPathName(fileName)).exists()) {
            completeExport();
        } else {
            // file exists - popup dialog confirming overwrite of existing file
            fileExistsAlert.show();
        }
    }

    public void importMidi(Context context, String fileName) {
        inFileName = fileName;
        if (!View.context.getTrackManager().anyNotes()) {
            completeImport(context);
        } else {
            confirmImportAlert.show();
        }
    }

    public static boolean isMidiFileName(String fileName) {
        return fileName.toLowerCase().endsWith(MIDI_FILE_EXTENSION);
    }

    private void completeExport() {
        // Create a MidiFile with the tracks we created
        final List<MidiTrack> midiTracks = new ArrayList<MidiTrack>();
        midiTracks.add(View.context.getMidiManager().getTempoTrack());
        midiTracks.add(new MidiTrack());
        for (Track track : View.context.getTrackManager().getTracks()) {
            for (MidiNote midiNote : track.getMidiNotes()) {
                midiTracks.get(1).insertEvent(midiNote.getOnEvent());
                midiTracks.get(1).insertEvent(midiNote.getOffEvent());
            }
        }
        Collections.sort(midiTracks.get(1).getEvents());
        midiTracks.get(1).recalculateDeltas();

        final MidiFile midi = new MidiFile(MidiManager.TICKS_PER_NOTE, midiTracks);

        // Write the MIDI data to a file
        try {
            midi.writeToFile(new File(getFullPathName(outFileName)));
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void completeImport(final Context context) {
        try {
            final MidiFile midiFile = new MidiFile(new FileInputStream(getFullPathName(inFileName)));
            View.context.getMidiManager().importFromFile(midiFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(context, inFileName, Toast.LENGTH_SHORT).show();
    }

    private String getFullPathName(String fileName) {
        if (!isMidiFileName(fileName)) {
            fileName = fileName.concat(MIDI_FILE_EXTENSION);
        }

        return View.context.getFileManager().getMidiDirectory().getPath() + "/" + fileName;
    }
}
