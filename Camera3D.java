package ExtendedModeler;

import java.awt.Color;

import com.jogamp.opengl.GL2;

public class Camera3D {
	private Point3D initialPosition;		// Initial setup
	private Point3D initialTarget;
	private Vector3D initialUp;
	
	static final public float fieldOfViewInDegrees = 30;

	static final public float orbitingSpeedInDegreesPerRadius = 300;

	// These are in world-space units.
	static final public float nearPlane = 1;

	static final public float farPlane = 10000;

	// During dollying (i.e. when the camera is translating into
	// the scene), if the camera gets too close to the target
	// point, we push the target point away.
	// The threshold distance at which such "pushing" of the
	// target point begins is this fraction of nearPlane.
	// To prevent the target point from ever being clipped,
	// this fraction should be chosen to be greater than 1.0.
	static final public float pushThreshold = 1.3f;

	// We give these some initial values just as a safeguard
	// against division by zero when computing their ratio.
	private int viewportWidthInPixels = 10;

	private int viewportHeightInPixels = 10;

	private float viewportRadiusInPixels = 5;

	private float sceneRadius = 10;

	// point of view, or center of camera; the ego-center; the eyeAt-point
	public Point3D position;

	// point of interest; what the camera is looking at; the exo-center
	public Point3D target;

	// This is the up vector for the (local) camera space
	public Vector3D up;

	// This is the up vector for the (global) world space;
	// it is perpendicular to the horizontal (x,z)-plane
	final public Vector3D ground = new Vector3D(0, 1, 0);

	public Camera3D() {
		reset();
	}

	public Camera3D(Point3D position, Point3D target, Vector3D up) {
		initialPosition = position;
		initialTarget = target;
		initialUp = up;
		reset();
	}
	public void reset() {
		sceneRadius = 10;			// Default
		if (initialPosition != null && initialTarget != null) {
			sceneRadius = Point3D.diff(initialPosition, initialTarget).length();
		}
		//float tangent = (float)Math.tan( fieldOfViewInDegrees/2 / 180 * (float)Math.PI );
		//float distanceFromTarget = sceneRadius / tangent;
		if (initialPosition == null) {
			///initialPosition = new Point3D(0, 0, distanceFromTarget );
			initialPosition = new Point3D(0, 0, sceneRadius);
		}
		if (initialTarget == null) {
			initialTarget = new Point3D(0, 0, 0);			
		}
		if (initialUp == null) {
			initialUp = ground;
		}
		position = initialPosition;
		target = initialTarget;
		up = initialUp;
	}

	
	/**
	 * Setup view
	 * If parameter(s) are null use current value, default if null
	 */
	public void setView(Point3D position, Point3D target, Vector3D up) {
		if (position == null)
			position = this.position;
		else
			this.position = position;
		if (target == null)
			target = this.target;
		else
			this.target = target;
		if (up == null)
			up = this.up;
		else
			this.up = up;
	}

	/**
	 * 
	 * @param widthInPixels
	 * @param heightInPixels
	 * Sets radius to .5 * shorter dimension
	 */
	public void setViewportDimensions( int widthInPixels, int heightInPixels ) {
		viewportWidthInPixels = widthInPixels;
		viewportHeightInPixels = heightInPixels;
		viewportRadiusInPixels = widthInPixels < heightInPixels
			? 0.5f*widthInPixels : 0.5f*heightInPixels;
	}
	public int getViewportWidth() { return viewportWidthInPixels; }
	public int getViewportHeight() { return viewportHeightInPixels; }
	public void setSceneRadius( float radius ) {
		sceneRadius = radius;
	}

