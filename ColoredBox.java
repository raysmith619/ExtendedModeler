package ExtendedModeler;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

import smTrace.SmTrace;


public class ColoredBox extends EMBlockBase {
	/**
	 *  Oriented box
	 * Use this for object size and orientation
	 * Functions that depend / affect position
	 * MUST be overridden to use "obox"
	 * instead of super.box
	 */
	AlignedBox3D abox = new AlignedBox3D();

	/**
	 * Generic object
	 */
	public ColoredBox() {
	}
	
	public ColoredBox(
		EMBox3D box,
		Color color
	) {
		super(box, color);
		setBox(box);
		isOk = true;
	}


	/**
	 * Base object
	 */
	public ColoredBox(Point3D center, Vector3D size, Color color, Vector3D up) {
		super(center, size2radius(size), color, up);
		if (size == null)
			size = new Vector3D(0,0,0);

		this.abox = new AlignedBox3D(center, size);
	}
	/**
	 * Create new object, from controls
	 * @throws EMBlockError 
	 **/
	public static ColoredBox newBlock(ControlsOfScene controlsOfScene) throws EMBlockError {
		ControlOfColor ctc = (ControlOfColor)controlsOfScene.getControl("color");
		Color color = ctc.getColor();
		ControlOfScene ctl = controlsOfScene.getControl("placement");
		ControlOfPlacement cop = (ControlOfPlacement)ctl;
		Point3D center = cop.getPosition();
		Vector3D size = cop.getSizeXYZ();
		Vector3D up =  cop.getUp();
		return new ColoredBox(center, size, color, up);
	}
	
	public ColoredBox(ColoredBox cbox) {
		this(cbox.getCenter(), cbox.getSize(), cbox.color, cbox.getUp());
	}

	
	public ColoredBox(EMBox3D box, Color color, int iD) {
		super(box, color, iD);
	}

	/**
	 * Same iD
	 * @param cb_base
	 */
	public ColoredBox(EMBlockBase cb_base) {
		super(cb_base);
	}

