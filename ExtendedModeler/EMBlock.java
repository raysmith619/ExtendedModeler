package ExtendedModeler;
import java.awt.Color;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import smTrace.SmTrace;

/*
 * Contains some block
 */
public class EMBlock{
	private static int blockId = 0;		// Unique block identifier, used as key
	private static EMBlockGroup blocks;	// The group to which this block
										// belongs, MUST be set BEFORE block  use
	boolean isAggrigate = false;		// true -> iff this is an aggrigate.
	boolean partOfAggrigate = false;	// true -> iff this is included in an aggrigate
	private EMBlockBase baseBlock;	
	
	public int getViewerLevel() {
		return baseBlock.getViewerLevel();
	}

	public void setViewerLevel(int viewerLevel) {
		baseBlock.setViewerLevel(viewerLevel);
	}

	/**
	 * Setup for generated group access
	 * @param group
	 */
	public static void setGenerated(EMBlockGroup group) {
		blocks = group;
	}

	/**
	 * place block
	 * @param pt - position
	 */
	public void setPosition(Point3D pt) {
		baseBlock.setPosition(pt);
	}
	
	
	public EMBlock(EMBlock cb) {
		this(cb.baseBlock);
	}

	/**
	 * Get base
	 */
	public EMBlockBase getBase() {
		return baseBlock;
	}

	/**
	 * Setup base
	 */
	EMBlock setBase(EMBlock cb) {
		this.baseBlock = cb.baseBlock;
		return cb;
	}
	
	
	/**
	 * From base, with
	 * NO change to iD
	 * NO entry to blocks
	 */
	public EMBlock(EMBlockBase base) {
		this.baseBlock = base;
	}
	
	public void EMBlock_OBSOLETE(EMBlockBase cb_base) {
		String type = cb_base.blockType();
		switch (type) {
			default:
				SmTrace.lg(String.format("EMBlock UNRECOGNIZED base: %s", type));
				System.exit(1);
				break;
				
			case "box":
				this.baseBlock = new ColoredBox(cb_base);
				break;
				
			case "ball":
				this.baseBlock = new ColoredBall(cb_base);
				break;
				
			case "cylinder":
				this.baseBlock = new ColoredCylinder(cb_base);
				break;
				
			case "cone":
				this.baseBlock = new ColoredCone(cb_base);
				break;
				
			case "eye":
				this.baseBlock = new ColoredEye(cb_base);
				break;
				
			case "text":
				this.baseBlock = new ColoredText(cb_base);
				break;
		}
	}
	
	
	/**
	 * Add block to database
	 */
	public static EMBlock addBlock(EMBlock cb) {
		cb.baseBlock.setId(nextId());
		blocks.putBlock(cb);
		colorCheck(cb.baseBlock.color, "EMBlock");
		return cb;
	}

	public static int nextId() {
		return ++blockId;
	}
	
