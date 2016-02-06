package com.kh.beatbot.manager;

import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.file.ProjectFile;

public class ProjectFileManager {
	private static final String PROJECT_FILE_EXTENSION = ".bb";

	private FileManager fileManager;
	private String projectFileName, pendingFileName;
	private AlertDialog confirmLoadAlert, fileExistsAlert;

	public ProjectFileManager(final BeatBotActivity context, final FileManager fileManager) {
		this.fileManager = fileManager;
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

		projectFileName = "temp_project";
	}

	public String getProjectName() {
		return projectFileName;
	}

	public void saveProject(String fileName) {
		pendingFileName = fileName;
		if (!new File(getFullPathName(fileName)).exists()) {
			completeSave();
		} else {
			// file exists - popup dialog confirming overwrite of existing file
			fileExistsAlert.show();
		}
	}

	public void loadProject(BeatBotActivity context, String fileName) {
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

	private void completeSave() {
		projectFileName = pendingFileName;
		try {
			new ProjectFile(getFullPathName(projectFileName)).save();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private void completeLoad(BeatBotActivity context) {
		projectFileName = pendingFileName;
		try {
			context.clearProject();
			new ProjectFile(getFullPathName(projectFileName)).load();
		} catch (IOException e) {
			System.err.println(e);
		}

		Toast.makeText(context, getFullPathName(projectFileName), Toast.LENGTH_SHORT).show();
	}

	private String getFullPathName(String fileName) {
		if (!isProjectFileName(fileName)) {
			fileName = fileName.concat(PROJECT_FILE_EXTENSION);
		}

		return fileManager.getProjectDirectory().getPath() + "/" + fileName;
	}
}
