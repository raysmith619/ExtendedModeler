package ExtendedModeler;

/**
 *  This class is for storing vector-aligned boxes.
 *  Patterned on AlignedBox3D
 * @author raysm
 *
 */
public class OrientedBox3D {
	private Vector3D up;				// Box orientation, in world coordinates
										// null - world's up
	private AlignedBox3D abox = new AlignedBox3D();			// Box aligned in world coordinates

	public OrientedBox3D() {
	}
	
	/**
	 * Deep copy, allowing modifications
	 */
	public OrientedBox3D(OrientedBox3D obox) {
		this(obox.abox, obox.up);
	}
	
	
	public OrientedBox3D(AlignedBox3D abox, Vector3D up) {
		this.abox = new AlignedBox3D(abox);
		if (up == null)
			up = EMBox3D.UP;
		this.up = new Vector3D(up);
	}

	/**
	 * Create Oriented box
	 * @param width (x)
	 * @param height (y)
	 * @param depth (z)
	 * @param up
	 */
	public OrientedBox3D(float width, float height, float depth, Point3D center, Vector3D up) {
		this.abox = new AlignedBox3D(width, height, depth, center);
		this.up = up;
	}

	/**
	 * Get standard dimensions
	 */
	public float getDepth() {
		return abox.getDepth();
	}
	public float getHeight() {
		return abox.getHeight();
	}
	public float getWidth() {
		return abox.getWidth();
	}
	/**
	 * Get local box - without regard to rotation
	 * no copy
	 */
	public AlignedBox3D getAlignedBox() {
		return abox;
	}
	
	
	/**
	 * Oriented box, given lower(min), upper(max) corners
	 * A rotated Aligned box
	 * @param min
	 * @param max
	 */
	public OrientedBox3D(Point3D min, Point3D max) {
		Vector3D diag = Point3D.diff(max, min);
		
	}

	public OrientedBox3D(Point3D center, Vector3D size, Vector3D up) {
		this(new AlignedBox3D(center, size), up);
	}

	public boolean isEmpty() { return abox.isEmpty(); }
	public void clear() { abox.clear(); }

	/**
	 * "points" with minimum x,y,z (global coordinates)
	 * Not necessarily on the box
	 */
	public float getMinX() {
		float min = getCorner(0).x();
		for (int ic = 1; ic < 8; ic++) {
			float val = getCorner(ic).x();
			if (val < min)
				min = val;
		}
		return min;
	}
	
	public float getMinY() {
		float min = getCorner(0).y();
		for (int ic = 1; ic < 8; ic++) {
			float val = getCorner(ic).y();
			if (val < min)
				min = val;
		}
		return min;
	}

	public float getMinZ() {
		float min = getCorner(0).z();
		for (int ic = 1; ic < 8; ic++) {
			float val = getCorner(ic).z();
			if (val < min)
				min = val;
		}
		return min;
	}

	/**
	 * "points" with minimum x,y,z (global coordinates)
	 * Not necessarily on the box
	 */
	public float getMaxX() {
		float max = getCorner(0).x();
		for (int ic = 1; ic < 8; ic++) {
			float val = getCorner(ic).x();
			if (val > max)
				max = val;
		}
		return max;
	}
	
	public float getMaxY() {
		float max = getCorner(0).y();
		for (int ic = 1; ic < 8; ic++) {
			float val = getCorner(ic).y();
			if (val > max)
				max = val;
		}
		return max;
	}

	public float getMaxZ() {
		float max = getCorner(0).z();
		for (int ic = 1; ic < 8; ic++) {
			float val = getCorner(ic).z();
			if (val > max)
				max = val;
		}
		return max;
	}

	public Point3D getMin() { return new Point3D(getMinX(), getMinY(), getMinZ()); }
	public Point3D getMax() { return new Point3D(getMaxX(), getMaxY(), getMaxZ()); }
	public Vector3D getDiagonal() { return Point3D.diff(getMin(), getMax()); }
	public float getRadius() { return getDiagonal().length()/2; }
	public Point3D getCenter() {
		return abox.getCenter();
	}

	
	/**
	 * Box enclosing sphere
	 * @param p
	 */
	public OrientedBox3D(EMBox3D sphere) {
		Point3D center = sphere.getCenter();
		float radius = sphere.getRadius();
		Point3D p0 = new Point3D(center.x()-radius, center.y()-radius, center.z()-radius);
		Point3D p1 = new Point3D(center.x()+radius, center.y()+radius, center.z()+radius);
		this.abox = new AlignedBox3D(p0, p1);
	}
	
	
	// Enlarge the box as necessary to contain the given point
	public void bound( Point3D p ) {
		abox.bound(p);		/// TBD - approximation - workaround
	}

