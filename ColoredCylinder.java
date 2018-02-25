package ExtendedModeler;
import java.awt.Color;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLUquadric;

import smTrace.SmTrace;

import com.jogamp.opengl.glu.GLU;

public class ColoredCylinder extends EMBlockBase {
	float height;			// Height - base to top
	float rBase;			// Radius of base



	/**
	 * Base object
	 */
	public ColoredCylinder(Point3D center, float height, float rBase, Color color, Vector3D up) {
		super(center, dim2radius(height, rBase), color, up);
		this.height = height;
		this.rBase = rBase;
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
	public static ColoredCylinder newBlock(ControlsOfScene controlsOfScene) throws EMBlockError {
		ControlOfColor ctc = (ControlOfColor)controlsOfScene.getControl("color");
		Color color = ctc.getColor();
		ControlOfScene ctl = controlsOfScene.getControl("placement");
		ControlOfPlacement cop = (ControlOfPlacement)ctl;
		Point3D center = cop.getPosition();
		float height = cop.getSizeY();
		float rBase = cop.getSizeX()/2;
		Vector3D up =  cop.getUp();
		return new ColoredCylinder(center, height, rBase, color, up);
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
	
	public ColoredCylinder(
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
	public ColoredCylinder(EMBlockBase cb_base) {
		super(cb_base);
	}
	


	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {

		SmTrace.lg("drawCylinder", "draw");
		GL2 gl = (GL2) drawable.getGL();
		OrientedBox3D obox = getOBox();
		AlignedBox3D abox = obox.getAlignedBox();
		if ( expand ) {
			float adj = (float) 1.1;
			obox.adjSize(adj, adj, adj);
		}
		if ( drawAsWireframe ) {
			EMBox3D.rotate2v(drawable, EMBox3D.UP, obox.getUp());
			setEmphasis(gl);
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
			clearEmphasis();
			EMBox3D.rotate2vRet(drawable);
		}
		else {
			Point3D center = abox.getCenter();
			GLU glu = new GLU();

			GLUquadric cylinder = glu.gluNewQuadric();
			float bx = center.x();
			float by = center.y();
			float bz = center.z();
			gl.glTranslatef(bx, by, bz);
			EMBox3D.rotate2v(drawable, EMBox3D.UP, obox.getUp());
			ColoredBox.setMaterial(gl, getColor());
			glu.gluCylinder(cylinder, rBase, rBase, height, nLongitudes, nLatitudes);
			EMBox3D.rotate2vRet(drawable);
			gl.glTranslatef(-bx, -by, -bz);
		}
	}



static public void drawCylinder(
	GLAutoDrawable drawable,
	ColoredCylinder ccylinder,
	boolean expand,
	boolean drawAsWireframe,
	boolean cornersOnly
) {
	ccylinder.draw(drawable, expand, drawAsWireframe, cornersOnly);
}
	

	/**
	 * Generate an image icon for create button
	 * @param gl
	 * @param width
	 * @param height
	 * @return
	 */
	static public ImageIcon imageIcon(
		int width,
		int height
	) {
		String icon_name = ControlOfComponent.iconFileName("cylinder.jpg");
		File file = new File(icon_name);
		
		String basename = file.getName();
		Icon icon = new ImageIcon(icon_name);
		icon = ControlOfComponent.resizeIcon((ImageIcon)icon, width, height);
		return (ImageIcon) icon;
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
		float xmin = box.getCenter().x()-rBase;
		float ymin = box.getCenter().y()-height/2;
		float zmin = box.getCenter().z()-rBase;
		return new Point3D(xmin, ymin, zmin);
	}

	public Point3D getMax() {
		float xmax = box.getCenter().x()+rBase;
		float ymax = box.getCenter().y()+height/2;
		float zmax = box.getCenter().z()+rBase;
		return new Point3D(xmax, ymax, zmax);
	}

	/**
	 * Get oriented bounding box
	 * @return
	 */
	@Override
	public OrientedBox3D getOBox() {
		float x = rBase*2;
		float y = rBase*2;
		
		OrientedBox3D obox = new OrientedBox3D(x, y, height, getCenter(), getUp());
		return obox;
	}


	/**
	 * 
	 * @param new_size - new size with center in place
	 */
	public void resize(
		Vector3D size
	) {
		this.height = size.y();
		this.rBase = Math.max(size.x(),  size.x())/2;
		setRadius(dim2radius(this.height, this.rBase));
	}
	
}
