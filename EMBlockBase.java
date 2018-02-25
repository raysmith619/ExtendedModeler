package ExtendedModeler;
import java.awt.Color;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import smTrace.SmTrace;

/*
 * Contains some block
 */
public class EMBlockBase {
	public static final float DEFAULT_SIZE = 0.5f;
	public static final float DEFAULT_ALPHA = 0.5f;
	int iD;				// Unique identifier
///	public Object block = null;
	
						// Universal object traits
	public EMBox3D box = new EMBox3D();	// Bounding box (sphere), with orientation
	protected Color color;		// Object color
	int nLongitudes = 50;			// Granularity for sphere, cylinder, cone
	int nLatitudes = nLongitudes;

	///public Point3D position;	// Position:  CALCULATED
	public Point3D target = box.getCenter();		// target
	private boolean isSelected = false;
	private static EMBlockBase defaultAtt;	// Default attributes for creation/access

	public boolean isOk = false;		// Set OK upon successful construction
	private int viewerLevel;			// Visible if viewer level > visible
	static SceneControler sceneControler = null;		// Recording for debugging

	public static void setSceneControler(SceneControler sceneControler) {
		EMBlockBase.sceneControler = sceneControler;
	}

	/**
	 * Check if is a currently executing viewer it is a "Local Viewer"
	 * @return
	 */
	public static boolean isLocalViewer() {
		if (sceneControler == null)
			return false;			// No controller known
		SceneViewer viewer = sceneControler.getSceneViewer();
		if (viewer == null)
			return false;			// No viewer displayed
		String name = viewer.getSceneName();
		if (name.equals(ExtendedModeler.ExternalViewerName))
			return false;			// Is external viewer
		
		return true;
	}

	/**
	 * Check if currently executing viewer is "External Viewer"
	 * @return
	 */
	public static boolean isExternalViewer() {
		if (sceneControler == null)
			return false;			// No controller known
		SceneViewer viewer = sceneControler.getDisplayedViewer();
		if (viewer == null)
			return false;
		String name = viewer.getSceneName();
		if (name.equals(ExtendedModeler.ExternalViewerName))
			return true;
		
		return false;
	}
		

	/**
	 * Setup for defaults
	 * @throws EMBlockError 
	 */
	public static void setDefaults(String blockType, EMBox3D box, Color color) throws EMBlockError {
		color = EMBlockBase.colorCheck(color, "setDefaults");
		defaultAtt = EMBlockBase.newBlock(blockType, box, color);		// Use std creation
	}

	
	
	public EMBlockBase(EMBlockBase cb) {
		this(cb.box, cb.color, cb.iD());
		this.isOk = cb.isOk();
	}

