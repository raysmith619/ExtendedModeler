package ExtendedModeler;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import smTrace.SmTrace;

// This class is the basis for bounded oriented objects.
public class EMBox3D {

	///private boolean isEmpty = true;
	public static final Vector3D xaxis = new Vector3D(1,0,0);
	public static final Vector3D yaxis = new Vector3D(0,1,0);
	public static final Vector3D zaxis = new Vector3D(0,0,1);
	public static final Vector3D UP = yaxis;		// Pointing in y direction
	public static final Vector3D GL_UP = zaxis;	// GL (eg cylinder) idea of up
	
	Vector3D up;											// null == UP
	private Point3D center = new Point3D(0, 0, 0);
	private float radius = 0;				// Bounding radius
	public static float RADIUS = .5f;
	/// diagonally opposite corners
	///private Point3D p0 = new Point3D(0,0,0);
	///private Point3D p1 = new Point3D(0,0,0);

	public EMBox3D() {
		this(new Point3D(0,0,0), EMBox3D.RADIUS);
	}
	
	/**
	 * Deep copy, allowing modifications
	 */
	public EMBox3D(EMBox3D box) {
		if (box == null)
			box = new EMBox3D();
		setCenter(box.center);
		setRadius(box.radius);
		setUp(box.up);
	}
	
	
	public EMBox3D( Point3D center, float radius, Vector3D up ) {
		setCenter(center);
		setRadius(radius);
		setUp(up);
	}
	
	
	public EMBox3D( Point3D center, float radius) {
		this(center, radius, null);
	}
	
	
	public EMBox3D( Point3D center) {
		this(center, 0, null);
	}

	public EMBox3D(float radius) {
		this(new Point3D(0, 0, 0), radius);
	}

	public EMBox3D(Vector3D size) {
		this(size.length()/2);
	}
	
	/**
	 * Create from simple box
	 */
	public EMBox3D(ColoredBox cbox) {
		this(cbox.getCenter(), cbox.getRadius(), cbox.getUp());
	}
	
	
	/**
	 * absolute min,max based on global coordinates
	 * @return
	 */
	public Point3D getMin() {
		return new Point3D(getMinX(), getMinY(), getMinZ());
	}

	public float getMinX() {
		return center.x()-radius;
	}

	public float getMinY() {
		return center.y()-radius;
	}

	public float getMinZ() {
		return center.z()-radius;
	}
	public Point3D getMax() {
		return new Point3D(getMaxX(), getMaxY(), getMaxZ());
	}

	public float getMaxX() {
		return center.x()+radius;
	}

	public float getMaxY() {
		return center.y()+radius;
	}

	public float getMaxZ() {
		return center.z()+radius;
	}

	/**
	 * Diagonal based on global coordiantes
	 * @return
	 */
	public Vector3D getDiagonal() { return Point3D.diff(getMax(), getMin()); }

	// Enlarge the box as necessary to contain the given point
	public void bound( Point3D p ) {
		float dist = Point3D.diff(p, center).length();
		if (dist > radius)
			radius = dist;
	}

	// Enlarge the box as necessary to contain the given box
	public void bound( EMBox3D box ) {
		Vector3D vcenter2box = Point3D.diff(box.center, center);
		float cent2cent = vcenter2box.length();
		float newradius = cent2cent + box.radius;
		if (newradius < radius)
			return;					// box is enclosed
		
		radius = newradius;			// Enlarge to contain new sphere
	}

	// Enlarge the box as necessary to contain the given block
	public AlignedBox3D boundingBox() {
		AlignedBox3D bbox = new AlignedBox3D(this);
		return bbox;
	}

	// Enlarge the sphere as necessary to contain the given block
	public EMBox3D boundingSphere() {
		return this;
	}

