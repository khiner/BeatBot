package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.midi.MidiFile;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.midi.MidiTrack;

public class MidiFileManager {

	private static String inFileName, outFileName;

	private static AlertDialog chooseFileAlert, confirmImportAlert,
			fileExistsAlert;

	public static void init() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				BeatBotActivity.mainActivity);
		builder.setMessage("The file exists. Would you like to overwrite it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								completeExport();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		fileExistsAlert = builder.create();

		final String[] fileNames = DirectoryManager.midiDirectory.list();

		builder = new AlertDialog.Builder(BeatBotActivity.mainActivity);
		builder.setTitle("Choose MIDI File");
		builder.setItems(fileNames, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				importMidi(fileNames[item]);
			}
		});
		chooseFileAlert = builder.create();

		builder = new AlertDialog.Builder(BeatBotActivity.mainActivity);
		builder.setMessage(
				"Are you sure you want to import this MIDI file? "
						+ "Your current project will be lost.")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								completeImport();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		confirmImportAlert = builder.create();
	}

	public static void chooseMidiFile() {
		chooseFileAlert.show();
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

	private static void importMidi(String fileName) {
		inFileName = fileName;
		if (MidiManager.getMidiNotes().isEmpty()) {
			completeImport();
		} else {
			confirmImportAlert.show();
		}
	}

	private static void completeExport() {
		// Create a MidiFile with the tracks we created
		ArrayList<MidiTrack> midiTracks = new ArrayList<MidiTrack>();
		midiTracks.add(MidiManager.getTempoTrack());
		midiTracks.add(new MidiTrack());
		for (MidiNote midiNote : MidiManager.getMidiNotes()) {
			midiTracks.get(1).insertEvent(midiNote.getOnEvent());
			midiTracks.get(1).insertEvent(midiNote.getOffEvent());
		}
		Collections.sort(midiTracks.get(1).getEvents());
		midiTracks.get(1).recalculateDeltas();

		MidiFile midi = new MidiFile(MidiManager.RESOLUTION, midiTracks);

		// Write the MIDI data to a file
		try {
			midi.writeToFile(new File(getFullPathName(outFileName)));
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private static void completeImport() {
		try {
			MidiFile midiFile = new MidiFile(new FileInputStream(
					getFullPathName(inFileName)));
			MidiManager.importFromFile(midiFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Toast.makeText(BeatBotActivity.mainActivity.getApplicationContext(),
				inFileName, Toast.LENGTH_SHORT).show();
	}

	private static String getFullPathName(String fileName) {
		String fullPathName = DirectoryManager.midiDirectory.getPath() + "/"
				+ fileName;
		if (!fileName.toLowerCase().endsWith(".midi")) {
			fullPathName = fullPathName.concat(".MIDI");
		}

		return fullPathName;
	}
}
