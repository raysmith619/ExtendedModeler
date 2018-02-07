package ExtendedModeler;
import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;

import smTrace.SmTrace;


public class ColoredText extends EMBlockBase {
	private String text;								// null - generate next text
	Vector3D size = new Vector3D(1,1,1);				// orthogonal dimensions
	private Font font;
	
	
	private float textXDir;
	private float textYDir;
	private float textZDir;
	private boolean textDirByChar;
	
	private float charXDir;
	private float charYDir;
	private float charZDir;
	private boolean charDirWithText;
	
	private float charXSize;
	private float charYSize;
	private float charZSize;
	private boolean charSizeByBlock;

								// Set via reset static function
	private static int def_font_size = 10;
	private static Font defaultFont = new Font(
			"Tahoma", Font.BOLD, def_font_size);

	static int nt = 0;				// nextText base
	/**
	 * Check if ok block
	 */

	public boolean isOk() {
		return isOk;
	}


	/**
	 * Base object
	 */
	public ColoredText(Point3D center, String text, Vector3D size, Font font, Color color, Vector3D up) {
		super(center, dim2radius(text, size, font), color, up);
		this.text = text;
		this.size = size;
		this.font = font;
	}

	/**
	 * Basic dimensions to encompassing radius
	 * @param size
	 * @param rBase
	 * @return
	 */
	public static float dim2radius(String text, Vector3D size, Font font) {
		if (text == null)
			text = nextText();
		if (size == null)
			size = new Vector3D(.5f,.5f,.5f);
		float height = size.y();
		float width = size.x()*text.length();
		float depth = size.z();
		float radius = (float) Math.sqrt(height*height + width*width + depth*depth)/2;
		return radius;
	}

	/**
	 * Get length dimension of a text string
	 */
	public static float getLength(String text, Font font, Vector3D charSize) {
		float char_height = charSize.y();
		float char_width = charSize.x();
		float char_depth = charSize.z();
		int text_length = text.length();
		float width = char_width;
		float height = char_height;
		float depth = width*.3f;
		if (text.length() > 1) {
			AffineTransform affinetransform = new AffineTransform();     
			FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
			int textwidth = (int)(font.getStringBounds(text, frc).getWidth());
			int textheight = (int)(font.getStringBounds(text, frc).getHeight());
			width = height * (float)textwidth/textheight * text.length();
			depth = width*.3f;
		}
		return width;
		
	}
	
	/**
	 * Create new object, from controls
	 * @throws EMBlockError 
	 **/
	public static ColoredText newBlock(ControlsOfScene controlsScene) throws EMBlockError {
		/*** proportional sizes
		if (text.length() > 1) {
			AffineTransform affinetransform = new AffineTransform();     
			FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
			int textwidth = (int)(font.getStringBounds(text, frc).getWidth());
			int textheight = (int)(font.getStringBounds(text, frc).getHeight());
			width = height * (float)textwidth/textheight * text.length();
			depth = width*.3f;
		}
		***/

		ControlOfColor ctc = (ControlOfColor)controlsScene.getControl("color");
		Color color = ctc.getColor();
		ControlOfText ctt = (ControlOfText)controlsScene.getControl("text");
		String text = ctt.getText();
		if (text.equals("~~~") || text.equals(""))		// Support ~~~ for legacy
			text = nextText();
		ControlOfScene ctl = controlsScene.getControl("placement");
		ControlOfPlacement cop = (ControlOfPlacement)ctl;
		Point3D center = cop.getPosition();
		Vector3D size = cop.getSizeXYZ();
		Font font = ctt.getFont();
		Vector3D up =  cop.getUp();
		return new ColoredText(center, text, size, font, color, up);
	}
	
	public ColoredText(
		EMBox3D box,
		Color color,
		String text,
		Font font
	) {
		super(box, color);
		this.text = text;
		this.font = font;
		isOk = true;
	}
	
	public ColoredText(
		EMBox3D box,
		Color color,
		String text
	) {
		this(box, color, text, defaultFont);
	}
	
	public ColoredText(
		EMBox3D box,
		Color color
	) {
		this(box, color, nextText());
	}

	/**
	 * Same iD
	 * @param cb_base
	 */
	public ColoredText(EMBlockBase cb_base) {
		super(cb_base);
		this.font = defaultFont;
	}
	
