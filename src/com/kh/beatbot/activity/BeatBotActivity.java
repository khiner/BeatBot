package com.kh.beatbot.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.kh.beatbot.FileHelper;
import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.R;
import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.manager.DirectoryManager;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.PlaybackManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.group.GLSurfaceViewGroup;
import com.kh.beatbot.ui.view.group.ViewPager;
import com.kh.beatbot.ui.view.page.MainPage;
import com.kh.beatbot.ui.view.page.Page;
import com.kh.beatbot.ui.view.page.effect.EffectPage;

public class BeatBotActivity extends Activity {

	public static final int BPM_DIALOG_ID = 0, EXIT_DIALOG_ID = 1,
			SAMPLE_NAME_EDIT_DIALOG_ID = 2;

	private static final int MAIN_PAGE_NUM = 0, EFFECT_PAGE_NUM = 1;

	private static ViewPager activityPager;
	private static EditText bpmInput, sampleNameInput;

	public static BeatBotActivity mainActivity;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GeneralUtils.initAndroidSettings(this);
		mainActivity = this;
		View.font = Typeface.createFromAsset(getAssets(),
				"REDRING-1969-v03.ttf");
		Colors.initColors(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		View.root = new GLSurfaceViewGroup(this);
		View.root.setLayoutParams(lp);

		LinearLayout layout = new LinearLayout(this);
		layout.addView(View.root);
		setContentView(layout, lp);

		DirectoryManager.init();

		FileHelper.copyAllSamplesToStorage();
		if (savedInstanceState == null) {
			initNativeAudio();
		}

		Page.mainPage = new MainPage();
		Page.effectPage = new EffectPage();

		activityPager = new ViewPager();
		activityPager.addPage(Page.mainPage);
		activityPager.addPage(Page.effectPage);
		activityPager.setPage(0);

		((GLSurfaceViewGroup) View.root).setBBRenderer(activityPager);

		TrackManager.init();
		MidiManager.init();
	}

	@Override
	public void onDestroy() {
		try {
			super.onDestroy();
			if (isFinishing()) {
				shutdown();
				// android.os.Process.killProcess(android.os.Process.myPid());
			}
		} finally {
			DirectoryManager.clearTempFiles();
		}
	}

	@Override
	public void onBackPressed() {
		if (activityPager.getCurrPageNum() == MAIN_PAGE_NUM) {
			showDialog(EXIT_DIALOG_ID);
		} else if (activityPager.getCurrPageNum() == EFFECT_PAGE_NUM) {
			Page.mainPage.pageSelectGroup.updateLevelsFXPage();
			activityPager.setPage(MAIN_PAGE_NUM);
		}
	}

	@Override
	public void onPause() {
		View.root.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		View.root.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("playing",
				PlaybackManager.getState() == PlaybackManager.State.PLAYING);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case BPM_DIALOG_ID:
			bpmInput.setText(String.valueOf((int) MidiManager.getBPM()));
			break;
		case SAMPLE_NAME_EDIT_DIALOG_ID:
			sampleNameInput.setText(TrackManager.currTrack.getCurrSampleName());
			break;
		case EXIT_DIALOG_ID:
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch (id) {
		case BPM_DIALOG_ID:
			bpmInput = new EditText(this);

			bpmInput.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
			builder.setTitle("Set BPM")
					.setView(bpmInput)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String bpmString = bpmInput.getText()
											.toString();
									if (!bpmString.isEmpty()) {
										Page.mainPage.pageSelectGroup
												.getMasterPage()
												.setBPM(Integer
														.valueOf(bpmString));
									}
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
			break;
		case SAMPLE_NAME_EDIT_DIALOG_ID:
			sampleNameInput = new EditText(this);

			builder.setTitle("Edit Sample Name")
					.setView(sampleNameInput)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String sampleName = sampleNameInput
											.getText().toString();
									if (!sampleName.isEmpty()) {
										TrackManager.currTrack
												.setCurrSampleName(sampleName);
										Page.mainPage.pageSelectGroup.update();
									}
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
			break;
		case EXIT_DIALOG_ID:
			builder.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Closing " + getString(R.string.app_name))
					.setMessage(
							"Are you sure you want to exit "
									+ getString(R.string.app_name) + "?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									try {
										finish();
									} catch (Exception e) {
										DirectoryManager.clearTempFiles();
									}
								}
							}).setNegativeButton("No", null);
			break;
		}
		return builder.create();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		Intent midiFileMenuIntent = new Intent(this, MidiFileMenuActivity.class);
		menu.findItem(R.id.midi_menu_item).setIntent(midiFileMenuIntent);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.snap:
			if (Page.mainPage.midiView.toggleSnapToGrid()) {
				item.setIcon(R.drawable.btn_check_buttonless_on);
			} else {
				item.setIcon(R.drawable.btn_check_buttonless_off);
			}
			return true;
		case R.id.quantize_current:
			MidiManager.quantize();
			return true;
		case R.id.quantize_quarter:
			MidiManager.quantize(1);
		case R.id.quantize_eighth:
			MidiManager.quantize(2);
			return true;
		case R.id.quantize_sixteenth:
			MidiManager.quantize(4);
			return true;
		case R.id.quantize_thirty_second:
			MidiManager.quantize(8);
			return true;
		case R.id.save_wav:
			return true;
			// midi import/export menu item is handled as an intent -
			// MidiFileMenuActivity.class
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * Set up the project. For now, this just means setting track 0, page 0 view
	 */
	public void setupProject() {
		TrackManager.getTrack(0).select();
		Page.mainPage.pageSelectGroup.selectPage(0);
	}

	public void launchEffect(Effect effect) {
		activityPager.setPage(EFFECT_PAGE_NUM);
		Page.effectPage.loadEffect(effect);
	}

	private void initNativeAudio() {
		createEngine();
		createAudioPlayer();
	}

	private void shutdown() {
		nativeShutdown();
	}

	public static native boolean createAudioPlayer();

	public static native void createEngine();

	public static native void nativeShutdown();

	/** Load jni .so on initialization */
	static {
		System.loadLibrary("nativeaudio");
	}
}