	// Enlarge the box as necessary to contain the given box
	public void bound( OrientedBox3D obox ) {
		bound( obox.getMin());
		bound( obox.getMax());
	}

	// Enlarge the box as necessary to contain the given block
	public OrientedBox3D boundingBox() {
		return this;
	}

	// if we are a box it is us
	public OrientedBox3D getBox() {
		return this;
	}

	
	/**
	 * Compare box 
	 * @param p
	 * @return -1, 0, 1
	 */
	public int cmp(OrientedBox3D box2) {
		
		int boxcmp = abox.cmp(box2.abox);
		if (boxcmp != 0)
			return boxcmp;
		
		float updiff = Vector3D.diff(getUp(), EMBox3D.UP).length();
		
		int upcmp = (int) Math.signum(updiff);
		return upcmp;
	}
	
	
	public boolean contains( Point3D p ) {
		Point3D center = getCenter();
		float radius = getRadius();
		if (Point3D.diff(center, p).length() > radius)
			return false;				// Can't be - too far

		Point3D p0 = getMin();			/// TBD - approximation!!!
		Point3D p1 = getMax();
		return !isEmpty()
			&& p0.x() <= p.x() && p.x() <= p1.x()
			&& p0.y() <= p.y() && p.y() <= p1.y()
			&& p0.z() <= p.z() && p.z() <= p1.z();
	}

	/**
	 * Orient point (translate around center of box)
	 * @return
	 */
	public Point3D orient(Point3D pt) {
		Point3D center = abox.getCenter();
		Vector3D up = getUp();
		Point3D new_pt = EMBox3D.orient(pt, EMBox3D.UP, up, center);
		return new_pt;
	}

	public Vector3D getUp() {
		if (up == null)
			up = EMBox3D.UP;
		return up;
	}

	/**
	 * Get corner in global coordinates
	 * @param i
	 * @return
	 */
	public Point3D getCorner(int i) {
		Point3D pt = abox.getCorner(i);
		Point3D pto = orient(pt);
		return pto;
	}