	/**
	 * Get enclosing oriented box
	 */
	@Override
	public OrientedBox3D getOBox() {
		float height = size.y();
		float width = size.x();
		float depth = size.z();
		OrientedBox3D obox = new OrientedBox3D(width, height, depth, getCenter(), getUp());
		return obox;
	}
	
	
	/**
	 * Generate an aligned box version
	 * 
	 */
	public AlignedBox3D alignedBox3D() {
		return new AlignedBox3D(getMin(), getMax());
	}
	
	private static String nextText() {
		nt++;
		String text = Character.toString((char)((nt%26)+'A'-1));
		return text;
	}

	/**
	 * Reset to initial context
	 */
	public static void reset() {
		def_font_size = 10;
		defaultFont = new Font(
				"Tahoma", Font.BOLD, def_font_size);
		nt = 0;
	}

	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		drawText(drawable, this, expand,
				drawAsWireframe, cornersOnly);
	}

	public String blockType() {
		return "text";
	}



	static public void drawText(
		GLAutoDrawable drawable,
		ColoredText ctext,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly) {
		String text = ctext.text;
		Font font = ctext.font;
		Color color = ctext.color;

		GL2 gl = (GL2) drawable.getGL();
		/*** test lighting
		float[] 		LightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] 		LightAmbient =	 { 0.8f, 0.8f, 0.8f, 1.0f };
		float[] 		LightPosition =	 { 1.0f, 1.0f, 1.0f, 0.0f };
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, LightAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, LightDiffuse, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, LightPosition, 0);
		***/
		OrientedBox3D obox = ctext.getOBox();
		if ( expand ) {
			obox.adjSize(1.1f, 1.1f, 1.1f);
		}
		
		if ( drawAsWireframe ) {
			AlignedBox3D abox = obox.getAlignedBox();
			EMBox3D.rotate2v(drawable, EMBox3D.UP, obox.getUp());
			if ( cornersOnly ) {
				gl.glBegin( GL.GL_LINES );
				for ( int dim = 0; dim < 3; ++dim ) {
					Vector3D v = Vector3D.mult( Point3D.diff(abox.getCorner(1<<dim),abox.getCorner(0)), 0.1f );
					for ( int a = 0; a < 2; ++a ) {
						for ( int b = 0; b < 2; ++b ) {
							int i = (a << ((dim+1)%3)) | (b << ((dim+2)%3));
							gl.glVertex3fv( abox.getCorner(i).get(), 0 );
							gl.glVertex3fv( Point3D.sum( abox.getCorner(i), v ).get(), 0 );
							i |= 1 << dim;
							gl.glVertex3fv( abox.getCorner(i).get(), 0 );
							gl.glVertex3fv( Point3D.diff( abox.getCorner(i), v ).get(), 0 );
						}
					}
				}
				gl.glEnd();
			}
			else {
				// Wire frame text
			}
			Vector3D diagonal = abox.getDiagonal();
			float xlen = Math.abs(diagonal.x());
			float ylen = Math.abs(diagonal.y());
			float zlen = Math.abs(diagonal.z());
			float minlen = Math.min(xlen, ylen);
			minlen = Math.min(minlen, zlen);
			float r = minlen/2;
			Point3D center = abox.getCenter();
			GLUT glut = new GLUT();
			int nLongitudes = 20;
			int nLatitudes = nLongitudes;
			gl.glTranslatef(center.x(), center.y(), center.z());
			// text
			gl.glTranslatef(-center.x(), -center.y(), -center.z());
		}
		else {
			AlignedBox3D abox = obox.getAlignedBox();
			EMBox3D.rotate2v(drawable, EMBox3D.UP, obox.getUp());
			Point3D center = abox.getCenter();
			Point3D base = obox.getMin();
			GLUT glut = new GLUT();
		    float tx = base.x();
		    float ty = base.y();
		    float tz = base.z();
		    float font_size = font.getSize();
		    float tscale = (float) (.01 * font_size );
		    ///tscale *= 2;
		    float depth = 5f;
			
			TextRenderer3D tr3 = new TextRenderer3D(font, tscale);

			float[] colors = new float[4];
			color.getComponents(colors);
		    gl.glColor4f(colors[0], colors[1], colors[2], colors[3]);
		    tr3.setDepth(depth);
		    tr3.draw(text,tx, ty, tz, tscale);
		    SmTrace.lg(String.format("tr3.draw(%s, tx=%.3g, ty=%.3g, tz=%.3g, tscale=%.3g)",
		    		text, tx, ty, tz, tscale), "textdraw");
		    EMBox3D.rotate2vRet(drawable);
		}
	}

	/**
	 * Change attributes from text control
	 * @throws EMBlockError 
	 */
	public void adjustFromControl(ControlOfText cot, EMBCommand bcmd) throws EMBlockError {
		///text = cot.getText();
		///if (text.equals("~~~"))
		///	text = nextText();		// Default sequence
		font = cot.getFont();
		textXDir = cot.getTextXDir();
		textYDir = cot.getTextYDir();
		textZDir = cot.getTextZDir();
		textDirByChar = cot.getTextDirByChar();
		
		charXDir = cot.getCharXDir();
		charYDir = cot.getCharYDir();
		charZDir = cot.getCharZDir();
		charDirWithText = cot.getCharDirWithText();
		
		charXSize = cot.getCharXSize();
		charYSize = cot.getCharYSize();
		charZSize = cot.getCharZSize();
		charSizeByBlock = cot.getCharSizeByBlock();
	}

	
	/**
	 * Set from text control
	 * @throws EMBlockError 
	 */
	public void setFromControl(ControlOfText cot) throws EMBlockError {
		text = cot.getText();
		if (text.equals("~~~"))
			text = nextText();		// Default sequence
		font = cot.getFont();
		textXDir = cot.getTextXDir();
		textYDir = cot.getTextYDir();
		textZDir = cot.getTextZDir();
		textDirByChar = cot.getTextDirByChar();
		
		charXDir = cot.getCharXDir();
		charYDir = cot.getCharYDir();
		charZDir = cot.getCharZDir();
		charDirWithText = cot.getCharDirWithText();
		
		charXSize = cot.getCharXSize();
		charYSize = cot.getCharYSize();
		charZSize = cot.getCharZSize();
		charSizeByBlock = cot.getCharSizeByBlock();
		
		boolean at_next = cot.getPosAtNext();
		if (at_next) {
			float text_x = textXDir;
			float text_y = textYDir;
			float text_z = textZDir;
			if (textDirByChar) {
				float char_x = charXSize;
				float char_y = charYSize;
				float char_z = charZSize;
				if (charSizeByBlock) {
					float bx = this.getSize().x();
					float by = this.getSize().y();
					float bz = this.getSize().z();
					char_x *= bx;
					char_y *= by;
					char_z *= bz;
				
				}
				text_x *= char_x;
				text_y *= char_y;
				text_z *= char_z;
			}
			this.translate(new Vector3D(text_x, text_y, text_z));
		}
	}
	
	/**
	 * Get default font
	 */
	public static Font getFont() {
		return defaultFont;
	}
	
	/**
	 * Set default font
	 */
	public static void setFont(Font font) {
		defaultFont = font;
	}

	
	/**
	 * duplicate block
	 * @throws EMBlockError 
	 */
	@Override
	public EMBlockBase duplicate() throws EMBlockError {
		EMBlockBase cb = copy();
		return cb;
	}
	
	
	@Override
	public EMBlockBase copy() {
		ColoredText cb = (ColoredText) super.copy();
		setText(getText());
		setFont(getFont());
		cb.charXDir = this.charXDir;
		cb.charYDir = this.charYDir;
		cb.charZDir = this.charZDir;
		cb.charDirWithText = this.charDirWithText;
		cb.charSizeByBlock = this.charSizeByBlock;
		cb.textXDir = this.textXDir;
		cb.textYDir = this.textYDir;
		cb.textZDir = this.textZDir;
		cb.textDirByChar = this.textDirByChar;
		
		return cb;
	}
	
	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	
	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		return getBox().intersects(ray, intersection, normalAtIntersection);		
	}

	
	/**
	 * Get size - vector of x,y,z extent of one character
	 */
	public Vector3D getSize() {
		
		Vector3D char_size = new Vector3D(size.x(), size.y(), size.z());
		return char_size;
	}
	


	/**
	 * 
	 * @param new_size - new size with center in place
	 */
	public void resize(
		Vector3D size
	) {
		this.size = new Vector3D(size);
		setRadius(dim2radius(this.text, this.size, this.font));
	}
	
}
