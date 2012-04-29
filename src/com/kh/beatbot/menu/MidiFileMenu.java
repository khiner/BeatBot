package com.kh.beatbot.menu;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;

import com.KarlHiner.BeatBox.R;
import com.kh.beatbot.MidiManager;

public class MidiFileMenu extends Activity {
	private static final String SAVE_FOLDER = "BeatBot/MIDI";
	private String baseFilePath = null;
	MidiManager midiManager;
	File outFile;
	AlertDialog fileExistsAlert;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.midi_menu);
		Intent intent = getIntent();
		midiManager = (MidiManager) intent.getParcelableExtra("midiManager");
		baseFilePath = createBasePath();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("The file exists. Would you like to overwrite it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								midiManager.writeToFile(outFile);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		fileExistsAlert = builder.create();		
	}

	public void saveMidi(View view) {
		EditText editText = (EditText) findViewById(R.id.filePathEdit);
		String fullPathName = getFullPathName(editText.getText().toString());
		outFile = new File(fullPathName);
		if (!outFile.exists())
			midiManager.writeToFile(outFile);
		else {
			// file exists - popup dialog for overwriting the existing file
			fileExistsAlert.show();
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