	/**
	 * Get Viewing box, including camera, target area under view
	 * @return
	 */
	public ColoredBox getViewBox() {
		Vector3D p2target = Point3D.diff(target, position);
		Point3D oside = Point3D.sum(target, p2target);
		Vector3D p2up = Vector3D.mult(up, p2target.length());
		Vector3D p2side = Vector3D.cross(p2target, p2up.normalized());
		Point3D position_ulc = Point3D.diff(position, p2up);
		position_ulc = Point3D.diff(position_ulc, p2side);
		Point3D oside_lrc = Point3D.sum(oside, p2up);
		oside_lrc = Point3D.sum(oside_lrc, p2side);
		/**
		 * Need to bound to ensure p0, p1 values
		 */
		
		ColoredBox vbox = new ColoredBox(
				target,
				null,
				Color.BLUE,
				up);

		vbox.bound(position_ulc);
		vbox.bound(oside_lrc);
		
		return vbox;
	}
	
	
	public void transform( GL2 gl ) {
		float tangent = (float)Math.tan( fieldOfViewInDegrees/2 / 180 * (float)Math.PI );
		float viewportRadius = nearPlane * tangent;
		float viewportWidth, viewportHeight;
		if ( viewportWidthInPixels < viewportHeightInPixels ) {
			viewportWidth = 2.0f*viewportRadius;
			viewportHeight = viewportWidth
				* viewportHeightInPixels / (float)viewportWidthInPixels;
		} else {
			viewportHeight = 2.0f * viewportRadius;
			viewportWidth = viewportHeight
				* viewportWidthInPixels / (float)viewportHeightInPixels;
		}
		gl.glFrustum(
			- 0.5f * viewportWidth,  0.5f * viewportWidth,    // left, right
			- 0.5f * viewportHeight, 0.5f * viewportHeight,   // bottom, top
			nearPlane, farPlane
		);

		//glu.gluPerspective(
		//	fieldOfViewInDegrees,
		//	viewportWidthInPixels
		//		/ (float)viewportHeightInPixels,
		//	nearPlane,
		//	farPlane
		//);

		Matrix4x4 M = new Matrix4x4();
		M.setToLookAt(position, target, up, false);
		gl.glMultMatrixf(M.m, 0);
	}

	// Causes the camera to "orbit" around the target point.
	// This is also called "tumbling" in some software packages.
	public void orbit(
		float old_x_pixels, float old_y_pixels,
		float new_x_pixels, float new_y_pixels
	) {
		float pixelsPerDegree = viewportRadiusInPixels
			/ orbitingSpeedInDegreesPerRadius;
		float radiansPerPixel = 1
			/ pixelsPerDegree * (float)Math.PI / 180;

		Vector3D t2p = Point3D.diff(position, target);

		Matrix4x4 M = new Matrix4x4();
		M.setToRotation(
			(old_x_pixels-new_x_pixels) * radiansPerPixel,
			ground
		);
		t2p = Matrix4x4.mult(M, t2p);
		up = Matrix4x4.mult(M, up);
		Vector3D right = (Vector3D.cross(up, t2p)).normalized();
		M.setToRotation(
			(old_y_pixels-new_y_pixels) * radiansPerPixel,
			right
		);
		t2p = Matrix4x4.mult(M, t2p);
		up = Matrix4x4.mult(M, up);
		position = Point3D.sum(target, t2p);
	}

	// This causes the scene to appear to translate right and up
	// (i.e., what really happens is the camera is translated left and down).
	// This is also called "panning" in some software packages.
	// Passing in negative delta values causes the opposite motion.
	public void translateSceneRightAndUp(
		float delta_x_pixels, float delta_y_pixels
	) {
		Vector3D direction = Point3D.diff(target, position);
		float distanceFromTarget = direction.length();
		direction = direction.normalized();

		float translationSpeedInUnitsPerRadius =
			distanceFromTarget * (float)Math.tan( fieldOfViewInDegrees/2 / 180 * (float)Math.PI );
		float pixelsPerUnit = viewportRadiusInPixels
			/ translationSpeedInUnitsPerRadius;

		Vector3D right = Vector3D.cross(direction, up);

		Vector3D translation = Vector3D.sum(
			Vector3D.mult( right, - delta_x_pixels / pixelsPerUnit ),
			Vector3D.mult( up, - delta_y_pixels / pixelsPerUnit )
		);

		position = Point3D.sum(position, translation);
		target = Point3D.sum(target, translation);
	}

	// This causes the camera to translate forward into the scene.
	// This is also called "dollying" or "tracking" in some software packages.
	// Passing in a negative delta causes the opposite motion.
	// If ``pushTarget'' is true, the point of interest translates forward (or backward)
	// *with* the camera, i.e. it's "pushed" along with the camera; otherwise it remains stationary.
	public void dollyCameraForward( float delta_pixels, boolean pushTarget ) {
		Vector3D direction = Point3D.diff(target,position);

		float distanceFromTarget = direction.length();
		direction = direction.normalized();

		float translationSpeedInUnitsPerRadius =
			distanceFromTarget * (float)Math.tan( fieldOfViewInDegrees/2 / 180 * (float)Math.PI );
		float pixelsPerUnit = viewportRadiusInPixels
			/ translationSpeedInUnitsPerRadius;

		float dollyDistance = delta_pixels / pixelsPerUnit;

		if (!pushTarget) {
			distanceFromTarget -= dollyDistance;
			if (distanceFromTarget < pushThreshold * nearPlane) {
				distanceFromTarget = pushThreshold * nearPlane;
			}
		}

		position = Point3D.sum( position, Vector3D.mult(direction,dollyDistance) );
		target = Point3D.sum( position, Vector3D.mult(direction,distanceFromTarget) );
	}

