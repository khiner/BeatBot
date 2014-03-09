package com.kh.beatbot.ui.texture;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.kh.beatbot.R;

public class ResourceTextureAtlas extends TextureAtlas {
	private final static int[] RESOURCE_IDS = getAllResourceIds();
	private Resources resources = null;

	public void initConfig() {
		config = new Config();
		Bitmap first = BitmapFactory.decodeResource(resources, RESOURCE_IDS[0]);

		config.numRegions = RESOURCE_IDS.length;
		config.regionIdOffset = RESOURCE_IDS[0];
		config.bitmapConfig = Bitmap.Config.ARGB_4444;
		config.cellWidth = first.getWidth();
		config.cellHeight = first.getHeight();

		config.textureYOffset = 0;
		// assume all resources are squares of the same width
		config.textureSize = (int) Math.ceil(Math.sqrt(RESOURCE_IDS.length)) * config.cellWidth;
	}

	public void load(Activity activity) {
		resources = activity.getResources();
		super.load(activity);
	}

	@Override
	protected void drawTextureRegion(int regionId, float x, float y) {
		Bitmap resourceBitmap = BitmapFactory.decodeResource(resources, regionId);
		canvas.drawBitmap(resourceBitmap, x, y, paint);
	}

	private static int[] getAllResourceIds() {
		Field[] ID_Fields = R.drawable.class.getFields();
		int[] resourceIds = new int[ID_Fields.length];
		for (int i = 0; i < ID_Fields.length; i++) {
			try {
				resourceIds[i] = ID_Fields[i].getInt(null);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return resourceIds;
	}
}
