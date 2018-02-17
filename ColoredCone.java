package ExtendedModeler;
import java.awt.Color;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLUquadric;

import smTrace.SmTrace;

import com.jogamp.opengl.glu.GLU;

public class ColoredCone extends EMBlockBase {
	float height;			// Height - base to top point
	float rBase;			// Radius of base

	public String blockType() {
		return "cone";
	}


	/**
	 * Base object
	 */
	public ColoredCone(Point3D center, float height, float rBase, Color color, Vector3D up) {
		super(center, dim2radius(height, rBase), color, up);
		this.height = height;
		this.rBase = rBase;
	}

	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		return getBox().intersects(ray, intersection, normalAtIntersection);		
	}

	/**
	 * Basic dimensions to encompassing radius
	 * @param height
	 * @param rBase
	 * @return
	 */
	public static float dim2radius(float height, float rBase) {
		float radius = (float) Math.sqrt(height*height + rBase*rBase + rBase*rBase)/2;
		return radius;
		
	}
	/**
	 * Create new object, from controls
	 * @throws EMBlockError 
	 **/
	public static ColoredCone newBlock(ControlsOfScene controlsOfScene) throws EMBlockError {
		ControlOfColor ctc = (ControlOfColor)controlsOfScene.getControl("color");
		Color color = ctc.getColor();
		ControlOfScene ctl = controlsOfScene.getControl("placement");
		ControlOfPlacement cop = (ControlOfPlacement)ctl;
		Point3D center = cop.getPosition();
		float height = cop.getSizeY();
		float rBase = Math.max(cop.getSizeX()/2, cop.getSizeZ()/2);
		Vector3D up =  cop.getUp();
		return new ColoredCone(center, height, rBase, color, up);
	}
	
	public ColoredCone(
		EMBox3D box,
		Color color
	) {
		super(box, color);
		isOk = true;
	}

	/**
	 * Same iD
	 * @param cb_base
	 */
	public ColoredCone(EMBlockBase cb_base) {
		super(cb_base);
	}
	
	/**
	 * Get enclosing oriented box
	 */
	@Override
	public OrientedBox3D getOBox() {
		float width = rBase*2;
		float depth = width;
		OrientedBox3D obox = new OrientedBox3D(width, height, depth, getCenter(), getUp());
		return obox;
	}

	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		OrientedBox3D obox = getOBox();
		drawCone(drawable, obox, expand, drawAsWireframe, cornersOnly);
	}



	static public void drawCone(
		GLAutoDrawable drawable,
		OrientedBox3D obox,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		float h = obox.getHeight();
		float r = obox.getWidth();
		SmTrace.lg("drawCone", "draw");
		GL2 gl = (GL2) drawable.getGL();
		glp = gl;				// For tracking gl.operations
		if ( expand ) {
			obox.adjSize(1.1f, 1.1f, 1.1f);
		}
		if ( drawAsWireframe ) {
			AlignedBox3D abox = obox.getAlignedBox();
			EMBox3D.rotate2v(drawable, EMBox3D.UP, obox.getUp());
			if ( cornersOnly ) {
				gl_glBegin( GL.GL_LINES, "drawAsWireframe cornersOnly GL.GL_LINES" );
				for ( int dim = 0; dim < 3; ++dim ) {
					Vector3D v = Vector3D.mult( Point3D.diff(abox.getCorner(1<<dim),abox.getCorner(0)), 0.1f );
					for ( int a = 0; a < 2; ++a ) {
						for ( int b = 0; b < 2; ++b ) {
							int i = (a << ((dim+1)%3)) | (b << ((dim+2)%3));
							gl_glVertex3fv( abox.getCorner(i).get(), 0 );
							gl_glVertex3fv( Point3D.sum( abox.getCorner(i), v ).get(), 0 );
							i |= 1 << dim;
							gl_glVertex3fv( abox.getCorner(i).get(), 0 );
							gl_glVertex3fv( Point3D.diff( abox.getCorner(i), v ).get(), 0 );
						}
					}
				}
				gl_glEnd("cornersOnly");
			}
			else {
				gl_glBegin( GL.GL_LINE_STRIP, "drawAsWireframe" );
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
				gl_glEnd("GL.GL_LINE_STRIP, \"drawAsWireframe");
				gl_glBegin( GL.GL_LINES, "drawAsWireframe" );
					gl_glVertex3fv( abox.getCorner( 1 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 5 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 3 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 7 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 4 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 6 ).get(), 0 );
				gl_glEnd("GL.GL_LINES, \"drawAsWireframe\"");
			}
			EMBox3D.rotate2vRet(drawable);
		}
		else {
			SmTrace.lg("drawCone body", "draw");
			int nLongitudes = 20;
			int nLatitudes = nLongitudes;
			Point3D center = obox.getCenter();

			float bx = center.x();
			float by = center.y();
			float bz = center.z();
			gl.glTranslatef(bx, by, bz);
			EMBox3D.rotate2v(drawable, EMBox3D.UP, obox.getUp());
			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
			///***
			GLU glu = new GLU();
			GLUquadric cylinder = glu.gluNewQuadric();
			glu.gluCylinder(cylinder, r, 0, h, nLongitudes, nLatitudes);
			///***/
			///***
			gl_glBegin(GL.GL_LINES, "cone single line in z-axis");
			gl.glColor3d(1,1,1);
			gl_glVertex3fv(new Point3D(0,0,0).get(), 0);
			gl_glVertex3fv(new Point3D(0,0,1).get(), 0);
			gl_glVertex3fv(new Point3D(0,0,0).get(), 0);
			gl_glVertex3fv(new Point3D(.0f,.3f,.0f).get(), 0);
			gl_glVertex3fv(new Point3D(0,0,0).get(), 0);
			gl_glVertex3fv(new Point3D(.5f,0.0f,.0f).get(), 0);
			gl_glEnd("cone single line in z-axis");
			gl.glPopAttrib();
			///***/
			EMBox3D.rotate2vRet(drawable);
			gl.glTranslatef(-bx, -by, -bz);
///			glut.glutSolidSphere(r, nLongitudes, nLatitudes);
		}
	}

	/**
	 * Tracking / Debugging 
	 * @param v
	 * @param offset
	 */
	private static GL2 glp;				// for tracking routine
	private static String block = "cone";
	private static void gl_glVertex3fv(float [] v, int offset) {
		glp.glVertex3fv(v, offset);
		SmTrace.lg(String.format("%s glVertex3fv %.2f %.2f %.2f", block, v[0], v[1], v[2]), "drawvertex");
	}
	private static void gl_glBegin(int mode, String desc) {
		glp.glBegin(mode);
		SmTrace.lg(String.format("%s gl_glBegin(%s)", block, desc), "draw");
	}
	private static void gl_glEnd(String desc) {
		glp.glEnd();
		SmTrace.lg(String.format("%s gl_glEnd %s", block, desc), "draw");
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

	public Point3D getMin() {
		float xmin = box.getCenter().x()-rBase;
		float ymin = box.getCenter().y()-height/2;
		float zmin = box.getCenter().z()-rBase;
		return new Point3D(xmin, ymin, zmin);
	}

	public Point3D getMax() {
		float xmax = box.getCenter().x()+rBase;
		float ymax = box.getCenter().y()+height/2;
		float zmax = box.getCenter().z()+rBase;
		return new Point3D(xmax, ymax, zmax);
	}




	/**
	 * 
	 * @param new_size - new size with center in place
	 */
	public void resize(
		Vector3D size
	) {
		this.height = size.y();
		this.rBase = Math.max(size.x(),  size.x())/2;
		setRadius(dim2radius(this.height, this.rBase));
	}
	
	
}