	/**
	 * Set our id
	 * For now set baseBlock id
	 */
	public int setId(int iD) {
		baseBlock.setId(iD);
		return iD();
	}
	
	
	/**
	 * Convert base to block, then add block to database
	 */
	public static EMBlock addBlock(EMBlockBase cb_base) {
		EMBlock cb = new EMBlock(cb_base);
		if (cb.blockType().contains("U")) {
			SmTrace.lg(String.format("Suspicious blockType: %s", cb.blockType()));
			System.exit(1);
		}
		return addBlock(cb);
	}

	
	
	
	/**
	 * Check if color is too dark
	 */
	public static Color colorCheck(Color color, String tag) {
		if (color == null)
			return color;			// Ignore
		
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

	
	/* Create block whos attributes are the
	 * difference betwee this block and the specified block
	 */
	public EMBlock changeBlock(EMBlock toBlock) {
		return null;		/// Won't  use this now
							/// Using prev + new instead
	}
	
	
	/**
	 * Create new block following based on existing block
	 * @throws EMBlockError 
	 */
	public static EMBlock newBlock(EMBlock cb) throws EMBlockError {
		EMBlock cb2 = newBlock(cb.blockType(), cb.getBox(), cb.getColor());
		return cb2;
	}

	/**
	 * Default blocks
	 * @throws EMBlockError 
	 */
	public static EMBlock newBlock() {
		EMBlockBase cb_base = EMBlockBase.newBlock(); 
		return addBlock(cb_base);
	}


	/**
	 * @throws EMBlockError 
	 */
	public static EMBlock newBlock(String blockType, ControlsOfScene controls, String name) throws EMBlockError {
		EMBlockBase cb_base = EMBlockBase.newBlock(blockType, controls, name);
		EMBlock cb = new EMBlock(cb_base);
		return addBlock(cb);
	}


	/**
	 * @throws EMBlockError 
	 */
	public static EMBlock newBlock(String blockType, EMBox3D box) throws EMBlockError {
		EMBlockBase cb_base = EMBlockBase.newBlock(blockType, box); 
		return addBlock(cb_base);
	}


	/**
	 * @throws EMBlockError 
	 */
	public static EMBlock newBlock(String blockType, EMBox3D box, Color color) {
		EMBlockBase cb_base = EMBlockBase.newBlock(blockType, box, color);
		if (cb_base.blockType().contains("U")) {
			SmTrace.lg(String.format("Suspicious blockType: %s", cb_base.blockType()));
		}
		return addBlock(cb_base);
	}

	public static EMBlock newBlock(String blockType, Point3D position,
				Point3D target,
				Vector3D up) {
		EMBlockBase cb_base = EMBlockBase.newBlock(blockType, position, target, up);
		return addBlock(cb_base);
		
	}


	/**
	 * Deep copy to insulate from subsequent modifications
	 * No change to table entries
	 */
	public EMBlock copy() {
		EMBlock cb_copy = new EMBlock(baseBlock);
		return cb_copy;
	}
	
	
	/**
	 * duplicate block
	 * @throws EMBlockError 
	 */
	public EMBlock duplicate() throws EMBlockError {
		///return duplicate(null, null, null, null);
		EMBlock cb = new EMBlock(baseBlock);
		addBlock(cb);
		return cb;
	}

	/**
	 * Compare blocks
	 * @return 1, 0, -1
	 */
	public int cmp(EMBlock cb) {
		return baseBlock.cmp(cb);
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
	 * @param blockType type string default: our type
	 * @param atpoint base point default: our base point
	 * @param size	containing vector default: our size
	 * @param color	color 4part color default: our color 
	 * @return new block with these attributes
	 * @throws EMBlockError 
	 */
	public EMBlock duplicate(String blockType,
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
	 * Get size - vector of x,y,z extent
	 */
	public Vector3D getSize() {
		return baseBlock.getSize();
	}

	
	/**
	 * Get position - Point of x,y,z minimum
	 */
	public Point3D getPos() {
		return baseBlock.getPos();
	}

	/**
	 * Unique id
	 */
	public int iD() {
		return baseBlock.iD();
	}
	
	/**
	 * Check if ok block
	 */
	public boolean isOk() {
		return baseBlock.isOk();
	}

	
	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		baseBlock.draw(drawable, expand, drawAsWireframe, cornersOnly);
	}


	public void resize(
		int indexOfCornerToResize, Vector3D translation
	) {
		baseBlock.resize(indexOfCornerToResize, translation);
	}

	/**
	 * 
	 * @param new_size - new size with lower corner in place
	 */
	public void resize(
		Vector3D adj_size
	) {
		baseBlock.resize(adj_size);
	}

	public void translate(Vector3D translation ) {
		baseBlock.translate(translation);
	}

	
	/**
	 * Get base point - currently min x,y,z
	 * @param point
	 */
	public Point3D getBasePoint() {
		return baseBlock.getBasePoint();
	}
	

	/**
	 * Move base corner to new point
	 * @param point
	 */
	public void moveTo(Point3D point ) {
		baseBlock.moveTo(point);
	}

	public String blockType() {
		return baseBlock.blockType();
	};


	// Not a box so return a bounding box
	public EMBox3D getBox() {
		return baseBlock.getBox();
	}
	
	public Point3D getCenter() {
		return baseBlock.getCenter();
	}

	public Point3D getPosition() {
		return baseBlock.getPosition();
	}
	
	public EMBox3D boundingBox() {
		return baseBlock.boundingBox();
	}
	
	public EMBox3D boundingSphere() {
		return baseBlock.boundingSphere();
	}
	
	public Point3D getTarget() {
		return baseBlock.getTarget();
	}
	
	public Vector3D getUp() {
		return baseBlock.getUp();
	}
	
	public void setUp(Vector3D up) {
		baseBlock.setUp(up);
	}
	
						// Overridden by all nontrivial blocks
	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		boolean allowIntersectionEvenIfRayOriginatesInsideSphere
	) {
		return baseBlock.intersects(ray, intersection,
				allowIntersectionEvenIfRayOriginatesInsideSphere);	
	}
	
								// Overridden by all nontrivial blocks
	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		boolean ret = baseBlock.intersects(ray, intersection, normalAtIntersection);
		SmTrace.lg(String.format("EMBlock.intersects(ray:%s, intersection(%s), normalAtIntersection(%s)",
				ray, intersection, normalAtIntersection), "intersection");
		if (normalAtIntersection.lengthSquared() == 0) {		
			normalAtIntersection = new Vector3D(1,0,0);		///TFD
			SmTrace.lg(String.format("EMBlock.intersects FORCED (ray:%s, intersection(%s), normalAtIntersection(%s)",
					ray, intersection, normalAtIntersection), "intersection");
		}
		return ret;
	}
	

