package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.kh.beatbot.file.MidiFile;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.midi.MidiTrack;

public class MidiFileManager {
	private static final String MIDI_FILE_EXTENSION = ".midi";
	private static String inFileName, outFileName;
	private static AlertDialog confirmImportAlert, fileExistsAlert;

	public static void init(final Context context) {
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

	public static void exportMidi(String fileName) {
		outFileName = fileName;
		if (!new File(getFullPathName(fileName)).exists()) {
			completeExport();
		} else {
			// file exists - popup dialog confirming overwrite of existing file
			fileExistsAlert.show();
		}
	}

	public static void importMidi(Context context, String fileName) {
		inFileName = fileName;
		if (!TrackManager.anyNotes()) {
			completeImport(context);
		} else {
			confirmImportAlert.show();
		}
	}

	public static boolean isMidiFileName(String fileName) {
		return fileName.toLowerCase().endsWith(MIDI_FILE_EXTENSION);
	}

	private static void completeExport() {
		// Create a MidiFile with the tracks we created
		ArrayList<MidiTrack> midiTracks = new ArrayList<MidiTrack>();
		midiTracks.add(MidiManager.getTempoTrack());
		midiTracks.add(new MidiTrack());
		for (MidiNote midiNote : MidiManager.allNotes()) {
			midiTracks.get(1).insertEvent(midiNote.getOnEvent());
			midiTracks.get(1).insertEvent(midiNote.getOffEvent());
		}
		Collections.sort(midiTracks.get(1).getEvents());
		midiTracks.get(1).recalculateDeltas();

		MidiFile midi = new MidiFile(MidiManager.TICKS_PER_NOTE, midiTracks);

		// Write the MIDI data to a file
		try {
			midi.writeToFile(new File(getFullPathName(outFileName)));
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private static void completeImport(Context context) {
		try {
			MidiFile midiFile = new MidiFile(new FileInputStream(getFullPathName(inFileName)));
			MidiManager.importFromFile(midiFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Toast.makeText(context, inFileName, Toast.LENGTH_SHORT).show();
	}

	private static String getFullPathName(String fileName) {
		if (!isMidiFileName(fileName)) {
			fileName = fileName.concat(MIDI_FILE_EXTENSION);
		}

		return FileManager.midiDirectory.getPath() + "/" + fileName;
	}
}