	// if we are a box it is us
	public EMBox3D getBox() {
		return this;
	}

	
	/**
	 * Compare box 
	 * @param p
	 * @return -1, 0, 1
	 */
	public int cmp(EMBox3D box2) {
		
		int cx_cmp = Float.compare(center.x(), box2.center.x());
		if (cx_cmp != 0)
			return cx_cmp;
		
		int cy_cmp = Float.compare(center.y(), box2.center.y());
		if (cy_cmp != 0)
			return cy_cmp;
		
		int cz_cmp = Float.compare(center.z(), box2.center.z());
		if (cz_cmp != 0)
			return cz_cmp;

		int radius_cmp = Float.compare(radius, box2.radius);
		if (radius_cmp != 0)
			return radius_cmp;

		if (up == null) {
			if (box2.up == null)
				return 0;
			return -1;
		}
		if (box2.up == null) {
			return 1;
		}
		
		int upx_cmp = Float.compare(up.x(), box2.up.x());
		if (upx_cmp != 0)
			return cx_cmp;
		
		int upy_cmp = Float.compare(up.y(), box2.up.y());
		if (upy_cmp != 0)
			return upy_cmp;
		
		int upz_cmp = Float.compare(up.z(), box2.up.z());
		if (upz_cmp != 0)
			return upz_cmp;
		
		return 0;

	}
	
	
	public boolean contains( Point3D p ) {
		float c2p = Point3D.diff(center, p).length();
		if (c2p <= radius)
			return true;		// on edge or closer
		
		return false;
	}

	
	public static float defaultRadius() {
		return RADIUS;
	}
	
	
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

	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		// Our base is a sphere ( previously we were a box)
		if ( !new Sphere(center, radius).intersects(
			ray, intersection, true
		) ) {
			return false;
		}
		Vector3D v = new Vector3D(0,1,0);
		normalAtIntersection = v.normalized();
		SmTrace.lg(String.format("EMBox3D.intersects(ray:%s, intersection(%s), normalAtIntersection(%s)",
				ray, intersection, normalAtIntersection), "intersection");
		return true;
	}

	public void setCenter(Point3D center) {
		if (center == null)
			center = new Point3D(0,0,0);
		this.center = center;
	}

	public void setPosition(Point3D center) {
		setCenter(center);
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	
	public void translate(Vector3D translation) {
		this.center = new Point3D(center.x()+translation.x(),
								center.y()+translation.y(),
								center.z()+translation.z());
	}
	
	public String toString() {
		return String.format("r:%.2g c: %s up: %s", radius, center, up);
	}

	public Point3D getCenter() {
		return center;
	}
	
	
	public float getRadius() {
		return radius;
	}
	
	public Vector3D getUp() {
		if (up == null)
			return UP;			// Aligned - use UP
		
		return up;
	}

	
	/**
	 * Orient box
	 * null - default orientation - "global up"
	 */
	public void setUp(Vector3D up) {
		if (up == null)
			this.up = null;
		else
			this.up = up.normalized();
	}

	/**
	 * Rotate coordinates to our understanding of up (0,1,0) vs standard (0,0,1)
	 * Expect pop after such as rotate2vRet()
	 */
	public static void setOurUp(GLAutoDrawable drawable) {
		rotate2v(drawable, GL_UP, UP);
	}
	/**
	 * Orientation control for drawing
	 */
	
	/**
	 * Rotate from "up" to vector
	 */
	public static void rotate2v(
			GLAutoDrawable drawable,
			Vector3D v_from,
			Vector3D v_to) {
		GL2 gl = (GL2) drawable.getGL();
		final float rad2deg = (float) (180./Math.PI);
		float ax = Vector3D.computeSignedAngle(v_from, v_to, xaxis ) * rad2deg;
		float ay = Vector3D.computeSignedAngle(v_from, v_to, yaxis ) * rad2deg; 
		float az = Vector3D.computeSignedAngle(v_from, v_to, zaxis ) * rad2deg; 
		SmTrace.lg(String.format("v_from(%s) angle ax=%.1f, ay=%.1f, az=%.1f v_to=%s",
				v_from, ax, ay, az, v_to), "rotate");
		gl.glPushAttrib(GL2.GL_TRANSFORM_BIT);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		/***
		gl.glRotatef(ay, v_from.x(), v_from.y(), v_from.z());
		gl.glRotatef(az, v_from.x(), v_from.y(), v_from.z());
		gl.glRotatef(ax, v_from.x(), v_from.y(), v_from.z());
		***/
		gl.glRotatef(ax, xaxis.x(), xaxis.y(), xaxis.z());
		gl.glRotatef(ay, yaxis.x(), yaxis.y(), yaxis.z());
		gl.glRotatef(az, zaxis.x(), zaxis.y(), zaxis.z());
	}
	
	
	/**
	 * Return rotation from rotate2v
	 */
	
	public static void rotate2vRet(GLAutoDrawable drawable) {
		GL2 gl = (GL2) drawable.getGL();
		gl.glPopMatrix();
		gl.glPopAttrib();
		
	}
	
	/**
	 * Orient (rotate) point about given center
	 */
	public static Point3D orient(Point3D pt,
			Vector3D v_from,
			Vector3D v_to,
			Point3D center) {
		if (v_from.equals(v_to))
			return pt;					// Shortcut if equal
		
		float rad2deg = (float) (180./Math.PI);
		float ax = Vector3D.computeSignedAngle(v_from, v_to, xaxis ) * rad2deg;
		float ay = Vector3D.computeSignedAngle(v_from, v_to, yaxis ) * rad2deg; 
		float az = Vector3D.computeSignedAngle(v_from, v_to, zaxis ) * rad2deg;
		Vector3D to_pt = Point3D.diff(pt, center);
		Vector3D new_vec = EMBox3D.rotate(to_pt, ax, xaxis);
		new_vec = EMBox3D.rotate(new_vec, ay, yaxis);
		new_vec = EMBox3D.rotate(new_vec, az, zaxis);
		Point3D new_pt = Point3D.sum(center, new_vec);
		return new_pt;
	}
	
	/**
	 * rotate vector by angle around an aligned axis (1,0,0), (0,1,0), or (0,0,1)
	 */
	public static Vector3D rotate(Vector3D vec, float angle, Vector3D pole) {
		
		float x1 = vec.x();
		float y1 = vec.y();
		float z1 = vec.z();
		
		float px = pole.x();
		float py = pole.y();
		float pz = pole.z();
		
		float x2 = px;
		float y2 = py;
		float z2 = pz;
						// Test for alignment
		int ndim = 0;
		if (px > 0) {
			ndim++;
			y2 = (float) (Math.cos(angle) * y1);
			z2 = (float) (Math.sin(angle) * z1);
		}
		if (py > 0) {
			ndim++;
			x2 = (float) (Math.cos(angle) * x1);
			z2 = (float) (Math.sin(angle) * x1);
		}
		if (pz > 0) {
			ndim++;
			x2 = (float) (Math.cos(angle) * x1);
			y2 = (float) (Math.sin(angle) * y1);
		}
		if (ndim != 1) {
			SmTrace.lg(String.format("EMBox3D.rotate pole(%s not aligned",
					pole));
			return vec;
		}
		Vector3D new_vec = new Vector3D(x2, y2, z2);
		return new_vec;	
	}
	
}

