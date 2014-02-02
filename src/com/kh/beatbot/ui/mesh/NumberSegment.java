package com.kh.beatbot.ui.mesh;

public class NumberSegment extends Shape {

	public NumberSegment(ShapeGroup group, float[] fillColor,
			float[] strokeColor) {
		super(group, fillColor, strokeColor, 6, 0);
	}

	@Override
	protected void updateVertices() {
		if (width > height) {
			updateVerticesHorizontal();
		} else {
			updateVerticesVerticle();
		}
	}
	
	/*      *
	 *    *   *
	 *    *   *
	 *      *
	 */
	private void updateVerticesVerticle() {
		// top triangle
		fillVertex(x + width / 2, y);
		fillVertex(x, y + width / 2);
		fillVertex(x + width, y + width / 2);
		// bottom triangle
		fillVertex(x + width / 2, y + height);
		fillVertex(x, y + height - width / 2);
		fillVertex(x + width, y + height - width / 2);
		// middle square
		fillVertex(x + width, y + width / 2);
		fillVertex(x, y + width / 2);
		fillVertex(x + width, y + height - width / 2);
		fillVertex(x, y + width / 2);
		fillVertex(x + width, y + height - width / 2);
		fillVertex(x, y + height - width / 2);
	}
	
	/*      * *
	 *    *     *
	 *      * *
	 */
	private void updateVerticesHorizontal() {
		float x = this.x + height / 2 + 1;
		float width = this.width - height - 2;
		// left triangle
		fillVertex(x, y + height / 2);
		fillVertex(x + height / 2, y);
		fillVertex(x + height / 2, y + height);
		// right triangle
		fillVertex(x + width, y + height / 2);
		fillVertex(x + width - height / 2, y);
		fillVertex(x + width - height / 2, y + height);
		// middle square
		fillVertex(x + height / 2, y);
		fillVertex(x + height / 2, y + height);
		fillVertex(x + width - height / 2, y);
		fillVertex(x + height / 2, y + height);
		fillVertex(x + width - height / 2, y);
		fillVertex(x + width - height / 2, y + height);
	}
}
