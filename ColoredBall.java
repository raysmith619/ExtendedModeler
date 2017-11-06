import java.awt.Color;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;

public class ColoredBall extends EMBlockBase {
	private boolean isOk = false;	// Set OK upon successful construction

	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		return getBox().intersects(ray, intersection, normalAtIntersection);		
	}
	/**
	 * Check if ok block
	 */

	public boolean isOk() {
		return isOk;
	}
	
	public ColoredBall(
		AlignedBox3D box,
		Color color
	) {
		super(box, color);
		isOk = true;
	}
	


	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		AlignedBox3D box = getBox();
		drawBall(drawable, box, expand, drawAsWireframe, cornersOnly);
	}

	public String blockType() {
		return "ball";
	}



	static public void drawBall(
		GLAutoDrawable drawable,
		AlignedBox3D box,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		GL2 gl = (GL2) drawable.getGL();
		drawAsWireframe = true;			/// Force frame
		///drawAsWireframe = false;		/// Force not frame
		cornersOnly = false;			/// Force no corners
		cornersOnly = true;				/// Force corners
		if ( expand ) {
			float diagonal = box.getDiagonal().length();
			diagonal /= 20;
			Vector3D v = new Vector3D( diagonal, diagonal, diagonal );
			box = new AlignedBox3D( Point3D.diff(box.getMin(),v), Point3D.sum(box.getMax(),v) );
		}
		if ( drawAsWireframe ) {
			/***
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
			else {
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
			}
			***/
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
	@Override
	public EMBlockBase copy() {
		super.copy();
		return null;
	}
	

	
}
