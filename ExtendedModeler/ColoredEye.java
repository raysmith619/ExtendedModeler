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

public class ColoredEye extends EMBlockBase {
	SceneViewer sceneViewer;		// viewer if not null
	private boolean isOk = false;	// Set OK upon successful construction
	private Ray3D rayDisplayed;		// TFD - debugging

	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		/***
		// We Use the eye's sphere directly.
		Point3D center = getCenter();
		float radius = getRadius();
		SmTrace.lg(String.format("ColoredEye intersects ray=%s center=%s radius=%.2g",
				ray, center, radius));
		Vector3D ray_to_center = Point3D.diff(center, ray.origin);
		Vector3D direction_to_center = ray_to_center.normalized();
		SmTrace.lg(String.format("vector to eye center: %s(%s)", ray_to_center, direction_to_center));
		SmTrace.lg(String.format("ColoredEye intersects ray=%s center=%s radius=%.2g",
				ray, center, radius));
		showRay(ray);
		boolean intersects = new Sphere( center, radius).intersects(
			ray, intersection, true);
		if (intersects ) {
			SmTrace.lg(String.format("ColorEye intersects AT %s", intersection));
		}
		///intersects = true;	/// TFD - FORCE INTERSECTION
		if (!intersects) {
			return false;
		}
		SmTrace.lg("ColoredEye: intersects");
		Vector3D normal = sceneViewer.camera.up;
		normalAtIntersection.copy(direction_to_center);
		return true;
		***/
		boolean ret = getBox().intersects(ray, intersection, normalAtIntersection);
		if (ret) {
			SmTrace.lg(String.format("ColoredEye intersection=%s, ray=%s, normalAtIntersection=%s)",
					intersection, ray, normalAtIntersection));
		}
		return ret;		

	}

	
	/**
	 * Store last ray for display - debugging
	 */
	private void showRay(Ray3D ray) {
		this.rayDisplayed = ray;
	}
	
	/**
	 * Check if ok block
	 */

	public boolean isOk() {
		return isOk;
	}
	
	public ColoredEye(
			Point3D position,
			Point3D target,
			Vector3D up,
			SceneViewer sceneViewer		// if not null, Viewer with this eye
		) {
		super(position, target, up);
		this.box = null;				// force new calculation
		this.box = position2box(position);
		this.sceneViewer = sceneViewer;
		isOk = true;
	}
	
	public ColoredEye(
		) {
		this(null, null, null, null);
	}

	/**
	 * Same iD
	 * @param cb_base
	 */
	public ColoredEye(EMBlockBase cb_base) {
		super(cb_base);
	}

	/**
	 * generic size
	 * @return
	 */
	public static float getWidth() {
		return .6f;
	}
	
	/**
	 * Get current radius
	 */
	public float getRadius() {
		Vector3D size = getSize();
		float radius = size.x()/2;
		return radius;
	}
	
	
	/**
	 * Get size - vector of x,y,z extent
	 */
	@Override
	public Vector3D getSize() {
		if (box == null) {
			float width = getWidth();			// Use Default width
			Vector3D size = new Vector3D(width, width, width);
			return  size;
		}
		Point3D minp = getMin();
		Point3D maxp = getMax();		
		Vector3D size = Point3D.diff(maxp, minp);
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

	
	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		Point3D eye_pt = getPosition();
		Point3D eye_target = target;
		Vector3D eye_to_target = Point3D.diff(eye_target, eye_pt);
		Vector3D eye_dir = eye_to_target.normalized();
		
		GL2 gl = (GL2) drawable.getGL();
		gl.glColor4d(.303, .18, .16, .565);	// cone around eye with point away from target
		GLUT glut = new GLUT();
		Point3D center = new Point3D(eye_pt);
		float bx = center.x();
		float by = center.y();
		float bz = center.z();
		float rvx = eye_dir.x();
		float rvy = eye_dir.y();
		float rvz = eye_dir.z();
		Vector3D xaxis = new Vector3D(1,0,0);
		Vector3D yaxis = new Vector3D(0,1,0);
		Vector3D zaxis = new Vector3D(0,0,1);
		Vector3D coneup = new Vector3D(0,1,0);	///TBD - External up
		float rad2deg = (float) (180./Math.PI);
		float ax = Vector3D.computeSignedAngle(coneup, eye_dir, xaxis ) * rad2deg;
		float ay = Vector3D.computeSignedAngle(coneup, eye_dir, yaxis ) * rad2deg; 
		float az = Vector3D.computeSignedAngle(coneup, eye_dir, zaxis ) * rad2deg; 
		SmTrace.lg(String.format("eye_dir(%s) angle ax=%.1f, ay=%.1f, az=%.1f coneup=%s",
				eye_dir, ax, ay, az, coneup), "eye_dir");
		float r = getWidth();
		float h = 2*r;
		gl.glPushAttrib(GL2.GL_TRANSFORM_BIT);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glTranslatef(bx, by, bz);
		
		gl.glRotatef(ax, eye_dir.x(), eye_dir.y(), eye_dir.z());
		gl.glRotatef(ay, eye_dir.x(), eye_dir.y(), eye_dir.z());
		gl.glRotatef(az, eye_dir.x(), eye_dir.y(), eye_dir.z());
		///gl.glRotatef(ax, 1, 0, 0);
		///gl.glRotatef(ay, 0, 1, 0);
		///gl.glRotatef(az, 0, 0, 1);
		glut.glutSolidCone(r, h, nLongitudes, nLatitudes);
//		gl.glRotatef(-45, rvx, rvy, rvz);
//		gl.glTranslatef(-bx, -by, -bz);
		gl.glPopMatrix();
		gl.glPopAttrib();

		Color color = new Color(.8f,.8f,.8f);
		ColoredBall.drawBall(drawable, box, color, expand, drawAsWireframe, cornersOnly);
		if (rayDisplayed != null) {
			gl.glColor3d(0,.5, 0);
			gl.glBegin(GL2.GL_LINES);
			gl.glVertex3f(rayDisplayed.origin.x(),
					rayDisplayed.origin.y(),
					rayDisplayed.origin.z());
			Point3D rayEnd = new Point3D(rayDisplayed.origin);
			rayEnd = Point3D.sum(rayEnd, Vector3D.mult(rayDisplayed.direction, 10f));
			
			gl.glVertex3f(rayEnd.x(), rayEnd.y(), rayEnd.z());
			gl.glEnd();
		}

		gl.glColor3d(.5,.5,.5);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3f(eye_pt.x(), eye_pt.y(), eye_pt.z());
		gl.glVertex3f(eye_target.x(), eye_target.y(), eye_target.z());
		gl.glEnd();
	}

	public String blockType() {
		return "eye";
	}



	static public void drawEye(
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
		Point3D position = getPosition();
		if (sceneViewer != null) {
			sceneViewer.eyeAt(position);
		}
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
	 *  Textual representation
	 */
	@Override
	public String toString() {
		String str = String.format("[%d]%s viewerLevel:%d position: %s",
				iD, blockType(), getViewerLevel(), getPosition());
		return str;
	}
	

	
}