	// Return the corner that is farthest along the given direction.
	public Point3D getExtremeCorner( Vector3D v ) {
		Point3D p0 = getMin();		/// TBD !!! approximation / workaround
		Point3D p1 = getMax();
		
		return new Point3D(
			v.x() > 0 ? p1.x() : p0.x(),
			v.y() > 0 ? p1.y() : p0.y(),
			v.z() > 0 ? p1.z() : p0.z()
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
		/**
		 * Workaround - use aligned box
		 * 
		 */
		AlignedBox3D abox = getAlignedBox();
		if (abox.intersects(ray, intersection, normalAtIntersection)) {
			return abox.intersects(ray, intersection, normalAtIntersection);
		}
			
		// We compute a bounding sphere for the box.
		// If the ray intersects the bounding sphere,
		// it *may* intersect the box.
		// If the ray does NOT intersect the bounding sphere,
		// then it cannot intersect the box.
		if ( ! new Sphere( getCenter(), getRadius()).intersects(
			ray, intersection, true
		) ) {
			return false;
		}

		boolean intersectionDetected = false;
		float distance = 0;
		Point3D p0 = getMin();			/// TBD an approximation/workaround
		Point3D p1 = getMax();
		// candidate intersection
		float candidateDistance;
		Point3D candidatePoint;

		if ( ray.direction.x() != 0 ) {
			candidateDistance = -(ray.origin.x() - p0.x())/ray.direction.x();
			if (
				distance>=0
				&& (!intersectionDetected || candidateDistance<distance)
			) {
				candidatePoint = ray.point( candidateDistance );
				if (p0.y()<=candidatePoint.y() && candidatePoint.y()<=p1.y()
					&& p0.z()<=candidatePoint.z() && candidatePoint.z()<=p1.z() ) {
					distance = candidateDistance;
					intersection.copy( candidatePoint );
					normalAtIntersection.copy( new Vector3D( -1, 0, 0 ) );
					intersectionDetected = true;
				}
			}
			candidateDistance = -(ray.origin.x() - p1.x())/ray.direction.x();
			if (
				distance>=0
				&& (!intersectionDetected || candidateDistance<distance)
			) {
				candidatePoint = ray.point( candidateDistance );
				if (p0.y()<=candidatePoint.y() && candidatePoint.y()<=p1.y()
					&& p0.z()<=candidatePoint.z() && candidatePoint.z()<=p1.z() ) {
					distance = candidateDistance;
					intersection.copy( candidatePoint );
					normalAtIntersection.copy( new Vector3D( 1, 0, 0 ) );
					intersectionDetected = true;
				}
			}
		}
		if ( ray.direction.y() != 0 ) {
			candidateDistance = -(ray.origin.y() - p0.y())/ray.direction.y();
			if (
				distance>=0
				&& (!intersectionDetected || candidateDistance<distance)
			) {
				candidatePoint = ray.point( candidateDistance );
				if (p0.x()<=candidatePoint.x() && candidatePoint.x()<=p1.x()
					&& p0.z()<=candidatePoint.z() && candidatePoint.z()<=p1.z() ) {
					distance = candidateDistance;
					intersection.copy( candidatePoint );
					normalAtIntersection.copy( new Vector3D( 0, -1, 0 ) );
					intersectionDetected = true;
				}
			}
			candidateDistance = -(ray.origin.y() - p1.y())/ray.direction.y();
			if (
				distance>=0
				&& (!intersectionDetected || candidateDistance<distance)
			) {
				candidatePoint = ray.point( candidateDistance );
				if (p0.x()<=candidatePoint.x() && candidatePoint.x()<=p1.x()
					&& p0.z()<=candidatePoint.z() && candidatePoint.z()<=p1.z() ) {
					distance = candidateDistance;
					intersection.copy( candidatePoint );
					normalAtIntersection.copy( new Vector3D( 0, 1, 0 ) );
					intersectionDetected = true;
				}
			}
		}
		if ( ray.direction.z() != 0 ) {
			candidateDistance = -(ray.origin.z() - p0.z())/ray.direction.z();
			if (
				distance>=0
				&& (!intersectionDetected || candidateDistance<distance)
			) {
				candidatePoint = ray.point( candidateDistance );
				if (p0.y()<=candidatePoint.y() && candidatePoint.y()<=p1.y()
					&& p0.x()<=candidatePoint.x() && candidatePoint.x()<=p1.x() ) {
					distance = candidateDistance;
					intersection.copy( candidatePoint );
					normalAtIntersection.copy( new Vector3D( 0, 0, -1 ) );
					intersectionDetected = true;
				}
			}
			candidateDistance = -(ray.origin.z() - p1.z())/ray.direction.z();
			if (
				distance>=0
				&& (!intersectionDetected || candidateDistance<distance)
			) {
				candidatePoint = ray.point( candidateDistance );
				if (p0.y()<=candidatePoint.y() && candidatePoint.y()<=p1.y()
					&& p0.x()<=candidatePoint.x() && candidatePoint.x()<=p1.x() ) {
					distance = candidateDistance;
					intersection.copy( candidatePoint );
					normalAtIntersection.copy( new Vector3D( 0, 0, 1 ) );
					intersectionDetected = true;
				}
			}
		}
		return intersectionDetected;

	}

	
	/**
	 * Resize in local orientation
	 */
	public void resize(Vector3D size) {
		abox.resize(size);
	}

	/**
	 * Adjust dimensions in local orientation
	 */
	public void adjSize(float adjX, float adjY, float adjZ) {
		Vector3D size = abox.getSize();
		float sizex = size.x() * adjX;
		float sizey = size.y() * adjY;
		float sizez = size.z() * adjZ;
		abox.resize(new Vector3D(sizex, sizey, sizez));
	}
		

	public String toString() {
		Point3D p0 = abox.getMin();		/// better?
		Point3D p1 = abox.getMax();
		return String.format("p0: %s  p1: %s", p0, p1, up);
	}

	public void translate(Vector3D translation) {
		abox.translate(translation);
	}
}

