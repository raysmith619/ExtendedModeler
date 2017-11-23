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
	private static EMBlockGroup blocks;	// MUST be set BEFORE block  use
	private EMBlockBase baseBlock;	
		
	/**
	 * Setup for generated group access
	 * @param group
	 */
	public static void setGenerated(EMBlockGroup group) {
		blocks = group;
	}

	public EMBlock(EMBlock cb) {
		setBase(cb);
		addBlock(cb);
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
	public EMBlock(EMBlockBase cb_base) {
		this.baseBlock = cb_base;
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
	public static EMBlock newBlock(String blockType) throws EMBlockError {
		EMBlockBase cb_base = EMBlockBase.newBlock(blockType); 
		return addBlock(cb_base);
	}


	/**
	 * @throws EMBlockError 
	 */
	public static EMBlock newBlock(String blockType, AlignedBox3D box) throws EMBlockError {
		EMBlockBase cb_base = EMBlockBase.newBlock(blockType, box); 
		return addBlock(cb_base);
	}


	/**
	 * @throws EMBlockError 
	 */
	public static EMBlock newBlock(String blockType, AlignedBox3D box, Color color) {
		EMBlockBase cb_base = EMBlockBase.newBlock(blockType, box, color);
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
		AlignedBox3D box = getBox();
		
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
	public AlignedBox3D getBox() {
		return baseBlock.getBox();
	}
	
	public Point3D getCenter() {
		return baseBlock.getCenter();
	}
	
	public AlignedBox3D boundingBox() {
		return baseBlock.boundingBox();
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
		return baseBlock.intersects(ray, intersection, normalAtIntersection);
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
	public static void setDefaults(String blockType, AlignedBox3D box, Color color) throws EMBlockError {
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

	public Point3D getMax() {
		return baseBlock.getMax();
	}
	
	// Overridden by all nontrivial blocks
	public Vector3D getDiagonal() {
		return baseBlock.getDiagonal();
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
	public void adjustFromControls(ControlsOfView controls, EMBCommand bcmd) throws EMBlockError {
		String[] ctl_names = controls.getControlNames();
		for (int i = 0; i < ctl_names.length; i++) {
			String ctl_name = ctl_names[i];
			ControlOf ctl = controls.getControl(ctl_name);
			if (ctl == null)
				continue;		// Ignore if not up
			
			ctl.adjustFromControl(this, bcmd);
		}
	}

	
	/**
	 * Set block from controls
	 * @throws EMBlockError 
	 */
	public void setFromControls(ControlsOfView cov) throws EMBlockError {
		baseBlock.setFromControls(cov);
	}

	/**
	 * Set controls based on current state
	 */
	public void setControls(ControlsOfView cov) {
		baseBlock.setControls(cov);
	}
	

	public void setControl(ControlOfPlacement ctl) {
		baseBlock.setControl(ctl);
	}

}
