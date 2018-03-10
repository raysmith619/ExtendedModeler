package ExtendedModeler;
import java.awt.Color;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import smTrace.SmTrace;
/**
 * A simple abstract poin
 * Experiment / Demonstratee orientation
 * Three lines at orthogonal x,y,z orientation, with y being "up"
 * x,z pointing at base, the center of the object
 *
 */
public class ColoredPointer extends EMBlockBase {
	float x;
	float y;							// up direction, == radius
	float z;
	private boolean isOk = false;	// Set OK upon successful construction
	private Ray3D rayDisplayed;		// TFD - debugging

	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		/***
		// We Use the pointer's sphere directly.
		***/
		boolean ret = getBox().intersects(ray, intersection, normalAtIntersection);
		if (ret) {
			SmTrace.lg(String.format("ColoredEye intersection=%s, ray=%s, normalAtIntersection=%s)",
					intersection, ray, normalAtIntersection), "pointer");
		}
		return ret;		

	}
	
	/**
	 * Check if ok block
	 */

	public boolean isOk() {
		return isOk;
	}
	
	public ColoredPointer(
			Point3D position,
			float x,
			float y,
			float  z,
			Color color,
			Vector3D up
		) {
		super(position, y, color , up);	// Base is at center, y in "up" direction
		this.x = x;
		this.y = y;
		this.z = z;
		isOk = true;
	}
	
	public ColoredPointer(
		) {
		this(new Point3D(0,0,0), 1.f, .5f, .25f, null, null);
	}

	/**
	 * Same iD
	 * @param cb_base
	 */
	public ColoredPointer(EMBlockBase cb_base) {
		super(cb_base);
	}

	/**
	 * generic size
	 * @return
	 */
	public static float getWidth() {
		return .5f;
	}
	
	/**
	 * Get current radius
	 */
	public float getRadius() {
		return box.getRadius();
	}
	
	/**
	 * Get size - vector of x,y,z basic dimensions
	 */
	public Vector3D getSize() {
		
		Vector3D size = new Vector3D(x, y, z);
		return size;
	}
	
	
	/**
	 * Convert position to box
	 **/ 

	public  EMBox3D position2box(Point3D position) {
		if (position == null)
			position = new Point3D(0, 0, 0);
		Vector3D size = getSize();
		box = new EMBox3D(position, size.length()/2);
		return box;
	}

/**
 * 3 orthogonal axis lines with y pointing "up"	
 */
	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		GL2 gl = (GL2) drawable.getGL();
		Point3D center = getPosition();
		float bx = center.x();
		float by = center.y();
		float bz = center.z();
		gl.glPushMatrix();
		gl.glTranslatef(bx, by, bz);
		EMBox3D.rotate2v(drawable, EMBox3D.UP, getUp());
		
		EMViewedText tr3 = new EMViewedText(gl);
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		setEmphasis(gl, getColor());
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(x, 0, 0);
		gl.glEnd();
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		gl.glColor3f(1, 0, 0);
		tr3.draw("x", x, 0, 0);
		gl.glPopAttrib();

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, y, 0);
		gl.glEnd();
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		gl.glColor3f(0, 1, 0);
		tr3.draw("y - up", 0, y, 0);
		gl.glPopAttrib();

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, 0, z);
		gl.glEnd();
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		gl.glColor3f(0, 0, 1);
		tr3.draw("z", 0, 0, z);
		gl.glPopAttrib();

		clearEmphasis();
		gl.glPopAttrib();
		
		EMBox3D.rotate2vRet(drawable);
		gl.glPopMatrix();
	}

	public String blockType() {
		return "pointer";
	}



	static public void drawPointer(
		GLAutoDrawable drawable,
		EMBox3D box,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		GL2 gl = (GL2) drawable.getGL();
		Color color = new Color(.8f,.8f,.8f);
		ColoredBall.drawBall(drawable, box, color, expand, drawAsWireframe, cornersOnly);
	}


@Override
	public void translate(Vector3D translation ) {
		super.translate(translation);
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
		super.copy();
		return null;
	}

	
	/**
	 * Create new object, from controls
	 * @throws EMBlockError 
	 **/
	public static ColoredPointer newBlock(ControlsOfScene controlsOfScene) throws EMBlockError {
		ControlOfColor ctc = (ControlOfColor)controlsOfScene.getControl("color");
		Color color = ctc.getColor();
		ControlOfScene ctl = controlsOfScene.getControl("placement");
		ControlOfPlacement cop = (ControlOfPlacement)ctl;
		Point3D center = cop.getPosition();
		float x = cop.getSizeX();
		float y = cop.getSizeY();
		float z = cop.getSizeZ();
		Vector3D up =  cop.getUp();
		return new ColoredPointer(center, x, y, z, color, up);
	}

	/**
	 *  Textual representation
	 */
	@Override
	public String toString() {
		String str = String.format("[%d]%s viewerLevel:%d position: %s",
				iD, blockType(), getViewerLevel(), getPosition());
		return str;
	}
	

	
}
