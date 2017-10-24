import java.awt.Color;
import java.awt.Font;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;

public class ColoredText extends EMBlockBase {
	private boolean isOk = false;	// Set OK upon successful construction
	private String text = "BLANK";
	static int nt = 0;				// nextText base
	
	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		return getBox().intersects(ray, intersection, normalAtIntersection);		
	}
	/**
	 * Check if ok block
	 */

	public boolean isOk() {
		return isOk;
	}
	
	public ColoredText(
		AlignedBox3D box,
		Color color,
		String text
	) {
		super(box, color);
		this.text = text;
		isOk = true;
	}
	
	public ColoredText(
		AlignedBox3D box,
		Color color
	) {
		this(box, color, nextText());
	}

	
	private static String nextText() {
		nt++;
		String text = Character.toString((char)((nt%26)+'A'-1));
		return text;
	}


	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		AlignedBox3D box = getBox();
		drawText(drawable, box, expand,
				drawAsWireframe, cornersOnly,
				text, color);
	}

	public String blockType() {
		return "text";
	}



	static public void drawText(
		GLAutoDrawable drawable,
		AlignedBox3D box,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly,
		String text,
		Color color
	) {
		GL2 gl = (GL2) drawable.getGL();
		/*** test lighting
		float[] 		LightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] 		LightAmbient =	 { 0.8f, 0.8f, 0.8f, 1.0f };
		float[] 		LightPosition =	 { 1.0f, 1.0f, 1.0f, 0.0f };
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, LightAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, LightDiffuse, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, LightPosition, 0);
		***/
		
		if ( expand ) {
			float diagonal = box.getDiagonal().length();
			diagonal /= 20;
			Vector3D v = new Vector3D( diagonal, diagonal, diagonal );
			box = new AlignedBox3D( Point3D.diff(box.getMin(),v), Point3D.sum(box.getMax(),v) );
		}
		if ( drawAsWireframe ) {
			if ( cornersOnly ) {
				gl.glBegin( GL.GL_LINES );
				for ( int dim = 0; dim < 3; ++dim ) {
					Vector3D v = Vector3D.mult( Point3D.diff(box.getCorner(1<<dim),box.getCorner(0)), 0.1f );
					for ( int a = 0; a < 2; ++a ) {
						for ( int b = 0; b < 2; ++b ) {
							int i = (a << ((dim+1)%3)) | (b << ((dim+2)%3));
							gl.glVertex3fv( box.getCorner(i).get(), 0 );
							gl.glVertex3fv( Point3D.sum( box.getCorner(i), v ).get(), 0 );
							i |= 1 << dim;
							gl.glVertex3fv( box.getCorner(i).get(), 0 );
							gl.glVertex3fv( Point3D.diff( box.getCorner(i), v ).get(), 0 );
						}
					}
				}
				gl.glEnd();
			}
			else {
				// Wire frame text
			}
			Vector3D diagonal = box.getDiagonal();
			float xlen = Math.abs(diagonal.x());
			float ylen = Math.abs(diagonal.y());
			float zlen = Math.abs(diagonal.z());
			float minlen = Math.min(xlen, ylen);
			minlen = Math.min(minlen, zlen);
			float r = minlen/2;
			Point3D center = box.getCenter();
			GLUT glut = new GLUT();
			int nLongitudes = 20;
			int nLatitudes = nLongitudes;
			gl.glTranslatef(center.x(), center.y(), center.z());
			// text
			gl.glTranslatef(-center.x(), -center.y(), -center.z());
		}
		else {
			Point3D center = box.getCenter();
			Point3D base = box.getMin();
			GLUT glut = new GLUT();
		    float tx = base.x();
		    float ty = base.y();
		    float tz = base.z();
		    int font_size = 10;
		    float tscale = (float) (.8 * 1./font_size);
		    float depth = 5f;
			
			TextRenderer3D tr3 = new TextRenderer3D(new Font("SansSerif", Font.BOLD, font_size), tscale);
			tr3 = new TextRenderer3D(new Font("TimesRoman", Font.BOLD, font_size), tscale);
			tr3 = new TextRenderer3D(new Font("Tahoma", Font.BOLD, font_size), tscale);

			float[] colors = new float[4];
			color.getComponents(colors);
		    gl.glColor4f(colors[0], colors[1], colors[2], colors[3]);
		    tr3.setDepth(depth);
		    ///tr3.setFill(false);		// false -> just edges
		    ///tr3.setFlatness(1f);
		    tr3.draw(text,tx, ty, tz, tscale);
		    ///renderer.end3DRendering();
			
			///gl.glTranslatef(-center.x(), -center.y(), -center.z());
		}
	}
	@Override
	public EMBlockBase copy() {
		super.copy();
		return null;
	}
	

	
}