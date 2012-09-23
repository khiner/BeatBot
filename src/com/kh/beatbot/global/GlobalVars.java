package com.kh.beatbot.global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kh.beatbot.R;
import com.kh.beatbot.view.MidiView;

public class GlobalVars {
	public static Typeface font;

	// time (in millis) between pointer down and pointer up to be considered a
	// tap
	public final static long SINGLE_TAP_TIME = 200;
	// time (in millis) between taps before handling as a double-tap
	public final static long DOUBLE_TAP_TIME = 300;
	// time (in millis) for a long press in one location
	public final static long LONG_CLICK_TIME = 500;

	public static final String[] allInstrumentTypes = { "kick", "snare",
			"hh_closed", "hh_open", "rim", "recorded" };
	public static final ArrayList<String> currentInstrumentNames = new ArrayList<String>(
			Arrays.asList(new String[] { "kick", "snare", "hh_closed",
					"hh_open", "rim" }));

	public static final Map<String, Integer> instrumentSources = new HashMap<String, Integer>();

	public static Map<String, Instrument> instruments = new HashMap<String, Instrument>();
	static {
		instrumentSources.put("kick", R.drawable.kick_icon_src);
		instrumentSources.put("snare", R.drawable.snare_icon_src);
		instrumentSources.put("hh_closed", R.drawable.hh_closed_icon_src);
		instrumentSources.put("hh_open", R.drawable.hh_open_icon_src);
		instrumentSources.put("rim", R.drawable.rimshot_icon_src);
		instrumentSources.put("recorded", R.drawable.microphone_icon_src);
	}

	public static BeatBotIconSource muteIcon, soloIcon, previewIcon,
			beatSyncIcon;

	public static ListAdapter instrumentSelectAdapter = null;
	public static OnShowListener instrumentSelectOnShowListener = null;

	public static final int UNDO_STACK_SIZE = 40;
	public static final int NUM_EFFECTS = 7;
	public static final short LEVEL_MAX = 127;

	public static MidiView midiView;
	public static String appDirectory;

	// effect settings are stored here instead of in the effect activities
	// because the activities are destroyed after clicking 'back', and we
	// need to persist state
	public static List<Track> tracks = new ArrayList<Track>();
	public static float currBeatDivision;

	public static void initTracks() {
		for (String instrumentName : allInstrumentTypes) {
			Instrument instrument = new Instrument(instrumentName,
					new BeatBotIconSource());
			instruments.put(instrumentName, instrument);
			if (!instrumentName.equals("recorded"))
				tracks.add(new Track(instrument));
		}
	}

	public static void initIcons() {
		muteIcon = new BeatBotIconSource(R.drawable.mute_icon,
				R.drawable.mute_icon_selected);
		soloIcon = new BeatBotIconSource(R.drawable.solo_icon,
				R.drawable.solo_icon_selected);
		previewIcon = new BeatBotIconSource(R.drawable.preview_icon,
				R.drawable.preview_icon_selected);
		beatSyncIcon = new BeatBotIconSource(R.drawable.clock,
				R.drawable.note_icon);
		instruments.get("kick").setIconResources(R.drawable.kick_icon_small,
				R.drawable.kick_icon_selected_small,
				R.drawable.kick_icon_listview);
		instruments.get("snare").setIconResources(R.drawable.snare_icon_small,
				R.drawable.snare_icon_selected_small,
				R.drawable.snare_icon_listview);
		instruments.get("hh_closed").setIconResources(
				R.drawable.hh_closed_icon_small,
				R.drawable.hh_closed_icon_selected_small,
				R.drawable.hh_closed_icon_listview);
		instruments.get("hh_open").setIconResources(
				R.drawable.hh_open_icon_small,
				R.drawable.hh_open_icon_selected_small,
				R.drawable.hh_open_icon_listview);
		instruments.get("rim").setIconResources(R.drawable.rimshot_icon_small,
				R.drawable.rimshot_icon_selected_small,
				R.drawable.rimshot_icon_listview);
		instruments.get("recorded").setIconResources(
				R.drawable.microphone_icon_small,
				R.drawable.microphone_icon_selected_small,
				R.drawable.microphone_icon_listview);
	}

	public static void initInstrumentSelect(final Activity activity) {
		initInstrumentSelectAdapter(activity);
		initInstrumentSelectOnShowListener();
	}
	
	private static void initInstrumentSelectAdapter(final Activity activity) {
		instrumentSelectAdapter = new ArrayAdapter<String>(activity,
				android.R.layout.select_dialog_item, android.R.id.text1,
				allInstrumentTypes) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				TextView tv = (TextView) v.findViewById(android.R.id.text1);
				// Put the image on the TextView
				tv.setCompoundDrawablesWithIntrinsicBounds(
						instruments.get(allInstrumentTypes[position]).getIconSource().listViewIcon.resourceId,
						0, 0, 0);
				// Add margin between image and text (support various screen
				// densities)
				int dp5 = (int) (5 * activity.getResources()
						.getDisplayMetrics().density + 0.5f);
				tv.setCompoundDrawablePadding(dp5);

				return v;
			}
		};
	}

	private static void initInstrumentSelectOnShowListener() {
		instrumentSelectOnShowListener = new OnShowListener() {
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
						View view = originalAdapter.getView(position,
								convertView, parent);
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
					public void unregisterDataSetObserver(
							DataSetObserver observer) {
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
		};
	}
}
