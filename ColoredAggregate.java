package ExtendedModeler;
import java.awt.Color;
import java.util.Iterator;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLUquadric;

import smTrace.SmTrace;

import com.jogamp.opengl.glu.GLU;

public class ColoredAggregate extends EMBlockBase {
	private EMBlockGroup group;


	/**
	 * Base object
	 */
	public ColoredAggregate(EMBlockGroup group, Vector3D up) {
		super(null, null, up);
		this.group = new EMBlockGroup(group);
	}

	public ColoredAggregate() {
		this(new EMBlockGroup(), null);
	}

	/**
	 * Basic dimensions to encompassing radius
	 * @param height
	 * @param rBase
	 * @return
	 */
	public static float dim2radius(float height, float rBase) {
		float radius = (float) Math.sqrt(height*height/4 + rBase*rBase);
		return radius;
		
	}

	
	/**
	 * Create new object, from controls
	 * @throws EMBlockError 
	 **/
	public static ColoredAggregate newBlock(ControlsOfScene controlsOfScene) throws EMBlockError {
		ControlOfScene ctl = controlsOfScene.getControl("placement");
		ControlOfPlacement cop = (ControlOfPlacement)ctl;
		Point3D center = cop.getPosition();
		Vector3D up =  cop.getUp();
		return new ColoredAggregate();
	}

	public String blockType() {
		return "cylinder";
	}

	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		return getBox().intersects(ray, intersection, normalAtIntersection);		
	}
	
	public ColoredAggregate(
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
	public ColoredAggregate(EMBlockBase cb_base) {
		super(cb_base);
	}
	


	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		Iterator<EMBlock> itr = group.getIterator();
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			cb.draw(drawable, expand, drawAsWireframe, cornersOnly);
		}
	}



	static public void drawCylinder(
		GLAutoDrawable drawable,
		EMBox3D ebox,
		float h,
		float r,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		SmTrace.lg("drawCylinder", "draw");
		GL2 gl = (GL2) drawable.getGL();
		OrientedBox3D obox = new OrientedBox3D(r, h, r, ebox.getCenter(), ebox.up);
		AlignedBox3D abox = obox.getAlignedBox();
		if ( expand ) {
			float adj = (float) 1.1;
			obox.adjSize(adj, adj, adj);
		}
		if ( drawAsWireframe ) {
			EMBox3D.rotate2v(drawable, EMBox3D.UP, obox.getUp());
			if ( cornersOnly ) {
				gl.glBegin( GL.GL_LINES );
				for ( int dim = 0; dim < 3; ++dim ) {
					Vector3D v = Vector3D.mult( Point3D.diff(abox.getCorner(1<<dim),abox.getCorner(0)), 0.1f );
					for ( int a = 0; a < 2; ++a ) {
						for ( int b = 0; b < 2; ++b ) {
							int i = (a << ((dim+1)%3)) | (b << ((dim+2)%3));
							gl.glVertex3fv( abox.getCorner(i).get(), 0 );
							gl.glVertex3fv( Point3D.sum( abox.getCorner(i), v ).get(), 0 );
							i |= 1 << dim;
							gl.glVertex3fv( abox.getCorner(i).get(), 0 );
							gl.glVertex3fv( Point3D.diff( abox.getCorner(i), v ).get(), 0 );
						}
					}
				}
				gl.glEnd();
			}
			else {
				gl.glBegin( GL.GL_LINE_STRIP );
					gl.glVertex3fv( abox.getCorner( 0 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 1 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 3 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 2 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 6 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 7 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 5 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 4 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 0 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 2 ).get(), 0 );
				gl.glEnd();
				gl.glBegin( GL.GL_LINES );
					gl.glVertex3fv( abox.getCorner( 1 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 5 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 3 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 7 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 4 ).get(), 0 );
					gl.glVertex3fv( abox.getCorner( 6 ).get(), 0 );
				gl.glEnd();
			}
			EMBox3D.rotate2vRet(drawable);
		}
		else {
			int nLongitudes = 20;
			int nLatitudes = nLongitudes;
			Point3D center = abox.getCenter();
			GLU glu = new GLU();

			GLUquadric cylinder = glu.gluNewQuadric();
			float bx = center.x();
			float by = center.y();
			float bz = center.z();
			gl.glTranslatef(bx, by, bz);
			EMBox3D.rotate2v(drawable, EMBox3D.UP, obox.getUp());
			glu.gluCylinder(cylinder, r, r, h, nLongitudes, nLatitudes);
			EMBox3D.rotate2vRet(drawable);
			gl.glTranslatef(-bx, -by, -bz);
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


	public Point3D getMin() {
		float xmin = getMinX();
		float ymin = getMinY();
		float zmin = getMinZ();
		return new Point3D(xmin, ymin, zmin);
	}

	
	public float getMinX() {
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMinX();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}

	
	public float getMinY() {
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMinY();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}

	
	public float getMinZ() {
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMinZ();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}
	
	
	public Point3D getMax() {
		float xmax = getMaxX();
		float ymax = getMaxY();
		float zmax = getMaxZ();
		return new Point3D(xmax, ymax, zmax);
	}

	
	public float getMaxX() {
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMaxX();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}

	
	public float getMaxY() {
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMaxY();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}

	
	public float getMaxZ() {
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMaxZ();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}



	/**
	 * 
	 * @param new_size - calculate from components
	 */
	public void resize(
		Vector3D size
	) {
		///TBD
	}
	
}
