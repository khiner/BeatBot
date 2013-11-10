package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	private static File outFile;
	private static FileInputStream inFile;
	private static AlertDialog fileNotExistsAlert, chooseFileAlert,
			confirmImportAlert, fileExistsAlert;

	public static void init() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				BeatBotActivity.mainActivity);
		builder.setMessage("The file exists. Would you like to overwrite it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								writeToFile(outFile);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		fileExistsAlert = builder.create();

		builder = new AlertDialog.Builder(BeatBotActivity.mainActivity);
		builder.setMessage("Sorry, but the chosen file does not exist.")
				.setCancelable(false)
				.setNeutralButton("Okay",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		fileNotExistsAlert = builder.create();

		final String[] fileNames = DirectoryManager.midiDirectory.list();
		
		builder = new AlertDialog.Builder(BeatBotActivity.mainActivity);
		builder.setTitle("Choose MIDI File");
		builder.setItems(fileNames, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Toast.makeText(
						BeatBotActivity.mainActivity.getApplicationContext(),
						fileNames[item], Toast.LENGTH_SHORT).show();
				importMidiFile(fileNames[item]);
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
								MidiManager.importFromFile(inFile);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		confirmImportAlert = builder.create();
	}

	public static void exportMidi(String midiFileName) {
		String fullPathName = getFullPathName(midiFileName);
		outFile = new File(fullPathName);
		if (!outFile.exists()) {
			writeToFile(outFile);
		} else {
			// file exists - popup dialog confirming overwrite of existing file
			fileExistsAlert.show();
		}
	}

	public static void chooseMidiFile() {
		chooseFileAlert.show();
	}

	private static void importMidiFile(String fileName) {
		String fullPath = DirectoryManager.midiDirectory.getPath() + "/" + fileName;
		try {
			inFile = new FileInputStream(fullPath);
			if (MidiManager.getMidiNotes().isEmpty()) {
				MidiManager.importFromFile(inFile);
			} else {
				confirmImportAlert.show();
			}
		} catch (FileNotFoundException e) {
			fileNotExistsAlert.setMessage("Sorry, but the file \"" + fullPath
					+ "\" does not exist.");
			fileNotExistsAlert.show();
		}
	}

	private static String getFullPathName(String fileName) {
		return DirectoryManager.midiDirectory.getPath() + "/" + fileName + ".MIDI";
	}
	
	private static void writeToFile(File outFile) {
		// 3. Create a MidiFile with the tracks we created
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

		// 4. Write the MIDI data to a file
		try {
			midi.writeToFile(outFile);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}