	/**
	 * Convert xyz size to radius of object, enclosing object
	 */
	public static float size2radius(Vector3D size) {
		if (size == null)
			size = new Vector3D(0,0,0);
		return size.length()/2;
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

	
	/**
	 * Copy
	 * "Deep-enough" copy to protect against subsequent modifications
	 */
	public EMBlockBase copy() {
		EMBlockBase cb = new ColoredBox(this.box, this.color);
		cb = cb.copy();
		return cb;
	}

	@Override
	public String blockType() {
		return "box";
	}
	

	/**
	 * Get corner (global coordinates)
	 * @param i
	 * @return
	 */
	public Point3D getCorner(int i) {
		return new Point3D(
			((i & 1)!=0) ? getMax().x() : getMin().x(),
			((i & 2)!=0) ? getMax().y() : getMin().y(),
			((i & 4)!=0) ? getMax().z() : getMin().z()
		);
	}

	// Return the corner that is farthest along the given direction.
	public Point3D getExtremeCorner( Vector3D v ) {
		return new Point3D(
			v.x() > 0 ? getMax().x() : getMin().x(),
			v.y() > 0 ? getMax().y() : getMin().y(),
			v.z() > 0 ? getMax().z() : getMin().z()
		);
	}

	// Return the index of the corner that
	// is farthest along the given direction.
	public int getIndexOfExtremeCorner( Vector3D v ) {
		int returnValue = 0;
		if (v.x() > 0) returnValue |= 1;
		if (v.y() > 0) returnValue |= 2;
		if (v.z() > 0) returnValue |= 4;
		return returnValue;
	}


	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		GL2 gl = (GL2) drawable.getGL();
		glp = gl;					// For tracking
		OrientedBox3D obox = getOBox();
		if ( expand ) {
			float adj = (float) 1.1;
			obox.adjSize(adj, adj, adj);
		}
		AlignedBox3D abox = obox.getAlignedBox();
		EMBox3D.rotate2v(drawable, EMBox3D.UP, obox.getUp());
		if ( drawAsWireframe ) {
			setEmphasis(gl);
			if ( cornersOnly ) {
				gl_glBegin( GL.GL_LINES, "drawAsWireframe cornersOnly GL.GL_LINES" );
				for ( int dim = 0; dim < 3; ++dim ) {
					Point3D pvto = abox.getCorner(1<<dim);
					Point3D pv0 = abox.getCorner(0);
					Vector3D pdiff = Point3D.diff( pvto, pv0);
					Vector3D v = Vector3D.mult(pdiff, 0.1f );
					for ( int a = 0; a < 2; ++a ) {
						for ( int b = 0; b < 2; ++b ) {
							int i = (a << ((dim+1)%3)) | (b << ((dim+2)%3));
							Point3D pvi = abox.getCorner(i);
							gl_glVertex3fv( pvi.get(), 0 );
							gl_glVertex3fv( Point3D.sum( pvi, v ).get(), 0 );
							i |= 1 << dim;
							Point3D pvi2 = abox.getCorner(i);
							gl_glVertex3fv( pvi2.get(), 0 );
							gl_glVertex3fv( Point3D.diff( pvi2, v ).get(), 0 );
						}
					}
				}
				gl_glEnd();
			}
			else {
				gl_glBegin(GL.GL_LINE_STRIP, "drawAsWireframe GL.GL_LINE_STRIP");
					gl_glVertex3fv( abox.getCorner( 0 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 1 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 3 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 2 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 6 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 7 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 5 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 4 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 0 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 2 ).get(), 0 );
				gl_glEnd();
				gl_glBegin( GL.GL_LINES, "drawAsWireframe GL.GL_LINE_STRIP" );
					gl_glVertex3fv( abox.getCorner( 1 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 5 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 3 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 7 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 4 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 6 ).get(), 0 );
				gl_glEnd();
			}
			clearEmphasis();

		}
		else {
			if (SmTrace.trace("drawloc")) {
				SmTrace.lg(String.format("drawLoc %d %s center: %s corner0: %s",
					iD(), blockType(), getCenter(), getCorner(0)));	
			}
			Color color = getColor();
			SmTrace.lg(String.format("drawColor %d %s color=%s",
					iD(), blockType(), color), "drawcolor");
			ColoredBox.setMaterial(gl, color);
			
			gl.glBegin( GL2.GL_QUAD_STRIP );
				gl_glVertex3fv( abox.getCorner( 0 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 1 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 4 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 5 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 6 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 7 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 2 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 3 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 0 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 1 ).get(), 0 );
			gl_glEnd();

			gl_glBegin( GL2.GL_QUADS, "solid GL2.GL_QUADS" );
				gl_glVertex3fv( abox.getCorner( 1 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 3 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 7 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 5 ).get(), 0 );

				gl_glVertex3fv( abox.getCorner( 0 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 4 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 6 ).get(), 0 );
				gl_glVertex3fv( abox.getCorner( 2 ).get(), 0 );
			gl_glEnd();
			ColoredBox.clearMaterial(gl);
		}
		EMBox3D.rotate2vRet(drawable);
	}

	

static public void drawBox(
	GLAutoDrawable drawable,
	ColoredBox cbox,
	boolean expand,
	boolean drawAsWireframe,
	boolean cornersOnly
	) {
	cbox.draw(drawable, expand, drawAsWireframe, cornersOnly);
}


	/**
	 * Tracking / Debugging 
	 * @param v
	 * @param offset
	 */

