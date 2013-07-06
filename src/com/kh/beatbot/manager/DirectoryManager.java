package com.kh.beatbot.manager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.database.DataSetObserver;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kh.beatbot.Directory;
import com.kh.beatbot.GlobalVars;
import com.kh.beatbot.Instrument;
import com.kh.beatbot.ui.IconResources;

public class DirectoryManager {

	private class SampleListOnClickAndShowListener implements
			DialogInterface.OnClickListener, OnShowListener {
		@Override
		public void onClick(DialogInterface dialog, int item) {
			Directory parent = currDirectory;
			currDirectory = currDirectory.getChild(item);
			if (currDirectory == null) {
				// Instrument type
				if (addingTrack) {
					TrackManager.createTrack((Instrument) parent, item);
				} else {
					TrackManager.currTrack.setInstrument((Instrument) parent,
							item);
				}
				currDirectory = audioDirectory;
			}
			updateInstrumentSelectAlert(currDirectory);
			// if the current dir is the root directory, we've
			// already selected a sample
			// do not display any select alert list.
			if (currDirectory == audioDirectory) {
				return;
			}
			// if the directory is empty, display the directory's
			// empty message. otherwise, show the next child
			// directories in a select alert list
			if (currDirectory.getChildNames().length == 0) {
				Toast.makeText(GlobalVars.mainActivity,
						currDirectory.getEmptyMsg(), Toast.LENGTH_SHORT).show();
			} else {
				instrumentSelectAlert.show();
			}
		}

		@Override
		public void onShow(DialogInterface alert) {
			ListView listView = ((AlertDialog) alert).getListView();
			final ListAdapter originalAdapter = listView.getAdapter();

			listView.setAdapter(new ListAdapter() {
				@Override
				public int getCount() {
					return originalAdapter.getCount();
				}

				@Override
				public Object getItem(int id) {
					return originalAdapter.getItem(id);
				}

				@Override
				public long getItemId(int id) {
					return originalAdapter.getItemId(id);
				}

				@Override
				public int getItemViewType(int id) {
					return originalAdapter.getItemViewType(id);
				}

				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View view = originalAdapter.getView(position, convertView,
							parent);
					TextView textView = (TextView) view;
					textView.setTypeface(GlobalVars.font);
					textView.setText(textView.getText().toString()
							.toUpperCase());
					return view;
				}

				@Override
				public int getViewTypeCount() {
					return originalAdapter.getViewTypeCount();
				}

				@Override
				public boolean hasStableIds() {
					return originalAdapter.hasStableIds();
				}

				@Override
				public boolean isEmpty() {
					return originalAdapter.isEmpty();
				}

				@Override
				public void registerDataSetObserver(DataSetObserver observer) {
					originalAdapter.registerDataSetObserver(observer);

				}

				@Override
				public void unregisterDataSetObserver(DataSetObserver observer) {
					originalAdapter.unregisterDataSetObserver(observer);

				}

				@Override
				public boolean areAllItemsEnabled() {
					return originalAdapter.areAllItemsEnabled();
				}

				@Override
				public boolean isEnabled(int position) {
					return originalAdapter.isEnabled(position);
				}
			});
		}
	}

	public void loadIcons() {
		getDrumInstrument(0).setIconResource(IconResources.KICK);
		getDrumInstrument(1).setIconResource(IconResources.SNARE);
		getDrumInstrument(2).setIconResource(IconResources.HH_CLOSED);
		getDrumInstrument(3).setIconResource(IconResources.HH_OPEN);
		getDrumInstrument(4).setIconResource(IconResources.RIMSHOT);
		drumsDirectory.setIconResource(IconResources.DRUMS);
		recordDirectory.setIconResource(IconResources.MICROPHONE);
		beatRecordDirectory.setIconResource(IconResources.BEAT);
		sampleRecordDirectory.setIconResource(IconResources.SAMPLE);
	}

	public static final String[] drumNames = { "kick", "snare", "hh_closed",
			"hh_open", "rim" };

	public static String appDirectoryPath;
	
	private static DirectoryManager singletonInstance = null;

	private SampleListOnClickAndShowListener sampleListOnClickAndShowListener = new SampleListOnClickAndShowListener();
	private AlertDialog.Builder instrumentSelectAlertBuilder;
	private AlertDialog instrumentSelectAlert = null;
	private ListAdapter instrumentSelectAdapter = null;

	private Directory audioDirectory, recordDirectory, beatRecordDirectory,
			sampleRecordDirectory, drumsDirectory, currDirectory = null;

	private boolean addingTrack = false;

	public static DirectoryManager getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new DirectoryManager();
		}
		return singletonInstance;
	}

	private DirectoryManager() {
		initDataDir();
		audioDirectory = new Directory(null, "audio", null);
		drumsDirectory = new Directory(audioDirectory, "drums", null);
		recordDirectory = new Directory(audioDirectory, "recorded", null);
		sampleRecordDirectory = new Instrument(recordDirectory, "samples", null);
		beatRecordDirectory = new Instrument(recordDirectory, "beats", null);
		beatRecordDirectory
				.setEmptyMsg("You haven't recorded any beats yet!  Use the record button at the top to record your beats.");
		for (String drumName : drumNames) {
			new Instrument(drumsDirectory, drumName, null);
		}
		instrumentSelectAlertBuilder = new AlertDialog.Builder(
				GlobalVars.mainActivity);
	}

	public void updateInstrumentSelectAlert(Directory newDirectory) {
		updateInstrumentSelectAdapter(newDirectory);
		updateInstrumentSelectTitleBar();
		instrumentSelectAlertBuilder.setAdapter(instrumentSelectAdapter,
				sampleListOnClickAndShowListener);
		instrumentSelectAlert = instrumentSelectAlertBuilder.create();
		instrumentSelectAlert
				.setOnShowListener(sampleListOnClickAndShowListener);
	}

	public void showAddTrackAlert() {
		addingTrack = true;
		show(audioDirectory);
	}

	public void showInstrumentSelectAlert() {
		addingTrack = false;
		show(audioDirectory);
	}

	public void showSampleSelectAlert() {
		addingTrack = false;
		show(TrackManager.currTrack.getInstrument());
	}

	public Instrument getDrumInstrument(int drumNum) {
		return (Instrument) drumsDirectory.getChild(drumNum);
	}

	public String getAudioPath() {
		return audioDirectory.getPath();
	}

	public String getBeatRecordPath() {
		return beatRecordDirectory.getPath();
	}

	public void clearTempFiles() {
		audioDirectory.clearTempFiles();
	}

	private void show(Directory directory) {
		currDirectory = directory;
		updateInstrumentSelectAlert(currDirectory);
		instrumentSelectAlert.show();
	}

	private void initDataDir() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// we can read and write to external storage
			String extStorageDir = Environment.getExternalStorageDirectory()
					.toString();
			appDirectoryPath = extStorageDir + "/BeatBot/";
		} else { // we need read AND write access for this app - default to
					// internal storage
			// appDirectoryPath = getFilesDir().toString() + "/";
			// TODO throw / catch exception - need External SD Card!
		}
	}

	private String[] formatNames(String[] names) {
		String[] formattedNames = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			formattedNames[i] = names[i].replace(".wav", "");
		}
		return formattedNames;
	}

	private void updateInstrumentSelectTitleBar() {
		if (currDirectory.getListTitleResource() != -1) {
			instrumentSelectAlertBuilder.setIcon(
					currDirectory.getListTitleResource()).setTitle(
					currDirectory.getName().toUpperCase());
		} else {
			instrumentSelectAlertBuilder.setIcon(0).setTitle(
					"Choose Instrument");
		}
	}

	private void updateInstrumentSelectAdapter(final Directory directory) {
		String[] list = formatNames(directory.getChildNames());
		instrumentSelectAdapter = new ArrayAdapter<String>(
				GlobalVars.mainActivity, android.R.layout.select_dialog_item,
				android.R.id.text1, list) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				TextView tv = (TextView) v.findViewById(android.R.id.text1);
				// if the directory is a root directory (only sample children)
				// or does not have an icon, no icon for this list element
				if (directory instanceof Instrument
						|| directory.getChild(position).getIconResource() == null) {
					tv.setCompoundDrawables(null, null, null, null);
					return v;
				}
				tv.setCompoundDrawablesWithIntrinsicBounds(
						directory.getChild(position).getListViewResource(), 0,
						0, 0);
				// Add margin between image and text (support various screen
				// densities)
				int dpMargin = (int) (15 * GlobalVars.mainActivity
						.getResources().getDisplayMetrics().density + 0.5f);
				tv.setCompoundDrawablePadding(dpMargin);

				return v;
			}
		};
	}
}
