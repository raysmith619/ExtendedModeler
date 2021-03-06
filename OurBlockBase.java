import java.awt.Color;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;

/*
 * Contains some block
 */
public class OurBlockBase {
	public static final float DEFAULT_SIZE = 0.5f;
	public static final float DEFAULT_ALPHA = 0.5f;
	int iD;				// Unique identifier
///	public Object block = null;
	
						// Universal object traits
	public AlignedBox3D box;	// Bounding box
	protected Color color;		// Object color
	private boolean isSelected = false;
	private static OurBlockBase defaultAtt;	// Default attributes for creation/access
	public boolean isOk = false;		// Set OK upon successful construction
		

	/**
	 * Setup for defaults
	 * @throws OurBlockError 
	 */
	public static void setDefaults(String blockType, AlignedBox3D box, Color color) throws OurBlockError {
		color = OurBlockBase.colorCheck(color, "setDefaults");
		defaultAtt = OurBlockBase.newBlock(blockType, box, color);		// Use std creation
	}

	public OurBlockBase(OurBlockBase cb) {
		this(cb.box, cb.color);
		this.isOk = cb.isOk();
		this.setId(cb.iD());
	}


	/**
	 * Base constructor
	 */
	public OurBlockBase(AlignedBox3D box, Color color) {
		
		this.box = box;
		this.color = color;
		colorCheck(color, "OurBlock");
	}

	
	/**
	 * Copy
	 * "Deep-enough" copy to protect against subsequent modifications
	 */
	public OurBlockBase copy() {
		OurBlockBase cb_copy = new OurBlockBase(this);
		return cb_copy;
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
	 * Create new block
	 */
	public static OurBlockBase newBlock(String blockType, AlignedBox3D box, Color color) {
		color = OurBlockBase.colorCheck(color, "newBlockBase");
		OurBlockBase cb_new = null;
		if (blockType.equals("box"))
			cb_new = new ColoredBox(box, color);
		else if (blockType.equals("ball"))
			cb_new = new ColoredBall(box, color);
		else if (blockType.equals("cone"))
			cb_new = new ColoredCone(box, color);
		else if (blockType.equals("cylinder"))
			cb_new = new ColoredCylinder(box, color);
		else {
			System.out.println(String.format("Unsupported block type %s",
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
	 * @throws OurBlockError 
	 */
	public static OurBlockBase newBlock(OurBlockBase cb) {
		OurBlockBase cb2 = newBlock(cb.blockType(), cb.box, cb.color);
		cb2.setId(cb.iD());
		return cb2;
	}

	/**
	 * Default blocks
	 * @throws OurBlockError 
	 */
	public static OurBlockBase newBlock() {
		OurBlockBase cb = OurBlockBase.newBlock(defaultAtt.blockType(), defaultAtt.box, defaultAtt.color); 
		return cb;
	}


	/**
	 * @throws OurBlockError 
	 */
	public static OurBlockBase newBlock(String blockType) throws OurBlockError {
		OurBlockBase cb = OurBlockBase.newBlock(blockType, defaultAtt.box, defaultAtt.color); 
		return cb;
	}


	/**
	 * @throws OurBlockError 
	 */
	public static OurBlockBase newBlock(String blockType, AlignedBox3D box) throws OurBlockError {
		OurBlockBase cb = OurBlockBase.newBlock(blockType, box, defaultAtt.color); 
		return cb;
	}

	
	/**
	 * Deep copy to insulate from subsequent modifications
	 * No change to table entries
	 */
	public static OurBlockBase copy(OurBlockBase cb) {
		OurBlockBase cb_copy = null;
		try {
			cb_copy = newBlock(cb.blockType(), cb.box);
		} catch (OurBlockError e) {
			System.out.println(String.format("newBlock failed"));
			e.printStackTrace();
			System.exit(1);
		}
		cb_copy.setId(cb.iD());
		return cb_copy;
	}
	
	
	/**
	 * duplicate block
	 * @throws OurBlockError 
	 */
	public OurBlockBase duplicate() throws OurBlockError {
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
	public OurBlockBase duplicate(String blockType,
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
		Point3D minp = box.getMin();
		Point3D maxp = box.getMax();
		
		Vector3D size = Point3D.diff(maxp, minp);
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
		GL2 gl,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		System.out.println("draw - ignored");
	}


	public void resize(
		int indexOfCornerToResize, Vector3D translation
	) {
		AlignedBox3D oldBox = getBox();
		box = new AlignedBox3D();

		// One corner of the new box will be the corner of the old
		// box that is diagonally opposite the corner being resized ...
		 box.bound( oldBox.getCorner( indexOfCornerToResize ^ 7 ) );

		// ... and the other corner of the new box will be the
		// corner being resized, after translation.
		box.bound( Point3D.sum( oldBox.getCorner( indexOfCornerToResize ), translation ) );
	}

	/**
	 * 
	 * @param new_size - new size with lower corner in place
	 */
	public void resize(
		Vector3D adj_size
	) {
		Point3D min = getMin();
		Point3D max = getMax();
		Point3D new_max = Point3D.sum(max, adj_size);
		box = new AlignedBox3D(min, new_max);
	}

	public void translate(Vector3D translation ) {
		AlignedBox3D oldBox = getBox();
		box = new AlignedBox3D(
			Point3D.sum( oldBox.getMin(), translation ),
			Point3D.sum( oldBox.getMax(), translation )
		);
	}

	
	/**
	 * Get base point - currently min x,y,z
	 * @param point
	 */
	public Point3D getBasePoint() {
		AlignedBox3D box = getBox();
		return box.getMin();
	}
	

	/**
	 * Move base corner to new point
	 * @param point
	 */
	public void moveTo(Point3D point ) {
		AlignedBox3D oldBox = getBox();
		Vector3D vm = Point3D.diff(point, oldBox.getMin());
		translate(vm);
	}

	public String blockType() {
		return "UNKNOWN_BASE_BLOCK_TYPE";
	}


	// Not a box so return a bounding box
	public AlignedBox3D getBox() {
		return boundingBox();
	}
	
	public Point3D getCenter() {
		return getBox().getCenter();
	}
	
	public AlignedBox3D boundingBox() {
		return box.boundingBox();
	}
	
						// Overridden by all nontrivial blocks
	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		boolean allowIntersectionEvenIfRayOriginatesInsideSphere
	) {
		System.out.println("intersects");
		return false;		
	}
	
								// Overridden by all nontrivial blocks
	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		System.out.println("intersects(ray, intersection, normalAtIntersection)");
		return false;		
	}
	

	public boolean isSelected() {
		return isSelected;
	}

	public boolean setSelected(boolean state) {
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
	 * @retur color
	 */
	public Color setColor(float red, float green, float blue) {
		Color new_color = new Color(red, green, blue);
		return setColor(new_color);
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
		return box.getMin();
	}

	public Point3D getMax() {
		return box.getMax();
	}
	
	// Overridden by all nontrivial blocks
	public Vector3D getDiagonal() {
		return box.getDiagonal();
	}

}
