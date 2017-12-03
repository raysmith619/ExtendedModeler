package ExtendedModeler;


/**
 * Block
 * @author raysm
 *
 */
public class EMDisplay {
	private String tag;				// Tag to identify display state
	private EMBlockGroup blocks;

	public EMDisplay(String tag) {
		this.tag = tag;
		blocks = new EMBlockGroup();
	}

	public EMDisplay(String tag, EMDisplay disp) {
		this(tag);
		 this.putBlocks(disp.getBlocks());
	}

	
	/**
	 * Get latest stored - null if none
	 */
	public EMBlock getNewestBlock() {
		return blocks.getNewestBlock();
	}

	
	/**
	 * Get display tag
	 */
	public String getTag() {
		return tag;
	}

	
	/**
	 * If the display is equal
	 * Our type of equals
	 */
	public boolean equals(EMDisplay disp) {
		int disp_cmp = cmp(disp);
		if (disp_cmp == 0)
			return true;
		
		return false;
	}
	
	/**
	 * Compare this display with another
	 * Compare the components in ascending id order
	 * @param other display
	 * @return -1, 0, 1
	 */
	public int cmp(EMDisplay other) {
		int[] ids = getIds();
		int[] other_ids = other.getIds();
		int lencmp = Integer.compare(ids.length, other_ids.length);
		if (lencmp != 0)
				return lencmp;	// lengths differ
		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			EMBlock cb = getBlock(id);
			int other_id = other_ids[i];
			EMBlock other_cb = other.getBlock(other_id);
			int cbcmp = cb.cmp(other_cb);
			if (cbcmp != 0)
				return cbcmp;
		}
		return 0;
	}
	
	/**
	 * Get unique block
	 * null if none
	 */
	public EMBlock getBlock(int id) {
		return blocks.getBlock(id);
	}
	
	/**
	 * Get blocks
	 * NO Copy
	 */
	public EMBlockGroup getBlocks() {
		return blocks;
	}

	
	/**
	 * Get array of ids(indexes)
	 */
	public int[] getIds( ) {
		return blocks.getIds();
	}

	
	/**
	 * Test if group is empty
	 */
	public boolean isEmpty() {
		return blocks.isEmpty();
	}
	
	/**
	 * Insert unique block
	 * Copy, in case of subsequent modification
	 */
	public EMBlock putBlock(EMBlock cb) {
		return blocks.putBlock(cb);
	}

	/**
	 * Insert group of blocks
	 */
	public EMBlockGroup putBlocks(EMBlockGroup group) {
		for (int id : group.getIds()) {
			putBlock(group.getBlock(id));
		}
		return group;
	}
	
	/**
	 * Remove block from group
	 */
	public EMBlock removeBlock(int id) {
		return blocks.removeBlock(id);
	}
	
	
	/**
	 * Remove all blocks from group
	 * - Initializes as empty
	 */
	public void removeAllBlocks() {
		blocks.removeAllBlocks();
	}

	/**
	 * Descriptive string
	 */
	public String toString() {
		String str = "";
		for (int id : this.getIds()) {
			if (!str.equals(""))
				str += ", ";
			str += getBlock(id);
		}
		return str;
	}

}
