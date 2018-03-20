package ExtendedModeler;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import smTrace.SmTrace;


public class ColoredImage extends EMBlockBase {
	/**
	 *  Oriented box
	 * Use this for object size and orientation
	 * Functions that depend / affect position
	 * MUST be overridden to use "obox"
	 * instead of super.box
	 */
	AlignedBox3D abox = new AlignedBox3D();
	Vector3D size;				// local x,y,z extent
	public static String defaultImageFileDirName = "C:\\Users\\raysm\\workspace\\ExtendedModeler\\images";
	public static String defaultImageFileName = "C:\\Users\\raysm\\workspace\\ExtendedModeler\\images\\LionsLions.png";
	String imageFileName;
	private File imageFile;
	Texture imageTexture;
	int imageTextureI;
	GLAutoDrawable drawable;

	/**
	 * Generic object
	 */
	public ColoredImage() {
		this(null, defaultImageFileName);
	}
	
	public ColoredImage(
		EMBox3D box,
		String imageFile
	) {
		super(box, null);
		setBox(box);
		isOk = true;
	}


	/**
	 * Base object
	 */
	public ColoredImage(Point3D center, Vector3D size, String imageFileName, Vector3D up) {
		super(center, size2radius(size), Color.WHITE, up);
		this.drawable = drawable;
		if (size == null)
			size = new Vector3D(0,0,0);
		this.size = size;
		this.abox = new AlignedBox3D(center, size);
		if (imageFileName == null || imageFileName.equals("")) {
			imageFileName = ColoredImage.defaultImageFileName;
		}
		if (!new File(imageFileName).isAbsolute()) {
			imageFileName = defaultImageFileDirName + File.separator + imageFileName;
		}
		
		imageFile = new File(imageFileName);
		try {
			imageTexture = TextureIO.newTexture(imageFile, true);	/// mipmaps?
			if (imageTexture == null) {
				SmTrace.lg("File new imageTexture is null");
				return;
			}
		} catch (GLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			SmTrace.lg(String.format("Texture IO Error File name=\"%s\" %s",
					imageFileName, e.getMessage()));
			return;
		}
	}

	
	/**
	 * Create new object, from controls
	 * @throws EMBlockError 
	 **/
	public static ColoredImage newBlock(ControlsOfScene	controlsOfScene,
			String name) throws EMBlockError {
		ControlOfScene ctl = controlsOfScene.getControl("placement");
		ControlOfPlacement cop = (ControlOfPlacement)ctl;
		Point3D center = cop.getPosition();
		Vector3D size = cop.getSizeXYZ();
		Vector3D up =  cop.getUp();
		if (name == null) {
			ControlOfComponent coco = (ControlOfComponent)controlsOfScene.getControl("component");
			name = coco.getFileName();
		}
		return new ColoredImage(center, size, name, up);
	}

	
	/**
	 * Create new object, from controls
	 * @throws EMBlockError 
	 **/
	public static ColoredImage newBlock(ControlsOfScene	controlsOfScene) throws EMBlockError {
		return newBlock(controlsOfScene, null);
	}

	
	public ColoredImage(EMBox3D box, Color color, int iD) {
		super(box, color, iD);
	}

	/**
	 * Same iD
	 * @param cb_base
	 */
	public ColoredImage(EMBlockBase cb_base) {
		super(cb_base);
	}

	/**
	 * Convert partial file name to full file name
	 * @param fileName
	 * @return
	 */
	public static String fullFileName(String fileName) {
		if (fileName == null || fileName.equals("")) {
			fileName = ColoredImage.defaultImageFileName;
		}
		if (!new File(fileName).isAbsolute()) {
			fileName = defaultImageFileDirName + File.separator + fileName;
		}
		return fileName;
	}

