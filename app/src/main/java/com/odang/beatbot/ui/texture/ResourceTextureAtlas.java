package com.odang.beatbot.ui.texture;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.odang.beatbot.R;

import java.lang.reflect.Field;

public class ResourceTextureAtlas extends TextureAtlas {
    private final static int[] RESOURCE_IDS = getAllResourceIds();
    private final Resources resources;
    private static int mainIconResourceId;

    public ResourceTextureAtlas(Resources resources) {
        this.resources = resources;

        config = new Config();
        config.numRegions = RESOURCE_IDS.length;
        config.regionIdOffset = RESOURCE_IDS[0];
        config.bitmapConfig = Bitmap.Config.ARGB_4444;

        final Bitmap first = BitmapFactory.decodeResource(resources, RESOURCE_IDS[0]);
        config.cellWidth = first.getWidth();
        config.cellHeight = first.getHeight();
        config.textureYOffset = 0;
        // assume all resources are squares of the same width
        config.textureSize = (int) Math.ceil(Math.sqrt(config.numRegions)) * config.cellWidth;

        super.createCanvas();
        initTextureRegions();
    }

    @Override
    protected void drawTextureRegion(int regionId, float x, float y) {
        Bitmap resourceBitmap = BitmapFactory.decodeResource(resources, regionId);
        canvas.drawBitmap(resourceBitmap, x, y, paint);
    }

    protected boolean shouldSkipResourceId(int resourceId) {
        return resourceId == mainIconResourceId;
    }

    private static int[] getAllResourceIds() {
        Field[] ID_Fields = R.drawable.class.getFields();
        int[] resourceIds = new int[ID_Fields.length];
        for (int i = 0; i < ID_Fields.length; i++) {
            try {
                if (ID_Fields[i].getName().equals("beat_bot_icon_large"))
                    mainIconResourceId = i;
                resourceIds[i] = ID_Fields[i].getInt(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return resourceIds;
    }
}
