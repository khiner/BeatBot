package com.odang.beatbot.manager;

import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.odang.beatbot.activity.BeatBotActivity;
import com.odang.beatbot.file.ProjectFile;
import com.odang.beatbot.ui.view.View;

public class ProjectFileManager {
	private static final String PROJECT_FILE_EXTENSION = ".bb";
	private static final String RECOVER_PROJECT_NAME = "recover_project";

	private String projectFileName, pendingFileName;
	private AlertDialog confirmLoadAlert, fileExistsAlert;

	public ProjectFileManager(final BeatBotActivity context) {
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

	public void saveProject(final String fileName, final boolean force) {
		pendingFileName = fileName;
		if (force || !new File(getFullPathName(fileName)).exists()) {
			completeSave();
		} else {
			// file exists - popup dialog confirming overwrite of existing file
			fileExistsAlert.show();
		}
	}

	public void importProject(final BeatBotActivity context, final String fileName) {
		pendingFileName = fileName;
		if (!context.getTrackManager().anyNotes()) {
			completeLoad(context);
		} else {
			confirmLoadAlert.show();
		}
	}

	public void saveRecoverProject() {
		saveProject(RECOVER_PROJECT_NAME, true);
	}

	public boolean importRecoverProject(final BeatBotActivity context) {
		if (!new File(getFullPathName(RECOVER_PROJECT_NAME)).exists())
			return false;
		pendingFileName = RECOVER_PROJECT_NAME;
		completeLoad(context);
		return true;
	}

	public void deleteRecoverProject() {
		final File recoverProjectFile = new File(getFullPathName(RECOVER_PROJECT_NAME));
		if (recoverProjectFile.exists()) {
			recoverProjectFile.delete();
		}
	}
	
	public static boolean isProjectFileName(final String fileName) {
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

	private void completeLoad(final BeatBotActivity context) {
		projectFileName = pendingFileName;
		try {
			context.clearProject();
			new ProjectFile(getFullPathName(projectFileName)).load();
		} catch (IOException e) {
			System.err.println(e);
		}

		Toast.makeText(context, getFullPathName(projectFileName), Toast.LENGTH_SHORT).show();
	}

	private String getFullPathName(final String fileName) {
		final String projectDirectoryPath = View.context.getFileManager().getProjectDirectory()
				.getPath();
		if (!isProjectFileName(fileName)) {
			return projectDirectoryPath + "/" + fileName + PROJECT_FILE_EXTENSION;
		} else {
			return projectDirectoryPath + "/" + fileName;
		}
	}
}
