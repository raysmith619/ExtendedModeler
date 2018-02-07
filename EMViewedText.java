package ExtendedModeler;

import java.awt.Font;

import com.jogamp.opengl.GL2;

/**
 * Produce viewable text in 3D environment Text that is facing the viewer
 * 
 * @author raysm
 *
 */
public class EMViewedText {
	GL2 gl;
	Font font;
	float scale;
	private static int def_font_size = 10;
	private static Font defaultFont = new Font(
			"Tahoma", Font.BOLD, def_font_size);
    float font_size;
    float tscale;
	TextRenderer3D tr3;

	EMViewedText(GL2 gl) {
		this(gl, null, 0);
	}
	EMViewedText(GL2 gl, Font font, float scale) {
		this.gl = gl;
		if (font == null)
			font = defaultFont;
		if (scale == 0)
			scale = def_font_size;
		this.font = font;
		this.scale = scale;

	    font_size = font.getSize();
	    tscale = (float) (.0015 * font_size );

		tr3 = new TextRenderer3D(font, tscale);
		tr3.setFill(true);
		tr3.setDepth(10f);

	}
	
	public void draw(String text, float tx, float ty, float tz) {		
		gl.glPushMatrix();
 		gl.glTranslatef(tx, ty, tz);
 		gl.glScalef(tscale, tscale, tscale);
		tr3.draw(text);
		gl.glPopMatrix(); 		
	}
}