	/**
	 * Tracking / Debugging 
	 * @param v
	 * @param offset
	 */
	private static GL2 glp;				// for tracking routine
	private static String block = "box";
	private static void gl_glVertex3fv(float [] v, int offset) {
		String desc = traceDesc;
		glp.glVertex3fv(v, offset);
		SmTrace.lg(String.format("%s glVertex3fv %.2f %.2f %.2f", block, v[0], v[1], v[2], desc), "draw");
	}
	private static String traceDesc = "trace description";
	private static void gl_glBegin(int mode, String desc) {
		traceDesc = desc;
		glp.glBegin(mode);
		SmTrace.lg(String.format("%s gl_glBegin(%s)", block, desc), "draw");
	}
	private static void gl_glBegin(int mode) {
		String desc = String.format("mode(%d)", mode);
		gl_glBegin(mode, desc);
	}
	
	private static void gl_glEnd(String desc) {
		glp.glEnd();
		SmTrace.lg(String.format("%s gl_glEnd %s", block, desc), "draw");
	}
	private static void gl_glEnd() {
		String desc = traceDesc;
		gl_glEnd(desc);
	}

	

	/**
	 * Generate an image icon for create button
	 * @param gl
	 * @param width
	 * @param height
	 * @return
	 */
	static public ImageIcon imageIcon(int width, int height) {
		String icon_name = ControlOfComponent.iconFileName("box.png");
		File file = new File(icon_name);
		
		String basename = file.getName();
		Icon icon = new ImageIcon(icon_name);
		icon = ControlOfComponent.resizeIcon((ImageIcon)icon, width, height);
		return (ImageIcon) icon;
	}
	

	public Point3D getMin() {
		OrientedBox3D obox = new OrientedBox3D(abox, getUp());
		return obox.getMin();
	}

	public Point3D getMax() {
		OrientedBox3D obox = new OrientedBox3D(abox, getUp());
		return obox.getMax();
	}
	
	
	/**
	 * Return copy of inner box
	 * 
	 */
	public OrientedBox3D getOBox() {
		OrientedBox3D obox = new OrientedBox3D(abox, getUp());
		return new OrientedBox3D(obox);
	}

	/**
	 * Enlarge to contain box
	 * @param box
	 */
	@Override
	public void bound(Point3D pt) {
		abox.bound(pt);
	}


	/**
	 * 
	 * @param new_size - new size with center in place
	 */
	public void resize(
		Vector3D size
	) {
		abox.resize(size);
		float radius = size2radius(size);
		setRadius(radius);
	}

	/**
	 * Setup location info
	 */
	public void setBox(EMBox3D box) {
		this.abox = new AlignedBox3D(box);
	}


	public void resize(
		int indexOfCornerToResize, Vector3D translation
	) {
		EMBox3D oldBox = getBox();
		box = new EMBox3D();

		// One corner of the new box will be the corner of the old
		// box that is diagonally opposite the corner being resized ...
		 box.bound( oldBox.getCorner( indexOfCornerToResize ^ 7 ) );

		// ... and the other corner of the new box will be the
		// corner being resized, after translation.
		box.bound( Point3D.sum( oldBox.getCorner( indexOfCornerToResize ), translation ) );
	}

	// Overridden when necessary
	public void translate(Vector3D translation ) {
		SmTrace.lg(String.format("ColoredBox.translation(%s)", translation), "boxdebug");
		abox.translate(translation);
		
	}

	
	/**
	 * Get base point - currently min x,y,z
	 * @param point
	 */
	public Point3D getBasePoint() {
		EMBox3D box = getBox();
		return box.getMin();
	}
	

	/**
	 * Move center to new point
	 * @param point
	 */
	public void moveTo(Point3D point ) {
		box.setPosition(point);
	}
	

	/**
	 * Move base corner to new point
	 * @param point
	 */
	public void moveBaseTo(Point3D point ) {
		EMBox3D oldBox = getBox();
		Vector3D vm = Point3D.diff(point, oldBox.getMin());
		translate(vm);
	}

	/**
	 * In progress
	 */
	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		OrientedBox3D obox = this.getOBox();
		return obox.intersects(ray, intersection, normalAtIntersection);
	}
	
}