	/**
	 * Setup material properties
	 */
	public static void setMaterial(GL2 gl, Color color) {
		if (color == null) {
			color = ControlOfView.nextColor();
		}
		gl.glEnable(GL2.GL_LIGHTING);		// Force it
		float[] colors = color.getComponents(new float[4]);
        float no_mat[] = { 0.0f, 0.0f, 0.0f, 1.0f };
        float mat_ambient[] = { 0.7f, 0.7f, 0.7f, 1.0f };
        float mat_ambient_color[] = { 0.8f, 0.8f, 0.2f, 1.0f };
        mat_ambient_color = colors;
        float mat_diffuse[] = { 0.1f, 0.5f, 0.8f, 1.0f };
        mat_diffuse = colors;
        float mat_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        mat_specular = colors;
        float no_shininess[] = { 0.0f };
        float low_shininess[] = { 5.0f };
        float high_shininess[] = { 100.0f };
        float mat_emission[] = { 0.3f, 0.2f, 0.2f, 0.0f };
        mat_emission = colors;
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_ambient_color, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, mat_diffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_specular, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, high_shininess, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION, no_mat, 0);
	}

	
	public static void setMaterial(GL2 gl) {
		setMaterial(gl, null);
	}

	/**
	 * Set for lighting emphasis
	 * disable lighting set color
	 * Needs matching clearEmphasis
	 * @param gl
	 * @param color
	 */
	private GL2 glEmphasis = null;
	public void setEmphasis(GL2 gl, Color color) {
		glEmphasis = gl;
		if (color == null) {
			color = this.color;
		}
		gl.glDisable(GL2.GL_LIGHTING);
		float cv[] = new float[4];
		float colors[] = color.getColorComponents(cv);
		gl.glColor4f(colors[0], colors[1], colors[2], colors[3]);
		
	}
	
	public void setEmphasis(GL2 gl) {
		setEmphasis(gl, null);
	}
	
	
	public void clearEmphasis() {
		if (glEmphasis == null) {
			SmTrace.lg("clearEmphasis - lacks matching setEmphasis");
		}
		glEmphasis.glEnable(GL2.GL_LIGHTING);
		glEmphasis = null;
	}
	
	/**
	 * Adjust from control settings based on command
	 */


	/**
	 * Adjust from color control
	 * @throws EMBlockError 
	 */
	public void adjustFromControl(ControlOfColor ctl, EMBCommand bcmd) throws EMBlockError {
		adjustFromControl(ctl, bcmd);
	}
		
	/**
	 * Set from placement control
	 * @throws EMBlockError 
	 */
	public void adjustFromControl(ControlOfPlacement ctl, EMBCommand bcmd) throws EMBlockError {
		adjustFromControl(ctl, bcmd);

	}
	
	/**
	 * Adjust from text control
	 * @throws EMBlockError 
	 */
	// Overridden for objects with text control e.g. ColoredText
	public void adjustFromControl(ControlOfText ctl, EMBCommand bcmd) throws EMBlockError {
		adjustFromControl(ctl, bcmd);
	}

	/**
	 * Set from color control
	 * @throws EMBlockError 
	 */
	public void setFromControl(ControlOfColor ctl) throws EMBlockError {
		Color color = ControlOfColor.nextColor();
		this.color = color;
	}
		
	/**
	 * Set from placement control
	 * @throws EMBlockError 
	 */
	public void setFromControl(ControlOfPlacement ctl) throws EMBlockError {
		this.box = ctl.getBox();
		if (this.target == null)
			this.target = new Point3D(0, 0, 0);
	}
	
/**
 * Set from text control
 * @throws EMBlockError 
 */
