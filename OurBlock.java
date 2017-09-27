import java.awt.Color;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;

/*
 * Contains some block
 */
public class OurBlock{
	private static int blockId = 0;		// Unique block identifier, used as key
	private static OurBlockGroup blocks;	// MUST be set BEFORE block  use
	private OurBlockBase baseBlock;	
		
	/**
	 * Setup for generated group access
	 * @param group
	 */
	public static void setGenerated(OurBlockGroup group) {
		blocks = group;
	}

	public OurBlock(OurBlock cb) {
		setBase(cb);
		addBlock(cb);
	}

	/**
	 * Setup base
	 */
	OurBlock setBase(OurBlock cb) {
		this.baseBlock = cb.baseBlock;
		return cb;
	}
	
	
	/**
	 * From base, with
	 * NO change to iD
	 * NO entry to blocks
	 */
	public OurBlock(OurBlockBase cb_base) {
		this.baseBlock = cb_base;
	}
	
	
	/**
	 * Add block to database
	 */
	public static OurBlock addBlock(OurBlock cb) {
		cb.baseBlock.setId(++blockId);
		blocks.putBlock(cb);
		colorCheck(cb.baseBlock.color, "OurBlock");
		return cb;
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
	public static OurBlock addBlock(OurBlockBase cb_base) {
		OurBlock cb = new OurBlock(cb_base);
		if (cb.blockType().contains("U")) {
			System.out.println(String.format("Suspicious blockType: %s", cb.blockType()));
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
			System.out.println(String.format("Dark color %s: [%f,%f,%f]",
					tag, cc[0],cc[1],cc[2]));
			Color newcolor = new Color(255,255,255);		// Set light
			return newcolor;
		}
		return color;				// Unchanged
	}

	/**
	 * Create new block following based on existing block
	 * @throws OurBlockError 
	 */
	public static OurBlock newBlock(OurBlock cb) throws OurBlockError {
		OurBlock cb2 = newBlock(cb.blockType(), cb.getBox(), cb.getColor());
		return cb2;
	}

	/**
	 * Default blocks
	 * @throws OurBlockError 
	 */
	public static OurBlock newBlock() {
		OurBlockBase cb_base = OurBlockBase.newBlock(); 
		return addBlock(cb_base);
	}


	/**
	 * @throws OurBlockError 
	 */
	public static OurBlock newBlock(String blockType) throws OurBlockError {
		OurBlockBase cb_base = OurBlockBase.newBlock(blockType); 
		return addBlock(cb_base);
	}


	/**
	 * @throws OurBlockError 
	 */
	public static OurBlock newBlock(String blockType, AlignedBox3D box) throws OurBlockError {
		OurBlockBase cb_base = OurBlockBase.newBlock(blockType, box); 
		return addBlock(cb_base);
	}


	/**
	 * @throws OurBlockError 
	 */
	public static OurBlock newBlock(String blockType, AlignedBox3D box, Color color) {
		OurBlockBase cb_base = OurBlockBase.newBlock(blockType, box, color);
		return addBlock(cb_base);
	}


	/**
	 * Deep copy to insulate from subsequent modifications
	 * No change to table entries
	 */
	public OurBlock copy() {
		OurBlock cb_copy = new OurBlock(baseBlock);
		return cb_copy;
	}
	
	
	/**
	 * duplicate block
	 * @throws OurBlockError 
	 */
	public OurBlock duplicate() throws OurBlockError {
		return duplicate(null, null, null, null);
	}

	/**
	 * Limit color range 0-1
	 */
	public float colorLimit(float val, String name) {	// Returns limited value
		if (val < 0) {
			System.out.println(String.format("%s (%.2f) is out of range 0-1", name, val));
			return 0f;
		}
		if (val > 1) {
		System.out.println(String.format("%s (%.2f) is out of range 0-1", name, val));
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
	 * @throws OurBlockError 
	 */
	public OurBlock duplicate(String blockType,
			Point3D atpoint,
			Vector3D size,
			Color color) throws OurBlockError {
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
		GL2 gl,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		baseBlock.draw(gl, expand, drawAsWireframe, cornersOnly);
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
	 * @throws OurBlockError 
	 */
	public static void setDefaults(String blockType, AlignedBox3D box, Color color) throws OurBlockError {
		OurBlockBase.setDefaults(blockType, box, color);
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

}
