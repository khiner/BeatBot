package com.kh.beatbot.ui.mesh;

class TextureRegion {
	
	public float u1, v1; // Top/Left U,V Coordinates
	public float u2, v2; // Bottom/Right U,V Coordinates

	// D: calculate U,V coordinates from specified texture coordinates
	// A: texWidth, texHeight - the width and height of the texture the region
	// is for
	// x, y - the top/left (x,y) of the region on the texture (in pixels)
	// width, height - the width and height of the region on the texture (in
	// pixels)
	public TextureRegion(float texWidth, float texHeight, float x, float y,
			float width, float height) {
		u1 = x / texWidth; // Calculate U1
		v1 = y / texHeight; // Calculate V1
		u2 = u1 + (width / texWidth); // Calculate U2
		v2 = v1 + (height / texHeight); // Calculate V2
	}
}
