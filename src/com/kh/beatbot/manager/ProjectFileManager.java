package com.kh.beatbot.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.kh.beatbot.file.ProjectFile;

public class ProjectFileManager {
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
				"Are you sure you want to import this project file? "
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

	public static void exportProject(String fileName) {
		outFileName = fileName;
		if (!new File(getFullPathName(fileName)).exists()) {
			completeExport();
		} else {
			// file exists - popup dialog confirming overwrite of existing file
			fileExistsAlert.show();
		}
	}

	public static void importProject(Context context, String fileName) {
		inFileName = fileName;
		if (!TrackManager.anyNotes()) {
			completeImport(context);
		} else {
			confirmImportAlert.show();
		}
	}

	private static void completeExport() {
		// Create a project file with all events serialized
		ProjectFile project = new ProjectFile();
		try {
			project.writeToFile(new File(getFullPathName(outFileName)));
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private static void completeImport(Context context) {
		try {
			ProjectFile project = new ProjectFile(new FileInputStream(getFullPathName(inFileName)));
			// TODO
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
