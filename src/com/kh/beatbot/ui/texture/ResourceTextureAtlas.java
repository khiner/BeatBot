package com.kh.beatbot.ui.texture;

import java.lang.reflect.Field;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.SparseArray;

import com.kh.beatbot.R;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.ui.view.GLSurfaceViewBase;

public class ResourceTextureAtlas {
	private static Bitmap bitmap = null;
	private static int[] textureId = new int[1],
			resourceIds = getAllResourceIds();

	private static SparseArray<TextureRegion> textureRegions = new SparseArray<TextureRegion>();

	public static void loadAllResources() {
		Resources resources = BeatBotActivity.mainActivity.getResources();

		Bitmap first = BitmapFactory.decodeResource(resources, resourceIds[0]);

		// assume all resources are squares of the same width
		int textureSize = (int) Math.ceil(Math.sqrt(resourceIds.length))
				* first.getWidth();

		bitmap = Bitmap.createBitmap(textureSize, textureSize,
				first.getConfig());
		bitmap.eraseColor(0x00000000); // Set Transparent Background (ARGB)

		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();

		float x = 0, y = 0;
		for (int resourceId : resourceIds) {
			Bitmap resourceBitmap = BitmapFactory.decodeResource(resources,
					resourceId);
			canvas.drawBitmap(resourceBitmap, x, y, paint);

			textureRegions.put(resourceId, new TextureRegion(textureSize,
					textureSize, x, y, resourceBitmap.getWidth(),
					resourceBitmap.getHeight()));

			x += resourceBitmap.getWidth();
			if (x + resourceBitmap.getWidth() > textureSize) {
				x = 0; // Set X for New Row
				y += resourceBitmap.getHeight(); // Move Down a Row
			}
		}
	}

	public static void loadTexture() {
		// load bitmap texture in OpenGL
		GLSurfaceViewBase.loadTexture(bitmap, textureId, 0);
	}

	public static int[] getTextureId() {
		return textureId;
	}

	public static TextureRegion getTextureRegion(int resourceId) {
		return textureRegions.get(resourceId);
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