// Overridden for objects with text control e.g. ColoredText
public void setFromControl(ControlOfText ctl) throws EMBlockError {
}

	/**
	 * Set block from controls
	 * @throws EMBlockError 
	 */
	public void setFromControls(ControlsOfScene controls) throws EMBlockError {

		ControlOfPlacement cop = (ControlOfPlacement) controls.getControl("placement");
		if (cop == null)
			throw new EMBlockError("No controlOfPlacement");
		setFromControl(cop);

		ControlOfColor coc = (ControlOfColor) controls.getControl("color");
		if (coc == null)
			throw new EMBlockError("No controlOfColor");
		setFromControl(coc);

		ControlOfText cot = (ControlOfText) controls.getControl("text");
		if (cot == null)
			throw new EMBlockError("No controlOfText");
		setFromControl(cot);
	}

	/**
	 * Set controls based on block settings
	 */


	/**
	 * Set controls based on current state
	 */
	public void setControls(ControlsOfView cov) {
		
	}


	/**
	 * Set controls based on current state
	 */
	public void setControls(ControlsOfScene cos) {
		ControlOfPlacement cop = (ControlOfPlacement) cos.getControl("placement");
		setControl(cop);
		
		ControlOfColor coc = (ControlOfColor) cos.getControl("color");
		setControl(coc);
		
		ControlOfText cot = (ControlOfText) cos.getControl("text");
		setControl(cot);
		
	}

	public void setControl(ControlOfPlacement ctl) {
		ctl.setControl(this);
	}

	public void setControl(ControlOfColor ctl) {
		
	}

	// Overridden by those who control text
	public void setControl(ControlOfText ctl) {
		return;
	}

	/**
	 * Base
	 */
	/**
	 * Base constructor with osition info held elsewhere
	 * Position/orientation members must be overridden
	 */
	public EMBlockBase() {
		this(null, null, 0);
	}
	
	
	public EMBlockBase(Color color, int iD) {
		if (color == null)
			color = defaultAtt.color;
		if (iD == 0)
			iD = EMBlock.nextId();
		this.color = color;
	}

	/**
	 * Base constructor with some size/position info
	 */
	public EMBlockBase(EMBox3D box, Color color) {
		this(box, color, 0);
	}
	
	public EMBlockBase(EMBox3D box, Color color, int iD) {
		if (iD == 0)
			iD = EMBlock.nextId();
		this.iD = iD;
		if (box == null)
			box = new EMBox3D();
		this.box = new EMBox3D(box);
		if (color == null)
			if (defaultAtt != null)
				color = defaultAtt.getColor();
			else
				color = Color.WHITE;
		this.color = color;
		colorCheck(color, "EMBlock");
	}

	/**
	 * "Position only" blocks, e.g. eye
	 * @param position
	 * @param up
	 */
	public EMBlockBase(Point3D position,
			Point3D target,
			Vector3D up) {		// "UP", null - aligned with world's up
		this.iD = EMBlock.nextId();
		if (position ==  null) {
			position = new Point3D(0,0,0);
		}
		setPosition(position);
		this.color = Color.WHITE;
		if (target == null)
			target = new Point3D(0,0,0);
		box.setPosition(position);
		this.box.up = up;
	}

	
	public EMBlockBase(Point3D center, float radius, Color color, Vector3D up) {
		this.box = new EMBox3D(center, radius, up);
		this.color = color;
	}



	/**
	 * Copy
	 * "Deep-enough" copy to protect against subsequent modifications
	 */
	public EMBlockBase copy() {
		EMBlockBase cb_copy = new EMBlockBase(this);
		return cb_copy;
	}

	
	
	/**
	 * Check if color is too dark
	 */
	public static Color colorCheck(Color color, String tag) {
		if (color == null)
			return color; 
			
		float cc[] = new float[4];
		color.getColorComponents(cc);
		if (cc[0] < .1f && cc[1] < .1f && cc[2] < .1f) {
			SmTrace.lg(String.format("Dark color %s: [%f,%f,%f]",
					tag, cc[0],cc[1],cc[2]));
			Color newcolor = new Color(255,255,255);		// Set light
			return newcolor;
		}
		return color;				// Unchanged
	}

	
	/**
	 * Create new block
	 * using controls, present state
	 * @throws EMBlockError 
	 */
	public static EMBlockBase newBlock(String blockType, ControlsOfScene controls, String name) throws EMBlockError {
		EMBlockBase cb_new = null;
		if (blockType.equals("box"))
			cb_new = ColoredBox.newBlock(controls);
		else if (blockType.equals("ball"))
			cb_new = ColoredBall.newBlock(controls);
		else if (blockType.equals("cone"))
			cb_new = ColoredCone.newBlock(controls);
		else if (blockType.equals("cylinder"))
			cb_new = ColoredCylinder.newBlock(controls);
		else if (blockType.equals("image"))
			cb_new = ColoredImage.newBlock(controls, name);
		else if (blockType.equals("text"))
			cb_new = ColoredText.newBlock(controls);
		else {
			SmTrace.lg(String.format("EMBlockBase.newBlock Unsupported block type %s",
					blockType));
		}
		return cb_new;
	}

	
	/**
	 * Create new block
	 */
	public static EMBlockBase newBlock(String blockType, EMBox3D box, Color color) {
		color = EMBlockBase.colorCheck(color, "newBlockBase");
		EMBlockBase cb_new = null;
		if (blockType.equals("box"))
			cb_new = new ColoredBox(box, color);
		else if (blockType.equals("ball"))
			cb_new = new ColoredBall(box, color);
		else if (blockType.equals("cone"))
			cb_new = new ColoredCone(box, color);
		else if (blockType.equals("cylinder"))
			cb_new = new ColoredCylinder(box, color);
		else if (blockType.equals("text"))
			cb_new = new ColoredText(box, color);
		else {
			SmTrace.lg(String.format("EMBlockBase.newBlock Unsupported block type %s",
					blockType));
		}
		return cb_new;
	}

	
	/**
	 * Create new block, position type
	 */
	public static EMBlockBase newBlock(String blockType,
			Point3D position,
			Point3D target,
			Vector3D up) {
		EMBlockBase cb_new = null;
		if (blockType.equals("eye")) {
			cb_new = new ColoredEye(position, target, up, null);
		}
		else {
			SmTrace.lg(String.format("Unsupported block position type %s",
					blockType));
		}
		
		return cb_new;
	}

	
	/**
	 * Set Id
	 * Generally to track this operation
	 */
	public void setId(int iD) {
		this.iD = iD;
	}
	
	
	/**
	 * Create new block following based on existing block
	 * @throws EMBlockError 
	 */
	public static EMBlockBase newBlock(EMBlockBase cb) {
		EMBlockBase cb2 = newBlock(cb.blockType(), cb.box, cb.color);
		cb2.setId(cb.iD());
		return cb2;
	}

	/**
	 * Default blocks
	 * @throws EMBlockError 
	 */
	public static EMBlockBase newBlock() {
		EMBlockBase cb = EMBlockBase.newBlock(defaultAtt.blockType(), defaultAtt.box, defaultAtt.color); 
		return cb;
	}


	/**
	 * @throws EMBlockError 
	 */
	public static EMBlockBase newBlock(String blockType, EMBox3D box) throws EMBlockError {
		EMBlockBase cb = EMBlockBase.newBlock(blockType, box, defaultAtt.color); 
		return cb;
	}

	/**
	 * Compare block
	 * @return -1, 0, 1
	 * Overridden if particular type requires more testing
	 */
	public int cmp(EMBlock cb) {
		if (!blockType().equals(cb.blockType())) {
			return blockType().compareTo(cb.blockType());
		}
		int ccmp = colorCompare(cb);
		if (ccmp != 0)
			return ccmp;
		
		int boxcmp = box.cmp(cb.getBox());
		if (boxcmp != 0)
			return boxcmp;
		
		return 0;
				
	}

	/**
	 * Compare block colors
	 */
	public int colorCompare(EMBlock cb) {
		float[] cc = getColorComponents();
		float[] ccb = cb.getColorComponents();
		for (int i = 0; i < cc.length; i++) {
			float col = cc[i];
			float col2 = ccb[i];
			if (col != col2)
				return (int) java.lang.Math.signum(col - col2);
		}
		return 0;
	}
	
	
	/**
	 * Deep copy to insulate from subsequent modifications
	 * No change to table entries
	 */
	public static EMBlockBase copy(EMBlockBase cb) {
		EMBlockBase cb_copy = null;
		try {
			cb_copy = newBlock(cb.blockType(), cb.box);
		} catch (EMBlockError e) {
			SmTrace.lg(String.format("newBlock failed"));
			e.printStackTrace();
			System.exit(1);
		}
		cb_copy.setId(cb.iD());
		return cb_copy;
	}
	
	
	/**
	 * duplicate block
	 * @throws EMBlockError 
	 */
	public EMBlockBase duplicate() throws EMBlockError {
		SmTrace.lg(String.format("Un-overidden duplicate - possible error: %s",
				this.blockType()));
		return null;
	}
	
	
	/**
	 * duplicate block
	 * @throws EMBlockError 
	 */
	public EMBlockBase duplicate(String blockType) throws EMBlockError {
		SmTrace.lg(String.format("Un-overidden duplicate - possible error: %s",
				this.blockType()));
		return null;
	}

	/**
	 * Limit color range 0-1
	 */
	public float colorLimit(float val, String name) {	// Returns limited value
		if (val < 0) {
			SmTrace.lg(String.format("%s (%.2f) is out of range 0-1", name, val));
			return 0f;
		}
		if (val > 1) {
		SmTrace.lg(String.format("%s (%.2f) is out of range 0-1", name, val));
			return 1f;
		}
		return val;
	}
	/**
	 * Vector adjust color
	 */
	public Color colorAdj(float red, float green, float blue, float alpha) {
		float cc[] = getColorComponents();
		cc[0] += red;
		cc[1] += green;
		cc[2] += blue;
		cc[3] += alpha;
		Color cl2 = new Color(
				colorLimit(cc[0], "red"),
				colorLimit(cc[1], "green"),
				colorLimit(cc[2], "blue"),
				colorLimit(cc[3], "alpha"));
		setColor(cl2);
		return getColor();
	}

	/**
	 * Adjust rgb, leaving alpha unchanged
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	public Color colorAdj(float red, float green, float blue) {
		float alpha = getAlpha();
		return colorAdj(red, green, blue, alpha);
	}
	
	
	/**
	 * @param cb - existing block
	 * @return new block with these attributes
	 * @throws EMBlockError 
	 */
	public EMBlockBase duplicate(EMBlockBase cb) throws EMBlockError {
		return newBlock(cb);
	}
	
	/**
	 * @param blockType type string default: our type
	 * @param atpoint base point default: our base point
	 * @param size	containing vector default: our size
	 * @param color	color 4part color default: our color 
	 * @return new block with these attributes
	 * @throws EMBlockError 
	 */
	public EMBlockBase duplicate(String blockType,
			Point3D atpoint,
			Vector3D size,
			Color color) throws EMBlockError {
		if (blockType == null)
			blockType = blockType();
		if (atpoint == null)
			atpoint = getBasePoint();
		if (size == null)
			size = getSize();
		if (color == null)
			color = getColor();
		EMBox3D box = getBox();
		
		return newBlock(blockType, box, color);
		
	}

	
	/**
	 * Get position - center point of x,y,z
	 */
	public Point3D getPos() {
		return getCenter();
	}

	/**
	 * Get effective radius
	 */
	public float getRadius() {
		return box.getRadius();
	}
	
	/**
	 * Get size - vector of x,y,z extent
	 */
	public Vector3D getSize() {
		
		Point3D minp = getMin();
		Point3D maxp = getMax();
		
		Vector3D diag = Point3D.diff(maxp, minp);
		Vector3D size = new Vector3D(Math.abs(diag.x()), Math.abs(diag.y()), Math.abs(diag.z()));
		return size;
	}

	/**
	 * Unique block id
	 */
	public int iD() {
		return iD;
	}
	
	/**
	 * Check if ok block
	 */
	public boolean isOk() {
		return isOk;
	}
	
	// Overridden by all nontrivial blocks
	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		SmTrace.lg("draw - ignored");
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

	/**
	 * 
	 * @param new_size - new size with center in place
	 */
	public void resize(
		Vector3D adj_size
	) {
		Point3D center = box.getCenter();
		float radius = box.getRadius();
		float adj = adj_size.x();
		if (adj_size.y() > adj)
			adj = adj_size.y();
		if (adj_size.z() > adj)
			adj = adj_size.z();
		box = new EMBox3D(center, radius+adj);
	}

	// Overridden when necessary
	public void translate(Vector3D translation ) {
		box.translate(translation);
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

	public String blockType() {
		return "UNKNOWN_BASE_BLOCK_TYPE";
	}


	// Not a box so return a bounding box
	public EMBox3D getBox() {
		return boundingSphere();
	}


	/**
	 * Get oriented bounding box
	 * @return
	 */
	// Overridden for not trivial blocks
	public OrientedBox3D getOBox() {
		SmTrace.lg("getOBox - not overridden");
		return new OrientedBox3D(getCenter(), new Vector3D(0,0,0), EMBox3D.UP);
	}
	
	public Point3D getCenter() {
		return getBox().getCenter();
	}

	public Point3D getPosition() {
		return getCenter();
	}
	
	public Point3D getTarget() {
		return target;
	}
	
	public Vector3D getUp() {
		return box.getUp();
	}

	public void setCenter(Point3D center) {
		box.setCenter(center);
	}

	
	/**
	 * set/change radius
	 */
	public void setRadius(float radius) {
		box.setRadius(radius);
	}

	public void setUp(Vector3D up) {
		box.setUp(up);
	}
	
	
	/**
	 * Enlarge to contain box
	 * @param box
	 */
	public void bound(EMBox3D box) {
		this.box.bound(box);
	}

	/**
	 * Enlarge to contain box
	 * @param box
	 */
	public void bound(Point3D pt) {
		this.box.bound(pt);
	}
	
	public EMBox3D boundingBox() {
		if (box == null) {
			box = new EMBox3D();
		}
		return box;
	}
	
	public EMBox3D boundingSphere() {
		if (box == null) {
			box = new EMBox3D();
		}
		return box.boundingSphere();
	}
	
						// Overridden by all nontrivial blocks
	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		boolean allowIntersectionEvenIfRayOriginatesInsideSphere
	) {
		SmTrace.lg("??? intersects");
		return false;		
	}
	
								// Overridden by all nontrivial blocks
	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		SmTrace.lg("??? intersects(ray, intersection, normalAtIntersection) %s NOT OVERIDING", blockType());
		return false;		
	}
	

	public boolean isSelected() {
		return isSelected;
	}

	public boolean setSelected(boolean state) {
		if (SmTrace.trace("selectOn")) {
			if (state)
				SmTrace.lg(String.format("selectOn: %d %s", iD(), blockType()));
		}
		if (SmTrace.trace("selectOff")) {
			if (!state)
				SmTrace.lg(String.format("selectOff: %d %s", iD(), blockType()));
		}
			

		isSelected = state;
		return state;
	}

	public boolean toggleSelected() {
		return setSelected(!isSelected());
	}

	/**
	 *  Textual representation
	 */
	public String toString() {
		String str = String.format("[%d]%s", iD, blockType());
		return str;
		
	}
	/**
	 * Inherent color to all blocks
	 * @return
	 */
	public Color getColor() {
		if (color == null)
			color = ControlOfView.nextColor();
		return color;
	}

	public Color setColor(Color color) {
		this.color = color;
		return color;
	}

	/**
	 * set color via components
	 * @param red
	 * @param green
	 * @param blue
	 * @return color
	 */
	public Color setColor(float red, float green, float blue) {
		Color new_color = new Color(red, green, blue);
		return setColor(new_color);
	}

	/**
	 * place block
	 * @param pt - position
	 */
	public void setPosition(Point3D pt) {
		Vector3D move = Point3D.diff(pt, getPosition());
		translate(move);
	}
	
	
	//base function for blocks
	public float setRed(float value) {
		color = new Color(value, color.getGreen(), color.getBlue(), color.getAlpha());
		return value;
	}

	public float[] getColorComponents() {
		Color cl = getColor();
		float cc[] = new float[4];
		cl.getColorComponents(cc);
		return cc;
	}
	
	public float getRed() {
		float cc[] = getColorComponents();
		return cc[0];
	}

	public float getGreen() {
		float cc[] = getColorComponents();
		return cc[1];
	}

	public float getBlue() {
		float cc[] = getColorComponents();
		return cc[2];
	}

	public float getAlpha() {
		float cc[] = getColorComponents();
		return cc[3];
	}

	public Point3D getMin() {
		if (box == null) {
			box = new EMBox3D();
		}
		return box.getMin();
	}


	public float getMinX() {
		if (box == null) {
			box = new EMBox3D();
		}
		return box.getMinX();
	}


	public float getMinY() {
		if (box == null) {
			box = new EMBox3D();
		}
		return box.getMinY();
	}


	public float getMinZ() {
		if (box == null) {
			box = new EMBox3D();
		}
		return box.getMinZ();
	}

	public Point3D getMax() {
		if (box == null) {
			box = new EMBox3D();
		}
		return box.getMax();
	}


	public float getMaxX() {
		if (box == null) {
			box = new EMBox3D();
		}
		return box.getMaxX();
	}


	public float getMaxY() {
		if (box == null) {
			box = new EMBox3D();
		}
		return box.getMaxY();
	}


	public float getMaxZ() {
		if (box == null) {
			box = new EMBox3D();
		}
		return box.getMaxZ();
	}

	

	// Overridden for types with name, eg filename
	public String getName() {
		return "";
	}

	// Overridden by all nontrivial blocks
	public Vector3D getDiagonal() {
		if (box == null) {
			box = new EMBox3D();
		}
		return box.getDiagonal();
	}
	
	public int getViewerLevel() {
		return viewerLevel;
	}

	public void setViewerLevel(int viewerLevel) {
		this.viewerLevel = viewerLevel;
	}


}
