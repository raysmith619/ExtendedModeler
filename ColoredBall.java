package ExtendedModeler;
import java.awt.Color;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;

import smTrace.SmTrace;

public class ColoredBall extends EMBlockBase {
	///private boolean isOk = false;	// Set OK upon successful construction

	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		EMBox3D box = getBox();
		boolean ret = box.intersects(ray, intersection, normalAtIntersection);
		SmTrace.lg(String.format("ColoredBall.intersects(ray:%s, intersection(%s), normalAtIntersection(%s)",
				ray, intersection, normalAtIntersection), "intersection");
		if (normalAtIntersection.lengthSquared() == 0) {		
			normalAtIntersection = new Vector3D(1,0,0);		///TFD
			SmTrace.lg(String.format("ColoredBall.intersects FORCED (ray:%s, intersection(%s), normalAtIntersection(%s)",
					ray, intersection, normalAtIntersection), "intersection");
		}
		return ret;		
	}
	/**
	 * Check if ok block
	 */

	public boolean isOk() {
		return isOk;
	}

	
	public ColoredBall(Point3D center, float radius, Color color, Vector3D up) {
		super(center, radius, color, up);
	}
	public ColoredBall(
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
	public ColoredBall(EMBlockBase cb_base) {
		super(cb_base);
	}
	

	/**
	 * Create new object, from controls
	 * @throws EMBlockError 
	 **/
	public static ColoredBall newBlock(ControlsOfScene controlsOfScene) throws EMBlockError {
		ControlOfColor ctc = (ControlOfColor)controlsOfScene.getControl("color");
		Color color = ctc.getColor();
		ControlOfScene ctl = controlsOfScene.getControl("placement");
		ControlOfPlacement cop = (ControlOfPlacement)ctl;
		Point3D center = cop.getPosition();
		Vector3D size = cop.getSizeXYZ();
		float diameter = java.lang.Math.min(size.x(), size.y());				// Inside the box
		diameter = java.lang.Math.min(diameter, size.z());
		float radius = diameter/2;
		Vector3D up =  cop.getUp();
		return new ColoredBall(center, radius, color, up);
	}


	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		EMBox3D box = getBox();
		drawBall(drawable, box, expand, drawAsWireframe, cornersOnly);
	}

	public String blockType() {
		return "ball";
	}



	public static void drawBall(
		GLAutoDrawable drawable,
		EMBox3D box,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		///drawable.getContext().makeCurrent(); 	///Hack to avoid no GLContext
		GL2 gl = (GL2) drawable.getGL();
		drawAsWireframe = true;			/// Force frame
		///drawAsWireframe = false;		/// Force not frame
		///cornersOnly = false;			/// Force no corners
		///cornersOnly = true;				/// Force corners
		if ( expand ) {
			float diagonal = box.getDiagonal().length();
			diagonal /= 20;
			Vector3D v = new Vector3D( diagonal, diagonal, diagonal );
			box = new EMBox3D(box.getCenter(), box.getRadius() + diagonal);
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
			float r = box.getRadius();
			Point3D center = box.getCenter();
			GLUT glut = new GLUT();
			int nLongitudes = 20;
			int nLatitudes = nLongitudes;
			gl.glTranslatef(center.x(), center.y(), center.z());
			EMBox3D.rotate2v(drawable, EMBox3D.UP, box.getUp());
			glut.glutWireSphere(r, nLongitudes, nLatitudes);
			EMBox3D.rotate2vRet(drawable);
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
			EMBox3D.rotate2vRet(drawable);
			gl.glTranslatef(-center.x(), -center.y(), -center.z());
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
		EMBox3D box,
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
			box = new EMBox3D(box.getCenter(), box.getRadius() + diagonal);
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

	public Point3D getMin() {
		float xmin = box.getCenter().x()-getRadius();
		float ymin = box.getCenter().y()-getRadius();
		float zmin = box.getCenter().z()-getRadius();
		return new Point3D(xmin, ymin, zmin);
	}

	public Point3D getMax() {
		float xmax = box.getCenter().x()+getRadius();
		float ymax = box.getCenter().y()+getRadius();
		float zmax = box.getCenter().z()+getRadius();
		return new Point3D(xmax, ymax, zmax);
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
	 * 
	 * @param new_size - new size with center in place
	 */
	public void resize(
		Vector3D size
	) {
		float radius = size.length()/2;
		setRadius(radius);
	}
	

	
}
