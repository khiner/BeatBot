package com.kh.beatbot.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.R;
import com.kh.beatbot.manager.MidiManager;

public class MidiFileMenuActivity extends Activity {
	private static final String SAVE_FOLDER = "BeatBot/MIDI";
	private String baseFilePath = null;
	File outFile;
	FileInputStream inFile;
	String[] fileNames;
	AlertDialog fileExistsAlert;
	AlertDialog fileNotExistsAlert;
	AlertDialog chooseFileAlert;
	AlertDialog confirmImportAlert;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GeneralUtils.initAndroidSettings(this);
		setContentView(R.layout.midi_menu);

		baseFilePath = createBasePath();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("The file exists. Would you like to overwrite it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								MidiManager.writeToFile(outFile);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		fileExistsAlert = builder.create();

		builder = new AlertDialog.Builder(this);
		builder.setMessage("Sorry, but the chosen file does not exist.")
				.setCancelable(false)
				.setNeutralButton("Okay",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		fileNotExistsAlert = builder.create();

		File[] files = new File(baseFilePath).listFiles();
		fileNames = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			fileNames[i] = files[i].getName();
		}

		builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose MIDI File");
		builder.setItems(fileNames, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Toast.makeText(getApplicationContext(), fileNames[item],
						Toast.LENGTH_SHORT).show();
				importMidiFile(fileNames[item]);
			}
		});
		chooseFileAlert = builder.create();

		builder = new AlertDialog.Builder(this);
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void saveMidi(View view) {
		EditText editText = (EditText) findViewById(R.id.filePathEdit);
		String fullPathName = getFullPathName(editText.getText().toString());
		outFile = new File(fullPathName);
		if (!outFile.exists())
			MidiManager.writeToFile(outFile);
		else {
			// file exists - popup dialog confirming overwrite of existing file
			fileExistsAlert.show();
		}
	}

	public void openMidi(View view) {
		chooseFileAlert.show();
	}

	private void importMidiFile(String fileName) {
		String fullPath = baseFilePath + "/" + fileName;
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

	private String createBasePath() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, SAVE_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		return file.getAbsolutePath();
	}

	private String getFullPathName(String fileName) {
		return baseFilePath + "/" + fileName + ".MIDI";
	}
}
