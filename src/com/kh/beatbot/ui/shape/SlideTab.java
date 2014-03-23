package com.kh.beatbot.ui.shape;

import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.view.View;

public class SlideTab extends Shape {
	private static final int NUM_FILL_VERTICES = RoundedRect.NUM_CORNER_VERTICES * 5 * 3 * 2;
	private static final short[] FILL_INDICES = getFillIndices();

	private static RoundedRect roundedRect = new RoundedRect(null, Color.LABEL_SELECTED, null);

	private float cornerRadius = 12;

	public SlideTab(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		super(group, fillColor, strokeColor, FILL_INDICES, null, NUM_FILL_VERTICES, 0);
	}

	private static short[] getFillIndices() {
		short[] fillIndices = new short[RoundedRect.FILL_INDICES.length * 2
				+ RoundedRect.NUM_CORNER_VERTICES * 2 + 2];
		for (int i = 0; i < 3; i++) {
			short[] indices = RoundedRect.FILL_INDICES;
			for (int j = 0; j < indices.length; j++) {
				int index = i * indices.length + j;
				if (index < fillIndices.length - 1) {
					fillIndices[index] = (short) (indices[j] + i * RoundedRect.NUM_FILL_VERTICES);
				} else if (index == fillIndices.length - 1) {
					fillIndices[index] = fillIndices[index - 1]; // degenerate triangle
				} else {
					break;
				}
			}
		}
		return fillIndices;
	}

	@Override
	protected void updateVertices() {
		roundedRect.setCornerRadius(cornerRadius);
		roundedRect.layout(x + View.mainPage.width - cornerRadius * 2, y, width, height);

		for (int i = 0; i < RoundedRect.NUM_FILL_VERTICES; i++) {
			fillVertex(roundedRect.getFillVertexX(i), roundedRect.getFillVertexY(i));
		}

		roundedRect.layout(x, y, View.mainPage.width, View.mainPage.height - y - cornerRadius);

		for (int i = 0; i < RoundedRect.NUM_FILL_VERTICES; i++) {
			fillVertex(roundedRect.getFillVertexX(i), roundedRect.getFillVertexY(i));
		}

		roundedRect.layout(x + View.mainPage.width, y + height, width, height);

		fillVertex(x + View.mainPage.width, y + height);

		for (int i = RoundedRect.NUM_CORNER_VERTICES * 2 + 1; i < RoundedRect.NUM_CORNER_VERTICES * 3 + 1; i++) {
			fillVertex(roundedRect.getFillVertexX(i), roundedRect.getFillVertexY(i));
		}
	}
}
