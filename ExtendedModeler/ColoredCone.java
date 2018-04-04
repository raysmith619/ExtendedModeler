package ExtendedModeler;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import com.jogamp.opengl.util.gl2.GLUT;

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
	
	public float getHeight() {
		return height;
	}
	
	public float getWidth() {
		return rBase*2;
	}

	/**
	 * Adjust dimensions in local orientation
	 */
	public void adjSize(float adjX, float adjY, float adjZ) {
		Vector3D size = getSize();
		float sizex = size.x() * adjX;
		float sizey = size.y() * adjY;
		float sizez = size.z() * adjZ;
		resize(new Vector3D(sizex, sizey, sizez));
	}
	
	
	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		float h = getHeight();
		float r = getWidth();
		SmTrace.lg("drawCone", "draw");
		if (SmTrace.trace("drawloc")) {
			SmTrace.lg(String.format("drawLoc %d %s center: %s corner0: %s",
				-1, "cone", box.getCenter(), box.getCorner(0)));	
		}
		GL2 gl = (GL2) drawable.getGL();
		glp = gl;				// For tracking gl.operations
		if ( drawAsWireframe ) {
			drawHilighted(drawable);
			 return;
		}
		else {
			SmTrace.lg("drawCone body", "draw");
			Point3D center = getCenter();

			float bx = center.x();
			///float by = center.y()-h/2;		// gl drawing definition
			float by = center.y();		// center 
			float bz = center.z();
			gl.glPushMatrix();
			gl.glTranslatef(bx, by, bz);
			Vector3D obj_up = getUp();
			Vector3D center_cone = Vector3D.mult(obj_up, -h/2);
			gl.glTranslatef(center_cone.x(), center_cone.y(), center_cone.z());
			EMBox3D.rotate2v(drawable, EMBox3D.GL_UP, obj_up);
			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
			ColoredCone.setMaterial(gl, getColor());
			GLU glu = new GLU();
			GLUquadric cylinder = glu.gluNewQuadric();
			glu.gluCylinder(cylinder, rBase, 0, h, nLongitudes, nLatitudes);
			gl.glPopAttrib();
			EMBox3D.rotate2vRet(drawable);
			gl.glPopMatrix();
		}
	}

	
	public Vector3D getSize() {
		float width = 2*rBase;
		return new Vector3D(width, height, width);
	}



static public void drawCone(
	GLAutoDrawable drawable,
	ColoredCone ccone,
	boolean expand,
	boolean drawAsWireframe,
	boolean cornersOnly
) {
	drawCone(drawable, ccone, expand, drawAsWireframe, cornersOnly);
}
	

	/**
	 * Generate an image icon for create button
	 * @param gl
	 * @param width
	 * @param height
	 * @return
	 */
	static public ImageIcon imageIcon(
		int width,
		int height
	) {
		String icon_name = ControlOfComponent.iconFileName("cone.jpg");
		File file = new File(icon_name);
		
		String basename = file.getName();
		Icon icon = new ImageIcon(icon_name);
		icon = ControlOfComponent.resizeIcon((ImageIcon)icon, width, height);
		return (ImageIcon) icon;
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
