package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.midinotes.MidiNotesEventManager;
import com.kh.beatbot.file.ProjectFile;

public class ProjectFileManager {
	private static String inFileName, outFileName;
	private static AlertDialog confirmLoadAlert, fileExistsAlert;

	private static ProjectFile eventTrackerFile;

	public static void init(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("A project with this name already exists. Would you like to overwrite it?").setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						completeSave();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		fileExistsAlert = builder.create();

		builder = new AlertDialog.Builder(context);
		builder.setMessage(
				"Are you sure you want to load this project file? "
						+ "Your current project will be lost.").setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						completeLoad(context);
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		confirmLoadAlert = builder.create();
		
		eventTrackerFile = new ProjectFile(FileManager.projectDirectory.getAbsolutePath() + "/project");
		EventManager.addListener(eventTrackerFile);
	}

	public static void saveProject(String fileName) {
		outFileName = fileName;
		if (!new File(getFullPathName(fileName)).exists()) {
			completeSave();
		} else {
			// file exists - popup dialog confirming overwrite of existing file
			fileExistsAlert.show();
		}
	}

	public static void loadProject(Context context, String fileName) {
		inFileName = fileName;
		if (!TrackManager.anyNotes()) {
			completeLoad(context);
		} else {
			confirmLoadAlert.show();
		}
	}

	private static void completeSave() {
		try {
			eventTrackerFile.writeToFile(new File(getFullPathName(outFileName)));
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private static void completeLoad(Context context) {
		try {
			MidiNotesEventManager.destroyNotes(MidiManager.allNotes()); // TODO fresh state
			new ProjectFile(new FileInputStream(getFullPathName(inFileName)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Toast.makeText(context, inFileName, Toast.LENGTH_SHORT).show();
	}

	private static String getFullPathName(String fileName) {
		String fullPathName = FileManager.projectDirectory.getPath() + "/" + fileName;
		if (!fileName.toLowerCase().endsWith(".bb")) {
			fullPathName = fullPathName.concat(".bb");
		}

		return fullPathName;
	}
}