	public boolean isSelected() {
		return baseBlock.isSelected();
	}

	public boolean setSelected(boolean state) {
		return baseBlock.setSelected(state);
	}

	public boolean toggleSelected() {
		return baseBlock.toggleSelected();
	}

	/**
	 *  Textual representation
	 */
	public String toString() {
		return baseBlock.toString();
	}
	
	
	/**
	 * Inherent color to all blocks
	 * @return
	 */
	public Color getColor() {
		return baseBlock.getColor();
	}

	public Color setColor(Color color) {
		return baseBlock.setColor(color);
	}

	/**
	 * set color via components
	 * @param red
	 * @param green
	 * @param blue
	 * @retur color
	 */
	public Color setColor(float red, float green, float blue) {
		return baseBlock.setColor(red, green, blue);
	}

	
	

	/**
	 * Setup for defaults
	 * @throws EMBlockError 
	 */
	public static void setDefaults(String blockType, EMBox3D box, Color color) throws EMBlockError {
		EMBlockBase.setDefaults(blockType, box, color);
	}

	
	//base function for blocks
	public float setRed(float value) {
		return baseBlock.setRed(value);
	}

	public float[] getColorComponents() {
		return baseBlock.getColorComponents();
	}
	
	public float getRed() {
		return baseBlock.getRed();
	}

	public float getGreen() {
		return baseBlock.getGreen();
	}

	public float getBlue() {
		return baseBlock.getBlue();
	}

	public float getAlpha() {
		float cc[] = getColorComponents();
		return cc[3];
	}

	public Point3D getMin() {
		return baseBlock.getMin();
	}

	public float getMinX() {
		return baseBlock.getMinX();
	}

	public float getMinY() {
		return baseBlock.getMinY();
	}

	public float getMinZ() {
		return baseBlock.getMinZ();
	}

	public Point3D getMax() {
		return baseBlock.getMax();
	}

	public float getMaxX() {
		return baseBlock.getMaxX();
	}

	public float getMaxY() {
		return baseBlock.getMaxY();
	}

	public float getMaxZ() {
		return baseBlock.getMaxZ();
	}
	
	// Overridden by all nontrivial blocks
	public Vector3D getDiagonal() {
		return baseBlock.getDiagonal();
	}

	// Overridden for types with name, eg filename
	public String getName() {
		return "";
	}

	/**
	 * Adjust from text control
	 * @throws EMBlockError 
	 */
	public void adjustFromControl(ControlOfText ctl, EMBCommand bcmd) throws EMBlockError {
		baseBlock.adjustFromControl(ctl, bcmd);
	}

	/**
	 * Adjust from control settings
	 * @throws EMBlockError 
	 */
	public void adjustFromControls(ControlsOfScene controls, EMBCommand bcmd) throws EMBlockError {
		String[] ctl_names = controls.getControlNames();
		for (int i = 0; i < ctl_names.length; i++) {
			String ctl_name = ctl_names[i];
			ControlOfScene ctl = controls.getControl(ctl_name);
			if (ctl == null)
				continue;		// Ignore if not up
			
			ctl.adjustFromControl(this, bcmd);
		}
	}

	
	/**
	 * Set block from controls
	 * @throws EMBlockError 
	 */
	public void setFromControls(ControlsOfScene controls) throws EMBlockError {
		baseBlock.setFromControls(controls);
	}

	/**
	 * Set controls based on current state
	 */
	public void setControls(ControlsOfView cov) {
		baseBlock.setControls(cov);
	}

	/**
	 * Set controls based on current state
	 */
	public void setControls(ControlsOfScene cos) {
		baseBlock.setControls(cos);
	}
	

	public void setControl(ControlOfPlacement ctl) {
		baseBlock.setControl(ctl);
	}

}