	// Rotates the camera around its position to look at the given point,
	// which becomes the new target.
	public void lookAt(Point3D p) {
		// FIXME: we do not check if the target point is too close
		// to the camera (i.e. less than pushThreshold*nearPlane ).
		// If it is, perhaps we should dolly the camera away from the
		// target to maintain the minimal distance.

		target = p;
		Vector3D direction = (Point3D.diff(target, position)).normalized();
		Vector3D right = (Vector3D.cross(direction, ground)).normalized();
		up = Vector3D.cross(right, direction);
		// TODO XXX assert here that ``up'' is normalized
	}

	/**
	 * Set our camera location
	 * @param eyeAt
	 */
	public void eyeAt(Point3D eyeAt) {
		position = eyeAt;
		if (target != null)
			lookAt(target);			// Orient
	}
	
	// Returns the ray through the center of the given pixel.
	public Ray3D computeRay(
		float pixel_x, float pixel_y
	) {
		float tangent = (float)Math.tan( fieldOfViewInDegrees/2 / 180 * (float)Math.PI );
		float viewportRadius = nearPlane * tangent;

		// Pixel coordinates of the viewport's center.
		// These will be half-integers if the viewport's dimensions are even.
		float viewportCenterX = (viewportWidthInPixels-1)*0.5f;
		float viewportCenterY = (viewportHeightInPixels-1)*0.5f;

		// This is a point on the near plane, in camera space
		Point3D p = new Point3D(
			(pixel_x-viewportCenterX)*viewportRadius/viewportRadiusInPixels,
			(viewportCenterY-pixel_y)*viewportRadius/viewportRadiusInPixels,
			nearPlane
		);

		// Transform p to world space
		Vector3D direction = Point3D.diff(target, position).normalized();
		Vector3D right = Vector3D.cross(direction, up);
		Vector3D v = Vector3D.sum(Vector3D.mult(right,p.x()), Vector3D.sum(Vector3D.mult(up,p.y()), Vector3D.mult(direction,p.z())));
		return new Ray3D( Point3D.sum(position, v), v.normalized() );
	}

	// Compute the necessary size, in world space,
	// of an object centerd at the given point
	// for it to cover the given length of pixels.
	// This is useful for choosing the size of something
	// to give it a constant length or size in screen space.
	public float convertPixelLength( Point3D p, float pixelLength ) {
		Vector3D direction = Point3D.diff(target, position).normalized();
		Vector3D v = Point3D.diff(p, position);

		// distance, in world space units, from plane through camera that
		// is perpendicular to line of site
		float z = Vector3D.dot(v, direction);

		float tangent = (float)Math.tan( fieldOfViewInDegrees/2 / 180 * (float)Math.PI );

		return pixelLength
			// The below is world space units per pixel
			* z * tangent / (float)viewportRadiusInPixels;

	}

	// Computes the pixel covering the given point.
	// Also returns the z-distance (in camera space) to the point.
	public float computePixel(
		Point3D p, // input
		int [] pixel_coordinates // output; caller must pass in a 2-element array (for x and y)
	) {
		// Transform the point from world space to camera space.

		Vector3D direction = (Point3D.diff(target, position)).normalized();
		Vector3D right = Vector3D.cross(direction, up);

		// Note that (right, up, direction) form an orthonormal basis.
		// To transform a point from camera space to world space,
		// we can use the 3x3 matrix formed by concatenating the
		// 3 vectors written as column vectors.  The inverse of such
		// a matrix is simply its transpose.  So here, to convert from
		// world space to camera space, we do

		Vector3D v = Point3D.diff(p, position);
		float x = Vector3D.dot(v, right);
		float y = Vector3D.dot(v, up);
		float z = Vector3D.dot(v, direction);

		// (or, more simply, the projection of a vector onto a unit vector
		// is their dot product)

		float k = nearPlane / z;

		float tangent = (float)Math.tan( fieldOfViewInDegrees/2 / 180 * (float)Math.PI );
		float viewportRadius = nearPlane * tangent;
		// Pixel coordinates of the viewport's center.
		// These will be half-integers if the viewport's dimensions are even.
		float viewportCenterX = (viewportWidthInPixels-1)*0.5f;
		float viewportCenterY = (viewportHeightInPixels-1)*0.5f;

		// The +0.5f here is for rounding.
		pixel_coordinates[0] = (int)(
			k*viewportRadiusInPixels*x/viewportRadius + viewportCenterX + 0.5f
		);
		pixel_coordinates[1] = (int)(
			viewportCenterY - k*viewportRadiusInPixels*y/viewportRadius + 0.5f
		);
		return z;
	}

}