	/**
	 * Convert partial file name to icon file name
	 * @param fileName
	 * @return
	 */
	public static String iconFileName(String fileName) {
		String full_file_name = fullFileName(fileName);
		File dir = new File(full_file_name);
		String icon_file_name = full_file_name;		// Default
		if (dir.isDirectory()) {
			String files[] = dir.list();
			for (String name : files) {
				if (name.startsWith("__ICON")) {
					icon_file_name = icon_file_name + File.separator + name;
					break;
				}
			}
		}
		return icon_file_name;
	}
	
	
	/**
	 * Convert xyz size to radius of object, enclosing object
	 */
	public static float size2radius(Vector3D size) {
		if (size == null)
			size = new Vector3D(0,0,0);
		return size.length()/2;
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

	
	/**
	 * Copy
	 * "Deep-enough" copy to protect against subsequent modifications
	 */
	public EMBlockBase copy() {
		EMBlockBase cb = new ColoredImage(this.box, this.imageFileName);
		cb = cb.copy();
		return cb;
	}

	@Override
	public String blockType() {
		return "image";
	}
	

	/**
	 * Get corner (global coordinates)
	 * @param i
	 * @return
	 */
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


	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		GL2 gl = (GL2) drawable.getGL();
		glp = gl;					// For tracking
		OrientedBox3D obox = getOBox();
		if ( expand ) {
			float adj = (float) 1.1;
			obox.adjSize(adj, adj, adj);
		}
		if (SmTrace.trace("drawimage")) {
			if (isExternalViewer()) {
				SmTrace.lg(String.format("draw %s in External Viewer", blockType()));
			}
			if (isLocalViewer()) {
				SmTrace.lg(String.format("draw %s in Local Viewer", blockType()));
			}
		}
		if (SmTrace.trace("skipimage")) {
			if (isExternalViewer()) {
				SmTrace.lg(String.format("draw %s skipping in External Viewer", blockType()));
				return;
			}
		}
		AlignedBox3D abox = obox.getAlignedBox();

		EMBox3D.rotate2v(drawable, EMBox3D.UP, obox.getUp());
		if ( drawAsWireframe ) {
			setEmphasis(gl);
			if ( cornersOnly ) {
				gl_glBegin( GL.GL_LINES, "drawAsWireframe cornersOnly GL.GL_LINES" );
				for ( int dim = 0; dim < 3; ++dim ) {
					Point3D pvto = abox.getCorner(1<<dim);
					Point3D pv0 = abox.getCorner(0);
					Vector3D pdiff = Point3D.diff( pvto, pv0);
					Vector3D v = Vector3D.mult(pdiff, 0.1f );
					for ( int a = 0; a < 2; ++a ) {
						for ( int b = 0; b < 2; ++b ) {
							int i = (a << ((dim+1)%3)) | (b << ((dim+2)%3));
							Point3D pvi = abox.getCorner(i);
							gl_glVertex3fv( pvi.get(), 0 );
							gl_glVertex3fv( Point3D.sum( pvi, v ).get(), 0 );
							i |= 1 << dim;
							Point3D pvi2 = abox.getCorner(i);
							gl_glVertex3fv( pvi2.get(), 0 );
							gl_glVertex3fv( Point3D.diff( pvi2, v ).get(), 0 );
						}
					}
				}
				gl_glEnd();
			}
			else {
				gl_glBegin(GL.GL_LINE_STRIP, "drawAsWireframe GL.GL_LINE_STRIP");
					gl_glVertex3fv( abox.getCorner( 0 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 1 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 3 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 2 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 6 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 7 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 5 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 4 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 0 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 2 ).get(), 0 );
				gl_glEnd();
				gl_glBegin( GL.GL_LINES, "drawAsWireframe GL.GL_LINE_STRIP" );
					gl_glVertex3fv( abox.getCorner( 1 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 5 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 3 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 7 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 4 ).get(), 0 );
					gl_glVertex3fv( abox.getCorner( 6 ).get(), 0 );
				gl_glEnd();
				clearEmphasis();
			}
		}
		else {
			boolean DBG = true;
			if (isExternalViewer()) {
				drawFromFile(drawable);
				return;
			}
			gl.glPushMatrix();
			gl.glTranslatef(obox.getCenter().x(), obox.getCenter().y(), obox.getCenter().z());
			float minx = -size.x()/2;
			float maxx = size.x()/2;
			float miny = -size.y()/2;
			float maxy = size.y()/2;
			float minz = -size.z()/2;
			float maxz = size.z()/2;
			if (imageTexture == null) {
				SmTrace.lg("adding imageTexture is null");
				return;
			}
		SmTrace.lg(String.format("draw: %s body", blockType()), "drawimage");
		int imageTextureI = imageTexture.getTextureObject(gl);
		///ColoredBox.setMaterial(gl);
		imageTexture.enable(gl);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glColor3f(1, 1, 1);		// Force white background
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, imageTextureI);
		gl.glBegin(GL2.GL_QUADS);
		
		  // Front Face
		  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(minx, miny, maxz);
		  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(maxx, miny, maxz);
		  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(maxx, maxy, maxz);
		  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(minx, maxy, maxz);
		
		  // Back Face
		  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(minx, miny, minz);
		  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(minx, maxy, minz);
		  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(maxx, maxy, minz);
		  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(maxx, miny, minz);
		
		  // Top Face
		  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(minx, maxy, minz);
		  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(minx, maxy, maxz);
		  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(maxx, maxy, maxz);
		  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(maxx, maxy, minz);
		
		  // Bottom Face
		  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(minx, miny, minz);
		  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(maxx, miny, minz);
		  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(maxx, miny, maxz);
		  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(minx, miny, maxz);
		
		  // Right face
		  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(maxx, miny, minz);
		  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(maxx, maxy, minz);
		  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(maxx, maxy, maxz);
		  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(maxx, miny, maxz);
		
		  // Left Face
		  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(minx, miny, minz);
		  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(minx, miny, maxz);
		  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(minx, maxy, maxz);
		  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(minx, maxy, minz);
		  gl.glEnd();
		  imageTexture.disable(gl);
		  gl.glFlush();
		}
		EMBox3D.rotate2vRet(drawable);
		gl.glPopMatrix();					// Undo translate
	}


	static public void drawImage(
		GLAutoDrawable drawable,
		ColoredImage cimage,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
			) {
		cimage.draw(drawable, expand, drawAsWireframe, cornersOnly);
	}

	/**
	 * Tracking / Debugging 
	 * @param v
	 * @param offset
	 */

	/**
	 * Tracking / Debugging 
	 * @param v
	 * @param offset
	 */
	private static GL2 glp;				// for tracking routine
	private static String block = "box";
	private static void gl_glVertex3fv(float [] v, int offset) {
		String desc = traceDesc;
		glp.glVertex3fv(v, offset);
		SmTrace.lg(String.format("%s glVertex3fv %.2f %.2f %.2f", block, v[0], v[1], v[2], desc), "draw");
	}
	private static String traceDesc = "trace description";
	private static void gl_glBegin(int mode, String desc) {
		traceDesc = desc;
		glp.glBegin(mode);
		SmTrace.lg(String.format("%s gl_glBegin(%s)", block, desc), "draw");
	}
	private static void gl_glBegin(int mode) {
		String desc = String.format("mode(%d)", mode);
		gl_glBegin(mode, desc);
	}
	
	private static void gl_glEnd(String desc) {
		glp.glEnd();
		SmTrace.lg(String.format("%s gl_glEnd %s", block, desc), "draw");
	}
	private static void gl_glEnd() {
		String desc = traceDesc;
		gl_glEnd(desc);
	}

	/**
	 * Recreate image from file
	 * Investigatory to investigate
	 * problem with multiple views
	 * @param gl
	 */
	public void drawFromFile(GLAutoDrawable drawable) {
		GL2 gl = (GL2) drawable.getGL();
		glp = gl;					// For tracking
		Texture imageTexture;
		
		try {
			imageTexture = TextureIO.newTexture(imageFile, true);	/// mipmaps?
			if (imageTexture == null) {
				SmTrace.lg("File new imageTexture is null");
				return;
			}
		} catch (GLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			SmTrace.lg(String.format("Texture IO Error File name=\"%s\" %s",
					imageFileName, e.getMessage()));
			return;
		}
		
		
		gl.glPushMatrix();
		OrientedBox3D obox = getOBox();
		gl.glTranslatef(obox.getCenter().x(), obox.getCenter().y(), obox.getCenter().z());
		float minx = -size.x()/2;
		float maxx = size.x()/2;
		float miny = -size.y()/2;
		float maxy = size.y()/2;
		float minz = -size.z()/2;
		float maxz = size.z()/2;
		if (imageTexture == null) {
			SmTrace.lg("adding imageTexture is null");
			return;
		}
	SmTrace.lg(String.format("draw: %s body", blockType()), "drawimage");
	int imageTextureI = imageTexture.getTextureObject(gl);
	///ColoredBox.setMaterial(gl);
	imageTexture.enable(gl);
	gl.glDisable(GL2.GL_LIGHTING);
	gl.glColor3f(1, 1, 1);		// Force white background
	gl.glEnable(GL2.GL_TEXTURE_2D);
	gl.glBindTexture(GL2.GL_TEXTURE_2D, imageTextureI);
	gl.glBegin(GL2.GL_QUADS);
	
	  // Front Face
	  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(minx, miny, maxz);
	  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(maxx, miny, maxz);
	  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(maxx, maxy, maxz);
	  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(minx, maxy, maxz);
	
	  // Back Face
	  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(minx, miny, minz);
	  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(minx, maxy, minz);
	  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(maxx, maxy, minz);
	  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(maxx, miny, minz);
	
	  // Top Face
	  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(minx, maxy, minz);
	  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(minx, maxy, maxz);
	  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(maxx, maxy, maxz);
	  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(maxx, maxy, minz);
	
	  // Bottom Face
	  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(minx, miny, minz);
	  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(maxx, miny, minz);
	  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(maxx, miny, maxz);
	  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(minx, miny, maxz);
	
	  // Right face
	  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(maxx, miny, minz);
	  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(maxx, maxy, minz);
	  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(maxx, maxy, maxz);
	  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(maxx, miny, maxz);
	
	  // Left Face
	  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(minx, miny, minz);
	  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(minx, miny, maxz);
	  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(minx, maxy, maxz);
	  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(minx, maxy, minz);
	  gl.glEnd();
	  imageTexture.disable(gl);
	  gl.glFlush();

	EMBox3D.rotate2vRet(drawable);
	gl.glPopMatrix();					// Undo translate
		
	}

	public Point3D getMin() {
		return abox.getMin();
	}

	public Point3D getMax() {
		return abox.getMax();
	}
	

	// Overridden for types with name, eg filename
	public String getName() {
		return imageFileName;
	}
	
	
	/**
	 * Return copy of inner box
	 * 
	 */
	public OrientedBox3D getOBox() {
		return new OrientedBox3D(abox, getUp());
	}

	/**
	 * In progress
	 */
	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		OrientedBox3D obox = this.getOBox();
		return obox.intersects(ray, intersection, normalAtIntersection);
	}
	
	
	/**
	 * Return copy of inner box
	 * 
	 */
	public OrientedBox3D orientedBox3D() {
		return new OrientedBox3D(abox, getUp());
	}


	/**
	 * Enlarge to contain box
	 * @param box
	 */
	@Override
	public void bound(Point3D pt) {
		this.abox.bound(pt);
	}


	/**
	 * 
	 * @param new_size - new size with center in place
	 */
	public void resize(
		Vector3D size
	) {
		abox.resize(size);
		float radius = size2radius(size);
		setRadius(radius);
	}

	/**
	 * Setup location info
	 */
	public void setBox(EMBox3D box) {
		this.abox = new AlignedBox3D(box);
		setUp(box.getUp());
	}


	public void resize(
		int indexOfCornerToResize, Vector3D translation
	) {
		EMBox3D oldBox = getBox();
		box = new EMBox3D();

		// One corner of the new box will be the corner of the old
		// box that is diagonally opposite the corner being resized ...
		 box.bound( oldBox.getCorner( indexOfCornerToResize ^ 7 ) );

		// ... and the other corner of the new box will be the
		// corner being resized, after translation.
		box.bound( Point3D.sum( oldBox.getCorner( indexOfCornerToResize ), translation ) );
	}

	
	// Overridden when necessary
	public void translate(Vector3D translation ) {
		SmTrace.lg(String.format("ColoredImage.translation(%s)", translation), "image");
		abox.translate(translation);
		
	}

	
	/**
	 * Get base point - currently min x,y,z
	 * @param point
	 */
	public Point3D getBasePoint() {
		EMBox3D box = getBox();
		return box.getMin();
	}
	

	/**
	 * Move center to new point
	 * @param point
	 */
	public void moveTo(Point3D point ) {
		box.setPosition(point);
	}
	

	/**
	 * Move base corner to new point
	 * @param point
	 */
	public void moveBaseTo(Point3D point ) {
		EMBox3D oldBox = getBox();
		Vector3D vm = Point3D.diff(point, oldBox.getMin());
		translate(vm);
	}
	
	
}
