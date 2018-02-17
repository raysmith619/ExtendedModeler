package ExtendedModeler;

import smTrace.SmTrace;

// This class is for storing axis-aligned boxes.
public class AlignedBox3D {

	private boolean isEmpty = true;

	// diagonally opposite corners
	private Point3D p0 = new Point3D(0,0,0);
	private Point3D p1 = new Point3D(0,0,0);

	public AlignedBox3D() {
	}

	/**
	 * Create box with standard dimension specifications
	 */
	public AlignedBox3D(float width, float height, float depth, Point3D center) {
		this(center, new Vector3D(width, height, depth));
	}
	
	
	public AlignedBox3D(float width, float height, float depth) {
		this(width, height, depth, new Point3D(0,0,0));
	}

	
	/**
	 * Deep copy, allowing modifications
	 */
	public AlignedBox3D(AlignedBox3D box) {
		this.isEmpty = box.isEmpty;
		this.p0 = new Point3D(box.p0.x(), box.p0.y(), box.p0.z());
		this.p1 = new Point3D(box.p1.x(), box.p1.y(), box.p1.z()); 
	}

	/**
	 * Box from size and center
	 */
	public AlignedBox3D(Point3D center, Vector3D size) {
		this.p0 = new Point3D(center.x()-size.x()/2, center.y()-size.y()/2, center.z()-size.z()/2);
		this.p1 = new Point3D(center.x()+size.x()/2, center.y()+size.y()/2, center.z()+size.z()/2);
	}
	
	public AlignedBox3D( Point3D min, Point3D max ) {
		/*** If assert is not enabled
		assert min.x() <= max.x() : "bounds error";
		assert min.y() <= max.y() : "bounds error";
		assert min.z() <= max.z() : "bounds error";
		***/
		if (min.x() > max.x())
			SmTrace.lg(String.format("AlignedBox3D min.x(%f) > max.x(%f)",
				min.x(), max.x()));
		if (min.y() > max.y())
			SmTrace.lg(String.format("AlignedBox3D min.x(%f) > max.x(%f)",
				min.y(), max.x()));
		if (min.z() > max.z())
			SmTrace.lg(String.format("AlignedBox3D min.x(%f) > max.x(%f)",
				min.z(), max.z()));
		p0.copy( min );
		p1.copy( max );
		isEmpty = false;
	}

	public AlignedBox3D(Vector3D size) {
		this(new Point3D(0, 0, 0), new Point3D(size.x(), size.y(), size.z()));
	}

	/**
	 * Get standard dimensions
	 */
	public float getDepth() {
		Vector3D size = getSize();		// May want to optimize TBD
		return size.z();
	}
	public float getHeight() {
		Vector3D size = getSize();
		return size.y();
	}
	public float getWidth() {
		Vector3D size = getSize();
		return size.z();
	}

	public boolean isEmpty() { return isEmpty; }
	public void clear() { isEmpty = true; }

	public Point3D getMin() { return p0; }
	public Point3D getMax() { return p1; }

	/**
	 * Get size (non zero) xyz dimensions
	 * @return
	 */
	public Vector3D getSize() {
		Vector3D diag = getDiagonal();
		return new Vector3D(Math.abs(diag.x()), Math.abs(diag.y()), Math.abs(diag.z()));
	}
	
	
	public Vector3D getDiagonal() { return Point3D.diff(p1,p0); }
	public Point3D getCenter() {
		return Point3D.average( p0, p1 );
	}

	
	/**
	 * Box enclosing sphere
	 * @param p
	 */
	public AlignedBox3D(EMBox3D sphere) {
		Point3D center = sphere.getCenter();
		float radius = sphere.getRadius();
		p0 = new Point3D(center.x()-radius, center.y()-radius, center.z()-radius);
		p1 = new Point3D(center.x()+radius, center.y()+radius, center.z()+radius);
	}
	
	
	// Enlarge the box as necessary to contain the given point
	public void bound( Point3D p ) {
		if ( isEmpty ) {
			p0.copy(p);
			p1.copy(p);
			isEmpty = false;
		}
		else {
			if ( p.x() < p0.x() )
				p0.p[0] = p.x();
			else if ( p.x() > p1.x() )
				p1.p[0] = p.x();

			if ( p.y() < p0.y() )
				p0.p[1] = p.y();
			else if ( p.y() > p1.y() )
				p1.p[1] = p.y();

			if ( p.z() < p0.z() )
				p0.p[2] = p.z();
			else if ( p.z() > p1.z() )
				p1.p[2] = p.z();
		}
	}

	// Enlarge the box as necessary to contain the given box
	public void bound( AlignedBox3D box ) {
		bound( box.p0 );
		bound( box.p1 );
	}

	// Enlarge the box as necessary to contain the given block
	public AlignedBox3D boundingBox() {
		return this;
	}

	// if we are a box it is us
	public AlignedBox3D getBox() {
		return this;
	}

	
	/**
	 * Compare box 
	 * @param p
	 * @return -1, 0, 1
	 */
	public int cmp(AlignedBox3D box2) {
		
		int p0cmp = p0.cmp(box2.p0);
		if (p0cmp != 0)
			return p0cmp;
		
		int p1cmp = p1.cmp(box2.p1);
		if (p1cmp != 0)
			return p1cmp;
		
		return 0;

	}
	
	
	public boolean contains( Point3D p ) {
		return !isEmpty
			&& p0.x() <= p.x() && p.x() <= p1.x()
			&& p0.y() <= p.y() && p.y() <= p1.y()
			&& p0.z() <= p.z() && p.z() <= p1.z();
	}

	public Point3D getCorner(int i) {
		return new Point3D(
			((i & 1)!=0) ? p1.x() : p0.x(),
			((i & 2)!=0) ? p1.y() : p0.y(),
			((i & 4)!=0) ? p1.z() : p0.z()
		);
	}

	// Return the corner that is furthest along the given direction.
	public Point3D getExtremeCorner( Vector3D v ) {
		return new Point3D(
			v.x() > 0 ? p1.x() : p0.x(),
			v.y() > 0 ? p1.y() : p0.y(),
			v.z() > 0 ? p1.z() : p0.z()
		);
	}

	// Return the index of the corner that
	// is furthest along the given direction.
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
		// We compute a bounding sphere for the box.
		// If the ray intersects the bounding sphere,
		// it *may* intersect the box.
		// If the ray does NOT intersect the bounding sphere,
		// then it cannot intersect the box.
		if ( ! new Sphere( getCenter(), Point3D.diff(p1,p0).length() / 2 ).intersects(
			ray, intersection, true
		) ) {
			return false;
		}

		boolean intersectionDetected = false;
		float distance = 0;

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
	 * Resize with same center
	 */
	public void resize(Vector3D size) {
		Point3D center = getCenter();
		p0 = new Point3D(center.x()-size.x()/2, center.y()-size.y()/2, center.z()-size.z()/2);
		p1 = new Point3D(center.x()+size.x()/2, center.y()+size.y()/2, center.z()+size.z()/2);
	}

	
	/**
	 * translate in world's x,y,z
	 */
	public void translate(Vector3D translate) {
		p0 = Point3D.sum(p0, translate);
		p1 = Point3D.sum(p1, translate);
	}
	
	public String toString() {
		return String.format("p0: %s  p1: %s", p0, p1);
	}
}

