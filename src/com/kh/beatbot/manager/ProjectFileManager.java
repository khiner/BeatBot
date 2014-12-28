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
	private static final String PROJECT_FILE_EXTENSION = ".bb";
	private static final String WORKING_PROJECT_FILE_NAME = ".working_project";

	private static String projectFileName, pendingFileName;
	private static AlertDialog confirmLoadAlert, fileExistsAlert;

	private static ProjectFile eventTrackerFile;

	public static void init(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(
				"A project with this name already exists. Would you like to overwrite it?")
				.setCancelable(false)
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

		eventTrackerFile = new ProjectFile(FileManager.projectDirectory.getAbsolutePath() + "/"
				+ WORKING_PROJECT_FILE_NAME);
		EventManager.addListener(eventTrackerFile);
		
		projectFileName = "temp_project";
	}

	public static String getProjectName() {
		return projectFileName;
	}

	public static void saveProject(String fileName) {
		pendingFileName = fileName;
		if (!new File(getFullPathName(fileName)).exists()) {
			completeSave();
		} else {
			// file exists - popup dialog confirming overwrite of existing file
			fileExistsAlert.show();
		}
	}

	public static void loadProject(Context context, String fileName) {
		pendingFileName = fileName;
		if (!TrackManager.anyNotes()) {
			completeLoad(context);
		} else {
			confirmLoadAlert.show();
		}
	}

	public static boolean isProjectFileName(String fileName) {
		return fileName.toLowerCase().endsWith(PROJECT_FILE_EXTENSION);
	}

	private static void completeSave() {
		projectFileName = pendingFileName;
		try {
			eventTrackerFile.writeToFile(new File(getFullPathName(projectFileName)));
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private static void completeLoad(Context context) {
		projectFileName = pendingFileName;
		try {
			MidiNotesEventManager.destroyNotes(MidiManager.allNotes()); // TODO fresh state
			new ProjectFile(new FileInputStream(getFullPathName(projectFileName)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Toast.makeText(context, getFullPathName(projectFileName), Toast.LENGTH_SHORT).show();
	}

	private static String getFullPathName(String fileName) {
		if (!isProjectFileName(fileName)) {
			fileName = fileName.concat(PROJECT_FILE_EXTENSION);
		}

		return FileManager.projectDirectory.getPath() + "/" + fileName;
	}
}
