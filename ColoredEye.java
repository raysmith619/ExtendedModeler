package ExtendedModeler;
import java.awt.Color;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
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
		return getBox().intersects(ray, intersection, normalAtIntersection);		

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

	public AlignedBox3D position2box(Point3D position) {
		if (position == null)
			position = new Point3D(0, 0, 0);
		Vector3D size = getSize();
		float p1x = position.x() - size.x()*getWidth();
		float p2x = position.x() + size.x()*getWidth();
		float p1y = position.y() - size.y()*getWidth();
		float p2y = position.y() + size.y()*getWidth();
		float p1z = position.z() - size.z()*getWidth();
		float p2z = position.z() + size.z()*getWidth();
		Point3D p1 = new Point3D(p1x, p1y, p1z);
		Point3D p2 = new Point3D(p2x, p2y, p2z);
		AlignedBox3D box = new AlignedBox3D(p1, p2);
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
		gl.glColor3d(.8,.8,.8);
		ColoredBall.drawBall(drawable, box, expand, drawAsWireframe, cornersOnly);
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
		AlignedBox3D box,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		GL2 gl = (GL2) drawable.getGL();
		gl.glColor3d(.8,.8,.8);
		ColoredBall.drawBall(drawable, box, expand, drawAsWireframe, cornersOnly);
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
 * drawBall, using gl directly
 * @param gl
 * @param box
 * @param expand
 * @param drawAsWireframe
 * @param cornersOnly
 */
	static public void drawBallGl(
		GL2 gl,
		AlignedBox3D box,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		///drawable.getContext().makeCurrent(); 	///Hack to avoid no GLContext
		drawAsWireframe = true;			/// Force frame
		///drawAsWireframe = false;		/// Force not frame
		///cornersOnly = false;			/// Force no corners
		///cornersOnly = true;				/// Force corners
		if ( expand ) {
			float diagonal = box.getDiagonal().length();
			diagonal /= 20;
			Vector3D v = new Vector3D( diagonal, diagonal, diagonal );
			box = new AlignedBox3D( Point3D.diff(box.getMin(),v), Point3D.sum(box.getMax(),v) );
		}
		if ( drawAsWireframe ) {
			///***
			if ( cornersOnly ) {
				gl.glBegin( GL.GL_LINES );
				for ( int dim = 0; dim < 3; ++dim ) {
					Vector3D v = Vector3D.mult( Point3D.diff(box.getCorner(1<<dim),box.getCorner(0)), 0.1f );
					for ( int a = 0; a < 2; ++a ) {
						for ( int b = 0; b < 2; ++b ) {
							int i = (a << ((dim+1)%3)) | (b << ((dim+2)%3));
							gl.glVertex3fv( box.getCorner(i).get(), 0 );
							gl.glVertex3fv( Point3D.sum( box.getCorner(i), v ).get(), 0 );
							i |= 1 << dim;
							gl.glVertex3fv( box.getCorner(i).get(), 0 );
							gl.glVertex3fv( Point3D.diff( box.getCorner(i), v ).get(), 0 );
						}
					}
				}
				gl.glEnd();
			}
			/***else {
				gl.glBegin( GL.GL_LINE_STRIP );
					gl.glVertex3fv( box.getCorner( 0 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 1 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 3 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 2 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 6 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 7 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 5 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 4 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 0 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 2 ).get(), 0 );
				gl.glEnd();
				gl.glBegin( GL.GL_LINES );
					gl.glVertex3fv( box.getCorner( 1 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 5 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 3 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 7 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 4 ).get(), 0 );
					gl.glVertex3fv( box.getCorner( 6 ).get(), 0 );
				gl.glEnd();
			}***/
			///***/
			Vector3D diagonal = box.getDiagonal();
			float xlen = Math.abs(diagonal.x());
			float ylen = Math.abs(diagonal.y());
			float zlen = Math.abs(diagonal.z());
			float minlen = Math.min(xlen, ylen);
			minlen = Math.min(minlen, zlen);
			float r = minlen/2;
			Point3D center = box.getCenter();
			GLUT glut = new GLUT();
			int nLongitudes = 20;
			int nLatitudes = nLongitudes;
			gl.glTranslatef(center.x(), center.y(), center.z());
			glut.glutWireSphere(r, nLongitudes, nLatitudes);
			gl.glTranslatef(-center.x(), -center.y(), -center.z());
		}
		else {
			int nLongitudes = 20;
			int nLatitudes = nLongitudes;
			Vector3D diagonal = box.getDiagonal();
			float xlen = Math.abs(diagonal.x());
			float ylen = Math.abs(diagonal.y());
			float zlen = Math.abs(diagonal.z());
			float minlen = Math.min(xlen, ylen);
			minlen = Math.min(minlen, zlen);
			float r = minlen/2;
			Point3D center = box.getCenter();
			GLUT glut = new GLUT();
			
			gl.glTranslatef(center.x(), center.y(), center.z());
			glut.glutSolidSphere(r, nLongitudes, nLatitudes);
			gl.glTranslatef(-center.x(), -center.y(), -center.z());
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
